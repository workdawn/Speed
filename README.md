[![](https://jitpack.io/v/workdawn/Speed.svg)](https://jitpack.io/#workdawn/Speed)
<br>
Speed
=====

一个Android端资源下载器，可以根据服务端对多线程下载的支持程度自动选择单线程下载或者多线程断点下载
----------------------------------------------------

更新日志
----

当前版本`1.0.6`
<br>
1.添加`SpeedOption.setAllowedNetworkTypes(int netFlags)`方法，允许设置下载器执行的网络环境<br>
2.优化字节缓冲区获取策略<br>
3.fix bugs<br>
<br>

版本`1.0.5`
<br>
更新内容：<br>
1.修复潜在内存泄露问题<br>
2.fix bugs<br>
<br>

版本`1.0.4`
<br>
更新内容：<br>
1.添加启动任务组方法`Speed.start(ArrayList, ArrayList)` <br>
2.修改一些使用bug<br>
<br>

版本 `1.0.3`
<br>
更新内容：<br>
1.添加全局退出框架方法Speed.quit();<br>
2.修改一些Bug<br>


------------------

一些特点
-----------

1.支持暂停、重启、取消<br>
2.支持自定义通知的显示样式，支持通知的显示与隐藏<br>
3.支持服务端文件变化检测，根据检测结果进行续传或者重新下载<br>
4.支持服务端对文件范围下载的支持检测，根据检测结果进行单线程或者多线程下载<br>
5.支持自定义网络下载器<br>
6.支持自定义数据库访问器<br>
7.支持全局请求头设置，支持特定下载请求头设置<br>
8.支持同时执行的下载任务数量调整，或者根据网络自动调整<br>
9.支持任务优先级(下面有详细介绍)<br>
10.支持针对任务级别的资源地址存放设置<br>
11.更多详见项目

效果展示
----------

![download_gif_1](https://github.com/workdawn/Speed/raw/master/gif/1.gif) &nbsp; &nbsp; ![download_gif_2](https://github.com/workdawn/Speed/raw/master/gif/2.gif) &nbsp; &nbsp; ![download_gif_3](https://github.com/workdawn/Speed/raw/master/gif/3.gif) &nbsp; &nbsp; ![download_gif_4](https://github.com/workdawn/Speed/raw/master/gif/4.gif)

![download_gif_5](https://github.com/workdawn/Speed/raw/master/gif/5.gif) &nbsp; &nbsp; ![download_gif_6](https://github.com/workdawn/Speed/raw/master/gif/6.gif)


如何使用
-------

1.引用方法<br>
<br>
1).gradle中使用<br>
在项目根目录的build.gradle文件中，添加如下配置：
```
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
在项目module的build.gradle中添加:
```
    dependencies {
	     compile 'com.github.workdawn:Speed:1.0.6'
	}
```

2).maven中使用<br>
<br>
在build中添加
```
    <repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
添加依赖
```
    <dependency>
	    <groupId>com.github.workdawn</groupId>
	    <artifactId>Speed</artifactId>
	    <version>1.0.6</version>
	</dependency>
```

2.使用方法<br>
<br>
1).最简单的使用方式，直接调用`Speed.start(url);`或者 `Speed.start(url, fileName);`，在调用了这个方法之后，Speed就会在后台启动相应线程去下载对应url下的资源文件<br>
<br>
注意：建议使用`Speed.start(url, fileName)`，因为在没有明确标明文件名的情况下，Speed无法确切的知道需要下载的文件类型，这个时候Speed会以`.tmp`扩展名结尾，这样当下载完成后，直接通过通知或者其他方式打开文件可能会出现异常！<br>
<br>

2).如果想监听下载进度怎么办？ 简单，直接在start方法后面跟上下载进度监听器即可，举例如下：
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

3).如果想监听下载结果怎么办？ 也很简单可以直接在上面进度监听的后面跟上结果监听，举例如下：
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

