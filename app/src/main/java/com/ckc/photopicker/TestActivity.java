package com.ckc.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = findViewById(R.id.iv);
        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PhotoPicker.Builder()
                        .setCompressMaxSize(0)
                        .setCompressMaxQuality(200)
                        .setMaxSelectNum(1)
                        .setEnableCrop(1)
                        .build()
                        .start(TestActivity.this, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 1){
                ArrayList<Photo> list = data.getParcelableArrayListExtra("list");
                if(list.size() > 0){
                    Log.e("fffffff", "最终图片："+list.get(0).getFilePath());
                    try {
                        imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), list.get(0).getUri()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            Toast.makeText(this, "失败了", Toast.LENGTH_SHORT).show();
        }
    }
}
