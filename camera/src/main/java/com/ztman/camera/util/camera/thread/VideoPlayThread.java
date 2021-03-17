package com.ztman.camera.util.camera.thread;

import com.ztman.camera.controller.flvController;
import com.ztman.camera.util.camera.cache.CacheUtil;
import com.ztman.camera.util.camera.pojo.VideoPojo;
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
public class VideoPlayThread {

	private final static Logger logger = LoggerFactory.getLogger(VideoPlayThread.class);

	public static class MyRunnable implements Runnable {

		// 创建线程池
		public static ExecutorService es = Executors.newCachedThreadPool();

		private VideoPojo videoPojo;
		private Thread nowThread;

		public MyRunnable(VideoPojo videoPojo) {
			this.videoPojo = videoPojo;
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
				CacheUtil.START_VIDEO_MAP.put(videoPojo.getToken(), videoPojo);
				for (int i = 0; i < CacheUtil.START_VIDEO_MAP.size(); ) {

					nowThread.sleep(1);
				}
				// 清除缓存
				CacheUtil.START_VIDEO_MAP.remove(videoPojo.getToken());
				flvController.videoJobMap.remove(videoPojo.getToken());
			} catch (Exception e) {
				logger.error("当前任务： " + videoPojo.getVideoName() + "停止...");
				CacheUtil.START_VIDEO_MAP.remove(videoPojo.getToken());
				flvController.videoJobMap.remove(videoPojo.getToken());
			}
		}
	}
}
