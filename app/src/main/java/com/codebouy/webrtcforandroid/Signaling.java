package com.codebouy.webrtcforandroid;

import android.util.Log;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;


public class Signaling implements WSocketClientDelegate {

    WSocketClient client;
    String url;
    String selfId;
    SignalingDelegate delegate;
    public Signaling(String url, String selfId) {
        this.url = url;
        this.selfId = selfId;

        try {         //初始化客户端 创造单例 并调用connect()方法实现连接
            this.client = WSocketClient.getInstance(new URI(url));
            this.client.delegate = this;
            this.client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void send(JSONObject object, String event) throws JSONException {  //send()方法用来发送消息
        object.put("type", event);
        Log.i("send", object.toString());  //包括完成对一般提示性information、warning警告、error错误在日志中打印输出
        client.send(object.toString());

    }
    @Override
    public void onOpen(ServerHandshake handshakedata) throws JSONException {
        Log.i("socket", "socket 连接成功");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "hello");
        jsonObject.put("id", selfId);
        this.send(jsonObject, "new");
    }

    @Override
    public void onMessage(String message) throws JSONException {
        Log.i("msg", message);
        JSONObject jsonObject = new JSONObject(message);
        this.delegate.onMessage(jsonObject);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("close", reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.i("onError", ex.toString());
    }
}
