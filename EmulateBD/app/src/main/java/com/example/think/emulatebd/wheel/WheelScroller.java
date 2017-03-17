package com.example.think.emulatebd.wheel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by HuangMei on 2016/12/14.
 */

public class WheelScroller {

    public interface ScrollingListener{
        void onScroll(int distance);
        void onStart();
        void onFinish();
        void onJustify();
    }

    private static final int SCROLLING_DURATION = 400;
    public static final int MIN_DELTA_FOR_SCROLLING = 1;

    private ScrollingListener listener;
    private Context context;

    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int lastScrollY;
    private float lastTouchedY;
    private boolean isScrollingPerformed;

    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

    public WheelScroller(ScrollingListener listener, Context context) {
        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);

        scroller = new Scroller(context);
        this.listener = listener;
        this.context = context;
    }

    public void setInterpolator(Interpolator interpolator){
        scroller.forceFinished(true);
        scroller = new Scroller(context, interpolator);
    }

    public void scroll(int distance, int time){
        scroller.forceFinished(true);

        lastScrollY = 0;
        scroller.startScroll(0, 0, 0, distance, time != 0 ? time : SCROLLING_DURATION);
        setNextMessage(MESSAGE_SCROLL);

        startScrolling();
    }

    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastTouchedY = event.getY();
                scroller.forceFinished(true);
                clearMessages();
                break;
            case MotionEvent.ACTION_MOVE:
                int distanceY = (int)(event.getY() - lastTouchedY);
                if (distanceY != 0){
                    startScrolling();
                    listener.onScroll(distanceY);
                    lastTouchedY = event.getY();
                }
                break;
        }
        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP){
            justify();
        }
        return true;
    }

    public void stopScrolling(){
        scroller.forceFinished(true);
    }

    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        public boolean onScroll(MotionEvent el, MotionEvent e2,  float distanceX, float distanceY){
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            lastScrollY = 0;
            final int maxY = 0x7FFFFFFF;
            final int minY = -maxY;
            scroller.fling(0, lastScrollY, 0, (int)-velocityX, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    private void setNextMessage(int message){
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    private void clearMessages(){
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    private Handler animationHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            lastScrollY = currY;
            if(delta != 0){
                listener.onScroll(delta);
            }

            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING){
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()){
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL){
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Justifies wheel
     */
    private void justify() {
        listener.onJustify();
        setNextMessage(MESSAGE_JUSTIFY);
    }

    /**
     * Starts scrolling
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            listener.onStart();
        }
    }

    /**
     * Finishes scrolling
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            listener.onFinish();
            isScrollingPerformed = false;
        }
    }
}
