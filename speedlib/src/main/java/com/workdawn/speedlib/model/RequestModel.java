package com.workdawn.speedlib.model;

import com.workdawn.speedlib.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2018/4/26.
 * @author workdawn
 */
public class RequestModel {
    private String url;
    private int readTimeout;
    private int connectTimeout;
    private long startRange;
    //-1 means total
    private long endRange;
    private long totalRange;
    private Map<String, String> headers = new HashMap<>();
    private String userAgent;
    private String method;
    //ETag
    private String if_None_Match;
    //Last-Modified
    private String if_Modified_Since;

    public String getIf_Modified_Since() {
        return if_Modified_Since;
    }

    public void setIf_Modified_Since(String if_Modified_Since) {
        this.if_Modified_Since = if_Modified_Since;
    }

    public String getIf_None_Match() {
        return if_None_Match;
    }

    public void setIf_None_Match(String if_None_Match) {
        this.if_None_Match = if_None_Match;
    }

    public String getMethod() {
        return Utils.isStringEmpty(method) ? "GET" : method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public long getStartRange() {
        return startRange;
    }

    public void setStartRange(long startRange) {
        this.startRange = startRange;
    }

    public long getEndRange() {
        return endRange;
    }

    public void setEndRange(long endRange) {
        this.endRange = endRange;
    }

    public long getTotalRange() {
        return totalRange;
    }

    public void setTotalRange(long totalRange) {
        this.totalRange = totalRange;
    }
}
