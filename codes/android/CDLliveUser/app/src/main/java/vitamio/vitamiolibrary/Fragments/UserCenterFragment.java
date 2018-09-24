package vitamio.vitamiolibrary.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
//import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import vitamio.vitamiolibrary.FileUtilcll;
import vitamio.vitamiolibrary.ForgetPwd;
import vitamio.vitamiolibrary.Https;
import vitamio.vitamiolibrary.Login;
import vitamio.vitamiolibrary.R;
import vitamio.vitamiolibrary.ResetPwd;
import vitamio.vitamiolibrary.config.Config;


import static android.app.Activity.RESULT_OK;


public class UserCenterFragment extends Fragment implements View.OnClickListener{
    private TextView mUsernameText;
    private TextView mEmailText;
    private TextView TIme1;
    private TextView TIme2;
    private TextView TIme3;
    private TextView TIme4;
    private String mUsername;
    private String mUsernameTemp;
    private String mEmail;
    private String mPortraitUrl;
    private String urlpath;//图片路径
    private Button mResetPwdButton;
    private String cookie;
    private Button mLogoffButton;
    private SharedPreferences usercenter_sp;//记录用户名、邮箱、图片路径
    private SharedPreferences.Editor editor;
    private ImageView ivPortrait;
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    protected static Uri tempUri;
    private Retrofit retrofit;
    private Https https;
    private boolean isGetData = false;

    public UserCenterFragment() { }

