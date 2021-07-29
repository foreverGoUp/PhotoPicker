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
        /**
         * 启用裁剪。1：是，0：否。
         *
         * 生效条件：拍照或只选择一张图片
         * */
        int enableCrop;
        int cropWidthRatio = 1; //剪裁宽度比例
        int cropHeightRatio = 1;//剪裁高度比例
        /**
         * 压缩后最大尺寸,px。
         *
         * 当选择相册图片时未选中原图时，在图片返回前会按照该配置进行压缩。
         * 按比例压缩像素尺寸，大边的像素大小不超过该值。
         * */
        int compressMaxSize = 500;
        /**
         * 压缩后最大图片大小，KB。
         *
         * 当选择相册图片时未选中原图时，在图片返回前会按照该配置进行压缩。
         * */
        int compressMaxQuality = 100;

        public Builder setMode(int mode) {
            this.mode = mode;
            return this;
        }

        public Builder setMaxSelectNum(int maxSelectNum) {
            this.maxSelectNum = maxSelectNum;
            return this;
        }

        public Builder setEnableCrop(int enableCrop) {
            this.enableCrop = enableCrop;
            return this;
        }

        public Builder setCropWidthRatio(int cropWidthRatio) {
            this.cropWidthRatio = cropWidthRatio;
            return this;
        }

        public Builder setCropHeightRatio(int cropHeightRatio) {
            this.cropHeightRatio = cropHeightRatio;
            return this;
        }

        public Builder setCompressMaxSize(int compressMaxSize) {
            this.compressMaxSize = compressMaxSize;
            return this;
        }

        public Builder setCompressMaxQuality(int compressMaxQuality) {
            this.compressMaxQuality = compressMaxQuality;
            return this;
        }

        public Builder(){}

        private Builder(Parcel in) {
            mode = in.readInt();
            maxSelectNum = in.readInt();
            enableCrop = in.readInt();
            cropWidthRatio = in.readInt();
            cropHeightRatio = in.readInt();
            compressMaxSize = in.readInt();
            compressMaxQuality = in.readInt();
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
            dest.writeInt(enableCrop);
            dest.writeInt(cropWidthRatio);
            dest.writeInt(cropHeightRatio);
            dest.writeInt(compressMaxSize);
            dest.writeInt(compressMaxQuality);
        }
    }
}
