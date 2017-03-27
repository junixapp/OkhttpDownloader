package com.lxj.okhttpdownloader.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.lxj.okhttpdownloader.db.DownloadInfoDao;

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

    public static final int STATE_NONE = 0;//未下载
    public static final int STATE_DOWNLOADING = 1;//下载中
    public static final int STATE_PAUSE = 2;//暂停
    public static final int STATE_FINISH = 3;//下载完成
    public static final int STATE_ERROR = 4;//下载出错
    public static final int STATE_WAITING = 5;//等待中，任务创建并添加到线程池，但是run方法没有执行

    //用来存放所有的DownloadObserver对象, key:taskId   val:all observers
    private HashMap<String, ArrayList<DownloadObserver>> observerMap = new HashMap<>();

    //store DownloadInfo in memery，
    private HashMap<String, DownloadInfo> downloadInfoMap = new HashMap<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private static DownloadEngine mInstance = null;
    private DownloadInfoDao downloadInfoDao = null;

    private DownloadEngine(Context context) {
        downloadInfoDao = new DownloadInfoDao(context);
        //从本地读取数据
        initDownloadInfo();
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

    private void initDownloadInfo() {
        List<DownloadInfo> list = downloadInfoDao.getAllDownloadInfos();
        if (list != null) {
            for (DownloadInfo downloadInfo : list) {
                downloadInfoMap.put(downloadInfo.taskId, downloadInfo);
                L.d("db downloadinfo: "+downloadInfo.toString());
            }
        }
        L.d("init downloadinfo from db , get " + (list == null ? 0 : list.size()) + " downloadinfo!");
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
     * 下载的方法
     */
    public void download(String taskId, String downloadUrl, String savePath) {
        L.d("download called! \ntaskId: " + taskId + "\n downloadUrl: " + downloadUrl
                + " \nsavePath: " + savePath);
        //先从map中取
        DownloadInfo downloadInfo = downloadInfoMap.get(taskId);
        if (downloadInfo == null) {
            //说明从来没有下载过，那么则创建downloadInfo，并存起来
            downloadInfo = DownloadInfo.create(taskId, downloadUrl, savePath);
            downloadInfoMap.put(taskId, downloadInfo);
        } else {
            downloadInfo.downloadUrl = downloadUrl;
        }
        //addToDb
        saveDownloadInfo(downloadInfo);

        //2.此时的任务有可能没有下载过，也有可能下载过一半，也有可能下载完，所以我们应该根据state来判断操作
        //只有在3种状态下才能开始下载：未下载， 暂停，下载失败，
        if (downloadInfo.state == STATE_NONE || downloadInfo.state == STATE_PAUSE
                || downloadInfo.state == STATE_ERROR) {
            //3.可以进行下载,创建下载任务，添加到线程池中
            DownloadTask downloadTask = new DownloadTask(this, downloadInfo);

            //将downloadInfo的state设置等待中
            downloadInfo.state = STATE_WAITING;
            //将状态变化通知给外界的监听器
            notifyDownloadUpdate(downloadInfo);

            //交给线程池管理
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

    public void deleteDownloadInfo(DownloadInfo downloadInfo) {
        downloadInfoDao.delete(downloadInfo);
    }

    /**
     * 通知所有的监听器状态更改了
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
    public void addDownloadObserver(DownloadObserver observer, String... taskIds) {
        if (taskIds == null || taskIds.length == 0 || observer == null) return;
        for (int i = 0; i < taskIds.length; i++) {
            String id = taskIds[i];
            ArrayList<DownloadObserver> list = observerMap.get(id);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(observer);
            observerMap.put(id, list);
            L.d("add observer successfully!");
        }
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
    public void removeDownloadObserver(DownloadObserver observer, String... taskIds) {
        if (taskIds == null || taskIds.length == 0 || observer == null) return;
        for (int i = 0; i < taskIds.length; i++) {
            String id = taskIds[i];
            if (observerMap.containsKey(id)) {
                ArrayList<DownloadObserver> list = observerMap.get(id);
                if (list != null) {
                    list.remove(observer);
                    observerMap.put(id, list);
                    L.d("remove observer successfully!");
                }
            }
        }
    }

    /**
     * 定义下载监听器，目的是暴露自己下载的状态和进度
     */
    public interface DownloadObserver {
        /**
         * 当下载状态改变，包括进度改变
         */
        void onDownloadUpdate(DownloadInfo downloadInfo);
    }

}
