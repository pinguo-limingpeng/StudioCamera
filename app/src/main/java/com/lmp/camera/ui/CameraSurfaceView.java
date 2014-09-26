package com.lmp.camera.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback, AutoFocusCallback {// 自动对焦接口

    public interface CameraOpenChangedListener {
        public void onOpenOrCloseScanChanged(boolean isOpen);

        public void onOpenOrCloseCameraChanged(boolean isOpen);
    }

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Bitmap mBitmap;
    private int mWidth, mHeight;
    private PictureCallback mPictureCallback;
    private boolean isOpen = true;
    private boolean isScan = true;
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
            startCamera();
        }
        openScan();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
        parameters.setPictureSize(mWidth, mHeight); // 设置大小
        parameters.set("rotation", 90);
        mCamera.setParameters(parameters);
    }

    // 在surface的大小发生改变时激发
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    // 销毁时激发，一般在这里将画图的线程停止、释放。
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeScan();
        closeCamera();
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
                FileOutputStream out = null;
                try {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        return;
                    }
                    mBitmap = BitmapFactory.decodeByteArray(data, 0,
                            data.length);

                    String fileName = String.format("%d.jpg", System.currentTimeMillis());
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
                    out = new FileOutputStream(file);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

    }

    public void setPictureCallback(PictureCallback callback) {
        mPictureCallback = callback;
    }

    //自动聚焦
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {//聚焦成功
            mCamera.takePicture(null, null, mPictureCallback);
        }
    }

    public void setOnCameraOpenOrCloseChangedListener(
            CameraOpenChangedListener listener) {
        mOpenChangedListener = listener;
    }

    /**
     * 打开照相机
     */
    public void startCamera() {
        mCamera = Camera.open();
        openScan();
        isOpen = true;
    }

    /**
     * 关闭照相机
     */
    public void closeCamera() {
        closeScan();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        isOpen = false;
    }

    /**
     * 开启预览
     */
    public void openScan() {
        if (mCamera != null) {
            try {
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);// 通过surfaceview显示取景画面
                mCamera.startPreview();// 开始预览
            } catch (Exception e) {
                mCamera.release();
                mCamera = null;
            }
            isScan = true;
        }
    }

    /**
     * 关闭预览
     */
    public void closeScan() {
        if (mCamera != null) {
            mCamera.stopPreview();
            isScan = false;
        }
    }

    public void openOrCloseScan() {
        if (isScan) {
            closeScan();
        } else {
            openScan();
        }
        if (mOpenChangedListener != null) {
            mOpenChangedListener.onOpenOrCloseScanChanged(isScan);
        }
    }

    public void openOrCloseCamera() {
        if (isOpen) {
            closeCamera();
            setBackgroundColor(Color.BLACK);
        } else {
            setBackground(null);
            startCamera();
        }
        if (mOpenChangedListener != null) {
            mOpenChangedListener.onOpenOrCloseCameraChanged(isOpen);
        }
    }

    /**
     * 是否支持变焦
     */
    public boolean isSupportZoom() {
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        return mCamera.getParameters().isSmoothZoomSupported();
    }

    /**
     * 设置变焦
     */
    public void setZoom(int zoom) {
        if (!isSupportZoom()) {
            return;
        }
        try {
            Camera.Parameters params = mCamera.getParameters();
            final int MAX = params.getMaxZoom();
            if (MAX == 0) return;

            if (zoom > MAX) {
                zoom = MAX;
            }
            params.setZoom(zoom);
            mCamera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取最大变焦值
     */
    public int getMaxZoom() {
        if (!isSupportZoom()) {
            return -1;
        }
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        Camera.Parameters params = mCamera.getParameters();
        return params.getMaxZoom();
    }

    public void setAutoFocus() {
        if (mCamera != null) {
            mCamera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    if (b) {
                        mCamera.setOneShotPreviewCallback(null);
                    }
                }
            });
        }
    }

    /**
     * 获取摄像头支持的各种分辨率
     */
    public List<Camera.Size> getsupportedPictureSizes() {
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        Camera.Parameters params = mCamera.getParameters();
        return params.getSupportedPictureSizes();
    }

    /**
     * 获取预览的各种分辨率
     */
    public List<Camera.Size> getSupportedPreviewSizes() {
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        Camera.Parameters params = mCamera.getParameters();
        return params.getSupportedPreviewSizes();
    }

    /**
     * 设置照片分辨率
     */
    public void setPictureSize(int width, int height) {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.setPictureSize(width, height);
            for (Camera.Size size : getSupportedPreviewSizes()) {
                if (size.width == width && size.height == height) {
                    params.setPreviewSize(width, height);
                }
            }
            mCamera.setParameters(params);
        }
    }

    public boolean isOpen() {
        return isOpen;
    }
}
