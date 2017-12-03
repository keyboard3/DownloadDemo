package com.github.keyboard3.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.keyboard3.APPUtil;
import com.keyboard3.KDownloader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDownload = findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new KDownloader.Builder(MainActivity.this)
                        //.setDialogTitle("提示")
                        //.setDialogMessage("新版本上线")
                        //.setDialogForceShow(false)
                        .setAppName("测试下载demo")
                        .setApkName(APPUtil.getDefaultInstallApkName(getApplicationContext()))
                        .setVersionName("1.0")
                        //.setApkDir(APPUtil.getDefaultInstallApkDir(MainActivity.this))
                        //.setInstall(true)
                        //.setSystemDownload(false)
                        .setDownloadUrl("http://download.fir.im/v2/app/install/59b63f33548b7a28a000008b?download_token=36abfb0627d8ecd0ad3146c5aecf6f78&source=update")
                        .startAndDialog();
            }
        });
    }
}