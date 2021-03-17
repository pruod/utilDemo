package com.ztman.camera.util.camera.thread;

import com.ztman.camera.controller.CameraController;
import com.ztman.camera.util.camera.cache.CacheUtil;
import com.ztman.camera.util.camera.pojo.CameraPojo;
import com.ztman.camera.util.camera.util.CameraPush;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Title CameraThread.java
 * @description TODO
 * @time 2019年12月16日 上午9:32:43
 * @author wuguodong
 **/
public class CameraThread {

	private final static Logger logger = LoggerFactory.getLogger(CameraThread.class);

	public static class MyRunnable implements Runnable {

		// 创建线程池
		public static ExecutorService es = Executors.newCachedThreadPool();

		private CameraPojo cameraPojo;
		private Thread nowThread;

		public MyRunnable(CameraPojo cameraPojo) {
			this.cameraPojo = cameraPojo;
		}

		// 中断线程
		public void setInterrupted() {
			nowThread.interrupt();
		}

		@Override
		public void run() {
			// 直播流
			try {
				// 获取当前线程存入缓存
				nowThread = Thread.currentThread();
				CacheUtil.STREAMMAP.put(cameraPojo.getToken(), cameraPojo);
				// 执行转流推流任务
				CameraPush push = new CameraPush(cameraPojo).from();
				if (push != null) {
					push.to().go(nowThread);
				}
				// 清除缓存
				CacheUtil.STREAMMAP.remove(cameraPojo.getToken());
				CameraController.jobMap.remove(cameraPojo.getToken());
			} catch (Exception e) {
				logger.error("当前任务： " + cameraPojo.getRtsp() + "停止...");
				CacheUtil.STREAMMAP.remove(cameraPojo.getToken());
				CameraController.jobMap.remove(cameraPojo.getToken());
			}
		}
	}
}