    public static UserCenterFragment newInstance(String userName,String cookie) {
        UserCenterFragment fragment = new UserCenterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_NAME", userName);
        bundle.putString("mCookie", cookie);//利用此构造函数在登录页面向此Fragment传递用户名参数
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_center, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(getActivity());
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mUsernameText = (TextView) view.findViewById(R.id.tv_username);
        mEmailText = (TextView) view.findViewById(R.id.tv_email);
        ivPortrait = (ImageView) view.findViewById(R.id.iv_portrait);
        mResetPwdButton = (Button) view.findViewById(R.id.usercenter_btn_resetpwd);
        mLogoffButton = (Button) view.findViewById(R.id.usercenter_btn_logoff);
        TIme1 = (TextView)view.findViewById(R.id.textView1);
        TIme2 =(TextView) view.findViewById(R.id.textView2);
        TIme3 =(TextView) view.findViewById(R.id.textView3);
        TIme4 =(TextView) view.findViewById(R.id.textView4);

        usercenter_sp = getActivity().getSharedPreferences("userInfo", 0);
        editor = usercenter_sp.edit();
        mUsername = usercenter_sp.getString("SP_USER_NAME", "");
        mEmail = usercenter_sp.getString("SP_EMAIL", "");
        mPortraitUrl = usercenter_sp.getString("SP_PORTRAITURL", "");

        if(getArguments() != null){
            mUsernameTemp = getArguments().getString("USER_NAME");//获取从登录页面传来的用户名
            cookie = getArguments().getString("mCookie");//获取从登录页面传来的cookie
        }

        AvgTime();
        TotalTime();
        TopThreeTime();
        TopThreeNum();
        ivPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoosePicDialog();
            }
        });
        ivPortrait.setOnClickListener(this);
        mResetPwdButton.setOnClickListener(this);
        mLogoffButton.setOnClickListener(this);

        //sp里保存的用户名和登录页面传进来的用户名比较，不一致则需重新获取用户信息
        if(!mUsername.equals(mUsernameTemp)) {
            mUsername = mUsernameTemp;
            editor.putString("SP_USER_NAME", mUsernameTemp);//修改保存的username，在后面提交修改
            //Toast.makeText(getActivity(), "获取用户信息", Toast.LENGTH_SHORT).show();

            Gson gson=new Gson();
            //实例化UserCenter1对象，在info.java文件
            UserCenter1 lg=new UserCenter1(mUsername);
            //UserCenter1 lg=new UserCenter1("lxx");
            String obj = gson.toJson(lg);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
            final UserCenterFragment.LiveServiceDown service = retrofit.create(UserCenterFragment.LiveServiceDown.class);
            retrofit2.Call<ResponseBody> call = service.getUserInfo(body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(
                        retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String result  = response.body().string();
                        JSONObject obj=new JSONObject(result);
                        mEmail = obj.getString("email");
                        mPortraitUrl = obj.getString("headPortrait");
                        System.out.println("email:" + mEmail + "  databaseurl:" + mPortraitUrl );
                        if(!mPortraitUrl.equals("")) {
                            Picasso.with(getActivity()).load(Config.DEFAULT_SERVER_URL_LOGIN + mPortraitUrl).into(ivPortrait);//从服务器获取头像
                            editor.putString("SP_PORTRAITURL", mPortraitUrl);//修改保存的url
                        }
                        editor.putString("SP_EMAIL", mEmail);//修改保存的email
                        editor.commit();
                        mUsernameText.setText(mUsername);
                        mEmailText.setText(mEmail);
                    } catch (Exception e) {
                        userCenterError();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    userCenterError();
                    t.printStackTrace();
                }
            });
        } else {
            mUsernameText.setText(mUsername);
            mEmailText.setText(mEmail);
            //有头像则读取，无则为默认头像
            if(!mPortraitUrl.equals("")) {
                Picasso.with(getActivity()).load(Config.DEFAULT_SERVER_URL_LOGIN + mPortraitUrl).into(ivPortrait);//从Picasso缓存获取头像
            }
        }
    }


    @Override
    public void onResume() {
        if (!isGetData) {
            //   这里可以做网络请求或者需要的数据刷新操作
            AvgTime();
            TotalTime();
            TopThreeTime();
            TopThreeNum();
            isGetData = true;
        }
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        isGetData = false;
    }





    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_portrait:
                showChoosePicDialog();//修改头像
                break;
            case R.id.usercenter_btn_resetpwd://修改密码
                Intent intent_userCenter_to_resetPwd = new Intent(getActivity(), ResetPwd.class);
                intent_userCenter_to_resetPwd.putExtra("USER_NAME", mUsername);
                intent_userCenter_to_resetPwd.putExtra("mCookie", cookie);
                startActivity(intent_userCenter_to_resetPwd);
                break;
            case R.id.usercenter_btn_logoff://退出登录
                Delsession();//删除session
                //重置sp
                editor.putString("SP_USER_NAME", "");
                editor.putString("SP_EMAIL", "");
                editor.putString("SP_PORTRAITURL", "");
                editor.putString("cookie", "");
                editor.commit();
                Intent intent_usercenter_to_login = new Intent(getActivity(), Login.class);//跳转到登录界面
                startActivity(intent_usercenter_to_login);
                getActivity().finish();
                break;
        }
    }

    private void AvgTime() {
        //mUsernameTemp = getArguments().getString("USER_NAME");
        Gson gson=new Gson();
        //实例化TOTIME对象，在info.java文件
        TOTIME lg=new TOTIME(mUsernameTemp);
        String obj = gson.toJson(lg);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
        final UserCenterFragment.avgTime service = retrofit.create(UserCenterFragment.avgTime.class);
        retrofit2.Call<ResponseBody> call = service.getAvgTime(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result  = response.body().string();
                    JSONObject obj=new JSONObject(result);
                    String mTime = obj.getString("time");
                    double value = Double.valueOf(mTime.toString());
                    String sec=String.format("%.2f",value);
                    System.out.println("平均观看时长:" + sec);
                    TIme2.setText("平均观看时长：" + sec + "秒");
                } catch (Exception e) {
                    userCenterError();
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                userCenterError();
                t.printStackTrace();
            }
        });
    }

    private void TopThreeTime() {
        //mUsernameTemp = getArguments().getString("USER_NAME");
        Gson gson=new Gson();
        //实例化TOTIME对象，在info.java文件
        TOTIME lg=new TOTIME(mUsernameTemp);
        String obj = gson.toJson(lg);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
        final UserCenterFragment.topThreeTime service = retrofit.create(UserCenterFragment.topThreeTime.class);
        retrofit2.Call<ResponseBody> call = service.getTopThreeTime(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result  = response.body().string();
                    JSONObject arr = new JSONObject(result);
                    if(arr.length() == 0){

                    }
                    else {
                        Iterator<String> sIterator = arr.keys();
                        String key[] = new String[arr.length()];
                        int i = 0;
                        while (sIterator.hasNext()) {
                            // 获得key
                            String temp = sIterator.next();
                            key[i++] = temp;
                        }


//                    System.out.println("累计观看时长:" + mTime);
                        String result1="累计观看时长top3用户:";
                        for(int j=0;j<key.length;j++)
                            result1+="\n" + key[j];
                        TIme3.setText(result1);
                    }
                } catch (Exception e) {
                    userCenterError();
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                userCenterError();
                t.printStackTrace();
            }
        });
    }


    private void TopThreeNum() {
        //mUsernameTemp = getArguments().getString("USER_NAME");
        Gson gson=new Gson();
        //实例化TOTIME对象，在info.java文件
        TOTIME lg=new TOTIME(mUsernameTemp);
        String obj = gson.toJson(lg);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
        final UserCenterFragment.topThreeNum service = retrofit.create(UserCenterFragment.topThreeNum.class);
        retrofit2.Call<ResponseBody> call = service.getTopThreeNum(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result  = response.body().string();
                    JSONObject arr = new JSONObject(result);
                    if(arr.length() == 0){

                    }
                    else {
                        Iterator<String> sIterator = arr.keys();
                        String key[] = new String[arr.length()];
                        int i = 0;
                        while (sIterator.hasNext()) {
                            // 获得key
                            String temp = sIterator.next();
                            key[i++] = temp;
                        }


//                    System.out.println("累计观看时长:" + mTime);
                        String result1="累计观看时长top3用户:";
                        for(int j=0;j<key.length;j++)
                            result1+="\n" + key[j];
                        TIme4.setText(result1);
                    }
                } catch (Exception e) {
                    userCenterError();
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                userCenterError();
                t.printStackTrace();
            }
        });
    }
    private void TotalTime() {
        //mUsernameTemp = getArguments().getString("USER_NAME");
        Gson gson=new Gson();
        //实例化TOTIME对象，在info.java文件
        TOTIME lg=new TOTIME(mUsernameTemp);
        String obj = gson.toJson(lg);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
        final UserCenterFragment.totalTime service = retrofit.create(UserCenterFragment.totalTime.class);
        retrofit2.Call<ResponseBody> call = service.getTotalTime(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result  = response.body().string();
                    JSONObject obj=new JSONObject(result);
                    String mTime = obj.getString("time");
                    System.out.println("累计观看时长:" + mTime);
                    TIme1.setText("累计观看时长：" + mTime + "秒");
                } catch (Exception e) {
                    userCenterError();
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                userCenterError();
                t.printStackTrace();
            }
        });
    }

    //上传头像接口定义
    public interface LiveServiceUp {
        @Multipart
        @POST("/userCenter/uploadPortrait")
        Call<ResponseBody> uploadPortrait(@Part MultipartBody.Part file, @Part("username") RequestBody username, @Part("is_anchor") RequestBody is_anchor);
    }

    //获取用户信息接口定义
    public interface LiveServiceDown {
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/userCenter/getUserInfo")
        Call<ResponseBody> getUserInfo(@Body RequestBody info);
    }

    //删除session接口定义
    public interface DeletSession{
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/login/delSession")
        Call<ResponseBody> DeletSession(@Header("cookie") String cookie);
    }


    //avgTime接口定义
    public interface topThreeNum{
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/statistics/user/topThreeNum")
        Call<ResponseBody> getTopThreeNum(@Body RequestBody info);
    }
    public interface topThreeTime{
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/statistics/user/topThreeTime")
        Call<ResponseBody> getTopThreeTime(@Body RequestBody info);
    }
    public interface avgTime{
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/statistics/user/avgTime")
        Call<ResponseBody> getAvgTime(@Body RequestBody info);
    }

    //totalTime接口定义
    public interface totalTime{
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/statistics/user/totalTime")
        Call<ResponseBody> getTotalTime(@Body RequestBody info);
    }

    //删除session
    private void Delsession() {
        final UserCenterFragment.DeletSession service = retrofit.create(UserCenterFragment.DeletSession.class);
        retrofit2.Call<ResponseBody> call = service.DeletSession(cookie);
        try {
            Response<ResponseBody> response = call.execute();
            System.out.println(response.toString());
            String result = response.body().string();
            JSONObject obj1 = new JSONObject(result);
            String ret = obj1.getString("ret");
            if(ret.equals("0")){
                Toast.makeText(getActivity(), "退出失败！", Toast.LENGTH_SHORT).show();
            } else if (ret.equals("1")) {
                Toast.makeText(getActivity(), "退出成功！", Toast.LENGTH_SHORT).show();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //显示修改头像的对话框
    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("设置头像");
        String[] items = { "选择本地照片", "拍照" };
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 选择本地照片
                        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        openAlbumIntent.setType("image/*");
                        //用startActivityForResult方法，重写onActivityResult()方法，拿到图片进行裁剪操作
                        startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);//携带requestCode跳转到对应子Activity进行裁剪
                        break;
                    case TAKE_PICTURE: // 拍照
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // 指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                        tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "image.jpg"));
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);//携带uri的intent
                        startActivityForResult(openCameraIntent, TAKE_PICTURE);//携带requestCode跳转到对应子Activity进行裁剪
                        break;
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回码是可以用的
            //根据requestCode对应各子Activity
            switch (requestCode) {
                case TAKE_PICTURE:
                    startPhotoZoom(tempUri); // 开始对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData()); // 开始对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        Bitmap bitmap = extras.getParcelable("data");//裁剪过的图
                        String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());//获取系统时间以命名头像
                        urlpath = FileUtilcll.saveFile(getActivity(), mUsername + date + ".jpg", bitmap);//保存头像到本地并获取路径以上传
                        System.out.println("----------路径----------" + urlpath);
                        uploadPortrait(bitmap);//上传并显示新头像
                    }
                    break;
            }
        }
    }

    //裁剪图片方法实现
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");// 设置裁剪
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);//裁剪后上传显示
    }


    //上传并显示新头像
    private void uploadPortrait(final Bitmap bitmap) {
        //登录接口请求
        File file = new File(urlpath);//访问手机端的文件资源，保证手机端中必须有这个文件
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filebody = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        String username = mUsername;
        RequestBody usernamebody = RequestBody.create(MediaType.parse("multipart/form-data"),username);
        String is_anchor = "0";
        RequestBody is_anchorbody = RequestBody.create(MediaType.parse("multipart/form-data"),is_anchor);
        final UserCenterFragment.LiveServiceUp service = retrofit.create(UserCenterFragment.LiveServiceUp.class);
        retrofit2.Call<ResponseBody> call = service.uploadPortrait(filebody, usernamebody, is_anchorbody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result  = response.body().string();
                    JSONObject obj=new JSONObject(result);
                    String ret = obj.getString("ret");
                    System.out.println("result:" + ret);
                    if (!ret.equals("0")) {
                        mPortraitUrl = ret;
                        editor.putString("SP_PORTRAITURL", mPortraitUrl);//修改保存的url
                        editor.commit();
                        //Picasso.with(getActivity()).load(Config.DEFAULT_SERVER_URL_PORTRAIT + mPortraitUrl).into(ivPortrait);//从服务器获取头像
                        ivPortrait.setImageBitmap(bitmap);//直接加载bitmap
                        Toast.makeText(getActivity(), "头像上传成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "头像上传失败，请重新上传！", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    userCenterError();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                userCenterError();
                t.printStackTrace();
            }
        });

    }

    //网络异常情况
    private void userCenterError() {
        Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
    }
}