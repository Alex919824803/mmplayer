package audio;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zhanghao.musicplayer.BaseActivity;
import com.zhanghao.musicplayer.R;

import java.util.ArrayList;

import domain.AudioItem;
import utils.Utils;

public class AudioListActivity extends BaseActivity {

    private ListView lv_audiolist;
    private TextView mnoaudio;
    private Utils utils;

    private ArrayList<AudioItem> audioItems;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (audioItems != null && audioItems.size() > 0) {
                lv_audiolist.setAdapter(new AudioListAdapter());
            } else {
                mnoaudio.setVisibility(View.VISIBLE);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //设置标题
        setTitle("本地音乐");
        //隐藏右边按钮
        setRightButton(View.GONE);

        lv_audiolist = (ListView) findViewById(R.id.lv_audiolist);
        mnoaudio = (TextView) findViewById(R.id.tv_noaudio);
        utils = new Utils();

        //加载视频数据
        getAllAudio();

        //设置点击事件
        lv_audiolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //根据点击位置获取视频信息
//                AudioItem videoItem=audioItems.get(position);
//
//                Intent intent=new Intent(AudioListActivity.this,AudioPlayerActivity.class);
//                intent.setDataAndType(Uri.parse(videoItem.getData()),"audio/*");
//                startActivity(intent);

                //在安卓中数据传递有意图，发送和接收
                //在安卓中，被传递的对象需要序列化

                //send list and this position
                Intent intent = new Intent(AudioListActivity.this, AudioPlayerActivity.class);
                //get the position
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }

    private class AudioListAdapter extends BaseAdapter {
        //返回总条数
        @Override
        public int getCount() {
            return audioItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View view;
            ViewHolder holder;
            if (convertView != null) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(AudioListActivity.this, R.layout.audiolist_item, null);
                holder = new ViewHolder();
                holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
                holder.tv_duration = (TextView) view.findViewById(R.id.tv_duration);
                holder.tv_size = (TextView) view.findViewById(R.id.tv_size);

                //对应关系保存起来
                view.setTag(holder);
            }
            //Get specific information of a video
            AudioItem audioItem = audioItems.get(position);
            holder.tv_name.setText(audioItem.getTitle());
            holder.tv_duration.setText(utils.stringForTime(Integer.valueOf(audioItem.getDuration())));
            holder.tv_size.setText(android.text.format.Formatter.formatFileSize(AudioListActivity.this, audioItem.getSize()));

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

    private static class ViewHolder {//缓存容器类
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size;
    }

    //在子线程加载视频
    private void getAllAudio() {
        new Thread() {
            @Override
            public void run() {//read local video from your phone or SDcard
                audioItems = new ArrayList<AudioItem>();
                ContentResolver resoler = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Audio.Media.TITLE,//视频标题
                        MediaStore.Audio.Media.DURATION,//时间长度
                        MediaStore.Audio.Media.SIZE,//视频大小
                        MediaStore.Audio.Media.DATA,//视频绝对地址
                        MediaStore.Audio.Media.ARTIST
                };

                Cursor cursor = resoler.query(uri, projection, null, null, null);
                while (cursor.moveToNext()) {
                    Long size = cursor.getLong(2);
                    if (size > 1 * 1024 * 1024) {//过滤掉3M以下的音频文件
                        //具体的音频信息
                        AudioItem item = new AudioItem();

                        String title = cursor.getString(0);
                        item.setTitle(title);

                        String duration = cursor.getString(1);
                        item.setDuration(duration);

                        item.setSize(size);

                        String data = cursor.getString(3);
                        item.setData(data);

                        String artist=cursor.getString(4);
                        item.setArtist(artist);

                        audioItems.add(item);
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    @Override
    public View setContentView() {
        return View.inflate(this, R.layout.activity_audio_list, null);
    }

    @Override
    public void returnButtonClick() {
        finish();
    }

    @Override
    public void rightButtonClick() {
    }
}
