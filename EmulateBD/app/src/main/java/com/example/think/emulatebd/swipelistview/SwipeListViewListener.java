package com.example.think.emulatebd.swipelistview;

/**
 * Created by HuangMei on 2016/12/8.
 */

public interface SwipeListViewListener {

    void onOpened(int position, boolean toRight);

    void onClosed(int position, boolean fromRight);

    void onListChanged();

    void onMove(int position, float x);

    void onStartOpen(int position, int action, boolean right);

    void onStartClose(int position, boolean right);

    void onClickFrontView(int position);

    void onClickBackView(int position);

    void onDismiss(int[] reverseSortedPositions);

    int onChangeSwipeMode(int position);

}
