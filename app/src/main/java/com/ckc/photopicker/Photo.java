package com.ckc.photopicker;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *     time   : 2021/5/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Photo implements Parcelable {

    private String name;        //名称
    private String mimeType;    //种类，如image/jpeg
    private long size;          //大小
    private int width;          //像素宽
    private int height;         //像素高
    private long addTime;       //添加时间
    private String filePath;    //文件路径
    private Uri uri;            //统一资源标识符

    public Photo(){}

    protected Photo(Parcel in) {
        name = in.readString();
        mimeType = in.readString();
        size = in.readLong();
        width = in.readInt();
        height = in.readInt();
        addTime = in.readLong();
        filePath = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mimeType);
        dest.writeLong(size);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeLong(addTime);
        dest.writeString(filePath);
        dest.writeParcelable(uri, flags);
    }
}
