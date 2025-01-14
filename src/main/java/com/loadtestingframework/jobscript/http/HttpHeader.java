package com.loadtestingframework.jobscript.http;

import java.util.ArrayList;
import java.util.List;

public class HttpHeader {
    private List<String> headers = new ArrayList<>();

    public HttpHeader addHeader(String name, String value) {
        headers.add(name);
        headers.add(value);
        return this;
    }

    public List<String> getHeaders() {
        return headers;
    }
}
