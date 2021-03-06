package com.ckc.photopicker;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AlbumActivity extends AppCompatActivity {

    private LinearLayout llContainer, llTitleBar, llSwitchAlbum, llOriginImage;
    private ImageView ivArrow;
    private Button btComplete;
    private TextView tvPreview, tvAlbumName;
    private PopupWindow popupWindow;
    private RecyclerView rvPhotoList;
    private PhotoListAdapter photoListAdapter;
    private AlbumListAdapter albumListAdapter;
    private List<PhotoFolder> photoFolders;
    private SelectionCollector selectionCollector = new SelectionCollector(9);

    private static final int REQ_CODE_PREVIEW = 1;
    private static final int REQ_CODE_CROP = 2;

    Uri takePhotoFileUri, croppedPhotoFileUri;
    private PhotoPicker.Builder builder;
    private String croppedPhotoFilePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_picker_activity_album);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "????????????????????????", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }


        //????????????????????????
        builder = getIntent().getParcelableExtra("builder");

        tvPreview = findViewById(R.id.photo_picker_tv_preview);
        tvAlbumName = findViewById(R.id.photo_picker_tv_album_name);
        rvPhotoList = findViewById(R.id.photo_picker_rv_album_content);
        llContainer = findViewById(R.id.photo_picker_ll_container);
        llTitleBar = findViewById(R.id.photo_picker_ll_title_bar);
        llOriginImage = findViewById(R.id.photo_picker_ll_origin_image);
        btComplete = findViewById(R.id.photo_picker_bt_complete);
        ivArrow = findViewById(R.id.photo_picker_iv_arrow);
        llSwitchAlbum = findViewById(R.id.photo_picker_ll_switch_album);
        llSwitchAlbum.setOnClickListener(v -> {
            if (photoFolders == null) return;

            showAlbumList();
        });

        tvPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Data.getInstance().currentAlbumPhotos = new ArrayList<>();
//                Data.getInstance().currentAlbumPhotos.addAll(selectionCollector.selectedItems);
//                PreviewActivity.start(AlbumActivity.this, 0, REQ_CODE_PREVIEW);

