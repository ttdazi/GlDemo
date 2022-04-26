package com.thunder.gldemo.view;

/**
 * <pre>
 *     author : 袁唯庆
 *     time   : 2022/04/26
 *     description:
 * </pre>
 */
interface TimerControlInterface {
    int getTimerPts();
    void setTimerPts(int pts);
    void startRun();
    void pause();
    void onResume();
    void clearMessage();
    void setTimerCallBack(TimerControl.TimerCallBack timerCallBack);
}
