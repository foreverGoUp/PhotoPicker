package com.ckc.photopicker;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * <pre>
 *     time   : 2021/5/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Data {

    private static Data data;

    public static Data getInstance(){
        if (data == null){
            data = new Data();
        }
        return data;
    }

    List<Photo> currentAlbumPhotos;
    SelectionCollector selectionCollector;
    private boolean isOriginImage;//是否原图

    public boolean isOriginImage() {
        return isOriginImage;
    }

    public void setOriginImage(boolean originImage) {
        isOriginImage = originImage;
    }

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
    //拍照
    public static Uri takePhoto(Activity activity, int reqCode){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        File tempFile = new File(activity.getExternalCacheDir().getAbsolutePath());
        tempFile = createTempFile(tempFile, "capture_", ".jpg");
        Uri uri;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            uri = Uri.fromFile(tempFile);
        } else {
            uri = FileProvider.getUriForFile(activity, activity.getPackageName()+".photo_picker_provider", tempFile);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(takePictureIntent, reqCode);
        return uri;
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    static File createTempFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    //裁剪图片
    static Uri cropPhoto(Activity activity, Uri uri, int reqCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(uri.getPath()));
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("circleCrop", 1);//无效，可能值不对。但即使该行注释了，打开也是圆形的剪裁风格，但实际输出方形图片。
        intent.putExtra("return-data", false);
//        intent.putExtra("scale", true);
//        intent.putExtra("scaleUpIfNeeded", true);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        File tempFile = createTempFile(new File(activity.getExternalCacheDir().getAbsolutePath()), "crop_", ".jpg");
        Uri outputUri = Uri.fromFile(tempFile);//不需要contentUri,可能是系统bug。
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        activity.startActivityForResult(intent, reqCode);
        return outputUri;
    }
}
