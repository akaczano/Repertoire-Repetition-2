package com.chessapps.chessstudyassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.chessapps.chessstudyassistant.observer.DirectoryChangeListener;
import com.chessapps.chessstudyassistant.view.dynamic.FileAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DirectoryChangeListener {

    private static final int PERMISSIONS_USE_EXTERNAL_STORAGE = 57;
    private File fileDir;

    private TextView greetingLabel;
    private Button promptNewButton;

    private ListView filesView;
    private FileAdapter fileAdapter;

    private Button newButton;
    private Button newFolderButton;
    private Button deleteButton;
    private Button quizButton;

    private String currentDirectory;

    private float x1, x2;
    private static final int MINIMUM_DISTANCE = 400;

    public static final Comparator<File> SORTER = new Comparator<File>(){

        @Override
        public int compare(File o1, File o2) {

            if (o1.isDirectory() && !o2.isDirectory()) {
                return -1;
            } else if (o2.isDirectory() && !o1.isDirectory()) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        }
    };


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        fileDir = getExternalFilesDir(null);

        currentDirectory = fileDir.getAbsolutePath() + "/";

        this.greetingLabel = findViewById(R.id.greetingLabel);
        this.promptNewButton = findViewById(R.id.promptNewButton);
        this.promptNewButton.setOnClickListener(this);


        filesView = findViewById(R.id.filesView);
        fileAdapter = new FileAdapter(this, R.layout.file_display, filesView, this);
        filesView.setAdapter(fileAdapter);

        newButton = findViewById(R.id.btnNewFile);
        newButton.setOnClickListener(this);

        newFolderButton = findViewById(R.id.btnNewFolder);
        newFolderButton.setOnClickListener(this);

        deleteButton = findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(this);

        quizButton = findViewById(R.id.btnQuiz);
        quizButton.setOnClickListener(this);

        if (!isExternalStorageWritable() || !isExternalStorageReadable()) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_USE_EXTERNAL_STORAGE);

        } else {

            loadContent();
        }

    }

    private void loadContent() {

        showGreeting(false);

        File dir = new File(currentDirectory);
        File[] files = fileDir.listFiles();
        if (this.fileDir.listFiles() == null || this.fileDir.listFiles().length < 1) {
            showGreeting(true);
        }
        else {
            showGreeting(false);

            this.fileAdapter.clear();

            for (File file : dir.listFiles()) {
                //Load files into list view
                this.fileAdapter.add(file);
            }

            this.fileAdapter.sort(SORTER);
            filesView.setVisibility(View.VISIBLE);
        }
    }


    public void onClick(View source) {

        if (source.getId() == R.id.promptNewButton || source.getId() == R.id.btnNewFile) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Create new file");
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!fileDir.exists()) {
                        fileDir.mkdir();
                    }
                    File file = new File(currentDirectory + input.getText() + ".pgn");
                    try {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            file.createNewFile();
                        }
                        loadContent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        else if (source.getId() == R.id.btnNewFolder) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Create new folder");
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!fileDir.exists()) {
                        fileDir.mkdir();
                    }
                    File file = new File(currentDirectory + input.getText() + "/");

                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        file.mkdir();
                    }
                    loadContent();


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
        else if (source.getId() == R.id.btnDelete){
            this.fileAdapter.delete();
        }
        else if (source.getId() == R.id.btnQuiz){
            fileAdapter.quiz();
        }
    }

    @Override
    public void directoryChanged(String appended){
        this.currentDirectory += appended + "/";
        loadContent();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                if (x2 - x1 > MINIMUM_DISTANCE){

                    if (currentDirectory.equals(fileDir.getAbsolutePath() + "/"))
                        break;

                    String newDirectory = "";
                    String[] folders = currentDirectory.split("/");
                    for (int i = 0; i < folders.length-1; i++){
                        newDirectory += folders[i] + "/";
                    }
                    this.currentDirectory = newDirectory;
                    this.loadContent();
                }
        }
        return super.dispatchTouchEvent(event);
    }

    private void showGreeting(boolean show) {
        greetingLabel.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        promptNewButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    //Permissions
    public boolean isExternalStorageReadable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public boolean isExternalStorageWritable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_USE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!fileDir.exists()) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                            fileDir.mkdir();
                    }
                    loadContent();

                } else {


                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


}
