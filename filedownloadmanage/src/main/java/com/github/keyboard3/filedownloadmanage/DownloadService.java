package com.github.keyboard3.filedownloadmanage;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
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
    public static final String BUNDLE_KEY_APK_DIR = "apk_dir";
    public static final String BUNDLE_KEY_INSTALL = "install";
    public static final String BUNDLE_KEY_SYSTEM_DOWNLOAD = "system_download";
    private String mDownloadUrl;
    private String mApkDir;
    private String mVersionName;
    private String mAppName;
    private String mApkName;
    private boolean mInstall;
    private boolean mSystemDownload;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mDownloadUrl = intent.getStringExtra(BUNDLE_KEY_DOWNLOAD_URL);
        mVersionName = intent.getStringExtra(BUNDLE_KEY_VERSION_NAME);
        mApkDir = intent.getStringExtra(BUNDLE_KEY_APK_DIR);
        mAppName = intent.getStringExtra(BUNDLE_KEY_APP_NAME);
        mApkName = intent.getStringExtra(BUNDLE_KEY_APK_NAME);
        mInstall = intent.getBooleanExtra(BUNDLE_KEY_INSTALL, true);
        mSystemDownload = intent.getBooleanExtra(BUNDLE_KEY_SYSTEM_DOWNLOAD, true);

        LogUtil.i(TAG, "下载路径：" + mDownloadUrl);
        downloadApk(mDownloadUrl, mVersionName, mAppName);
    }

    /**
     * 下载最新APK
     */
    private void downloadApk(String url, String versionName, String appName) {
        if (TextUtils.isEmpty(mApkDir)) {
            mApkDir = APPUtil.getDefaultInstallApkDir(getApplicationContext());
        }
        if (TextUtils.isEmpty(appName)) {
            appName = "测试应用";
        }
        if (TextUtils.isEmpty(versionName)) {
            versionName = "1.0";
        }
        if (TextUtils.isEmpty(mApkName)) {
            mApkName = APPUtil.getDefaultInstallApkName(getApplicationContext());
        }
        mApkName += "_" + versionName;
        String apkUrl = mApkDir + "/" + mApkName + ".apk";
        DownloadInfo info = new DownloadInfo(
                url
                , versionName
                , appName
                , mApkName
                , apkUrl
                , mInstall
                , mSystemDownload
        );
        File file = new File(apkUrl);
        if (file.exists()) {
            Intent intent = new Intent(getApplicationContext(), FileDownActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(FileDownActivity.BUNDLE_KEY_ACTION, "dialog");
            intent.putExtra(FileDownActivity.BUNDLE_KEY_MSG, "检测到" + mApkName + "已经存在是否直接打开！");
            intent.putExtra(FileDownActivity.BUNDLE_KEY_INFO, info);
            startActivity(intent);
            LogUtil.d(TAG, "检测到已经存在是否直接打开");
            return;
        }
        dispatchDownload(getApplicationContext(), info);
    }

    /**
     * 分发下载
     *
     * @param context
     * @param info
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
        if (info.system) {
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
        Intent intent = new Intent(context, FileDownActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FileDownActivity.BUNDLE_KEY_ACTION, "download");
        intent.putExtra(FileDownActivity.BUNDLE_KEY_INFO, info);
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