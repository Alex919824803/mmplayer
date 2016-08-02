package video;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghao.musicplayer.BaseActivity;
import com.zhanghao.musicplayer.R;
import java.util.ArrayList;
import java.util.Formatter;

import domain.VideoItem;
import utils.Utils;

public class VideoListActivity extends BaseActivity {

    private ListView lv_videolist;
    private TextView tv_mnovideo;
    private Utils utils;

    private ArrayList<VideoItem> videoItems;

    private Handler handler=new Handler(){
        public void handleMessage(Message msg) {
            if (videoItems!=null&&videoItems.size()>0){
                lv_videolist.setAdapter(new VideoListAdapter());
            }else {
                tv_mnovideo.setVisibility(View.VISIBLE );
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //设置标题
        setTitle("本地视频");
        //隐藏右边按钮
        setRightButton(View.GONE);

        lv_videolist=(ListView)findViewById(R.id.lv_videolist);
        tv_mnovideo=(TextView)findViewById(R.id.tv_novideo);
        utils=new Utils();

        //加载视频数据
        getAllVideo();

        //设置点击事件
        lv_videolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               //根据点击位置获取视频信息
//                VideoItem videoItem=videoItems.get(position);
//
//                Intent intent=new Intent(VideoListActivity.this,VideoPlayerActivity.class);
//                intent.setData(Uri.parse(videoItem.getData()));
//                startActivity(intent);

                //在安卓中数据传递有意图，发送和接收
                //在安卓中，被传递的对象需要序列化


                //send list and this position
                Intent intent=new Intent(VideoListActivity.this,VideoPlayerActivity.class);
                Bundle extras=new Bundle();
                //videos list
                extras.putSerializable("videolist",videoItems);
                intent.putExtras(extras);
                //get the position
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }

    private class VideoListAdapter extends BaseAdapter{

        //返回总条数
        @Override
        public int getCount() {
            return videoItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View view;
            ViewHolder holder;
            if (convertView!=null){
               view=convertView;
                holder=(ViewHolder) view.getTag();
            }else {
                view = View.inflate(VideoListActivity.this, R.layout.vediolist_item, null);
                holder=new ViewHolder();
                holder.tv_name=(TextView)view.findViewById(R.id.tv_name);
                holder.tv_duration=(TextView)view.findViewById(R.id.tv_duration);
                holder.tv_size=(TextView)view.findViewById(R.id.tv_size);

                //对应关系保存起来
                view.setTag(holder);
            }

            //Get specific information of a video
            VideoItem videoItem=videoItems.get(position);
            holder.tv_name.setText(videoItem.getTitle());
            holder.tv_duration.setText(utils.stringForTime(Integer.valueOf(videoItem.getDuration())));
            holder.tv_size.setText(android.text.format.Formatter.formatFileSize(VideoListActivity.this,videoItem.getSize()));

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

    private static class ViewHolder{//缓存容器类
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size ;
    }

    //在子线程加载视频
    private void getAllVideo() {
        new Thread(){
            @Override
            public void run() {//read local video from your phone or SDcard
                videoItems=new ArrayList<VideoItem>();

                ContentResolver resoler=getContentResolver();

                Uri uri= MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection={
                        MediaStore.Video.Media.TITLE,//视频标题
                        MediaStore.Video.Media.DURATION,//时间长度
                        MediaStore.Video.Media.SIZE,//视频大小
                        MediaStore.Video.Media.DATA//视频绝对地址
                };

                Cursor cursor=resoler.query(uri,projection,null,null,null);
                while (cursor.moveToNext()){
                    Long size=cursor.getLong(2);
                    if(size>3*1024*1024) {//过滤掉3M以下的视频文件
                        //具体的视频信息
                        VideoItem item = new VideoItem();

                        String title = cursor.getString(0);
                        item.setTitle(title);

                        String duration = cursor.getString(1);
                        item.setDuration(duration);

                        item.setSize(size);

                        String data = cursor.getString(3);
                        item.setData(data);

                        videoItems.add(item);
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public View setContentView() {
        return  View.inflate(this, R.layout.activity_video_list,null);
    }

    @Override
    public void returnButtonClick() {
        finish();
    }

    @Override
    public void rightButtonClick() {
    }
}
