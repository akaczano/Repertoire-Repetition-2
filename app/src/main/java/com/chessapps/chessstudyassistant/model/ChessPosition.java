package com.chessapps.chessstudyassistant.model;

import java.util.ArrayList;

public class ChessPosition {

    private ChessPiece[][] pieces = new ChessPiece[8][8];
    private boolean whiteToMove = true;
    private boolean whiteShortCastle = true;
    private boolean whiteLongCastle = true;
    private boolean blackShortCastle = true;
    private boolean blackLongCastle = true;
    public MoveSnapshot lastMove = null;

    public ChessPosition() {

        ChessPiece[][] demopieces = new ChessPiece[][]{
                {ChessPiece.WHITE_ROOK, ChessPiece.WHITE_KNIGHT, ChessPiece.WHITE_BISHOP, ChessPiece.WHITE_QUEEN,
                        ChessPiece.WHITE_KING, ChessPiece.WHITE_BISHOP, ChessPiece.WHITE_KNIGHT, ChessPiece.WHITE_ROOK},
                {ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN,
                        ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN, ChessPiece.WHITE_PAWN},
                {ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                        ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY},
                {ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                        ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY},
                {ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                        ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY},
                {ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY,
                        ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY, ChessPiece.EMPTY},
                {ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN,
                        ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN, ChessPiece.BLACK_PAWN},
                {ChessPiece.BLACK_ROOK, ChessPiece.BLACK_KNIGHT, ChessPiece.BLACK_BISHOP, ChessPiece.BLACK_QUEEN,
                        ChessPiece.BLACK_KING, ChessPiece.BLACK_BISHOP, ChessPiece.BLACK_KNIGHT, ChessPiece.BLACK_ROOK}
        };
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pieces[i][j] = demopieces[j][i];
            }
        }
    }

    public void makeMoveOnBoard(MoveSnapshot move) {

        if (move.getPromotion() != ChessPiece.EMPTY) {
            pieces[move.getEndSquare().getX()][move.getEndSquare().getY()] = move.getPromotion();
        }
        else {
            pieces[move.getEndSquare().getX()][move.getEndSquare().getY()] =
                    pieces[move.getStartSquare().getX()][move.getStartSquare().getY()];
        }


        pieces[move.getStartSquare().getX()][move.getStartSquare().getY()] = ChessPiece.EMPTY;

        if (move.isEnPassant()) {
            pieces[move.getEndSquare().getX()][move.getStartSquare().getY()] = ChessPiece.EMPTY;
        }
        else if (move.isCastle()) {
            if (move.getStartSquare().getX() > move.getEndSquare().getX()) {
                //Long castles
                pieces[0][move.getStartSquare().getY()] = ChessPiece.EMPTY;
                if (move.getStartSquare().getY() == 0) {
                    //White
                    pieces[0][0] = ChessPiece.EMPTY;
                    pieces[3][0] = ChessPiece.WHITE_ROOK;
                }
                else {
                    //Black
                    pieces[0][7] = ChessPiece.EMPTY;
                    pieces[3][7] = ChessPiece.BLACK_ROOK;
                }
            }
            else {
                //Short castles
                if (move.getStartSquare().getY() == 0) {
                    //White
                    pieces[7][0] = ChessPiece.EMPTY;
                    pieces[5][0] = ChessPiece.WHITE_ROOK;
                }
                else {
                    //Black
                    pieces[7][7] = ChessPiece.EMPTY;
                    pieces[5][7] = ChessPiece.BLACK_ROOK;
                }
            }
        }

        //Castling rights
        if (move.getMovedPiece() == ChessPiece.WHITE_KING) {
            whiteShortCastle = false;
            whiteLongCastle = false;
        }
        else if (move.getMovedPiece() == ChessPiece.BLACK_KING) {
            blackShortCastle = false;
            blackLongCastle = false;
        }
        else if (move.getMovedPiece() == ChessPiece.WHITE_ROOK) {
            if (move.getStartSquare().getX() == 7 && move.getStartSquare().getY() == 0)
                whiteShortCastle = false;
            if (move.getStartSquare().getX() == 0 && move.getStartSquare().getY() == 0)
                whiteLongCastle = false;
        }
        else if (move.getMovedPiece() == ChessPiece.BLACK_ROOK) {
            if (move.getStartSquare().getX() == 7 && move.getStartSquare().getY() == 7)
                blackShortCastle = false;
            if (move.getStartSquare().getX() == 0 && move.getStartSquare().getY() == 7)
                blackLongCastle = false;
        }

        lastMove = move;
        whiteToMove = !whiteToMove;
    }

    public void undoLastMove(MoveSnapshot moveBefore) {

        pieces[lastMove.getStartSquare().getX()][lastMove.getStartSquare().getY()] = lastMove.getMovedPiece();
        pieces[lastMove.getEndSquare().getX()][lastMove.getEndSquare().getY()] = lastMove.getCapturedPiece();

        if (lastMove.isEnPassant()) {
            ChessPiece addition = isPieceWhite(lastMove.getMovedPiece()) ? ChessPiece.BLACK_PAWN : ChessPiece.WHITE_PAWN;
            pieces[lastMove.getEndSquare().getX()][lastMove.getEndSquare().getY() + (isPieceWhite(lastMove.getMovedPiece()) ? -1 : 1)] = addition;
        }
        else if (lastMove.isCastle()) {
            if (lastMove.getEndSquare().getY() == 0) {
                if (lastMove.getEndSquare().getX() == 3) {
                    this.whiteLongCastle = true;
                    pieces[3][0] = ChessPiece.EMPTY;
                    pieces[0][0] = ChessPiece.WHITE_ROOK;
                }
                else {
                    this.whiteShortCastle = true;
                    pieces[5][0] = ChessPiece.EMPTY;
                    pieces[7][0] = ChessPiece.WHITE_ROOK;
                }
            }
            else {
                if (lastMove.getEndSquare().getX() == 3) {
                    this.blackLongCastle = true;
                    pieces[3][7] = ChessPiece.EMPTY;
                    pieces[0][7] = ChessPiece.BLACK_ROOK;
                }
                else {
                    this.blackShortCastle = true;
                    pieces[5][7] = ChessPiece.EMPTY;
                    pieces[7][7] = ChessPiece.BLACK_ROOK;
                }
            }
        }

       if (lastMove.getLossOfCastles()[0])
           whiteShortCastle = true;
       if (lastMove.getLossOfCastles()[1])
           whiteLongCastle = true;
       if (lastMove.getLossOfCastles()[2])
           blackShortCastle = true;
       if (lastMove.getLossOfCastles()[3])
           blackLongCastle = true;

        lastMove = moveBefore;
        whiteToMove = !whiteToMove;
    }

    public boolean isMoveLegal(MoveSnapshot move) {

        if (move.getMovedPiece() == ChessPiece.EMPTY)
            return false;
        else if (isPieceWhite(move.getMovedPiece()) != whiteToMove)
            return false;
        else if (move.isCastle()) {
            if (isKingInCheck(whiteToMove))
                return false;
            //TODO: implement castling through check
        }
        if (getLegalDestinations(move.getStartSquare()).contains(move.getEndSquare())) {
            ChessPosition clone = (ChessPosition) this.clone();
            clone.makeMoveOnBoard(move);
            if (!clone.isKingInCheck(whiteToMove))
                return true;
        }

        return false;
    }

    private ArrayList<Location> getLegalDestinations(Location start) {
        ArrayList<Location> locs = new ArrayList<>();
        ChessPiece piece = pieces[start.getX()][start.getY()];

        if (piece == ChessPiece.WHITE_PAWN) {
            //Forward movement
            if (start.getY() < 7 && pieces[start.getX()][start.getY() + 1] == ChessPiece.EMPTY) {
                locs.add(new Location(start.getX(), start.getY() + 1));
                if (start.getY() == 1 && pieces[start.getX()][start.getY() + 2] == ChessPiece.EMPTY) {
                    locs.add(new Location(start.getX(), start.getY() + 2));
                }
            }

            //Captures
            if (start.getX() > 0 && start.getY() < 7 && pieces[start.getX() - 1][start.getY() + 1] != ChessPiece.EMPTY
                    && !isPieceWhite(pieces[start.getX() - 1][start.getY() + 1]))
                locs.add(new Location(start.getX() - 1, start.getY() + 1));
            if (start.getX() < 7 && start.getY() < 7 && pieces[start.getX() + 1][start.getY() + 1] != ChessPiece.EMPTY
                    && !isPieceWhite(pieces[start.getX() + 1][start.getY() + 1]))
                locs.add(new Location(start.getX() + 1, start.getY() + 1));

            //En passant
            if (lastMove != null && lastMove.getMovedPiece() == ChessPiece.BLACK_PAWN) {
                if (Math.abs(lastMove.getStartSquare().getX() - start.getX()) == 1) {
                    if (lastMove.getStartSquare().getY() == 6 & lastMove.getEndSquare().getY() == 4 && start.getY() == 4) {
                        locs.add(new Location(lastMove.getStartSquare().getX(), start.getY() + 1));
                    }
                }
            }
        }
        else if (piece == ChessPiece.BLACK_PAWN) {

            //Forward
            if (start.getY() > 0 && pieces[start.getX()][start.getY() - 1] == ChessPiece.EMPTY) {
                locs.add(new Location(start.getX(), start.getY() - 1));
                if (start.getY() == 6 && pieces[start.getX()][start.getY() - 2] == ChessPiece.EMPTY) {
                    locs.add(new Location(start.getX(), start.getY() - 2));
                }
            }

            //Captures
            if (start.getX() > 0 && start.getY() > 0 && pieces[start.getX() - 1][start.getY() - 1] != ChessPiece.EMPTY
                    && isPieceWhite(pieces[start.getX() - 1][start.getY() - 1]))
                locs.add(new Location(start.getX() - 1, start.getY() - 1));
            if (start.getX() < 7 && start.getY() > 0 && pieces[start.getX() + 1][start.getY() - 1] != ChessPiece.EMPTY
                    && isPieceWhite(pieces[start.getX() + 1][start.getY() - 1]))
                locs.add(new Location(start.getX() + 1, start.getY() - 1));

            //En passant
            if (lastMove != null && lastMove.getMovedPiece() == ChessPiece.WHITE_PAWN) {
                if (Math.abs(lastMove.getStartSquare().getX() - start.getX()) == 1) {
                    if (lastMove.getStartSquare().getY() == 1 & lastMove.getEndSquare().getY() == 3 && start.getY() == 3) {
                        locs.add(new Location(lastMove.getStartSquare().getX(), start.getY() - 1));
                    }
                }
            }
        }
        else if (piece == ChessPiece.WHITE_ROOK || piece == ChessPiece.BLACK_ROOK) {
            for (int i = start.getX() + 1; i < 8; i++) {
                Location potLoc = new Location(i, start.getY());
                ChessPiece p = pieces[potLoc.getX()][potLoc.getY()];
                if (p == ChessPiece.EMPTY)
                    locs.add(potLoc);
                else {
                    if (isPieceWhite(p) != isPieceWhite(piece))
                        locs.add(potLoc);
                    break;
                }

            }
            for (int j = start.getX() - 1; j >= 0; j--) {
                Location potLoc = new Location(j, start.getY());
                ChessPiece p = pieces[potLoc.getX()][potLoc.getY()];
                if (p == ChessPiece.EMPTY)
                    locs.add(potLoc);
                else {
                    if (isPieceWhite(p) != isPieceWhite(piece))
                        locs.add(potLoc);
                    break;
                }
            }
            for (int k = start.getY() + 1; k < 8; k++) {
                Location potLoc = new Location(start.getX(), k);
                ChessPiece p = pieces[potLoc.getX()][potLoc.getY()];
                if (p == ChessPiece.EMPTY)
                    locs.add(potLoc);
                else {
                    if (isPieceWhite(p) != isPieceWhite(piece))
                        locs.add(potLoc);
                    break;
                }
            }
            for (int l = start.getY() - 1; l >= 0; l--) {
                Location potLoc = new Location(start.getX(), l);
                ChessPiece p = pieces[potLoc.getX()][potLoc.getY()];
                if (p == ChessPiece.EMPTY)
                    locs.add(potLoc);
                else {
                    if (isPieceWhite(p) != isPieceWhite(piece))
                        locs.add(potLoc);
                    break;
                }
            }
        }
        else if (piece == ChessPiece.WHITE_KNIGHT || piece == ChessPiece.BLACK_KNIGHT) {
            Location[] potLocs = new Location[]{
                    new Location(start.getX() + 1, start.getY() + 2),
                    new Location(start.getX() - 1, start.getY() + 2),
                    new Location(start.getX() + 2, start.getY() + 1),
                    new Location(start.getX() + 2, start.getY() - 1),
                    new Location(start.getX() - 2, start.getY() + 1),
                    new Location(start.getX() - 2, start.getY() - 1),
                    new Location(start.getX() + 1, start.getY() - 2),
                    new Location(start.getX() - 1, start.getY() - 2)
            };

            for (Location l : potLocs) {
                if (l.getX() > 7 || l.getX() < 0 || l.getY() > 7 || l.getY() < 0)
                    continue;
                ChessPiece p = pieces[l.getX()][l.getY()];
                if (p == ChessPiece.EMPTY) {
                    locs.add(l);
                    continue;
                }
                if (isPieceWhite(p) != isPieceWhite(piece))
                    locs.add(l);
            }
        }
        else if (piece == ChessPiece.WHITE_BISHOP || piece == ChessPiece.BLACK_BISHOP) {

            int rank = start.getX();
            int file = start.getY();

            for (int i = rank + 1, j = file + 1; i < 8 && j < 8; i++, j++) {
                Location potLoc = new Location(i, j);
                if (pieces[potLoc.getX()][potLoc.getY()] == ChessPiece.EMPTY) {
                    locs.add(potLoc);
                }
                else {
                    if (isPieceWhite(pieces[potLoc.getX()][potLoc.getY()]) != isPieceWhite(pieces[rank][file])) {
                        locs.add(potLoc);
                    }
                    break;
                }
            }
            for (int i = rank + 1, j = file - 1; i < 8 && j >= 0; i++, j--) {
                Location potLoc = new Location(i, j);
                if (pieces[potLoc.getX()][potLoc.getY()] == ChessPiece.EMPTY) {
                    locs.add(potLoc);
                }
                else {
                    if (isPieceWhite(pieces[potLoc.getX()][potLoc.getY()]) != isPieceWhite(pieces[rank][file])) {
                        locs.add(potLoc);
                    }
                    break;
                }
            }
            for (int i = rank - 1, j = file + 1; i >= 0 && j < 8; i--, j++) {
                Location potLoc = new Location(i, j);
                if (pieces[potLoc.getX()][potLoc.getY()] == ChessPiece.EMPTY) {
                    locs.add(potLoc);
                }
                else {
                    if (isPieceWhite(pieces[potLoc.getX()][potLoc.getY()]) != isPieceWhite(pieces[rank][file])) {
                        locs.add(potLoc);
                    }
                    break;
                }
            }
            for (int i = rank - 1, j = file - 1; i >= 0 && j >= 0; i--, j--) {
                Location potLoc = new Location(i, j);
                if (pieces[potLoc.getX()][potLoc.getY()] == ChessPiece.EMPTY) {
                    locs.add(potLoc);
                }
                else {
                    if (isPieceWhite(pieces[potLoc.getX()][potLoc.getY()]) != isPieceWhite(pieces[rank][file])) {
                        locs.add(potLoc);
                    }
                    break;
                }
            }


        }
        else if (piece == ChessPiece.WHITE_QUEEN || piece == ChessPiece.BLACK_QUEEN) {

            pieces[start.getX()][start.getY()] = isPieceWhite(piece) ? ChessPiece.WHITE_BISHOP : ChessPiece.BLACK_BISHOP;
            locs.addAll(getLegalDestinations(start));
            pieces[start.getX()][start.getY()] = isPieceWhite(piece) ? ChessPiece.WHITE_ROOK : ChessPiece.BLACK_ROOK;
            locs.addAll(getLegalDestinations(start));
            pieces[start.getX()][start.getY()] = piece;
        }
        else if (piece == ChessPiece.WHITE_KING || piece == ChessPiece.BLACK_KING) {
            Location[] potLocs = new Location[]{
                    new Location(start.getX() + 1, start.getY()),
                    new Location(start.getX() + 1, start.getY() + 1),
                    new Location(start.getX(), start.getY() + 1),
                    new Location(start.getX() - 1, start.getY() + 1),
                    new Location(start.getX() - 1, start.getY()),
                    new Location(start.getX() - 1, start.getY() - 1),
                    new Location(start.getX(), start.getY() - 1),
                    new Location(start.getX() + 1, start.getY() - 1)
            };

            for (Location l : potLocs) {
                if (l.getX() > 7 || l.getX() < 0 || l.getY() > 7 || l.getY() < 0)
                    continue;
                if (pieces[l.getX()][l.getY()] == ChessPiece.EMPTY) {
                    locs.add(l);
                }
                else if (isPieceWhite(pieces[l.getX()][l.getY()]) != isPieceWhite(piece))
                    locs.add(l);
            }

            //Castling
            if (isPieceWhite(piece)) {
                if (whiteShortCastle) {
                    if (pieces[start.getX() + 1][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() + 2][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() + 3][start.getY()] == ChessPiece.WHITE_ROOK)
                        locs.add(new Location(start.getX() + 2, start.getY()));
                }
                if (whiteLongCastle) {
                    if (pieces[start.getX() - 1][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() - 2][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() - 3][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() - 4][start.getY()] == ChessPiece.WHITE_ROOK)
                        locs.add(new Location(start.getX() - 2, start.getY()));
                }
            }
            else {
                if (blackShortCastle) {
                    if (pieces[start.getX() + 1][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() + 2][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() + 3][start.getY()] == ChessPiece.BLACK_ROOK)
                        locs.add(new Location(start.getX() + 2, start.getY()));
                }
                if (blackLongCastle) {
                    if (pieces[start.getX() - 1][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() - 2][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() - 3][start.getY()] == ChessPiece.EMPTY &&
                            pieces[start.getX() - 4][start.getY()] == ChessPiece.BLACK_ROOK)
                        locs.add(new Location(start.getX() - 2, start.getY()));
                }
            }
        }
        return locs;
    }

    private boolean isKingInCheck(boolean whiteKing) {

        int kingRank = -1;
        int kingFile = -1;

        ArrayList<Location> attackers = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                if (pieces[i][j] == ChessPiece.WHITE_KING || pieces[i][j] == ChessPiece.BLACK_KING) {
                    if (isPieceWhite(pieces[i][j]) == whiteKing) {
                        kingRank = i;
                        kingFile = j;
                        continue;
                    }
                }

                if (pieces[i][j] != ChessPiece.EMPTY && isPieceWhite(pieces[i][j]) != whiteKing) {
                    attackers.add(new Location(i, j));
                }
            }

            for (Location loc : attackers) {
                if (getLegalDestinations(loc).contains(new Location(kingRank, kingFile))) {
                    return true;
                }
            }
        }

        return false;
    }

    public MoveSnapshot buildMove(Location startSquare, Location endSquare, ChessPiece promotion) {

        ChessPiece movingPiece = pieces[startSquare.getX()][startSquare.getY()];
        ChessPiece capturedPiece = pieces[endSquare.getX()][endSquare.getY()];
        boolean castles = false;
        boolean EP = false;
        boolean[] lossOfCastles = {false, false, false, false};

        if (movingPiece == ChessPiece.WHITE_KING || movingPiece == ChessPiece.BLACK_KING) {
            if (Math.abs(startSquare.getX() - endSquare.getX()) > 1) {
                castles = true;
            }
            else {
                if (isPieceWhite(movingPiece)) {
                    lossOfCastles[0] = whiteShortCastle;
                    lossOfCastles[1] = whiteLongCastle;
                }
                else {
                    lossOfCastles[2] = blackShortCastle;
                    lossOfCastles[3] = blackLongCastle;
                }
            }
        }
        else if (movingPiece == ChessPiece.WHITE_ROOK || movingPiece == ChessPiece.BLACK_ROOK) {
            if (startSquare.getX() == 0 && startSquare.getY() == 0) {
                lossOfCastles[1] = whiteLongCastle;
            }
            else if (startSquare.getX() == 7 && startSquare.getY() == 0) {
                lossOfCastles[0] = whiteShortCastle;
            }
            else if (startSquare.getX() == 0 && startSquare.getY() == 7) {
                lossOfCastles[3] = blackLongCastle;
            }
            else if (startSquare.getX() == 7 && startSquare.getY() == 7) {
                lossOfCastles[2] = blackShortCastle;
            }
        }
        else if (movingPiece == ChessPiece.WHITE_PAWN || movingPiece == ChessPiece.BLACK_PAWN) {
            if (capturedPiece == ChessPiece.EMPTY && startSquare.getX() != endSquare.getX()) {
                EP = true;
            }
        }
        MoveSnapshot snap = new MoveSnapshot(startSquare, endSquare, movingPiece, capturedPiece, EP, castles, promotion);
        snap.setNotation(getNotation(snap));
        snap.setLossOfCastles(lossOfCastles);
        return snap;
    }

    public MoveSnapshot buildMove(String notation) {

        if (notation.equals("O-O")) {
            if (whiteToMove) {
                return new MoveSnapshot(
                        new Location(4, 0),
                        new Location(6, 0),
                        ChessPiece.WHITE_KING,
                        ChessPiece.EMPTY,
                        false,
                        true,
                        ChessPiece.EMPTY
                ).setNotation(notation);
            }
            else {
                return new MoveSnapshot(
                        new Location(4, 7),
                        new Location(6, 7),
                        ChessPiece.BLACK_KING,
                        ChessPiece.EMPTY,
                        false,
                        true,
                        ChessPiece.EMPTY
                ).setNotation(notation);
            }
        }
        else if (notation.equals("O-O-O")) {
            if (whiteToMove) {
                return new MoveSnapshot(
                        new Location(4, 0),
                        new Location(2, 0),
                        ChessPiece.WHITE_KING,
                        ChessPiece.EMPTY,
                        false,
                        true,
                        ChessPiece.EMPTY
                ).setNotation(notation);
            }
            else {
                return new MoveSnapshot(
                        new Location(4, 7),
                        new Location(2, 7),
                        ChessPiece.BLACK_KING,
                        ChessPiece.EMPTY,
                        false,
                        true,
                        ChessPiece.EMPTY
                ).setNotation(notation);
            }
        }
        else {

            ChessPiece promotion = ChessPiece.EMPTY;

            if (notation.contains("=")) {
                promotion = pieceFromLetter(notation.split("=")[1].charAt(0), whiteToMove);
                notation = notation.split("=")[0];
            }

            ChessPiece piece = pieceFromLetter(notation.charAt(0), whiteToMove);
            int index = notation.length() - 1;
            int rank = -1;

            while (!Character.isDigit(notation.charAt(index))) {
                index--;
            }
            rank = Integer.parseInt("" + notation.charAt(index));
            int file = columnFromLetter(notation.charAt(index - 1));
            if (rank == -1) {
                return null;
            }
            rank--;

            Location destination = new Location(file, rank);

            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (pieces[i][j] == piece) {

                        Location potLoc = new Location(i, j);
                        String plainMove = notation.substring(0, index + 1);
                        if (plainMove.length() >= 4 && plainMove.charAt(1) != 'x') {

                            if (Character.isLetter(plainMove.charAt(1))) {
                                int fileReq = columnFromLetter(plainMove.charAt(1));
                                if (i != fileReq)
                                    continue;
                            }
                            else if (Character.isDigit(plainMove.charAt(1))) {
                                int rankReq = Integer.parseInt("" + plainMove.charAt(1)) - 1;
                                if (j != rankReq)
                                    continue;
                            }
                        }
                        else if (piece == ChessPiece.WHITE_PAWN || piece == ChessPiece.BLACK_PAWN) {
                            if (plainMove.charAt(1) == 'x') {
                                if (!(potLoc.getX() == columnFromLetter(plainMove.charAt(0))))
                                    continue;
                            }
                        }
                        if (getLegalDestinations(potLoc).contains(destination)) {

                            return buildMove(potLoc, destination, promotion);
                        }
                    }
                }
            }
        }
        return null;
    }

    public String getNotation(MoveSnapshot move) {
        String output = "";

        ChessPiece movedPiece = move.getMovedPiece();
        ChessPiece promotion = move.getPromotion();

        if (movedPiece == ChessPiece.WHITE_KING || movedPiece == ChessPiece.BLACK_KING) {
            if (Math.abs(move.getEndSquare().getX() - move.getStartSquare().getX()) > 1) {
                if (move.getEndSquare().getX() > move.getStartSquare().getX()) {
                    return "O-O";
                }
                else {
                    return "O-O-O";
                }
            }
        }

        String rankNum = Integer.toString((move.getEndSquare().getY() + 1));
        char fileLet = letterFromFile(move.getEndSquare().getX());
        String pieceLet = letterFromPiece(movedPiece);

        boolean capture = pieces[move.getEndSquare().getX()][move.getEndSquare().getY()] != ChessPiece.EMPTY;
        output += pieceLet;


        if (capture && (movedPiece == ChessPiece.WHITE_PAWN || movedPiece == ChessPiece.BLACK_PAWN)) {
            output += letterFromFile(move.getStartSquare().getX());
        }


        if (movedPiece == ChessPiece.BLACK_KNIGHT || movedPiece == ChessPiece.WHITE_KNIGHT ||
                movedPiece == ChessPiece.BLACK_ROOK || movedPiece == ChessPiece.WHITE_ROOK ||
                movedPiece == ChessPiece.WHITE_QUEEN || movedPiece == ChessPiece.BLACK_QUEEN) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Location loc = new Location(i, j);
                    if (pieces[i][j] == movedPiece && !loc.equals(move.getStartSquare())) {
                        if (getLegalDestinations(loc).contains(move.getEndSquare())) {
                            if (j != move.getStartSquare().getX()) {
                                output += letterFromFile(move.getStartSquare().getX());
                                break;
                            }
                            else {
                                output += move.getStartSquare().getY() + 1;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (capture) {
            output += 'x';
        }

        output += fileLet;
        output += rankNum;

        if (promotion != ChessPiece.EMPTY) {
            output += '=' + letterFromPiece(promotion);
        }


        ChessPosition future = (ChessPosition) this.clone();
        //future.makeMoveOnBoard(move);
        if (future.isKingInCheck(!isPieceWhite(movedPiece))) {
            output += '+';
        }

        return output;
    }

    private static char letterFromFile(int file) {
        switch (file) {
            case 0:
                return 'a';
            case 1:
                return 'b';
            case 2:
                return 'c';
            case 3:
                return 'd';
            case 4:
                return 'e';
            case 5:
                return 'f';
            case 6:
                return 'g';
            case 7:
                return 'h';
            default:
                return 'i';
        }
    }

    private static String letterFromPiece(ChessPiece piece) {
        if (piece == ChessPiece.WHITE_ROOK || piece == ChessPiece.BLACK_ROOK) {
            return "R";
        }
        else if (piece == ChessPiece.WHITE_KNIGHT || piece == ChessPiece.BLACK_KNIGHT) {
            return "N";
        }
        else if (piece == ChessPiece.WHITE_BISHOP || piece == ChessPiece.BLACK_BISHOP) {
            return "B";
        }
        else if (piece == ChessPiece.WHITE_QUEEN || piece == ChessPiece.BLACK_QUEEN) {
            return "Q";
        }
        else if (piece == ChessPiece.WHITE_KING || piece == ChessPiece.BLACK_KING) {
            return "K";
        }
        else {
            return "";
        }
    }

    private static ChessPiece pieceFromLetter(char letter, boolean white) {

        if (letter == 'R' && white) {
            return ChessPiece.WHITE_ROOK;
        }
        else if (letter == 'R' && !white) {
            return ChessPiece.BLACK_ROOK;
        }
        else if (letter == 'N' && white) {
            return ChessPiece.WHITE_KNIGHT;
        }
        else if (letter == 'N' && !white) {
            return ChessPiece.BLACK_KNIGHT;
        }
        else if (letter == 'B' && white) {
            return ChessPiece.WHITE_BISHOP;
        }
        else if (letter == 'B' && !white) {
            return ChessPiece.BLACK_BISHOP;
        }
        else if (letter == 'Q' && white) {
            return ChessPiece.WHITE_QUEEN;
        }
        else if (letter == 'Q' && !white) {
            return ChessPiece.BLACK_QUEEN;
        }
        else if (letter == 'K' && white) {
            return ChessPiece.WHITE_KING;
        }
        else if (letter == 'K' && !white) {
            return ChessPiece.BLACK_KING;
        }
        else if (white) {
            return ChessPiece.WHITE_PAWN;
        }
        else {
            return ChessPiece.BLACK_PAWN;
        }
    }

    private static int columnFromLetter(char letter) {
        switch (letter) {
            case 'a':
                return 0;
            case 'b':
                return 1;
            case 'c':
                return 2;
            case 'd':
                return 3;
            case 'e':
                return 4;
            case 'f':
                return 5;
            case 'g':
                return 6;
            case 'h':
                return 7;
            default:
                return -1;
        }
    }

    public static boolean isPieceWhite(ChessPiece piece) {

        switch (piece) {
            case WHITE_PAWN:
            case WHITE_ROOK:
            case WHITE_KNIGHT:
            case WHITE_BISHOP:
            case WHITE_QUEEN:
            case WHITE_KING:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object clone() {
        ChessPosition copy = new ChessPosition();
        copy.pieces = clonePieces();
        copy.lastMove = (lastMove != null) ? (MoveSnapshot) lastMove.clone() : null;
        copy.whiteShortCastle = whiteShortCastle;
        copy.whiteLongCastle = whiteLongCastle;
        copy.whiteToMove = whiteToMove;
        copy.blackLongCastle = blackLongCastle;
        copy.blackShortCastle = blackShortCastle;

        return copy;
    }

    private ChessPiece[][] clonePieces() {
        ChessPiece[][] ps = new ChessPiece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ps[i][j] = pieces[i][j];
            }
        }
        return ps;
    }

    public ChessPiece[][] getPieces() {
        return pieces;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public boolean isWhiteShortCastle() {
        return whiteShortCastle;
    }

    public boolean isWhiteLongCastle() {
        return whiteLongCastle;
    }

    public boolean isBlackShortCastle() {
        return blackShortCastle;
    }

    public boolean isBlackLongCastle() {
        return blackLongCastle;
    }

    public MoveSnapshot getLastMove() {
        return lastMove;
    }
}
