package com.example.pong;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Player {
    private int paddleWidth;
    private int paddleHeight;
    public int score;
    private Paint paint;

    public RectF bounds;

    public Player(int paddleWidth, int paddleHeight, Paint paint) {
        this.paddleWidth = paddleWidth;
        this.paddleHeight = paddleHeight;
        this.paint = paint;
        score = 0;
        bounds = new RectF(0,0, paddleWidth, paddleHeight);
    }

    public void draw(Canvas canvas){
        canvas.drawRoundRect(bounds, 5, 5, paint);
    }

    public int getPaddleWidth() {
        return paddleWidth;
    }

    public int getPaddleHeight() {
        return paddleHeight;
    }

    public void setPaddleWidth(int Width){
        this.paddleWidth = Width;
    }

    @Override
    public String toString() {
        return ("Width = "+ paddleWidth +
                "Height = "+ paddleHeight +
                "score = "+ score +
                "Top = "+ bounds.top +
                "Left = " + bounds.left);
    }
}