4).暂停下载，调用`Speed.pause(url)`，只有在正在执行的任务（状态为RUNNING）或者重启状态下的任务（状态为RESUME）才能够暂停，不是这个状态的任务如果执行暂停的话没有效果，这个方法有可能会返回一个为null<br>
<br>
5).重启下载，调用`Speed.resume(url)`，只有暂停状态下的任务（状态为PAUSE)执行这个方法才有效果，否则忽略，这个方法有可能返回null<br>
<br>
6).取消下载，`Speed.cancel(url)`取消指定任务，`Speed.cancel(List<String> urls)`取消一组任务，注意组里面的任务不应该过多，`Speed.cancelAll()`取消所有任务<br>
<br>

一些允许自定义的设置项
-----------------------

全局自定义设置都通过SpeedOption类来完成
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
public SpeedOption setAllowedNetworkTypes(int netFlags);//设置允许下载器执行的网络环境(可取值SpeedOption.NETWORK_WIFI, SpeedOption.NETWORK_MOBILE，支持或运算)，默认WIFI + MOBILE

```

特定的任务设置通过RequestTask来完成，比如：
```
public RequestTask setOnDownloadProgressChangeListener(IDownloadProgressCallback cb); //设置下载进度监听器
public RequestTask setOnDownloadResultListener(IDownloadResultCallback cb); //设置下载完成度监听器
public RequestTask setRequestHeaders(Map<String, String> headers); //设置请求头
public void setPriority(int priority); //设置任务优先级
public RequestTask setSaveFile(File saveFile); //设置该下载任务的资源存放地址

```

有关任务优先级说明：
------------------
Speed内部使用了优先级队列来组织每个下载任务，所以Speed中的任务天然带有优先级特性，改变任务的优先级可以通过设置`setPriority(int priority)`，数值越高优先级越高，设置的优先级仅仅影响在Resume队列中的任务，而不会影响正在执行的任务，
也就是说，如果当前同时允许最多执行3个任务，那么当第四个，第五个任务到来的时候它们会进入Resume队列进行等待，等到执行队列中有任务执行完成后，Speed会自动调用Resume队列中的任务执行，这个时候会优先调用优先级高的任务；

<br>

自定义通知举例(其他自定义，如网络请求，数据库访问类似)
--------------
1.不想使用默认的通知怎么办？ 可以通过实现INotificationShow来实现自己的通知样式，举例如下：
```
public class NotificationShowImpl implements INotificationShow {
    @Override
    public int getSmallIcon() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public Bitmap getLargeIcon() {
        return null;
    }

    @Override
    public String getContentTitle() {
        return "开始下载";
    }

    @Override
    public String getContent() {
        return "正在下载";
    }

    @Override
    public String getDownloadFinishContent() {
        return "已经下载完成";
    }

    @Override
    public boolean getAutoCancel() {
        return true;
    }

    @Override
    public RemoteViews getRemoteViews(RequestTask requestTask, Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_notification_content);
        return remoteViews;
    }

    @Override
    public int getRemoteViewTitleViewId() {
        return R.id.remote_tv_title;
    }

    @Override
    public int getRemoteViewContentViewId() {
        return R.id.remote_tv_content;
    }

    @Override
    public int getRemoteViewProgressBarViewId() {
        return R.id.p_remote;
    }
}
```
然后在初始化Speed的时候
```
    SpeedOption option = SpeedOption.newInstance()
                .setNotificationShowImpl(new NotificationShowImpl());
    Speed.init(context, option);
```
<br>


2.默认的通知动作是什么也不干，如果想在下载完成后点击通知执行特定动作（比如安装应用），那么可以实现INotificationAction类，举例如下：
```
public class NotificationActionImpl implements INotificationAction {
    @Override
    public PendingIntent onAction(RequestTask requestTask, Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(requestTask.getSaveFile()), type);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }
}
```
然后在初始化Speed的时候
```
    SpeedOption option = SpeedOption.newInstance()
                .setNotificationActionImpl(new NotificationActionImpl());
    Speed.init(context, option);
```

