package com.workdawn.speed;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.workdawn.speedlib.callback.IDownloadProgressCallback;
import com.workdawn.speedlib.callback.IDownloadResultCallback;
import com.workdawn.speedlib.callback.ITaskGroupDownloadProgressCallback;
import com.workdawn.speedlib.callback.ITaskGroupDownloadResultCallback;
import com.workdawn.speedlib.core.Speed;

import java.util.ArrayList;

public class MainActivity extends Activity {

    //Download link may fail, if it fails, please add new address
    private final static String WeChat = "http://imtt.dd.qq.com/16891/9A7CBD9CAFF7AA35E754408E2D2C6288.apk?fsname=com.tencent.mm_6.6.6_1300.apk&csr=1bbd";
    private final static String TT = "http://a4.res.meizu.com/source/3668/55f6fd40391a4614b5f5418034daeb12?sign=bb047e10529185a34ae57f0eded5e95e&t=5afedecb&fname=com.meizu.media.reader_4003001";
    private final static String BZ = "http://a3.res.meizu.com/source/3653/4af78c4aab7c48a18eb7fa65ba8c1a04?auth_key=1525960634-0-0-1904564795e931ccc24f17f189ee2c26&fname=com.lovebizhi.wallpaper_198";
    private final static String WB = "http://a4.res.meizu.com/source/3651/335989b4d0594d868ee8f74538717310?sign=2fedf65086642a40c5e3eafe3a1b1387&t=5af451fd&fname=com.sina.weibo_3619";
    private final static String QQ = "http://a3.res.meizu.com/source/3637/0e1a6a84267a4fb493bfe38c1b2ac8dd?auth_key=1525961282-0-0-ae7f6d07ca22be612933348d7f96d2ce&fname=com.tencent.mobileqq_832";

