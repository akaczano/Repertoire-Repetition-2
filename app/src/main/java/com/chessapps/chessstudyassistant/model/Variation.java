package com.chessapps.chessstudyassistant.model;

import java.util.ArrayList;
import java.util.List;

public class Variation {

    private static int index = -1;

    private ArrayList<MoveSnapshot> mainLine;
    private ArrayList<Variation> sideLines;
    private int startIndex;
    private int ID;
    private int parentID = -1;

    public Variation (int startIndex, int parentID){
        mainLine = new ArrayList<>();
        sideLines = new ArrayList<>();
        this.startIndex = startIndex;
        ID = ++index;
        this.parentID = parentID;
    }

    public void setMainLine(ArrayList<MoveSnapshot> mainLine){
        this.mainLine = mainLine;
    }

    public void remove(int ID){
        for (int i = 0; i < sideLines.size(); i++){
            Variation v = sideLines.get(i);
            if (v.getID() == ID){
                sideLines.remove(v);
                return;
            }

        }
        for (Variation v : sideLines){
            v.remove(ID);
        }
    }

    public Variation find(int ID){
        if (ID == this.ID)
            return this;
        else {
            for (Variation v : sideLines){
                Variation potMatch = v.find(ID);
                if (potMatch != null)
                    return potMatch;
            }
        }
        return null;
    }

    public ArrayList<MoveSnapshot> getMainLine() {
        return mainLine;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public ArrayList<Variation> getSideLines() {
        return sideLines;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getID() {
        return ID;
    }

    public int getParentID(){
        return parentID;
    }
}
