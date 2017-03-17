package com.example.think.emulatebd.switchbtn;

import android.os.Handler;
import android.os.Message;

/**
 * Created by HuangMei on 2016/12/8.
 */

public class FrameAnimationController {
    private static final int MSG_ANIMATE = 1000;
    public static final int ANIMATION_FREME_DURUTION = 1000 / 60;
    private static final Handler mHandler = new AnimationHandler();

    public FrameAnimationController() {
        throw new UnsupportedOperationException();
    }

    public static void requestAnimationFrame(Runnable runnable){
        Message message = new Message();
        message.what = MSG_ANIMATE;
        message.obj = runnable;
        mHandler.sendMessageDelayed(message, ANIMATION_FREME_DURUTION);
    }

    public static void requestFrameDelay(Runnable runnable, long delay){
        Message message = new Message();
        message.what = MSG_ANIMATE;
        message.obj = runnable;
        mHandler.sendMessageDelayed(message, delay);
    }

    private static class AnimationHandler extends Handler{
        public void handleMessage(Message m){
            switch (m.what){
                case MSG_ANIMATE:
                    if (m.obj != null){
                        ((Runnable) m.obj).run();
                    }
                    break;
            }
        }
    }
}
