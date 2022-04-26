package com.thunder.gldemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;

import com.thunder.gldemo.view.RotateGlSurfaceView;

public class MainActivity extends AppCompatActivity {

    private RotateGlSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                glStart(null);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    public void glStart(View view) {
        glSurfaceView.setSpeed(1.57f);
        glSurfaceView.start();
    }

    public void glImage(View view) {
        glSurfaceView.update();
    }

    public void glPause(View view) {
        glSurfaceView.pause();
    }

    public void glResume(View view) {
        glSurfaceView.resume();
    }
}