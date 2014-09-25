package com.lmp.camera;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;


public class MainActivity extends Activity {

    public static String CAMERA_RESULE_PHOTO = "camera_result_photo";
    private FragmentManager mManager;
    private FragmentTransaction mTransaction;
    private CameraBGFragment mBgFragment;
    private CameraResultShowFragment mShowFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = getFragmentManager();

        mBgFragment = (CameraBGFragment) mManager.findFragmentByTag("mBgFragment");
        if (mBgFragment == null) {
            mBgFragment = new CameraBGFragment();
            mBgFragment.setPictureCallback(new PictureCallback() {

                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    mTransaction = mManager.beginTransaction();
                    mShowFragment = new CameraResultShowFragment();
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(CAMERA_RESULE_PHOTO, data);
                    mShowFragment.setArguments(bundle);
                    mTransaction.replace(R.id.container, mShowFragment, "mShowFragment");
                    mTransaction.addToBackStack(null);
                    mTransaction.commit();
                }
            });
        }

        mTransaction = mManager.beginTransaction();
        mTransaction.add(R.id.container, mBgFragment, "mBgFragment");
        mTransaction.commit();
    }
}
