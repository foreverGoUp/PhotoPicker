package com.ckc.photopicker;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : 陈孔财
 *     e-mail : chenkongcai@lexiangbao.com
 *     time   : 2021/5/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Data {

    public static List<PhotoFolder> getPhotoFolders(Context context) throws Exception{
        List<PhotoFolder> photoFolders = new ArrayList<>();

        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
                MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492	            MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920	            MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080	            MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg	            MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATE_ADDED,     //图片被添加的时间，long型
        };
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , projection
                , null
                , null
                , sortOrder);

        List<Photo> allPhotos = new ArrayList<>();

        while (cursor.moveToNext()) {
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

            File file = new File(imagePath);
            if (!file.exists() || file.length() <= 0) {
                continue;
            }

            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            String imageName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            long imageSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            int imageWidth = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
            int imageHeight = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
            String imageMimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
            long imageAddTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);


            //封装实体
            Photo photo = new Photo();
//            LogFileUtils.write(activity, "从设备获取的图片imageResult.imageName:"+imageName+",imagePath="+imagePath, true);
            photo.setName(imageName);
            photo.setFilePath(imagePath);
            photo.setSize(imageSize);
            photo.setWidth(imageWidth);
            photo.setHeight(imageHeight);
            photo.setMimeType(imageMimeType);
            photo.setAddTime(imageAddTime);
            photo.setUri(uri);

            allPhotos.add(photo);

            //根据父路径分类存放图片
            File parentFile = file.getParentFile();
            PhotoFolder folder = new PhotoFolder();
            folder.setName(parentFile.getName());
            folder.setPath(parentFile.getAbsolutePath());

            if (!photoFolders.contains(folder)) {
                List<Photo> photos = new ArrayList<>();
                photos.add(photo);
                folder.setCover(photo);
                folder.setPhotos(photos);
                photoFolders.add(folder);
            } else {
                photoFolders.get(photoFolders.indexOf(folder)).getPhotos().add(photo);
            }
        }

        //构造相册“所有”
        PhotoFolder albumAllFolder = new PhotoFolder();
        albumAllFolder.setName(context.getResources().getString(R.string.photo_picker_name_of_album_all));
        albumAllFolder.setPath("/");
        albumAllFolder.setCover(allPhotos.get(0));
        albumAllFolder.setPhotos(allPhotos);
        photoFolders.add(0, albumAllFolder);
        return photoFolders;
    }
}
