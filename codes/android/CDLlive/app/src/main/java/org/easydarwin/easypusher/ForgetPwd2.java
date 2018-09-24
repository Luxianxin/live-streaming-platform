package org.easydarwin.easypusher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class ForgetPwd2 extends AppCompatActivity implements View.OnClickListener{
    private EditText mPwd_new;
    private EditText mPwd_check;
    private Button mSureButton;
    private Button mCancleButton;
    private Retrofit retrofit;
    private Https https;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd2);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(this);
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mPwd_check = (EditText) findViewById(R.id.resetpwd_edit_pwd_check);
        mPwd_new = (EditText) findViewById(R.id.resetpwd_edit_pwd_new);

        mSureButton = (Button) findViewById(R.id.resetpwd_btn_sure);
        mCancleButton = (Button) findViewById(R.id.resetpwd_btn_cancel);

        mSureButton.setOnClickListener(this);
        mCancleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resetpwd_btn_sure:                              /*按键触发的事件*/
                resetpwd_check();
                break;
            case R.id.resetpwd_btn_cancel:
                finish();//取消按钮监听事件，返回上个页面
                break;
        }
    }

    //确认按钮的监听事件
    private void resetpwd_check() {                                        /*判断两次密码是否一致*/
        String userPwd_new = mPwd_new.getText().toString().trim();
        String userPwdCheck = mPwd_check.getText().toString().trim();
        if (userPwd_new.length() > 0 && userPwdCheck.length() > 0) {
            if (userPwd_new.equals(userPwdCheck) == false) {
                //两次输入的新密码不一致
                Toast.makeText(this, getString(R.string.pwd_not_the_same), Toast.LENGTH_SHORT).show();
                return;
            } else {
                login();
                Toast.makeText(this, getString(R.string.resetpwd_success), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "请填完整信息",
                    Toast.LENGTH_SHORT).show();
        }
    }
    String result;
    public interface PwdService{
        @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
        @POST("/login/resetPwd")
        Call<ResponseBody> getLogin(@Body RequestBody info);
    }

    private void login()
    {
        String password=mPwd_new.getText().toString().trim();
        String mPwdcheck=mPwd_check.getText().toString().trim();
        // 获取意图对象
        Intent getIntent = getIntent();
        //获取上一忘记密码页面传递username的值
        String username = getIntent.getStringExtra("musername");
        //设置值
        System.out.println("result:"+result);

        if (username.length() > 0 && password.length() > 0) {
            Gson gson = new Gson();
            Forget2 info = new Forget2(username, md5(password));
            String obj = gson.toJson(info);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), obj);
            final ForgetPwd2.PwdService service = retrofit.create(ForgetPwd2.PwdService.class);
            retrofit2.Call<ResponseBody> call = service.getLogin(body);
            //Call<ResponseBody> call = service.getLogin(account, password);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(
                        retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String result = response.body().string();
                        JSONObject obj = new JSONObject(result);
                        String ret = obj.getString("ret");
                        System.out.println("result:" + ret);
                        if (ret.equals("1")) {
                            System.out.println("result:" + ret);
                            PwdSuccess();
                        } else {                                       /*处理接口收发过程*/
                            loginFail();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    private void PwdSuccess() {
        String password=mPwd_new.getText().toString();
        Toast.makeText(this, "重置成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ForgetPwd2.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void loginFail() {
        Toast.makeText(this, "重置失败", Toast.LENGTH_SHORT).show();
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

