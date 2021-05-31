package com.ckc.photopicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     time   : 2021/5/28
 *     desc   : 浏览大图翻页ViewPager适配器
 *     version: 1.0
 * </pre>
 */
public class ViewBigImagePagerAdapter extends PagerAdapter {

    List<Photo> data;
    List<GestureImageView> gestureImageViews = new ArrayList<>(10);

    public ViewBigImagePagerAdapter(Context context, List<Photo> data) {
        this.data = data;
        for (int i = 0; i < 10; i++) {
            gestureImageViews.add(new GestureImageView(context));
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
//        Log.e("22222222222", "instantiateItem position="+position);
        ViewPager viewPager = (ViewPager) container;
        GestureImageView view = gestureImageViews.get(0);
        gestureImageViews.remove(0);
        gestureImageViews.add(view);

        Glide.with(container.getContext())
                .load(data.get(position).getUri())
                .into(view);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ViewGroup parent = (ViewGroup) view.getParent();
        if(parent != null) parent.removeView(view);

        viewPager.addView(view, layoutParams);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.destroyItem(container, position, object);
        container.removeView((View) object);
    }
}
