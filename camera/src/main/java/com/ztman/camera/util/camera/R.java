package com.ztman.camera.util.camera;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: wsd
 * @date: 2021/3/18 11:04
 */
@Data
public class R<T> implements Serializable {
    private T data;
    private String msg = "seccess";
    private boolean flag = true;
    public R(boolean flag,String msg){
        this.flag = flag;
        this.msg = msg;
    }

    public R(T data, String msg) {
        this.data = data;
        this.msg = msg;
    }
}
