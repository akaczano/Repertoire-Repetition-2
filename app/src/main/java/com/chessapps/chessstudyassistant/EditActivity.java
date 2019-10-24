package com.chessapps.chessstudyassistant;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chessapps.chessstudyassistant.model.ChessFile;
import com.chessapps.chessstudyassistant.model.Variation;
import com.chessapps.chessstudyassistant.observer.MoveInputListener;
import com.chessapps.chessstudyassistant.model.MoveSnapshot;
import com.chessapps.chessstudyassistant.observer.MoveSelectionListener;
import com.chessapps.chessstudyassistant.view.BoardView;
import com.chessapps.chessstudyassistant.view.NotationDisplay;

import java.io.File;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity implements MoveInputListener, MoveSelectionListener, View.OnClickListener, View.OnLongClickListener {

    private BoardView boardView;

    private File file;
    private ChessFile chessFile;
    private NotationDisplay notationDisplay;

    private TextView notesLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_edit);

        this.file = new File(getIntent().getStringExtra("file_name"));
        ChessFile chessFile = ChessFile.loadFromPGN(file);
        this.chessFile = chessFile;


        this.boardView = findViewById(R.id.boardView);
        boardView.addMoveInputListener(this);
        boardView.updatePosition(this.chessFile.getCurrentPosition());
        getSupportActionBar().hide();

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
        backButton.setLongClickable(true);
        backButton.setOnLongClickListener(this);
        backButton.setMinimumWidth(backButton.getHeight());

        Button forwardButton = findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(this);
        forwardButton.setMinimumWidth(forwardButton.getHeight());
        forwardButton.setLongClickable(true);
        forwardButton.setOnLongClickListener(this);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        Button deleteButton = findViewById(R.id.delButton);
        deleteButton.setOnClickListener(this);

        Button flipButton = findViewById(R.id.flipButton);
        flipButton.setOnClickListener(this);

        notesLabel = findViewById(R.id.notesLabel);
        notesLabel.setOnClickListener(this);
        notesLabel.setLongClickable(true);
        notesLabel.setOnLongClickListener(this);

        notationDisplay = findViewById(R.id.notationDisplay);
        notationDisplay.setVariation(chessFile.getVariation());
        notationDisplay.setListener(this);
        notationDisplay.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getY() <= this.boardView.getHeight())
            this.boardView.touchUpdate(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean moveEntered(MoveSnapshot move, boolean legal) {
        boolean result = this.chessFile.enterMove(move, legal);
        updateDisplay();
        return result;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            chessFile.goBackward();
            updateDisplay();
        }
        else if (v.getId() == R.id.forwardButton) {
            if (chessFile.getCurrentVariations().size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose line");

                String[] names = new String[chessFile.getCurrentVariations().size()+1];
                names[0] = chessFile.getVariation().find(chessFile.getCurrentVariation())
                        .getMainLine().get(chessFile.getCurrentIndex()).getNotation();
                for (int i = 1; i < names.length; i++) {
                    names[i] = chessFile.getCurrentVariations().get(i-1).getMainLine().get(0).getNotation();
                }

                builder.setSingleChoiceItems(names, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        chessFile.goForward(which);
                        updateDisplay();
                    }
                });

                builder.show();
            } else {
                chessFile.goForward(-1);
                updateDisplay();
            }

        } else if (v.getId() == R.id.saveButton) {
            chessFile.writeToPGN(this.file);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.delButton) {
            chessFile.delete();
            updateDisplay();
        } else if (v.getId() == R.id.flipButton) {
            boardView.flip();
        }
    }

    @Override
    public void moveSelected(int variationID, int moveIndex) {
        this.chessFile.selectMove(variationID, moveIndex);
        this.notationDisplay.selectMove(chessFile.getCurrentVariation(), chessFile.getCurrentIndex() - 1);
        boardView.invalidate();
        notationDisplay.invalidate();
    }

    public void updateDisplay() {
        notationDisplay.selectMove(chessFile.getCurrentVariation(), chessFile.getCurrentIndex() - 1);
        boardView.invalidate();
        notationDisplay.invalidate();
        Variation current = chessFile.getVariation().find(chessFile.getCurrentVariation());
        if (chessFile.getCurrentIndex() > 0) {
            MoveSnapshot move = current.getMainLine().get(chessFile.getCurrentIndex() - 1);
            notesLabel.setText(move.getNotes() == null ? "Add notes" : move.getNotes());
        }
        else {
            notesLabel.setText("");
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.notesLabel) {
            TextView view = (TextView) v;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add notes to current move");
            final EditText input = new EditText(this);
            builder.setView(input);

            builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (chessFile.getCurrentIndex() < 1) {
                        return;
                    }
                    Variation current = chessFile.getVariation().find(chessFile.getCurrentVariation());
                    current.getMainLine().get(chessFile.getCurrentIndex() - 1).setNotes(input.getText().toString());
                    updateDisplay();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        else if (v.getId() == R.id.backButton){
            this.chessFile.goToStart();
        }
        else if (v.getId() == R.id.forwardButton){
            this.chessFile.goToEnd();
        }
        return false;
    }
}
