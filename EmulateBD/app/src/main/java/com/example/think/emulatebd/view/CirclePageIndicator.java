package com.example.think.emulatebd.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.think.emulatebd.R;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

/**
 * Created by HuangMei on 2016/12/5.
 */

public class CirclePageIndicator extends View implements PageIndicator{
    private static final int INVALID_POINTER = -1;

    private float mRadius;
    private final Paint mPaintPageFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mCurrentPage;
    private int mSnapPage;
    private float mPageOffset;
    private int mScrollState;
    private int mOrientation;
    private boolean mCentered;
    private boolean mSnap;

    private int mTouchSlop;
    private float mLastMotionX = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsDragging;

    public CirclePageIndicator(Context context) {
        super(context, null);
    }

    public CirclePageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CirclePageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode())
            return;

        //Load defaults from resources
        final Resources res = getResources();
        final int defaultPageColor = res.getColor(R.color.default_circle_indicator_page_color);
        final int defaultFillColor = res.getColor(R.color.default_circle_indicator_fill_color);
        final int defaultOrientation = res.getInteger(R.integer.default_circle_indicator_orientation);
        final int defaultStrokeColor = res.getColor(R.color.default_circle_indicator_stroke_color);
        final float defaultStrokeWidth = res.getDimension(R.dimen.default_circle_indicator_stroke_width);
        final float defaultRadius = res.getDimension(R.dimen.default_circle_indicator_radius);
        final boolean defaultCentered = res.getBoolean(R.bool.default_circle_indicator_centered);
        final boolean defaultSnap = res.getBoolean(R.bool.default_circle_indicator_snap);

        //Retrieve styles attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, 0);

        mCentered = a.getBoolean(R.styleable.CirclePageIndicator_centered, defaultCentered);
        mOrientation = a.getInt(R.styleable.CirclePageIndicator_android_orientation, defaultOrientation);
        mPaintPageFill.setStyle(Paint.Style.FILL);
        mPaintPageFill.setColor(a.getColor(R.styleable.CirclePageIndicator_pageColor, defaultPageColor));
        mPaintStroke.setStyle(Paint.Style.STROKE);
        mPaintStroke.setColor(a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultStrokeColor));
        mPaintStroke.setStrokeWidth(a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth));
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor));
        mRadius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius);
        mSnap = a.getBoolean(R.styleable.CirclePageIndicator_snap, defaultSnap);

        Drawable background = a.getDrawable(R.styleable.CirclePageIndicator_android_background);
        if (background != null) {
            setBackgroundDrawable(background);
        }

        a.recycle();

        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }

    public void setCentered(boolean centered){
        mCentered = centered;
        invalidate();
    }

    public boolean isCentered(){
        return mCentered;
    }

    public void setPageColor(int pageColor){
        mPaintPageFill.setColor(pageColor);
        invalidate();
    }

    public int getPageColor(){
        return mPaintPageFill.getColor();
    }

    public void setFillColor(int fillColor){
        mPaintFill.setColor(fillColor);
        invalidate();
    }

    public int getFillColor(){
        return mPaintFill.getColor();
    }

    public void setOrientation(int orientation){
        switch (orientation){
            case HORIZONTAL:
            case VERTICAL:
                mOrientation = orientation;
                requestLayout();
                break;
            default:
                throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
        }
    }

    public int getOrientation(){
        return mOrientation;
    }

    public void setStrokeColor(int strokeColor){
        mPaintStroke.setColor(strokeColor);
        invalidate();
    }

    public int getStrokeColor(){
        return mPaintStroke.getColor();
    }

    public void setStrokeWidth(float strokeWidth) {
        mPaintStroke.setStrokeWidth(strokeWidth);
        invalidate();
    }

    public float getStrokeWidth() {
        return mPaintStroke.getStrokeWidth();
    }

    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setSnap(boolean snap) {
        mSnap = snap;
        invalidate();
    }

    public boolean isSnap() {
        return mSnap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null){
            return;
        }

        final int count = mViewPager.getAdapter().getCount();
        if (count == 0){
            return;
        }

        if (mCurrentPage >= count){
            setCurrentItem(count - 1);
            return;
        }

        int longSize;
        int longPaddingBefore;
        int longPaddingAfter;
        int shortPaddingBefore;
        if (mOrientation == HORIZONTAL){
            longSize = getWidth();
            longPaddingBefore = getPaddingLeft();
            longPaddingAfter = getPaddingRight();
            shortPaddingBefore = getPaddingTop();
        } else {
            longSize = getHeight();
            longPaddingBefore = getPaddingTop();
            longPaddingAfter = getPaddingBottom();
            shortPaddingBefore = getPaddingLeft();
        }

        final float threeRadius = mRadius * 3;
        final float shortOffset = shortPaddingBefore + mRadius;
        float longOffset = longPaddingBefore + mRadius;
        if (mCentered){
            longOffset += ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f) - ((count * threeRadius) / 2.0f);
        }
        float dX;
        float dY;

        float pageFillRadius = mRadius;
        if (mPaintStroke.getStrokeWidth() > 0) {
            pageFillRadius -= mPaintStroke.getStrokeWidth() / 2.0f;
        }

        //Draw stroked circles
        for (int iLoop = 0; iLoop < count; iLoop++) {
            float drawLong = longOffset + (iLoop * threeRadius);
            if (mOrientation == HORIZONTAL) {
                dX = drawLong;
                dY = shortOffset;
            } else {
                dX = shortOffset;
                dY = drawLong;
            }
            // Only paint fill if not completely transparent
            if (mPaintPageFill.getAlpha() > 0) {
                canvas.drawCircle(dX, dY, pageFillRadius, mPaintPageFill);
            }

            // Only paint stroke if a stroke width was non-zero
            if (pageFillRadius != mRadius) {
                canvas.drawCircle(dX, dY, mRadius, mPaintStroke);
            }
        }

        //Draw the filled circle according to the current scroll
        float cx = (mSnap ? mSnapPage : mCurrentPage) * threeRadius;
        if (!mSnap) {
            cx += mPageOffset * threeRadius;
        }
        if (mOrientation == HORIZONTAL) {
            dX = longOffset + cx;
            dY = shortOffset;
        } else {
            dX = shortOffset;
            dY = longOffset + cx;
        }
        canvas.drawCircle(dX, dY, mRadius, mPaintFill);
    }

    public boolean onTouchEvent(MotionEvent event){
        if (super.onTouchEvent(event)){
            return true;
        }
        if ((mViewPager == null) || (mViewPager.getAdapter().getCount()==0)){
            return false;
        }

        final int action = event.getAction() & MotionEventCompat.ACTION_MASK;

        switch (action){
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                mLastMotionX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                final int actionPointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);
                final float x = MotionEventCompat.getX(event, actionPointerIndex);
                final float deltaX = x - mLastMotionX;

                if (!mIsDragging){
                    if (Math.abs(deltaX) > mTouchSlop){
                        mIsDragging = true;
                    }
                }

                if (mIsDragging){
                    mLastMotionX = x;
                    if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()){
                        mViewPager.fakeDragBy(deltaX);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!mIsDragging){
                    final int count = mViewPager.getAdapter().getCount();
                    final int width = getWidth();
                    final float halfWidth = width / 2f;
                    final float sixthWidth = width / 6f;

                    if ((mCurrentPage > 0) && (event.getX() < halfWidth - sixthWidth)){
                        if (action != MotionEvent.ACTION_CANCEL){
                            mViewPager.setCurrentItem(mCurrentPage - 1);
                        }
                        return true;
                    } else if((mCurrentPage < count - 1) && (event.getX() > halfWidth + sixthWidth)){
                        if (action != MotionEvent.ACTION_CANCEL){
                            mViewPager.setCurrentItem(mCurrentPage - 1);
                        }
                        return true;
                    }
                }

                mIsDragging = false;
                mActivePointerId = INVALID_POINTER;
                if (mViewPager.isFakeDragging())
                    mViewPager.endFakeDrag();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(event);
                mLastMotionX = MotionEventCompat.getX(event, index);
                mActivePointerId = MotionEventCompat.getPointerId(event, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final int pointId = MotionEventCompat.getPointerId(event, pointerIndex);
                if (pointId == mActivePointerId){
                    final  int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                }
                mLastMotionX = MotionEventCompat.getX(event, MotionEventCompat.findPointerIndex(event, mActivePointerId));
                break;
        }
        return true;
    }

    @Override
    public void setViewPager(ViewPager viewPager) {
        if (mViewPager == viewPager){
            return;
        }
        if (mViewPager != null){
            mViewPager.setOnPageChangeListener(null);
        }
        if (viewPager.getAdapter() == null){
            throw new IllegalArgumentException("ViewPager does not have adapter instance.");
        }
        mViewPager = viewPager;
        mViewPager.setOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void setViewPager(ViewPager viewPager, int initialPosition) {
        setViewPager(viewPager);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null){
            throw  new IllegalArgumentException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mCurrentPage = position;
        mPageOffset = positionOffset;
        invalidate();

        if (mListener != null){
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE){
            mCurrentPage = position;
            mSnapPage = position;
            invalidate();
        }

        if (mListener != null){
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
        if (mListener != null){
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        mSnapPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == HORIZONTAL){
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
        }
    }

    private int measureLong(int measureSpec){
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)){
            result = specSize;
        } else {
            final int count = mViewPager.getAdapter().getCount();
            result = (int) (getPaddingLeft() + getPaddingRight()
                    + (count * 2 * mRadius) + (count - 1) * mRadius + 1);
            if (specMode == MeasureSpec.AT_MOST){
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureShort(int measureSpec){
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY){
            result = specSize;
        } else {
            result = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
            if (specMode == MeasureSpec.AT_MOST){
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    static class SavedState extends BaseSavedState{
        int currentPage;

        public SavedState(Parcel source) {
            super(source);
            currentPage = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentPage);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>(){
            @Override
            public SavedState createFromParcel(Parcel in) {
               return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
