package com.ckc.photopicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * <pre>
 *     author : 陈孔财
 *     e-mail : chenkongcai@lexiangbao.com
 *     time   : 2020/8/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RectRelativeLayout extends RelativeLayout {

    public RectRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
