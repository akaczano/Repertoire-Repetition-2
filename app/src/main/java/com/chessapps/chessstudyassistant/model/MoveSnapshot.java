package com.chessapps.chessstudyassistant.model;

public class MoveSnapshot {

    private Location startSquare;
    private Location endSquare;
    private ChessPiece capturedPiece;
    private ChessPiece promotion;
    private ChessPiece movedPiece;
    private boolean enPassant;
    private boolean castle;
    private String notes;
    private String notation;
    private boolean lossOfCastles[] = {false, false, false, false};

    public MoveSnapshot(Location start, Location end, ChessPiece movedPiece, ChessPiece capturedPiece,
                        boolean ep, boolean castle, ChessPiece promotion){
        startSquare = start;
        endSquare = end;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        enPassant = ep;
        this.castle = castle;
        this.promotion = promotion;
    }

    @Override
    public Object clone () {
        return new MoveSnapshot((Location)startSquare.clone(), (Location)endSquare.clone(), movedPiece, capturedPiece, enPassant, castle, promotion);
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof MoveSnapshot))
            return false;

        MoveSnapshot snap = (MoveSnapshot)o;
        if (!snap.getStartSquare().equals(startSquare))
            return false;
        if (!snap.getEndSquare().equals(endSquare))
            return false;
        if (!snap.getPromotion().equals(promotion))
            return false;
        return true;
    }

    public Location getStartSquare() {
        return startSquare;
    }

    public Location getEndSquare() {
        return endSquare;
    }

    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public boolean isCastle() {
        return castle;
    }

    public String getNotes() {
        return notes;
    }

    public ChessPiece getPromotion() {
        return promotion;
    }

    public ChessPiece getMovedPiece() {
        return movedPiece;
    }

    public String getNotation() {
        return notation;
    }

    public MoveSnapshot setNotation(String notation) {
        this.notation = notation;
        return this;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setPromotion (ChessPiece promotion){
        this.promotion = promotion;
    }

    public boolean[] getLossOfCastles() {
        return lossOfCastles;
    }

    public void setLossOfCastles(boolean[] lossOfCastling) {
        this.lossOfCastles= lossOfCastling;
    }
}
