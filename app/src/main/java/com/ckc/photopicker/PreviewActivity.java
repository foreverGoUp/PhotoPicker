package com.ckc.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : 陈孔财
 *     e-mail : chenkongcai@lexiangbao.com
 *     time   : 2021/5/26
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PreviewActivity extends AppCompatActivity {

    RecyclerView rvPhotoList;
    TextView tvPhotoIndex;
    Button btComplete;
    LinearLayout llSelect;

    private PhotoListAdapter photoListAdapter;
    private SelectionCollector selectionCollector;
    private int currentPhotoPos, photoNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_picker_activity_preview);
        rvPhotoList = findViewById(R.id.photo_picker_rv_photo_list);
        tvPhotoIndex = findViewById(R.id.photo_picker_tv_photo_index);
        llSelect = findViewById(R.id.photo_picker_ll_select);
        btComplete = findViewById(R.id.photo_picker_bt_complete);

        findViewById(R.id.photo_picker_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        currentPhotoPos = getIntent().getIntExtra("currentPhotoPos", 0);

        selectionCollector = Data.getInstance().selectionCollector;
        selectionCollector.setOnSelectChangeListener(new SelectionCollector.OnSelectChangeListener() {
            @Override
            public void onSelectChange(int maxSelectableNum, int selectedNum) {
                btComplete.setText(getString(R.string.photo_picker_complete_i1_i2, selectedNum, maxSelectableNum));
                btComplete.setEnabled(selectedNum > 0);
            }
        });

        btComplete.setText(getString(R.string.photo_picker_complete_i1_i2, selectionCollector.getSelectedNum(), selectionCollector.getMaxSelectableNum()));
        btComplete.setEnabled(selectionCollector.getSelectedNum() > 0);

        photoListAdapter = new PhotoListAdapter(selectionCollector, currentPhotoPos);
        rvPhotoList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPhotoList.getItemAnimator().setChangeDuration(0);
        rvPhotoList.setAdapter(photoListAdapter);
        photoListAdapter.setData(Data.getInstance().currentAlbumPhotos);
        photoListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos, Object object) {
                currentPhotoPos = pos;
                Photo photo = (Photo) object;
                int index = selectionCollector.selectedIndex(photo);
                llSelect.setSelected(index > 0);

                tvPhotoIndex.setText((currentPhotoPos+1) + "/" + photoNum);
            }
        });
        rvPhotoList.scrollToPosition(currentPhotoPos);

        photoNum = photoListAdapter.data.size();
        tvPhotoIndex.setText((currentPhotoPos+1) + "/" + photoNum);

        int index = selectionCollector.selectedIndex(photoListAdapter.data.get(currentPhotoPos));
        llSelect.setSelected(index > 0);

        llSelect.setOnClickListener(v -> {
            int selectResult = selectionCollector.select(photoListAdapter.data.get(currentPhotoPos));
            if (selectResult == 0){
                Toast.makeText(PreviewActivity.this, "最多选择9张", Toast.LENGTH_SHORT).show();
            }else {
                photoListAdapter.notifyItemChanged(currentPhotoPos);
                llSelect.setSelected(selectResult > 0);
            }
        });
    }

    private static class PhotoListAdapter extends RecyclerView.Adapter<PreviewActivity.PhotoListViewHolder> {

        private List<Photo> data = new ArrayList<>();
        private OnItemClickListener onItemClickListener;
        private SelectionCollector selectionCollector;
        private int lastCurrentPhotoPos = -1, currentPhotoPos;

        public PhotoListAdapter(SelectionCollector selectionCollector, int currentPhotoPos) {
            this.currentPhotoPos = currentPhotoPos;
            this.selectionCollector = selectionCollector;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @NonNull
        @Override
        public PreviewActivity.PhotoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PreviewActivity.PhotoListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_picker_item_preview_photo_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull PreviewActivity.PhotoListViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext())
                    .load(data.get(position).getUri())
                    .optionalCenterCrop()
                    .into(holder.ivPhoto);

            if (currentPhotoPos == position) {
                holder.vCurrentPhotoTag.setVisibility(View.VISIBLE);
            }else {
                holder.vCurrentPhotoTag.setVisibility(View.INVISIBLE);
            }

            int index = selectionCollector.selectedIndex(data.get(position));
            holder.vSelectedShadow.setVisibility(index > 0?View.VISIBLE:View.INVISIBLE);

            holder.itemView.setOnClickListener(v -> {
                lastCurrentPhotoPos = currentPhotoPos;
                currentPhotoPos = position;
                notifyItemChanged(lastCurrentPhotoPos);
                notifyItemChanged(currentPhotoPos);
                onItemClickListener.onItemClick(position, data.get(position));
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

    private static class PhotoListViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        View vCurrentPhotoTag;
        View vSelectedShadow;

        public PhotoListViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.photo_picker_iv_photo);
            vCurrentPhotoTag = itemView.findViewById(R.id.photo_picker_v_current_photo_tag);
            vSelectedShadow = itemView.findViewById(R.id.photo_picker_v_selected_shadow);
        }
    }

    public static void start(Activity activity, int currentPhotoPos, int reqCode){
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra("currentPhotoPos", currentPhotoPos);
        activity.startActivityForResult(intent, reqCode);
    }
}
