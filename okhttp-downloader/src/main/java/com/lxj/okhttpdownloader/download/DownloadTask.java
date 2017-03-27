package com.lxj.okhttpdownloader.download;

/**
 * Created by dance on 2017/3/26.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 下载任务类
 * @author lxj
 *
 */
class DownloadTask implements Runnable{
    private DownloadEngine engine;
    private DownloadInfo downloadInfo;
    HttpStack httpStack = null;
    public DownloadTask(DownloadEngine engine,DownloadInfo downloadInfo) {
        super();
        this.engine = engine;
        this.downloadInfo = downloadInfo;

        httpStack = new OkHttpStack();
    }

    @Override
    public void run() {
        L.d("start downloading...");
        //4.只要run方法一执行，那么久将状态设置下载中
        downloadInfo.state = DownloadEngine.STATE_DOWNLOADING;
        //状态更改，就要通知外界
        engine.notifyDownloadUpdate(downloadInfo);

        //5.进行下载，下载分为2种：a.从头下载    b.断点下载
        File file = new File(downloadInfo.path);
        if(!file.exists() || file.length()!=downloadInfo.currentLength) {
            //从头下载或者下载有误
            file.delete();//删除无效文件
            downloadInfo.currentLength = 0;//清空currentLength
        }
        InputStream is = httpStack.download(downloadInfo.downloadUrl);
        downloadInfo.size = httpStack.getContentLength();

        //6.对httpResult进行处理
        if(is!=null){
            //说明请求文件成功，可以进行读写了
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, true);
                byte[] buffer = new byte[1024*8];//8k的缓冲区
                int len = -1;
                while(downloadInfo.state==DownloadEngine.STATE_DOWNLOADING && downloadInfo.currentLength<downloadInfo.size
                      &&downloadInfo.size>0 && (len=is.read(buffer))!=-1){
                    fos.write(buffer, 0, len);
                    //更新下载进度
                    downloadInfo.currentLength += len;
                    //通知外界监听器进度更新
                    engine.notifyDownloadUpdate(downloadInfo);

                    engine.updateDownloadInfo(downloadInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
                processErrerState();
            } finally{
                httpStack.close();
                try {
                    if(fos!=null)fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //7.走到这里有2种情况：a.下载完成  b.暂停
                if(file.length()==downloadInfo.size
                        && downloadInfo.currentLength==downloadInfo.size
                        && downloadInfo.state==DownloadEngine.STATE_DOWNLOADING){
                    //说明下载完成了
                    downloadInfo.state = DownloadEngine.STATE_FINISH;
                }
                engine.notifyDownloadUpdate(downloadInfo);
                engine.updateDownloadInfo(downloadInfo);
                L.d("download task is over: "+downloadInfo.toString());
            }
        }else {
            //说明请求失败，
            processErrerState();
        }
    }

    /**
     * 处理下载失败的状态
     */
    public void processErrerState() {
        downloadInfo.state = DownloadEngine.STATE_ERROR;
        engine.notifyDownloadUpdate(downloadInfo);
    }

}
