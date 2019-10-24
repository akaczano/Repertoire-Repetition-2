package com.chessapps.chessstudyassistant;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chessapps.chessstudyassistant.model.ChessFile;
import com.chessapps.chessstudyassistant.model.ChessPosition;
import com.chessapps.chessstudyassistant.model.MoveSnapshot;
import com.chessapps.chessstudyassistant.model.Variation;
import com.chessapps.chessstudyassistant.observer.MoveInputListener;
import com.chessapps.chessstudyassistant.view.BoardView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class QuizActivity extends AppCompatActivity implements MoveInputListener, View.OnClickListener {

    private ArrayList<ChessFile> files;
    private int quizIndex = 0;

    private BoardView boardView;

    private TextView historyLabel;
    private TextView scoreLabel;
    private TextView fileLabel;
    private ProgressBar progressBar;

    private Button nextButton;
    private Button helpButton;
    private Button flipButton;
    private Button quitButton;

    private Button forwardButton;
    private Button backButton;

    private int score = 0;
    private int index = 0;
    private int maxScore = 0;

    private int maxMoveNumber;
    private boolean forWhite;

    private boolean alreadyHelped = false;

    private HashMap<ChessPosition, MoveSnapshot> missed = new HashMap<>();

    private Stack<Variation> variationStack = new Stack<>();

    private int currentVariation = -1;
    private int currentMoveIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        getSupportActionBar().hide();

        files = new ArrayList<>();

        int counter = 0;

        while (getIntent().hasExtra("file_path_" + counter)) {
            ChessFile file = ChessFile.loadFromPGN(new File(getIntent().getStringExtra("file_path_" + counter)));
            files.add(file);
            counter++;
        }

        this.forWhite = getIntent().getBooleanExtra("quiz_white", true);
        this.maxMoveNumber = getIntent().getIntExtra("max_move_number", -1);
        if (!forWhite){
            maxMoveNumber++;
        }

        this.boardView = findViewById(R.id.quizBoard);
        this.boardView.addMoveInputListener(this);
        if (!forWhite) {
            files.get(quizIndex).goForward(-1);
        }
        this.boardView.updatePosition(files.get(quizIndex).getCurrentPosition());

        if (!forWhite) {
            this.boardView.flip();
        }

        this.historyLabel = findViewById(R.id.historyLabel);
        this.scoreLabel = findViewById(R.id.lblScore);
        this.fileLabel = findViewById(R.id.fileLabel);
        this.progressBar = findViewById(R.id.progressBar);

        this.nextButton = findViewById(R.id.nextButton);
        this.nextButton.setOnClickListener(this);

        this.helpButton = findViewById(R.id.helpButton);
        this.helpButton.setOnClickListener(this);

        this.flipButton = findViewById(R.id.btnFlip);
        this.flipButton.setOnClickListener(this);

        this.quitButton = findViewById(R.id.quitButton);
        this.quitButton.setOnClickListener(this);

        this.backButton = findViewById(R.id.backButton);
        this.backButton.setOnClickListener(this);

        this.forwardButton = findViewById(R.id.forwardButton);
        this.forwardButton.setOnClickListener(this);

        for (ChessFile cf : this.files) {
            this.maxScore += countQuizPositions(cf.getVariation());
        }
    }

    public int countQuizPositions(Variation v) {

        int total = 0;

        for (int i = 0; i < v.getMainLine().size(); i++) {

            if (maxMoveNumber > 0 && i - 1 + v.getStartIndex() >= (maxMoveNumber - 1) * 2) {
                break;
            }

            if (ChessPosition.isPieceWhite(v.getMainLine().get(i).getMovedPiece()) == forWhite) {
                total++;
            }
        }

        for (Variation var : v.getSideLines()) {
            total += countQuizPositions(var);
        }

        return total;
    }

    public ArrayList<String> getMovesToPoint(int variationID, int moveIndex) {
        ArrayList<String> moveText = new ArrayList<>();

        Variation target = this.files.get(quizIndex).getVariation().find(variationID);
        Variation current = target;
        MoveSnapshot[] moves = new MoveSnapshot[target.getStartIndex() + moveIndex + 1];
        int index = target.getStartIndex() + moveIndex;

        for (int i = moveIndex; i >= 0; i--) {
            moves[index] = current.getMainLine().get(i);
            index--;
        }

        int lastStartIndex = target.getStartIndex();

        while (current.getParentID() != -1) {
            current = files.get(quizIndex).getVariation().find(current.getParentID());

            for (int i = lastStartIndex - 1 - current.getStartIndex(); i >= 0; i--) {
                moves[index] = current.getMainLine().get(i);
                index--;
            }
            lastStartIndex = current.getStartIndex();

        }

        for (MoveSnapshot move : moves) {
            moveText.add(move.getNotation());
        }

        return moveText;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getY() <= this.boardView.getHeight() && !nextButton.isEnabled()) {
            if (currentMoveIndex != -1) {
                this.files.get(quizIndex).selectMove(currentVariation, currentMoveIndex - 1);
                currentMoveIndex = -1;
                currentVariation = -1;
                return super.onTouchEvent(event);
            }
            this.boardView.touchUpdate(event);
        }

        return super.onTouchEvent(event);
    }

    public boolean moveEntered(MoveSnapshot move, boolean legal) {
        if (files.get(quizIndex).getVariation().getMainLine().size() < 1)
            return false;

        Variation current = files.get(quizIndex).getVariation().find(files.get(quizIndex).getCurrentVariation());
        MoveSnapshot snap = current.getMainLine().get(files.get(quizIndex).getCurrentIndex());

        if (move.equals(snap)) {
            files.get(quizIndex).enterMove(move, legal);
            score++;
            index++;


        } else {
            index++;
            Toast toast = Toast.makeText(
                    this,
                    String.format("Incorrect move: You entered %s, correct was %s",
                            move.getNotation(),
                            snap.getNotation()),
                    Toast.LENGTH_LONG);
            toast.show();
            files.get(quizIndex).enterMove(snap, legal);
            boardView.renderWrongMove(move, snap);
        }
        for (Variation v : files.get(quizIndex).getCurrentVariations()) {
            if (maxMoveNumber > 0 && v.getStartIndex() >= (maxMoveNumber - 1) * 2)
                continue;
            variationStack.push(v);
        }

        boolean more = false;
        if (!(maxMoveNumber > 0 && current.getStartIndex() + files.get(quizIndex).getCurrentIndex() >= (maxMoveNumber - 1) * 2))
            more = files.get(quizIndex).goForward(-1);
        updateQuizDisplay();
        if ((maxMoveNumber > 0 && current.getStartIndex() + files.get(quizIndex).getCurrentIndex() >= (maxMoveNumber - 1) * 2) || !more) {
            if (variationStack.size() < 1 && quizIndex + 1 >= files.size()) {
                nextButton.setText("View results");
            }
            nextButton.setEnabled(true);

        }

        if (files.get(quizIndex).getCurrentIndex() >= current.getMainLine().size()) {
            if (variationStack.size() < 1 && quizIndex + 1 >= files.size()) {
                nextButton.setText("View results");
            }
            nextButton.setEnabled(true);

        }

        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == nextButton.getId()) {
            if (nextButton.getText().equals("Next Line")) {
                if (variationStack.size() > 0) {
                    Variation next = variationStack.pop();
                    files.get(quizIndex).selectMove(next.getID(), 0);
                } else {
                    quizIndex++;
                    this.boardView.updatePosition(files.get(quizIndex).getCurrentPosition());
                    if (!forWhite) {
                        files.get(quizIndex).goForward(-1);
                    }
                }
                updateQuizDisplay();
                nextButton.setEnabled(false);
            }
            else {
                onQuizCompleted();
            }

        } else if (v.getId() == flipButton.getId()) {
            this.boardView.flip();
        } else if (v.getId() == helpButton.getId()) {
            Variation current = files.get(quizIndex).getVariation().find(files.get(quizIndex).getCurrentVariation());
            if (files.get(quizIndex).getCurrentIndex() < current.getMainLine().size()) {
                MoveSnapshot snap = current.getMainLine().get(files.get(quizIndex).getCurrentIndex());
                Toast toast = Toast.makeText(this, "How about " + snap.getNotation(), Toast.LENGTH_LONG);
                toast.show();
                if (!alreadyHelped) {
                    score--;
                    alreadyHelped = true;
                }
            }
        } else if (v.getId() == quitButton.getId()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (v.getId() == backButton.getId()) {
            if (currentMoveIndex == -1) {
                this.currentVariation = files.get(quizIndex).getCurrentVariation();
                this.currentMoveIndex = files.get(quizIndex).getCurrentIndex();
            }
            files.get(quizIndex).goBackward();
            this.boardView.invalidate();
        } else if (v.getId() == forwardButton.getId()) {
            if (currentMoveIndex != -1) {
                if (currentVariation != files.get(quizIndex).getCurrentVariation()) {
                    List<Variation> options = files.get(quizIndex).getCurrentVariations();
                    if (options.size() > 0) {
                        int i;
                        for (i = 0; i < options.size(); i++) {
                            Variation var = options.get(i);
                            if (var.find(currentVariation) != null) {
                                files.get(quizIndex).goForward(i + 1);
                                break;
                            }
                        }
                        if (i == options.size()) {
                            files.get(quizIndex).goForward(0);
                        }

                    } else {
                        files.get(quizIndex).goForward(-1);
                    }
                } else {
                    files.get(quizIndex).goForward(0);
                }
            }

            if (currentVariation == files.get(quizIndex).getCurrentVariation() && currentMoveIndex == files.get(quizIndex).getCurrentIndex()) {
                currentVariation = -1;
                currentMoveIndex = -1;
            }
        }
    }

    public void updateQuizDisplay() {
        alreadyHelped = false;
        double percent = ((double) this.index) / this.maxScore;
        progressBar.setProgress((int) (percent * 100));

        scoreLabel.setText(String.format("Position %d of %d (%d out of %d correct).", index + 1, maxScore, score, index));

        ArrayList<String> history = getMovesToPoint(files.get(quizIndex).getCurrentVariation(), files.get(quizIndex).getCurrentIndex() - 1);
        StringBuilder text = new StringBuilder("");
        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0) {
                text.append((i / 2) + 1);
                text.append(".");
            }
            text.append(history.get(i));
            text.append(" ");
        }

        String historyText = text.toString();

        int counter = 0;
        do {
            historyText = text.substring(counter);
            historyLabel.setText(historyText);
            historyLabel.measure(0, 0);
            counter++;

        } while (historyLabel.getMeasuredWidth() > ((View) historyLabel.getParent()).getWidth());
        if (counter > 1) {

            historyLabel.setText("..." + historyText.substring(3));
        }
    }

    public void onQuizCompleted() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = LayoutInflater.from(this).inflate(R.layout.quiz_completion_dialog, null);

        int percentCorrect = (int)(((double)score)/maxScore * 100);

        TextView gradeLabel = v.findViewById(R.id.gradeLabel);
        gradeLabel.setText(String.format("%d%% Accuracy", percentCorrect));

        TextView scoreLabel = v.findViewById(R.id.lblScore);
        scoreLabel.setText(String.format("%d/%d Correct", score, maxScore));

        final Button reviewButton = v.findViewById(R.id.btnReview);

        final Button startOverButton = v.findViewById(R.id.btnStartOver);

        final Button doneButton = v.findViewById(R.id.btnDone);

        builder.setView(v);
        final AlertDialog dialog = builder.create();

        View.OnClickListener clickHandler = new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                if (view.getId() == reviewButton.getId()){
                    //TODO implement review feature
                }
                else if (view.getId() == startOverButton.getId()){
                    startActivity(getIntent());
                }
                else if (view.getId() == doneButton.getId()){
                    dialog.cancel();
                }
            }
        };

        reviewButton.setOnClickListener(clickHandler);
        startOverButton.setOnClickListener(clickHandler);
        doneButton.setOnClickListener(clickHandler);



        dialog.show();
    }

}
