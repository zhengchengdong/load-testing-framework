package com.loadtestingframework.web.viewobject;

import java.util.HashMap;
import java.util.Map;

public class CommonVO {

    private boolean success;

    private String msg;

    private Object data;

    private CommonVO() {
    }

    public static CommonVO success() {
        CommonVO vo = new CommonVO();
        vo.success = true;
        return vo;
    }

    public static CommonVO success(Object key, Object value) {
        CommonVO vo = new CommonVO();
        vo.success = true;
        Map data = new HashMap();
        data.put(key, value);
        vo.data = data;
        return vo;
    }

    public static CommonVO unsuccess(String msg) {
        CommonVO vo = new CommonVO();
        vo.success = false;
        vo.msg = msg;
        return vo;
    }

    public static CommonVO success(Object data) {
        CommonVO vo = new CommonVO();
        vo.success = true;
        vo.data = data;
        return vo;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public CommonVO append(Object key, Object value) {
        if (data == null) {
            data = new HashMap();
        }
        ((Map) data).put(key, value);
        return this;
    }
}
