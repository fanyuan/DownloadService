package com.example.downloadservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    String [] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkFilePermission();
    }
    public void download(View v){
        Dialog dialog = Utils.showLoading(this);
        TextView textView = dialog.findViewById(R.id.tip_text_view);

        String url = "https://cdn4.mydown.com/608cfbbf/370bbe089013d055e78d797a21d9eb3a/newsoft/QQBrowser_Download1100115562.exe";//"https://mmbiz.qpic.cn/mmbiz_png/liaczD18OicSzSQtKEciaWiaJgvsgfx89V5w6WEve6LNvF6HdFbsibIaaicnq7Wxic1hib0I1G1wM23w8OKG3qFj0XOTAQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1";
        String outputPath = getExternalCacheDir().getAbsolutePath() + File.separator + "QQBrowser.exe";//"image.png";
        DownloadHelper.download(this, url, outputPath, new DownloadService.DownloadCallback() {
            @Override
            public void progressNotify(int progress) {
                Utils.log(getApplicationContext(),"progressNotify --- " + progress);
                runOnUiThread(() -> {
                    Utils.log(getApplicationContext(),"123 progressNotify --- " + progress);
                    textView.setText(progress + "%");
                });
            }

            @Override
            public void finish(String filePath) {
                String msg = "文件下载成功\n" + filePath;
                Utils.log(getApplicationContext(),msg);
                runOnUiThread(() -> {
                    if(dialog != null && dialog.isShowing()){dialog.dismiss();}
                    Dialog d = Utils.showSingleButtonDialog(MainActivity.this,msg,null);
                });
            }

            @Override
            public void fail(String msg) {
                msg = "下载失败\n\n可能网络不好,或者远端文件不存在\n-----------\n" + msg;
                String finalMsg = msg;
                runOnUiThread(()->{
                    if(dialog != null && dialog.isShowing()){dialog.dismiss();}
                    Utils.showSingleButtonDialog(MainActivity.this, finalMsg,null);
                });
            }
        });
        //new Handler(Looper.getMainLooper()).postDelayed(() -> {dialog.dismiss();},5000);
    }
    private void checkFilePermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, perms, 123);
        }
        Utils.log(this,"---checkFilePermission---");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Utils.log(this,"onRequestPermissionsResult permissions = " + Arrays.toString(permissions) + "  grantResults = " + Arrays.toString(grantResults));
    }
}