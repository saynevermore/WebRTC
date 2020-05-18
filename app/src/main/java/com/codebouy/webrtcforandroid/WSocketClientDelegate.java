package com.codebouy.webrtcforandroid;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;



public interface WSocketClientDelegate {


    public void onOpen(ServerHandshake handshakedata) throws JSONException;  //websocket连接开启时调用


    public void onMessage(String message) throws JSONException;  //接收消息时调用


    public void onClose(int code, String reason, boolean remote);  //websocket连接断开时调用


    public void onError(Exception ex);  //连接出错时调用
}
