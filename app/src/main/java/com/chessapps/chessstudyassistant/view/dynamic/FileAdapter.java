package com.chessapps.chessstudyassistant.view.dynamic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.chessapps.chessstudyassistant.EditActivity;
import com.chessapps.chessstudyassistant.MainActivity;
import com.chessapps.chessstudyassistant.QuizActivity;
import com.chessapps.chessstudyassistant.R;
import com.chessapps.chessstudyassistant.model.ChessFile;
import com.chessapps.chessstudyassistant.observer.DirectoryChangeListener;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;

public class FileAdapter extends ArrayAdapter<File> {

    private ArrayList<Integer> selectedIndices = new ArrayList<>();
    private DirectoryChangeListener listener;


    public FileAdapter(Context context, int resource, ListView view, DirectoryChangeListener listener) {
        super(context, resource, 0, new ArrayList<File>());
        this.listener = listener;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent){


        final File file = getItem(position);

        final String fileName = file.isDirectory() ? file.getName() : file.getName().substring(0, file.getName().length()-4);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_display, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.iconView);
        if (file.isDirectory())
            icon.setImageResource(R.drawable.folder_icon);
        else {
            icon.setImageResource(R.drawable.white_pawn);
        }

        TextView view  = (TextView) convertView.findViewById(R.id.lblFileName);

        view.setText(fileName);
        if (selectedIndices.contains(position)){
            view.setTextColor(Color.CYAN);
        }
        else {
            view.setTextColor(Color.parseColor("#F9F7F7"));
        }
        View.OnClickListener handleClick = new View.OnClickListener(){
            public void onClick(View v){
                if (selectedIndices.contains(position)){
                    selectedIndices.remove((Object)position);
                }
                else {
                    selectedIndices.add(position);
                }
                notifyDataSetChanged();
            }
        };

        view.setOnClickListener(handleClick);
        icon.setOnClickListener(handleClick);

        view.setLongClickable(true);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setSingleChoiceItems(new String[]{"Rename", "Move"}, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            AlertDialog.Builder renameBuilder = new AlertDialog.Builder(getContext());
                            renameBuilder.setTitle("Rename file");
                            final EditText newNameField = new EditText(getContext());
                            newNameField.setText(fileName);
                            renameBuilder.setView(newNameField);
                            renameBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    File origin = getItem(position);

                                    String destPath = getContext().getExternalFilesDir(null).getAbsolutePath()+ "/" + newNameField.getText();
                                    destPath += origin.isDirectory() ? "/" : ".pgn";

                                    File destination = new File(destPath);
                                    origin.renameTo(destination);
                                    remove(origin);
                                    add(destination);
                                    sort(MainActivity.SORTER);
                                    notifyDataSetChanged();
                                }
                            });

                            renameBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which){
                                    dialog.cancel();
                                }
                            });

                            dialog.cancel();
                            renameBuilder.show();
                        }

                        else {
                            AlertDialog.Builder moveBuilder = new AlertDialog.Builder(getContext());
                            moveBuilder.setTitle("Move file");
                            ListView view = new ListView(getContext());
                            ArrayAdapter<String> adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1);
                            adapter.add("Root");
                            ArrayList<File> dirs = new ArrayList<>();
                            addDirsRecursive(new File(getContext().getExternalFilesDir(null).getAbsolutePath() + "/"), dirs);
                            for (File f : dirs){
                                if (!f.getName().equals(getItem(position).getName()))
                                    adapter.add(f.getName());
                            }


                            view.setAdapter(adapter);
                            moveBuilder.setView(view);

                            dialog.cancel();
                            moveBuilder.show();
                        }
                    }
                });

                builder.show();

                return true;
            }


        });

        Button editButton = convertView.findViewById(R.id.btnEdit);
        editButton.setText(file.isDirectory() ? "Open" : "Edit");
        if (!file.isDirectory()) {
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), EditActivity.class);
                    intent.putExtra("file_name", file.getAbsolutePath());
                    getContext().startActivity(intent);
                }
            });
        }
        else {
            editButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    listener.directoryChanged(file.getName());
                }
            });
        }



        return convertView;
    }

    public void delete(){

        if (this.selectedIndices.size() < 1){
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Are you sure you want to delete these files?");
        ListView filesList = new ListView(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        for (int i : selectedIndices){
            String text = getItem(i).getName();
            if (!getItem(i).isDirectory()){
                text = text.substring(0, text.length()-4);
            }
            adapter.add(text);
        }
        filesList.setAdapter(adapter);
        builder.setView(filesList);
        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i : selectedIndices){
                    File f = getItem(i);
                    if (f.isDirectory())
                        deleteDir(f);
                    f.delete();
                    remove(getItem(i));
                }
                selectedIndices.clear();
                notifyDataSetChanged();
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

    private void deleteDir(File file){
        File dir = new File(file.getAbsolutePath()+"/");
        for (File f : dir.listFiles()){
            if (!f.isDirectory()){
                f.delete();
            }
            else {
                deleteDir(f);
            }
        }
    }

    private void addFilesRecursive(File dir, ArrayList<File> arr){
        File actual = new File(dir.getAbsolutePath()+"/");
        for (File f : dir.listFiles()){
            if (f.isDirectory()){
                addFilesRecursive(f, arr);
            }
            else {
                arr.add(f);
            }
        }
    }

    private void addDirsRecursive(File dir, ArrayList<File> arr){
        File actual = new File(dir.getAbsolutePath()+"/");
        for (File f : dir.listFiles()){
            if (f.isDirectory()){
                arr.add(f);
                addDirsRecursive(f, arr);
            }
        }
    }

    public void quiz() {
        if (selectedIndices.size() < 1)
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Configure Quiz");

        View dialog = LayoutInflater.from(getContext()).inflate(R.layout.quiz_dialog, null, false);

        final ListView itemsView = dialog.findViewById(R.id.quizItemsView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_checked);
        for (int i : selectedIndices){
            String text = getItem(i).getName();
            if (!getItem(i).isDirectory()){
                text = text.substring(0, text.length()-4);
            }
            adapter.add(text);
        }

        itemsView.setAdapter(adapter);
        itemsView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        for (int i = 0; i < selectedIndices.size(); i++)
            itemsView.setItemChecked(i, true);


        final EditText moveNumberField = dialog.findViewById(R.id.maxNumberField);
        final CheckBox quizWhiteBox = dialog.findViewById(R.id.quizWhiteBox);

        builder.setView(dialog);

        builder.setPositiveButton("Begin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ArrayList<File> files = new ArrayList<>();
                for (int i = 0; i < selectedIndices.size(); i++) {
                    if (itemsView.isItemChecked(i)){
                        File f = getItem(selectedIndices.get(i));
                        if (!f.isDirectory()) {
                            files.add(getItem(selectedIndices.get(i)));
                        }
                        else {
                            addFilesRecursive(f, files);
                        }
                    }
                }

                Intent intent = new Intent(getContext(), QuizActivity.class);

                for (int j = 0; j < files.size(); j++) {
                    intent.putExtra("file_path_" + j, files.get(j).getAbsolutePath());
                }
                if (!moveNumberField.getText().toString().equals("")) {
                    intent.putExtra("max_move_number", Integer.parseInt(moveNumberField.getText().toString()));
                }
                intent.putExtra("quiz_white", quizWhiteBox.isChecked());

                getContext().startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builder.show();
    }

    @Override
    public void clear() {
        super.clear();
        this.selectedIndices.clear();
    }
}
