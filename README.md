Speed
=
一个Android端资源下载器，可以根据服务端对多线程下载的支持程度自动选择单线程下载或者多线程断点下载
-----------

一些特点
-----------

1.支持暂停、重启、取消<br>
2.支持自定义通知的显示样式，支持通知的显示与隐藏<br>
3.支持服务端文件变化检测，根据检测结果进行续传或者重新下载<br>
4.支持服务端对文件范围下载的支持检测，根据检测结果进行单线程或者多线程下载<br>
5.支持自定义网络下载器<br>
6.支持自定义数据库访问器<br>
7.支持全局请求头设置，支持特定下载请求头设置<br>
8.支持同时执行的下载任务数量调整，或者根据网络自动跳转
9.更多详见项目

效果展示(稍后添加)
-----------

如何使用
-----------

1.最简单的使用方式，直接调用`Speed.start(url);`或者 `Speed.start(url, fileName);`，在调用了这个方法之后，Speed就会在后台启动相应线程去下载对应url下的资源文件<br>
<br>

2.如果想监听下载进度怎么办？ 简单，直接在start方法后面跟上下载进度监听器即可，如下面这样：
```
            Speed.start(url, fileName).setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                    //这里监听下载进度
                }

                @Override
                public void onPreDownload(long totalSize) {
                    //这里监听下载准备情况，当调用完onPreDownload后 会立马调用onDownloading
                }
            })
```
<br>

3.如果想监听下载结果怎么办？ 也很简单可以直接在上面进度监听的后面跟上结果监听，如下面这样：
```
Speed.start(url, fileName).setOnDownloadProgressChangeListener(new IDownloadProgressCallback() {
                @Override
                public void onDownloading(long totalSize, long currentSize) {
                }

                @Override
                public void onPreDownload(long totalSize) {
                }
            }).setOnDownloadResultListener(new IDownloadResultCallback() {
                @Override
                public void onComplete(String filePath) {
                }

                @Override
                public void onError(String reason) {
                }
            });
```

4.暂停下载，调用`Speed.pause(url)`，只有在正在执行的任务（状态为RUNNING）或者重启状态下的任务（状态为RESUME）才能够暂停，不是这个状态的任务如果执行暂停的话没有效果，这个方法有可能会返回一个为null<br>
<br>
5.重启下载，调用`Speed.resume(url)`，只有暂停状态下的任务（状态为PAUSE)执行这个方法才有效果，否则忽略，这个方法有可能返回null<br>
<br>
6.取消下载，`Speed.cancel(url)`取消指定任务，`Speed.cancel(List<String> urls)`取消一组任务，注意组里面的任务不应该过多，`Speed.cancelAll()`取消所有任务，执行完这个方法后会退出Speed，清空任务队列，取消任务执行线程池，如果想再次启动下载需要重新初始化Speed，也就是调用`Speed.init(context) 或者 Speed.init(context, speedOption)`<br>
<br>

一些允许自定义的设置项
-----------------------

自定义设置都通过SpeedOption类来完成
<br>

```
public SpeedOption setConnectTimeout(int timeoutMillis);//设置连接超时时间
public SpeedOption setReadTimeOut(int timeoutMillis);//设置流读取超时时间
public SpeedOption setDatabaseFactory(IDatabaseFactory databaseFactory);//设置数据库实现工厂
public SpeedOption setHttpClientFactory(IHttpClientFactory httpClientFactory);//设置网络下载工厂
public SpeedOption setNotificationShowImpl(INotificationShow iNotificationShow);//设置通知展现实现类
public SpeedOption setNotificationActionImpl(INotificationAction iNotificationAction);//设置通知动作执行实现类
public SpeedOption showNotification(boolean show);//设置是否在下载的同时显示下载通知，默认显示
public SpeedOption showLog(boolean show);//设置是否打印日志，默认不打印
public SpeedOption setMaxAllowRunningTaskCount(int maxNum);//设置最大允许同时执行的任务数量，默认3个任务
public SpeedOption setAutoMaxAllowRunningTaskCount(boolean auto);//设置是否自动决定同时运行的任务数量，如果设置了这个那么执行的任务数量会根据当前网络状态自动调整，默认不自动
public SpeedOption setUserAgent(String agent);//设置请求的Agent
public SpeedOption setDownloadDir(File saveDir);//设置文件下载的存放位置
public SpeedOption setAutoExitSpeedWhenAllTaskComplete(boolean exit);//设置是否在任务全部完成后退出Speed，默认不退出
public SpeedOption setRequestHeaders(Map<String, String> headers);//设置全局请求头，如果某个任务设置了自己的请求头那么会忽略全局请求头

```
