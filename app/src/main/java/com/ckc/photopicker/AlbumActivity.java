package com.ckc.photopicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private LinearLayout llContainer, llTitleBar, llSwitchAlbum;
    private ImageView ivArrow;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_picker_activity_album);
        llContainer = findViewById(R.id.photo_picker_ll_container);
        llTitleBar = findViewById(R.id.photo_picker_ll_title_bar);
        ivArrow = findViewById(R.id.photo_picker_iv_arrow);
        llSwitchAlbum = findViewById(R.id.photo_picker_ll_switch_album);
        llSwitchAlbum.setOnClickListener(v -> {
            showAlbumList();
        });


    }

    private void showAlbumList() {
        //设置箭头图标的动画
        ObjectAnimator animator = ObjectAnimator.ofFloat(ivArrow, "rotation", ivArrow.getRotation(), ivArrow.getRotation() + 180);
        animator.setDuration(500);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                llSwitchAlbum.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                llSwitchAlbum.setEnabled(true);
            }
        });
        animator.start();

        if (popupWindow == null) {
            View contentView = getLayoutInflater().inflate(R.layout.photo_picker_album_list_pw, null);
            View outsideView = contentView.findViewById(R.id.photo_picker_v_outside);
            outsideView.setOnClickListener(v -> showAlbumList());

            popupWindow = new PopupWindow(contentView, llTitleBar.getWidth(), llContainer.getHeight() - llTitleBar.getHeight());
            popupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

            //添加过渡动画
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TransitionSet transitionSet = new TransitionSet();
                Slide slide = new Slide(Gravity.TOP);
                transitionSet.addTransition(slide);
                transitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
                transitionSet.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                        outsideView.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        outsideView.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onTransitionCancel(Transition transition) { }
                    @Override
                    public void onTransitionPause(Transition transition) { }
                    @Override
                    public void onTransitionResume(Transition transition) { }
                });
                popupWindow.setEnterTransition(transitionSet);
                popupWindow.setExitTransition(transitionSet);
            }
        }

        if(!popupWindow.isShowing()){
            popupWindow.showAsDropDown(llTitleBar);
        }else {
            popupWindow.dismiss();
        }
    }

    private static class AlbumContentAdapter extends RecyclerView.Adapter<AlbumContentViewHolder>{

        private List<Photo> data = new ArrayList<>();

        public AlbumContentAdapter() {
        }

        @NonNull
        @Override
        public AlbumContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AlbumContentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_picker_item_album_content, parent));
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumContentViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

//        public void setData(List<Photo> data) {
//            this.data = data;
//            notifyDataSetChanged();
//        }
    }

    private static class AlbumContentViewHolder extends RecyclerView.ViewHolder{

        ImageView ivPhoto;

        public AlbumContentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.photo_picker_iv_photo);
        }
    }
}
