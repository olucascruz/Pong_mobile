package com.example.pong;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class PongTable extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = PongTable.class.getSimpleName();

    private GameThread mGame;
    private TextView mStatus;
    private TextView mScoreOpponent;
    private TextView mScorePlayer;

    private Player mPlayer;
    private Player mOpponent;
    private Ball mBall;
    private int ballColor = R.color.black;
    private Paint mNetPaint;
    private Paint mTableBoundPaint;
    private int mTableWidth;
    private int mTableHeight;
    private Context mContext;

    SurfaceHolder mHolder;
    public static float PHY_PADDLE_SPEED = 20.0f;
    public static float PHY_BALL_SPEED = 20.0f;

    private float mAiMoveProbability;
    private boolean moving = false;
    private float mLastTouchY;


    public void initPongTable(Context ctx, AttributeSet attr){
        mContext = ctx;
        mHolder = getHolder();
        mHolder.addCallback(this);


        // Game loop
        mGame = new GameThread(this.getContext(), mHolder, this,
                new Handler(){
                    @Override
                    public void handleMessage(@NonNull Message msg){
                        super.handleMessage(msg);
                        mStatus.setVisibility(msg.getData().getInt("visibility"));
                        mStatus.setText(msg.getData().getString("text"));
                    }
                },
                new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        mScorePlayer.setText(msg.getData().getString("player"));
                        mScoreOpponent.setText(msg.getData().getString("opponent"));
                    }

                }
                );

        TypedArray a = ctx.obtainStyledAttributes(attr, R.styleable.PongTable);

        int paddleHeight = a.getInteger(R.styleable.PongTable_paddleHeight, 250);
        int paddleWidth = a.getInteger(R.styleable.PongTable_paddleWidth, 100);
        int ballRadius = a.getInteger(R.styleable.PongTable_ballRadius, 20);

        Paint playerPaint = new Paint();
        playerPaint.setAntiAlias(true);
        playerPaint.setColor(ContextCompat.getColor(mContext, R.color.orange));
        mPlayer = new Player(paddleWidth, paddleHeight, playerPaint);


        Paint opponentPaint = new Paint();
        opponentPaint.setAntiAlias(true);
        opponentPaint.setColor(ContextCompat.getColor(mContext, R.color.green));
        mOpponent = new Player(paddleWidth, paddleHeight, opponentPaint);

        Paint ballPaint = new Paint();
        ballPaint.setAntiAlias(true);
        ballPaint.setColor(ContextCompat.getColor(mContext, ballColor));
        mBall = new Ball(ballRadius, ballPaint);


        mNetPaint = new Paint();
        mNetPaint.setAntiAlias(true);
        mNetPaint.setColor(ContextCompat.getColor(mContext, R.color.white));
        mNetPaint.setAlpha(80);
        mNetPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mNetPaint.setStrokeWidth(10.f);
        mNetPaint.setPathEffect(new DashPathEffect(new float[]{5,5}, 0));

        //Draw Bounds
        mTableBoundPaint = new Paint();
        mTableBoundPaint.setAntiAlias(true);
        mTableBoundPaint.setColor(ContextCompat.getColor(mContext, R.color.white));
        mTableBoundPaint.setStyle(Paint.Style.STROKE);
        mTableBoundPaint.setStrokeWidth(15.0f);

        mAiMoveProbability = 0.8f;


    }

    public PongTable(Context context, AttributeSet attrs) {

        super(context, attrs);
        initPongTable(context, attrs);
    }

    public PongTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPongTable(context, attrs);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawColor(ContextCompat.getColor(mContext, R.color.purple_500));
        canvas.drawRect(0,0,mTableWidth,mTableHeight,mTableBoundPaint);

        int middle = mTableWidth/2;
        canvas.drawLine(middle, 1, middle, mTableHeight-1,mNetPaint);


        mGame.setScoreText(String.valueOf(mPlayer.score), String.valueOf(mOpponent.score));


        mPlayer.draw(canvas);
        mOpponent.draw(canvas);
        mBall.draw(canvas);
    }

    private void doAI(){
        float paddleMiddle = mOpponent.bounds.top + mOpponent.getPaddleHeight()*2f;
        String moveAI = "WAIT";

        if(mBall.getPosition_y() >= mOpponent.bounds.top+100 &&
                mBall.getPosition_y()+mBall.getRadius()*2 <= mOpponent.bounds.top + mOpponent.getPaddleHeight()-100){
            moveAI = "WAIT";
        }
        else if(mOpponent.bounds.top+100 > mBall.getPosition_y()+mBall.getRadius()*2) {
            moveAI = "UP";
        }else if(mOpponent.bounds.top + mOpponent.getPaddleHeight()-100 < mBall.getPosition_y()+mBall.getRadius()) {
            moveAI = "DOWN";
        }



        if(mBall.getPosition_x() > mTableWidth/2.5f) {
            switch (moveAI){
                case "UP":
                    movePlayer(mOpponent, mOpponent.bounds.left, mOpponent.bounds.top - PHY_PADDLE_SPEED);
                    break;
                case "DOWN":
                    movePlayer(mOpponent, mOpponent.bounds.left, mOpponent.bounds.top + PHY_PADDLE_SPEED);
                    break;
                case "WAIT":
                    movePlayer(mOpponent, mOpponent.bounds.left, mOpponent.bounds.top);
                    break;
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        mGame.setRunning(true);
        mGame.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
        mTableWidth = width;
        mTableHeight = height;
        mGame.setUpNewRound();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        boolean retry = true;
        mGame.setRunning(false);
        while(retry){
            try{
                mGame.join();
                retry = false;
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(!mGame.SensorsOn()){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(mGame.isBetweenRounds()){
                        mGame.setState(GameThread.STATE_RUNNING);
                    }else{
                        if (isTouchOnPaddle(event, mPlayer)) {
                            moving = true;
                            mLastTouchY = event.getY();
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (moving){
                        float y = event.getY();
                        float dy = y- mLastTouchY;
                        mLastTouchY = y;
                        movePlayerPaddle(dy,mPlayer);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    moving = false;
                    break;
            }
        }else {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                if(mGame.isBetweenRounds()){
                    mGame.setState(GameThread.STATE_RUNNING);
                }
            }
        }

        return true;
    }

    public GameThread getGame(){
        return mGame;
    }

    public void movePlayerPaddle(float dy, Player player){
        synchronized (mHolder){
            movePlayer(player, player.bounds.left, player.bounds.top + dy);
        }

    }

    public boolean isTouchOnPaddle(MotionEvent event, Player mPlayer){
        return  mPlayer.bounds.contains(event.getX(),event.getY());
    }

    public synchronized void movePlayer(Player player, float left, float top){
        if(left<2){
            left = 2;
        } else if(left+player.getPaddleWidth()> mTableWidth-2){
            left = mTableWidth-player.getPaddleWidth()-2;
        }

        if(top<0){
            top=0;
        }else if(top + player.getPaddleHeight() >= mTableHeight){
            top = mTableHeight-player.getPaddleHeight();
        }

        player.bounds.offsetTo(left,top);

    }
    public void update(Canvas c){

        if(mBall.getVelocity_x() >= 40){
            ballColor = R.color.red;
        }
        if(checkCollisionPlayer(mPlayer, mBall)){
            handleCollision(mPlayer, mBall);
            if(mBall.velocity_x < 45) {
                getBall().velocity_x++;
            }
            if(mBall.getPosition_y() < mTableHeight/2f) {
                mBall.setVelocity_y(new Random().nextInt(12));
            }else{
                mBall.setVelocity_y((new Random().nextInt(12)) * -1);
            }
        }
         if(checkCollisionOpponent(mOpponent, mBall)){
             if(mBall.velocity_x < 45) {
                 getBall().velocity_x++;
             }
             if(mBall.getPosition_y() < mTableHeight/2f) {
                 mBall.setVelocity_y(new Random().nextInt(12));
             }else{
                 mBall.setVelocity_y((new Random().nextInt(12)) * -1);
             }
             handleCollision(mOpponent, mBall);
        } if(checkCollisionWithTopOrButtonWall()){
            mBall.velocity_y = -mBall.velocity_y;
        }else if(checkCollisionWithLeftWall()){
            mGame.setState(GameThread.STATE_LOSE);
            return;
        }else if(checkCollisionWithRightWall()){
            mGame.setState(GameThread.STATE_WIN);
            return;
        }
        // Collision Detection Code
        doAI();
        mBall.moveBall(c);
    }

    private boolean checkCollisionPlayer(Player player, Ball ball){
        return (ball.getPosition_x() < player.bounds.right &&
                ball.getPosition_y() > player.bounds.top &&
                ball.getPosition_y() < player.bounds.bottom);
    }

    private boolean checkCollisionOpponent(Player player, Ball ball){
        return (ball.getPosition_x() > player.bounds.left &&
                ball.getPosition_y() > player.bounds.top &&
                ball.getPosition_y() < player.bounds.bottom);
    }

    private boolean checkCollisionWithTopOrButtonWall(){
        return ((mBall.getPosition_y() <= mBall.getRadius()) || (mBall.getPosition_y() + mBall.getRadius() >= mTableHeight - 1));
    }

    private boolean checkCollisionWithLeftWall(){
        return mBall.getPosition_x() <= mBall.getRadius();
    }

    private boolean checkCollisionWithRightWall(){
        return mBall.getPosition_x() + mBall.getRadius() >= mTableWidth - 1;
    }

    private void handleCollision(Player player, Ball ball){
        ball.velocity_x = -ball.velocity_x;


        if(player == mPlayer){
            ball.setPosition_x(mPlayer.bounds.right + ball.getRadius());
        }else if (player == mOpponent){
            ball.setPosition_x(mOpponent.bounds.left - ball.getRadius());
        }
    }



    public void setupTable(){
        placeBall();
        placePlayers();
    }


    private void placePlayers(){
        mPlayer.bounds.offsetTo(30, (mTableHeight- mPlayer.getPaddleHeight())/4f);

        mOpponent.bounds.offsetTo(mTableWidth-mOpponent.getPaddleWidth()-30,
                                    (mTableHeight - mOpponent.getPaddleHeight())/4f);
    }
    private void placeBall(){
        mBall.setPosition_x(mTableWidth/2f);
        mBall.setPosition_y(mTableHeight/2f);
        mBall.setVelocity_x(PHY_BALL_SPEED);
        mBall.setVelocity_y(new Random().nextInt(2));
    }

    public Player getPlayer(){
        return mPlayer;
    }
    public Player getOpponent(){
        return mOpponent;
    }
    public Ball getBall(){
        return mBall;
    }

    public void setScorePlayer(TextView view){
        mScorePlayer = view;
    }
    public void setScoreOpponent(TextView view){
        mScoreOpponent = view;
    }
    public void setStatusView(TextView view){
        mStatus = view;
    }
}
