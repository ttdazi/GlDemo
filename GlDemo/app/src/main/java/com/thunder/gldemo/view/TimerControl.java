package com.thunder.gldemo.view;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * <pre>
 *     author : 袁唯庆
 *     time   : 2022/04/26
 *     description: loop 循环异步执行类
 * </pre>
 */
public class TimerControl implements TimerControlInterface {

    private final LoopHandler glHandler;
    private TimerCallBack timerCallBack;
    private boolean isPause;

    public int getTimerPts() {
        return timerPts;
    }

    private int timerPts=1000;//默认定时1s执行一次

    public void setTimerPts(int timerPts) {
        this.timerPts = timerPts;
    }

    public void setTimerCallBack(TimerCallBack timerCallBack) {
        this.timerCallBack = timerCallBack;
    }

    private TimerControl() {
        HandlerThread handlerThread = new HandlerThread("GL-HANDLER");
        handlerThread.start();
        glHandler = new LoopHandler(handlerThread.getLooper(),this);
    }

    public static TimerControlInterface getTimerControl() {
        return new TimerControl();
    }

    public void runThread(Runnable runnable) {
        glHandler.post(runnable);
    }
    public void startRun(){
        isPause = false;
        glHandler.sendEmptyMessage(LoopHandler.LOOP);
    }
    private void runCallBack(){
        if(timerCallBack!=null){
            timerCallBack.runTimerTask();
        }
    }

    @Override
    public void pause() {
        isPause = true;
    }

    @Override
    public void onResume() {
        isPause = false;
        startRun();
    }

    @Override
    public void clearMessage() {
        pause();
        glHandler.removeCallbacksAndMessages(null);
    }


    private static class LoopHandler extends Handler{
        private static final int LOOP=102;
        private WeakReference<TimerControl> timerControlWeakReference;
        public LoopHandler(Looper looper,TimerControl timerControl) {
            super(looper);
            timerControlWeakReference=new WeakReference<>(timerControl);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            TimerControl timerControl = timerControlWeakReference.get();
            if (timerControl == null) {
                clearMessageAndExit();
                return;
            }
            if (msg.what == LOOP && !timerControl.isPause) {
                timerControl.runCallBack();
                sendEmptyMessageDelayed(msg.what, timerControl.getTimerPts());
            }
        }
        private void clearMessageAndExit() {
            removeCallbacksAndMessages(null);
        }
    }

     interface TimerCallBack{

         void runTimerTask();
     }
}
