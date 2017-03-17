package com.example.think.emulatebd.switchbtn;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewParentCompat;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.CompoundButton;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/8.
 */

public class SwitchButton extends CompoundButton{

    private Paint mPaint;
    private ViewParent mParent;
    private Bitmap mBottom;
    private Bitmap mCurBtnPic;
    private Bitmap mBtnPressed;
    private Bitmap mBtnNormal;
    private Bitmap mFrame;
    private Bitmap mMask;
    private RectF mSaveLayerRectF;
    private PorterDuffXfermode mXfermode;
    private float mFirstDownY;
    private float mFirstDownX;
    private float mRealPos;
    private float mBtnPos;
    private float mBtnOnPos;
    private float mBtnOffPos;
    private float mMaskWidth;
    private float mMaskHeight;
    private float mBtnWidth;
    private float mBtnInitPos;
    private int mClickTimeout;
    private int mTouchSlop;
    private final int MAX_ALPHA = 255;
    private int mAlpha = MAX_ALPHA;
    private boolean mChecked = false;
    private boolean mBroadcasting;
    private boolean mTurningOn;
    private boolean mAnimating;
    private final float VELOCITY = 350;
    private float mVelocity;
    private final float EXTENDED_OFFSET_Y = 15;
    private float mExtendOffsetY;
    private float mAnimationPosition;
    private float mAnimatedVelocity;

    private PerformClick mPerformClick;
    private OnCheckedChangeListener mOnCheckedChangListener;
    private OnCheckedChangeListener mOncheckedChangeWidgetListener;

//    private

    public SwitchButton(Context context) {
        super(context);
    }

    private void initView(Context context){
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        Resources resources = context.getResources();

        mClickTimeout = ViewConfiguration.getPressedStateDuration()
                + ViewConfiguration.getTapTimeout();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mBottom = BitmapFactory.decodeResource(resources, R.drawable.bottom);
        mBtnPressed = BitmapFactory.decodeResource(resources, R.drawable.btn_pressed);
        mBtnNormal = BitmapFactory.decodeResource(resources, R.drawable.btn_unpressed);
        mFrame = BitmapFactory.decodeResource(resources, R.drawable.frame);
        mMask = BitmapFactory.decodeResource(resources, R.drawable.mask);

        mCurBtnPic = mBtnNormal;

        mBtnWidth = mBtnPressed.getWidth();
        mMaskWidth = mMask.getWidth();
        mMaskHeight = mMask.getHeight();

        mBtnOnPos = mBtnWidth / 2;
        mBtnOffPos = mMaskWidth - mBtnWidth / 2;

        mBtnPos = mChecked ? mBtnOnPos : mBtnOffPos;
        mRealPos = getRealPos(mBtnPos);

        final float density = getResources().getDisplayMetrics().density;
        mVelocity = (int) (VELOCITY * density + 0.5f);
        mExtendOffsetY = (int) (EXTENDED_OFFSET_Y * density + 0.5f);

        mSaveLayerRectF = new RectF(0, mExtendOffsetY, mMask.getWidth(), mMask.getHeight()
                + mExtendOffsetY);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mAlpha = enabled ? MAX_ALPHA : MAX_ALPHA / 2;
        super.setEnabled(enabled);
    }

    public boolean isChecked(){
        return mChecked;
    }

    public void toggle(){
        setChecked(!mChecked);
    }

