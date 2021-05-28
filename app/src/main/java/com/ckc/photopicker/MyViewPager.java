package com.ckc.photopicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * <pre>
 *     author : 陈孔财
 *     e-mail : chenkongcai@lexiangbao.com
 *     time   : 2021/5/28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MyViewPager extends ViewPager {

    GestureDetector gestureDetector;

    public MyViewPager(@NonNull Context context) {
        super(context);
    }

    public MyViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if((Math.abs(distanceX) > Math.abs(distanceY)) && (Math.abs(distanceX) > 5)){
                    return true;
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });

//        setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
////                if (event.getAction() == MotionEvent.ACTION_DOWN){
////                    return false;
////                }
////                return false;
//                return gestureDetector.onTouchEvent(event);
//            }
//        });

    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Log.e("11111111111111", "dispatchTouchEvent ev getAction="+ev.getAction());
//        return true;
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.e("11111111111111", "onInterceptTouchEvent ev getAction="+ev.getAction());
//        getParent().requestDisallowInterceptTouchEvent(true);
//        return false;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        Log.e("11111111111111", "onTouchEvent ev getAction="+ev.getAction());
//        return false;
////        if (ev.getAction() == MotionEvent.ACTION_DOWN){
////            return false;
////        }
////        return super.onTouchEvent(ev);
//    }
}
