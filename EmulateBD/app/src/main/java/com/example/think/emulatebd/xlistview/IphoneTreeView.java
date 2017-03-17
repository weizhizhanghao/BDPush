package com.example.think.emulatebd.xlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;

/**
 * Created by HuangMei on 2016/12/13.
 */

public class IphoneTreeView extends ExpandableListView implements
        AbsListView.OnScrollListener, ExpandableListView.OnGroupClickListener{

    public IphoneTreeView(Context context) {
        super(context);
        registerListener();
    }

    public interface IphoneTreeHeaderAdapter{
        public static final int PINNED_HEADER_GONE = 0;
        public static final int PINNED_HEADER_VISIBLE = 1;
        public static final int PINNED_HEADER_PUSHED_UP = 2;

        int getTreeHeaderState(int groupPosition, int childPosition);

        void configureTreeHeader(View header, int groupPosition, int childPosition, int alpha);

        void onHeadViewClick(int groupPosition, int status);

        int getHeadViewClickStatus(int groupPosition);
    }

    private static final int MAX_ALPHA = 255;

    private IphoneTreeHeaderAdapter mAdapter;

    private View mHeaderView;

    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;

    private int mHeaderViewHeight;

    public void setHeaderView(View view){
        mHeaderView = view;
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        if (mHeaderView != null){
            setFadingEdgeLength(0);
        }

        requestLayout();
    }

    private void headerViewClick(){
        long packedPosition = getExpandableListPosition(this
                .getFirstVisiblePosition());

        int groupPosition = ExpandableListView
                .getPackedPositionGroup(packedPosition);

        if (mAdapter.getHeadViewClickStatus(groupPosition) == 1){
            this.collapseGroup(groupPosition);
            mAdapter.onHeadViewClick(groupPosition, 0);
        } else {
            this.expandGroup(groupPosition);
            mAdapter.onHeadViewClick(groupPosition, 1);
        }
        this.setSelectedGroup(groupPosition);
    }

    private float mDownX;
    private float mDownY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mHeaderViewVisible){
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mDownX = ev.getX();
                    mDownY = ev.getY();
                    if (mDownX <= mHeaderViewWidth && mDownY <= mHeaderViewHeight){
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float x = ev.getX();
                    float y = ev.getY();
                    float offsetX = Math.abs(x - mDownX);
                    float offsetY = Math.abs(y - mDownY);

                    if (x <= mHeaderViewWidth && y <= mHeaderViewHeight
                            && offsetX <= mHeaderViewWidth
                            && offsetY <= mHeaderViewHeight){
                        if (mHeaderView != null){
                            headerViewClick();
                        }
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (IphoneTreeHeaderAdapter)adapter;
    }


    protected void dispatchDraw(Canvas canvas){
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible){
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final long flatPos = getExpandableListPosition(firstVisibleItem);
        int groupPosition = ExpandableListView.getPackedPositionGroup(flatPos);
        int childPosition = ExpandableListView.getPackedPositionChild(flatPos);

        configureHeaderView(groupPosition, childPosition);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null){
            measureChild(mHeaderView,widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    private int mOldState = -1;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        final long flatPosition = getExpandableListPosition(getFirstVisiblePosition());
        final int groupPos = ExpandableListView
                .getPackedPositionGroup(flatPosition);
        final int childPos = ExpandableListView
                .getPackedPositionChild(flatPosition);

        int state = mAdapter.getTreeHeaderState(groupPos, childPos);
        if (mHeaderView != null && mAdapter != null && state != mOldState){
            mOldState = state;
            mHeaderView.layout(0,0, mHeaderViewWidth, mHeaderViewHeight);
        }
        configureHeaderView(groupPos, childPos);
    }

    public void configureHeaderView(int groupPosition, int childPosition){
        if (mHeaderView == null || mAdapter == null
                || ((ExpandableListAdapter)mAdapter).getGroupCount() == 0){
            return;
        }

        int state = mAdapter.getTreeHeaderState(groupPosition, childPosition);

        switch (state){
            case IphoneTreeHeaderAdapter.PINNED_HEADER_GONE:
                mHeaderViewVisible = false;
                break;
            case IphoneTreeHeaderAdapter.PINNED_HEADER_VISIBLE:
                mAdapter.configureTreeHeader(mHeaderView, groupPosition,
                        childPosition, MAX_ALPHA);

                if (mHeaderView.getTop() != 0){
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            case IphoneTreeHeaderAdapter.PINNED_HEADER_PUSHED_UP:
                View firstView = getChildAt(0);
                int bottom = firstView.getBottom();

                int headerHeight = mHeaderView.getHeight();
                int  y;
                int alpha;
                if (bottom < headerHeight){
                    y = (bottom - headerHeight);
                    alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
                } else {
                    y = 0;
                    alpha = MAX_ALPHA;
                }
                mAdapter.configureTreeHeader(mHeaderView, groupPosition, childPosition, alpha);

                if (mHeaderView.getTop() != y){
                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
                }
                mHeaderViewVisible = true;
                break;
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View view, int gruopPosition, long id) {
        if (mAdapter.getHeadViewClickStatus(gruopPosition) == 0){
            mAdapter.onHeadViewClick(gruopPosition, 1);
            parent.expandGroup(gruopPosition);
            parent.setSelectedGroup(gruopPosition);
        } else if (mAdapter.getHeadViewClickStatus(gruopPosition) == 1){
            mAdapter.onHeadViewClick(gruopPosition, 0);
            parent.collapseGroup(gruopPosition);
        }
        // 返回 true 才可以弹回第一行 , 不知道为什么
        return true;
    }

    private void registerListener(){
        setOnScrollListener(this);
        setOnGroupClickListener(this);
    }
}
