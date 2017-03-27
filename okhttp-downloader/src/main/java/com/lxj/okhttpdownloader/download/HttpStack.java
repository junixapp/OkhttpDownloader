package com.lxj.okhttpdownloader.download;

import java.io.InputStream;

/**
 * Created by dance on 2017/3/26.
 * HttpStack for Download!
 */

public interface HttpStack {

    InputStream download(String downloadUrl);

    void close();

    long getContentLength();
}
