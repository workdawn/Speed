package com.workdawn.speedlib.load;

import com.workdawn.speedlib.model.RequestModel;
import com.workdawn.speedlib.utils.LogUtils;
import com.workdawn.speedlib.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created on 2018/4/25.
 * @author workdawn
 */
public class DefaultHttpClientImpl implements IHttpClient {

    private HttpURLConnection urlConnection = null;
    private static final int MAXIMUM_REDIRECTS = 5;
    private InputStream inStream = null;

    @Override
    public void close() {
        if(inStream != null){
            try {
                inStream.close();
            } catch (Exception e) {
                LogUtils.e(e.getMessage());
                e.printStackTrace();
            }
        }
        if(urlConnection != null){
            urlConnection.disconnect();
        }
    }

    @Override
    public InputStream loadData(RequestModel requestModel) throws Exception{
        return loadDataMayRedirects(requestModel, 0);
    }

    /**
     * Load data and process redirect
     * @param requestModel request params
     * @param redirects redirects
     * @throws IOException may throw IOException
     */
    private InputStream loadDataMayRedirects(RequestModel requestModel, int redirects) throws IOException{
        if(redirects > MAXIMUM_REDIRECTS){
            throw new IOException("Too many (> " + MAXIMUM_REDIRECTS + ") redirects!");
        }
        urlConnection = (HttpURLConnection) new URL(requestModel.getUrl()).openConnection();
        urlConnection.setReadTimeout(requestModel.getReadTimeout());
        urlConnection.setConnectTimeout(requestModel.getConnectTimeout());
        urlConnection.setRequestMethod(requestModel.getMethod());

        if(!Utils.isStringEmpty(requestModel.getUserAgent())){
            urlConnection.addRequestProperty("User-Agent", requestModel.getUserAgent());
        } else {
            urlConnection.addRequestProperty("User-Agent", "Android-Speed-Downloader");
        }
        if(!Utils.isStringEmpty(requestModel.getIf_None_Match())){
            urlConnection.addRequestProperty("If-None-Match", requestModel.getIf_None_Match());
        }
        if(!Utils.isStringEmpty(requestModel.getIf_Modified_Since())){
            urlConnection.addRequestProperty("If-Modified-Since", requestModel.getIf_Modified_Since());
        }
        if(requestModel.getEndRange() != -1){
            urlConnection.addRequestProperty("Range", "bytes=" + requestModel.getStartRange() + "-" + requestModel.getEndRange());
        }

        Map<String, String> headers = requestModel.getHeaders();
        if(headers != null && headers.size() > 0){
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        //Notice:If the HTTP header field adds a range attribute, If-Modified-Since and If-None-Match do not seem to work,
        // so this attribute should not be set in the judgment phase of resource change
        /*urlConnection.addRequestProperty("Range", "bytes=" + requestModel.getStartRange()
                + "-" + (requestModel.getEndRange() == -1 ? "" : requestModel.getEndRange()));*/

        urlConnection.connect();
        int responseCode = urlConnection.getResponseCode();
        if(responseCode / 100 == 2){
            inStream = urlConnection.getInputStream();
        } else if(responseCode / 300 == 3 && responseCode != 304){
            // 304 means that the resource has not changed, in addition to the need to find the real resource address from the "Location"
            String realUrl = urlConnection.getHeaderField("Location");
            if(Utils.isStringEmpty(realUrl)){
                throw new IOException("Request failed, responseCode = " + responseCode + " but redirect location is null");
            }
            requestModel.setUrl(realUrl);
            return loadDataMayRedirects(requestModel, redirects + 1);
        } else {
            throw new IOException("Request failed, responseCode = " + responseCode);
        }
        return inStream;
    }

    @Override
    public long getContentLength() {
        return urlConnection.getContentLength();
    }

    @Override
    public boolean isAcceptRange() {
        return "bytes".equals(getHeaderField("Accept-Ranges"));
    }

    @Override
    public int getResponseCode() throws Exception{
        return urlConnection.getResponseCode();
    }

    @Override
    public String getHeaderField(String key) {
        return urlConnection.getHeaderField(key);
    }
}
