package org.easydarwin.easypusher;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import com.squareup.picasso.Picasso;

import org.easydarwin.bus.StartRecord;
import org.easydarwin.bus.StopRecord;
import org.easydarwin.config.Config;
import org.easydarwin.push.MediaStream;
import org.easydarwin.push.MuxerModule;
import org.easydarwin.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class EasyApplication extends Application {
    private static OkHttpClient sOkHttpClient;
    public static final String KEY_ENABLE_VIDEO = "key-enable-video";
    private static EasyApplication mApplication;


    public static final Bus BUS = new Bus(ThreadEnforcer.ANY);
    public long mRecordingBegin;
    public boolean mRecording;

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         *  初始化picasso使用okhttp作为网络请求框架
         */
        Picasso.setSingletonInstance(new Picasso.Builder(this)
                .downloader(new ImageDownLoader(getSOkHttpClient()))
                .loggingEnabled(true)
                .build());

        mApplication = this;
        // for compatibility
        resetDefaultServer();
        File youyuan = getFileStreamPath("SIMYOU.ttf");
        if (!youyuan.exists()){
            AssetManager am = getAssets();
            try {
                InputStream is = am.open("zk/SIMYOU.ttf");
                FileOutputStream os = openFileOutput("SIMYOU.ttf", MODE_PRIVATE);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.close();
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BUS.register(this);
    }

    /**
     * 创建全局OkHttpClient对象
     * <p>
     * OkHttpClient 用于管理所有的请求，内部支持并发，
     * 所以我们不必每次请求都创建一个 OkHttpClient 对象，这是非常耗费资源的。接下来就是创建一个 Request 对象了
     *
     * @return
     */
    public OkHttpClient getSOkHttpClient() {
        //创建okhttp的请求对象 参考地址  http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0106/2275.html
        File file = new File(getFilesDir() + "/Portrait/");//缓存文件夹
        if (!file.exists()) {
            file.mkdirs();
        }
        int cacheSize = 10 * 1024 * 1024;//缓存大小为10M

        if (sOkHttpClient == null) {
            sOkHttpClient = new OkHttpClient.Builder()
                    .readTimeout(20000, TimeUnit.SECONDS)//设置读取超时时间
                    .writeTimeout(20000, TimeUnit.SECONDS)//设置写的超时时间
                    .connectTimeout(20000, TimeUnit.SECONDS)//设置连接超时时间
                    .sslSocketFactory(createSSLSocketFactory())    //添加信任所有证书
                    .hostnameVerifier(new HostnameVerifier() {     //信任规则全部信任
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .cache(new Cache(file, cacheSize))
                    .build();
        }
        return sOkHttpClient;
    }

    /**
     * 测试环境https添加全部信任
     * okhttp的配置
     *
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    private void resetDefaultServer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultIP = sharedPreferences.getString(Config.SERVER_IP, Config.DEFAULT_SERVER_IP);
        if ("114.55.107.180".equals(defaultIP)
                || "121.40.50.44".equals(defaultIP)
                || "www.easydarwin.org".equals(defaultIP)){
            sharedPreferences.edit().putString(Config.SERVER_IP, Config.DEFAULT_SERVER_IP).apply();
        }

        String defaultRtmpURL = sharedPreferences.getString(Config.SERVER_URL, Config.DEFAULT_SERVER_URL);
        int result1 = defaultRtmpURL.indexOf("rtmp://www.easydss.com/live");
        int result2 = defaultRtmpURL.indexOf("rtmp://121.40.50.44/live");
        if(result1 != -1 || result2 != -1){
            sharedPreferences.edit().putString(Config.SERVER_URL, Config.DEFAULT_SERVER_URL).apply();
        }
    }

    public static EasyApplication getEasyApplication() {
        return mApplication;
    }

    public void saveStringIntoPref(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getIp() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = sharedPreferences.getString(Config.SERVER_IP, Config.DEFAULT_SERVER_IP);
        return ip;
    }

    public String getPort() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String port = sharedPreferences.getString(Config.SERVER_PORT, Config.DEFAULT_SERVER_PORT);
        return port;
    }

    public String getId() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String id = sharedPreferences.getString(Config.STREAM_ID, Config.DEFAULT_STREAM_ID);
        if (!id.contains(Config.STREAM_ID_PREFIX)) {
            id = Config.STREAM_ID_PREFIX + id;
        }
        saveStringIntoPref(Config.STREAM_ID, id);
        return id;
    }


    public String getUrl() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defValue = Config.DEFAULT_SERVER_URL;
        String ip = sharedPreferences.getString(Config.SERVER_URL, defValue);
        if (ip.equals(defValue)){
            sharedPreferences.edit().putString(Config.SERVER_URL, defValue).apply();
        }
        return ip;
    }


    public String getUrlLogin() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defValue = Config.DEFAULT_SERVER_URL_LOGIN;
        String ip = sharedPreferences.getString(Config.LOGIN_URL, defValue);
        if (ip.equals(defValue)){
            sharedPreferences.edit().putString(Config.LOGIN_URL, defValue).apply();
        }
        return ip;
    }

    public static boolean isRTMP() {
        return true;
    }

    @Subscribe
    public void onStartRecord(StartRecord sr){
        mRecording = true;
        mRecordingBegin = System.currentTimeMillis();
    }

    @Subscribe
    public void onStopRecord(StopRecord sr){
        mRecording = false;
        mRecordingBegin = 0;
    }
}
