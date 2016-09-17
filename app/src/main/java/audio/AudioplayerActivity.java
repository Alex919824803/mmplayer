package audio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghao.musicplayer.BaseActivity;
import com.zhanghao.musicplayer.R;

import utils.Utils;

public class AudioPlayerActivity extends BaseActivity {

    //音频播放进度更新
    private static final int PROGRESS = 1;
    //代表了服务,通过它得到服务里面的信息
    private IMusicPlayerService service;
    //要播放的列表的位置
    private int position;
    //Activity是否已经销毁
    private boolean isDestory = false;
    //是否来自状态栏
    private boolean from_notification = false;

    private MyBroadcastReceiver receiver;

    private Utils utils;

    private Button btn_model;
    private Button btn_pre;
    private Button btn_playandpause;
    private Button btn_next;
    private Button btn_lyric;
    private TextView music_artist;
    private TextView music_name;
    private SeekBar music_seekbar;
    private TextView music_time;

    //是否在播放中
    private boolean isPlaying;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    //得到当前时间
                    try {
                        int currentPosition = service.getCurrentPosition();
                        music_seekbar.setProgress(currentPosition);
                        music_time.setText(utils.stringForTime(service.getCurrentPosition()) + "/" + utils.stringForTime(service.getDuration()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    if (!isDestory) {
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDestory = false;
        initData();
        initView();
        setListener();
        getData();
        bindService();
    }

    //初始化数据
    private void initData() {
        utils = new Utils();
        //监听准备好的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlayerService.PREPARED_MESSAGE);
        receiver = new MyBroadcastReceiver();//ps:千万别忘了实例化
        registerReceiver(receiver, filter);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setViewStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    //设置view状态
    private void setViewStatus() throws RemoteException {
        music_artist.setText(service.getArtist());
        music_name.setText(service.getName());
        music_time.setText(utils.stringForTime(service.getCurrentPosition()) + "/" + utils.stringForTime(service.getDuration()));
        music_seekbar.setMax(service.getDuration());
        isPlaying = service.isPlaying();
        setButtonStatus();

        //发消息开始更新音频进度
        handler.sendEmptyMessage(PROGRESS);
    }


    //点击事件
    private void setListener() {
        btn_playandpause.setOnClickListener(mClickListener);
        btn_model.setOnClickListener(mClickListener);
        btn_next.setOnClickListener(mClickListener);
        btn_pre.setOnClickListener(mClickListener);
        //进度条的拖动
        music_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        service.seekTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.music_btn_pre:
                    try {
                        service.pre();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.music_btn_next:
                    try {
                        service.next();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.music_btn_playandpause:
                    try {
                        if (isPlaying) {
                            service.pause();
                        } else {
                            service.play();
                        }
                        isPlaying = !isPlaying;
                        setButtonStatus();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.music_btn_model:
                    try {
                        changeModel();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    //设置播放模式
    private void changeModel() throws RemoteException {
        int playmodel = service.getPlayModel();
        if (playmodel == MusicPlayerService.REPEAT_MODE_NORMAL) {
            //单曲循环
            playmodel = MusicPlayerService.REPEAT_MODE_CURRENT;
        }else  if (playmodel == MusicPlayerService.REPEAT_MODE_CURRENT){
            //全部循环
            playmodel=MusicPlayerService.REPEAT_MODE_ALL;
        }else if ( playmodel==MusicPlayerService.REPEAT_MODE_ALL){
            //顺序
            playmodel=MusicPlayerService.REPEAT_MODE_NORMAL;
        }
        //设置播放模式
        service.setPlayModel(playmodel);
        setPlayModeButton();
    }

    private void setPlayModeButton() throws RemoteException {
        int playmodel = service.getPlayModel();
        if (playmodel == MusicPlayerService.REPEAT_MODE_NORMAL) {
            //顺序
            btn_model.setBackgroundResource(R.drawable.music_normal);
            Toast.makeText(getApplicationContext(),"顺序",Toast.LENGTH_LONG).show();
        }else  if (playmodel == MusicPlayerService.REPEAT_MODE_CURRENT){
            //单曲循环
            btn_model.setBackgroundResource(R.drawable.music_one);
            Toast.makeText(getApplicationContext(),"单曲循环",Toast.LENGTH_LONG).show();
        }else if ( playmodel==MusicPlayerService.REPEAT_MODE_ALL){
            //全部循环
            btn_model.setBackgroundResource(R.drawable.music_all);
            Toast.makeText(getApplicationContext(),"全部循环",Toast.LENGTH_LONG).show();
        }
    }

    private void setButtonStatus() {
        if (isPlaying) {
            btn_playandpause.setBackgroundResource(R.drawable.pause);
        } else {
            btn_playandpause.setBackgroundResource(R.drawable.start);
        }
    }


    private void initView() {
        setTitle("音乐播放器");
        setRightButton(View.GONE);//隐藏右侧按钮

        btn_model = (Button) findViewById(R.id.music_btn_model);
        btn_pre = (Button) findViewById(R.id.music_btn_pre);
        btn_playandpause = (Button) findViewById(R.id.music_btn_playandpause);
        btn_next = (Button) findViewById(R.id.music_btn_next);
        btn_lyric = (Button) findViewById(R.id.music_btn_lyric);
        music_artist = (TextView) findViewById(R.id.music_artist);
        music_name = (TextView) findViewById(R.id.music_name);
        music_time = (TextView) findViewById(R.id.music_time);
        music_seekbar = (SeekBar) findViewById(R.id.music_seekbar);
    }

    //得到数据
    private void getData() {
        from_notification = getIntent().getBooleanExtra("from_notification", false);
        if (!from_notification) {
            position = getIntent().getIntExtra("position", 0);
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        //绑定成功
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if (service != null) {
                try {
                    if (!from_notification) {
                        service.openAudio(position);
                    } else {
                        //发一个消息告诉Activity准备好了
                        service.notifyChange(MusicPlayerService.PREPARED_MESSAGE);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        //取消绑定
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

    //以绑定方式启动服务
    private void bindService() {
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        intent.setAction("bindservice");
        intent.setPackage(this.getPackageName());//安卓5.0之后必须要加这个否则会报错
        intent.putExtras(bundle);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public View setContentView() {
        return View.inflate(this, R.layout.activity_audioplayer, null);
    }

    @Override
    public void returnButtonClick() {
        finish();
    }

    @Override
    public void rightButtonClick() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);//退出时销毁绑定连接
        unregisterReceiver(receiver);//退出时取消注册广播接收者
        receiver = null;
        isDestory = true;
    }
}
