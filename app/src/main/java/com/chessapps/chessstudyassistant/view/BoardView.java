package com.chessapps.chessstudyassistant.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.chessapps.chessstudyassistant.R;
import com.chessapps.chessstudyassistant.util.Util;
import com.chessapps.chessstudyassistant.model.ChessPiece;
import com.chessapps.chessstudyassistant.model.ChessPosition;
import com.chessapps.chessstudyassistant.model.Location;
import com.chessapps.chessstudyassistant.observer.MoveInputListener;
import com.chessapps.chessstudyassistant.model.MoveSnapshot;

import androidx.constraintlayout.widget.ConstraintLayout;


public class BoardView extends View {

    private ChessPosition position;

    private int squareWidth;

    private boolean dragging = false;
    private Location underDrag = null;
    private int mouseX = -1;
    private int mouseY = -1;

    private int marginLeft = 30;
    private int marginTop = 30;

    private MoveInputListener listener;

    private boolean flipped = false;

    private MoveSnapshot wrongMove;
    private MoveSnapshot rightMove;

    public BoardView(Context context, AttributeSet attributeSet){

        super(context, attributeSet);
        position = new ChessPosition();

    }

    public void addMoveInputListener(MoveInputListener listener){
        this.listener = listener;
    }



    public void touchUpdate(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            int x = (int)(event.getX()-marginLeft)/squareWidth;
            int y = 7 - (int)(event.getY()-this.getY()-marginTop)/squareWidth;
            if (flipped){
                x = 7-x;
                y = 7-y;
            }
            if (y == -1)
                y = 0;
            else if (y == 8){
                y = 7;
            }
            if (x < 0 || x > 7 || y < 0 || y > 7) {
                this.invalidate();
                return;
            }
            dragging = true;
            underDrag = new Location(x, y);
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            if (!dragging)
                return;
            dragging = false;
            int x = (int)(event.getX()-marginLeft)/squareWidth;
            int y = 7 - (int)(event.getY()-marginTop)/squareWidth;
            if (flipped){
                x = 7-x;
                y = 7-y;
            }
            if (x < 0 || x > 7 || y < 0 || y > 7) {
                this.invalidate();
                return;
            }
            final MoveSnapshot move = position.buildMove(underDrag, new Location(x,y), ChessPiece.EMPTY);
            final boolean legal = position.isMoveLegal(move);

            if ((move.getEndSquare().getY() == 0 || move.getEndSquare().getY() == 7) && legal){
                if (move.getMovedPiece() == ChessPiece.WHITE_PAWN || move.getMovedPiece() == ChessPiece.BLACK_PAWN){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                    builder.setTitle("Promote pawn");
                    View buttons = LayoutInflater.from(getContext()).inflate(R.layout.promotion_dialog, null, false);
                    builder.setView(buttons);

                    final AlertDialog dialog = builder.create();

                    final boolean white = move.getMovedPiece() == ChessPiece.WHITE_PAWN;

                    if (!white){
                        ((ImageButton)buttons.findViewById(R.id.queenButton)).setImageResource(R.drawable.black_queen);
                        ((ImageButton)buttons.findViewById(R.id.rookButton)).setImageResource(R.drawable.black_rook);
                        ((ImageButton)buttons.findViewById(R.id.knightButton)).setImageResource(R.drawable.black_knight);
                        ((ImageButton)buttons.findViewById(R.id.bishopButton)).setImageResource(R.drawable.black_bishop);
                    }

                    View.OnClickListener clickHandler = new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            if (v.getId() == R.id.queenButton){
                                move.setPromotion(white ? ChessPiece.WHITE_QUEEN : ChessPiece.BLACK_QUEEN);

                            }
                            else if (v.getId() == R.id.rookButton){
                                move.setPromotion(white ? ChessPiece.WHITE_ROOK : ChessPiece.BLACK_ROOK);
                            }
                            else if (v.getId() == R.id.knightButton){
                                move.setPromotion(white ? ChessPiece.WHITE_KNIGHT : ChessPiece.BLACK_KNIGHT);
                            }
                            else if (v.getId() == R.id.bishopButton){
                                move.setPromotion(white ? ChessPiece.WHITE_BISHOP : ChessPiece.BLACK_BISHOP);
                            }
                            move.setNotation(position.getNotation(move));
                            dialog.cancel();
                        }
                    };


                    buttons.findViewById(R.id.queenButton).setOnClickListener(clickHandler);
                    buttons.findViewById(R.id.rookButton).setOnClickListener(clickHandler);
                    buttons.findViewById(R.id.knightButton).setOnClickListener(clickHandler);
                    buttons.findViewById(R.id.bishopButton).setOnClickListener(clickHandler);


                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (legal && listener.moveEntered(move, true)) {
                                position.makeMoveOnBoard(move);
                            }
                            invalidate();
                        }
                    });
                    dialog.show();
                    return;
                }
            }

            if (legal && listener.moveEntered(move, true)){
                position.makeMoveOnBoard(move);
            }
            this.invalidate();
        }
        else {
            mouseX = (int) event.getX();
            mouseY = (int) event.getY();
            this.invalidate();
        }
    }

    public void updatePosition(ChessPosition position){
        this.position = position;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.squareWidth = (getWidth()-2*marginLeft)/8;
        this.setLayoutParams(new ConstraintLayout.LayoutParams(getWidth(), getWidth()));

        Drawable board = getResources().getDrawable(R.drawable.board, null);
        board.setBounds(marginLeft, marginTop, marginLeft+(getWidth()-2*marginLeft), marginTop+(getWidth()-2*marginLeft));
        board.draw(canvas);

        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                int resId = Util.pieceToResource(position.getPieces()[i][j]);
                if (resId == -1)
                    continue;
                Drawable drawable = getResources().getDrawable(resId, null);
                int xCoord = flipped ? (7-i) : i;
                int yCoord = flipped ? j : (7-j);
                int x = xCoord*squareWidth + marginLeft;
                int y = yCoord*squareWidth + marginTop;

                if (dragging){
                    if (underDrag.getX() == i && underDrag.getY()==j){
                        x = mouseX-(squareWidth/2);
                        y = mouseY-(squareWidth/2);
                    }
                }

                drawable.setBounds(x, y, x+squareWidth, y+squareWidth);
                drawable.draw(canvas);
            }
        }

        if (this.position.getLastMove() != null){
            renderMove(canvas, this.position.getLastMove(), Color.BLUE);
        }
        if (this.wrongMove != null){
            renderMove(canvas, this.wrongMove, Color.RED);
            renderMove(canvas, this.rightMove, Color.GREEN);
            this.wrongMove = null;
        }
    }

    public void renderWrongMove(MoveSnapshot wrongMove, MoveSnapshot rightMove){
        this.wrongMove = wrongMove;
        this.rightMove = rightMove;
        this.invalidate();
    }


    public void renderMove(Canvas canvas, MoveSnapshot move, int color){

        int x1 = flipped ? 7 - move.getStartSquare().getX() : move.getStartSquare().getX();
        int x2 = flipped ? 7 - move.getEndSquare().getX() : move.getEndSquare().getX();

        int y1 = flipped ? move.getStartSquare().getY() : 7 - move.getStartSquare().getY();
        int y2 = flipped ? move.getEndSquare().getY() : 7 - move.getEndSquare().getY();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(5);

        int x1Converted = x1 * squareWidth + marginLeft;
        int x2Converted = x2 * squareWidth + marginLeft;

        int y1Converted = y1 * squareWidth + marginTop;
        int y2Converted = y2 * squareWidth + marginTop;


        canvas.drawRect(x1Converted+5, y1Converted+5, x1Converted + squareWidth, y1Converted + squareWidth, paint);
        canvas.drawRect(x2Converted+5, y2Converted+5, x2Converted + squareWidth, y2Converted + squareWidth, paint);
    }


    public void flip (){
        flipped = !flipped;
        this.invalidate();
    }

}
