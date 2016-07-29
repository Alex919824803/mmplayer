package com.zhanghao.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import audio.AudioListActivity;
import video.VideoListActivity;

public class MainActivity extends BaseActivity {

    private GridView gridView;
    private int[] ids={R.drawable.video,R.drawable.music};
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏返回按钮
        setReturnButton(View.GONE);
        setTitle("手机影音");
        gridView=(GridView)findViewById(R.id.gridview);
        gridView.setAdapter(new MyMainAdapter());

        //设置点击事件
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        intent=new Intent(MainActivity.this, VideoListActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent=new Intent(MainActivity.this, AudioListActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    private class MyMainAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return ids.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View view;
            if(convertView!=null){
                view=convertView;
                holder=(ViewHolder) view.getTag();
            }else {
                view=View.inflate(MainActivity.this,R.layout.main_item,null);
                holder=new ViewHolder();//容器
                holder.iv_icon=(ImageView) view.findViewById(R.id.iv_icon);
                //容器和view的对应关系保存起来
                view.setTag(holder);
            }
            holder.iv_icon.setImageResource(ids[position]);
            return view;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

    }

    private static class ViewHolder{
        ImageView iv_icon;
    }


    @Override
    public View setContentView() {
        //把activity_main布局文件转化为View对象
        return View.inflate(this,R.layout.activity_main,null);
    }

    @Override
    public void returnButtonClick() {

    }

    @Override
    public void rightButtonClick() {
        Toast.makeText(getApplicationContext(),"丛宇昕是大笨蛋",Toast.LENGTH_LONG).show();
    }
}
