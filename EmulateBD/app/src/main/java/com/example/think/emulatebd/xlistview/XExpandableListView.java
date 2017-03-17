package com.example.think.emulatebd.xlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.think.emulatebd.R;
import com.example.think.emulatebd.common.util.TimeUtil;


/**
 * Created by HuangMei on 2016/12/12.
 */

public class XExpandableListView extends ExpandableListView implements
        AbsListView.OnScrollListener {

    private float mLastY = -1;
    private Scroller mScroller;
    private OnScrollListener mScrollListener;

    private IXListViewListener mListViewListener;

    private XListViewHeader mHeaderView;

    private RelativeLayout mHeaderViewContent;
    private TextView mHeaderTimeView;
    private int mHeaderViewHeight;
    private boolean mEnablePullRefresh = true;
    private boolean mPullRefreshing = false;

    private XListViewFooter mFooterView;
    private boolean mEnablePullLoad;
    private boolean mPullLoading;
    private boolean mIsFooterReady = false;

    private int mTotalItemCount;

    private int mScrollBack;
    private final static int SCROLLBACK_HEADER = 0;
    private final static int SCROLLBACK_FOOTER = 1;

    private final static int SCROLL_DURATION = 400;
    private final static int PULL_LOAD_MORE_DELTA = 50;

    private final static float OFFSET_RADIO = 1.8F;

    public XExpandableListView(Context context) {
        super(context);
    }

    public XExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initWithContext(Context context){
        mScroller = new Scroller(context, new DecelerateInterpolator());
        super.setOnScrollListener(this);

        mHeaderView = new XListViewHeader(context);
        mHeaderViewContent = (RelativeLayout) mHeaderView.
                findViewById(R.id.xlistview_header_content);
        mHeaderTimeView = (TextView) mHeaderView.
                findViewById(R.id.xlistview_header_time);
        addHeaderView(mHeaderView);

        mFooterView = new XListViewFooter(context);

        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mHeaderViewHeight = mHeaderViewContent.getHeight();
                        getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
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

    public void setRefreshTime(String time){
        mHeaderTimeView.setText(time);
    }

    public void setRefreshTime(long time){
        mHeaderTimeView.setText(TimeUtil.getChatTime(time));
    }

    private void invokeOnScrolling(){
        if (mScrollListener instanceof OnXScrollListener){
            OnXScrollListener l = (OnXScrollListener) mScrollListener;
            l.onXScrollLing(this);
        }
    }

    private void updateHeaderHeight(float delta){
        mHeaderView.setVisiableHeight((int) delta
            + mHeaderView.getVisiableHeinght());
        if (mEnablePullRefresh && !mPullRefreshing){
            if (mHeaderView.getVisiableHeinght() > mHeaderViewHeight){
                mHeaderView.setState(XListViewHeader.STATE_READY);
            } else {
                mHeaderView.setState(XListViewHeader.STATE_NORMAL);
            }
        }
        setSelection(0);
    }

    public void resetHeaderHeight(){
        int height = mHeaderView.getMeasuredHeight();
        if (height == 0)
            return;
        if (mPullRefreshing && height <= mHeaderViewHeight){
            return;
        }

        int finalHeight = 0;
        if (mPullRefreshing && height > mHeaderViewHeight){
            finalHeight = mHeaderViewHeight;
        }
        mScrollBack = SCROLLBACK_HEADER;
        mScroller.startScroll(0, height, 0, finalHeight - height,
                SCROLL_DURATION);

        invalidate();
    }

    private void updateFooterHeight(float delta){
        int height = mFooterView.getBottomMargin() - (int)delta;
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
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
                    SCROLL_DURATION);
            invalidate();
        }
    }

    public void startLoadMore(){
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
                        && (mHeaderView.getVisiableHeinght() > 0) || deltaY > 0){
                    updateHeaderHeight(deltaY / OFFSET_RADIO);
                    invokeOnScrolling();
                } else if (getLastVisiblePosition() == mTotalItemCount - 1
                        && (mFooterView.getBottomMargin() > 0)){
                    updateHeaderHeight(-deltaY/OFFSET_RADIO);
                }
                break;
            default:
                mLastY = -1;
                if (getFirstVisiblePosition() == 0){
                    if (mEnablePullRefresh && mHeaderView.getVisiableHeinght() > mHeaderViewHeight){
                        mPullRefreshing = true;
                        mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
                        if (mListViewListener != null){
                            mListViewListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                } else if (getLastVisiblePosition() == mTotalItemCount - 1){
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
            if (mScrollBack == SCROLLBACK_HEADER){
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
        mScrollListener = l;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if (mScrollListener != null){
            mScrollListener.onScrollStateChanged(absListView, i);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
        if (mScrollListener != null){
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setXListViewListener(IXListViewListener l){
        mListViewListener = l;
    }

    public interface OnXScrollListener extends OnScrollListener{
        public void onXScrollLing(View view);
    }

    public interface IXListViewListener{
        public void onRefresh();
        public void onLoadMore();
    }
}
