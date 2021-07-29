package com.ckc.photopicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.WorkerThread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * <pre>
 *     time   : 2021/7/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CompressUtils {

    @WorkerThread
    public static void compress(Context context, List<Photo> photos, int maxSize, int maxQuality){
        for (Photo p :
                photos) {
            if (maxSize > 0){
                compressBySampling(context, p, maxSize);
            }
            if (maxQuality > 0){
                compressByQuality(context, p, maxQuality, 1);
            }
        }
    }

    private static boolean isUseQStrategy(Context context, String filePath){
        String appExternalDir = context.getExternalCacheDir().getAbsolutePath();
        appExternalDir = appExternalDir.substring(0, appExternalDir.lastIndexOf("/"));
        String appInternalDir = context.getCacheDir().getAbsolutePath();
        appInternalDir = appInternalDir.substring(0, appInternalDir.lastIndexOf("/"));
        Log.e("fffff", "appExternalDir="+appExternalDir+",appInternalDir="+appInternalDir+",filePath="+filePath);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !filePath.startsWith(appExternalDir) && !filePath.startsWith(appInternalDir)){
            return true;
        }
        return false;
    }

    @WorkerThread
    public static void compressBySampling(Context context, Photo photo, int maxSize){
        String fp;
        if (isUseQStrategy(context, photo.getFilePath())){
            // 配置压缩的参数
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; //获取当前图片的边界大小，而不是将整张图片载入在内存中，避免内存溢出
            try {
                BitmapFactory.decodeStream(context.getContentResolver().openInputStream(photo.getUri()), null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            options.inSampleSize = calculateSampleSize(options, maxSize);
            if (options.inSampleSize == 1) return;
            options.inJustDecodeBounds = false;//设置为false，否则BitmapFactory.decodeFile返回null
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(photo.getUri()), null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            fp = context.getExternalCacheDir().getAbsolutePath() + "/compressBySampling_"+System.currentTimeMillis()+".jpg";
            write(fp, bitmap2bytes(bitmap));
        }else {
            // 配置压缩的参数
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; //获取当前图片的边界大小，而不是将整张图片载入在内存中，避免内存溢出
            BitmapFactory.decodeFile(photo.getFilePath(), options);

            options.inSampleSize = calculateSampleSize(options, maxSize);
            if (options.inSampleSize == 1) return;
            options.inJustDecodeBounds = false;//设置为false，否则BitmapFactory.decodeFile返回null
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getFilePath(), options); // 解码文件

            fp = context.getExternalCacheDir().getAbsolutePath() + "/compressBySampling_"+System.currentTimeMillis()+".jpg";
            write(fp, bitmap2bytes(bitmap));
        }

        photo.setFilePath(fp);
        photo.setUri(Uri.fromFile(new File(fp)));
        Log.e("ffffff", "compressBySampling成功");
    }

    @WorkerThread
    public static void compressByQuality(Context context, Photo photo, int maxQuality, int currentTimes) {
        double size;
        if (isUseQStrategy(context, photo.getFilePath())){
            size = FileSizeUtil.getFileOrFilesSize(context, photo.getUri(), FileSizeUtil.SIZETYPE_KB);
        }else{
            size = FileSizeUtil.getFileOrFilesSize(photo.getFilePath(), FileSizeUtil.SIZETYPE_KB);
        }
        Log.e("fd", "图片大小kb："+size);
        if (size <= maxQuality) {
            Log.e("ffffff", "compressByQuality取消压缩：size="+size);
            return;
        }

        Bitmap photoBitmap = null;
        try {
            //裁剪后的图像转成BitMap
            //photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriClipUri));
            photoBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photo.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path = context.getExternalCacheDir().getAbsolutePath();
        File file = new File(path);
        file.mkdirs();
        file = new File(file.toString() + "/compressByQuality_" + System.currentTimeMillis() + ".jpg");
        Log.e("fileNew", file.getPath());
        //创建输出流
        OutputStream out = null;
        try {
            out = new FileOutputStream(file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //压缩文件，返回结果，参数分别是压缩的格式，压缩质量的百分比，输出流
        boolean bCompress = photoBitmap.compress(Bitmap.CompressFormat.WEBP, 50, out);
        Log.e("压缩成功？", ""+bCompress);

        photo.setFilePath(file.getAbsolutePath());
        photo.setUri(Uri.fromFile(file));
        Log.e("ffffff", "第"+currentTimes+"次compressByQuality成功");

        if (currentTimes == 5) {
            Log.e("ffffff", "达到压缩质量次数上限，停止compressByQuality");
            return;
        }
        compressByQuality(context, photo, maxQuality, currentTimes+1);
    }

    private static byte[] bitmap2bytes(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);
        return outputStream.toByteArray();
    }

    private static void write(String filePath, byte[] bytes) {
        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fout = null;
        try {
            // true表示追加写入
            fout = new FileOutputStream(file, false);
            fout.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 计算出所需要压缩的大小
     * @param options
     * @param maxSize  我们期望的图片的最大边最大值，单位px
     * @return
     */
    private static int calculateSampleSize(BitmapFactory.Options options, int maxSize) {
        int sampleSize = 1;
        int picWidth = options.outWidth;
        int picHeight = options.outHeight;
        int imageMaxSize = picWidth > picHeight ? picWidth:picHeight;
        Log.e("fffff", "imageMaxSize="+imageMaxSize+",maxSize="+maxSize);
        if (imageMaxSize > maxSize) {
            while (imageMaxSize / sampleSize > maxSize) {
                sampleSize *= 2;
            }
        }
        Log.e("fffff", "sampleSize="+sampleSize);
        return sampleSize;
    }

    /**
     * 此方式尺寸缩小与采样法相同倍数，清晰度小于采样压缩法，并且所得大小大于采样法，所以不建议使用、
     * */
    @Deprecated
    private static Uri compressBySize(Context context, Uri imageUri) {
        Bitmap bitmap = null;
        try {
            //裁剪后的图像转成BitMap
            //photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriClipUri));
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //设置缩放比
        int radio = 8;
        Log.e("fffffffff", "w="+bitmap.getWidth()+",h="+bitmap.getHeight());
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth() / radio, bitmap.getHeight() / radio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        RectF rectF = new RectF(0, 0, bitmap.getWidth() / radio, bitmap.getHeight() / radio);
        //将原图画在缩放之后的矩形上
        canvas.drawBitmap(bitmap, null, rectF, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        String fp = context.getExternalCacheDir().getAbsolutePath() + "/compress-"+System.currentTimeMillis()+".jpg";
        File file = new File(fp);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }


}
