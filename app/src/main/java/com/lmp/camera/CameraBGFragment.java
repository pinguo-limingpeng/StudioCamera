package com.lmp.camera;

import android.app.Fragment;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.lmp.camera.ui.CameraSurfaceView;
import com.lmp.camera.ui.CameraSurfaceView.CameraOpenChangedListener;

import java.util.ArrayList;
import java.util.List;

public class CameraBGFragment extends Fragment implements View.OnClickListener {

    private final static String PICTURESIZE_SPILT = "×";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1 && !isSmooth) {
                mCameraZoomBar.setVisibility(View.GONE);
            }
        }
    };

    private Button mPhotoStartBtn, mCameraSwitchButton, mScanSwitchButton;
    private CameraSurfaceView mCameraSurfaceview;
    private PictureCallback mPictureCallback;
    private SeekBar mCameraZoomBar;
    private boolean isSmooth;
    private Spinner mPictureSize;

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
        mCameraSwitchButton = (Button) view.findViewById(R.id.btn_camera_open_or_close);
        mCameraSurfaceview = (CameraSurfaceView) view.findViewById(R.id.surface_camera_view);
        mScanSwitchButton = (Button) view.findViewById(R.id.btn_scan_open_or_close);
        mCameraZoomBar = (SeekBar) view.findViewById(R.id.camera_seekbar);
        mPictureSize = (Spinner) view.findViewById(R.id.camera_picture_size);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, getSize());
        mPictureSize.setAdapter(adapter);
        mPictureSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] size = ((String) adapterView.getAdapter().getItem(i)).split(PICTURESIZE_SPILT);
                mCameraSurfaceview.setPictureSize(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        int max = mCameraSurfaceview.getMaxZoom();
        if (max > 0) {
            mCameraZoomBar.setMax(max);
            hideSeekBar();
        } else {
            mCameraZoomBar.setVisibility(View.GONE);
        }
        mCameraZoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mCameraSurfaceview.setZoom(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSmooth = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSmooth = false;
                hideSeekBar();
            }
        });

        mPhotoStartBtn.setOnClickListener(this);
        mScanSwitchButton.setOnClickListener(this);
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
        mCameraSurfaceview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && mCameraSurfaceview.isOpen() && mCameraSurfaceview.getMaxZoom() > 0) {
                    mCameraZoomBar.setVisibility(View.VISIBLE);
                    hideSeekBar();
                    return true;
                }
                return false;
            }
        });

        mPhotoStartBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //在此进行聚焦,当放手后进行拍照
                mCameraSurfaceview.setAutoFocus();
                return false;//返回false，则表示长按世界为执行完，将交给onclick事件进行处理
            }
        });
    }

    private List<String> getSize() {
        List<String> size = new ArrayList<String>();
        List<Camera.Size> pictureSize = mCameraSurfaceview.getsupportedPictureSizes();
        for (Camera.Size s : pictureSize) {
            size.add(s.width + PICTURESIZE_SPILT + s.height);
        }
        return size;
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

    private void hideSeekBar() {
        if (!isSmooth) {
            mHandler.removeMessages(1);
            mHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int max = mCameraSurfaceview.getMaxZoom();
        if (max > 0) {
            int i = mCameraZoomBar.getProgress();
            switch (keyCode) {
                // 音量减小
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    // 音量减小时应该执行的功能代码
                    i = i - 2;
                    if (i < 0) {
                        i = 0;
                    }
                    mCameraSurfaceview.setZoom(i);
                    mCameraZoomBar.setVisibility(View.VISIBLE);
                    mCameraZoomBar.setProgress(i);
                    hideSeekBar();
                    return true;
                // 音量增大
                case KeyEvent.KEYCODE_VOLUME_UP:
                    // 音量增大时应该执行的功能代码
                    i = i + 2;
                    if (i > max) {
                        i = max;
                    }
                    mCameraSurfaceview.setZoom(i);
                    mCameraZoomBar.setVisibility(View.VISIBLE);
                    mCameraZoomBar.setProgress(i);
                    hideSeekBar();
                    return true;

            }
        }
        return false;
    }
}
