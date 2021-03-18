package com.ztman.camera.util.camera.util;

import com.ztman.camera.controller.flvController;
import com.ztman.camera.util.camera.cache.CacheUtil;
import com.ztman.camera.util.camera.pojo.Config;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Title TimerUtil.java
 * @description 定时任务
 * @time 2019年12月16日 下午3:10:08
 * @author wuguodong
 **/
@Component
public class TimerVideoUtil implements CommandLineRunner {

	private final static Logger logger = LoggerFactory.getLogger(TimerVideoUtil.class);
	@Autowired
	private Config config;// 配置文件bean
	private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(4, new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
	@Override
	public void run(String... args) throws Exception {
		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (null != CacheUtil.START_VIDEO_MAP && 0 != CacheUtil.START_VIDEO_MAP.size()) {
					Set<String> keys = CacheUtil.START_VIDEO_MAP.keySet();
					System.out.println(keys);

					for (String key : keys) {
						try {
							// 最后打开时间
							long openTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.parse(CacheUtil.START_VIDEO_MAP.get(key).getOpenTime()).getTime();
							// 当前系统时间
							long newTime = System.currentTimeMillis();
							logger.info("当前系统时间"+newTime);
							logger.info("最后打开时间"+openTime);
							logger.info("当前时间-最后打开时间" + (newTime - openTime));
							logger.info("打开分钟" + ((newTime - openTime) / 1000 / 60));
							logger.info("保活时长" + 5+"s");
							// 如果通道使用人数为0，则关闭推流
							logger.info("视频播放人数"+CacheUtil.START_VIDEO_MAP.get(key) +"  "+key);
							if (CacheUtil.START_VIDEO_MAP.get(key) == null) {
								// 结束线程
								flvController.videoJobMap.get(key).setInterrupted();
								logger.debug("[定时任务：]  结束： " + CacheUtil.START_VIDEO_MAP.get(key).getVideoName() + "  播放任务！");
								// 清除缓存
								CacheUtil.START_VIDEO_MAP.remove(key);
								flvController.videoJobMap.remove(key);
							}
							else if ((newTime - openTime) / 1000  > 5) {
								flvController.videoJobMap.get(key).setInterrupted();
								logger.debug("[定时任务：]  结束： " + CacheUtil.START_VIDEO_MAP.get(key).getVideoName() + "  播放任务！");
								flvController.videoJobMap.remove(key);
								CacheUtil.START_VIDEO_MAP.remove(key);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}

				}
			}
		}, 1, 1, TimeUnit.SECONDS);

	}
}
