package com.github.keyboard3.downloaddemo;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.keyboard3.filedownloadmanage.APPUtil;
import com.github.keyboard3.filedownloadmanage.KDownloadManager;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndPermission.with(getApplicationContext())
                        .requestCode(100)
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .callback(new PermissionListener() {
                            @Override
                            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                if (requestCode == 100) {
                                    new KDownloadManager.Builder(getApplicationContext())
                                            .setAppName("测试下载demo")
                                            .setApkName("downloadDemo")
                                            .setVersionName("1.0")
                                            .setApkDir(APPUtil.getDefaultInstallApkDir(MainActivity.this))
                                            .setInstall(true)
                                            .setSystemDownload(false)
                                            .setDownloadUrl("http://download.fir.im/v2/app/install/59b63f33548b7a28a000008b?download_token=36abfb0627d8ecd0ad3146c5aecf6f78&source=update")
                                            .start();
                                }
                            }

                            @Override
                            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                            }
                        }).start();
            }
        });
    }
}