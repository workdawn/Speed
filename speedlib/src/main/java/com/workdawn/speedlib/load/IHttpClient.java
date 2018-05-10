package com.workdawn.speedlib.load;

import com.workdawn.speedlib.model.RequestModel;

import java.io.InputStream;

/**
 * Created on 2018/4/25.
 * @author workdawn
 */
public interface IHttpClient {

    /**
     * Close the connection and resources
     */
    void close();

    /**
     * Initiate a request and get data
     * @param requestModel request params
     * @return responseData
     * @throws Exception
     */
    InputStream loadData(RequestModel requestModel) throws Exception;

    /**
     * Get resource content length
     * @return contentLength
     */
    long getContentLength();

    /**
     * Whether this request supports Range
     */
    boolean isAcceptRange();

    /**
     * Get Http response code
     * @return responseCode
     * @throws Exception
     */
    int getResponseCode() throws Exception;

    /**
     * Get Http header field information
     * @param key header key
     * @return header value
     */
    String getHeaderField(String key);
}
