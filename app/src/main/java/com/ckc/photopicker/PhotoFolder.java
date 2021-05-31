package com.ckc.photopicker;

import java.util.List;

/**
 * <pre>
 *     time   : 2021/5/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PhotoFolder {


    private String name;  //当前文件夹的名字
    private String path;  //当前文件夹的路径
    private Photo cover;   //当前文件夹需要要显示的缩略图，默认为最近的一次图片
    private List<Photo> photos;  //当前文件夹下所有图片的集合

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Photo getCover() {
        return cover;
    }

    public void setCover(Photo cover) {
        this.cover = cover;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    /** 只要文件夹的路径和名字相同，就认为是相同的文件夹 */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PhotoFolder){
            PhotoFolder other = (PhotoFolder) o;
            return this.path.equals(other.path) && this.name.equals(other.name);
        }else {
            return false;
        }
    }
}
