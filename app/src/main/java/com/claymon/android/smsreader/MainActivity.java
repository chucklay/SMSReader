package com.claymon.android.smsreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.net.URI;


public class MainActivity extends ActionBarActivity {

    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;

    private Reader reader;
    private ToggleButton button;
    private CompoundButton.OnCheckedChangeListener toggleListener;

    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (ToggleButton) findViewById(R.id.reader_toggle);

        toggleListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    reader.setAllowed(true);
                    reader.read(getString(R.string.enable_message));
                }
                else{
                    reader.read(getString(R.string.disable_message));
                    reader.setAllowed(false);
                }
            }
        };

        button.setOnCheckedChangeListener(toggleListener);

        checkTTS();
        initializeSMSReceiver();
        registerSMSReceiver();

    }

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CHECK_CODE){
            reader = new Reader(this);
        }
        else{
            Intent install = new Intent();
            install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(install);
        }
    }

    private void initializeSMSReceiver(){
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    Object[] pdus = (Object[])bundle.get("pdus");
                    for(int i = 0; i < pdus.length; i++){
                        byte[] pdu = (byte[])pdus[i];
                        SmsMessage message = SmsMessage.createFromPdu(pdu);
                        String text = message.getDisplayMessageBody();
                        String sender = getContactName(message.getOriginatingAddress());
                        reader.pause(LONG_DURATION);
                        if(sender != null){
                            reader.read("Incoming message from " + sender + "!");
                        }
                        else{
                            reader.read("Incoming message from " + message.getOriginatingAddress() + "!");
                        }
                        reader.pause(SHORT_DURATION);
                        reader.read(text);
                    }
                }
            }
        };
    }

    private void registerSMSReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }

    private String getContactName(String phoneNum){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum));
        String projection[] = new String[] {ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToFirst()){
            return cursor.getString(0);
        }
        else{
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(smsReceiver);
        reader.destroy();
    }
}
