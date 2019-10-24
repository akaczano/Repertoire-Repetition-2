package com.chessapps.chessstudyassistant.model;

public class Location {

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    private int x;
    private int y;

    public Location(int x, int y){
        this.x = x;
        this.y = y;

    }

    @Override
    public String toString(){
        return String.format("(%d, %d)", x ,y);
    }

    @Override
    public Object clone(){
        return new Location(x, y);
    }

    @Override
    public boolean equals (Object loc){
        if (!(loc instanceof Location))
            return false;
        Location location = (Location) loc;
        if (location.getX() == x && location.getY() == y)
            return true;

        return false;
    }

}
