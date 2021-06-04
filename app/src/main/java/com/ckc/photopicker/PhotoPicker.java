package com.ckc.photopicker;

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

    public void start(){

    }

    public static class Builder{
        int mode = MODE_CAMERA;  //模式：相机(默认)、相册
        int maxSelectNum;   //最大选择数量
        int cropWidthRatio; //剪裁宽度比例
        int cropHeightRatio;//剪裁高度比例
        int maxSize;        //最大图片大小，KB

        public PhotoPicker build(){
            return new PhotoPicker(this);
        }
    }
}
