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
	TimeUnit unit = TimeUnit.HOURS;//时间单位
	BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
	//如果任务数量超过maximumPoolSize，那么我是拒绝执行的
	RejectedExecutionHandler  handler = new ThreadPoolExecutor.AbortPolicy();
	
	public static ThreadPoolManager getInstance() {
		return mInstance;
	}
	private ThreadPoolManager() {
		//1.使用java封装好的线程
		//计算corePoolSize的算法:设备的可用处理器核心数*2 + 1，能够让cpu的效率得到最大发挥
		corePoolSize = Runtime.getRuntime().availableProcessors()*2 + 1;
		
		maximumPoolSize = corePoolSize;
		//2.使用可以自定义的线程池
		executor = new ThreadPoolExecutor(
				corePoolSize, //3
				maximumPoolSize, //5
				keepAliveTime, 
				unit, 
				workQueue, 
				Executors.defaultThreadFactory(), 
				handler
				);
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
