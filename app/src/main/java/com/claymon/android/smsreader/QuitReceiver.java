package com.claymon.android.smsreader;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Charlie on 2/16/2015.
 */
public class QuitReceiver extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.onDestroy();
    }
}
