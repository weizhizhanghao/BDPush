package com.example.think.emulatebd.wheel.adapters;

import android.content.Context;

/**
 * Created by HuangMei on 2016/12/14.
 */

public class NumericWheelAdapter extends AbstractWheelTextAdapter{

    public static final int DEFAULT_MAX_VALUE = 9;
    private static final int DEFAULT_MIN_VALUE = 0;

    private int minValue;
    private int maxValue;

    private String format;

    public NumericWheelAdapter(Context context) {
        this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue) {
        this(context, minValue, maxValue, null);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the wheel min value
     * @param maxValue the wheel max value
     * @param format the format string
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
        super(context);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
    }

    @Override
    protected CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()){
            int value = minValue + index;
            return format != null ? String.format(format, value) : Integer.toString(value);
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return maxValue - minValue + 1;
    }
}
