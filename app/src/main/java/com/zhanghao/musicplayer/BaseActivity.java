package com.zhanghao.musicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class BaseActivity extends Activity {
    private Button btn_return;
    private TextView tv_title;
    private Button btn_right;
    private LinearLayout children_content;
    private FrameLayout fl_titlebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Use code to kill Bar
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_base);
        initView();
        setOnclickListener();
    }
    //init
    private void initView(){
        btn_return=(Button)findViewById(R.id.btn_return);
        tv_title=(TextView)findViewById(R.id.title);
        btn_right=(Button)findViewById(R.id.btn_right);

        children_content=(LinearLayout)findViewById(R.id.children_content);
        fl_titlebar=(FrameLayout)findViewById(R.id.fl_titlerbar);

        View child=setContentView();
        if (child!=null){
            LinearLayout.LayoutParams params= new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            //add children's xml
            children_content.addView(child,params);
        }
    }

    //click some event
    private  void setOnclickListener(){
        btn_return.setOnClickListener(clickListener);
        btn_right.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_return:
                    returnButtonClick();
                    break;
                case R.id.btn_right:
                    rightButtonClick();
                    break;
            }
        }
    };

    public abstract View setContentView();

    //返回键的抽象方法
    public abstract void returnButtonClick();
    //没啥卵用键的抽象方法
    public abstract void rightButtonClick();
    //设置按钮的显示状态
    public void setReturnButton(int visibility){
        btn_return.setVisibility(visibility);
    }
    //设置按钮的显示状态
    public void setRightButton(int visibility){
        btn_right.setVisibility(visibility);
    }
    //设置标题
    public void setTitle(String title){
        tv_title.setText(title);
    }
    //设置标题栏是否隐藏
    public void setTitleBar(int visibility){
        fl_titlebar.setVisibility(visibility);
    }
}
