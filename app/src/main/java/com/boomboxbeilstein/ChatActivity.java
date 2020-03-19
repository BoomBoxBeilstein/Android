package com.boomboxbeilstein;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrone.lib.HistoryRoomListener;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity implements
        RoomListener {

        private String channelID = "ZHWM5tJHGgZfjdZg";
        private String roomName = "observable-room";
        private EditText editText;
        private Scaledrone scaledrone;
        private MessageAdapter messageAdapter;
        private ListView messagesView;
        public String res;
        private Thread thread;
        private Handler mHandler;
        private ImageButton btnsend;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_chat);

            messageAdapter = new MessageAdapter(this);
            messagesView = (ListView) findViewById(R.id.messages_view);
            messagesView.setAdapter(messageAdapter);

            checkBan();

            btnsend = (ImageButton) findViewById(R.id.sendmessage);


            //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
//            setSupportActionBar(toolbar);
            // User Check for Ban
           // if (checkBan() == 0){
              //  ;
            //}else{
              //  chatDenied();
            //}

            // This is where we write the message
            editText = (EditText) findViewById(R.id.editText);
            MemberData data = new MemberData(getName(), getRandomColor());

            //check if user/device is banned

            scaledrone = new Scaledrone(channelID, data);
            scaledrone.connect(new Listener() {
                @Override
                public void onOpen() {
                    System.out.println("Scaledrone connection open");
                    // Since the MainActivity itself already implement RoomListener we can pass it as a target
                    Room room = scaledrone.subscribe(roomName, ChatActivity.this);
                    room.listenToHistoryEvents(new HistoryRoomListener() {
                        @Override
                        public void onHistoryMessage(Room room, com.scaledrone.lib.Message message) {
                            System.out.println("Received a message from the past " + message.getData().asText());
                        }

                    });

                }

                @Override
                public void onOpenFailure(Exception ex) {
                    System.err.println(ex);
                }

                @Override
                public void onFailure(Exception ex) {
                    System.err.println(ex);
                }

                @Override
                public void onClosed(String reason) {
                    System.err.println(reason);
                }
            });

            final Handler handler = new Handler();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkBan();
                    handler.postDelayed(this, 20000);

                }
            }, 5000);

        }

        // Successfully connected to Scaledrone room
        @Override
        public void onOpen(Room room) {
            System.out.println("Conneted to room");
        }

        // Connecting to Scaledrone room failed
        @Override
        public void onOpenFailure(Room room, Exception ex) {
            System.err.println(ex);
        }

        // Received a message from Scaledrone room

    @Override
    public void onMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final MemberData data = mapper.treeToValue(receivedMessage.getMember().getClientData(), MemberData.class);
            boolean belongsToCurrentUser = receivedMessage.getClientID().equals(scaledrone.getClientID());
            final Message message = new Message(receivedMessage.getData().asText(), data, belongsToCurrentUser);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.add(message);
                    messagesView.setSelection(messagesView.getCount() - 1);
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }    public void sendMessage(View view) {
            String message = editText.getText().toString();
            if (message.length() > 0) {
                scaledrone.publish("observable-room", message);
                editText.getText().clear();
            }
        }

        private String getRandomColor() {
            Random r = new Random();
            StringBuffer sb = new StringBuffer("#");
            while(sb.length() < 7){
                sb.append(Integer.toHexString(r.nextInt()));
            }
            return sb.toString().substring(0, 7);
        }

        private String getName(){
            final SQLiteDatabase mydatabase = openOrCreateDatabase("chatuser",MODE_PRIVATE,null);
            Cursor resultSet = mydatabase.rawQuery("Select * from chatuser",null);
            resultSet.moveToFirst();
            String username = resultSet.getString(0);
            return username;
        }

        private String getuuid(){
        final SQLiteDatabase mydatabase = openOrCreateDatabase("chatuser",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from chatuser",null);
        resultSet.moveToFirst();
        String uuid = resultSet.getString(1);
        return uuid;
        }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.back:
                finish();
                return true;


            default:
                // Wenn wir hier ankommen, wurde eine unbekannt Aktion erfasst.
                // Daher erfolgt der Aufruf der Super-Klasse, die sich darum kümmert.
                return super.onOptionsItemSelected(item);



        }
    }


    public void chatBan(){

        AlertDialog.Builder banmsg = new AlertDialog.Builder(this);
        banmsg.setTitle("Ban");
        banmsg.setMessage("Du wurdest aus dem Chat gebannt und kannst ab sofort keine Nachrichten mehr verschicken. Du kannst weiterhin Mails ins Studio schreiben");
        banmsg.setPositiveButton("Ich war böse :|", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnsend.setEnabled(false);
                editText.setHint("Du warst böse!");
                dialog.cancel();
            }
        });
        AlertDialog bandialog = banmsg.create();
        bandialog.setCanceledOnTouchOutside(false);
        bandialog.show();
    }

    public void chatUnBan(){
        AlertDialog.Builder banmsg = new AlertDialog.Builder(this);
        banmsg.setTitle("Entbannung");
        banmsg.setMessage("Du wurdest entbannt, sei lieb, ansonsten schlägt der Bannhammer erneut zu!");
        banmsg.setPositiveButton("Ich werde lieb sein", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnsend.setEnabled(true);
                editText.setHint("Write a message");
                dialog.cancel();
            }
        });
        AlertDialog bandialog = banmsg.create();
        bandialog.setCanceledOnTouchOutside(false);
        bandialog.show();

    }



    public void chatUnavailable(){
        AlertDialog.Builder banmsg = new AlertDialog.Builder(this);
        banmsg.setTitle("Fehler");
        banmsg.setMessage("Aus technischen Gründen ist die Benutzerverwaltung zur Zeit nicht erreichbar. Das Team ist informiert und wird das Problem so schnell wie möglich lösen!");
        banmsg.setPositiveButton("OK :(", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnsend.setEnabled(false);
                editText.setHint("Chat nicht verfügbar");
                dialog.cancel();
            }
        });
        AlertDialog bandialog = banmsg.create();
        bandialog.show();
    }

    private void checkBan() {
        String name = getName();
        String uuid = getuuid();
        String api = "http://37.120.178.44:8000/chat/check?uuid="+uuid+"&name="+name;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(api)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                chatUnavailable();
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                res = response.body().string();
                try {
                    JSONObject Jobject = new JSONObject(res);
                    String status = Jobject.getString("status");
                    int statusint = Integer.parseInt(status);
                    System.out.println(statusint);
                    if (statusint == 1){
                        System.out.println("Here"+statusint);
                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (btnsend.isEnabled()){
                                    chatBan();
                                } else{
                                    //Use already banned
                                    ;
                                }

                            }
                        });

                    } else {
                        mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (btnsend.isEnabled()) {
                                    // User can write messages
                                } else {
                                    chatUnBan();
                                    ;
                                }

                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });




    }

    protected void onDestroy() {

        super.onDestroy();
    }


}
class MemberData {
    private String name;
    private String color;

    public MemberData(String name, String color) {
        this.name = name;
        this.color = color;
    }

    // Add an empty constructor so we can later parse JSON into MemberData using Jackson
    public MemberData() {
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "MemberData{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}



