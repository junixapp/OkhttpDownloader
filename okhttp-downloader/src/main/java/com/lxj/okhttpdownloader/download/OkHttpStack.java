package com.lxj.okhttpdownloader.download;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.*;

/**
 * Created by dance on 2017/3/26.
 */

public class OkHttpStack implements HttpStack {
    private OkHttpClient client;
    public OkHttpStack(){
        client = new OkHttpClient();
    }
    private InputStream is;
    private ResponseBody body;
    private long contentLength;

    @Override
    public long getContentLength(){
        return contentLength;
    }
    public InputStream download(String downloadUrl, Headers headers) {
        Request.Builder builder = new Request.Builder();
        if (headers!=null){
            builder.headers(headers);
        }
        Request request = builder
                .get()
                .url(downloadUrl)
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            if(response.isSuccessful()){
                body = response.body();
                contentLength =body.contentLength();
                is = body.byteStream();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    @Override
    public void close() {
        if(is!=null){
            try {
                body.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
