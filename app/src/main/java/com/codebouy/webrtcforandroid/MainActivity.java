package com.codebouy.webrtcforandroid;

import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //寻找id为VideoRoomBtn的按钮并增加点击事件,点击跳转至新的activity
        findViewById(R.id.VideoRoomBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                       //intent显式启动，用startActivity(intent)调用新的activity（CallVideoActivity）
                Intent intent = new Intent(MainActivity.this,
                        CallVideoActivity.class);          //第一个参数是提供起始Activity的包名，第二个参数提供了目标Activity的全限定名（包名+类名）
                startActivity(intent);
            }
        });
    }
}
