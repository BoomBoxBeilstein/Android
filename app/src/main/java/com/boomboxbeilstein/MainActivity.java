package com.boomboxbeilstein;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Application;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pushbots.push.Pushbots;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    PowerManager pm;
    PowerManager.WakeLock wl;
    WifiManager wm;
    WifiManager.WifiLock wfl;

    SimpleExoPlayer player;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.CALL_PHONE};

    private Context context;
    private Toolbar toolbar;

    public String res;

    private String chatname = "";

    private BottomNavigationView bottomNavigationView;

    // API song and artist
    private String artist;
    private String song;



    private ImageButton buttonPlay;
    TextView statusText;
    ImageView cover;
    ImageView background;
    public String url= "https://boomboxbeilstein.de/api/status.php";







    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BoomBoxBeilstein:wakelock");

         wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         wfl = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "BoomBoxBeilstein:sync_all_wifi");

//        buttonPlay.setBackgroundResource(R.drawable.btn_play);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.Call:
                        callaction();

                        return true;
                    case R.id.action_settings:
                        openContact();
                        return true;

                    case R.id.chat:
                        chataction();
                        return true;


                }
                return false;
            }

        });



        buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(this);

        statusText = (TextView) findViewById(R.id.statusText);
        cover = (ImageView) findViewById(R.id.imageView);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));

        Pushbots.sharedInstance().init(this);
        Pushbots.sharedInstance().registerForRemoteNotifications();

        //Create Unique Device id for detection




        try {
            runx();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Handler handler = new Handler();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    runx();
                    picture();
                    handler.postDelayed(this, 20000);
                }catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 5000);

        context= getApplicationContext();
        checkPermissions();



    }

    public void callaction(){
        String phoneNumber = "015223450164";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Möchtest du uns anrufen?");
        builder.setMessage("Wenn du jetzt auf Ja drückst rufst du direkt bei unseren Moderatoren im Studio an. Der Anruf kostet entsprechend deinem Mobilfunk tarif.");
        builder.setPositiveButton("Ja",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        call();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    void runx() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://37.120.178.44:8000/live/song")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                statusText.setText("Error");

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();
                try {
                    JSONObject Jobject = new JSONObject(myResponse);
                    song = Jobject.getString("song");
                    artist = Jobject.getString("artist");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusText.setText(artist +  System.getProperty ("line.separator") + song);
                    }
                });

            }
        });
    }

    public void chataction(){
        //check for username - if not - ask for new one
        final SQLiteDatabase mydatabase = openOrCreateDatabase("chatuser",MODE_PRIVATE,null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS chatuser(user VARCHAR,identifier VARCHAR);");
        Cursor resultSet = mydatabase.rawQuery("Select * from chatuser",null);
        if (resultSet.getCount() == 0){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setCancelable(true);
            builder1.setTitle("Dein Chat-Name");
            builder1.setMessage("Jetzt brauchst du noch einen Benutzernamen, wähle ihn weise, danach kannst du ihn nicht mehr ändern.");
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder1.setView(input);
            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String chatname = input.getText().toString();
                    String uniqueID = UUID.randomUUID().toString();
                    System.out.println("Creating new device id");
                    mydatabase.execSQL("INSERT INTO chatuser VALUES('"+chatname+"','"+uniqueID+"');");
                    System.out.println("Identifier " + uniqueID);
                    openChat();

                }
            });
            builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog1 = builder1.create();
            dialog1.show();

        } else {
            System.out.println(resultSet.getCount());
            resultSet.moveToFirst();
            String user = resultSet.getString(0);
            String identifier = resultSet.getString(1);
            System.out.println("Identifier " + identifier + " username " + user);
            openChat();
        }

    }




    void picture(){
        ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
        imageLoader.displayImage("http://37.120.178.44/api/cover.jpeg", cover);


    }
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Um die Funktion 'Anruf ins Studio' nutzen zu können, musst du der App erlauben Anrufe zu tätigen.", Toast.LENGTH_LONG).show();

                        return;
                    }
                }
                // all permissions were granted
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu mainmenu) {
        //BottomNavigationView.inflate(this,R.menu.menu,);

        return true;
    }

    public boolean setOnNavigationItemSelectedListener(BottomNavigationView item){
        switch (item.getSelectedItemId()) {

            case R.id.Call:
                String phoneNumber = "015223450164";
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle("Möchtest du uns anrufen?");
                builder.setMessage("Wenn du jetzt auf Ja drückst rufst du direkt bei unseren Moderatoren im Studio an. Der Anruf kostet entsprechend deinem Mobilfunk tarif.");
                builder.setPositiveButton("Ja",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                call();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;

            case R.id.action_settings:
                openContact();
                return true;

            case R.id.chat:
                //check for username - if not - ask for new one
                final SQLiteDatabase mydatabase = openOrCreateDatabase("chatuser",MODE_PRIVATE,null);
                mydatabase.execSQL("CREATE TABLE IF NOT EXISTS chatuser(user VARCHAR,identifier VARCHAR);");
                Cursor resultSet = mydatabase.rawQuery("Select * from chatuser",null);
                if (resultSet.getCount() == 0){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setCancelable(true);
                    builder1.setTitle("Dein Chat-Name");
                    builder1.setMessage("Jetzt brauchst du noch einen Benutzernamen, wähle ihn weise, danach kannst du ihn nicht mehr ändern.");
                    final EditText input = new EditText(this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder1.setView(input);
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String chatname = input.getText().toString();
                            String uniqueID = UUID.randomUUID().toString();
                            System.out.println("Creating new device id");
                            mydatabase.execSQL("INSERT INTO chatuser VALUES('"+chatname+"','"+uniqueID+"');");
                            System.out.println("Identifier " + uniqueID);
                            openChat();

                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog1 = builder1.create();
                    dialog1.show();

                } else {
                    System.out.println(resultSet.getCount());
                    resultSet.moveToFirst();
                    String user = resultSet.getString(0);
                    String identifier = resultSet.getString(1);
                    System.out.println("Identifier " + identifier + " username " + user);
                    openChat();
                }

                return true;

            default:
                // Wenn wir hier ankommen, wurde eine unbekannt Aktion erfasst.
                // Daher erfolgt der Aufruf der Super-Klasse, die sich darum kümmert.
                return false;

        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Call:
                String phoneNumber = "015223450164";
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle("Möchtest du uns anrufen?");
                builder.setMessage("Wenn du jetzt auf Ja drückst rufst du direkt bei unseren Moderatoren im Studio an. Der Anruf kostet entsprechend deinem Mobilfunk tarif.");
                builder.setPositiveButton("Ja",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                call();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;

            case R.id.action_settings:
                openContact();
                return true;

            case R.id.chat:
                //check for username - if not - ask for new one
                final SQLiteDatabase mydatabase = openOrCreateDatabase("chatuser",MODE_PRIVATE,null);
                mydatabase.execSQL("CREATE TABLE IF NOT EXISTS chatuser(user VARCHAR,identifier VARCHAR);");
                Cursor resultSet = mydatabase.rawQuery("Select * from chatuser",null);
                if (resultSet.getCount() == 0){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setCancelable(true);
                    builder1.setTitle("Dein Chat-Name");
                    builder1.setMessage("Jetzt brauchst du noch einen Benutzernamen, wähle ihn weise, danach kannst du ihn nicht mehr ändern.");
                    final EditText input = new EditText(this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder1.setView(input);
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String chatname = input.getText().toString();
                            String uniqueID = UUID.randomUUID().toString();
                            System.out.println("Creating new device id");
                            mydatabase.execSQL("INSERT INTO chatuser VALUES('"+chatname+"','"+uniqueID+"');");
                            System.out.println("Identifier " + uniqueID);
                            openChat();

                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog1 = builder1.create();
                    dialog1.show();

                } else {
                    System.out.println(resultSet.getCount());
                    resultSet.moveToFirst();
                    String user = resultSet.getString(0);
                    String identifier = resultSet.getString(1);
                    System.out.println("Identifier " + identifier + " username " + user);
                    openChat();
                }

                return true;

            default:
                // Wenn wir hier ankommen, wurde eine unbekannt Aktion erfasst.
                // Daher erfolgt der Aufruf der Super-Klasse, die sich darum kümmert.
                return super.onOptionsItemSelected(item);

        }
    }




    public void openContact() {
        Intent intent = new Intent (this, ContactActivity.class);
        startActivity(intent);
    }

    public void openChat() {
        Intent intent = new Intent (this, ChatActivity.class);
        startActivity(intent);
    }


    public void call(){
        String phoneNumber = "015223450164";
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            Toast.makeText(MainActivity.this, "Fehlende Berechtigung", Toast.LENGTH_SHORT).show();
            // Permission is not granted
        }else{

                Toast
                        .makeText(MainActivity.this,
                                "Der Anruf wird gestartet",
                                Toast.LENGTH_SHORT)
                        .show();
                startActivity(callIntent);


        }

    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonPlay:
                if(player != null){
                    stopPlaying();
                    ((ImageButton) v).setImageResource(R.drawable.btn_play);


                }else{
                    startPlaying();
                    requestStatus();
                    ((ImageButton) v).setImageResource(R.drawable.btn_pause);


                }

                /*
                if(((Button)v).getText().equals("Start")) {
                    ((Button) v).setText("Stop");
                    startPlaying();
                    requestStatus();
                }else{

                    stopPlaying();
                    ((Button)v).setText("Start");
                }
                */
                break;


        }

    }
    public void initMediaPlayer(){
        context=MainActivity.this;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build();
        Uri uri = Uri.parse("http://boomboxbeilstein.de:8000/live");
        DataSource.Factory dataSourceFactory =
                new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "BoomBoxBeilstein"));
        MediaSource MediaSource =
                new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        player.prepare(MediaSource);
        player.setAudioAttributes(audioAttributes);
        wl.acquire();
        wfl.acquire();
        startService();


        player.setPlayWhenReady(true);
        this.player = player;
    }


    public void startPlaying() {


        //toolbar.setTitle("On Air");
        //toolbar.setSubtitle("Es läuft gerade:");

        super.onStart();
        initMediaPlayer();



        };



    public void stopPlaying() {
        if(player != null)
            super.onStop();
            player.stop();
            player.release();
            player= null;
            wl.release();
            wfl.release();
            stopService();
        //toolbar.setTitle("Stream gestoppt");
        //toolbar.setSubtitle("");



    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "BoomBoxBeilstein - Webradio läuft");

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
        player= null;
        wl.release();
        wfl.release();
        stopService();

    }



    public void requestStatus(){





    }

}