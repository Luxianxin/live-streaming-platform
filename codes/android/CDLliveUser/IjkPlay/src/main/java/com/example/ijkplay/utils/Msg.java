package com.example.ijkplay.utils;

 public class Msg
 {

    public static final int TYPE_RECEIVED = 0;//收到的消息
    public static final int TYPE_SENT = 1;//发送的消息
    private String chatinfo;//消息内容
    private  int type;//消息类型

    public  Msg(String chatinfo, int type){
        this.chatinfo = chatinfo;
        this.type = type;
    }

    public String getchatinfo() {
        return chatinfo;
    }

    public int getType() {
        return type;
    }
}
