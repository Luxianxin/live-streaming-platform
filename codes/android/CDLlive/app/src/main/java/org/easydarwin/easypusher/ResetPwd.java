package org.easydarwin.easypusher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.easydarwin.Fragments.UserCenterFragment;
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

public class ResetPwd extends AppCompatActivity implements View.OnClickListener{


    private EditText mPwd_old;
    private EditText mPwd_new;
    private EditText mPwd_check;
    private Button mSureButton;
    private Button mCancleButton;
    private String userName = "";
    private String cookie;
    private SharedPreferences resetpwd_sp;//记录cookie
    private SharedPreferences.Editor editor;
    private Retrofit retrofit;
    private Https https;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpwd);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(this);
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mPwd_old = (EditText) findViewById(R.id.resetpwd_edit_pwd_old);
        mPwd_new = (EditText) findViewById(R.id.resetpwd_edit_pwd_new);
        mPwd_check = (EditText) findViewById(R.id.resetpwd_edit_pwd_check);

        mSureButton = (Button) findViewById(R.id.resetpwd_btn_sure);
        mCancleButton = (Button) findViewById(R.id.resetpwd_btn_cancel);

        mSureButton.setOnClickListener(this);
        mCancleButton.setOnClickListener(this);

        userName = (String) getIntent().getSerializableExtra("USER_NAME");
        cookie = (String) getIntent().getSerializableExtra("mCookie");
        resetpwd_sp = getSharedPreferences("userInfo", 0);
        editor = resetpwd_sp.edit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resetpwd_btn_sure:
                resetpwd_check();
                break;
            case R.id.resetpwd_btn_cancel:
                //取消按钮监听事件，由修改密码页面返回用户中心页面
                /*Intent intent_resetpwd_to_login = new Intent(ResetPwd.this,
                        Login.class);
                startActivity(intent_resetpwd_to_login);*/
                finish();
                break;
        }
    }

    private void resetpwd_check() {

        if(mPwd_check.getText().length() != 0 && mPwd_new.length() != 0 && mPwd_old.length() != 0)
        {
            if (mPwd_new.getText().toString().equals(mPwd_check.getText().toString())) {
                resetpwd();

            } else {
                Toast.makeText(this, "两次新密码不一致！", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
                Toast.makeText(this, "  请将信息填写完整", Toast.LENGTH_SHORT).show();
        }
    }

    public interface LiveService{
        @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
        @POST("/login/changePwd")
        Call<ResponseBody> getRestPwd(@Body RequestBody info);
    }

    public interface DeletSession{
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/login/delSession")
        Call<ResponseBody> DeletSession(@Header("cookie") String cookie);
    }

    private void resetpwd(){
        String pwdOld = mPwd_old.getText().toString().trim();
        String pwdNew = mPwd_new.getText().toString().trim();

        //接口访问
        Gson gson = new Gson();
        //实例化login1对象，在info.java文件
        ResetPwd1 lg = new ResetPwd1(userName, md5(pwdOld), md5(pwdNew));
        String obj = gson.toJson(lg);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),obj);
        final ResetPwd.LiveService service = retrofit.create(ResetPwd.LiveService.class);
        Call<ResponseBody> call = service.getRestPwd(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result  = response.body().string();
                    JSONObject obj = new JSONObject(result);
                    String ret = obj.getString("ret");
                    System.out.println("result:" + ret);
                    if (ret.equals("1")) {
                        Toast.makeText(ResetPwd.this, "密码重置成功，请重新登录！", Toast.LENGTH_SHORT).show();
                        Delsession();
                        editor.putString("cookie", "");
                        editor.commit();
                        Intent intent_resetpwd_to_login = new Intent(ResetPwd.this, Login.class);
                        intent_resetpwd_to_login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent_resetpwd_to_login);
                        finish();
                    } else if(ret.equals("2")) {
                        Toast.makeText(ResetPwd.this, "密码错误！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetPwd.this, "修改失败！", Toast.LENGTH_SHORT).show();
                    };
                } catch (Exception e) {
                    resetPwdError();
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                resetPwdError();
                t.printStackTrace();
            }
        });

    }

    private void Delsession() {
        final ResetPwd.DeletSession service = retrofit.create(ResetPwd.DeletSession.class);
        retrofit2.Call<ResponseBody> call = service.DeletSession(cookie);

        try {
            Response<ResponseBody> response = call.execute();
            System.out.println(response.toString());
            String result = response.body().string();
            JSONObject obj1 = new JSONObject(result);
            String ret = obj1.getString("ret");
            /*if(ret.equals("0")){
                Toast.makeText(this, "删除失败！", Toast.LENGTH_SHORT).show();
            }
            else if (ret.equals("1")){
                Toast.makeText(this, "删除成功！", Toast.LENGTH_SHORT).show();
            }*/

        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //网络异常情况
    private void resetPwdError() {
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
