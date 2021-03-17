package com.ztman.camera.util.camera.pojo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Title ConfigPojo.java
 * @description 读取配置文件的bean
 * @time 2019年12月25日 下午5:11:21
 * @author wuguodong
 **/
@Data
@Component
@ConfigurationProperties(prefix = "config")
public class Config {
	private String keepalive;// 保活时长（分钟）
	private String push_host;// 推送地址
	private String host_extra;// 额外地址
	private String push_port;// 推送端口
	private String main_code;// 主码流最大码率
	private String sub_code;// 主码流最大码率

}
