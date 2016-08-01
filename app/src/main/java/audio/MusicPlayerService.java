package audio;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import domain.AudioItem;


public class MusicPlayerService extends Service {
    private ArrayList<AudioItem> audioItems;
    private  AudioItem currAudioItem;//当前播放音频信息
    private int currentPosition;
    private MediaPlayer mediaPlayer;
    private IMusicPlayerService.Stub iBinder=new IMusicPlayerService.Stub() {
        MusicPlayerService service=MusicPlayerService.this;
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
        public void seekTo() throws RemoteException {
            service.seekTo();
        }

        @Override
        public void setPlayModel(int model) throws RemoteException {
            service.setPlayModel(model);
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }
    };


    private MediaPlayer.OnPreparedListener mOnPreparedListener =new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            play();
        }
    };
    private MediaPlayer.OnCompletionListener mOnCompletionListener=new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            next();
        }
    };
    private MediaPlayer.OnErrorListener mOnErrorListener=new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_LONG).show();;
            return true;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getAllAudio();
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
                        item.setData(artist);

                        audioItems.add(item);
                    }
                }
            }
        }.start();
    }

    //根据位置打开
    private void openAudio(int position) throws IOException {
        currentPosition=position;
        currAudioItem=audioItems.get(position);
        //释放资源
        if (mediaPlayer!=null){
            mediaPlayer.reset();
            mediaPlayer=null;
        }
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mediaPlayer.setOnErrorListener(mOnErrorListener);
        mediaPlayer.setDataSource(currAudioItem.getData());
        mediaPlayer.prepareAsync();//异步准备，一般用这个方法
//        mediaPlayer.prepare();//同步准备,一般本地资源用它
    }
    //播放
    private void play(){
        if (mediaPlayer!=null){
            mediaPlayer.start();
        }
    }
    //暂停
    private void pause(){
        if (mediaPlayer!=null){
            mediaPlayer.pause();
        }
    }

    //得到歌曲名称
    private String getName(){
        return null;
    }
    //得到艺术家
    private String getArtist(){
        return null;
    }
    //得到歌曲总时长
    private int getDuration(){
        return  0;
    }
    //得到当前播放位置
    private int getCurrentPosition(){
        return  0;
    }
    //定位当前播放位置
    private void seekTo(){
    }
    //设置播放模式-顺序，单曲，全部
    private void setPlayModel(int model){
    }
    private void pre(){}
    private void next(){}
}
