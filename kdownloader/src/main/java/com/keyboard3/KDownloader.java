package com.keyboard3;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import java.lang.ref.WeakReference;

/**
 * @author keyboard3 on 2017/11/24
 */

public class KDownloader {
    protected static final String BUNDLE_KEY_DOWNLOAD = "key_download";
    protected static final String BUNDLE_KEY_DIALOG = "key_dialog";
    protected static final String BUNDLE_KEY_ACTION = "key_action";
    protected static final String ACTION_EXIST = "exist";
    protected static final String ACTION_DOWNLOAD = "download";
    protected static final String ACTION_UPGRADE = "upgrade";
    private BuilderParams params;

    protected KDownloader(BuilderParams params) {
        this.params = params;
    }

    protected void go() {
        if (params == null || params.wra.get() == null) {
            return;
        }
        Context context = params.wra.get();

        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.url = params.url;
        downloadInfo.appName = params.appName;
        downloadInfo.apkName = params.apkName;
        downloadInfo.versionName = params.versionName;
        downloadInfo.apkDir = params.apkDir;
        downloadInfo.install = params.install;
        downloadInfo.systemDownload = params.systemDownload;

        if (params.downloadTarget) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(BUNDLE_KEY_DOWNLOAD, downloadInfo);
            context.startService(intent);
        } else {
            DialogInfo dialogInfo = new DialogInfo();
            dialogInfo.title = params.title;
            dialogInfo.message = params.message;
            dialogInfo.negativeText = params.negativeText;
            dialogInfo.positiveText = params.positiveText;
            dialogInfo.forceShow = params.forceShow;

            Intent intent = new Intent(context, FileDownloadActivity.class);
            intent.putExtra(BUNDLE_KEY_DOWNLOAD, downloadInfo);
            intent.putExtra(BUNDLE_KEY_DIALOG, dialogInfo);
            intent.putExtra(BUNDLE_KEY_ACTION, ACTION_UPGRADE);
            context.startActivity(intent);
        }
    }

    static class BuilderParams {
        public WeakReference<Context> wra;
        public String title;
        public String message;
        public String positiveText;
        public String negativeText;
        public boolean forceShow;
        public boolean downloadTarget = true;
        public String url;
        public String apkDir;
        public String versionName;
        public String appName;
        public String apkName;
        public boolean install = true;
        public boolean systemDownload = true;
    }

    public static class Builder {
        private BuilderParams params;
        private AlertDialog.Builder builder = null;
        private boolean forceShow = false;
        private final int REQUEST_CODE = 8080;
        private CharSequence mPositiveText;
        private CharSequence mNegativeText;
        private CharSequence mTitle;
        private CharSequence mMessage;

        public Builder(Context context) {
            params = new BuilderParams();
            params.wra = new WeakReference<Context>(context);
        }

        /**
         * Dialog配置部分
         */
        public Builder setDialogTitle(@Nullable CharSequence title) {
            mTitle = title;
            return this;
        }

        public Builder setDialogMessage(@Nullable CharSequence message) {
            mMessage = message;
            return this;
        }

        public Builder setPositiveButton(CharSequence text) {
            this.mPositiveText = text;
            return this;
        }

        public Builder setNegativeButton(CharSequence text) {
            this.mNegativeText = text;
            return this;
        }

        public Builder setDialogForceShow(boolean forceShow) {
            this.forceShow = forceShow;
            return this;
        }

        /**
         * 下载配置部分
         */
        public Builder setDownloadUrl(String url) {
            params.url = url;
            return this;
        }

        public Builder setApkDir(String apkDir) {
            params.apkDir = apkDir;
            return this;
        }

        public Builder setInstall(boolean isInstall) {
            params.install = isInstall;
            return this;
        }

        public Builder setSystemDownload(boolean systemDownload) {
            params.systemDownload = systemDownload;
            return this;
        }

        public Builder setVersionName(String versionName) {
            params.versionName = versionName;
            return this;
        }

        public Builder setAppName(String appName) {
            params.appName = appName;
            return this;
        }

        public Builder setApkName(String apkName) {
            params.apkName = apkName;
            return this;
        }

        private KDownloader create() {
            return new KDownloader(params);
        }

        public KDownloader startAndDialog() {
            params.downloadTarget = false;
            return start();
        }

        public KDownloader start() {
            KDownloader kDownloadManager = create();
            kDownloadManager.go();
            return kDownloadManager;
        }
    }
}
