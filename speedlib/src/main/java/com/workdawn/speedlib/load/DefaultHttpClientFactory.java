package com.workdawn.speedlib.load;

public class  DefaultHttpClientFactory implements IHttpClientFactory {

    @Override
    public IHttpClient create() {
        return new DefaultHttpClientImpl();
    }
}
