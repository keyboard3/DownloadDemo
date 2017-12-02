package com.github.keyboard3.download;

import java.io.Serializable;

/**
 * @author keyboard3
 * @date 2017/11/26
 */

public class DownloadInfo implements Serializable {
    public DownloadInfo(String url, String versionName, String appName, String apkName, String apkUrl, boolean install, boolean system) {
        this.url = url;
        this.versionName = versionName;
        this.appName = appName;
        this.apkName = apkName;
        this.apkUrl = apkUrl;
        this.install = install;
        this.system = system;
    }

    public String url;
    public String versionName;
    public String appName;
    public String apkName;
    public String apkUrl;
    public boolean install;
    public boolean system;
}