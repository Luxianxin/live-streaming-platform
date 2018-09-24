/*
	Copyright (c) 2013-2016 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/
package org.easydarwin.easypusher;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.squareup.otto.Subscribe;

import org.easydarwin.bus.StreamStat;
import org.easydarwin.config.Config;
import org.easydarwin.easyrtmp.push.EasyRTMP;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.MediaStream;
//import org.easydarwin.update.UpdateMgr;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdlp.Cdlp;
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

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.easydarwin.easypusher.EasyApplication.BUS;
import static org.easydarwin.easypusher.SettingActivity.REQUEST_OVERLAY_PERMISSION;
//import static org.easydarwin.update.UpdateMgr.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;
public class StreamActivity extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {
    AlertDialog.Builder DialogBuilder;
    static final String TAG = "EasyPusher";
    public static final int REQUEST_MEDIA_PROJECTION = 1002;
    public static final int REQUEST_CAMERA_PERMISSION = 1003;
    public static final int REQUEST_STORAGE_PERMISSION = 1004;

    //默认分辨率
    int width = 640, height = 480;
    //Button btnSwitch;
    //TextView txtStreamAddress;
    ImageButton btnSwitchCemera;
    Spinner spnResolution;
    List<String> listResolution = new ArrayList<String>();
    MediaStream mMediaStream;
    TextView txtStatus, streamStat;
    static Intent mResultIntent;
    static int mResultCode;
    //private UpdateMgr update;
    TextView textRecordTick;
    private BackgroundCameraService mService;
    private ServiceConnection conn;

    Socket client = null;
    private EditText name, input_text;
    private Button send,startLive;
    private Cdlp.CdlpMessage mes;
    private Cdlp.CdlpMessage mes1;
    private ListView itemlist = null;
    String LiverName,UserName;
    ImageView imgA;
    private List<Map<String, Object>> list;
    private SimpleAdapter adapter;
    private Retrofit retrofit;
    private Https https;

    private Runnable mRecordTickRunnable = new Runnable() {
        @Override
        public void run() {
            long duration = System.currentTimeMillis() - EasyApplication.getEasyApplication().mRecordingBegin;
            duration /= 1000;
            textRecordTick.setText(String.format("%02d:%02d", duration / 60, (duration) % 60));
            if (duration % 2 == 0) {
                textRecordTick.setCompoundDrawablesWithIntrinsicBounds(R.drawable.recording_marker_shape, 0, 0, 0);
            } else {
                textRecordTick.setCompoundDrawablesWithIntrinsicBounds(R.drawable.recording_marker_interval_shape, 0, 0, 0);
            }

            textRecordTick.removeCallbacks(this);
            textRecordTick.postDelayed(this, 1000);
        }
    };
    private boolean mNeedGrantedPermission;

    public interface QuitService {
        @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
        @POST("/streaming/anchor/end")
        Call<ResponseBody> getLogin(@Body RequestBody info);
    }


    Handler myHandler1 = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if (msg.what == 123) {
                if(msg.getData().getString("Type").equals("0"))
                {
                    String username=msg.getData().getString("UserName");
                    //String chatinfo="asgsfg";
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("chatportrait",imgA);
                    map.put("chatinfo", username+"退出"+LiverName+"的直播间啦");
                    map.put("username",username);
                    list.add(map);
                    adapter.notifyDataSetChanged();//当有消息时刷新

                    input_text.setText("");//清空输入框的内容
                }
                else if(msg.getData().getString("Chat").length()!=0)
                {
                    String chatinfo=msg.getData().getString("Chat");
                    String username=msg.getData().getString("UserName");
                    //String chatinfo="asgsfg";
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("chatportrait",imgA);
                    map.put("chatinfo", chatinfo);
                    map.put("username",username);
                    list.add(map);
                    adapter.notifyDataSetChanged();//当有消息时刷新

                    input_text.setText("");//清空输入框的内容
                }
            }

        };
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化Retrofit，通过 Retrofit.Builder 来创建一个retrofit客户端
        https = new Https(this);
        retrofit = new Retrofit.Builder()
                .baseUrl(Config.DEFAULT_SERVER_URL_LOGIN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(https.provideOkHttpClient())
                .build();

        BUS.register(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);
            mNeedGrantedPermission = true;
            return;
        } else {
            // resume..
        }

        initDialog();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Intent getIntent = getIntent();
        //获取上一忘记密码页面传递username的值
        String username = getIntent.getStringExtra("musername");
        LiverName=username;
        UserName=username;
        imgA=(ImageView)findViewById(R.id.imgPortraitA);
        send = (Button) findViewById(R.id.send);
        startLive = (Button) findViewById(R.id.startLive);
        input_text = (EditText) findViewById(R.id.input_text);
        itemlist = (ListView) findViewById(R.id.list_message);
        refreshListItems();
        send.setOnClickListener(this);
        startLive.setOnClickListener(this);


        //创建房间
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {

                    System.out.println("main");
                    client = new Socket(Config.DEFAULT_SERVER_URL_Chat, 8001);// 网络访问最好放在线程中
                    Cdlp.CdlpMessage.Builder builder1 = Cdlp.CdlpMessage.newBuilder();
                    mes1 = builder1.setChat(UserName+"进入"+LiverName+"的直播间啦")
                            .setUserName(UserName)
                            .setLevel(1)
                            .setType(1)
                            .setAnchorName(LiverName)
                            .build();

                    try {
                        mes1.writeTo(client.getOutputStream());
                        //mes.writeDelimitedTo(client.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        OutputStream os = client.getOutputStream();
                        os.write(("\n").getBytes("utf-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    InputStream inputStream = client.getInputStream();
                    //InputStreamReader ir=new InputStreamReader(client.getInputStream());
                    byte buffer[] = new byte[1024];
                    while (true)
                    {

                        int count = inputStream.read(buffer);
                        //ir.close();
                        if (count>0)
                        {
                            //inputStream.close();
                            byte[] temp = new byte[count];
                            for (int i = 0; i < count; i++) {
                                temp[i] = buffer[i];
                            }
                            sendMessage1(temp);
                        }
                    }
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        }).start();


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startLive: {
                Intent getIntent = getIntent();
            //获取上一忘记密码页面传递username的值
           String username = getIntent.getStringExtra("musername");
                Play(username);
                if(startLive.getText().equals("开始直播"))
                    startLive.setText("结束直播");
                else startLive.setText("开始直播");
            }
            break;
            //case R.id.btn_switch:

            //    break;
//            case R.id.btn_setting:
//                startActivity(new Intent(this, SettingActivity.class));
//                break;
            case R.id.sv_surfaceview:
                try {
                    mMediaStream.getCamera().autoFocus(null);
                } catch (Exception e) {
                }
                break;
            case R.id.btn_switchCamera: {
                mMediaStream.switchCamera();
            }
            break;
            case R.id.send:
                //发送消息
                String s2 = input_text.getText().toString().trim().replace("\n","");
                input_text.setText("");
                System.out.println(s2);
                if (s2.length()>0&&client != null) {
                    Cdlp.CdlpMessage.Builder builder = Cdlp.CdlpMessage.newBuilder();
                    mes = builder.setType(2)
                            .setChat(s2)
                            .setUserName(UserName)
                            .setLevel(1)
                            .setAnchorName(LiverName)
                            .build();
                    try {
                        mes.writeTo(client.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        OutputStream os = client.getOutputStream();
                        os.write(("\n").getBytes("utf-8"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            //确认退出初始化
            DialogBuilder.show();
           return true;
        }

        return super.onKeyUp(keyCode, event);
    }


    //聊天室界面模块
    private void refreshListItems() {

        list = buildListForSimpleAdapter();
        //实例适配器
        adapter = new SimpleAdapter(this, list, R.layout.chata,
                new String[] {"chatportrait","chatinfo","username"}, new int[] {R.id.imgPortraitA,R.id.txvInfo,R.id.user_name});
        itemlist.setAdapter(adapter);
        itemlist.setSelection(0);
    }

    //用来实例化列表容器的函数
    private List<Map<String, Object>> buildListForSimpleAdapter()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(2);

        //向列表容器中添加数据（每列中包括一个头像和聊天信息）
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("chatportrait",imgA);
        map.put("chatinfo", "---欢迎进入"+UserName+"的房间---");
        map.put("username",UserName);
        list.add(map);

        return list;
    }




    //聊天室通信模块

    private  void sendMessage1(byte[] temp)
    {
        Cdlp.CdlpMessage mes = null;
        try {
            mes = Cdlp.CdlpMessage.parseFrom(temp);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        Bundle bundle = new Bundle();
        bundle.putString("AnchorName", mes.getAnchorName());
        bundle.putString("Chat", mes.getChat());
        bundle.putString("UserName", mes.getUserName());
        bundle.putString("Level", String.valueOf(mes.getLevel()));
        bundle.putString("Type", String.valueOf(mes.getType()));
        Message msg = new Message();
        msg.setData(bundle);
        msg.what = 123;
        myHandler1.sendMessage(msg);
    }


    /**
     * 初始化好确认退出的对话框
     */
    private void initDialog() {

        DialogBuilder = new AlertDialog.Builder(this).setIcon(android.R.drawable.btn_star)
                .setTitle("退出").setMessage("确定要退出直播吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                        if (client != null) {
                            Cdlp.CdlpMessage.Builder builder = Cdlp.CdlpMessage.newBuilder();
                            mes = builder.setType(0)
                                    .setChat("主播退出房间啦")
                                    .setUserName(UserName)
                                    .setLevel(1)
                                    .setAnchorName(LiverName)
                                    .build();

                            try {
                                mes.writeTo(client.getOutputStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                OutputStream os = client.getOutputStream();
                                os.write(("\n").getBytes("utf-8"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                client.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        finish();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }
















    private void goonWithPermissionGranted() {
        spnResolution = (Spinner) findViewById(R.id.spn_resolution);
        streamStat = (TextView) findViewById(R.id.stream_stat);
        streamStat.setText(null);
        txtStatus = (TextView) findViewById(R.id.txt_stream_status);
        //btnSwitch = (Button) findViewById(R.id.btn_switch);
        //btnSwitch.setOnClickListener(this);
//        btnSetting = (Button) findViewById(R.id.btn_setting);
//        btnSetting.setOnClickListener(this);
        btnSwitchCemera = (ImageButton) findViewById(R.id.btn_switchCamera);
        btnSwitchCemera.setOnClickListener(this);
        //txtStreamAddress = (TextView) findViewById(R.id.txt_stream_address);
        textRecordTick = (TextView) findViewById(R.id.tv_start_record);
        final TextureView surfaceView = (TextureView) findViewById(R.id.sv_surfaceview);
        surfaceView.setSurfaceTextureListener(this);

        surfaceView.setOnClickListener(this);


        Button pushScreen = (Button) findViewById(R.id.push_screen);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            pushScreen.setVisibility(View.GONE);
        }

        Button button = (Button) findViewById(R.id.push_screen);
        if (RecordService.mEasyPusher != null) {
            button.setText("停止推送屏幕");
            //TextView viewById = (TextView) findViewById(R.id.push_screen_url);
            //viewById.setText(EasyApplication.getEasyApplication().getUrl() + "_s");
        }

        /*String url = "http://www.easydarwin.org/versions/easyrtmp/version.txt";

        update = new UpdateMgr(this);
        update.checkUpdate(url);*/


        // create background service for background use.
        Intent intent = new Intent(this, BackgroundCameraService.class);
        startService(intent);

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((BackgroundCameraService.LocalBinder) iBinder).getService();
                if (surfaceView.isAvailable()) {
                    goonWithAvailableTexture(surfaceView.getSurfaceTexture());
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        bindService(new Intent(this, BackgroundCameraService.class), conn, 0);



        if (EasyApplication.getEasyApplication().mRecording) {
            textRecordTick.setVisibility(View.VISIBLE);

            textRecordTick.removeCallbacks(mRecordTickRunnable);
            textRecordTick.post(mRecordTickRunnable);
        } else {
            textRecordTick.setVisibility(View.GONE);
            textRecordTick.removeCallbacks(mRecordTickRunnable);
        }

    }


    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    update.doDownload();
                }
                break;
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mNeedGrantedPermission = false;
                    goonWithPermissionGranted();

                } else {
                    finish();
                }
                break;
            }
        }
    }*/


    private void startScreenPushIntent() {
        if (StreamActivity.mResultIntent != null && StreamActivity.mResultCode != 0) {
            Intent intent = new Intent(getApplicationContext(), RecordService.class);
            startService(intent);
            //TextView viewById = (TextView) findViewById(R.id.push_screen_url);

            //viewById.setText(EasyApplication.getEasyApplication().getUrl() + "_s");
            Button button = (Button) findViewById(R.id.push_screen);
            button.setText("停止推送屏幕");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MediaProjectionManager mMpMngr = (MediaProjectionManager) getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mMpMngr.createScreenCaptureIntent(), StreamActivity.REQUEST_MEDIA_PROJECTION);
            }
        }
    }

    public void onPushScreen(final View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            new AlertDialog.Builder(this).setMessage("推送屏幕需要安卓5.0以上,您当前系统版本过低,不支持该功能。").setTitle("抱歉").show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {

                new AlertDialog.Builder(this).setMessage("推送屏幕需要APP出现在顶部.是否确定?").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                    }
                }).setNegativeButton(android.R.string.cancel,null).setCancelable(false).show();
                return;
            }
        }

        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("alert_screen_background_pushing", false)) {
            new AlertDialog.Builder(this).setTitle("提醒").setMessage("屏幕直播将要开始,直播过程中您可以切换到其它屏幕。不过记得直播结束后,再进来停止直播哦!").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PreferenceManager.getDefaultSharedPreferences(StreamActivity.this).edit().putBoolean("alert_screen_background_pushing", true).apply();
                    onPushScreen(view);
                }
            }).show();
            return;
        }
        Button button = (Button) findViewById(R.id.push_screen);
        if (RecordService.mEasyPusher != null) {
            Intent intent = new Intent(getApplicationContext(), RecordService.class);
            stopService(intent);

          //  TextView viewById = (TextView) findViewById(R.id.push_screen_url);
           // viewById.setText(null);
            button.setText("推送屏幕");
        } else {
            startScreenPushIntent();
        }
    }


    private static final String STATE = "state";
    private static final int MSG_STATE = 1;

    private void sendMessage(String message) {
        Message msg = Message.obtain();
        msg.what = MSG_STATE;
        Bundle bundle = new Bundle();
        bundle.putString(STATE, message);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STATE:
                    String state = msg.getData().getString("state");
                    txtStatus.setText(state);
                    break;
            }
        }
    };

    private void initSpninner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spn_item, listResolution);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnResolution.setAdapter(adapter);
        int position = listResolution.indexOf(String.format("%dx%d", width, height));
        spnResolution.setSelection(position, false);
        spnResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mMediaStream != null && mMediaStream.isStreaming()) {
                    int pos = listResolution.indexOf(String.format("%dx%d", width, height));
                    if (pos == position) return;
                    spnResolution.setSelection(pos, false);
                    Toast.makeText(StreamActivity.this, "正在推送中,无法切换分辨率", Toast.LENGTH_SHORT).show();
                    return;
                }
                String r = listResolution.get(position);
                String[] splitR = r.split("x");

                int wh = Integer.parseInt(splitR[0]);
                int ht = Integer.parseInt(splitR[1]);
                if (width != wh || height != ht) {
                    width = wh;
                    height = ht;
                    /*if (mMediaStream != null) {
                        mMediaStream.updateResolution(width, height);
                    }*/
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void startCamera() {
        //mMediaStream.updateResolution(width, height);
        mMediaStream.setDgree(getDgree());
        mMediaStream.createCamera();
        mMediaStream.startPreview();

        if (mMediaStream.isStreaming()) {
            sendMessage("直播中");
            //btnSwitch.setText("停止");
            //txtStreamAddress.setText(EasyApplication.getEasyApplication().getUrl());
        }
    }

    private int getDgree() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        return degrees;
    }


    public void Play(String username){
        if (!mMediaStream.isStreaming()) {
            String url = Config.DEFAULT_SERVER_URL_Live+username;
            mMediaStream.startStream(url, new InitCallback() {
                @Override
                public void onCallback(int code) {
                    switch (code) {
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                            sendMessage("无效Key");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                            sendMessage("激活成功");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_RTMP_STATE_CONNECTING:
                            sendMessage("连接中");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_RTMP_STATE_CONNECTED:
                            sendMessage("连接成功");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_RTMP_STATE_CONNECT_FAILED:
                            sendMessage("连接失败");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_RTMP_STATE_CONNECT_ABORT:
                            sendMessage("连接异常中断");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_RTMP_STATE_PUSHING:
                            sendMessage("直播中");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_RTMP_STATE_DISCONNECTED:
                            sendMessage("断开连接");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                            sendMessage("平台不匹配");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                            sendMessage("断授权使用商不匹配");
                            break;
                        case EasyRTMP.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                            sendMessage("进程名称长度不匹配");
                            break;
                    }
                }
            });
            //btnSwitch.setText("停止");
            //txtStreamAddress.setText(url);
        } else {
            //Quit();
            mMediaStream.stopStream();
            //btnSwitch.setText("开始");
            sendMessage("断开连接");
        }
    }

    private void Quit() {

        Intent getIntent = getIntent();
        //获取上一忘记密码页面传递username的值
        String username = getIntent.getStringExtra("musername");
        //设置值


        //切换Login Activity至User Activity
        //登录接口访问
        Gson gson = new Gson();
        //实例化login1对象，在info.java文件
        Quit info = new Quit(username);
        String obj = gson.toJson(info);
        //登录接口请求

        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"), obj);
        final StreamActivity.QuitService service = retrofit.create(StreamActivity.QuitService.class);

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
                        mMediaStream.stopStream();
                    } else {

                        Toast.makeText(getApplicationContext(),"退出失败", Toast.LENGTH_LONG);
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

    @Subscribe
    public void onStreamStat(final StreamStat stat) {
        streamStat.post(new Runnable() {
            @Override
            public void run() {
                streamStat.setText(getString(R.string.stream_stat, stat.fps, stat.bps / 1024));
            }
        });
    }


    @Override
    protected void onDestroy() {
        BUS.unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        startLive.setText("开始直播");
        super.onStop();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG, "get capture permission success!");
                mResultCode = resultCode;
                mResultIntent = data;
                startScreenPushIntent();

            }
        }
    }


    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {



        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
        if (isStreaming && PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingActivity.KEY_ENABLE_BACKGROUND_CAMERA, false)) {
            new AlertDialog.Builder(this).setTitle("是否允许后台上传？")
                    .setMessage("您设置了使能摄像头后台采集,是否继续在后台采集并上传视频？如果是，记得直播结束后,再回来这里关闭直播。")
                    .setNeutralButton("后台采集", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PreferenceManager.getDefaultSharedPreferences(StreamActivity.this).edit().putBoolean("background_camera_alert", true).apply();
                    StreamActivity.super.onBackPressed();
                }
            }).setPositiveButton("退出程序", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mMediaStream.stopStream();
                    StreamActivity.super.onBackPressed();
                    Toast.makeText(StreamActivity.this, "程序已退出。", Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton(android.R.string.cancel, null).show();
            return;
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
        if (mService != null) {
            goonWithAvailableTexture(surface);

//            Intent getIntent = getIntent();
//            //获取上一忘记密码页面传递username的值
//            String username = getIntent.getStringExtra("musername");
//            //设置值
//            if(username!=null)
//                Play(username);
        }
    }

    private void goonWithAvailableTexture(SurfaceTexture surface) {
        final File easyPusher = new File(Environment.getExternalStorageDirectory() +"/EasyRTMP");
        easyPusher.mkdir();
        MediaStream ms = mService.getMediaStream();
        if (ms != null) {    // switch from background to front
            ms.stopPreview();
            mService.inActivePreview();
            ms.setSurfaceTexture(surface);
            ms.startPreview();
            mMediaStream = ms;

            if (ms.isStreaming()) {
                String url = EasyApplication.getEasyApplication().getUrl();
                //btnSwitch.setText("停止");
                //txtStreamAddress.setText(url);
                sendMessage("直播中");
            }
        } else {
            ms = new MediaStream(getApplicationContext(), surface, PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(EasyApplication.KEY_ENABLE_VIDEO, true));
            ms.setRecordPath(easyPusher.getPath());
            mMediaStream = ms;
            startCamera();
            mService.setMediaStream(ms);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    @Override
    protected void onPause() {
        if (!mNeedGrantedPermission) {
            unbindService(conn);
            handler.removeCallbacksAndMessages(null);
        }
        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
        if (mMediaStream != null) {
            mMediaStream.stopPreview();
            if (isStreaming && PreferenceManager.getDefaultSharedPreferences(StreamActivity.this)
                    .getBoolean(SettingActivity.KEY_ENABLE_BACKGROUND_CAMERA, false)) {
                mService.activePreview();
            } else {
                mMediaStream.stopStream();
                mMediaStream.release();
                mMediaStream = null;

                stopService(new Intent(this, BackgroundCameraService.class));
            }
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mNeedGrantedPermission) {
            goonWithPermissionGranted();
        }
    }

    public void onRecord(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            return;
        }
        ImageButton ib = (ImageButton) view;
        if (mMediaStream != null) {
            if (mMediaStream.isRecording()) {
                mMediaStream.stopRecord();
                ib.setImageResource(R.drawable.ic_action_record);
            } else {
                mMediaStream.startRecord();
                ib.setImageResource(R.drawable.ic_action_recording);
            }
        }
    }

    public void onClickResolution(View view) {
        findViewById(R.id.spn_resolution).performClick();
    }

    public void onSwitchOrientation(View view) {
        if (mMediaStream != null) {
            if (mMediaStream.isStreaming()){
                Toast.makeText(this,"正在推送中,无法更改屏幕方向", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        int orientation = getRequestedOrientation();
        if (orientation == SCREEN_ORIENTATION_UNSPECIFIED || orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
//        if (mMediaStream != null) mMediaStream.setDgree(getDgree());
    }
}