package com.codebouy.webrtcforandroid;

import org.json.JSONException;
import org.json.JSONObject;



public interface SignalingDelegate {
    public void onMessage(JSONObject data) throws JSONException;
}
