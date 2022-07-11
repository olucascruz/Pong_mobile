package com.example.pong;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Ball {
    public float position_x;
    public float position_y;
    public float velocity_x;
    public float velocity_y;

    private int radius;
    private Paint paint;

    public Ball(int radius, Paint paint) {
        this.paint = paint;
        this.radius = radius;
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(position_x, position_y, radius, paint);
    }

    public void moveBall(Canvas canvas){
        position_x += velocity_x;
        position_y += velocity_y;

        if(position_y<radius){
           position_y = radius;
        }else if(position_y+radius > canvas.getHeight()){
            position_y = canvas.getHeight() - radius - 1;
        }
    }

    public int getRadius() {
        return radius;
    }

    public float getPosition_x() {
        return position_x;
    }

    public float getPosition_y() {
        return position_y;
    }

    public void setPosition_x(float position_x) {
        this.position_x = position_x;
    }
    

    public void setPosition_y(float position_y) {
        this.position_y = position_y;
    }
    
    public void setVelocity_x(float velocity_x) {
        this.velocity_x = velocity_x;
    }
    
    public void setVelocity_y(float velocity_y) {
        this.velocity_y = velocity_y;
    }

    public float getVelocity_x() {
        return velocity_x;
    }

    public float getVelocity_y() {
        return velocity_y;
    }

    @Override
    public String toString() {
        return ("Position X = "+ position_x +
                "Position Y = "+ position_y +
                "Velocity X = "+ velocity_x +
                "Velocity Y = "+ velocity_y );
    }
}