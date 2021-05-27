package com.ckc.photopicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * <pre>
 *     author : 陈孔财
 *     e-mail : chenkongcai@lexiangbao.com
 *     time   : 2021/5/27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class GestureImageView extends AppCompatImageView {

    private static final String TAG = "GestureImageView";
    private Matrix mSuppMatrix = new Matrix();
    private Matrix mBaseMatrix = new Matrix();
    private Matrix mDrawableMatrix = new Matrix();
    private RectF mDisplayRect = new RectF();
    private ScaleGestureDetector scaleGestureDetector;
    private float drawableWidth, drawableHeight;
    private float viewWidth, viewHeight;
    private GestureDetector gestureDetector;

    public GestureImageView(@NonNull Context context) {
        super(context);
    }

    public GestureImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);

        //GestureDetector的实例生成
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }

                Log.e(TAG, "onScale scaleFactor="+scaleFactor+", focusX="+focusX+", focusY="+focusY);
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
//                Log.e(TAG, "getDisplayRect Drawable, getIntrinsicWidth="+getDrawable().getIntrinsicWidth()+", getIntrinsicHeight()="+getDrawable().getIntrinsicHeight());
//                setImageMatrix(getDrawMatrix());
                if(checkMatrixBounds()) {
                    setImageMatrix(getDrawMatrix());
                }

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                //图片宽高显示小于控件宽高回弹
                RectF rect = getDisplayRect(getDrawMatrix());
                if(rect.width() < viewWidth && rect.height() < viewHeight){
                    float scaleFactor = Math.min(viewWidth/rect.width(), viewHeight/rect.height());
                    mSuppMatrix.postScale(scaleFactor, scaleFactor, viewWidth/2, viewHeight/2);
                    setImageMatrix(getDrawMatrix());
                }
            }
        });
        scaleGestureDetector.setQuickScaleEnabled(false);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                RectF rect = getDisplayRect(getDrawMatrix());
                float scaleFactor = Math.min(viewWidth/rect.width(), viewHeight/rect.height());
                if(scaleFactor > 0.9){
                    mSuppMatrix.postScale(2, 2, viewWidth/2, viewHeight/2);
                    setImageMatrix(getDrawMatrix());
                }else {
                    mSuppMatrix.postScale(scaleFactor, scaleFactor, viewWidth/2, viewHeight/2);
                    if (checkMatrixBounds()) setImageMatrix(getDrawMatrix());
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.e(TAG, "+++++++++++onScroll distanceX="+distanceX+",distanceY="+distanceY);
                //dis<0为向右或下滑动
                RectF rect = getDisplayRect(getDrawMatrix());
                if(rect.width() > viewWidth || rect.height() > viewHeight){
                    mSuppMatrix.postTranslate(distanceX, 0);
                    RectF rect2 = getDisplayRect(getDrawMatrix());//RectF(-729.8274, -782.89996, 812.1499, 1958.3931)
                    Log.e(TAG, "+++++++++++onScroll handle rect2="+rect2.toString());
                    if (distanceX > 0){
                        if(rect2.left >=0){
                            setImageMatrix(getDrawMatrix());
                        }else if(rect2.left < 0){
                            mSuppMatrix.postTranslate(-rect2.left, 0);
                            setImageMatrix(getDrawMatrix());
                        }
                    }
//                    else {
//                        if(rect2.right >=0){
//                            setImageMatrix(getDrawMatrix());
//                        }else if(rect2.left < 0){
//                            mSuppMatrix.postTranslate(-rect2.left, 0);
//                            setImageMatrix(getDrawMatrix());
//                        }
//                    }
                    return true;
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean handled = false;
                scaleGestureDetector.onTouchEvent(event);
                handled = scaleGestureDetector.isInProgress();
//                Log.e(TAG, "onTouch scaleGestureDetector isInProgress="+handled);
                if (handled) return true;
                handled = gestureDetector.onTouchEvent(event);
//                Log.e(TAG, "onTouch gestureDetector.onTouchEvent="+handled);
                if(handled) return true;
                return true;
            }
        });
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) return;
        drawableWidth = drawable.getIntrinsicWidth();
        drawableHeight = drawable.getIntrinsicHeight();

        viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        viewHeight = getHeight() - getPaddingLeft() - getPaddingRight();
        RectF mTempScr = new RectF(0, 0, drawableWidth, drawableHeight);
        RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);
        mBaseMatrix.setRectToRect(mTempScr, mTempDst, Matrix.ScaleToFit.CENTER);
        mDrawableMatrix.set(mBaseMatrix);
//        float[] values = new float[] { 1.0f, 0, 200, 0, 1.0f, 100, 0, 0, 1.0f };
//        mDrawableMatrix.setValues(values);
        setImageMatrix(mDrawableMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("eeeeee", "fee");
        int a = 10;
    }

    private boolean checkMatrixBounds() {
        RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }

        Log.e(TAG,"checkMatrixBounds rect:"+rect.toString()+",rect.width:"+rect.width()+",rect.height:"+rect.height()+",viewWidth:"+viewWidth+",viewHeight:"+viewHeight);
        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;

        if (height <= viewHeight) {
            deltaY = (viewHeight - height) / 2 - rect.top;
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }


        if (width <= viewWidth) {
            deltaX = (viewWidth - width) / 2 - rect.left;
        } else if (rect.left > 0) {
//            Log.v("dyp","1111");
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
//            Log.v("dyp","2222");
        }

        Log.e(TAG,"checkMatrixBounds deltaX:"+deltaX+",deltaY:"+deltaY);
        mSuppMatrix.postTranslate(deltaX, deltaY);

        return true;
    }


    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = getDrawable();
        if (d != null) {
            Log.e(TAG, "getDisplayRect Drawable, getIntrinsicWidth="+d.getIntrinsicWidth()+", getIntrinsicHeight()="+d.getIntrinsicHeight());
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            Log.e(TAG, "getDisplayRect mDisplayRect="+mDisplayRect.toString());
            return mDisplayRect;
        }
        return null;
    }

    private Matrix getDrawMatrix() {
        mDrawableMatrix.set(mBaseMatrix);
        mDrawableMatrix.postConcat(mSuppMatrix);
        return mDrawableMatrix;
    }
}
