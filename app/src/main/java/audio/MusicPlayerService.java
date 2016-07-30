package audio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class MusicPlayerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //根据位置打开
    private void openAudio(int position){

    }
    //播放
    private void play(){

    }
    //暂停
    private void pause(){}

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
