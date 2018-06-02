package com.workdawn.speedlib.load;

import java.io.File;
import java.util.Map;

/**
 * Created on 2018/6/2.
 * @author workdawn
 */
public class RequestTaskWrapper {
    private RequestTask task;
    private static RequestTaskWrapper sRequestTaskWrapper;

    public static RequestTaskWrapper getInstance(){
        if(sRequestTaskWrapper == null){
            synchronized (RequestTaskWrapper.class) {
                if(sRequestTaskWrapper == null){
                    sRequestTaskWrapper = new RequestTaskWrapper();
                }
            }
        }
        return sRequestTaskWrapper;
    }

    public RequestTaskWrapper wrapperTask(RequestTask task){
        this.task = task;
        return this;
    }

    public void setPriority(int priority){
       task.setPriority(priority);
    }

    public void setSaveFile(File saveFile) {
        task.setSaveFile(saveFile);
    }

    public void setRequestHeaders(Map<String, String> headers){
        task.setRequestHeaders(headers);
    }
}
