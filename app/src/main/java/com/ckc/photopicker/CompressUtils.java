package com.ckc.photopicker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <pre>
 *     time   : 2021/7/23
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CompressUtils {

    public static Uri compressByQuality(Context context, Uri uriClipUri) {
        Bitmap photoBitmap = null;
        try {
            //裁剪后的图像转成BitMap
            //photoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriClipUri));
            photoBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uriClipUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //创建路径
        String path = context.getExternalCacheDir().getAbsolutePath();
        //获取外部储存目录
        File file = new File(path);
        //创建新目录, 创建此抽象路径名指定的目录，包括创建必需但不存在的父目录。
        file.mkdirs();
        //以当前时间重新命名文件
        long i = System.currentTimeMillis();
        //生成新的文件
        file = new File(file.toString() + "/compress" + i + ".jpg");
        Log.e("fileNew", file.getPath());
        //创建输出流
        OutputStream out = null;
        try {
            out = new FileOutputStream(file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //压缩文件，返回结果，参数分别是压缩的格式，压缩质量的百分比，输出流
        boolean bCompress = photoBitmap.compress(Bitmap.CompressFormat.WEBP, 0, out);
        Log.e("压缩成功？", ""+bCompress);
//        try {
//            photoBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),Uri.fromFile(file));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return Uri.fromFile(file);
    }

    public static byte[] bitmap2bytes(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static void write(String filePath, byte[] bytes, boolean append) {
        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream fout = null;
        try {
            // true表示追加写入
            fout = new FileOutputStream(file, append);
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

    public static void saveImage(Context context, Bitmap bitmap){
//        FileInputStream inputStream = new FileInputStream(new BufferedInputStream(bi));
    }

    public static Uri compressBySampling(Context context, Uri imageUri){
        String[] filePathColumns = {MediaStore.Images.Media.DATA};
        Cursor c = context.getContentResolver().query(imageUri, filePathColumns, null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePathColumns[0]);
        String imagePath = c.getString(columnIndex);
        c.close();

        Bitmap bm;
        // 配置压缩的参数
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //获取当前图片的边界大小，而不是将整张图片载入在内存中，避免内存溢出
        BitmapFactory.decodeFile(imagePath, options);
        options.inSampleSize = calculateSampleSize(options,520);
        options.inJustDecodeBounds = false;//设置为false，否则BitmapFactory.decodeFile返回null
        bm = BitmapFactory.decodeFile(imagePath, options); // 解码文件

        String fp = context.getExternalCacheDir().getAbsolutePath() + "/compress-"+System.currentTimeMillis()+".jpg";
        write(fp, bitmap2bytes(bm), false);

        return Uri.fromFile(new File(fp));
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
