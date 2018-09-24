package org.easydarwin.Fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.easydarwin.config.Config;
import org.easydarwin.easypusher.Https;
import org.easydarwin.easypusher.R;
import org.easydarwin.easypusher.StreamActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

/*import android.support.v4.app.Fragment;*/


public class HomeFragment extends Fragment implements View.OnClickListener{
    private EditText mRoomname;
    private Button btn_start;
    private Retrofit retrofit;
    private Https https;

    public HomeFragment() { }

    //利用此构造函数在登录页面向此Fragment传递用户名参数
    public static HomeFragment newInstance(String userName) {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_NAME", userName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //super.onCreate(savedInstanceState);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(getActivity());
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        mRoomname = (EditText) view.findViewById(R.id.et_title);
        btn_start =view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        //Begin();
        Intent it=new Intent(getActivity(), StreamActivity.class);
        String UserName = getArguments().getString("USER_NAME");
        it.putExtra("musername",UserName);
        mRoomname.setText("");
        startActivity(it);
    }


    public interface LiveService {
        @Headers({"Content-Type: application/json","Accept: application/json"})//json
        @POST("/streaming/anchor/start")
        Call<ResponseBody> getLogin(@Body RequestBody info);
    }

    private void Begin() {
        String roomname = mRoomname.getText().toString().trim();

        //设置值
        final String UserName = getArguments().getString("USER_NAME");

        //切换Login Activity至User Activity
        //登录接口访问
        Gson gson = new Gson();
        //实例化login1对象，在info.java文件
        Begin info = new Begin(UserName, roomname);
        String obj = gson.toJson(info);


        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), obj);
        final LiveService service = retrofit.create(LiveService.class);

        retrofit2.Call<ResponseBody> call = service.getLogin(body);

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
                        Intent intent = new Intent(getActivity(), StreamActivity.class);
                        intent.putExtra("musername", UserName);
                        startActivity(intent);

                    } else {

                        Toast.makeText(getActivity(),"进入失败", Toast.LENGTH_LONG);
                        return;


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
