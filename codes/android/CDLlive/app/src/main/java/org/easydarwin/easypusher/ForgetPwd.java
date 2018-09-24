package org.easydarwin.easypusher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.easydarwin.config.Config;
import org.json.JSONObject;

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


public class ForgetPwd extends AppCompatActivity implements View.OnClickListener {
    private EditText mAccount;
    private EditText mEmail;
    private Button mSureButton;
    private Button mCancleButton;

    private Button mRegisterButton;
    private Retrofit retrofit;
    private Https https;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(this);
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mAccount = (EditText) findViewById(R.id.resetpwd_edit_name);
        mEmail = (EditText) findViewById(R.id.resetpwd_edit_email);


        mSureButton = (Button) findViewById(R.id.resetpwd_btn_sure);
        mCancleButton = (Button) findViewById(R.id.resetpwd_btn_cancel);

        mSureButton.setOnClickListener(this);
        mCancleButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resetpwd_btn_sure:
                forgetpwd();                                                /*确认按键触发部分*/
                break;
            case R.id.resetpwd_btn_cancel:
                finish();//取消按钮监听事件，由修改密码界面返回上个登录页面
                break;
        }
    }

    public interface PwdService {
        @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
        @POST("/login/forgetPwd")
        Call<ResponseBody> getLogin(@Body RequestBody info);
    }

    private void forgetpwd() {
        String username = mAccount.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
//        SharedPreferences.Editor editor = pwd_sp.edit();
        //切换Login Activity至User Activity

        if (username.length() > 0 && email.length() > 0) {
            if(!isValidEmail(email)){
                Toast.makeText(this, getString(R.string.email_format_error), Toast.LENGTH_SHORT).show();
            }
            else {
                Gson gson = new Gson();
                Forget info = new Forget(username, email);                     /*接口处理过程*/
                String obj = gson.toJson(info);
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), obj);
                final PwdService service = retrofit.create(ForgetPwd.PwdService.class);
                retrofit2.Call<ResponseBody> call = service.getLogin(body);
                //Call<ResponseBody> call = service.getLogin(account, password);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            String result = response.body().string();
                            //JSONArray jsonArray = new JSONArray(result);
                            JSONObject obj = new JSONObject(result);
                            String ret = obj.getString("ret");
                            System.out.println("result:" + ret);
                            if (ret.equals("1")) {
                                PwdSuccess();
                            }
                            if (ret.equals("0")) {
                                loginFail();                                /*根据接口返回值触发判断*/
                            }
                            if (ret.equals("2")) {
                                match();
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
            }
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

    private void PwdSuccess ()
    {
        String userName = mAccount.getText().toString();
        Toast.makeText(this, "验证成功",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ForgetPwd.this, ForgetPwd2.class);
        intent.putExtra("musername", userName);
        startActivity(intent);
        finish();
    }

    private void loginFail ()
    {
        Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show();
    }
    private void match ()
    {
        Toast.makeText(this, "用户信息不匹配", Toast.LENGTH_SHORT).show();
    }


    private void message_check ()
    {
        String account = mAccount.getText().toString().trim();
        String email = mEmail.getText().toString().trim();

        if (account.length() > 0 && email.length() > 0) {
            Toast.makeText(this, "信息审核正确，请修改密码。",
                    Toast.LENGTH_SHORT).show();

        } else Toast.makeText(this, "请填完整信息",
                Toast.LENGTH_SHORT).show();
    }

    //网络异常情况
    private void loginError()
    {
        Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
    }
}

