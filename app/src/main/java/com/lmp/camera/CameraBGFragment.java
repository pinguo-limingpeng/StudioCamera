package com.lmp.camera;

import android.app.Fragment;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.lmp.camera.ui.CameraSurfaceView;
import com.lmp.camera.ui.CameraSurfaceView.CameraOpenChangedListener;

import mycamera.lmp.com.mycamera.R;

public class CameraBGFragment extends Fragment implements View.OnClickListener {

    private Button mPhotoStartBtn, mCameraButtonOpenOrClose;
    private CameraSurfaceView mCameraSurfaceview;
    private PictureCallback mPictureCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container,
                false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 拍照过程屏幕一直处于高亮
        mPhotoStartBtn = (Button) view.findViewById(R.id.btn_camera_ok);
        mCameraButtonOpenOrClose = (Button) view
                .findViewById(R.id.btn_camera_open_or_close);
        mCameraSurfaceview = (CameraSurfaceView) view
                .findViewById(R.id.surface_camera_view);
        mPhotoStartBtn.setOnClickListener(this);
        mCameraButtonOpenOrClose.setOnClickListener(this);
        mCameraSurfaceview
                .setOnCameraOpenChangedListener(new CameraOpenChangedListener() {

                    @Override
                    public void onCameraOpenChanged(boolean isOpen) {
                        if (isOpen) {
                            mCameraButtonOpenOrClose.setText(R.string.btn_close_text);
                        } else {
                            mCameraButtonOpenOrClose
                                    .setText(R.string.btn_open_text);
                        }
                    }
                });
        mCameraSurfaceview.setPictureCallback(mPictureCallback);
    }

    public void setPictureCallback(PictureCallback callback) {
        mPictureCallback = callback;
    }

    @Override
    public void onClick(View v) {
        if (v == mPhotoStartBtn) {
            mCameraSurfaceview.takePicture();
        } else if (v == mCameraButtonOpenOrClose) {
            mCameraSurfaceview.changeCamera();
        }
    }
}
