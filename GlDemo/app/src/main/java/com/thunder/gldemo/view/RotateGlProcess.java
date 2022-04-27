package com.thunder.gldemo.view;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.thunder.gllibrary.GlUtil;
import com.thunder.gllibrary.VBOHelper;

public class RotateGlProcess {

    public static String vertexShader = "attribute vec2 a_position;\n" +
            "attribute vec2 a_texcoord;\n" +
            "varying vec2 v_texcoord;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(a_position.x,a_position.y, 0, 1);\n" +
            "    v_texcoord = a_texcoord;\n" +
            "}";
    public static String fragmentShader = "precision mediump float;\n" +
            "// External texture containing video decoder output.\n" +
            "uniform sampler2D uImageTexture;\n" +
            "uniform vec4 imageRect;\n" +
            "uniform vec4 imageDestRect;\n" +
            "varying vec2 v_texcoord;\n" +
            "uniform mat4 textureTransform;\n" +
            "void main() {\n" +
            "       float redirX=(imageRect.b-imageRect.r);\n"+
            "       float redirY=(imageRect.a-imageRect.g);\n"+
            "       float pointX=(v_texcoord.x-imageRect.r)/redirX-0.5;\n"+
            "       float pointY=(v_texcoord.y-imageRect.g)/redirY-0.5;\n"+
            "       if(pointX > 0.0  && pointX < 0.1 && pointY >0.0 && pointY < 0.1) {\n"+
            "           gl_FragColor = vec4(1,1,1,1);\n"+
            "           return;\n"+
            "       }\n"+
            "        vec2 imagexy = (textureTransform*vec4(pointX,pointY, 0, 1)).xy;\n" +
            "       if(imagexy.x >= 0.0 && imagexy.x <= 1.0 && imagexy.y >= 0.0 && imagexy.y <= 1.0) {\n"+
            "             vec4 color = texture2D(uImageTexture, vec2((imagexy.x), imagexy.y));\n" +
            "             gl_FragColor = gl_FragColor * (1.0 - color.a) + color * color.a;\n" +
            "       } else {\n" +
            "            gl_FragColor = vec4(0,0,0,0);\n" +
            "       }\n" +
            "}";


    public static final int NO_TEXTURE = -1;
    private int glProgram;
    private  GlUtil.Attribute[] attributes;
    private   GlUtil.Uniform[] uniforms;
    protected int imageTexture = -1;
    private VBOHelper mVboHelper; //vbo绘制
    protected Bitmap iconBitmap;
    protected boolean needUpdate;
    private boolean isNeedUpdateSize;
    private static final String TAG = "RotateGlProcess";

    protected final Object syncBitmap = new Object();
    protected RectF iconRectF;



    public RotateGlProcess() {
        iconBitmap = null;
        needUpdate = true;
        isNeedUpdateSize = true;
        iconRectF = new RectF(0, 0, 0, 0);
        mVboHelper = new VBOHelper();
        mVboHelper.setFlipY(false);
    }

    public void updateIcon(Bitmap _bitmap, RectF _rect) {
        Log.i(TAG, "updateIcon: " + _bitmap + "..." + _rect);
        synchronized (syncBitmap) {
            if (iconBitmap != null) {
                iconBitmap.recycle();
            }
            iconBitmap = _bitmap;
            needUpdate = true;
            if (_rect != null) {
                isNeedUpdateSize = !_rect.equals(iconRectF);
                if (isNeedUpdateSize) {
                    iconRectF.set(_rect);
                }
            }
        }
    }

    /**
     * 加载编译shader
     */
    public void onInit() {
        Log.i(TAG, "onInit: " );
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        String vertexShaderCode = vertexShader;
        String fragmentShaderCode = fragmentShader;
        glProgram = GlUtil.compileProgram(vertexShaderCode, fragmentShaderCode);
        this.attributes = GlUtil.getAttributes(glProgram);
        this.uniforms = GlUtil.getUniforms(glProgram);
        mVboHelper.initialize(attributes);
        mVboHelper.bind();
    }
    float[] textureTransform = {
            1, 0, 0, 0,
            0, 1, 0, 0, //y 缩放
            0, 0, 1.0f, 0,
            0.5f, 0.5f, 0, 1.0f   //平移
    };
    private float degree=0;

    public void setDegree(float degree) {
        this.degree = degree % 360;
    }

    public void onDraw() {
        if (needUpdate) {
            synchronized (syncBitmap) {
                if (needUpdate) {
                    if (isNeedUpdateSize && imageTexture != NO_TEXTURE) {
                        GLES20.glDeleteTextures(1, new int[]{imageTexture}, 0);
                        imageTexture = NO_TEXTURE;
                        isNeedUpdateSize = false;
                    }
                    if (iconBitmap != null) {
                        imageTexture = loadTexture(iconBitmap, imageTexture);
                    }
                    needUpdate = false;
                }
            }
            GLES20.glUseProgram(glProgram);

        }
        for (GlUtil.Uniform uniform : uniforms) {
            switch (uniform.name) {
                case "uImageTexture":
                    uniform.setSamplerTexId(imageTexture, /* unit= */ 0);
                    break;
                case "imageRect":
                    uniform.setFloatVec4(iconRectF.left, iconRectF.top, iconRectF.right, iconRectF.bottom);
                    break;
                case "textureTransform":
                    textureTransform[0] = (float) Math.cos(degree);
                    textureTransform[1] = -(float) Math.sin(degree);
                    textureTransform[4] = (float) Math.sin(degree);
                    textureTransform[5] = (float) Math.cos(degree);
                    uniform.setFloatMAT4(textureTransform);
                    break;
            }
        }
        for (GlUtil.Uniform copyExternalUniform : uniforms) {
            copyExternalUniform.bind();
        }
        mVboHelper.draw();
        Log.d(TAG, "onDraw: ");
    }

    /**
     * 回收处理
     */
    public void onDestroy() {
        GLES20.glDeleteProgram(glProgram);
        GLES20.glDeleteTextures(1, new int[]{imageTexture}, 0);
    }


    public static int loadTexture(final Bitmap image, final int reUseTexture) {
        int[] texture = new int[1];
        if (reUseTexture == NO_TEXTURE) {
            GLES20.glGenTextures(1, texture, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, reUseTexture);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, image);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            texture[0] = reUseTexture;
        }
        return texture[0];
    }
}
