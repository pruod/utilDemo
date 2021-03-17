package com.ztman.camera.util.camera.cache;


import com.ztman.camera.util.camera.pojo.CameraPojo;
import com.ztman.camera.util.camera.pojo.VideoPojo;

import java.util.HashMap;
import java.util.Map;

/**
 * @Title CacheUtil.java
 * @description 推流缓存信息
 * @time 2019年12月17日 下午3:12:45
 * @author wuguodong
 **/
public final class CacheUtil {
	/*
	 * 保存已经开始推的流
	 */
	public static Map<String, CameraPojo> STREAMMAP = new HashMap<String, CameraPojo>();
	public static Map<String, VideoPojo> START_VIDEO_MAP = new HashMap<>();

	/*
	 * 保存服务启动时间
	 */
	public static long STARTTIME;
	public static long START_VIDEO_TIME;

}
