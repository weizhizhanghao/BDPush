package com.example.think.emulatebd.wheel.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by HuangMei on 2016/12/14.
 */

public abstract class AbstractWheelAdapter implements WheelViewAdapter{

    private List<DataSetObserver> dataSetObservers;

    @Override
    public View getEmptyItem(View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (dataSetObservers == null){
            dataSetObservers = new LinkedList<>();
        }
        dataSetObservers.add(observer);
    }

    @Override
    public void ungeristerDataSetObserver(DataSetObserver observer) {
        if (dataSetObservers != null){
            dataSetObservers.remove(observer);
        }
    }

    protected void notifyDataChangedEvent(){
        if (dataSetObservers != null){
            for (DataSetObserver observer : dataSetObservers){
                observer.onChanged();
            }
        }
    }

    protected void notifyDataInvalidatedEvent(){
        if (dataSetObservers != null){
            for (DataSetObserver observer : dataSetObservers){
                observer.onInvalidated();
            }
        }
    }
}
