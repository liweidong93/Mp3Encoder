package com.pxwx.stududent.mp3recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pxwx.stududent.mp3recorder.view.AudioWindow;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonStart = findViewById(R.id.btn_main_start);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()){
                    requestPermission();
                }else{
                    AudioWindow.getInstance().show(MainActivity.this, findViewById(R.id.ll_main_root), new AudioWindow.RecorderFinishListener() {
                        @Override
                        public void fileCallBack(File file, long time) {
                            Toast.makeText(MainActivity.this, "文件存储在：" + file.getAbsolutePath() + ",录制时长为:" + time, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }



    private boolean checkPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //TODO
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //获得了权限

        } else {
            //拒绝了权限
            Toast.makeText(MainActivity.this, "拒绝权限后将不能进行录音", Toast.LENGTH_SHORT).show();
        }
    }

}
