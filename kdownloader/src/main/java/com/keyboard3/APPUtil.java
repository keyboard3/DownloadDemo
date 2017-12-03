package com.keyboard3;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.util.List;


/**
 * 有关APP的信息包括版本号，版本名，签名，安装路径等
 *
 * @author keyboard3 on 2017-11-24 13:57:21
 */
public class APPUtil {

    private APPUtil() {
    }

    /**
     * 获取应用包名
     *
     * @param context 上下文 上下文信息
     * @return 包名
     */
    public static String getPackageName(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Should not be null");
        }
        return context.getPackageName();
    }

    /**
     * @param context 上下文 上下文信息
     * @return 获取包信息
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        /** getPackageName()是当前类的包名，0代表获取版本信息 */
        try {
            return packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 判断应用是否已经启动
     *
     * @param context 上下文 一个context
     * @return boolean
     */
    public static boolean isAppAlive(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(getPackageName(context))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取应用版本号
     *
     * @param context 上下文
     * @return 成功返回版本号，失败返回-1
     */
    public static int getVersionCode(Context context) {
        if (getPackageInfo(context) != null) {
            return getPackageInfo(context).versionCode;
        }

        return -1;
    }

    /**
     * 获取应用版本名
     *
     * @param context 上下文
     * @return 成功返回版本名， 失败返回null
     */
    public static String getVersionName(Context context) {
        if (getPackageInfo(context) != null) {
            return getPackageInfo(context).versionName;
        }

        return null;
    }

    /**
     * 获取APP名称
     *
     * @param context 上下文
     * @return 返回APP名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 安装APK
     *
     * @param context 上下文
     * @param apkPath 安装包的路径
     */
    public static void installApk(Context context, String apkPath) {
        File file = new File(apkPath);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri,
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 获取默认安装目录
     *
     * @param context 上下文
     * @return 返回默认安装目录
     */
    public static String getDefaultInstallApkDir(Context context) {
        return String.valueOf(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
    }

    /**
     * 获取默认的安装包的名字
     *
     * @param context 上下文
     * @return 返回默认的安装包的名字
     */
    public static String getDefaultInstallApkName(Context context) {
        return context.getPackageName().substring(context.getPackageName().lastIndexOf(".") + 1);
    }
}