    private void setCheckedDelayed(final  boolean checked){
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                setChecked(checked);
            }
        }, 10);
    }

    public void setChecked(boolean checked){
        if (mChecked != checked){
            mChecked = checked;

            mBtnPos = checked ? mBtnOnPos : mBtnOffPos;
            mRealPos = getRealPos(mBtnPos);
            invalidate();

            if (mBroadcasting){
                return;
            }

            mBroadcasting = true;
            if (mOncheckedChangeWidgetListener != null){
                mOnCheckedChangListener.onCheckedChanged(SwitchButton.this, mChecked);
            }
            if (mOncheckedChangeWidgetListener != null){
                mOncheckedChangeWidgetListener.onCheckedChanged(SwitchButton.this, mChecked);
            }
            mBroadcasting = false;
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener){
        mOnCheckedChangListener = onCheckedChangeListener;
    }

    public void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener){
        mOncheckedChangeWidgetListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float deltaX = Math.abs(x - mFirstDownX);
        float deltaY = Math.abs(y - mFirstDownY);
        switch (action){
            case MotionEvent.ACTION_DOWN:
                attempClaimDrag();
                mFirstDownX = x;
                mFirstDownY = y;
                mCurBtnPic = mBtnPressed;
                mBtnInitPos = mChecked ? mBtnOnPos : mBtnOffPos;
                break;
            case MotionEvent.ACTION_MOVE:
                //float time = event.getEventTime() - event.getDownTime();
                mBtnPos = mBtnInitPos + event.getX() - mFirstDownX;

                if (mBtnPos <= mBtnOffPos){
                    mBtnPos = mBtnOffPos;
                }
                if (mBtnPos >= mBtnOnPos){
                    mBtnPos = mBtnOnPos;
                }

                mTurningOn = mBtnPos > (mBtnOnPos - mBtnOffPos) / 2 + mBtnOffPos;
                mRealPos = getRealPos(mBtnPos);
                break;
            case MotionEvent.ACTION_UP:
                mCurBtnPic = mBtnNormal;
                float time = event.getEventTime() - event.getDownTime();
                if (deltaY < mTouchSlop && deltaX < mTouchSlop && time < mClickTimeout){
                    if (mPerformClick == null){
                        mPerformClick = new PerformClick();
                    }
                    if (!post(mPerformClick)){
                        performClick();
                    }
                } else {
                    startAnimation(!mTurningOn);
                }
                break;
        }
        invalidate();
        return isEnabled();
    }

    private final class PerformClick implements Runnable{
        @Override
        public void run() {
            performClick();
        }
    }

    @Override
    public boolean performClick() {
        startAnimation(mChecked);
        return true;
    }

    private void attempClaimDrag(){
        mParent = getParent();
        if (mParent != null){
            mParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private float getRealPos(float btnPos){
        return btnPos - mBtnWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayerAlpha(mSaveLayerRectF, mAlpha, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        canvas.drawBitmap(mMask, 0, mExtendOffsetY, mPaint);
        mPaint.setXfermode(mXfermode);

        canvas.drawBitmap(mBottom, mRealPos, mExtendOffsetY, mPaint);
        mPaint.setXfermode(null);

        canvas.drawBitmap(mFrame, 0, mExtendOffsetY, mPaint);

        canvas.drawBitmap(mCurBtnPic, mRealPos, mExtendOffsetY, mPaint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mMaskWidth, (int)(mMaskHeight + 2 * mExtendOffsetY));
    }

    private void startAnimation(boolean trunOn){
        mAnimating = true;
        mAnimatedVelocity = trunOn ? -mVelocity : mVelocity;
        mAnimationPosition = mBtnOnPos;
        new SwitchAnimation().run();
    }

    private void stopAnimation(){
        mAnimating = false;
    }

    private final class SwitchAnimation implements Runnable{
        @Override
        public void run() {
            if (!mAnimating)
                return;
            doAnimation();
            FrameAnimationController.requestAnimationFrame(this);
        }
    }

    private void doAnimation(){
        mAnimationPosition += mAnimatedVelocity * FrameAnimationController.ANIMATION_FREME_DURUTION
                 / 1000;
        if (mAnimationPosition >= mBtnPos){
            stopAnimation();
            mAnimationPosition = mBtnOnPos;
            setCheckedDelayed(true);
        } else if(mAnimationPosition <= mBtnOffPos){
            stopAnimation();
            mAnimationPosition = mBtnOffPos;
            setCheckedDelayed(false);
        }
        moveView(mAnimationPosition);
    }

    private void moveView(float position){
        mBtnPos = position;
        mRealPos = getRealPos(mBtnPos);
        invalidate();
    }
}
