package com.ztman.camera.util.camera.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: wsd
 * @date: 2021/3/11 14:14
 */
@Data
public class VideoPojo  implements Serializable {
    private String videoName;
    private String type;
    private String token;
    private long range;
    private long length;
    private boolean status;
    private String openTime;// 打开时间
}
