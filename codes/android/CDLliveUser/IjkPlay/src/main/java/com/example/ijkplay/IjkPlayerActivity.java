package com.example.ijkplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ijkplay.utils.Msg;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cdlp.Cdlp;

//import vitamio.vitamiolibrary.cdlp.Cdlp;

public class IjkPlayerActivity extends BaseActivity implements View.OnClickListener {
    private String TAG=IjkPlayerActivity.class.getSimpleName();

    private Activity mActivity;
    AlertDialog.Builder DialogBuilder;
    private VideoPlayView videoPlayView;
    Socket client = null;
    private View parentlayout;
    private EditText name, input_text;
    private Button send;
    private Cdlp.CdlpMessage mes;
    private Cdlp.CdlpMessage mes1;
    private ListView itemlist = null;
    TextView State;
    String LiverName,UserName;
    ImageView imgA;
    private List<Map<String, Object>> list;
    private SimpleAdapter adapter;
    private Handler mHandler=new Handler(){

    };
    Handler myHandler1 = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if (msg.what==123) {
                //if(!"".equals(msg.getData().getString("Chat")))
                //{
                  //  if (!"".equals(msg.getData().getInt("Type")))
                    //{
                        String Type=msg.getData().getString("Type");
                        String State1=msg.getData().getString("Level");
                        if (Type.equals("4") )
                        {

                            if(State1.equals("1"))
                                State.setText("直播中");
                            else State.setText("主播断开");
                            return;
                        }
                        else
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
                    //}

                //}

            }

        };
    };

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_ijk_player);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        initDialog();
        Intent GetContent=getIntent();
        LiverName = GetContent.getStringExtra("LiverName");
        UserName = GetContent.getStringExtra("UserName");
        State=(TextView)findViewById(R.id.textone);
        //LiverName="zcl";
        //UserName="HAHA";
        imgA=(ImageView)findViewById(R.id.imgPortraitA);
        parentlayout=this.findViewById(R.id.parentlayout);
        send = (Button) findViewById(R.id.send);
        name = (EditText) findViewById(R.id.name);
        input_text = (EditText) findViewById(R.id.input_text);
        itemlist = (ListView) findViewById(R.id.list_message);
        TextView State=(TextView)findViewById(R.id.textone);
        State.setText("直播中");
       refreshListItems();
        mActivity=this;
        send.setOnClickListener(this);

        if(videoPlayView==null){
            videoPlayView=new VideoPlayView(mActivity);
        }
        videoPlayView.initViews(parentlayout);

        final String url=getIntent().getStringExtra("url");

        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {

                    System.out.println("main");
                    client = new Socket("192.168.1.71", 8001);// 网络访问最好放在线程中
                    Cdlp.CdlpMessage.Builder builder = Cdlp.CdlpMessage.newBuilder();
                    mes = builder.setChat(UserName+"进入"+LiverName+"的直播间啦")
                            .setUserName(UserName)
                            .setLevel(1)
                            .setType(1)
                            .setAnchorName(LiverName)
                            .build();

                    try {
                        mes.writeTo(client.getOutputStream());
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
                            sendMessage(temp);
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

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(videoPlayView!=null){
                    videoPlayView.setShowContoller(true);
                    videoPlayView.start(url);
                }
            }
        },1000);
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
        map.put("chatinfo", "---欢迎进入"+LiverName+"的房间---");
        list.add(map);


        return list;
    }




 //聊天室通信模块
@Override
public void onClick(View arg0) {
    // TODO Auto-generated method stub
    System.out.println("button");
    String s2 = input_text.getText().toString().trim().replace("\n","");
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



            //client.getOutputStream().write(s3.getBytes("utf-8"));// 获取从客户端得到的数据

        } //input_text.setText("");//相当于刷新
}



    private  void sendMessage(byte[] temp)
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




    //播放器函数模块
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
                                    .setChat("用户"+UserName+"退出房间啦")
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
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoPlayView!=null) {
            videoPlayView.onChanged(newConfig);
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            } else {

            }
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            DialogBuilder.show();
            return true;
        }

        if(keyCode==KeyEvent.KEYCODE_MENU){
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle("选择播放比例");
            final String[] cities = {"4:3", "16:9", "full screen", "Original Scale"};
            builder.setItems(cities, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String item = cities[which];
                    if (!TextUtils.isEmpty(item)) {
                        int scale=getScale(item);
                        videoPlayView.getmVideoView().setVideoLayout(scale, 0);
                    }
                }
            });
            builder.show();
        }
        return super.onKeyUp(keyCode, event);
    }

    private int getScale(String str){
        if(str.equals("4:3")){
            return 3;
        }
        if(str.equals("16:9")){
            return 2;
        }
        if(str.equals("full screen")){
            return 1;
        }
        if(str.equals("Original Scale")){
            return 0;
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoPlayView!=null){
            videoPlayView.stop();
            videoPlayView.release();
            videoPlayView.onDestroy();
            videoPlayView=null;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayView!=null){
            videoPlayView.stop();
        }
    }

}





