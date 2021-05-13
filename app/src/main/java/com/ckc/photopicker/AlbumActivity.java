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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AlbumActivity extends AppCompatActivity {

    private LinearLayout llContainer, llTitleBar, llSwitchAlbum;
    private ImageView ivArrow;
    private Button btComplete;
    private PopupWindow popupWindow;
    private RecyclerView rvAlbumContent;
    private AlbumContentAdapter albumContentAdapter;
    private AlbumListAdapter albumListAdapter;
    private List<PhotoFolder> photoFolders;
    private SelectionCollector selectionCollector = new SelectionCollector(9);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_picker_activity_album);
        rvAlbumContent = findViewById(R.id.photo_picker_rv_album_content);
        llContainer = findViewById(R.id.photo_picker_ll_container);
        llTitleBar = findViewById(R.id.photo_picker_ll_title_bar);
        btComplete = findViewById(R.id.photo_picker_bt_complete);
        ivArrow = findViewById(R.id.photo_picker_iv_arrow);
        llSwitchAlbum = findViewById(R.id.photo_picker_ll_switch_album);
        llSwitchAlbum.setOnClickListener(v -> {
            if (photoFolders == null) return;

            showAlbumList();
        });

        albumContentAdapter = new AlbumContentAdapter(selectionCollector);
        rvAlbumContent.setLayoutManager(new GridLayoutManager(this, 4));
        rvAlbumContent.getItemAnimator().setChangeDuration(0);
        rvAlbumContent.addItemDecoration(new GridDividerItemDecoration(this.getApplicationContext(), 5f));
        rvAlbumContent.setAdapter(albumContentAdapter);
        albumContentAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object object) {
            }
        });

        albumListAdapter = new AlbumListAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object object) {
                PhotoFolder photoFolder = (PhotoFolder) object;
                albumContentAdapter.setData(photoFolder.getPhotos());
                showAlbumList();
            }
        });

        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                List<PhotoFolder> photoFolders = null;
                try {
                    photoFolders = Data.getPhotoFolders(AlbumActivity.this.getApplicationContext());
                } catch (Exception e) {
                    Log.e("fef", "获取数据异常：");
                    e.printStackTrace();
                }
                List<PhotoFolder> finalPhotoFolders = photoFolders;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlbumActivity.this.photoFolders = finalPhotoFolders;
                        albumListAdapter.setData(finalPhotoFolders);
                        albumContentAdapter.setData(finalPhotoFolders.get(0).getPhotos());
                    }
                });
            }
        });

        selectionCollector.setOnSelectChangeListener(new SelectionCollector.OnSelectChangeListener() {
            @Override
            public void onSelectChange(int maxSelectableNum, int selectedNum) {
                btComplete.setText(getString(R.string.photo_picker_complete_i1_i2, selectedNum, maxSelectableNum));
                btComplete.setEnabled(selectedNum > 0);
            }
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
            RecyclerView rv = contentView.findViewById(R.id.photo_picker_rv_album_list);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(albumListAdapter);
            albumListAdapter.setData(photoFolders);

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

    private interface OnItemClickListener{
        void onItemClick(int pos, Object object);
    }

    private static class AlbumListAdapter extends RecyclerView.Adapter<AlbumListViewHolder>{

        private List<PhotoFolder> data = new ArrayList<>();
        private int currentSelectedPos = 0, lastSelectedPos = -1;
        private OnItemClickListener onItemClickListener;

        public AlbumListAdapter(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @NonNull
        @Override
        public AlbumListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AlbumListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_picker_item_album_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumListViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext())
                    .load(data.get(position).getCover().getUri())
                    .optionalCenterCrop()
                    .into(holder.ivCover);

             holder.tvName.setText(data.get(position).getName());
             holder.tvNum.setText("("+data.get(position).getPhotos().size()+")");

             holder.ivSelected.setVisibility(position == currentSelectedPos ? View.VISIBLE:View.INVISIBLE);
             if (position == lastSelectedPos){
                 holder.ivSelected.setVisibility(View.INVISIBLE);
             }

             holder.itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if (position == currentSelectedPos) return;
                     setCurrentSelectedPos(position);
                     onItemClickListener.onItemClick(position, data.get(position));
                 }
             });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setCurrentSelectedPos(int currentSelectedPos) {
            if (currentSelectedPos == this.currentSelectedPos) return;
            lastSelectedPos = this.currentSelectedPos;
            this.currentSelectedPos = currentSelectedPos;
            notifyItemChanged(lastSelectedPos);
            notifyItemChanged(currentSelectedPos);
        }

        public void setData(List<PhotoFolder> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    private static class AlbumListViewHolder extends RecyclerView.ViewHolder{

        ImageView ivCover, ivSelected;
        TextView tvName, tvNum;

        public AlbumListViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.photo_picker_iv_photo);
            ivSelected = itemView.findViewById(R.id.photo_picker_iv_selected);
            tvName = itemView.findViewById(R.id.photo_picker_tv_name);
            tvNum = itemView.findViewById(R.id.photo_picker_tv_num);
        }
    }

    private static class AlbumContentAdapter extends RecyclerView.Adapter<AlbumContentViewHolder>{

        private List<Photo> data = new ArrayList<>();
        private OnItemClickListener onItemClickListener;
        private OnItemClickListener onSelectionClickListener;
        private SelectionCollector selectionCollector;

        public AlbumContentAdapter(SelectionCollector selectionCollector) {
            this.selectionCollector = selectionCollector;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void setOnSelectionClickListener(OnItemClickListener onSelectionClickListener) {
            this.onSelectionClickListener = onSelectionClickListener;
        }

        @NonNull
        @Override
        public AlbumContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AlbumContentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_picker_item_album_content, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumContentViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext())
                    .load(data.get(position).getUri())
                    .optionalCenterCrop()
                    .into(holder.ivPhoto);

            int index = selectionCollector.selectedIndex(data.get(position));
            if (index > 0){
                holder.tvSelectionIndicator.setSelected(true);
                holder.tvSelectionIndicator.setText(String.valueOf(index));
                holder.vSelectedShadow.setVisibility(View.VISIBLE);
            }else {
                holder.tvSelectionIndicator.setSelected(false);
                holder.tvSelectionIndicator.setText(null);
                holder.vSelectedShadow.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(v->{
                onItemClickListener.onItemClick(position, data.get(position));
            });
            holder.rlSelection.setOnClickListener(v->{
                int selectResult = selectionCollector.select(data.get(position));
                if (selectResult < 0){
                    notifyDataSetChanged();
                }else if (selectResult == 0){
                    Toast.makeText(holder.itemView.getContext(), "最多选择9张", Toast.LENGTH_SHORT).show();
                }else {
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<Photo> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    private static class AlbumContentViewHolder extends RecyclerView.ViewHolder{

        ImageView ivPhoto;
        RelativeLayout rlSelection;
        TextView tvSelectionIndicator;
        View vSelectedShadow;

        public AlbumContentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.photo_picker_iv_photo);
            rlSelection = itemView.findViewById(R.id.photo_picker_rl_selection);
            tvSelectionIndicator = itemView.findViewById(R.id.photo_picker_tv_selection_indicator);
            vSelectedShadow = itemView.findViewById(R.id.photo_picker_v_selected_shadow);
        }
    }

}
