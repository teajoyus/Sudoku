package com.hat_cloud.sudo.entry;

import android.content.Context;
import android.media.MediaPlayer;

import com.hat_cloud.sudo.activity.Prefs;

public class Music {
   private static MediaPlayer mp = null;

   /** Stop old song and start new one */
   
   public static void play(Context context, int resource) {
      stop();

      // Start music only if not disabled in preferences
      if (Prefs.getMusic(context)) {
         mp = MediaPlayer.create(context, resource);
         mp.setLooping(true);
         mp.start();
      }
   }
   

   /** Stop the music */
   public static void stop() {
      if (mp != null) {
         mp.stop();
         mp.release();
         mp = null;
      }
   }
}