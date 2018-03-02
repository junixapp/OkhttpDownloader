package com.lxj.okhttpdownloaderdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ApkUtils {

	/**
	 * 安装apk
	 * @param apkFilePath apk所在路径
	 */
	public static void install(Context context,String apkFilePath){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//指定apk文件路径,安装apk
		intent.setDataAndType(Uri.parse("file://"+apkFilePath),"application/vnd.android.package-archive");
		context.startActivity(intent);
	}
}
