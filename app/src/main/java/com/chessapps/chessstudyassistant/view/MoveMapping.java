package com.chessapps.chessstudyassistant.view;

public class MoveMapping {

    public int x;
    public int y;
    public int width;
    public int height;
    public int variationID;
    public int moveIndex;

    public MoveMapping(int x, int y, int width, int height, int variationID, int moveIndex){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.variationID = variationID;
        this.moveIndex = moveIndex;
    }

    public MoveMapping(){

    }

}
