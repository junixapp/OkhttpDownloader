# OkhttpDownloader
a multi-thread downloader for okhttp!



# Dependencies

[![](https://jitpack.io/v/li-xiaojun/OkhttpDownloader.svg)](https://jitpack.io/#li-xiaojun/OkhttpDownloader)

Step 1.** Add the JitPack repository to your build file

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

**Step 2.** Add the dependency

```groovy
dependencies {
	 compile 'com.github.li-xiaojun:OkhttpDownloader:latest release'
}
```





# Feature

- multi-thread download
- support break-download
- db save downloadInfo





# How To Use

- 下载方法：

  ```java
  DownloadEngine.create(this).download(taskId, url, path);
  ```

- 暂停方法：

  ```java
  DownloadEngine.create(this).pause(taskId);
  ```

- 设置最大同时运行的任务数量：

  ```java
  DownloadEngine.create(this).setMaxTaskCount(5);
  ```

- 添加和移除下载监听器：

  ```java
  //添加
  DownloadEngine.create(this).addDownloadObserver(new DownloadEngine.DownloadObserver() {
              @Override
              public void onDownloadUpdate(DownloadInfo downloadInfo) {
              }
          });
  //移除
  DownloadEngine.create(this).removeDownloadObserver(this);
  ```

- 获取下载详细数据：

  ```java
  DownloadEngine.create(this).getDownloadInfo(taskId);
  ```

- 删除下载数据，同时删除数据库记录和本地文件：

  ```java
  DownloadEngine.create(this).deleteDownloadInfo(downloadInfo);
  ```

  ​