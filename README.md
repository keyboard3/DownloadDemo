# DownloadDemo
系统下载apk并自动安装

1.intentService异步启动DownlaodManager下载
2.可配置通知栏显示内容app名称和app版本号
3.可配置是否下载成功自动安装
4.支持7.0版本自动安装

```
new KDownloadManager.Builder(getApplicationContext())
        .setAppName("测试下载demo")
        .setApkName("downloadDemo")
        .setVersionName("1.0")
        //.setApkDir(**)
        //.setInstall(true)
        .setDownloadUrl("http://download.fir.im/v2/app/install/59b63f33548b7a28a000008b?download_token=36abfb0627d8ecd0ad3146c5aecf6f78&source=update")
        .start();
```
