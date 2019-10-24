package com.chessapps.chessstudyassistant.model;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class PGNReader {

    private String moveText = "";
    private static int ply = 0;

    public Variation buildVariation(){
        return buildVariation(-1, moveText, 0, new ChessPosition());
    }

    public static void saveFile (Variation mainLine, File file){
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(file));
            ply = 0;
            writeVariation(pw, mainLine);
            pw.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void writeVariation (PrintWriter writer, Variation variation) {

        for (MoveSnapshot move : variation.getMainLine()){
            if (ply % 2 == 0) {
                writer.append((ply/2)+1 + ". ");
            }
            writer.append(move.getNotation());
            writer.append(" ");
            if (move.getNotes() != null){
                writer.append('{');
                writer.append(move.getNotes());
                writer.append('}');
            }

            for (Variation v : variation.getSideLines()){
                if (v.getStartIndex() == ply){
                    int temp = ply;
                    writer.append("(");
                    writeVariation(writer, v);
                    writer.append(")");
                    ply = temp;
                }
            }

            ply++;
        }
    }

    private Variation buildVariation(int parentID, String moveText, int startIndex, ChessPosition startingPosition){

        Variation variation = new Variation(startIndex, parentID);
        variation.setStartIndex(startIndex);

        String mainLine = "";
        String subVariation = "";
        int depth = 0;

        boolean inNote = false;
        String note = "";
        MoveSnapshot lastAdded = null;

        //Iterate through every character in the file
        for (char c :  moveText.toCharArray()) {

            if (depth == 0) {
                if (c == '{') {
                    inNote = true;
                    continue;
                } else if (c == '}') {
                    inNote = false;
                    lastAdded.setNotes(note);
                    note = "";
                    continue;
                }
                if (inNote) {
                    note += c;
                    continue;
                }
            }
            if (c == '(') {
                depth++;
                if (depth == 1) {
                    continue;
                }
            }
            else if (c == ')') {

                depth--;
                if (depth == 0) {

                    subVariation+=" ";
                    MoveSnapshot cache = (MoveSnapshot) startingPosition.lastMove.clone();
                    startingPosition.undoLastMove(null);
                    variation.getSideLines().add(buildVariation(variation.getID(), subVariation, variation.getMainLine().size()+startIndex-1, (ChessPosition)startingPosition.clone()));
                    startingPosition.makeMoveOnBoard(cache);
                    subVariation = "";
                    continue;
                }
            }

            if (depth == 0) {
                if ((c == ' ' || c == ')') && mainLine.length()>1 && mainLine.trim().length()>1){
                    if (!mainLine.endsWith(".")){
                        String rawMove;
                        if (mainLine.contains(".")){
                            String[] terms = mainLine.split(".");
                            rawMove = terms[terms.length-1].trim();
                        }
                        else {
                            rawMove = mainLine.trim();
                        }
                        MoveSnapshot move = startingPosition.buildMove(rawMove);
                        variation.getMainLine().add(move);
                        startingPosition.makeMoveOnBoard(move);
                        lastAdded = move;
                    }
                    mainLine = "";
                }
                if (c != '(' && c != ')') {
                    mainLine += c;
                }

            }
            else {
                if (c==')')
                    subVariation+=" ";
                subVariation += c;
            }


        }
        /*
        string[] moves = mainLine.Split(" ".ToArray()).Select(delegate(string rawMove) {
            if (rawMove.Contains('.')) {
                string[] terms = rawMove.Split('.');
                return terms[terms.Length - 1];
            }
            else {
                return rawMove;
            }
        }).ToArray();
        */


        //variation.Notation.AddRange(moves.Where(x => x.Length > 0));

        return variation;
    }

    public PGNReader (File file){
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()){

                String line = scanner.nextLine();

                if (line.contains("["))
                    continue;
                else
                    moveText += line + " ";

            }
            scanner.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }
}