//                takePhotoFileUri = Data.takePhoto(AlbumActivity.this, 2);

            }
        });
        llOriginImage.setOnClickListener(v -> {
            Data.getInstance().setOriginImage(!Data.getInstance().isOriginImage());
            llOriginImage.setSelected(Data.getInstance().isOriginImage());
        });

        photoListAdapter = new PhotoListAdapter(selectionCollector);
        rvPhotoList.setLayoutManager(new GridLayoutManager(this, 4));
        rvPhotoList.getItemAnimator().setChangeDuration(0);
        rvPhotoList.addItemDecoration(new GridDividerItemDecoration(this.getApplicationContext(), 5f));
        rvPhotoList.setAdapter(photoListAdapter);
        photoListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object object) {
                Data.getInstance().currentAlbumPhotos = photoListAdapter.data;
                PreviewActivity.start(AlbumActivity.this, pos, REQ_CODE_PREVIEW);
            }
        });

        albumListAdapter = new AlbumListAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object object) {
                PhotoFolder photoFolder = (PhotoFolder) object;
                tvAlbumName.setText(photoFolder.getName());
                photoListAdapter.setData(photoFolder.getPhotos());
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
                    Log.e("fef", "?????????????????????");
                    e.printStackTrace();
                }
                List<PhotoFolder> finalPhotoFolders = photoFolders;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlbumActivity.this.photoFolders = finalPhotoFolders;
                        tvAlbumName.setText(finalPhotoFolders.get(0).getName());
                        albumListAdapter.setData(finalPhotoFolders);
                        photoListAdapter.setData(finalPhotoFolders.get(0).getPhotos());
                    }
                });
            }
        });

        selectionCollector.setOnSelectChangeListener(onSelectChangeListener);
        Data.getInstance().selectionCollector = selectionCollector;

        //????????????
        btComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectionCollector.selectedItems.size() == 0){
                    Toast.makeText(AlbumActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                //??????
                if (builder.enableCrop == 1 && builder.maxSelectNum == 1){
                    String path = getExternalCacheDir().getAbsolutePath() + "/crop" + System.currentTimeMillis() + ".jpg";
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);

                    UCrop.Options options = new UCrop.Options();
                    options.setCircleDimmedLayer(true);
                    options.setCompressionFormat(Bitmap.CompressFormat.WEBP);
                    options.setCompressionQuality(100);
                    UCrop.of(selectionCollector.selectedItems.get(0).getUri(), uri)
                            .withAspectRatio(builder.cropWidthRatio, builder.cropHeightRatio)
                            .withOptions(options)
                            .start(AlbumActivity.this, REQ_CODE_CROP);
                    croppedPhotoFilePath = path;
                    croppedPhotoFileUri = uri;
                    return;
                }
                onCompressAndComplete();
            }
        });
    }

    private void onCompressAndComplete(){
        if (!Data.getInstance().isOriginImage()){
            AppExecutors.getInstance().getDiskExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    CompressUtils.compress(AlbumActivity.this, selectionCollector.selectedItems, builder.compressMaxSize, builder.compressMaxQuality);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onComplete();
                        }
                    });
                }
            });
        }else {
            onComplete();
        }
    }

    private void onComplete(){
        Intent intent = new Intent();
        ArrayList<Photo> data = new ArrayList<>();
        data.addAll(selectionCollector.selectedItems);
        intent.putParcelableArrayListExtra("list", data);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private SelectionCollector.OnSelectChangeListener onSelectChangeListener = new SelectionCollector.OnSelectChangeListener() {
        @Override
        public void onSelectChange(int maxSelectableNum, int selectedNum) {
            btComplete.setText(getString(R.string.photo_picker_complete_i1_i2, selectedNum, maxSelectableNum));
            btComplete.setEnabled(selectedNum > 0);

            tvPreview.setText(getString(R.string.photo_picker_preview_i, selectedNum));
            tvPreview.setEnabled(selectedNum > 0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PREVIEW){
            selectionCollector.setOnSelectChangeListener(onSelectChangeListener);
            photoListAdapter.notifyDataSetChanged();
            selectionCollector.notifySelectChanged();

            llOriginImage.setSelected(Data.getInstance().isOriginImage());
        }
        if (requestCode == REQ_CODE_CROP){
//            Data.cropPhoto(AlbumActivity.this, takePhotoFileUri, 3);
            if (resultCode == Activity.RESULT_OK){
                selectionCollector.selectedItems.get(0).setFilePath(croppedPhotoFilePath);
                selectionCollector.selectedItems.get(0).setUri(croppedPhotoFileUri);
                onCompressAndComplete();
            }
        }
    }

    private void showAlbumList() {
        //???????????????????????????
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

            //??????????????????
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

    private static class PhotoListAdapter extends RecyclerView.Adapter<PhotoListViewHolder>{

        private List<Photo> data = new ArrayList<>();
        private OnItemClickListener onItemClickListener;
        private OnItemClickListener onSelectionClickListener;
        private SelectionCollector selectionCollector;

        public PhotoListAdapter(SelectionCollector selectionCollector) {
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
        public PhotoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PhotoListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_picker_item_album_content, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoListViewHolder holder, int position) {
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
                    Toast.makeText(holder.itemView.getContext(), "????????????9???", Toast.LENGTH_SHORT).show();
                }else {
                    notifyItemChanged(position);
                }
                Log.e("ffff", data.get(position).getUri().getPath()+"|"+data.get(position).getUri().getEncodedPath());
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

    private static class PhotoListViewHolder extends RecyclerView.ViewHolder{

        ImageView ivPhoto;
        RelativeLayout rlSelection;
        TextView tvSelectionIndicator;
        View vSelectedShadow;

        public PhotoListViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.photo_picker_iv_photo);
            rlSelection = itemView.findViewById(R.id.photo_picker_rl_selection);
            tvSelectionIndicator = itemView.findViewById(R.id.photo_picker_tv_selection_indicator);
            vSelectedShadow = itemView.findViewById(R.id.photo_picker_v_selected_shadow);
        }
    }

}
