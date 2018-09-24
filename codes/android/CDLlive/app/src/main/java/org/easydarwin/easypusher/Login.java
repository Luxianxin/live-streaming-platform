
package org.easydarwin.easypusher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.easydarwin.config.Config;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public class Login extends Activity implements View.OnClickListener {
    private EditText mAccount;      //用户名编辑
    private EditText mPwd;          //密码编辑
    private Button mLoginButton;    //登录按钮
    private Button mRegisterButton;
    private TextView mChangepwdText;
    //private CheckBox mRememberCheck;//记住密码
    private SharedPreferences login_sp;
    private ImageView iv;
    private String UserName = "";
    private Retrofit retrofit;
    private Https https;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(this);
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mAccount = (EditText) findViewById(R.id.login_edit_account);
        mPwd = (EditText) findViewById(R.id.login_edit_pwd);
        mLoginButton = (Button) findViewById(R.id.login_btn_login);
        mLoginButton.setOnClickListener(this);
        mRegisterButton = (Button) findViewById(R.id.login_btn_register);
        mRegisterButton.setOnClickListener(this);
        mChangepwdText = (TextView) findViewById(R.id.login_text_change_pwd);
        mChangepwdText.setOnClickListener(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        iv=(ImageView) findViewById(R.id.logo);
        iv.setOnClickListener(this);
        //记住密码实现，调用getSharedPreferences类
        login_sp = getSharedPreferences("userInfo", 0);
        //mRememberCheck = (CheckBox) findViewById(R.id.login_remember);
     /*   boolean choseRemember = login_sp.getBoolean("mRememberCheck", false);*/
        final String cookie = login_sp.getString("cookie","");
        /*String name = login_sp.getString("USER_NAME", "");
        String pwd = login_sp.getString("PASSWORD", "");*/

        ImageView image = (ImageView) findViewById(R.id.logo);
        image.setImageResource(R.drawable.logo);
        if (cookie.length() > 0){
            new AsyncTask<Integer, Integer, Integer>() {

                @Override
                protected void onPreExecute() {

                }

                @Override
                protected Integer doInBackground(Integer... integers) {
                    check(cookie);
                    return null;
                }

                @Override
                protected void onPostExecute(Integer integer) {
                    AutoLogin();
                }
            }.execute();

        }

        /*//如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
        if(choseRemember) {
            mAccount.setText(name);
            mPwd.setText(pwd);
            mRememberCheck.setChecked(true);
        }*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn_register:
                //登录界面的注册按钮
                Intent intent_login_to_register = new Intent(Login.this, Register.class);
                mAccount.setText("");
                mPwd.setText("");
                startActivity(intent_login_to_register);
                break;
            case R.id.login_btn_login:
                login();
                break;
            case R.id.login_text_change_pwd:
                Intent intent_login_to_reset = new Intent(Login.this, ForgetPwd.class);
                startActivityForResult(intent_login_to_reset, 1);
                mAccount.setText("");
                mPwd.setText("");
                break;
        }
    }

    //retrofit 定义
    public interface LiveService {
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("login/login")
        Call<ResponseBody> getLogin(@Body RequestBody info);
    }
    public interface CheckService{
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("login/session")
        Call<ResponseBody> CheckService(@Header("cookie") String cookie,@Body RequestBody info);
    }

    private void check(String cookie)
    {
        int is_anchor = 0;
        Gson gson=new Gson();
        //实例化login1对象，在info.java文件
        Check1 check=new Check1(is_anchor);
        String obj = gson.toJson(check);

        //登录接口请求
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
        final CheckService service = retrofit.create(CheckService.class);
        retrofit2.Call<ResponseBody> call = service.CheckService(cookie,body);
        try {
            Response<ResponseBody> response = call.execute();
            System.out.println(response.toString());
            String result = response.body().string();
            JSONObject obj1 = new JSONObject(result);
            String ret = obj1.getString("userName");
            UserName = ret;
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void AutoLogin(){
        if(UserName.equals("0")){
            Toast.makeText(this,"自动登录失败",Toast.LENGTH_SHORT).show();
        } else if(UserName.equals("")) {
            Toast.makeText(this,"网络异常",Toast.LENGTH_SHORT).show();
        } else {
            Intent intent_login_to_bottomtab = new Intent(Login.this, BottomTab.class);
            intent_login_to_bottomtab.putExtra("USER_NAME", UserName);
            String cookie = login_sp.getString("cookie","");
            intent_login_to_bottomtab.putExtra("mCookie", cookie);
            startActivity(intent_login_to_bottomtab);
            finish();
        }
    }

    //登录事件
    private void login()
    {
        final String account=mAccount.getText().toString().trim();
        String password=mPwd.getText().toString().trim();
       /* SharedPreferences.Editor editor = login_sp.edit();*/
        //登录接口访问
        if(account.length()>0 && mPwd.length()>0) {
            Gson gson=new Gson();
            //实例化login1对象，在info.java文件
            Login1 lg=new Login1(account,md5(password));
            String obj = gson.toJson(lg);

            //登录接口请求
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
            final LiveService service = retrofit.create(LiveService.class);
            retrofit2.Call<ResponseBody> call = service.getLogin(body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(
                        retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String result  = response.headers().toString();
                        String bd = response.body().string();
                        JSONObject obj = new JSONObject(bd);
                        String bd1 = obj.getString("ret");
                        System.out.println("result:" + bd1);
                        if (bd1.equals("1")) {
                            if(result.length() != 0) {
                                String ret="";
                                String arr[] = result.split("\n");
                                for(int i=0;i<arr.length;i++)
                                    if(arr[i].substring(0,arr[i].indexOf(':')).equals("Set-Cookie"))
                                        ret = arr[i];
                           if(ret.length()>0)
                               ret=ret.substring(ret.indexOf(":")+1);
                            saveCookie(ret);
                            System.out.println("result:" + ret);
                            }
                            loginSuccess(account);
                        } else if(bd1.equals("2")){
                            loginFail("2");
                        } else {
                            loginFail("0");
                        }
                    } catch (Exception e) {
                        loginError();
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loginError();
                    t.printStackTrace();
                }
            });
            /*//选择记住密码，app保留账户信息
            String userName = mAccount.getText().toString();
            String userPwd = mPwd.getText().toString();
            editor.putString("USER_NAME", userName);
            editor.putString("PASSWORD", userPwd);
            //是否记住密码
            if (mRememberCheck.isChecked()) {
                editor.putBoolean("mRememberCheck", true);
            } else {
                editor.putBoolean("mRememberCheck", false);
            }
            editor.commit();*/
        } else {
            Toast.makeText(this,"请填完整信息",Toast.LENGTH_SHORT).show();
        }
    }

    public void saveCookie(String cookie) {
        SharedPreferences.Editor editor=login_sp.edit();
        editor.putString("cookie", cookie);
        editor.commit();
    }

    private void loginSuccess(String UserName) {
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        Intent intent_login_to_bottomtab = new Intent(Login.this, BottomTab.class);
        intent_login_to_bottomtab.putExtra("USER_NAME", UserName);
        String cookie = login_sp.getString("cookie","");
        intent_login_to_bottomtab.putExtra("mCookie", cookie);
        startActivity(intent_login_to_bottomtab);
        finish();
    }

    private void loginFail(String flag) {
        if(flag.equals("2")) {
            Toast.makeText(this, "账号或密码错误",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "用户不存在",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //网络异常情况
    private void loginError() {
        Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
    }

    //MD5加密算法
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
