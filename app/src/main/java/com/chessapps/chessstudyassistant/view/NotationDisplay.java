package com.chessapps.chessstudyassistant.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

import com.chessapps.chessstudyassistant.model.Variation;
import com.chessapps.chessstudyassistant.observer.MoveSelectionListener;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;


public class NotationDisplay extends View {

    private Variation variation;
    private int cursorX;
    private int cursorY;

    private int selectedVariation;
    private int selectedMoveIndex;

    private ArrayList<MoveMapping> moveMappings = new ArrayList<>();

    private MoveSelectionListener listener;

    private Paint paint;

    public NotationDisplay(Context context, AttributeSet attrs){

        super(context, attrs);

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_SCROLL){
            return super.onTouchEvent(event);
        }
        for (MoveMapping map : moveMappings){
            if (event.getX() >= map.x && event.getX()-map.x < map.width){
                if (event.getY() >= map.y-map.height && event.getY() < map.y){
                    this.selectedMoveIndex = map.moveIndex;
                    this.selectedVariation = map.variationID;
                    this.listener.moveSelected(map.variationID, map.moveIndex);
                    this.invalidate();
                }
            }
        }

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        cursorX = 10;
        cursorY = 50;
        this.paint = new Paint();
        paint.setTextSize(45);


        displayVariation(variation, canvas);
        paint.setStyle(Paint.Style.STROKE);
        View parent = (View) getParent().getParent();
        int parentHeight = parent.getHeight();
        this.setLayoutParams(new ConstraintLayout.LayoutParams(getWidth(), cursorY < parent.getHeight() ? parent.getHeight() : cursorY+20));
    }

    public void setVariation(Variation v){
        this.variation = v;
    }

    public void setListener (MoveSelectionListener listener){
        this.listener = listener;
    }

    public void selectMove(int variationID, int moveIndex){
        this.selectedVariation = variationID;
        this.selectedMoveIndex = moveIndex;
    }

    private void displayVariation(Variation variation, Canvas canvas){
        boolean flagNewVar = false;
        for (int i = variation.getStartIndex(); i < variation.getMainLine().size()+variation.getStartIndex(); i++){
            String text = ((i % 2 == 0) ? (i / 2 + 1) + "." : "") + variation.getMainLine().get(i-variation.getStartIndex()).getNotation();
            if (flagNewVar) {
                if (i % 2 != 0) {
                    text = (i / 2 + 1) + "..." + text;
                }
                flagNewVar = false;
            }
            if (variation.getParentID() != -1 && variation.getStartIndex() == i) {
                if (i % 2 != 0) {
                    text = (i / 2 + 1) + "..." + text;
                }
                text = "(" + text;
            }

            if (variation.getParentID() != -1 && i + 1 == variation.getMainLine().size()+variation.getStartIndex())
                text += ")";




            Paint.FontMetrics metrics = paint.getFontMetrics();
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int width = bounds.width();
            int height = bounds.height();


            if (cursorX + width > this.getWidth()) {
                cursorX = 10;
                cursorY += height + 30;
            }
            if (variation.getID() == selectedVariation && i-variation.getStartIndex() == selectedMoveIndex)
                paint.setColor(Color.BLUE);
            moveMappings.add(new MoveMapping(cursorX, cursorY, width, height, variation.getID(), i-variation.getStartIndex()));
            canvas.drawText(text, cursorX, cursorY, paint);
            paint.setColor(Color.BLACK);
            cursorX += width + 25;

            for (Variation v : variation.getSideLines()){
                if (v.getStartIndex()==i) {
                    displayVariation(v, canvas);
                    flagNewVar = true;
                }
            }

        }

    }

}
