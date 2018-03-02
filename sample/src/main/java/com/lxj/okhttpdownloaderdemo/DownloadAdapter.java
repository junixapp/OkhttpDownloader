package com.lxj.okhttpdownloaderdemo;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lxj.okhttpdownloader.download.DownloadEngine;
import com.lxj.okhttpdownloader.download.DownloadInfo;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dance on 2017/3/27.
 */

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadHolder> {
    ArrayList<AppInfo> list;

    public DownloadAdapter(ArrayList<AppInfo> list) {
        this.list = list;
    }

    @Override
    public DownloadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_download, parent, false);
        return new DownloadHolder(view);
    }

    @Override
    public void onBindViewHolder(DownloadHolder holder, int position) {
        AppInfo appInfo = list.get(position);
        holder.bindData(appInfo);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class DownloadHolder extends RecyclerView.ViewHolder implements DownloadEngine.DownloadObserver{
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.iv_image)
        ImageView ivImage;
        @BindView(R.id.btn_download)
        Button btnDownload;
        @BindView(R.id.pb_progress)
        ProgressBar pbProgress;

        Context context;
        String taskId;
        String downloadDir;//下载目录
        AppInfo appInfo;
        public DownloadHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
            downloadDir = Environment.getExternalStorageDirectory() + "/" + context.getPackageName();

            File file = new File(downloadDir);
            if(!file.exists()){
                file.mkdirs();
            }
        }

        public void bindData(AppInfo appInfo){
            this.appInfo = appInfo;
            taskId = appInfo.id+"";
            tvName.setText(appInfo.name);
            Glide.with(context).load(Url.ImagePrefix+appInfo.iconUrl)
                 .placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
                 .crossFade(1000).into(ivImage);

            //注册下载监听器,监听其会监控指定taskId的任务
            DownloadEngine.create(context).addDownloadObserver(this,taskId);

            //执行初始化操作
            DownloadInfo downloadInfo = DownloadEngine.create(context).getDownloadInfo(taskId);
            if(downloadInfo!=null){
                //根据state展示文字
                updateUIByState(downloadInfo);
            }else {
                //如果是空，说明没有下载过，那么就显示默认文字
                btnDownload.setText("下载");
                pbProgress.setProgress(0);
            }

        }
        @OnClick({R.id.btn_download})
        public void onClick(View view){
            String path = downloadDir + "/" + appInfo.name + ".apk";
            //点击操作，首先获取downloadInfo
            DownloadInfo downloadInfo = DownloadEngine.create(context).getDownloadInfo(taskId);
            if(downloadInfo==null){
                //说明是第一次下载，那么直接下载即可
                String url = String.format(Url.Download,appInfo.downloadUrl);
                DownloadEngine.create(context).download(taskId, url, path);
            }else {
                //说明不是空，那就要根据state来进行下一部操作
                if(downloadInfo.state==DownloadEngine.STATE_DOWNLOADING
                        || downloadInfo.state==DownloadEngine.STATE_WAITING  ){
                    //需要暂停
                    DownloadEngine.create(context).pause(taskId);
                }else if(downloadInfo.state==DownloadEngine.STATE_PAUSE
                        || downloadInfo.state==DownloadEngine.STATE_ERROR  ){
                    //需要继续下载(断点下载)
                    String url = String.format(Url.BreakDownload, appInfo.downloadUrl, downloadInfo.currentLength);
                    DownloadEngine.create(context).download(taskId, url, path);
                }else if(downloadInfo.state==DownloadEngine.STATE_FINISH){
                    //需要安装
                    ApkUtils.install(context,downloadInfo.path);
                }
            }
        }

        /**
         * 根据downloadINfo来更UI
         * @param downloadInfo
         */
        private void updateUIByState(DownloadInfo downloadInfo) {
            switch (downloadInfo.state){
                case DownloadEngine.STATE_NONE:
                    btnDownload.setText("下载");
                    pbProgress.setProgress(0);
                    break;
                case DownloadEngine.STATE_DOWNLOADING:
                    //显示进度
                    int progress = (int) (downloadInfo.currentLength*100f/downloadInfo.size);
                    pbProgress.setProgress(progress);

                    //显示文字进度
                    btnDownload.setText(progress+"%");

                    break;
                case DownloadEngine.STATE_FINISH:
                    btnDownload.setText("安装");
                    pbProgress.setProgress(100);
                    break;
                case DownloadEngine.STATE_PAUSE:
                    btnDownload.setText("继续下载");
                    //显示进度
                    pbProgress.setProgress((int) (downloadInfo.currentLength*100f/downloadInfo.size));
                    break;
                case DownloadEngine.STATE_ERROR:
                    btnDownload.setText("失败，重下");
                    break;
                case DownloadEngine.STATE_WAITING:
                    btnDownload.setText("等待中...");
                    break;
            }
        }

        /**
         * 下载监听器回掉方法
         * @param downloadInfo
         */
        @Override
        public void onDownloadUpdate(DownloadInfo downloadInfo) {
            updateUIByState(downloadInfo);
        }

    }

}
