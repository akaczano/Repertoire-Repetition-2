package com.chessapps.chessstudyassistant.observer;


import com.chessapps.chessstudyassistant.model.MoveSnapshot;

public interface MoveInputListener {

    public boolean moveEntered(MoveSnapshot move, boolean legal);

}
