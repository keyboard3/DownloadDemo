package com.github.keyboard3.filedownloadmanage;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;

/**
 * 用DownloadManager来实现版本更新
 *
 * @author keyboard3
 * @date 2017-11-24 13:51:28
 */
public class DownloadService extends IntentService {
    private static final String TAG = DownloadService.class.getSimpleName();

    public static final String BUNDLE_KEY_DOWNLOAD_URL = "download_url";
    public static final String BUNDLE_KEY_VERSION_NAME = "version_name";
    public static final String BUNDLE_KEY_APP_NAME = "app_name";
    public static final String BUNDLE_KEY_APK_NAME = "apk_name";
    public static final String BUNDLE_KEY_INSTALL = "install";

    private DownloadManager downloadManager;

    private long downloadId;
    private String downloadUrl;
    private String apkDir;
    private String versionName;
    private String appName;
    private String apkName;
    private boolean install;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        downloadUrl = intent.getStringExtra(BUNDLE_KEY_DOWNLOAD_URL);
        versionName = intent.getStringExtra(BUNDLE_KEY_VERSION_NAME);
        appName = intent.getStringExtra(BUNDLE_KEY_APP_NAME);
        apkName = intent.getStringExtra(BUNDLE_KEY_APK_NAME);
        install = intent.getBooleanExtra(BUNDLE_KEY_INSTALL, true);

        LogUtil.i(TAG, "下载路径：" + downloadUrl);
        downloadApk(downloadUrl, versionName, appName);
    }

    /**
     * 下载最新APK
     */
    private void downloadApk(String url, String versionName, String appName) {
        if (TextUtils.isEmpty(apkName)) {
            apkName = APPUtil.getDefaultInstallApkName(getApplicationContext());
        }
        if (TextUtils.isEmpty(apkDir)) {
            apkDir = APPUtil.getDefaultInstallApkDir(getApplicationContext());
        }
        if (TextUtils.isEmpty(appName)) {
            appName = "测试应用";
        }
        if (TextUtils.isEmpty(versionName)) {
            versionName = "1.0";
        }
        String apkUrl = APPUtil.getDefaultInstallApkDir(getApplicationContext()) + "/" + apkName + ".apk";
        File file = new File(apkUrl);
        if (file.exists()) {
            LogUtil.i(TAG, "文件已存在删除：");
            file.delete();
        }
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        /**设置用于下载时的网络状态*/
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(appName);
        request.setDescription(versionName);
        /**设置漫游状态下是否可以下载*/
        request.setAllowedOverRoaming(false);
        /**如果我们希望下载的文件可以被系统的Downloads应用扫描到并管理，
         我们需要调用Request对象的setVisibleInDownloadsUi方法，传递参数true.*/
        request.setVisibleInDownloadsUi(true);
        /**设置文件保存路径*/
        request.setDestinationInExternalFilesDir(getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, apkName + ".apk");

        LogUtil.d(TAG, "apkUrl" + apkUrl);
        downloadId = downloadManager.enqueue(request);
        DownLoadBroadcast.downloadId = downloadId;
        PreferencesUtils.putBoolean(getApplicationContext(), downloadId + "", install);
    }
}
