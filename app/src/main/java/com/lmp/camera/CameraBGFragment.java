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

public class CameraBGFragment extends Fragment implements View.OnClickListener {

    private Button mPhotoStartBtn, mCameraSwitchButton, mScanSwitchButton;
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
        mCameraSwitchButton = (Button) view
                .findViewById(R.id.btn_camera_open_or_close);
        mCameraSurfaceview = (CameraSurfaceView) view
                .findViewById(R.id.surface_camera_view);
        mScanSwitchButton = (Button) view.findViewById(R.id.btn_scan_open_or_close);

        mScanSwitchButton.setOnClickListener(this);
        mPhotoStartBtn.setOnClickListener(this);
        mCameraSwitchButton.setOnClickListener(this);
        mCameraSurfaceview
                .setOnCameraOpenOrCloseChangedListener(new CameraOpenChangedListener() {

                    @Override
                    public void onOpenOrCloseScanChanged(boolean isOpen) {
                        if (isOpen) {
                            mScanSwitchButton.setText(R.string.btn_close_scan_text);
                        } else {
                            mScanSwitchButton
                                    .setText(R.string.btn_open_scan_text);
                        }
                    }

                    @Override
                    public void onOpenOrCloseCameraChanged(boolean isOpen) {
                        if (isOpen) {
                            mCameraSwitchButton.setText(R.string.btn_close_text);
                        } else {
                            mCameraSwitchButton
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
        } else if (v == mCameraSwitchButton) {
            mCameraSurfaceview.openOrCloseCamera();
        } else if (v == mScanSwitchButton) {
            mCameraSurfaceview.openOrCloseScan();
        }
    }
}
