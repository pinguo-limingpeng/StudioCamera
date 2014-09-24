package com.lmp.camera;

import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class CameraResultShowFragment extends Fragment implements
        OnClickListener {

    private ImageView mPhotoImg;
    private byte[] mData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragmetn_result_show,
                container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mData = getArguments().getByteArray(MainActivity.CAMERA_RESULE_PHOTO);
        mPhotoImg = (ImageView) view.findViewById(R.id.img_resule_show);
        mPhotoImg.setImageBitmap(BitmapFactory.decodeByteArray(mData, 0,
                mData.length));
    }

    @Override
    public void onClick(View v) {
    }
}
