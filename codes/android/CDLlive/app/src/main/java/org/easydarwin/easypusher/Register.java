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
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class Register extends AppCompatActivity implements View.OnClickListener{
    private EditText mAccount;                      //用户名编辑
    private EditText mPwd;                          //密码编辑
    private EditText mPwdCheck;                     //密码确定检查
    private EditText mEmail;
    private Button mSureButton;                     //确定按钮
    //private Button mCancleButton;                 //取消按钮
    private Button mBackLogin;                      //返回登录按钮
    private Retrofit retrofit;
    private Https https;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(this);
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mAccount = (EditText) findViewById(R.id.resetpwd_edit_name);
        mPwd = (EditText) findViewById(R.id.resetpwd_edit_pwd);
        mPwdCheck = (EditText) findViewById(R.id.resetpwd_edit_pwd_check);
        mEmail = (EditText) findViewById(R.id.resetpwd_edit_email);

        mSureButton = (Button) findViewById(R.id.register_btn_sure);
        //mCancleButton = (Button) findViewById(R.id.register_btn_cancel);
        mBackLogin = findViewById(R.id.register_btn_login);

        mSureButton.setOnClickListener(this);
        //mCancleButton.setOnClickListener(this);
        mBackLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_btn_sure:
                //确认按钮监听事件
                register_check();
                break;
            /*case R.id.register_btn_cancel:
                //取消按钮监听事件，由注册页面返回登陆页面
                Intent intent_register_to_login = new Intent(Register.this,
                        Login.class);
                startActivity(intent_register_to_login);
                finish();
                break;*/
            case R.id.register_btn_login:
                //左上角返回登录按钮监听事件，由注册页面返回登陆页面
                Intent intent_reg_to_login = new Intent(Register.this, Login.class);
                startActivity(intent_reg_to_login);
                finish();
                break;
            default:
                break;
        }
    }

    //retrofit定义
    public interface LiveService {
        @Headers({"Content-Type: application/json","Accept: application/json"})//需要添加头
        @POST("login/register")

        Call<ResponseBody> getRegister(@Body RequestBody info);
    }

    //确认按钮的监听事件
    private void register_check() {
        String account = mAccount.getText().toString().trim();
        String password = mPwd.getText().toString().trim();
        String PwdCheck = mPwdCheck.getText().toString().trim();
        String email = mEmail.getText().toString().trim();

		
            if(account.length()>0 && password.length()>0 && PwdCheck.length()>0 && email.length()>0)
            {
                //检测两次输入的密码是否一致
                if (password.equals(PwdCheck) == false) {
                    Toast.makeText(this, getString(R.string.pwd_not_the_same), Toast.LENGTH_SHORT).show();
                }
                else {
                    //检测输入邮箱格式是否正确
                    if(!isValidEmail(email)){
                        Toast.makeText(this, getString(R.string.email_format_error), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //注册接口访问
                        Gson gson=new Gson();
                        Register1 info=new Register1(account, md5(password), email);

                        String obj = gson.toJson(info);

                        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), obj);
                        final Register.LiveService service = retrofit.create(Register.LiveService.class);
                        retrofit2.Call<ResponseBody> call = service.getRegister(body);

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(
                                    retrofit2.Call<ResponseBody> call, Response<ResponseBody> response)
                            {
                                try {
                                    String result  = response.body().string();
                                    //JSONArray jsonArray = new JSONArray(result);
                                    JSONObject obj=new JSONObject(result);
                                    String ret = obj.getString("ret");
                                    System.out.println("result:" + ret);
                                    if (ret.equals("1")) {
                                        registerSuccess();
                                    } else {
                                        registerFail();
                                    }
                                } catch (Exception e)
                                {
                                    registerError();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                registerError();
                                t.printStackTrace();
                            }
                        });
                    }
                }
            }
            else
            {
                Toast.makeText(this, "请填完整信息！", Toast.LENGTH_SHORT).show();
            }
        }

    //验证邮箱格式是否正确
    private boolean isValidEmail(String email) {
        // 1、\\w+表示@之前至少要输入一个匹配字母或数字或下划线 \\w 单词字符：[a-zA-Z_0-9]
        // 2、(\\w+\\.)表示域名. 如新浪邮箱域名是sina.com.cn
        // {1,3}表示可以出现一次或两次或者三次.
        String reg = "\\w+@(\\w+\\.){1,3}\\w+";
        Pattern pattern = Pattern.compile(reg);
        boolean flag = false;
        if (email != null) {
            Matcher matcher = pattern.matcher(email);
            flag = matcher.matches();
        }
        return flag;
    }

    //注册成功
    private void registerSuccess()
    {
        Toast.makeText(this, "注册成功，请登录！", Toast.LENGTH_SHORT).show();
        //清空注册页面信息
        mAccount.setText("");
        mPwd.setText("");
        mPwdCheck.setText("");
        mEmail.setText("");
        Intent intent = new Intent(Register.this, Login.class);//注册成功后跳转登录页面
        startActivity(intent);
        finish();
    }

    //注册失败
    private void registerFail()
    {
        Toast.makeText(this, "用户名已存在，请重新输入！", Toast.LENGTH_SHORT).show();
    }

    //网络异常情况
    private void registerError()
    {
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
