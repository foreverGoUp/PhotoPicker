package com.ckc.photopicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



/**
 * @author ckc
 * 2021-05-12
 * 垂直方向的Grid布局的分割线
 * <p>
 * 适配对象：Grid布局方向为垂直的RecyclerView。
 * 功能：十字形样式分割线
 * 若期望Item四周有相同宽度的分割线，此时在布局文件的RecyclerView中添加【padding=分割线宽度】属性即可
 * 这样的实现方式是经过三思得到的最佳方案，因此该类没有打算实现回字型的分割线的功能。
 */
public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;//分割线

    /**
     * 透明的分割线
     * @param gapDp 水平垂直间距,dp值
     */
    public GridDividerItemDecoration(Context context, float gapDp) {
        this(context, gapDp, gapDp);
    }

    /**
     * 透明的分割线
     * @param horizontalGapDp 水平间距,dp值
     * @param verticalGapDp 垂直间距,dp值
     */
    public GridDividerItemDecoration(Context context, float horizontalGapDp, float verticalGapDp) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setAlpha(0);
        drawable.setIntrinsicWidth(dp2px(context, horizontalGapDp));
        drawable.setIntrinsicHeight(dp2px(context, verticalGapDp));
        mDivider = drawable;
    }

    /**
     * 适合Item四周无阴影的RecyclerView
     *
     * @param context       上下文
     * @param drawableResId 图像资源ID
     */
    public GridDividerItemDecoration(Context context, int drawableResId) {
        mDivider = context.getResources().getDrawable(drawableResId);
    }

    /**
     * 适合Item底部有阴影的RecyclerView。
     * 原理是处理最后一行Item的时候，让Item的底部多占用参数dpOfHeightOfBottomShadowOfItem的值的高度，从而让Item底部阴影能够显示出来。
     *
     * @param context                        上下文
     * @param drawableResId                  图像资源ID
     * @param dpOfHeightOfBottomShadowOfItem item底部阴影高度的dp值
     */
//    public GridDividerItemDecoration(Context context, int drawableResId, int dpOfHeightOfBottomShadowOfItem) {
//        mHeightOfBottomShadowOfItem = (int) (dpOfHeightOfBottomShadowOfItem * (context.getResources().getDisplayMetrics().density) + 0.5f);
//    }

    private static int dp2px(Context context, final float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 在绘制Item前回调，相当于画Item的背景图案。
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawVerticalDivider(c, parent);
        drawHorizontalDivider(c, parent);
    }

    /**
     * 在绘制Item后回调，相当于画Item的覆盖图案。
     */
    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    /**
     * 获得列数
     */
    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager && ((GridLayoutManager) layoutManager).getOrientation() == GridLayoutManager.VERTICAL) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else {
            throw new IllegalStateException("This item decoration is not for the RecyclerView!");
        }
        return spanCount;
    }

    /**
     * 在item底部画分割线，分隔线宽度大于item宽度一个分割线内在宽度，高度为分割线内在高度。
     */
    public void drawVerticalDivider(Canvas c, RecyclerView parent) {
        int spanCount = getSpanCount(parent);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getLeft() - params.leftMargin;
            final int right = child.getRight() + params.rightMargin
                    + mDivider.getIntrinsicWidth();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();

            if (isLastRow(parent, childCount, spanCount, i)){
                continue;
            }

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    /**
     * 在item右边画分割线，分隔线高度与item一致，宽度为分割线内在宽度。
     */
    public void drawHorizontalDivider(Canvas c, RecyclerView parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getTop() - params.topMargin;
            final int bottom = child.getBottom() + params.bottomMargin;
            final int left = child.getRight() + params.rightMargin;
            int right = left + mDivider.getIntrinsicWidth();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    /**
     * 是否是第一列
     */
    private boolean isFirstColumn(RecyclerView parent, int childCount, int spanCount, int pos) {
        if (pos % spanCount == 0) return true;
        return false;
    }

    /**
     * 是否是最后一列
     */
    private boolean isLastColumn(RecyclerView parent, int childCount, int spanCount, int pos) {
        if ((pos + 1) % spanCount == 0) return true;
        return false;
    }

    /**
     * 是否是最后一行
     */
    private boolean isLastRow(RecyclerView parent, int childCount, int spanCount, int pos) {
        if (childCount % spanCount == 0) {
            if (pos >= childCount - spanCount) return true;
        } else {
            childCount = childCount - childCount % spanCount;
            if (pos >= childCount) return true;
        }
        return false;
    }

    /**
     * 控制Item使用的区域，使分割线显示出来。
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//        int spanCount = getSpanCount(parent);
//        int childCount = parent.getAdapter().getItemCount();
//        int itemPosition = parent.getChildLayoutPosition(view);
//
//        if (isLastRow(parent, childCount, spanCount, itemPosition)){//最后一行
//            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
//        }else if (isLastColumn(parent, childCount, spanCount, itemPosition)){//最后一列
//            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
//        }else {
//            outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
//        }

        //此方案在span count=3，且item间隔设置为10时会出现问题。
//        int spanCount = getSpanCount(parent);
//        int pos = parent.getChildAdapterPosition(view);
//        int column = (pos) % spanCount+1;// 计算这个child 处于第几列
//
//        outRect.top = 0;
//        outRect.bottom = mDivider.getIntrinsicHeight();
//        //注意这里一定要先乘 后除  先除数因为小于1然后强转int后会为0
//        outRect.left = (column-1) * mDivider.getIntrinsicWidth() / spanCount; //左侧为(当前条目数-1)/总条目数*divider宽度
//        outRect.right = (spanCount-column)* mDivider.getIntrinsicWidth() / spanCount ;//右侧为(总条目数-当前条目数)/总条目数*divider宽度


        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();

        boolean isLastRow = isLastRow(parent, childCount, spanCount, itemPosition);

        int top = 0;
        int left;
        int right;
        int bottom;
        int eachWidth = (spanCount - 1) * mDivider.getIntrinsicWidth() / spanCount;
        int dl = mDivider.getIntrinsicWidth() - eachWidth;

        left = itemPosition % spanCount * dl;
        right = eachWidth - left;
        bottom = mDivider.getIntrinsicHeight();
        if (isLastRow){
            bottom = 0;
        }
        outRect.set(left, top, right, bottom);
    }
}
