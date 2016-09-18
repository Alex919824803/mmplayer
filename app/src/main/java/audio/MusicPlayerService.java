package audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.zhanghao.musicplayer.R;

import java.io.IOException;
import java.util.ArrayList;

import domain.AudioItem;


public class MusicPlayerService extends Service {
    //视频播放准备完成的时候发这个消息
    public static final String PREPARED_MESSAGE = "PREPARED_MESSAGE";
    //是否播放完成
    private boolean isCompletion = false;
    //默认模式-顺序循环
    public static int REPEAT_MODE_NORMAL = 0;
    //单曲循环
    public static int REPEAT_MODE_CURRENT = 1;
    //播放全部
    public static int REPEAT_MODE_ALL = 2;
    public static int playmodel = REPEAT_MODE_NORMAL;

    private SharedPreferences sp;

    private ArrayList<AudioItem> audioItems;
    private AudioItem currAudioItem;//当前播放音频信息
    private int currentPosition;//音频列表中的位置
    private MediaPlayer mediaPlayer;
    private IMusicPlayerService.Stub iBinder = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            try {
                service.openAudio(position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void play() throws RemoteException {
            service.play();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }

        @Override
        public void setPlayModel(int model) throws RemoteException {
            service.setPlayModel(model);
        }

        @Override
        public int getPlayModel() throws RemoteException {
            return service.getPlayModel();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void notifyChange(String notify) throws RemoteException {
            service.notifyChange(notify);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

    };


    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            isCompletion = false;
            play();
            notifyChange(PREPARED_MESSAGE);
        }
    };


    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            isCompletion = true;
            next();
        }
    };
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Toast.makeText(getApplicationContext(), "播放出错", Toast.LENGTH_LONG).show();
            return true;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        getAllAudio();
    }

    private void initData() {
        sp=getSharedPreferences("config",MODE_PRIVATE);
        playmodel=sp.getInt("playmode",REPEAT_MODE_NORMAL);
    }

    //在子线程加载音频
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
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor = resoler.query(uri, projection, null, null, null);
                while (cursor.moveToNext()) {
                    Long size = cursor.getLong(2);
                    if (size > 1024 * 1024) {//过滤掉1M以下的音频文件
                        //具体的音频信息
                        AudioItem item = new AudioItem();

                        String title = cursor.getString(0);
                        item.setTitle(title);

                        String duration = cursor.getString(1);
                        item.setDuration(duration);

                        item.setSize(size);

                        String data = cursor.getString(3);
                        item.setData(data);

                        /*
                         *2016/9/16 解决错误，误将artist作为路径保存，纪念长达两个月的愚蠢
                         */
                        String artist = cursor.getString(4);
                        item.setArtist(artist);

                        audioItems.add(item);
                    }
                }
            }
        }.start();
    }

    //根据位置打开
    private void openAudio(int position) throws IOException {
        currentPosition = position;
        currAudioItem = audioItems.get(position);
        //释放资源
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mediaPlayer.setOnErrorListener(mOnErrorListener);
        mediaPlayer.setDataSource(currAudioItem.getData());//出错，不执行这里

        mediaPlayer.prepareAsync();//异步准备，一般用这个方法
//        mediaPlayer.prepare();//同步准备,一般本地资源用它
    }


    //播放
    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }


        int icon = R.drawable.music;
        String title = "正在播放:" + getName();
        String text = getArtist();

        Notification note;
        //新建通知
        Notification.Builder builder = new Notification.Builder(getApplicationContext()).setTicker("111").setSmallIcon(icon);
        //设置属性:点击后还在,而且执行某个任务
//        note.flags = Notification.FLAG_ONGOING_EVENT;
        //制造意图
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("from_notification", true);
        //延期的意图
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //设置事件
        note = builder.setContentIntent(pendingIntent).setContentTitle(title).setContentText(text).build();
        //一定要想
        startForeground(1, note);
    }

    //暂停
    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        //把状态栏音乐播放器消掉
        stopForeground(true);
    }

    //得到歌曲名称
    private String getName() {
        if (currAudioItem != null) {
            return currAudioItem.getTitle();
        }
        return null;
    }

    //得到艺术家
    private String getArtist() {
        if (currAudioItem != null) {
            return currAudioItem.getArtist();
        }
        return null;
    }

    //得到歌曲总时长
    private int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    //得到当前播放位置
    private int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    //定位当前播放位置
    private void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    //设置播放模式-顺序，单曲，全部
    private void setPlayModel(int model) {
        playmodel = model;
        SharedPreferences.Editor ed=sp.edit();
        ed.putInt("playmode",model);
        ed.commit();
    }

    private int getPlayModel() {
        return playmodel;
    }

    private void pre() {

        setPrePosition();
        try {
            openPreAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据位置打开上一曲
    private void setPrePosition() {
        if (playmodel == MusicPlayerService.REPEAT_MODE_NORMAL) {
            //顺序
            currentPosition--;
            if (currentPosition < 0) {
                currentPosition = 0;
            }
        } else if (playmodel == MusicPlayerService.REPEAT_MODE_CURRENT) {
            //单曲循环

        } else if (playmodel == MusicPlayerService.REPEAT_MODE_ALL) {
            //全部循环
            currentPosition--;
            if (currentPosition < 0) {
                currentPosition = audioItems.size() - 1;
            }
        }
    }

    //设置上一曲位置
    private void openPreAudio() throws IOException {
        if (playmodel == MusicPlayerService.REPEAT_MODE_NORMAL) {
            //顺序
            if (currentPosition != 0) {
                openAudio(currentPosition);
            } else if (currentPosition == 0 && !isCompletion) {
                openAudio(currentPosition);
            }

        } else if (playmodel == MusicPlayerService.REPEAT_MODE_CURRENT) {
            //单曲循环
            openAudio(currentPosition);
        } else if (playmodel == MusicPlayerService.REPEAT_MODE_ALL) {
            //全部循环
            openAudio(currentPosition);
        }
    }


    private void next() {
        setNextPosition();
        try {
            openNextAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据位置打开下一曲
    private void openNextAudio() throws IOException {
        if (playmodel == MusicPlayerService.REPEAT_MODE_NORMAL) {
            //顺序
            if (currentPosition != audioItems.size() - 1) {
                openAudio(currentPosition);
            } else if (currentPosition == audioItems.size() - 1 && !isCompletion) {
                openAudio(currentPosition);
            }

        } else if (playmodel == MusicPlayerService.REPEAT_MODE_CURRENT) {
            //单曲循环
            openAudio(currentPosition);
        } else if (playmodel == MusicPlayerService.REPEAT_MODE_ALL) {
            //全部循环
            openAudio(currentPosition);
        }
    }

    //设置下一曲位置
    private void setNextPosition() {
        if (playmodel == MusicPlayerService.REPEAT_MODE_NORMAL) {
            //顺序
            currentPosition++;
            if (currentPosition > audioItems.size() - 1) {
                currentPosition = audioItems.size() - 1;
            }
        } else if (playmodel == MusicPlayerService.REPEAT_MODE_CURRENT) {
            //单曲循环

        } else if (playmodel == MusicPlayerService.REPEAT_MODE_ALL) {
            //全部循环
            currentPosition++;
            if (currentPosition > audioItems.size() - 1) {
                currentPosition = 0;
            }
        }
    }

    private boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    //发广播
    protected void notifyChange(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }
}
