package com.lxj.okhttpdownloader.db;

import android.content.Context;


import com.j256.ormlite.dao.Dao;
import com.lxj.okhttpdownloader.download.DownloadInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by dance on 2017/3/26.
 */

public class DownloadInfoDao {
    private Context context;
    private Dao<DownloadInfo, String> downloadInfoOpe;
    private DBHelper helper;

    public DownloadInfoDao(Context context) {
        this.context = context;
        try {
            helper = DBHelper.create(context);
            downloadInfoOpe = helper.getDao(DownloadInfo.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加一个用户
     *
     * @param downloadInfo
     * @throws SQLException
     */
    public void add(DownloadInfo downloadInfo) {
        /*//事务操作
		TransactionManager.callInTransaction(helper.getConnectionSource(),
				new Callable<Void>()
				{
					@Override
					public Void call() throws Exception
					{
						return null;
					}
				});*/
        try {
            downloadInfoOpe.create(downloadInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public DownloadInfo get(String id) {
        try {
            return downloadInfoOpe.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void update(DownloadInfo downloadInfo){
        try {
            downloadInfoOpe.update(downloadInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(String id){
        try {
            downloadInfoOpe.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(DownloadInfo downloadInfo){
        try {
            downloadInfoOpe.delete(downloadInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DownloadInfo> getAllDownloadInfos(){
        try {
            return downloadInfoOpe.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
