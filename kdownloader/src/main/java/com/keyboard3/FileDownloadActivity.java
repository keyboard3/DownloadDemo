package com.keyboard3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.keyboard3.download.R;

import java.io.File;
import java.io.IOException;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class FileDownloadActivity extends Activity {
    public static final String BUNDLE_KEY_ACTION = "BUNDLE_KEY_ACTION";
    public static final String BUNDLE_KEY_MSG = "BUNDLE_KEY_MSG";
    public static final String BUNDLE_KEY_INFO = "BUNDLE_KEY_INFO";
    private DownloadInfo mInfo;
    private Call mCall;
    private ProgressDialog mProDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_down);
        String action = getIntent().getStringExtra(BUNDLE_KEY_ACTION);
        mInfo = (DownloadInfo) getIntent().getSerializableExtra(BUNDLE_KEY_INFO);
        switch (action) {
            case "dialog":
                String msg = getIntent().getStringExtra(BUNDLE_KEY_MSG);
                AlertDialog show = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage(msg)
                        .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                APPUtil.installApk(getApplicationContext(), mInfo.apkUrl);
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setNegativeButton("直接下载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(FileDownloadActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
                                File file = new File(mInfo.apkUrl);
                                file.delete();
                                DownloadService.dispatchDownload(getApplicationContext(),
                                        mInfo);
                                dialog.dismiss();
                                finish();
                            }
                        }).show();
                show.setCanceledOnTouchOutside(false);
                break;
            case "download":
                okDownload();
            default:
                break;
        }
    }

    private void okDownload() {
        /**
         * 页面显示风格
         */
        //新建ProgressDialog对象
        mProDialog = new ProgressDialog(this);
        //设置显示风格
        mProDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //设置标题
        mProDialog.setTitle(mInfo.apkName);
        /**
         * 设置关于ProgressBar属性
         */
        //设置最大进度
        mProDialog.setMax(100);
        //设定初始化已经增长到的进度
        mProDialog.incrementProgressBy(0);
        //进度条是明显显示进度的
        mProDialog.setIndeterminate(false);
        mProDialog.show();


        LogUtil.d("start download");
        String url = mInfo.url;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        mCall = okHttpClient.newCall(builder.build());
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "=============onFailure===============");
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileDownloadActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
                finish();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("TAG", "=============onResponse===============");
                Log.e("TAG", "request headers:" + response.request().headers());
                Log.e("TAG", "response headers:" + response.headers());
                ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                    //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressStart(long totalBytes) {
                        super.onUIProgressStart(totalBytes);
                        Log.e("TAG", "onUIProgressStart:" + totalBytes);
                    }

                    @Override
                    public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                        Log.e("TAG", "=============start===============");
                        Log.e("TAG", "numBytes:" + numBytes);
                        Log.e("TAG", "totalBytes:" + totalBytes);
                        Log.e("TAG", "percent:" + percent);
                        Log.e("TAG", "speed:" + speed);
                        Log.e("TAG", "============= end ===============");
                        mProDialog.setProgress((int) (100 * percent));
                    }

                    //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressFinish() {
                        super.onUIProgressFinish();
                        Log.e("TAG", "onUIProgressFinish:");
                        Toast.makeText(FileDownloadActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                        mProDialog.dismiss();
                        finish();
                        APPUtil.installApk(FileDownloadActivity.this, mInfo.apkUrl);
                    }
                });

                try {
                    BufferedSource source = responseBody.source();

                    File outFile = new File(mInfo.apkUrl);
                    outFile.delete();
                    outFile.getParentFile().mkdirs();
                    outFile.createNewFile();

                    BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                    source.readAll(sink);
                    sink.flush();
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FileDownloadActivity.this, "文件异常", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCall != null) {
            mCall.cancel();
        }
        if (mProDialog != null) {
            mProDialog.dismiss();
        }
    }
}
