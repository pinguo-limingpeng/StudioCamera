package com.lmp.camera.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CameraSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback, AutoFocusCallback {// 自动对焦接口

    public interface CameraOpenChangedListener {
        public void onCameraOpenChanged(boolean isOpen);
    }

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Bitmap mBitmap;
    private int mWidth, mHeight;
    private PictureCallback mPictureCallback;
    private boolean isOpen = true;
    private CameraOpenChangedListener mOpenChangedListener;

    public CameraSurfaceView(Context context) {
        super(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mWidth = getResources().getDisplayMetrics().widthPixels;
        mHeight = getResources().getDisplayMetrics().heightPixels;
        mHolder = getHolder();
        mHolder.addCallback(this);
        // 设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到用户面前
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // 在创建时激发，一般在这里调用画图的线程。
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);// 通过surfaceview显示取景画面
            mCamera.startPreview();// 开始预览
        } catch (Exception e) {
            mCamera.release();
            mCamera = null;
        }
    }

    // 在surface的大小发生改变时激发
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    // 销毁时激发，一般在这里将画图的线程停止、释放。
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * 拍照
     */
    public void takePicture() {
        if (mCamera != null && isOpen) {
            if (mPictureCallback == null) {
                setPictureCallback();
            }
            mCamera.autoFocus(this);// 自动对焦
        }
    }

    public void setPictureCallback() {
        // 拍照并且保存的回调函数
        mPictureCallback = new PictureCallback() {

            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        return;
                    }
                    mBitmap = BitmapFactory.decodeByteArray(data, 0,
                            data.length);
                    File file = new File(Environment
                            .getExternalStorageDirectory().getPath(),
                            "/camera/camera00001");
                    BufferedOutputStream bos = new BufferedOutputStream(
                            new FileOutputStream(file));
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                    bos.flush();
                    bos.close();
                    Canvas canvas = mHolder.lockCanvas();
                    canvas.drawBitmap(mBitmap, 0, 0, null);
                    mHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

    }

    public void setPictureCallback(PictureCallback callback) {
        mPictureCallback = callback;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
            parameters.setPictureSize(mWidth, mHeight); // 设置大小
            parameters.set("rotation", 90);
            mCamera.setParameters(parameters);
            mCamera.takePicture(null, null, mPictureCallback);
        }
    }

    public void setOnCameraOpenChangedListener(
            CameraOpenChangedListener listener) {
        mOpenChangedListener = listener;
    }

    /**
     * 打开照相机
     */
    public void startCamera() {
        if (isOpen) {
            return;
        }
        surfaceCreated(mHolder);
        isOpen = true;
    }

    public void stopCamera() {
        surfaceDestroyed(mHolder);
        isOpen = false;
    }

    public void changeCamera() {
        if (isOpen) {
            stopCamera();
        } else {
            startCamera();
        }
        if (mOpenChangedListener != null) {
            mOpenChangedListener.onCameraOpenChanged(isOpen);
        }
    }

    public boolean isOpen() {
        return isOpen;
    }
}
