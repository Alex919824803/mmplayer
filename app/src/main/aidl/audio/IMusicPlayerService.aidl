package audio;

import android.content.Intent;
import android.os.IBinder;

interface IMusicPlayerService {
   //发广播
   void notifyChange(String notify);
   boolean isPlaying();
   void openAudio(int position);
   void play();
   void pause();
   String getName();
   String getArtist() ;
   int getDuration();
   int getCurrentPosition();
   void seekTo(int position);
   void setPlayModel(int model);
   int getPlayModel();
   void pre();
   void next();
}
