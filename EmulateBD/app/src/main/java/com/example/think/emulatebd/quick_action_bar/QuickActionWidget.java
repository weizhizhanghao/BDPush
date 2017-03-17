package com.example.think.emulatebd.quick_action_bar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.example.think.emulatebd.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuangMei on 2016/12/20.
 */

public abstract class QuickActionWidget extends PopupWindow{

    private static final int MEASURE_AND_LAYOUT_DONE = 1 << 1;

    private final int[] mLocation = new int[2];
    private final Rect mRect = new Rect();

    private int mPrivateFlags;
    private Context mContext;

    private boolean mDismissOnClick;
    private int mArrowOffsetY;

    private int mPopupY;
    private boolean mIsOnTop;

    private int mScreenHeight;
    private int mScreenWidth;
    private boolean mIsDirty;

    private OnQuickActionClickListener mOnQuickActionClickListener;
    private ArrayList<QuickAction> mQuickActions = new ArrayList<>();

    public static interface OnQuickActionClickListener{
        void onQuickActionClicked(QuickActionWidget widget, int position);
    }

    public QuickActionWidget(Context context) {
        super(context);

        mContext = context;


    }

    public void setContentView(int layoutId){
        setContentView(LayoutInflater.from(mContext).inflate(layoutId, null));;
    }

    private void initialazeDefault(){
        mDismissOnClick = true;
        mArrowOffsetY = mContext.getResources().getDimensionPixelSize(
                R.dimen.gd_arrow_offset);
    }

    public int getArrowOffsetY(){
        return mArrowOffsetY;
    }

    public void setArrowOffsetY(int offsetY){
        mArrowOffsetY = offsetY;
    }

    protected int getScreenWidth(){
        return mScreenWidth;
    }

    protected int getScreenHeight(){
        return mScreenHeight;
    }

    public void setDismissOnclick(boolean dismissOnclick){
        mDismissOnClick = dismissOnclick;
    }

    public boolean getDismissOnclick(){
        return mDismissOnClick;
    }

    public void setOnQuickActionClickListener(
            OnQuickActionClickListener listener) {
        mOnQuickActionClickListener = listener;
    }

    public void addQuickAction(QuickAction action){
        if (action != null){
            mQuickActions.add(action);
            mIsDirty = true;
        }
    }

    public void clearAllQucikActions(){
        if (!mQuickActions.isEmpty()){
            mQuickActions.clear();
            mIsDirty = true;
        }
    }

    public void show(View anchor){
        final View contentView = getContentView();
        if (contentView == null){
            throw new IllegalStateException(
                    "You need to set the content view using the setContentView method"
            );
        }

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final int[] loc = mLocation;
        anchor.getLocationOnScreen(loc);
        mRect.set(loc[0], loc[1], loc[0] + anchor.getWidth(),
                loc[1] + anchor.getHeight());

        if (mIsDirty){
            clearAllQucikActions();
            populateQuickActions(mQuickActions);
        }

        onMeasureAndLayout(mRect, contentView);

        if ((mPrivateFlags & MEASURE_AND_LAYOUT_DONE) != MEASURE_AND_LAYOUT_DONE){
            throw new IllegalStateException(
                    "onMeasureAndLayout() did not set the widget specification by calling"
                            + " setWidgetSpecs()"
            );
        }

    }

    protected void clearQuickActions(){
        if (!mQuickActions.isEmpty()){
            onClearQucikActions();

        }
    }

    protected void onClearQucikActions(){
    }
    protected abstract void populateQuickActions(List<QuickAction> quickActions);

    protected abstract void onMeasureAndLayout(Rect anchorRect, View contentView);

    protected void setWidgetSpecs(int popupY, boolean isOnTop){
        mPopupY = popupY;
        mIsOnTop = isOnTop;

        mPrivateFlags |= MEASURE_AND_LAYOUT_DONE;
    }

    private void showArrow(){
        final View contentView = getContentView();
        final int arrowId = mIsOnTop ? R.id.gdi_arrow_down : R.id.gdi_arrow_up;
        final View arrow = contentView.findViewById(arrowId);
        final View arrowUp = contentView.findViewById(R.id.gdi_arrow_up);
        final View arrowDown = contentView.findViewById(R.id.gdi_arrow_down);

        if (arrowId == R.id.gdi_arrow_up) {
            arrowUp.setVisibility(View.VISIBLE);
            arrowDown.setVisibility(View.INVISIBLE);
        } else if (arrowId == R.id.gdi_arrow_down) {
            arrowUp.setVisibility(View.INVISIBLE);
            arrowDown.setVisibility(View.VISIBLE);
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) arrow
                .getLayoutParams();
        params.leftMargin = mRect.centerX() - (arrow.getMeasuredWidth()) / 2;
    }

    private void prepareAnimationStyle(){
        final int screenWidth = mScreenWidth;
        final boolean onTop = mIsOnTop;
        final int arrowPointX = mRect.centerX();

        if (arrowPointX <= screenWidth / 4){
            setAnimationStyle(onTop ? R.style.popup_left : R.style.popdown_left);
        } else if (arrowPointX >= 3 * screenWidth / 4){
            setAnimationStyle(onTop ? R.style.popup_right : R.style. popdown_right);
        } else {
            setAnimationStyle(onTop ? R.style.popup_center : R.style.popdown_right );
        }
    }

    protected Context getContext(){
        return mContext;
    }

    protected OnQuickActionClickListener getOnQuickActionClickListener(){
        return mOnQuickActionClickListener;
    }
}
