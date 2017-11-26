package com.github.keyboard3.filedownloadmanage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

public class FileDownActivity extends Activity {
    public static final String BUNDLE_KEY_ACTION = "BUNDLE_KEY_ACTION";
    public static final String BUNDLE_KEY_MSG = "BUNDLE_KEY_MSG";
    public static final String BUNDLE_KEY_INFO = "BUNDLE_KEY_INFO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_down);
        String action = getIntent().getStringExtra(BUNDLE_KEY_ACTION);
        switch (action) {
            case "dialog":
                String msg = getIntent().getStringExtra(BUNDLE_KEY_MSG);
                final DownloadInfo info = (DownloadInfo) getIntent().getSerializableExtra(BUNDLE_KEY_INFO);
                new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage(msg)
                        .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                APPUtil.installApk(getApplicationContext(), info.apkUrl);
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setNegativeButton("直接下载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(FileDownActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
                                File file = new File(info.apkUrl);
                                file.delete();
                                DownloadService.download(getApplicationContext()
                                        , info.url
                                        , info.versionName
                                        , info.appName
                                        , info.apkName
                                        , info.apkUrl
                                        , info.install);
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                break;
            default:
                break;
        }
    }

}
