package com.keyboard3;

import java.io.Serializable;

/**
 * @author keyboard3 on 2017/11/26
 */

public class DownloadInfo implements Serializable {
    public String url;
    public String versionName;
    public String appName;
    public String apkName;
    public String apkDir;
    public String apkUrl;
    public boolean install;
    public boolean systemDownload;

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "url='" + url + '\'' +
                ", versionName='" + versionName + '\'' +
                ", appName='" + appName + '\'' +
                ", apkName='" + apkName + '\'' +
                ", apkDir='" + apkDir + '\'' +
                ", apkUrl='" + apkUrl + '\'' +
                ", install=" + install +
                ", systemDownload=" + systemDownload +
                '}';
    }
}