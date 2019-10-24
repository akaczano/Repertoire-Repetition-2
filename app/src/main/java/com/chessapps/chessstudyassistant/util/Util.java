package com.chessapps.chessstudyassistant.util;

import com.chessapps.chessstudyassistant.R;
import com.chessapps.chessstudyassistant.model.ChessPiece;

public class Util {

    public static int pieceToResource(ChessPiece piece){
        switch (piece){

            case WHITE_PAWN:
                return R.drawable.white_pawn;
            case WHITE_ROOK:
                return R.drawable.white_rook;
            case WHITE_KNIGHT:
                return R.drawable.white_knight;
            case WHITE_BISHOP:
                return R.drawable.white_bishop;
            case WHITE_QUEEN:
                return R.drawable.white_queen;
            case WHITE_KING:
                return R.drawable.white_king;
            case BLACK_PAWN:
                return R.drawable.black_pawn;
            case BLACK_ROOK:
                return R.drawable.black_rook;
            case BLACK_KNIGHT:
                return R.drawable.black_knight;
            case BLACK_BISHOP:
                return R.drawable.black_bishop;
            case BLACK_QUEEN:
                return R.drawable.black_queen;
            case BLACK_KING:
                return R.drawable.black_king;
            default:
                return -1;
        }
    }

}
