package com.lxj.okhttpdownloader.download;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * 下载任务的数据封装
 * @author lxj
 *
 */
@DatabaseTable(tableName = "download")
public class DownloadInfo implements Serializable{
	@DatabaseField(id = true)
	public String taskId;//下载任务的标识，存取的时候用到，
	@DatabaseField
	public long currentLength;//已经下载的大小
	@DatabaseField
	public long size;//总大小
	@DatabaseField
	public int state;
	@DatabaseField
	public String downloadUrl;
	@DatabaseField
	public String path;//下载文件保存的路径
	
	/**
	 * 快速初始化DownloadInfo的方法
	 * @return
	 */
	public static DownloadInfo create(String taskId,String downloadUrl,String savePath){
		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.taskId = taskId;
		downloadInfo.downloadUrl = downloadUrl;
		downloadInfo.currentLength = 0L;
		downloadInfo.state = DownloadEngine.STATE_NONE;//一开始是未下载的状态
		
		downloadInfo.path = savePath;
		
		return downloadInfo;
	}

	@Override
	public String toString() {
		return "DownloadInfo{" +
				"id='" + taskId + '\'' +
				", currentLength=" + currentLength +
				", size=" + size +
				", state=" + state +
				", downloadUrl='" + downloadUrl + '\'' +
				", path='" + path + '\'' +
				'}';
	}
}
