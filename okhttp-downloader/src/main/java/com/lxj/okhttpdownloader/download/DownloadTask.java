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
        //set state downloading.
        downloadInfo.state = DownloadEngine.STATE_DOWNLOADING;
        engine.notifyDownloadUpdate(downloadInfo);

        //consider 2 cases: normal download and break download
        File file = new File(downloadInfo.path);
        if(!file.exists() || file.length()!=downloadInfo.currentLength) {
            file.delete();//delete invalid file
            downloadInfo.currentLength = 0;//reset currentLength
        }

        //request file from url
        InputStream is = httpStack.download(downloadInfo.downloadUrl);
        downloadInfo.size = httpStack.getContentLength();

        //6.process io
        if(is!=null){
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, true);
                byte[] buffer = new byte[1024*8];
                int len = -1;
                while(downloadInfo.state==DownloadEngine.STATE_DOWNLOADING && downloadInfo.currentLength<downloadInfo.size
                      &&downloadInfo.size>0 && (len=is.read(buffer))!=-1){
                    fos.write(buffer, 0, len);

                    downloadInfo.currentLength += len;

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
                //when code come to here, there are 2 cases: download finised and pause
                if(file.length()==downloadInfo.size
                        && downloadInfo.currentLength==downloadInfo.size
                        && downloadInfo.state==DownloadEngine.STATE_DOWNLOADING){
                    downloadInfo.state = DownloadEngine.STATE_FINISH;
                }
                engine.notifyDownloadUpdate(downloadInfo);
                engine.updateDownloadInfo(downloadInfo);
                L.d("download task is over: "+downloadInfo.toString());
            }
        }else {
            processErrerState();
        }
    }

    /**
     * process error state
     */
    public void processErrerState() {
        downloadInfo.state = DownloadEngine.STATE_ERROR;
        engine.notifyDownloadUpdate(downloadInfo);
    }

}
