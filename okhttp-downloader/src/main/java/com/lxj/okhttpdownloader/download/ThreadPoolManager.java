package com.lxj.okhttpdownloader.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于线程池进行封装的类
 * @author lxj
 *
 */
public class ThreadPoolManager {
	private static ThreadPoolManager mInstance = new ThreadPoolManager();
	private ThreadPoolExecutor executor;

	int corePoolSize;
	int maximumPoolSize;
	long keepAliveTime = 2;
	TimeUnit unit = TimeUnit.HOURS;
	BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	RejectedExecutionHandler  handler = new ThreadPoolExecutor.AbortPolicy();
	
	public static ThreadPoolManager getInstance() {
		return mInstance;
	}
	private ThreadPoolManager() {
		//calculate corePoolSize, which is the same to AsyncTask.
		corePoolSize = Runtime.getRuntime().availableProcessors()*2 + 1;
		
		maximumPoolSize = corePoolSize;
		//we custom the threadpool.
		executor = new ThreadPoolExecutor(
				corePoolSize, //is 3 in avd.
				maximumPoolSize, //which is unuseless
				keepAliveTime, 
				unit, 
				workQueue, 
				Executors.defaultThreadFactory(), 
				handler
				);
	}

	public void setCorePoolSize(int size){
		this.corePoolSize = size;
	}
	
	/**
	 * 往线程池中添加任务
	 * @param runnable
	 */
	public void execute(Runnable runnable){
		if(runnable!=null){
			executor.execute(runnable);
		}
	}
	/**
	 * 从线程池中移除任务
	 * @param runnable
	 */
	public void remove(Runnable runnable){
		if(runnable!=null){
			executor.remove(runnable);
		}
	}
	
}
