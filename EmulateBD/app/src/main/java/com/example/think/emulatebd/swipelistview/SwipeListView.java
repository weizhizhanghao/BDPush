package com.example.think.emulatebd.swipelistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/9.
 */

public class SwipeListView extends ListView{
    public final static int SWIPE_MODE_DEFAULT = -1;
    public final static int SWIPE_MODE_NONE = 0;
    public final static int SWIPE_MODE_BOTH = 1;
    public final static int SWIPE_MODE_RIGHT = 2;
    public final static int SWIPE_MODE_LEFT = 3;

    public final static int SWIPE_ACTION_REVEAL = 0;
    public final static int SWIPE_ACTION_DISMISS = 1;
    public final static int SWIPE_ACTION_CHECK = 2;
    public final static int SWIPE_ACTION_NONE = 3;

    public final static String SWIPE_DEFAULT_FRONT_VIEW = "swipelist_frontview";
    public final static String SWIPE_DEFAULT_BACK_VIEW = "swipelist_backview";

    public final static int TOUCH_STATE_REST = 0;
    public final static int TOUCH_STATE_SCROLLING_X = 1;
    public final static int TOUCH_STATE_SCROLLING_Y = 2;

    private int touchState = TOUCH_STATE_REST;

    private float lastMotionX;
    private float lastMotionY;
    private int touchSlop;

    int swipeFrontView = 0;
    int swipeBackView = 0;

    private BaseSwipeListViewListener swipeListViewListener;
    private SwipeListViewTouchListener swipeListViewTouchListener;

    public SwipeListView(Context context, int swipeFrontView, int swipeBackView) {
        super(context);
        this.swipeFrontView = swipeFrontView;
        this.swipeBackView = swipeBackView;
    }

