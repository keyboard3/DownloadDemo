package com.keyboard3;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author keyboard3 on 2017/11/24
 */

public class KDownloader {
    private BuilderParams params;

    protected KDownloader(BuilderParams params) {
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
        intent.putExtra(DownloadService.BUNDLE_KEY_APK_NAME, params.apkName);
        intent.putExtra(DownloadService.BUNDLE_KEY_VERSION_NAME, params.versionName);
        intent.putExtra(DownloadService.BUNDLE_KEY_APK_DIR, params.apkDir);
        intent.putExtra(DownloadService.BUNDLE_KEY_INSTALL, params.install);
        intent.putExtra(DownloadService.BUNDLE_KEY_SYSTEM_DOWNLOAD, params.systemDownload);
        activity.startService(intent);
    }

    static class BuilderParams {
        public WeakReference<Context> wra;
        public String downloadUrl;
        public String apkDir;
        public String versionName;
        public String appName;
        public String apkName;
        public boolean install = true;
        public boolean systemDownload = true;
    }

    public static class Builder {
        private final BuilderParams params;
        private AlertDialog.Builder builder = null;
        private boolean forceShow = false;
        private final int REQUEST_CODE = 8080;
        private CharSequence mPositveText;
        private CharSequence mNegativeText;
        private int mPositveTextId = -1;
        private int mNegativeTextId = -1;
        private CharSequence mTitle;
        private CharSequence mMessage;

        public Builder(ContextThemeWrapper context) {
            params = new BuilderParams();
            params.wra = new WeakReference<Context>(context);
            builder = new AlertDialog.Builder(params.wra.get());
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

        public Builder setPositiveButton(@StringRes int textId) {
            mPositveTextId = textId;
            return this;
        }

        public Builder setPositiveButton(CharSequence text) {
            this.mPositveText = text;
            return this;
        }

        public Builder setNegativeButton(@StringRes int textId) {
            this.mNegativeTextId = textId;
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
            Dialog tempDialog = null;
            final KDownloader kDownloadManager = create();
            /**升级提示框配置**/
            if (mPositveText == null || mNegativeTextId == -1) {
                mPositveText = "升级";
            } else if (mNegativeTextId != -1) {
                mPositveText = params.wra.get().getResources().getText(mPositveTextId);
            }
            if (TextUtils.isEmpty(mTitle)) {
                mTitle = "升级提示";
            }
            builder.setTitle(mTitle);
            if (TextUtils.isEmpty(mMessage)) {
                mMessage = "暂无更新内容";
            }
            builder.setMessage(mMessage);
            builder.setPositiveButton(mPositveText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    kDownloadManager.go();
                    dialog.dismiss();
                }
            });

            if (forceShow) {
                tempDialog = builder.create();
                tempDialog.setCancelable(false);
                tempDialog.setCanceledOnTouchOutside(false);
            } else {
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                            Toast.makeText(builder.getContext(), "授权失败", Toast.LENGTH_SHORT).show();
                        }
                    }).start();

            return kDownloadManager;
        }

        public KDownloader start() {
            KDownloader kDownloadManager = create();
            kDownloadManager.go();
            return kDownloadManager;
        }
    }
}
