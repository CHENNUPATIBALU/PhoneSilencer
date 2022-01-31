package com.phonesilencer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.widget.Toast;

public class AudioManagerHelper{

    AudioManager audioManager;
    Context context;

    public AudioManagerHelper(Context context){
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    public void setAudioToSilent() throws SecurityException{
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void setAudioToNormal(){
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void setAudioToVibration(){
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }
}
