package com.example.think.emulatebd.xlistview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.example.think.emulatebd.R;

/**
 * Created by HuangMei on 2016/12/13.
 */

public class MsgListView extends ListView implements AbsListView.OnScrollListener {

    private float mLastY = -1;
    private Scroller mScroller;
    private OnScrollListener mScrollListener;

    private XListViewListener mListViewListener;

    private MsgHeader mHeaderView;
    private RelativeLayout mHeaderViewContent;

    private int mHeaderViewHeight;
    private boolean mEnablePullRefresh = true;
    private boolean mPullRefreshing = true;

    private XListViewFooter mFooterView;
    private boolean mEnablePullLoad;
    private boolean mPullLoading;
    private boolean mIsFooterReady = false;

    private int mToatalItemCount;

    private int mScrollBack;
    private final static int SCROLLBACK_HEDAER = 0;
    private final static int SCROLLBACK_FOOTER = 1;

    private final static int SCROLL_DURATION = 400;
    private final static int PULL_LOAD_MORE_DELTA = 50;

    private final static float OFFSET_RADIO = 1.8f;

    public MsgListView(Context context) {
        super(context);
    }

    private void initWithContext(Context context){
        mScroller = new Scroller(context, new DecelerateInterpolator());
        super.setOnScrollListener(this);

        mHeaderView = new MsgHeader(context);
        mHeaderViewContent = (RelativeLayout) mHeaderView
                .findViewById(R.id.msg_header_content);
        addHeaderView(mHeaderView);
        mFooterView = new XListViewFooter(context);

        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onGlobalLayout() {
                        mHeaderViewHeight = mHeaderViewContent.getHeight();
                        getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                }
        );
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mIsFooterReady == false){
            mIsFooterReady = true;
            addFooterView(mFooterView);
        }
        super.setAdapter(adapter);
    }

    public void setPullRefreshEnable(boolean enable){
        mEnablePullRefresh = enable;
        if (!mEnablePullRefresh){
            mHeaderViewContent.setVisibility(INVISIBLE);
        } else {
            mHeaderViewContent.setVisibility(VISIBLE);
        }
    }

    public void setPullLoadEnable(boolean enable){
        mEnablePullLoad = enable;
        if (!mEnablePullLoad){
            mFooterView.hide();
            mFooterView.setOnClickListener(null);
        } else {
            mPullLoading = false;
            mFooterView.show();
            mFooterView.setState(XListViewFooter.STATE_NORMAL);
            mFooterView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    startLoadMore();
                }
            });
        }
    }

    public void stopRefresh(){
        if (mPullRefreshing == true){
            mPullRefreshing = false;
            resetHeaderHeight();
        }
    }

    public void stopLoadMore(){
        if (mPullLoading == true){
            mPullLoading = false;
            mFooterView.setState(XListViewFooter.STATE_NORMAL);
        }
    }

    private void invokeOnScrolling(){
        if (mScrollListener instanceof OnXScrollListener){
            OnXScrollListener l = (OnXScrollListener)mScrollListener;
            l.onXScrolling(this);
        }
    }

    private void updateHeaderHeight(float delta){
        mHeaderView.setVisiableHeight((int)delta + mHeaderView.getVisiableHeight());
        if (mEnablePullRefresh && !mPullRefreshing){
            if (mHeaderView.getVisiableHeight() > mHeaderViewHeight){
                mHeaderView.setState(XListViewHeader.STATE_READY);
            } else {
                mHeaderView.setState(XListViewHeader.STATE_NORMAL);
            }
        }
        setSelection(0);
    }

    private void resetHeaderHeight(){
        int height = mHeaderView.getVisiableHeight();
        if (height == 0)
            return;
        if (mPullRefreshing && height <= mHeaderViewHeight){
            return;
        }

        int finalHeight = 0;
        if (mPullRefreshing && height > mHeaderViewHeight){
            finalHeight = mHeaderViewHeight;
        }

        mScrollBack = SCROLLBACK_HEDAER;
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        invalidate();
    }

    private void updateFooterHeight(float delta){
        int height = mFooterView.getBottomMargin() + (int)delta;
        if (mEnablePullLoad && !mPullLoading){
            if (height > PULL_LOAD_MORE_DELTA){
                mFooterView.setState(XListViewFooter.STATE_READY);
            } else {
                mFooterView.setState(XListViewFooter.STATE_NORMAL);
            }
        }
        mFooterView.setBottomMargin(height);
    }

    private void resetFooterHeight(){
        int bottomMargin = mFooterView.getBottomMargin();
        if (bottomMargin > 0){
            mScrollBack = SCROLLBACK_FOOTER;
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
        }
    }

    private void startLoadMore(){
        mPullLoading = true;
        mFooterView.setState(XListViewFooter.STATE_LOADING);
        if (mListViewListener != null){
            mListViewListener.onLoadMore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1){
            mLastY = ev.getRawY();
        }

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (getFirstVisiblePosition() == 0
                        && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)){
                    updateHeaderHeight(deltaY / OFFSET_RADIO);
                }
                break;
            default:
                mLastY = -1;
                if (getFirstVisiblePosition() == 0){
                    if (mEnablePullRefresh &&
                            mHeaderView.getVisiableHeight() > mHeaderViewHeight){
                        mPullRefreshing = true;
                        mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
                        if (mListViewListener != null){
                            mListViewListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                } else if (getLastVisiblePosition() == mToatalItemCount - 1){
                    if (mEnablePullLoad
                            && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA){
                        startLoadMore();
                    }
                    resetFooterHeight();
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){
            if (mScrollBack == SCROLLBACK_HEDAER){
                mHeaderView.setVisiableHeight(mScroller.getCurrY());
            } else {
                mFooterView.setBottomMargin(mScroller.getCurrY());
            }
            postInvalidate();
            invokeOnScrolling();
        }
        super.computeScroll();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        super.setOnScrollListener(l);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if (mScrollListener != null){
            mScrollListener.onScrollStateChanged(absListView, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mToatalItemCount = totalItemCount;
        if (mScrollListener != null){
            mScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setXListViewListener(XListViewListener l){
        mListViewListener = l;
    }

    public interface OnXScrollListener extends OnScrollListener{
        public void onXScrolling(View view);
    }

    public interface XListViewListener{
        public void onRefresh();
        public void onLoadMore();
    }
}