    public SwipeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(AttributeSet attributeSet){
        int swipeMode = SWIPE_MODE_BOTH;
        boolean swipeOpenOnLongPress = true;
        boolean swipeCloseAllItemsWhenMoveList = true;
        long swipeAnimationTime = 0;
        float swipeOffsetLeft = 0;
        float swipeOffsetRight = 0;

        int swipeActionLeft = SWIPE_ACTION_REVEAL;
        int swipeActionRight = SWIPE_ACTION_REVEAL;

        if (attributeSet != null){
            TypedArray styled = getContext().obtainStyledAttributes(attributeSet, R.styleable.SwipeListView);
            swipeMode = styled.getInt(R.styleable.SwipeListView_swipeMode, SWIPE_MODE_BOTH);
            swipeActionLeft = styled.getInt(R.styleable.SwipeListView_swipeActionLeft, SWIPE_ACTION_REVEAL);
            swipeActionRight = styled.getInt(R.styleable.SwipeListView_swipeActionRight, SWIPE_ACTION_REVEAL);
            swipeOffsetLeft = styled.getDimension(R.styleable.SwipeListView_swipeOffsetLeft, 0);
            swipeOffsetRight = styled.getDimension(R.styleable.SwipeListView_swipeOffsetRight, 0);
            swipeOpenOnLongPress = styled.getBoolean(R.styleable.SwipeListView_swipeOpenOnLongPress, true);
            swipeAnimationTime = styled.getInteger(R.styleable.SwipeListView_swipeAnimationTime, 0);
            swipeCloseAllItemsWhenMoveList = styled.getBoolean(R.styleable.SwipeListView_swipeCloseAllItemsWhenMoveList, true);
            swipeFrontView = styled.getResourceId(R.styleable.SwipeListView_swipeFrontView, 0);
            swipeBackView = styled.getResourceId(R.styleable.SwipeListView_swipeBackView, 0);
        }

        if(swipeFrontView == 0 || swipeBackView == 0){
            swipeFrontView = getContext().getResources().getIdentifier(SWIPE_DEFAULT_FRONT_VIEW, "id", getContext().getPackageName());
            swipeBackView = getContext().getResources().getIdentifier(SWIPE_DEFAULT_BACK_VIEW, "id", getContext().getPackageName());

            if (swipeFrontView == 0 || swipeBackView == 0){
                throw new RuntimeException(String.format("You forget the attributes swipeFrontView or swipeBackView. You can add this attributes or use '%s' and '%s' identifiers", SWIPE_DEFAULT_FRONT_VIEW, SWIPE_DEFAULT_BACK_VIEW));
            }
        }

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        swipeListViewTouchListener = new SwipeListViewTouchListener(this, swipeFrontView, swipeBackView);
        if (swipeAnimationTime > 0){
            swipeListViewTouchListener.setAnimatinTime(swipeAnimationTime);
        }

        swipeListViewTouchListener.setRightOffset(swipeOffsetRight);
        swipeListViewTouchListener.setLeftOffset(swipeOffsetLeft);
        swipeListViewTouchListener.setSwipeActionLeft(swipeActionLeft);
        swipeListViewTouchListener.setSwipeActionRight(swipeActionRight);
        swipeListViewTouchListener.setSwipeMode(swipeMode);
        swipeListViewTouchListener.setSwipeClosesAllItemsWhenListMoves(swipeCloseAllItemsWhenMoveList);
        swipeListViewTouchListener.setSwipeOpenOnLongPress(swipeOpenOnLongPress);
        setOnTouchListener(swipeListViewTouchListener);
        setOnScrollListener(swipeListViewTouchListener.makeScrollListener());
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        swipeListViewTouchListener.resetItems();
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onListChanged();
                swipeListViewTouchListener.resetItems();
            }
        });
    }

    public void openAnimate(int position){
        swipeListViewTouchListener.openAnimation(position);
    }

    public void closeAnimate(int position){
        swipeListViewTouchListener.closeAnimation(position);
    }

    protected void onDismiss(int[] reverseSortedPositions){
        if (swipeListViewListener != null){
            swipeListViewListener.onDismiss(reverseSortedPositions);
        }
    }

    protected void onStartOpen(int position, int action, boolean right){
        if (swipeListViewListener != null){
            swipeListViewListener.onStartOpen(position, action, right);
        }
    }

    protected void onStartClose(int position, boolean right){
        if (swipeListViewListener != null){
            swipeListViewListener.onStartClose(position, right);
        }
    }

    protected void onClickFrontView(int position){
        if (swipeListViewListener != null){
            swipeListViewListener.onClickFrontView(position);
        }
    }

    protected void onClickBackView(int position){
        if (swipeListViewListener != null){
            swipeListViewListener.onClickBackView(position);
        }
    }

    protected void onOpened(int position, boolean toRight){
        if (swipeListViewListener != null){
            swipeListViewListener.onOpened(position, toRight);
        }
    }

    protected void onClosed(int position, boolean fromRight){
        if (swipeListViewListener != null){
            swipeListViewListener.onClosed(position, fromRight);
        }
    }

    protected void onListChanged(){
        if (swipeListViewListener != null){
            swipeListViewListener.onListChanged();
        }
    }

    protected void onMove(int position, float x){
        if (swipeListViewListener != null){
            swipeListViewListener.onMove(position, x);
        }
    }

    protected int changeSwipeMode(int position){
        if (swipeListViewListener != null){
            return swipeListViewListener.onChangeSwipeMode(position);
        }
        return SWIPE_MODE_DEFAULT;
    }

    public void setSwipeListViewListener(BaseSwipeListViewListener baseSwipeListViewListener){
        this.swipeListViewListener = baseSwipeListViewListener;
    }

    public void resetScrooling(){
        touchState = TOUCH_STATE_REST;
    }

    public void setOffsetRight(float offsetRight) {
        swipeListViewTouchListener.setRightOffset(offsetRight);
    }

    public void setOffsetLeft(float offsetLeft) {
        swipeListViewTouchListener.setLeftOffset(offsetLeft);
    }


    public void setSwipeCloseAllItemsWhenMoveList(boolean swipeCloseAllItemsWhenMoveList){
        swipeListViewTouchListener.setSwipeClosesAllItemsWhenListMoves(swipeCloseAllItemsWhenMoveList);
    }

    public void setSwipeOpenOnLongPress(boolean swipeOpenOnLongPress){
        swipeListViewTouchListener.setSwipeOpenOnLongPress(swipeOpenOnLongPress);
    }

    public void setSwipeMode(int swipeMode){
        swipeListViewTouchListener.setSwipeMode(swipeMode);
    }

    public int getSwipeActionLeft(){
        return swipeListViewTouchListener.getSwipeActionLeft();
    }

    public void setSwipeActionLeft(int swipeActionLeft){
        swipeListViewTouchListener.setSwipeActionLeft(swipeActionLeft);
    }

    public int getSwipeActionRight(){
        return swipeListViewTouchListener.getSwipeActionRight();
    }

    public void  setSwipeActionRight(int swipeActionRight){
        swipeListViewTouchListener.setSwipeActionRight(swipeActionRight);
    }

    public void setAnimationTime(long animationTime){
        swipeListViewTouchListener.setAnimatinTime(animationTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        final float x = ev.getX();
        final float y = ev.getY();

        if (touchState == TOUCH_STATE_SCROLLING_X)
            return swipeListViewTouchListener.onTouch(this, ev);

        switch (action){
            case MotionEvent.ACTION_MOVE:
                checkInMoving(x, y);
                return touchState == TOUCH_STATE_SCROLLING_Y;
            case MotionEvent.ACTION_DOWN:
                swipeListViewTouchListener.onTouch(this,ev);
                touchState = TOUCH_STATE_REST;
                lastMotionX = x;
                lastMotionY = y;
                return false;
            case MotionEvent.ACTION_CANCEL:
                touchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_UP:
                swipeListViewTouchListener.onTouch(this, ev);
                return touchState == TOUCH_STATE_SCROLLING_Y;
            default:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private void checkInMoving(float x, float y){
        final int xDiff = (int) Math.abs(x - lastMotionX);
        final int yDiff = (int) Math.abs(y - lastMotionY);

        final int touchSlop = this.touchSlop;
        boolean xMoved = xDiff > touchSlop;
        boolean yMoved = yDiff > touchSlop;

        if (xMoved){
            touchState = TOUCH_STATE_SCROLLING_X;
            lastMotionX = x;
            lastMotionY = y;
        }

        if (yMoved){
            touchState = TOUCH_STATE_SCROLLING_Y;
            lastMotionX = x;
            lastMotionY = y;
        }
    }

    public void closeOpenedItems(){
        swipeListViewTouchListener.closeOpendItems();
    }
}
