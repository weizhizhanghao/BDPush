package com.example.think.emulatebd.swipelistview;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Created by HuangMei on 2016/12/9.
 */

public class SwipeListViewTouchListener implements View.OnTouchListener{

    private int swipeMode = SwipeListView.SWIPE_MODE_BOTH;
    private boolean swipeOpenOnLongPress = true;
    private boolean swipeClosesAllItemsWhenListMoves = true;

    private int swipeFrontView = 0;
    private int swipeBackView = 0;

    private Rect rect = new Rect();

    private int slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private long configShortAnimationTime;
    private long animationTime;

    private float leftOffset = 0;
    private float rightOffset = 0;

    private SwipeListView swipeListView;
    private int viewWidth = 1;

    private List<PendingDismissData> pendingDismisses = new ArrayList<>();
    private int dismissAnimationRefCount = 0;

    private float downX;
    private boolean swiping;
    private VelocityTracker velocityTracker;
    private int downPosition;
    private View parentView;
    private View frontView;
    private View backView;
    private boolean paused;

    private int swipeCurrentAction = SwipeListView.SWIPE_ACTION_NONE;
    private int swipeActionLeft = SwipeListView.SWIPE_ACTION_REVEAL;
    private int swipeActionRight = SwipeListView.SWIPE_ACTION_REVEAL;

    private List<Boolean> opened = new ArrayList<>();
    private List<Boolean> openedRight = new ArrayList<>();
    private boolean listViewMoving;

    public SwipeListViewTouchListener( SwipeListView swipeListView, int swipeFrontView, int swipeBackView) {
        this.swipeFrontView = swipeFrontView;
        this.swipeListView = swipeListView;
        this.swipeBackView = swipeBackView;

        ViewConfiguration vc = ViewConfiguration.get(swipeListView.getContext());
        slop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        configShortAnimationTime = swipeListView.getContext().getResources()
                .getInteger(android.R.integer.config_shortAnimTime);
        animationTime = configShortAnimationTime;
    }

    private void setParentView(View parentView){
        this.parentView = parentView;
    }

