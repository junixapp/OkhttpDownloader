package com.lxj.okhttpdownloader.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.lxj.okhttpdownloader.db.DownloadInfoDao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 下载管理类
 * 模型：多任务多线程(一个任务一个线程)的断点下载
 *
 * @author lxj
 */
public class DownloadEngine {

    //define state var.
    public static final int STATE_NONE = 0;//未下载
    public static final int STATE_DOWNLOADING = 1;//下载中
    public static final int STATE_PAUSE = 2;//暂停
    public static final int STATE_FINISH = 3;//下载完成
    public static final int STATE_ERROR = 4;//下载出错
    public static final int STATE_WAITING = 5;//等待中

    //key:taskId   val:all observers
    private HashMap<String, ArrayList<DownloadObserver>> observerMap = new HashMap<>();

    //store DownloadInfo in memery，
    private HashMap<String, DownloadInfo> downloadInfoMap = new HashMap<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private static DownloadEngine mInstance = null;
    private DownloadInfoDao downloadInfoDao = null;

    private DownloadEngine(Context context) {
        downloadInfoDao = new DownloadInfoDao(context);
        //read downloadInfoMap
        initDownloadInfoMap();
    }

    public static DownloadEngine create(Context context) {
        if (mInstance == null) {
            synchronized (DownloadEngine.class) {
                if (mInstance == null) {
                    mInstance = new DownloadEngine(context);
                }
            }
        }
        return mInstance;
    }

    private void initDownloadInfoMap() {
        List<DownloadInfo> list = downloadInfoDao.getAllDownloadInfos();
        if (list != null) {
            for (DownloadInfo downloadInfo : list) {
                downloadInfoMap.put(downloadInfo.taskId, downloadInfo);
                L.d("db downloadinfo: " + downloadInfo.toString());
            }
        }
        L.d("init downloadinfo from db , get " + (list == null ? 0 : list.size()) + " downloadinfo!");
    }

    /**
     * 设置最大同时执行的任务数量
     *
     * @param taskCount
     */
    public void setMaxTaskCount(int taskCount) {
        ThreadPoolManager.getInstance().setCorePoolSize(taskCount);
    }

    /**
     * 获取指定任务的下载状态
     *
     * @param taskId
     * @return
     */
    public int getDownloadState(String taskId) {
        return downloadInfoMap.containsKey(taskId) ?
                downloadInfoMap.get(taskId).state : STATE_NONE;
    }

    /**
     * 下载任务的方法
     *
     * @param taskId      任务id，需要外界维护和提供
     * @param downloadUrl 完整下载地址
     * @param savePath    文件保存路径
     */
    public void download(String taskId, String downloadUrl, String savePath) {
        L.d("download called! \ntaskId: " + taskId + "\n downloadUrl: " + downloadUrl
                + " \nsavePath: " + savePath);
        DownloadInfo downloadInfo = downloadInfoMap.get(taskId);
        if (downloadInfo == null) {
            //first download.
            downloadInfo = DownloadInfo.create(taskId, downloadUrl, savePath);
            downloadInfoMap.put(taskId, downloadInfo);
        } else {
            downloadInfo.downloadUrl = downloadUrl;
        }
        //addToDb
        saveDownloadInfo(downloadInfo);

        //2.only three states allow download.
        if (downloadInfo.state == STATE_NONE || downloadInfo.state == STATE_PAUSE
                || downloadInfo.state == STATE_ERROR) {
            DownloadTask downloadTask = new DownloadTask(this, downloadInfo);

            //set state  waitting
            downloadInfo.state = STATE_WAITING;
            notifyDownloadUpdate(downloadInfo);

            ThreadPoolManager.getInstance().execute(downloadTask);
            L.d("enqueue download task into thread pool!");
        } else {
            L.d("The state of current task is " + downloadInfo.state + ",  can't be downloaded!");
        }
    }

    private void saveDownloadInfo(DownloadInfo downloadInfo) {
        downloadInfoDao.add(downloadInfo);
    }


    public void updateDownloadInfo(DownloadInfo downloadInfo) {
        downloadInfoDao.update(downloadInfo);
    }

    /**
     * 删除下载的文件，同时删除数据库记录和文件
     *
     * @param downloadInfo
     */
    public void deleteDownloadInfo(DownloadInfo downloadInfo) {
        //pause first.
        pause(downloadInfo.taskId);

        downloadInfoDao.delete(downloadInfo);
        //delete file
        new File(downloadInfo.path).delete();
    }

    public void deleteDownloadInfo(String taskId) {
        DownloadInfo downloadInfo = getDownloadInfo(taskId);
        if (downloadInfo != null) {
            deleteDownloadInfo(downloadInfo);
        }
    }

    /**
     * 通知所有的监听器下载更新
     *
     * @param downloadInfo
     */
    public void notifyDownloadUpdate(final DownloadInfo downloadInfo) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<DownloadObserver> observerList = observerMap.get(downloadInfo.taskId);
                if (observerList == null || observerList.size() == 0) return;

                for (DownloadObserver observer : observerList) {
                    observer.onDownloadUpdate(downloadInfo);
                }
            }
        });
    }

    /**
     * 暂停的方法
     */
    public void pause(String taskId) {
        DownloadInfo downloadInfo = getDownloadInfo(taskId);
        if (downloadInfo != null) {
            //直接将downloadInfo的state设置pause
            downloadInfo.state = STATE_PAUSE;
            L.d("pause task : " + taskId);
        }
    }


    /**
     * 添加一个下载监听器对象,监听某些任务
     *
     * @param observer
     */
    public void addDownloadObserver(DownloadObserver observer, String taskId) {
        if (taskId == null) return;
        ArrayList<DownloadObserver> list = observerMap.get(taskId);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(observer);
        observerMap.put(taskId, list);
        L.d("add observer successfully!");
    }

    public DownloadInfo getDownloadInfo(String taskId) {
        DownloadInfo downloadInfo = downloadInfoMap.get(taskId);
        L.d("fetch downloadInfo:  " + (downloadInfo == null ? null : downloadInfo.toString()));
        return downloadInfo;
    }

    /**
     * 移除一个下载监听器对象
     *
     * @param observer
     */
    public void removeDownloadObserver(DownloadObserver observer, String taskId) {
        if (taskId == null || observer == null) return;
        if (observerMap.containsKey(taskId)) {
            ArrayList<DownloadObserver> list = observerMap.get(taskId);
            if (list != null) {
                list.remove(observer);
                observerMap.put(taskId, list);
                L.d("remove observer successfully!");
            }
        }
    }

    public interface DownloadObserver {
        void onDownloadUpdate(DownloadInfo downloadInfo);
    }

}
