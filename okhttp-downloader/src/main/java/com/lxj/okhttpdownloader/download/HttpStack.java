package com.lxj.okhttpdownloader.download;

import okhttp3.Headers;

import java.io.InputStream;

/**
 * Created by dance on 2017/3/26.
 * HttpStack for Download!
 */

public interface HttpStack {

    InputStream download(String downloadUrl, Headers headers);

    void close();

    long getContentLength();
}
