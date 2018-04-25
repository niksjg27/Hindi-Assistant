package com.example.dell.assistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    ImageButton micp;
    TextView result;
    TextToSpeech tts;
    ListView lv;
    ArrayList<String> contactList;
    Socket s;
    String body, obj1;
    DataInputStream in;
    DataOutputStream out;
    Cursor cursor;
    HashMap<String,String> ContactMap;

    private static final int REQUEST_READ_CONTACTS = 444;

    public void populateContacts() {

        ContentResolver cr = getContentResolver();
        contactList = new ArrayList<String>();
        ContactMap=new HashMap<>();
        try {
            cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Test", "Inside function ");
        try {
            if (cursor.getCount() > 0) {
                Log.d("Test", "Inside if getcount");
                while (cursor.moveToNext()) {
//                    Log.d("Test", "Inside move to next");
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    //contactList.add(name);
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // Query phone here. Covered next
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                        while (phones.moveToNext()) {
                            String phoneNumberX = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            name=name.toLowerCase();
                            ContactMap.put(name,phoneNumberX);
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                populateContacts();
            }
        }).start();

        micp = (ImageButton) findViewById(R.id.mic);
        result = (TextView) findViewById(R.id.tv1);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(new Locale("en-US"));
                    tts.setSpeechRate(0.9f);
                }
            }
        });
        {
            micp.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            tts.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
                                            try {
                                                Thread.sleep(500);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
                                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                                            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en-US");
                                            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

                                            try {
                                                startActivityForResult(intent, 100);
                                            } catch (Exception e) {
                                                Log.e("test", "Error");
                                            }
                                        }


                                    }
            );
        }
        System.out.println("Yes it reaches here........");
    }


    @Override
    protected void onActivityResult(int req, int res, Intent i) {
        if (req == 100) {
            if (res == RESULT_OK) {
                ArrayList<String> msg = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                result.setText(msg.get(0));
                System.out.println("Yes it reaches here");
                Conn c = new Conn();
                c.execute(msg.get(0));

            }
        }
        if (req == 200) {
            Log.d("Test","Result before  OK");
            if (res == RESULT_OK) {
                Log.d("Test","Result is OK");
                ArrayList<String> msg = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                body = msg.get(0);
                result.setText(msg.get(0));
                Log.d("Test",body);
                doMsg(obj1,body);
            }

        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
    }

    public void doCall(String nameToFind) {
        if (!mayRequestContacts()) {
            return;
        }
        Log.d("Test","Permission accepted!!");
        int size=ContactMap.size();
        String temp=Integer.toString(size);
        Log.d("Test",temp);
                            boolean a = (ContactMap.containsKey(nameToFind));
                        if (a) {
                                final int REQUEST_PHONE_CALL = 1;
                                String b = "tel:";
                                String phoneCallUri = b + ContactMap.get(nameToFind);
                                Intent phoneCallIntent = new Intent(Intent.ACTION_CALL);
                                phoneCallIntent.setData(Uri.parse(phoneCallUri));
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                                    return;
                                }
                                startActivity(phoneCallIntent);

                            }
                        else{
                            tts.setLanguage(new Locale("hi"));
                            tts.speak(nameToFind + "आपकी संपर्क सूची में नहीं है!", TextToSpeech.QUEUE_FLUSH, null);
                        }


                        }

    public void doMsg(String nameToFind, String body) {
        if (!mayRequestContacts()) {
            return;
        }
        Log.d("Test","Permission accepted!!");
        int size=ContactMap.size();
        String temp=Integer.toString(size);
        Log.d("Test",temp);
        boolean a = (ContactMap.containsKey(nameToFind));
        if (a) {
            String b = "sms:";
            String smsUri = b + ContactMap.get(nameToFind);
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse(smsUri));
            smsIntent.putExtra("sms_body", body);
            startActivity(smsIntent);

        }
        else{
            tts.setLanguage(new Locale("hi"));
            tts.speak(nameToFind + "आपकी संपर्क सूची में नहीं है!", TextToSpeech.QUEUE_FLUSH, null);
        }


    }


    private class Conn extends AsyncTask<String,Void,String[]>{
        @Override
        protected void onPostExecute(final String[] s) {
            switch (s[0]){
                case "call":
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        doCall(s[1]);
                                    }
                                }).start();
                                break;
                case "msg":
                                if(s[2].equals("None"))
                                {
                                    obj1 = s[1];
                                    Log.d("Test","Inside second tts");



//                                    try {
//                                        Thread.sleep(
//                                                2000
//                                        );
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }

                                    tts.setLanguage(new Locale("hi"));
                                    tts.speak("क्या संदेश भेजना है।", TextToSpeech.QUEUE_FLUSH, null);
                                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
                                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                                        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en-US");
                                        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                                    try {  Thread.sleep(2000);
                                    }
                                    catch (Exception e ){
                                        e.printStackTrace();
                                    }
                                    try {
                                        startActivityForResult(intent, 200);
                                    } catch (Exception e) {
                                        e.printStackTrace();                                    }


                                }
                                else{
                                    doMsg(s[1],s[2]);
                                }


                                break;

                default:        result.setText(s[0]);
                                tts.setLanguage(new Locale("hi"));
                                tts.speak("शमा कीजिए । मैंं आपकी क्या सहायता कर सकती हूँ ।", TextToSpeech.QUEUE_FLUSH, null);
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en-US");
                                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                                try {  Thread.sleep(4500);
                                }
                                catch (Exception e ){
                                    e.printStackTrace();
                                }

                                try {
                                    startActivityForResult(intent, 100);
                                } catch (Exception e) {
                                    Log.e("test", "Error");
                                }
            }
        }

        @Override
        protected String[] doInBackground(String... strings) {
            String msg1 = strings[0];
            Log.d("Test", msg1);

            String res = "";
            String [] parts = null;
            try{
                s = new Socket("192.168.0.5",1234);
//                in = new DataInputStream(s.getInputStream());
//                out = new DataOutputStream(s.getOutputStream());
//                out.writeBytes(msg1);
//                out.flush();
//                res = in.readLine();
                PrintWriter outtoserver = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                outtoserver.print(msg1);
                outtoserver.flush();
                BufferedReader in1 = new BufferedReader(new InputStreamReader(s.getInputStream()));
                res = in1.readLine();
                parts = res.split("_");
                Log.d("Test","hello;;;;;"+ res);
                Log.d("Test","function "+ parts[0]);
                Log.d("Test","object "+ parts[1]);
                Log.d("Test","body "+ parts[2]);


            }catch (Exception e){
                e.printStackTrace();
            }
            return parts;
        }
    }
}



