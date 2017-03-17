package com.example.think.emulatebd.slidinglayer;

import java.util.Random;

/**
 * Created by HuangMei on 2016/12/16.
 */

public class CommonUtils {
    private static Random mRandom;
    public static boolean getNextRandomBoolean(){
        if (mRandom == null){
            mRandom = new Random();
        }
        return mRandom.nextBoolean();
    }
}
