package com.boomboxbeilstein;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

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


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_chat);

            messageAdapter = new MessageAdapter(this);
            messagesView = (ListView) findViewById(R.id.messages_view);
            messagesView.setAdapter(messageAdapter);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
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
                    scaledrone.subscribe(roomName, ChatActivity.this);

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
    public void createUser(String uuid,String name){
        String api = "http://37.120.178.44:8000/chat/check?"+uuid+"&name="+name;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(api)
                .build();

        client.newCall(request);

    }

    public void chatDenied(){
        AlertDialog.Builder banmsg = new AlertDialog.Builder(this);
        banmsg.setTitle("Du wurdest gebannt");
        banmsg.setMessage("Du wurdest vom BoomBox Team aus dem Chat gebannt. Du kannst aber weiterhin Mails ins Studio schicken.");
        banmsg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.cancel();
            }
        });
        AlertDialog bandialog = banmsg.create();
        bandialog.show();
    }

    private int checkBan() {
        String api = "http://37.120.178.44:8000/chat/check?";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(api)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                res = "2";
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                res = response.body().string();
            }
        });

        System.out.println("Erkennen "+res);
        return 0;

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



