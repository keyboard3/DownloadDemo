package com.github.keyboard3.filedownloadmanage;

import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

/**
 * @author keyboard3
 * @date 2017/11/24
 */

public class KDownloadManager {
    private final BuilderParams params;

    protected KDownloadManager(BuilderParams params) {
        this.params = params;
    }

    protected void go() {
        if (params == null || params.wra.get() == null) {
            return;
        }
        Context activity = params.wra.get();
        Intent intent = new Intent(activity, DownloadService.class);
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_URL, params.downloadUrl);
        intent.putExtra(DownloadService.BUNDLE_KEY_APP_NAME, params.appName);
        intent.putExtra(DownloadService.BUNDLE_KEY_VERSION_NAME, params.versionName);
        intent.putExtra(DownloadService.BUNDLE_KEY_APK_NAME, params.apkName);
        intent.putExtra(DownloadService.BUNDLE_KEY_INSTALL, params.install);
        activity.startService(intent);
    }

    static class BuilderParams {
        public WeakReference<Context> wra;
        public String downloadUrl;
        public String apkDir;
        public String versionName;
        public String appName;
        public String apkName;
        public boolean install;
    }

    public static class Builder {
        private final BuilderParams params;

        public Builder(Context context) {
            params = new BuilderParams();
            params.wra = new WeakReference<Context>(context);
        }

        public Builder setDownloadUrl(String url) {
            params.downloadUrl = url;
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

        private KDownloadManager create() {
            return new KDownloadManager(params);
        }

        public KDownloadManager start() {
            KDownloadManager kDownloadManager = create();
            kDownloadManager.go();
            return kDownloadManager;
        }
    }
}
