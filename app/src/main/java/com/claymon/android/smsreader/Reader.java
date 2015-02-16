package com.claymon.android.smsreader;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Charlie on 2/15/2015.
 */
public class Reader implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    private boolean ready = false;
    private boolean allowed = false;

    public Reader (Context context){
        tts = new TextToSpeech(context, this);
    }
    public boolean isAllowed(){
        return allowed;
    }
    public void setAllowed(boolean allowed){
        this.allowed = allowed;
    }

    @Override
    public void onInit(int status){
        if(status == TextToSpeech.SUCCESS){
            tts.setLanguage(Locale.US);
            ready = true;
        }
        else{
            ready = false;
        }
    }

    public void read(String text){
        //Read the given text.
        if(ready && allowed) {
            if(Build.VERSION.SDK_INT >= 21) {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
            }
            else{
                HashMap<String, String> hMap = new HashMap<String, String>();
                hMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
                tts.speak(text, TextToSpeech.QUEUE_ADD, hMap);
            }
        }
    }

    public void pause(int time_wait){
        if(Build.VERSION.SDK_INT >= 21) {
            tts.playSilentUtterance(time_wait, TextToSpeech.QUEUE_ADD, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }
        else{
            tts.playSilence(time_wait, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void destroy(){
        tts.shutdown();
    }
}
