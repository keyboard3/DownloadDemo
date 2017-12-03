package com.keyboard3;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.Serializable;

/**
 * 用DownloadManager来实现版本更新
 *
 * @author keyboard3 on 2017-11-24 13:51:28
 */
public class DownloadService extends IntentService {
    private static final String TAG = DownloadService.class.getSimpleName();

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DownloadInfo info = (DownloadInfo) intent.getSerializableExtra(KDownloader.BUNDLE_KEY_DOWNLOAD);
        if (info == null) {
            LogUtil.i(TAG, "info null");
            return;
        }
        LogUtil.i(TAG, "data:" + info.toString());
        downloadApk(info);
    }

    /**
     * 下载最新APK
     */
    private void downloadApk(DownloadInfo downloadInfo) {
        if (TextUtils.isEmpty(downloadInfo.apkDir)) {
            downloadInfo.apkDir = APPUtil.getDefaultInstallApkDir(getApplicationContext());
        }
        if (TextUtils.isEmpty(downloadInfo.appName)) {
            downloadInfo.appName = "测试应用";
        }
        if (TextUtils.isEmpty(downloadInfo.versionName)) {
            downloadInfo.versionName = "1.0";
        }
        if (TextUtils.isEmpty(downloadInfo.apkName)) {
            downloadInfo.apkName = APPUtil.getDefaultInstallApkName(getApplicationContext());
        }
        downloadInfo.apkName += "_" + downloadInfo.versionName;
        downloadInfo.apkUrl = downloadInfo.apkDir + "/" + downloadInfo.apkName + ".apk";
        File file = new File(downloadInfo.apkUrl);
        if (file.exists()) {
            Intent intent = new Intent(getApplicationContext(), FileDownloadActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(KDownloader.BUNDLE_KEY_ACTION, KDownloader.ACTION_EXIST);
            intent.putExtra(KDownloader.BUNDLE_KEY_DOWNLOAD, downloadInfo);
            startActivity(intent);
            LogUtil.d(TAG, "检测到已经存在是否直接打开");
            return;
        }
        dispatchDownload(getApplicationContext(), downloadInfo);
    }

    /**
     * 分发下载
     *
     * @param context 上下文
     * @param info    下载参数
     */
    public static void dispatchDownload(Context context, DownloadInfo info) {
        long downloadId = 0;
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(info.url));
        /**设置用于下载时的网络状态*/
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(info.appName);
        request.setDescription(info.versionName);
        /**设置漫游状态下是否可以下载*/
        request.setAllowedOverRoaming(false);
        /**如果我们希望下载的文件可以被系统的Downloads应用扫描到并管理，
         我们需要调用Request对象的setVisibleInDownloadsUi方法，传递参数true.*/
        request.setVisibleInDownloadsUi(true);
        /**设置文件保存路径*/
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, info.apkName + ".apk");

        LogUtil.d(TAG, "apkUrl" + info.apkUrl);
        if (info.systemDownload) {
            try {
                downloadId = downloadManager.enqueue(request);
            } catch (Exception e) {
                LogUtil.d(TAG, "系统下载被禁用，采用OKHttp下载");
                okDownload(context, info);
                return;
            }
        } else {
            okDownload(context, info);
            return;
        }
        saveRecord(context, info.appName, downloadId, info.install);
    }

    private static void okDownload(Context context, DownloadInfo info) {
        Intent intent = new Intent(context, FileDownloadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KDownloader.BUNDLE_KEY_ACTION, KDownloader.ACTION_DOWNLOAD);
        intent.putExtra(KDownloader.BUNDLE_KEY_DOWNLOAD, info);
        context.startActivity(intent);
    }

    public static void saveRecord(Context context, String appName, long downloadId, boolean install) {
        PreferencesUtils.putBoolean(context, downloadId + "", install);
        /**将记录当前下载downloadId*/
        PreferencesUtils.putLong(context, "downloadId", downloadId);
    }

    public static void clearRecord(Context context, long downloadId) {
        /**将记录当前下载的appName和downloadId都清空*/
        PreferencesUtils.putLong(context, "downloadId", 0);
    }
}