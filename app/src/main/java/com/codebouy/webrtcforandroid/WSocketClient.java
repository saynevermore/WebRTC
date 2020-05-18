package com.codebouy.webrtcforandroid;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;

import java.net.URI;


public class WSocketClient extends WebSocketClient {    //继承引入的第三方库 WebSocketClient,实现构造函数和四个抽象方法

    private static volatile WSocketClient client = null;
    public WSocketClientDelegate delegate ;  //定义一个delegate变量用于完成接口回调
    public static WSocketClient getInstance(URI uri) {
        if (client == null) {
            synchronized (WSocketClient.class) {
                if (client == null) {
                    client = new WSocketClient(uri);
                }
            }
        }
        return client;
    }
    public WSocketClient(URI serverUri) {        //子类实现父类构造方法
        super(serverUri);
    }
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        try {
            delegate.onOpen(handshakedata);
        } catch (JSONException e) {
            e.printStackTrace();  //在命令行打印异常信息在程序中出错的位置及原因
        }

    }
    @Override
    public void onMessage(String message) {
        try {
            delegate.onMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClose(int code, String reason, boolean remote) {
        delegate.onClose(code, reason, remote);

    }
    @Override
    public void onError(Exception ex) {
        delegate.onError(ex);
    }
}
