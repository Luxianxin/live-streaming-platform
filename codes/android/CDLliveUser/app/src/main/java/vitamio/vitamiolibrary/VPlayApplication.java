package vitamio.vitamiolibrary;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Cache;
import okhttp3.OkHttpClient;


/**
 * Created by aoe on 2016/3/25.
 */
public class VPlayApplication extends Application{
    private static OkHttpClient sOkHttpClient;
    private static VPlayApplication boPlayApplication;

    private static Context mContext;

    /**
     * 是否调试模式 true 调试模式，显示日志 false 发布模式，关闭日志等调试信息
     */
    public static boolean isDebug;

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

        mContext = getApplicationContext();
        isDebug = isApkDebugable(boPlayApplication);
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


    public static VPlayApplication getApplication() {
        if (boPlayApplication == null){
            initialize();
        }
        return boPlayApplication;
    }

    private static void initialize() {
        boPlayApplication = new VPlayApplication();
        boPlayApplication.onCreate();
    }

    public static Context getContext() {
        return boPlayApplication;

    }

    public static VPlayApplication getInstance() {
        return boPlayApplication;
    }


    private static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info= context.getApplicationInfo();
            return (info.flags&ApplicationInfo.FLAG_DEBUGGABLE)!=0;
        } catch (Exception e) {

        }
        return false;
    }




}
