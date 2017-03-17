package com.example.think.emulatebd.slidinglayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.example.think.emulatebd.R;

import java.lang.reflect.Method;

/**
 * Created by HuangMei on 2016/12/16.
 */

public class SlidingLayer extends FrameLayout {

    public static final int STICK_TO_AUTO = 0;
    public static final int STICK_TO_RIGHT = -1;
    public static final int STICK_TO_LEFT = -2;
    public static final int STICK_TO_MIDDLE = -3;

    private static final int MAX_SCROLLING_DURATION = 600;
    private static final int MIN_DISTANCE_FOR_FLING = 25;

    private static final Interpolator sMenuInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return (float) Math.pow(t, 5) + 1.0f;
        }
    };

    private Scroller mScroller;

    private int mShadowWidth;
    private Drawable mShadowDrawable;

    private boolean mDrawingCacheEnabled;

    private int mScreenSide = STICK_TO_AUTO;
    private boolean closeOnTapEnabled = true;

    private boolean mEnabled = true;
    private boolean mSlidingFromShadowEnabled = true;
    private boolean mIsDragging;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;

    private float mLastX = -1;
    private float mLastY = -1;
    private float mInitaialX = -1;

    protected int mActivePointerId = INVALID_POINTER;

    private static final int INVALID_POINTER = -1;

    private boolean mIsOpen;
    private boolean mScrolling;

    private OnInteractListener mOnInteractListener;

    protected VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    protected int mMaximumVelocity;
    private int mFlingDistance;

    private boolean mLastTouchAllowed = false;

    public interface OnInteractListener {
        public void onOpen();

        public void onClose();

        public void onOpened();

        public void onClosed();
    }

    private void init() {
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setFocusable(true);
        final Context context = getContext();
        mScroller = new Scroller(context, sMenuInterpolator);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat
                .getScaledPagingTouchSlop(configuration);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        final float density = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
    }

    public boolean isOpened(){
        return mIsOpen;
    }

    public void openLayer(boolean smoothAnim){
        openLayer(smoothAnim);
    }

    private void openLayer(boolean smoothAnim, boolean forceOpen) {
        switchLayer(true, smoothAnim, forceOpen, 0);
    }

    public void closeLayer(boolean smoothAnim) {
        closeLayer(smoothAnim, false);
    }

    private void closeLayer(boolean smoothAnim, boolean forceClose) {
        switchLayer(false, smoothAnim, forceClose, 0);
    }

    private void switchLayer(boolean open, boolean smoothAnim,
                             boolean forceSwitch) {
        switchLayer(open, smoothAnim, forceSwitch, 0);
    }

    private void switchLayer(boolean open, boolean smoothAnim,
                             boolean forceSwitch, int velocity){
        if (!forceSwitch && open == mIsOpen){
            setDrawingCacheEnabled(false);
            return;
        }

        if(open){
            if (mOnInteractListener != null){
                mOnInteractListener.onOpen();
            }
        } else {
            if (mOnInteractListener != null){
                mOnInteractListener.onClose();
            }
        }
        mIsOpen = open;
        final int destX = getDestScrollX(velocity);

        if (smoothAnim){
            smoothScrollTo(destX, 0, velocity);
        } else {
            completeScroll();
            scrollTo(destX, 0);
        }
    }

    public void setOnInteractListener(OnInteractListener listener) {
        mOnInteractListener = listener;
    }
    public void setShadowWidth(int shadowWidth) {
        mShadowWidth = shadowWidth;
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    public int getShadowWidth() {
        return mShadowWidth;
    }

    public void setShadowDrawable(Drawable d) {
        mShadowDrawable = d;
        refreshDrawableState();
        setWillNotDraw(false);
        invalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    public void setShadowWidthRes(int resId) {
        setShadowWidth((int) getResources().getDimension(resId));
    }

    public void setShadowDrawable(int resId) {
        setShadowDrawable(getContext().getResources().getDrawable(resId));
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mShadowDrawable;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final Drawable d = mShadowDrawable;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
    }

    public SlidingLayer(Context context) {
        this(context, null);
    }

    public SlidingLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingLayer);

        setStickTo(ta.getInt(R.styleable.SlidingLayer_stickTo, STICK_TO_RIGHT));
        int shadowRes = ta.getResourceId(
                R.styleable.SlidingLayer_shadow_drawable, -1);
        if (shadowRes != -1){
            setShadowDrawable(shadowRes);
        }

        setShadowWidth((int)ta.getDimension(
                R.styleable.SlidingLayer_shadow_width, 0
        ));

        closeOnTapEnabled = ta.getBoolean(
                R.styleable.SlidingLayer_close_on_tapEnabled, false
        );
        ta.recycle();
        init();
    }

    public boolean isSlidingEnabled() {
        return mEnabled;
    }

    public void setSlidingEnabled(boolean _enabled) {
        mEnabled = _enabled;
    }

    public boolean isSlidingFromShadowEnabled() {
        return mSlidingFromShadowEnabled;
    }

    public void setSlidingFromShadowEnabled(boolean _slidingShadow) {
        mSlidingFromShadowEnabled = _slidingShadow;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!mEnabled){
            return false;
        }

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (action == MotionEvent.ACTION_CANCEL
            || action == MotionEvent.ACTION_UP){
            mIsDragging = false;
            mIsUnableToDrag = false;
            mActivePointerId = INVALID_POINTER;
            if (mVelocityTracker != null){
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN){
            if (mIsDragging){
                return true;
            } else if (mIsUnableToDrag){
                return false;
            }
        }

        switch (action){
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER){
                    break;
                }

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev,
                        activePointerId);
                if (pointerIndex == -1){
                    mActivePointerId = INVALID_POINTER;
                    break;
                }

                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float dx = x - mLastX;
                final float xDiff = Math.abs(dx);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = Math.abs(y - mLastY);
                if (xDiff > mTouchSlop && xDiff > yDiff && allowDraging(dx)){
                    mIsDragging = true;
                    mLastX = x;
                    setDrawingCacheEnabled(true);
                } else if (yDiff > mTouchSlop){
                    mIsUnableToDrag = true;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getAction()
                        & (Build.VERSION.SDK_INT >= 8 ? MotionEvent.ACTION_POINTER_INDEX_MASK
                    : MotionEvent.ACTION_POINTER_INDEX_MASK);
                mLastX = mInitaialX = MotionEventCompat.getX(ev, mActivePointerId);
                mLastY = MotionEventCompat.getY(ev, mActivePointerId);
                if (allowSlidingFromHere(ev)){
                    mIsDragging = false;
                    mIsUnableToDrag = false;
                    return super.onInterceptTouchEvent(ev);
                } else {
                    mIsUnableToDrag = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                onSecondarypointerUp(ev);
                break;
        }

        if (!mIsDragging){
            if (mVelocityTracker == null){
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
        }

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnabled || !mIsDragging && !mLastTouchAllowed
                && !allowSlidingFromHere(event)){
            return false;
        }

        final int action = event.getAction();

        if (action ==MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_OUTSIDE){
            mLastTouchAllowed = false;
        } else {
            mLastTouchAllowed = true;
        }

        if (mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch(action & MotionEventCompat.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                completeScroll();
                mLastX = mInitaialX = event.getX();
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                break;
            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = MotionEventCompat.findPointerIndex(event,
                        mActivePointerId);
                if (pointerIndex == -1){
                    mActivePointerId = INVALID_POINTER;
                    break;
                }
                final float x = MotionEventCompat.getX(event, pointerIndex);
                final float xDiff = Math.abs(x - mLastX);
                final float y = MotionEventCompat.getY(event, pointerIndex);
                final float yDiff = Math.abs(y - mLastY);
                if (xDiff > mTouchSlop && xDiff > yDiff){
                    mIsDragging = true;
                    mLastX = x;
                    setDrawingCacheEnabled(true);
                }
                if (mIsDragging){
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(event, mActivePointerId);
                    if (activePointerIndex == -1){
                        mActivePointerId = INVALID_POINTER;
                        break;
                    }

                    final float x1 = MotionEventCompat.getX(event, activePointerIndex);
                    final float deltaX = mLastX - x1;
                    mLastX = x1;
                    float oldScrollX = getScrollX();
                    float scrollX = oldScrollX + deltaX;
                    final float leftBound = mScreenSide < STICK_TO_RIGHT ? getWidth()
                            : 0;
                    final float rightBound = mScreenSide == STICK_TO_LEFT ? 0
                            : -getWidth();
                    if (scrollX > leftBound){
                        scrollX = leftBound;
                    } else if (scrollX < rightBound){
                        scrollX = rightBound;
                    }
                    mLastX += scrollX - (int)scrollX;
                    scrollTo((int) scrollX, getScrollY());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging){
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelcity = (int) VelocityTrackerCompat.getXVelocity(
                            velocityTracker, mActivePointerId);
                    final int scrollX = getScrollX();
                    final int activePointerIndex = MotionEventCompat
                            .findPointerIndex(event, mActivePointerId);
                    final float x2 = MotionEventCompat.getX(event, activePointerIndex);
                    final int totalDlta = (int) (x2 - mInitaialX);
                    boolean nextStateOpened = determineNextStateOpened(mIsOpen, scrollX,
                            initialVelcity, totalDlta);
                    switchLayer(nextStateOpened, true, true, initialVelcity);

                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                } else if (mIsOpen && closeOnTapEnabled){
                    closeLayer(true);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging){
                    switchLayer(mIsOpen, true, true);
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(event);
                final float x3 = MotionEventCompat.getX(event, index);
                mLastX = x3;
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondarypointerUp(event);
                mLastX = MotionEventCompat.getX(event,
                        MotionEventCompat.findPointerIndex(event, mActivePointerId));
                break;
        }
        if (mActivePointerId == INVALID_POINTER){
            mLastTouchAllowed = false;
        }
        return true;
    }

    private boolean allowSlidingFromHere(MotionEvent ev) {
        return mIsOpen /* && allowSlidingFromShadow || ev.getX() > mShadowWidth */;
    }

    private boolean allowDraging(float dx) {
        return mIsOpen
                && (mScreenSide == STICK_TO_RIGHT && dx > 0
                || mScreenSide == STICK_TO_LEFT && dx < 0 || mScreenSide == STICK_TO_MIDDLE
                && dx != 0);
    }

    private boolean determineNextStateOpened(boolean currentState,
                                             float swipeOffset,
                                             int velocity,
                                             int deltaX){
        boolean targetState;

        if (Math.abs(deltaX) > mFlingDistance &&
                Math.abs(velocity) > mMaximumVelocity){
            targetState = mScreenSide == STICK_TO_MIDDLE
                    || mScreenSide == STICK_TO_RIGHT && velocity < 0
                    || mScreenSide == STICK_TO_RIGHT && velocity > 0;
        } else {
            int w = getWidth();

            if (mScreenSide == STICK_TO_RIGHT){
                targetState = swipeOffset > -w / 2;
            } else if (mScreenSide == STICK_TO_LEFT){
                targetState = swipeOffset < w / 2;
            } else if (mScreenSide == STICK_TO_MIDDLE){
                targetState = Math.abs(swipeOffset) < w / 2;
            } else {
                targetState = true;
            }
        }

        return targetState;
    }

    void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    void smoothScrollTo(int x, int y, int velocity){
        if (getChildCount() == 0){
            setDrawingCacheEnabled(false);
            return;
        }

        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0){
            completeScroll();
            if (mIsOpen){
                if (mOnInteractListener != null){
                    mOnInteractListener.onOpened();
                }
            } else {
                if (mOnInteractListener != null){
                    mOnInteractListener.onClosed();
                }
            }
            return;
        }

        setDrawingCacheEnabled(true);
        mScrolling = true;

        final int width = getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth
                * distanceInfluenceForSnapDuration(distanceRatio);
        int duration = 0;
        velocity = Math.abs(velocity);
        if (velocity > 0){
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            duration = MAX_SCROLLING_DURATION;
        }
        duration = Math.min(duration, MAX_SCROLLING_DURATION);

        mScroller.startScroll(sx, sy, dx, dy, duration);
        invalidate();
    }

    float distanceInfluenceForSnapDuration(float f){
        f -= 0.5f;
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    private void endDrag(){
        mIsDragging = false;
        mIsUnableToDrag = false;
        mLastTouchAllowed = false;

        if (mVelocityTracker != null){
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void setDrawingCacheEnabled(boolean enabled) {

        if (mDrawingCacheEnabled != enabled){
            super.setDrawingCacheEnabled(enabled);
            mDrawingCacheEnabled = enabled;

            final int l = getChildCount();
            for (int i = 0; i < 1; i ++){
                final View child = getChildAt(i);
                if (child.getVisibility() != GONE){
                    child.setDrawingCacheEnabled(enabled);
                }
            }
        }
    }

    private void onSecondarypointerUp(MotionEvent event){
        final int pointerIndex = MotionEventCompat.getActionIndex(event);
        final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
        if (pointerId == mActivePointerId){
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastX = MotionEventCompat.getX(event, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);

            if (mVelocityTracker != null){
                mVelocityTracker.clear();
            }
        }
    }

    private void completeScroll(){
        boolean needPopulate = mScrolling;
        if (needPopulate){
            setDrawingCacheEnabled(false);
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();

            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y){
                scrollTo(x, y);
            }
            if (mIsOpen){
                if (mOnInteractListener != null){
                    mOnInteractListener.onOpened();
                }
            } else {
                if (mOnInteractListener != null){
                    mOnInteractListener.onClose();
                }
            }
        }
        mScrolling = false;
    }

    public void setStickTo(int screenSide){
        mScreenSide = screenSide;
        closeLayer(false, true);
    }

    public void setCloseOnTapEnabled(boolean _closeOnTapEnabled){
        closeOnTapEnabled = _closeOnTapEnabled;
    }

    @SuppressWarnings("deprecation")
    private int getScreenSideAuto(int newLeft, int newRight){
        int newScreenSide;

        if (mScreenSide == STICK_TO_AUTO){
            int screenWidth;
            Display display = ((WindowManager) getContext().getSystemService(
                    Context.WINDOW_SERVICE)).getDefaultDisplay();

            try {

                Class<?> cls = Display.class;
                Class<?>[] parameterTypes = {Point.class};
                Point parameter = new Point();
                Method method = cls.getMethod("getSize", parameterTypes);
                method.invoke(display, parameter);
                screenWidth = parameter.x;
            } catch (Exception e){
                screenWidth = display.getWidth();
            }

            boolean boundToLeftBorder = newLeft == 0;
            boolean boundToRightBorder = newRight == screenWidth;

            if (boundToLeftBorder == boundToRightBorder
                    && getLayoutParams().width == WindowManager.LayoutParams.MATCH_PARENT){
                newScreenSide = STICK_TO_MIDDLE;
            } else if (boundToLeftBorder){
                newScreenSide =STICK_TO_LEFT;
            } else {
                newScreenSide = STICK_TO_RIGHT;
            }
        } else {
            newScreenSide = mScreenSide;
        }
        return newScreenSide;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);

        super.onMeasure(getChildMeasureSpec(widthMeasureSpec, 0, width),
                getChildMeasureSpec(heightMeasureSpec, 0, height));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw){
            computeScroll();
            scrollTo(getDestScrollX(), getScrollY());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        int screenSide = mScreenSide;

        if (mScreenSide == STICK_TO_AUTO){
            screenSide = getScreenSideAuto(left, right);
        }

        if (screenSide != mScreenSide){
            setStickTo(screenSide);

            if (mScreenSide == STICK_TO_RIGHT) {
                setPadding(getPaddingLeft() + mShadowWidth, getPaddingTop(),
                        getPaddingRight(), getPaddingBottom());
            } else if (mScreenSide == STICK_TO_LEFT){
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight()
                + mScreenSide, getPaddingBottom());
            } else if (mScreenSide == STICK_TO_MIDDLE){
                setPadding(getPaddingLeft() + mShadowWidth, getPaddingTop(),
                        getPaddingRight() + mShadowWidth, getPaddingBottom());
            }
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private int getDestScrollX(){
        return getDestScrollX(0);
    }

    private int getDestScrollX(int velocity){
        if (mIsOpen){
            return 0;
        } else {
            if (mScreenSide == STICK_TO_RIGHT){
                return -getWidth();
            } else if (mScreenSide == STICK_TO_LEFT){
                return getWidth();
            } else {
                if (velocity == 0){
                    return CommonUtils.getNextRandomBoolean() ? -getWidth() : getWidth();
                } else {
                    return velocity > 0 ? -getWidth() : getWidth();
                }
            }
        }
    }

    public int getContentLeft(){
        return getLeft() + getPaddingLeft();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mShadowWidth > 0 && mShadowDrawable != null){
            if (mScreenSide != STICK_TO_LEFT){
                mShadowDrawable.setBounds(0, 0, mShadowWidth, getHeight());
            }
            if (mScreenSide < STICK_TO_RIGHT){
                mShadowDrawable.setBounds(getWidth() - mShadowWidth, 0,
                        getWidth(), getHeight());
            }
            mShadowDrawable.draw(canvas);
        }
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()){
            if (mScroller.computeScrollOffset()){
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();

                if (oldX != x || oldY != y){
                    scrollTo(x, y);
                }

                invalidate(getLeft() + oldX, getTop(), getRight(), getBottom());
                return;
            }
        }
        computeScroll();
    }
}
