package com.ckc.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *     time   : 2021/5/31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PhotoPicker {

    public static final int MODE_CAMERA = 0;
    public static final int MODE_ALBUM = 1;

    Builder builder;

    private PhotoPicker(Builder builder) {
        this.builder = builder;
    }

    public void start(Activity activity, int reqCode){
        Intent intent = new Intent(activity, AlbumActivity.class);
        intent.putExtra("builder", builder);
        activity.startActivityForResult(intent, reqCode);
    }

    public static class Builder implements Parcelable {
        int mode = MODE_CAMERA;  //模式：相机(默认)、相册
        int maxSelectNum;   //最大选择数量
        int cropWidthRatio; //剪裁宽度比例
        int cropHeightRatio;//剪裁高度比例
        int compressMaxSize;//压缩后最大尺寸,px。按比例压缩像素尺寸，大边的像素大小不超过该值。
        int maxSize;        //最大图片大小，KB

        public Builder(){}

        private Builder(Parcel in) {
            mode = in.readInt();
            maxSelectNum = in.readInt();
            cropWidthRatio = in.readInt();
            cropHeightRatio = in.readInt();
            maxSize = in.readInt();
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

        public PhotoPicker build(){
            return new PhotoPicker(this);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mode);
            dest.writeInt(maxSelectNum);
            dest.writeInt(cropWidthRatio);
            dest.writeInt(cropHeightRatio);
            dest.writeInt(maxSize);
        }
    }
}
