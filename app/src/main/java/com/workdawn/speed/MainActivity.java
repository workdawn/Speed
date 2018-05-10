package com.workdawn.speed;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.workdawn.speedlib.callback.IDownloadProgressCallback;
import com.workdawn.speedlib.callback.IDownloadResultCallback;
import com.workdawn.speedlib.core.Speed;

public class MainActivity extends Activity {

    private final static String WeChat = "http://imtt.dd.qq.com/16891/9A7CBD9CAFF7AA35E754408E2D2C6288.apk?fsname=com.tencent.mm_6.6.6_1300.apk&csr=1bbd";
    private final static String HYS = "http://media.cmechina.net/wwwhaoyishengcom/app/app-release-version_code_28__version_name_3.3.3.apk";

    private ProgressBar p_we_chat;
    private ProgressBar p_Hys;
    private int mode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p_we_chat = (ProgressBar) findViewById(R.id.p_we_chat);
        p_Hys = (ProgressBar) findViewById(R.id.p_hys);
        p_we_chat.setMax(100);
        p_Hys.setMax(100);
    }

    public void weChat(final View view){
        final Button btn = (Button) view;
        if(mode == 0){
            //mode = 1;
            Speed.start(WeChat, "weChat.apk").setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                    Log.i("Speed",  "WeChat TotalSize = " + totalSize + " ; AlreadyDownloadedSize = " + currentSize);
                    float progress = currentSize / (totalSize * 1.0f);
                    p_we_chat.setProgress((int)(progress  * 100));
                }

                @Override
                public void onPreDownload(long totalSize) {
                    btn.setText("暂停下载");
                }
            }).setOnDownloadResultListener(new IDownloadResultCallback() {
                @Override
                public void onComplete(String filePath) {
                    btn.setText("下载完成:" + filePath);
                }

                @Override
                public void onError(String reason) {
                    Toast.makeText(MainActivity.this, "下载失败！！！" + reason, Toast.LENGTH_LONG).show();
                }
            });
        } else if(mode == 1){
            //mode = 2;
            btn.setText("重新开始");
            Speed.pause(WeChat);
        } else if(mode == 2){
            //mode = 1;
            btn.setText("暂停下载");
            Speed.resume(WeChat);
        }

    }

    public void hys(final View view){
        final Button btn = (Button) view;
        if(mode == 0){
            //mode = 1;
            Speed.start(HYS, "Hys.apk").setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                    //Log.i("Speed",  "Hys TotalSize = " + totalSize + " ; AlreadyDownloadedSize = " + currentSize);
                    float progress = currentSize / (totalSize * 1.0f);
                    p_Hys.setProgress((int)(progress  * 100));
                }

                @Override
                public void onPreDownload(long totalSize) {
                    btn.setText("退出下载");
                }
            }).setOnDownloadResultListener(new IDownloadResultCallback() {
                @Override
                public void onComplete(String filePath) {
                    btn.setText("下载完成:" + filePath);
                }

                @Override
                public void onError(String reason) {
                    Toast.makeText(MainActivity.this, "下载失败！！！" + reason, Toast.LENGTH_LONG).show();
                }
            });
        } else if(mode == 1){
            //mode = 0;
            btn.setText("开始下载");
            Speed.cancel(HYS);
        }
        /*else if(mode == 1) {
            mode = 2;
            btn.setText("重新开始");
            Speed.pause(HYS);
        } else if(mode == 2){
            mode = 1;
            btn.setText("暂停下载");
            Speed.resume(HYS);
        }*/

    }
}
