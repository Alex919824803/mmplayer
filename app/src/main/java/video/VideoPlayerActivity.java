package video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.VideoView;

import com.zhanghao.musicplayer.BaseActivity;
import com.zhanghao.musicplayer.R;
import view.MyVideoView;
import java.util.ArrayList;

import domain.VideoItem;
import utils.Utils;

//video player from system api
public class VideoPlayerActivity extends BaseActivity {
    private MyVideoView videoView;
    private Uri uri;
    private TextView tv_system_time;
    private TextView battery;
    private TextView tv_title;
    private Button btn_voice;
    private Button btn_switch;
    private SeekBar seekBar_voice;

    private TextView tv_current_time;
    private SeekBar seekbar_video;
    private TextView tv_duration;

    private Button btn_exit;
    private Button btn_pre;
    private Button btn_startandpause;
    private Button btn_next;
    private Button btn_fullscreen;

    private Utils utils;
    private MyBroadRecever recever;
    private int level;//电量
    private ArrayList<VideoItem> videoItems;
    private int position;

    //定义手势识别器
    private GestureDetector detector;

    private LinearLayout controller_player;
    private LinearLayout loadding;

    //判断是否被销毁
    private boolean isDestroyed = false;
    //判断是否显示控制栏
    private boolean isShowController = false;
    //判断是否为播放状态
    private boolean isPlaying = false;
    //判断是否全屏状态
    private boolean isFullScreen=false;
    //判断是否为静音
    private boolean isMute=false;
    //判断是否在缓冲
    private boolean isBuffer=true;

    private static final int PROGRESS = 1;
    private static final int DELAYED_HIDECONTROLLER = 2;//隐藏控制面板
    private static final int FULL_SCREEN=3;//全屏
    private static final int DEFAULT_SCREEN=4;//默认屏幕

    private int screenWidth;
    private int screenHeight;

    private AudioManager audioManager;//管理音量大小
    private int currentVolume;//当前音量
    private int maxVolume;//最大音量

    private float startY;//手指起始位置的Y坐标
    private float audioTouchRang;//屏幕滑动范围
    private int mVo;//滑动前的音量值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        getData();
        setData();
        //judge video ready or not
        new SetListener().invoke();
    }

    private void initView() {
        //设置不锁屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏标题栏
        setTitleBar(View.GONE);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //关联xml/布局文件
        videoView = (MyVideoView) findViewById(R.id.videoview);
        tv_title = (TextView) findViewById(R.id.tv_title);
        battery = (TextView) findViewById(R.id.battery);
        tv_system_time = (TextView) findViewById(R.id.tv_system_time);
        btn_voice = (Button) findViewById(R.id.btn_voice);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        seekBar_voice = (SeekBar) findViewById(R.id.seekbar_voice);
        controller_player = (LinearLayout) findViewById(R.id.controller_player);
        tv_current_time = (TextView) findViewById(R.id.tv_current_time);
        seekbar_video = (SeekBar) findViewById(R.id.seekbar_video);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
        btn_exit = (Button) findViewById(R.id.btn_exit);
        btn_pre = (Button) findViewById(R.id.btn_pre);
        btn_startandpause = (Button) findViewById(R.id.btn_playandpause);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_fullscreen = (Button) findViewById(R.id.btn_fullscreen);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight= wm.getDefaultDisplay().getHeight();
        loadding=(LinearLayout)findViewById(R.id.loadding);
        //实例化手势识别器
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startOrPause();
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isFullScreen){
                    setVideoType(DEFAULT_SCREEN);
                }else {
                    setVideoType(FULL_SCREEN);
                }
                return super.onDoubleTap(e);
            }
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
               if (isShowController==false&&isBuffer==false){
                   showControllerPlater();
                   sendDelayHideControllerPlayer();
               }else {
                   hideControllerPlayer();
                   removeDelayHideControllerPlayer();
               }
