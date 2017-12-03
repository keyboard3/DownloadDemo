package com.keyboard3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.github.keyboard3.download.R;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private static final String TAG = "FileDownloadActivity";
    private DownloadInfo mDownloadInfo;
    private DialogInfo mDialogInfo;
    private Call mCall;
    private ProgressDialog mProDialog;
    private int REQUEST_CODE = 808;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_down);

        String action = getIntent().getStringExtra(KDownloader.BUNDLE_KEY_ACTION);
        mDownloadInfo = (DownloadInfo) getIntent().getSerializableExtra(KDownloader.BUNDLE_KEY_DOWNLOAD);
        mDialogInfo = (DialogInfo) getIntent().getSerializableExtra(KDownloader.BUNDLE_KEY_DIALOG);
        switch (action) {
            case KDownloader.ACTION_UPGRADE:
                upgrade();
                break;
            case KDownloader.ACTION_EXIST:
                apkExist();
                break;
            case KDownloader.ACTION_DOWNLOAD:
                okDownload();
            default:
                break;
        }
    }

    private void upgrade() {
        Dialog tempDialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        /**升级提示框配置**/
        if (TextUtils.isEmpty(mDialogInfo.title)) {
            mDialogInfo.title = "升级提示" + mDownloadInfo.versionName;
        }
        builder.setTitle(mDialogInfo.title);
        if (TextUtils.isEmpty(mDialogInfo.message)) {
            mDialogInfo.message = "暂无更新内容";
        }
        if (TextUtils.isEmpty(mDialogInfo.positiveText)) {
            mDialogInfo.positiveText = "升级";
        }
        builder.setMessage(mDialogInfo.message);
        builder.setPositiveButton(mDialogInfo.positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(FileDownloadActivity.this, DownloadService.class);
                intent.putExtra(KDownloader.BUNDLE_KEY_DOWNLOAD, mDownloadInfo);
                startService(intent);
                dialog.dismiss();
                finish();
            }
        });

        if (mDialogInfo.forceShow) {
            tempDialog = builder.create();
            tempDialog.setCancelable(false);
            tempDialog.setCanceledOnTouchOutside(false);
        } else {
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.dismiss();
                }
            });
            tempDialog = builder.create();
        }
        /**权限检查*/
        final Dialog dialog = tempDialog;
        AndPermission.with(builder.getContext())
                .requestCode(REQUEST_CODE)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        if (requestCode == REQUEST_CODE) {
                            dialog.show();
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        Toast.makeText(FileDownloadActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                    }
                }).start();
    }

    private void apkExist() {
        if (mDownloadInfo == null) {
            LogUtil.d(TAG, "mDownloadInfo null");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("检测到" + mDownloadInfo.apkName + "已经存在是否直接打开！")
                .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        APPUtil.installApk(getApplicationContext(), mDownloadInfo.apkUrl);
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("直接下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(FileDownloadActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
                        File file = new File(mDownloadInfo.apkUrl);
                        file.delete();
                        DownloadService.dispatchDownload(getApplicationContext(),
                                mDownloadInfo);
                        dialog.dismiss();
                        finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        builder.show();
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
        mProDialog.setTitle(mDownloadInfo.apkName);
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
        String url = mDownloadInfo.url;

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
                        APPUtil.installApk(FileDownloadActivity.this, mDownloadInfo.apkUrl);
                    }
                });

                try {
                    BufferedSource source = responseBody.source();

                    File outFile = new File(mDownloadInfo.apkUrl);
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
