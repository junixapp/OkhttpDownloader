# OkhttpDownloader
a multi-thread downloader for okhttp!

# Dependencies

```url
[![](https://jitpack.io/v/li-xiaojun/OkhttpDownloader.svg)](https://jitpack.io/#li-xiaojun/OkhttpDownloader)
```

**Step 1.** Add the JitPack repository to your build file

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
	        compile 'com.github.li-xiaojun:OkhttpDownloader:1.0.0'
}
```



# Feature

- multi-thread download
- support break-download
- db save 