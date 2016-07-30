package audio;

import android.content.Intent;
import android.os.IBinder;

interface IMusicPlayerService {
   void openAudio(int position);
   void play();
   void pause();
   String getName();
   String getArtist() ;
   int getDuration();
   int getCurrentPosition();
   void seekTo();
   void setPlayModel(int model);
   void pre();
   void next();
}
