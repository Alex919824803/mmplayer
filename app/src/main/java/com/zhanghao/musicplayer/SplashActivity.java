package com.zhanghao.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

public class SplashActivity extends Activity {
    private  boolean isEnterMain=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        isEnterMain=false;

        //等待两秒再进入主页面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                enterMain();
            }

        }, 2000);
    }
    //进入主页面
    private void enterMain(){
        if (!isEnterMain) {
            isEnterMain=true;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();//关闭启动页面
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        enterMain();
        return super.onTouchEvent(event);
    }
}
