package com.example.think.emulatebd.wheel;

/**
 * Created by HuangMei on 2016/12/14.
 */

public class ItemsRange {

    private int first;
    private int count;
    public ItemsRange(){
        this(0, 0);
    }

    public ItemsRange(int first, int count){
        this.first = first;
        this.count = count;
    }

    public int getFirst(){
        return first;
    }

    public int getLast(){
        return getFirst() + getCount() - 1;
    }

    public int getCount(){
        return count;
    }

    public boolean contains(int index){
        return index >= getFirst() && index <= getLast();
    }
}
