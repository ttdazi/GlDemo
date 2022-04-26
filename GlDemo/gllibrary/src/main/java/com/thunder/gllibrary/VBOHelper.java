package com.thunder.gllibrary;

import android.opengl.GLES20;

import java.nio.ShortBuffer;

public class VBOHelper {
    private final static int HANDLE_VERTEX = 0;
    private final static int HANDLE_INDEX = 1;
    private final static int HANDLE_TEXTURE_COORD = 2;
    private final static int HANDLE_NUM = 3;
    private int[] bufHandle = new int[HANDLE_NUM];

    private boolean needUpdateVBO = true;

    private GlUtil.Attribute attributePosition;
    private GlUtil.Attribute attributeTextureCoord;

    public VBOHelper() {
    }

    //顶点坐标
    private float[] mVertices = new float[] {
            -1.0f, -1.0f, 0.0f,     //0     left-bottom
            1.0f,  -1.0f, 0.0f,     //1     right-bottom
            1.0f,  1.0f,  0.0f,     //2     right-top
            -1.0f, 1.0f,  0.0f      //3     left-top
    };

    //索引点
    private short[] mIndices = {
            0, 3, 1,        //triangle 1
            1, 3, 2,        //triangle 2
    };

    //纹理坐标
    private float[] mTexCoords = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };

    //纹理反向左边坐标
    private float[] mTexCoordsFlipY = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };
    private boolean isFlipY;

    public void setFlipY(boolean isFlipY) {
        this.isFlipY = isFlipY;
    }
    public void initialize(GlUtil.Attribute[] attributes) {
        GLES20.glGenBuffers(bufHandle.length, bufHandle, 0);//创建vbobuffer
        for (GlUtil.Attribute attribute : attributes) {
            if (attribute.name.equals("a_position")) {
                attributePosition = attribute;
            } else if (attribute.name.equals("a_texcoord")) {
                attributeTextureCoord = attribute;
            }
        }
    }


    private float[] getFlipYTexCoords() {
        float[] coords = new float[mTexCoords.length];
        System.arraycopy(mTexCoords, 0, coords, 0, mTexCoords.length);
        int pointLength = coords.length / 2;
        for(int i = 0; i < pointLength; i++) {
            int yIndex = i * 2 + 1;
            coords[yIndex] = 1.0f - coords[yIndex];
        }
        return coords;
    }

    public void bind() {
        if(!needUpdateVBO)
            return;

        attributePosition.setBuffer(mVertices, 3);
        attributePosition.bind(bufHandle[HANDLE_VERTEX]);
        GlUtil.checkGlError();

//        attributeTextureCoord.setBuffer(getFlipYTexCoords(), 2);
        attributeTextureCoord.setBuffer(isFlipY?mTexCoordsFlipY:mTexCoords, 2);
        attributeTextureCoord.bind(bufHandle[HANDLE_TEXTURE_COORD]);
        GlUtil.checkGlError();

        ShortBuffer indicesBuffer = GlUtil.createBuffer(mIndices);
        indicesBuffer.position(0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufHandle[HANDLE_INDEX]);//绑定buffer
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,
                indicesBuffer.capacity() * GlUtil.BYTES_PER_SHORT,
                indicesBuffer,
                GLES20.GL_STATIC_DRAW); //上传数据到buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);//解除绑定
        GlUtil.checkGlError();

        needUpdateVBO = false;
    }

    public void draw() {
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufHandle[HANDLE_INDEX]);//绑定之前创建好的IBO
        GlUtil.checkGlError();

        //使用IBO指定的6个索引来绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mIndices.length,
                GLES20.GL_UNSIGNED_SHORT, 0);
        GlUtil.checkGlError();

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);//解除IBO绑定
        GlUtil.checkGlError();
    }

}
