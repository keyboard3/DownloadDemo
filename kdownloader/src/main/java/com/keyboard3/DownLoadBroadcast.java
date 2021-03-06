package com.keyboard3;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by keyboard3 on 2017/11/23.
 */

public class DownLoadBroadcast extends BroadcastReceiver {
    private static String TAG = "DownLoadBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        long downloadId = PreferencesUtils.getLong(context, "downloadId");
        long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        LogUtil.i(TAG, "通知");
        switch (intent.getAction()) {
            case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                if (downloadId == downId && downId != -1 && downloadManager != null) {
                    String fileName = "";
                    DownloadManager.Query myDownloadQuery = new DownloadManager.Query();
                    myDownloadQuery.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(myDownloadQuery);
                    if (cursor.moveToFirst()) {
                        int fileUriId = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        fileName = cursor.getString(fileUriId);
                    }
                    cursor.close();
                    fileName = fileName.replaceAll("file:///", "");
                    LogUtil.i(TAG, "广播监听下载完成，APK存储路径为 ：" + fileName);

                    boolean install = PreferencesUtils.getBoolean(context, downId + "", true);
                    LogUtil.d(TAG, "是否安装：" + install);
                    if (install) {
                        APPUtil.installApk(context, fileName);
                        DownloadService.clearRecord(context, downloadId);
                    }
                }
                break;
            default:
                break;
        }
    }
}