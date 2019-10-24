package com.chessapps.chessstudyassistant.model;



import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class ChessFile {


    private String fileName;

    private Variation variation;
    private int currentVariation;
    private int currentIndex = 0;
    private ChessPosition position;

    public ChessFile (){
        position = new ChessPosition();
        variation = new Variation(0, -1);
        currentVariation = variation.getID();
    }

    public void promote(){

        if (currentIndex-1 == 0){

            Variation current = variation.find(currentVariation);
            Variation parent = variation.find(current.getParentID());
            if (parent != null){

                ArrayList<MoveSnapshot> oldMainLine = parent.getMainLine();
                ArrayList<MoveSnapshot> newMainLine = current.getMainLine();


            }
        }
    }

    public void delete(){
        delete(currentVariation, currentIndex-1);
    }

    public void delete(int variationID, int moveIndex){
        goBackward();

        if (moveIndex == 0 && variation.getID() != variationID){
            variation.remove(variationID);
            return;
        }
        Variation target = variation.find(variationID);

        int index = moveIndex;

        while(index < target.getMainLine().size()){
            target.getMainLine().remove(index);
        }

    }

    public boolean enterMove(MoveSnapshot move, boolean legal){
        if (legal){

            Variation variation = this.variation.find(currentVariation);
            if (variation == null)
                return false;

            if (currentIndex == variation.getMainLine().size()){
                variation.getMainLine().add(move);
                currentIndex++;
            }
            else {

                if (variation.getMainLine().get(currentIndex).equals(move)){
                    goForward(0);
                    return false;
                }

                for (int i = 0; i < variation.getSideLines().size(); i++){
                    Variation v = variation.getSideLines().get(i);
                    if (v.getStartIndex() == currentIndex + variation.getStartIndex()){
                        if (v.getMainLine().get(0).equals(move)){
                            goForward(i+1);
                            return false;
                        }
                    }
                }

                Variation var = new Variation(currentIndex+variation.getStartIndex(), currentVariation);
                var.getMainLine().add(move);
                variation.getSideLines().add(var);
                currentVariation = var.getID();
                currentIndex = 1;
            }

            return true;
        }
        return false;
    }

    public void selectMove(int variationID, int moveIndex) {

        Variation target = this.variation.find(variationID);
        Variation current = target;
        MoveSnapshot[] moves = new MoveSnapshot[target.getStartIndex() + moveIndex + 1];
        int index = target.getStartIndex() + moveIndex;

        for (int i = moveIndex; i >= 0; i--) {
            moves[index] = current.getMainLine().get(i);
            index--;
        }

        int lastStartIndex = target.getStartIndex();

        while (current.getParentID() != -1) {
            current = variation.find(current.getParentID());

            for (int i = lastStartIndex - 1 - current.getStartIndex(); i >= 0; i--) {
                moves[index] = current.getMainLine().get(i);
                index--;
            }
            lastStartIndex = current.getStartIndex();

        }

        while (variation.find(currentVariation).getParentID() != -1 || currentIndex > 0)
            goBackward();
        for (MoveSnapshot move : moves) {
            position.makeMoveOnBoard(move);
        }
        this.currentVariation = variationID;
        this.currentIndex = moveIndex + 1;
    }

    public List<Variation> getCurrentVariations(){
        ArrayList<Variation> options = new ArrayList<>();

        Variation current = variation.find(currentVariation);

        for (Variation v : current.getSideLines()){
            if (v.getStartIndex() == currentIndex+current.getStartIndex()){
                options.add(v);
            }
        }

        return options;
    }

    public boolean goForward(int preference){
        Variation current = variation.find(currentVariation);



        int count = 0;
        for (Variation subVar : current.getSideLines()){

            if (subVar.getStartIndex() == currentIndex+current.getStartIndex()){
                count++;
                if (preference == -1);
                    //JOptionPane.showMessageDialog(null, subVar.getMainLine().get(0).getNotation());
                else if (preference == 0)
                    break;
                else if (count == preference){
                    currentVariation = subVar.getID();
                    currentIndex = 0;
                }
            }
        }

        current = variation.find(currentVariation);

        if (currentIndex == current.getMainLine().size())
            return false;
        position.makeMoveOnBoard(current.getMainLine().get(currentIndex));
        currentIndex++;
        return true;
    }

    public void goBackward(){
        if (currentIndex == 0)
            return;
        Variation current = this.variation.find(currentVariation);
        if (currentIndex == 1){
            if (current.getParentID() == -1) {
                position.undoLastMove(null);
                currentIndex--;

            }
            else if (current.getStartIndex() == 0){
                position.undoLastMove(null);
                currentIndex--;
                currentVariation = current.getParentID();
            }
            else {
                Variation papa = this.variation.find(current.getParentID());
                currentVariation = papa.getID();
                if (current.getStartIndex()-papa.getStartIndex()-1 < 0)
                    return;
                position.undoLastMove(papa.getMainLine().get(current.getStartIndex()-papa.getStartIndex() - 1));
                currentIndex = current.getStartIndex()-papa.getStartIndex();
            }
        }
        else {
            currentIndex--;
            position.undoLastMove(variation.find(currentVariation).getMainLine().get(currentIndex-1));
        }


    }

    public void goToStart(){
        while (variation.find(currentVariation).getStartIndex() + currentIndex > 0){
            goBackward();
        }
    }

    public void goToEnd(){
        while (currentIndex < variation.find(currentVariation).getMainLine().size()){
            goForward(-1);
        }
    }

    public Variation getVariation(){
        return this.variation;
    }

    public int getCurrentIndex(){
        return currentIndex;
    }

    public int getCurrentVariation(){
        return currentVariation;
    }

    public ChessPosition getCurrentPosition (){
        return this.position;
    }

    public String getFileName (){
        return this.fileName;
    }

    public void setFileName (String name){
        this.fileName = name;
    }

    public void writeToPGN(File file){
        PGNReader.saveFile(this.variation, file);
    }

    public static ChessFile loadFromPGN(File file){
        ChessFile chessFile = new ChessFile();

        PGNReader reader = new PGNReader(file);
        Variation var = reader.buildVariation();
        chessFile.fileName = file.getName().replace('_', ' ').substring(0,file.getName().length()-4);
        chessFile.variation = var;
        chessFile.currentVariation = var.getID();
        chessFile.currentIndex = 0;

        return chessFile;
    }



}
