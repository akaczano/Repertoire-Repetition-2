package com.chessapps.chessstudyassistant.view.dynamic;

import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MoveAdapter extends RecyclerView.Adapter<MoveAdapter.MoveHolder>{

    private ArrayList<String> dataSet;
    private String[] data = {"1.e4", "e5", "2.Nf3", "Nc6", "3.Bb5"};

    public static class MoveHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public MoveHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    public MoveAdapter(){
        dataSet = new ArrayList<String>();
    }

    public ArrayList<String> getDataSet() {
        return this.dataSet;
    }

    @NonNull
    @Override
    public MoveHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        TextView v = new TextView(parent.getContext());


        return new MoveHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MoveHolder holder, int position) {
        holder.textView.setText(data[position]);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }
}
