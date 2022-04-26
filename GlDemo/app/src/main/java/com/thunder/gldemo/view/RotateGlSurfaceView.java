package com.thunder.gldemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * <pre>
 *     author : 袁唯庆
 *     time   : 2022/04/22
 *     description: 用于旋转动画的GlSurfaceView
 * </pre>
 */
public class RotateGlSurfaceView extends GLSurfaceView {
    private static final String TAG = "RotateGlSurfaceView";
    private RotateGlProcess rotateGlProcess;
    private TimerControlInterface timerControl;
    private int width;
    private int pts = 40;
    private static final double Par = Math.PI;
    private double angularSpeed;
    private double degree = 0;
    private static final float defaultSpeed=1.57f;

    public RotateGlSurfaceView(Context context) {
        super(context);
        init();
    }

    public RotateGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 每秒多长 3.14 px/1 s 速度是相对
     *
     * @param speed
     */
    public void setSpeed(float speed) {
        float radius = width / 2.0f;
        //每次增加的角度
        float ptsUpdate = pts / 1000f;//将毫秒转为秒
        angularSpeed = 180 * speed * ptsUpdate / Par / radius;//根据公式2ParR=周长计算,单次pts运行多少角度
        Log.d(TAG, "setSpeed: " + radius + "..." + angularSpeed);
    }

    private void init() {
        setSpeed(defaultSpeed);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        rotateGlProcess = new RotateGlProcess();
        updateImage("132.png");
        RotateGlRender glRender = new RotateGlRender(rotateGlProcess);
        setRenderer(glRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        timerControl = TimerControl.getTimerControl();
        timerControl.setTimerPts(pts);
        timerControl.setTimerCallBack(new TimerControl.TimerCallBack() {
            @Override
            public void runTimerTask() {
                degree = degree + angularSpeed;
                rotateGlProcess.setDegree((float) degree);
                requestRender();
            }
        });
    }

    private void updateImage(String imageSrc) {
        InputStream open = null;
        Context context = getContext();
        try {
            open = context.getAssets().open(imageSrc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(open);
        if(imageSrc.equals("update.png")){

        }
        width = bitmap.getWidth();
        int height = bitmap.getHeight();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels; // 屏幕宽（像素，如：480px）
        int screenHeight = displayMetrics.heightPixels; // 屏幕高（像素，如：800p）
        float top = (screenHeight - height) / 2.0f / screenHeight;
        float left = (screenWidth - width) / 2.0f / screenWidth;
        float right = left + width * 1.0f / screenWidth;
        float bottom = top + height * 1.0f / screenHeight;
        RectF rectF = new RectF();
        rectF.set(left, top, right, bottom);
        rotateGlProcess.setScreen(displayMetrics.widthPixels,displayMetrics.heightPixels);
        rotateGlProcess.updateIcon(bitmap, rectF);
    }

    public void start() {
        timerControl.startRun();
    }

    public void pause() {
        timerControl.pause();
    }

    public void resume() {
        timerControl.onResume();
    }

    public void update() {
        //方案一图片合成
        //方案二 图片两个纹理
        //方案三 openGl区域绘制
       updateImage("update.png");
    }

    private static class RotateGlRender implements Renderer {
        RotateGlProcess rotateGlProcess;

        public RotateGlRender(RotateGlProcess rotateGlProcess) {
            this.rotateGlProcess = rotateGlProcess;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            rotateGlProcess.onInit();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            rotateGlProcess.onDraw();
        }
    }
}