//                if (isShowController) {
//                    removeDelayHideControllerPlayer();
//                    hideControllerPlayer();
//                } else {
//                    showControllerPlater();
//                    sendDelayHideControllerPlayer();
//                }
                return true;
            }
        });
        //得到当前音量和最大音量
        audioManager=(AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //得到最大音量值 ps:0~15
        maxVolume=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void initData() {
        utils = new Utils();
        isDestroyed = false;
        //监听电量
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);//when battery has changed,system will send broadcast
        recever = new MyBroadRecever();
        registerReceiver(recever, filter);
    }

    private void getData() {
        //获取播放列表
        videoItems = (ArrayList<VideoItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);
        //获取视频地址 通常来自第三方软件,文件管理器,QQ空间等
        uri = getIntent().getData();
    }

    private void setData() {
        if (videoItems != null && videoItems.size() > 0) {
            //the Data from play list
            VideoItem videoitem = videoItems.get(position);
            //set play position
            videoView.setVideoPath(videoitem.getData());
            //set video title
            tv_title.setText(videoitem.getTitle());
        } else if (uri != null) {
            //set play position
            videoView.setVideoURI(uri);
            //set video title
            tv_title.setText(uri.toString());
            //设置上一个和下一个按钮不可点击
            btn_pre.setEnabled(false);
            btn_next.setEnabled(false);
        }
        //seekbar和声音进行关联
        seekBar_voice.setMax(maxVolume);
        seekBar_voice.setProgress(currentVolume);
    }

    //使用手势识别器
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);//执行父类的方法
        detector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN://手指按下
                removeDelayHideControllerPlayer();
                //1.记录初始值
                startY=event.getY();
                audioTouchRang=Math.min(screenHeight,screenWidth);
                mVo=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                //2记录endY
                float endY=event.getY();
                //3.计算偏移量
                float distanceY=startY-endY;
                //4.计算屏幕滑动比例
                float datel=distanceY/audioTouchRang;
                //5.计算改变的音量值
                float volume=datel*maxVolume;
                //6.屏幕非法值,找出要设置的音量值
                float volumeS=Math.min(Math.max(volume+mVo,0),maxVolume);
                if (datel!=0){
                    updataVolume((int)volumeS);
                }
                break;
            case MotionEvent.ACTION_UP://手指抬起
                sendDelayHideControllerPlayer();
                break;
        }
        return true;//对事件进行处理了
    }

    ///////////////////////电量/////////////////////
    private void setBattery() {
        battery.setText(level + "%");
    }

    private class MyBroadRecever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取电量的值0~100
            level = intent.getIntExtra("level", 0);
        }
    }
    //////////////////////////////////


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    //得到视频播放进程
                    int currentPosition = videoView.getCurrentPosition();
                    tv_current_time.setText(utils.stringForTime(currentPosition));
                    //设置电量的显示
                    setBattery();
                    //显示系统时间
                    tv_system_time.setText(utils.getSystemTime());
                    seekbar_video.setProgress(currentPosition);

                    ///////////网络视频时显示缓存进度///////////////
                    //设置缓冲进度,在0~100
                    int percentage= videoView.getBufferPercentage();
                    int total=percentage*seekbar_video.getMax();
                    int buffer=total/100;//缓冲进度值
                    seekbar_video.setSecondaryProgress(buffer);
                    //////////////////////////////////////////////

                    //消息的死循环
                    if (!isDestroyed) {
                        handler.removeMessages(PROGRESS);//如果不加会变卡，消息全部在队列中
                        handler.sendEmptyMessageDelayed(PROGRESS, 1000);
                    }
                    break;
                case DELAYED_HIDECONTROLLER:
                    hideControllerPlayer();
                    break;
                default:
                    break;
            }
        }
    };

    private void hideControllerPlayer() {
        controller_player.setVisibility(View.GONE);
        isShowController = false;
    }

    private void showControllerPlater() {
        controller_player.setVisibility(View.VISIBLE);
        isShowController = true;
    }

    private class SetListener {
        //设置按钮的监听
        private void invoke() {
            //设置按钮的监听
            btn_startandpause.setOnClickListener(onClickListener);
            btn_pre.setOnClickListener(onClickListener);
            btn_next.setOnClickListener(onClickListener);
            btn_exit.setOnClickListener(onClickListener);
            btn_fullscreen.setOnClickListener(onClickListener);
            btn_voice.setOnClickListener(onClickListener);
            //设置seekbar的监听用来调节进度
            seekbar_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                //seekbar状态发生变化时回调这个方法
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b){
                        videoView.seekTo(i);
                    }
                }
                //手指离开Seekbar调用此方法
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    removeDelayHideControllerPlayer();
                }
                //点击seekbar调用此方法
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    sendDelayHideControllerPlayer();
                }
            });
            //设置seekbar的监听用来调节音量大小
            seekBar_voice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b){
                        updataVolume(i);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    removeDelayHideControllerPlayer();//移除隐藏控制面板的消息
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    sendDelayHideControllerPlayer();//手指离开后可以隐藏了
                }
            });
            //监听视频是否准备好了，开始播放
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //开始播放视频
                    videoView.start();
                    isPlaying = true;
                    //设置屏幕默认大小
                    setVideoType(DEFAULT_SCREEN);
                    int duration = videoView.getDuration();
                    tv_duration.setText(utils.stringForTime(duration));
                    //视频总时长关联seekbar
                    seekbar_video.setMax(duration);
                    //隐藏加载效果
                    loadding.setVisibility(View.GONE);
                    isBuffer=false;
                    //开始更新进度
                    handler.sendEmptyMessage(PROGRESS);

                }
            });
            //监听播放完成
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playNextVideo();
                }
            });
            //监听是否播放出错
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Toast.makeText(getApplicationContext(),"播放出错",Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }
    }

    private void playPreVideo() {
        if (videoItems!=null&&videoItems.size()>0){
            position--;
            if (position>=0){
                VideoItem videoItem=videoItems.get(position);
                videoView.setVideoPath(videoItem.getData());
                tv_title.setText(videoItem.getTitle());
            }else {
                position=0;
                Toast.makeText(getApplicationContext(),"这是第一个视频",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void playNextVideo() {
        //没有下个视频了，退出此次播放
        //有下一个视频，播放下一个
        if (videoItems!=null&&videoItems.size()>0){
            position++;
            if (position<videoItems.size()){
                VideoItem videoItem=videoItems.get(position);
                videoView.setVideoPath(videoItem.getData());
                tv_title.setText(videoItem.getTitle());
                //如果是最后一个视频，按钮就不应该点击并且变灰
            }else {
                //最后一个的位置
                position=videoItems.size()-1;
                Toast.makeText(getApplicationContext(),"视频全部播放完毕",Toast.LENGTH_LONG).show();
                finish();//退出播放
            }
        } else if (uri!=null){
            Toast.makeText(getApplicationContext(),"播放完成退出播放器",Toast.LENGTH_LONG).show();
            finish();//退出播放
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            removeDelayHideControllerPlayer();
            sendDelayHideControllerPlayer();
            switch (view.getId()) {
                case R.id.btn_playandpause://播放和暂停切换
                    startOrPause();
                    break;
                case R.id.btn_next:
                    playNextVideo();
                    break;
                case R.id.btn_pre:
                    playPreVideo();
                    break;
                case R.id.btn_exit:
                    finish();
                    break;
                case R.id.btn_voice://设置静音或者取消静音
                    isMute=!isMute;
                    updataVolume(currentVolume);
                    break;
                case R.id.btn_fullscreen:
                    if (isFullScreen){
                        setVideoType(DEFAULT_SCREEN);
                    }else {
                        setVideoType(FULL_SCREEN);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //视频的播放和暂停
    private void startOrPause() {
        if (isPlaying) {
            videoView.pause();
            btn_startandpause.setBackgroundResource(R.drawable.start);
        } else {
            videoView.start();
            btn_startandpause.setBackgroundResource(R.drawable.pause);
        }
        isPlaying = !isPlaying;
    }

    private void setVideoType(int type){
        switch (type){
            case FULL_SCREEN:
                videoView.setVideoSize(screenWidth,screenHeight);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                isFullScreen=true;
                break;
            case DEFAULT_SCREEN:

                int mVideoWidth=videoView.getVideoWidth();
                int mvideoHeight=videoView.getVideoHeight();
                int width=screenWidth;
                int height=screenHeight;
                if ( mVideoWidth > 0 && mvideoHeight > 0 ){
                    height = width * mvideoHeight / mVideoWidth;
                }else if ( mVideoWidth * height < width * mvideoHeight){
                    width = height * mVideoWidth / mvideoHeight;
                }
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                videoView.setVideoSize(width,height);
                isFullScreen=false;
                break;
            default: break;
        }
    }

    //调节音量的方法
    private void updataVolume(int volume) {
        if (isMute){
            //静音
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            seekBar_voice.setProgress(0);
            btn_voice.setBackgroundResource(R.drawable.btn_mute);
        }
        else {
            //非静音
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,0);
            seekBar_voice.setProgress(volume);
            btn_voice.setBackgroundResource(R.drawable.btn_voice);
        }
        currentVolume=volume;
    }

    //开始计算隐藏控制面板的时间
    private void sendDelayHideControllerPlayer() {handler.sendEmptyMessageDelayed(DELAYED_HIDECONTROLLER, 5000);}
    //不隐藏控制面板
    private void removeDelayHideControllerPlayer() {handler.removeMessages(DELAYED_HIDECONTROLLER);}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        unregisterReceiver(recever);
    }
    @Override
    public View setContentView() {
        return View.inflate(this, R.layout.activity_videoplayer, null);
    }
    @Override
    public void returnButtonClick() {}
    @Override
    public void rightButtonClick() {}
}
