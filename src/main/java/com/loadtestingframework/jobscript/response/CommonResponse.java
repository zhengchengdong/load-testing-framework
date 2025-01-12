package com.loadtestingframework.jobscript.response;

import java.util.HashMap;
import java.util.Map;

public class CommonResponse {

    private boolean success;

    private String msg;

    private Object data;

    private CommonResponse() {
    }

    public static CommonResponse success() {
        CommonResponse vo = new CommonResponse();
        vo.success = true;
        return vo;
    }

    public static CommonResponse success(Object key, Object value) {
        CommonResponse vo = new CommonResponse();
        vo.success = true;
        Map data = new HashMap();
        data.put(key, value);
        vo.data = data;
        return vo;
    }

    public static CommonResponse unsuccess(String msg) {
        CommonResponse vo = new CommonResponse();
        vo.success = false;
        vo.msg = msg;
        return vo;
    }

    public static CommonResponse success(Object data) {
        CommonResponse vo = new CommonResponse();
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

    public CommonResponse append(Object key, Object value) {
        if (data == null) {
            data = new HashMap();
        }
        ((Map) data).put(key, value);
        return this;
    }
}
