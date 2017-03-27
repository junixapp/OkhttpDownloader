package com.lxj.okhttpdownloader.download;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
    @Override
    public InputStream download(String downloadUrl) {
        Request request = new Request.Builder()
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