    private ProgressBar p_we_chat;
    private ProgressBar p_Hys;
    private ProgressBar p_wb;
    private ProgressBar p_qq;
    private ProgressBar p_bz;
    private int weChatMode = 0;
    private int hysMode = 0;
    private int wbMode = 0;
    private int qqMode = 0;
    private int bzMode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p_we_chat = (ProgressBar) findViewById(R.id.p_we_chat);
        p_Hys = (ProgressBar) findViewById(R.id.p_hys);
        p_wb = (ProgressBar) findViewById(R.id.p_wb);
        p_qq = (ProgressBar) findViewById(R.id.p_qq);
        p_bz = (ProgressBar) findViewById(R.id.p_bz);

    }

    public void weChat(final View view){
        final Button btn = (Button) view;
        if(weChatMode == 0){
            weChatMode = 1;
            Speed.start(WeChat, "weChat.apk").setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
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
                    btn.setText("下载失败，点击重新下载");
                }
            });
        } else if(weChatMode == 1){
            weChatMode = 2;
            btn.setText("重新开始");
            Speed.pause(WeChat);
        } else if(weChatMode == 2){
            weChatMode = 1;
            btn.setText("暂停下载");
            Speed.resume(WeChat);
        }
        /*ArrayList<String> urls = new ArrayList<>();
        urls.add(WeChat);
        urls.add(TT);
        urls.add(BZ);
        urls.add(WB);
        urls.add(QQ);
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("weChat.apk");
        fileNames.add("Tt.apk");
        fileNames.add("Bz.apk");
        fileNames.add("Wb.apk");
        fileNames.add("Qq.apk");

        Speed.start(urls, fileNames).setOnTaskGroupDownloadProgressListener(new ITaskGroupDownloadProgressCallback() {
            @Override
            public void onTasksDownloading(String url, long totalSize, long downloadedSize) {
                float progress = downloadedSize / (totalSize * 1.0f);
                switch (url) {
                    case WeChat:
                        Log.i("Speed", "WeChat  --> " + progress);
                        break;
                    case TT:
                        Log.i("Speed", "Tt  --> " + progress);
                        break;
                    case BZ:
                        Log.i("Speed", "Bz  --> " + progress);
                        break;
                    case WB:
                        Log.i("Speed", "Wb  --> " + progress);
                        break;
                    case QQ:
                        Log.i("Speed", "Qq  --> " + progress);
                        break;
                }
            }
        }).setOnTaskQueueDownloadResultListener(new ITaskGroupDownloadResultCallback() {
            @Override
            public void onTaskComplete(String url, String filePath) {
                switch (url) {
                    case WeChat:
                        Log.i("Speed", "WeChat  --> complete path = " + filePath );
                        break;
                    case TT:
                        Log.i("Speed", "Tt  --> complete path = " + filePath);
                        break;
                }
            }

            @Override
            public void onTaskError(String url, String reason) {
                switch (url) {
                    case WeChat:
                        Log.i("Speed", "WeChat  --> error = " + reason);
                        break;
                    case TT:
                        Log.i("Speed", "Tt  --> error = " + reason);
                        break;
                    case BZ:
                        Log.i("Speed", "Bz  --> error = " + reason);
                        break;
                    case WB:
                        Log.i("Speed", "Wb  --> error = " + reason);
                        break;
                    case QQ:
                        Log.i("Speed", "Qq  --> error = " + reason);
                        break;
                }
            }
        });*/
    }

    public void hys(final View view){
        final Button btn = (Button) view;
        if(hysMode == 0){
            hysMode = 1;
            Speed.start(TT, "Tt.apk").setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                    float progress = currentSize / (totalSize * 1.0f);
                    p_Hys.setProgress((int) (progress * 100));
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
                    btn.setText("下载失败，点击重新下载");
                }
            });
        } else if(hysMode == 1) {
            hysMode = 2;
            btn.setText("重新开始");
            Speed.pause(TT);
        } else if(hysMode == 2){
            hysMode = 1;
            btn.setText("暂停下载");
            Speed.resume(TT);
        }

    }

    public void bz(View view){
        final Button btn = (Button) view;
        if(bzMode == 0){
            bzMode = 1;
            Speed.start(BZ, "Bz.apk").setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                    float progress = currentSize / (totalSize * 1.0f);
                    p_bz.setProgress((int)(progress  * 100));
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
                    btn.setText("下载失败，点击重新下载");
                }
            });
        } else if(bzMode == 1){
            bzMode = 2;
            btn.setText("重新开始");
            Speed.pause(BZ);
        } else if(bzMode == 2){
            bzMode = 1;
            btn.setText("暂停下载");
            Speed.resume(BZ);
        }
    }

    public void wb(View view){
        final Button btn = (Button) view;
        if(wbMode == 0){
            wbMode = 1;
            Speed.start(WB, "Wb.apk").setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                    float progress = currentSize / (totalSize * 1.0f);
                    p_wb.setProgress((int)(progress  * 100));
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
                    btn.setText("下载失败，点击重新下载");
                }
            });
        } else if(wbMode == 1){
            wbMode = 2;
            btn.setText("重新开始");
            Speed.pause(WB);
        } else if(wbMode == 2){
            wbMode = 1;
            btn.setText("暂停下载");
            Speed.resume(WB);
        }
    }

    public void qq(View view){
        final Button btn = (Button) view;
        if(qqMode == 0){
            qqMode = 1;
            Speed.start(QQ, "qq.apk").setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                    float progress = currentSize / (totalSize * 1.0f);
                    p_qq.setProgress((int)(progress  * 100));
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
                    btn.setText("下载失败，点击重新下载");
                }
            });
        } else if(qqMode == 1){
            qqMode = 2;
            btn.setText("重新开始");
            Speed.pause(QQ);
        } else if(qqMode == 2){
            qqMode = 1;
            btn.setText("暂停下载");
            Speed.resume(QQ);
        }
    }

    public void cancelAll(View view){
        Speed.cancelAll();
    }

    public void quit(View view){
        Speed.quit();
    }
}