    private void setFrontView(View frontView){
        this.frontView = frontView;
        frontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeListView.onClickFrontView(downPosition);
            }
        });
        if (swipeOpenOnLongPress){
            frontView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return false;
                }
            });
        }
    }

    private void setBackView(View backView){
        this.backView = backView;
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public boolean isListViewMoving(){
        return listViewMoving;
    }

    public void setAnimatinTime(long animationTime){
        if (animationTime > 0){
            this.animationTime = animationTime;
        } else {
            this.animationTime = configShortAnimationTime;
        }
    }

    public void setRightOffset(float rightOffset){
        this.rightOffset = rightOffset;
    }

    public void setLeftOffset(float leftOffset){
        this.leftOffset = leftOffset;
    }

    public void setSwipeClosesAllItemsWhenListMoves(boolean swipeClosesAllItemsWhenListMoves){
        this.swipeClosesAllItemsWhenListMoves = swipeClosesAllItemsWhenListMoves;
    }

    public void setSwipeMode(int swipeMode) {
        this.swipeMode = swipeMode;
    }

    public void setSwipeOpenOnLongPress(boolean swipeOpenOnLongPress) {
        this.swipeOpenOnLongPress = swipeOpenOnLongPress;
    }

    public void setSwipeActionLeft(int swipeActionLeft) {
        this.swipeActionLeft = swipeActionLeft;
    }

    public void setSwipeActionRight(int swipeActionRight) {
        this.swipeActionRight = swipeActionRight;
    }

    public int getSwipeActionRight() {
        return swipeActionRight;
    }

    public int getSwipeActionLeft() {
        return swipeActionLeft;
    }

    public void resetItems(){
        if (swipeListView.getAdapter() != null){
            int count = swipeListView.getAdapter().getCount();
            for (int i = opened.size(); i <= count; i ++){
                opened.add(false);
                openedRight.add(false);
            }
        }
    }

    protected void openAnimation(int position){
        openAnimate(swipeListView.getChildAt(position - swipeListView.getFirstVisiblePosition()).findViewById(swipeFrontView), position);

    }

    protected void closeAnimation(int position){
        closeAnimate(swipeListView.getChildAt(position - swipeListView.getFirstVisiblePosition()).findViewById(swipeFrontView), position);
    }

    private void openAnimate(View view, int position){
        if (!opened.get(position)){
            generateRevealAnimate(view, true, false, position);
        }
    }

    private void closeAnimate(View view, int position){
        if (opened.get(position)){
            closeAnimate(swipeListView.getChildAt(position - swipeListView.getFirstVisiblePosition()).findViewById(swipeFrontView), position);
        }
    }

    private void generateAnimate(final View view, final boolean swap, final boolean swapRight, final int position) {
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL) {
            generateRevealAnimate(view, swap, swapRight, position);
        }
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_DISMISS) {
            generateDismissAnimate(parentView, swap, swapRight, position);
        }
    }

    private void generateDismissAnimate(final View view, final boolean swap, final boolean swapRight, final int position){
        int moveTo = 0;
        if (opened.get(position)){
            if (!swap){
                moveTo = openedRight.get(position) ? (int)(viewWidth - rightOffset) : (int)(-viewWidth + leftOffset);
            }
        } else {
            if (swap){
                moveTo = swapRight ? (int)(viewWidth - rightOffset) : (int)(-viewWidth + leftOffset);
            }
        }

        int alpha = 1;
        if (swap){
            ++dismissAnimationRefCount;
            alpha = 0;
        }

        animate(view).translationX(moveTo)
                .alpha(alpha)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (swap){
                            closeOpendItems();
                            performDismiss(view, position);
                        }
                    }
                });
    }

    private void generateRevealAnimate(final View view, final boolean swap, final boolean swapRight, final int position){
        int moveTo = 0;
        if (opened.get(position)){
            if (!swap){
                moveTo = openedRight.get(position) ? (int)(viewWidth - rightOffset) : (int)(-viewWidth + leftOffset);
            }
        } else {
            if (swap){
                moveTo = swapRight ? (int)(viewWidth - rightOffset) : (int)(-viewWidth + leftOffset);
            }
        }

        animate(view)
                .translationX(moveTo)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListView.resetScrooling();
                        if (swap){
                            boolean aux = !opened.get(position);
                            opened.set(position, aux);
                            if (aux){
                                swipeListView.onOpened(position, swapRight);
                                openedRight.set(position, swapRight);
                            } else {
                                swipeListView.onClosed(position, openedRight.get(position));
                            }
                        }
                    }
                });
    }

    public void setEnabled(boolean enabled){
        paused = !enabled;
    }

    public AbsListView.OnScrollListener makeScrollListener(){
        return new AbsListView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        };
    }

    void closeOpendItems(){
        if (opened != null){
            int start = swipeListView.getFirstVisiblePosition();
            int end = swipeListView.getLastVisiblePosition();
            for (int i = start; i <= end; i ++){
                if (opened.get(i)){
                    closeAnimate(swipeListView.getChildAt(i - start).findViewById(swipeFrontView), i);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (viewWidth < 2){
            viewWidth = swipeListView.getWidth();
        }

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (paused)
                    return false;
                swipeCurrentAction = SwipeListView.SWIPE_ACTION_NONE;

                int childCount = swipeListView.getChildCount();
                int[] listViewCoords = new int[2];
                swipeListView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawX() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++){
                    child = swipeListView.getChildAt(i);
                    child.getHitRect(rect);

                    if (rect.contains(x, y) && swipeListView.getAdapter().isEnabled(swipeListView.getFirstVisiblePosition() + i)){
                        setParentView(child);
                        setFrontView(child.findViewById(swipeFrontView));
                        downX = motionEvent.getRawX();
                        downPosition = swipeListView.getPositionForView(child);

                        frontView.setClickable(!opened.get(downPosition));
                        frontView.setLongClickable(!opened.get(downPosition));

                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(motionEvent);
                        if (swipeBackView > 0){
                            setBackView(child.findViewById(swipeBackView));
                        }
                        break;
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            case MotionEvent.ACTION_UP: {
                if (velocityTracker == null || !swiping) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - downX;
                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                if (!opened.get(downPosition)) {
                    if (swipeMode == SwipeListView.SWIPE_MODE_LEFT && velocityTracker.getXVelocity() > 0) {
                        velocityX = 0;
                    }
                    if (swipeMode == SwipeListView.SWIPE_MODE_RIGHT && velocityTracker.getXVelocity() < 0) {
                        velocityX = 0;
                    }
                }
                float velocityY = Math.abs(velocityTracker.getYVelocity());
                boolean swap = false;
                boolean swapRight = false;
                if (minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity && velocityY < velocityX) {
                    swapRight = velocityTracker.getXVelocity() > 0;
                    if (opened.get(downPosition) && openedRight.get(downPosition) && swapRight) {
                        swap = false;
                    } else if (opened.get(downPosition) && !openedRight.get(downPosition) && !swapRight) {
                        swap = false;
                    } else {
                        swap = true;
                    }
                } else if (Math.abs(deltaX) > viewWidth / 2) {
                    swap = true;
                    swapRight = deltaX > 0;
                }
                generateAnimate(frontView, swap, swapRight, downPosition);

                velocityTracker.recycle();
                velocityTracker = null;
                downX = 0;
                // change clickable front view
                if (swap) {
                    frontView.setClickable(opened.get(downPosition));
                    frontView.setLongClickable(opened.get(downPosition));
                }
                frontView = null;
                backView = null;
                this.downPosition = ListView.INVALID_POSITION;
                swiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (velocityTracker == null || paused) {
                    break;
                }

                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                float velocityY = Math.abs(velocityTracker.getYVelocity());

                float deltaX = motionEvent.getRawX() - downX;
                float deltaMode = Math.abs(deltaX);

                int swipeMode = this.swipeMode;
                int changeSwipeMode = swipeListView.changeSwipeMode(downPosition);
                if (changeSwipeMode >= 0) {
                    swipeMode = changeSwipeMode;
                }

                if (swipeMode == SwipeListView.SWIPE_MODE_NONE) {
                    deltaMode = 0;
                } else if (swipeMode != SwipeListView.SWIPE_MODE_BOTH) {
                    if (opened.get(downPosition)) {
                        if (swipeMode == SwipeListView.SWIPE_MODE_LEFT && deltaX < 0) {
                            deltaMode = 0;
                        } else if (swipeMode == SwipeListView.SWIPE_MODE_RIGHT && deltaX > 0) {
                            deltaMode = 0;
                        }
                    } else {
                        if (swipeMode == SwipeListView.SWIPE_MODE_LEFT && deltaX > 0) {
                            deltaMode = 0;
                        } else if (swipeMode == SwipeListView.SWIPE_MODE_RIGHT && deltaX < 0) {
                            deltaMode = 0;
                        }
                    }
                }
                if (deltaMode > slop && swipeCurrentAction == SwipeListView.SWIPE_ACTION_NONE && velocityY < velocityX) {
                    swiping = true;
                    boolean swipingRight = (deltaX > 0);
                    if (opened.get(downPosition)) {
                        swipeListView.onStartClose(downPosition, swipingRight);
                        swipeCurrentAction = SwipeListView.SWIPE_ACTION_REVEAL;
                    } else {
                        if (swipingRight && swipeActionRight == SwipeListView.SWIPE_ACTION_DISMISS) {
                            swipeCurrentAction = SwipeListView.SWIPE_ACTION_DISMISS;
                        } else if (!swipingRight && swipeActionLeft == SwipeListView.SWIPE_ACTION_DISMISS) {
                            swipeCurrentAction = SwipeListView.SWIPE_ACTION_DISMISS;
                        } else if (swipingRight && swipeActionRight == SwipeListView.SWIPE_ACTION_CHECK) {
                            swipeCurrentAction = SwipeListView.SWIPE_ACTION_CHECK;
                        } else if (!swipingRight && swipeActionLeft == SwipeListView.SWIPE_ACTION_CHECK) {
                            swipeCurrentAction = SwipeListView.SWIPE_ACTION_CHECK;
                        } else {
                            swipeCurrentAction = SwipeListView.SWIPE_ACTION_REVEAL;
                        }
                        swipeListView.onStartOpen(downPosition, swipeCurrentAction, swipingRight);
                    }
                    swipeListView.requestDisallowInterceptTouchEvent(true);
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    swipeListView.onTouchEvent(cancelEvent);
                }

                if (swiping) {
                    if (opened.get(downPosition)) {
                        deltaX += openedRight.get(downPosition) ? viewWidth - rightOffset : -viewWidth + leftOffset;
                    }
                    move(deltaX);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public void move(float deltaX){
        swipeListView.onMove(downPosition, deltaX);
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_DISMISS){
            setTranslationX(parentView, deltaX);
            setAlpha(parentView, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / viewWidth)));
        } else {
            setTranslationX(frontView, deltaX);
        }
    }

    class PendingDismissData implements Comparable<PendingDismissData>{

        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            return other.position - position;
        }
    }

    private void performDismiss(final View dismissView, final int dismissPosition){
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --dismissAnimationRefCount;
                if (dismissAnimationRefCount == 0){
                    Collections.sort(pendingDismisses);

                    int[] dismissPosition = new int[pendingDismisses.size()];
                    for (int i = pendingDismisses.size() - 1; i >= 0; i --){
                        dismissPosition[i] = pendingDismisses.get(i).position;
                    }
                    swipeListView.onDismiss(dismissPosition);

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismissData : pendingDismisses){
                        setAlpha(pendingDismissData.view, 1f);
                        setTranslationX(pendingDismissData.view, 0);
                        lp = pendingDismissData.view.getLayoutParams();
                        lp.height = originalHeight;
                        pendingDismissData.view.setLayoutParams(lp);
                    }
                    pendingDismisses.clear();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        pendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }
}
