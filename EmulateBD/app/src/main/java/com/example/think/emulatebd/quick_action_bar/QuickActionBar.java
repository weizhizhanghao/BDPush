package com.example.think.emulatebd.quick_action_bar;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.think.emulatebd.R;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by HuangMei on 2016/12/20.
 */

public class QuickActionBar extends QuickActionWidget{

    private HorizontalScrollView mScrollView;
    private Animation mRackAnimation;
    private RelativeLayout mRack;
    private ViewGroup mQuickActionItems;

    private List<QuickAction> mQuickActions;

    public QuickActionBar(Context context) {
        super(context);

        mRackAnimation = AnimationUtils.loadAnimation(context, R.anim.gd_rack);

        mRackAnimation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                final float inner = (input * 1.55f) - 1.1f;
                return 1.2f - inner * inner;
            }
        });

        setContentView(R.layout.gd_quick_action_bar);

        final View v = getContentView();
        mRack = (RelativeLayout) v.findViewById(R.id.gdi_rack);
        mQuickActionItems = (ViewGroup) v.findViewById(R.id.gdi_quick_action_items);
        mScrollView = (HorizontalScrollView) v.findViewById(R.id.gdi_scroll);
    }

    @Override
    public void show(View anchor) {
        super.show(anchor);
        mScrollView.scrollTo(0, 0);
        mRack.startAnimation(mRackAnimation);
    }

    @Override
    protected void populateQuickActions(List<QuickAction> quickActions) {
        mQuickActions = quickActions;

        final LayoutInflater inflater = LayoutInflater.from(getContext());

        for (QuickAction action : quickActions){
            TextView view = (TextView) inflater.inflate(R.layout.gd_quick_action_bar_item, mQuickActionItems, false);
            view.setText(action.mTitle);

            view.setCompoundDrawablesWithIntrinsicBounds(null, action.mDrawable, null, null);
            view.setOnClickListener(mClickHandlerInternal);
            mQuickActionItems.addView(view);
            action.mView = new WeakReference<View>(view);
        }
    }

    @Override
    protected void onMeasureAndLayout(Rect anchorRect, View contentView) {
        contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contentView.measure(View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.EXACTLY), ViewGroup.LayoutParams.WRAP_CONTENT);

        int rootHeight = contentView.getMeasuredHeight();

        int offsetY = getArrowOffsetY();
        int dyTop = anchorRect.top;
        int dyBottom = getScreenHeight() - anchorRect.bottom;

        boolean onTop = (dyTop > dyBottom);
        int popupY = (onTop) ? anchorRect.top - rootHeight + offsetY
                    : anchorRect.bottom - offsetY;

        setWidgetSpecs(popupY, onTop);
    }

    @Override
    protected void onClearQucikActions() {
        super.onClearQucikActions();
        mQuickActionItems.removeAllViews();
    }

    private View.OnClickListener mClickHandlerInternal = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            final OnQuickActionClickListener listener = getOnQuickActionClickListener();

            if (listener != null){
                final int itemCount = mQuickActions.size();
                for (int i = 0; i < itemCount; i ++){
                    if (v == mQuickActions.get(i).mView.get()){
                        listener.onQuickActionClicked(QuickActionBar.this, i);
                        break;
                    }
                }
            }
            if (getDismissOnclick()){
                dismiss();
            }
        }
    };
}
