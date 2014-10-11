package com.lmp.cavnvasbitmap.app2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by camera360 on 14-10-10.
 */
public class TouchImageView extends ImageView {
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private Bitmap mBitmap;
    private Matrix mSavedMatrix = new Matrix();
    private Matrix mMatrix = new Matrix();
    private Matrix mMatrix1 = new Matrix();
    private int mWidth, mHeight;
    private float mXDown = 0;
    private float mYDown = 0;
    private float oldDist = 1f;
    private float oldRotation = 0;
    private int mode;
    private PointF mid = new PointF();

    public TouchImageView(Context context) {
        super(context, null);
        init();
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init() {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gintama);
        mWidth = getResources().getDisplayMetrics().widthPixels;
        mHeight = getResources().getDisplayMetrics().heightPixels;

    }

    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(mBitmap, mMatrix, null);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {//多点触控
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                mXDown = event.getX();
                mYDown = event.getY();
                mSavedMatrix.set(mMatrix);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                oldDist = spacing(event);
                oldRotation = rotation(event);
                mSavedMatrix.set(mMatrix);
                midPoint(mid, event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    mMatrix1.set(mSavedMatrix);
                    float rotation = rotation(event) - oldRotation;
                    float newDist = spacing(event);
                    float scale = newDist / oldDist;
                    mMatrix1.postScale(scale, scale, mid.x, mid.y);// 縮放
                    mMatrix1.postRotate(rotation, mid.x, mid.y);// 旋轉
                    if (!matrixCheck()) {
                        mMatrix.set(mMatrix1);
                        invalidate();
                    }
                } else if (mode == DRAG) {
                    mMatrix1.set(mSavedMatrix);
                    mMatrix1.postTranslate(event.getX() - mXDown, event.getY()
                            - mYDown);// 平移
                    if (!matrixCheck()) {//是否移出边界
                        mMatrix.set(mMatrix1);
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
        }
        return true;
    }

    private boolean matrixCheck() {
        float[] f = new float[9];
        mMatrix1.getValues(f);
        // 图片4个顶点的坐标
        float x1 = f[0] * 0 + f[1] * 0 + f[2];
        float y1 = f[3] * 0 + f[4] * 0 + f[5];
        float x2 = f[0] * mBitmap.getWidth() + f[1] * 0 + f[2];
        float y2 = f[3] * mBitmap.getWidth() + f[4] * 0 + f[5];
        float x3 = f[0] * 0 + f[1] * mBitmap.getHeight() + f[2];
        float y3 = f[3] * 0 + f[4] * mBitmap.getHeight() + f[5];
        float x4 = f[0] * mBitmap.getWidth() + f[1] * mBitmap.getHeight() + f[2];
        float y4 = f[3] * mBitmap.getWidth() + f[4] * mBitmap.getHeight() + f[5];
        // 图片现宽度
        double width = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        // 缩放比率判断
        if (width < mWidth / 3 || width > mWidth * 3) {
            return true;
        }
        // 出界判断
        if ((x1 < mWidth / 3 && x2 < mWidth / 3
                && x3 < mWidth / 3 && x4 < mWidth / 3)
                || (x1 > mWidth * 2 / 3 && x2 > mWidth * 2 / 3
                && x3 > mWidth * 2 / 3 && x4 > mWidth * 2 / 3)
                || (y1 < mHeight / 3 && y2 < mHeight / 3
                && y3 < mHeight / 3 && y4 < mHeight / 3)
                || (y1 > mHeight * 2 / 3 && y2 > mHeight * 2 / 3
                && y3 > mHeight * 2 / 3 && y4 > mHeight * 2 / 3)) {
            return true;
        }
        return false;
    }

    /**
     * 触碰两点间距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 设置手势中心点
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 取亮点间旋转角度
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    // 将移动，缩放以及旋转后的图层保存为新图片
    // 本例中沒有用到該方法，需要保存圖片的可以參考
    public Bitmap CreatNewPhoto() {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight,
                Bitmap.Config.ARGB_8888); // 背景图片
        Canvas canvas = new Canvas(bitmap); // 新建画布
        canvas.drawBitmap(mBitmap, mMatrix, null); // 画图片
        canvas.save(Canvas.ALL_SAVE_FLAG); // 保存画布
        canvas.restore();
        return bitmap;
    }
}
