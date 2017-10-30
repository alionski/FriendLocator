package aliona.mah.se.friendlocator.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import aliona.mah.se.friendlocator.MainActivity;
import aliona.mah.se.friendlocator.R;

/**
 * Created by aliona on 2017-10-22.
 */

public class IncomingMessageService extends Service {
    public static final String JSON_TYPE = "type";

    public static final String REPLY_REGISTER = "register";
    public static final String REPLY_UNREGISTER = "unregister";
    public static final String REPLY_MEMBERS = "members";
    public static final String REPLY_GROUPS = "groups";
    public static final String REPLY_LOCATION = "location";
    public static final String UPDATE_LOCATIONS = "locations";
    public static final String UPDATE_EXCEPTION = "exception";
    public static final String REPLY_IMAGECHAT = "imagechat";
    public static final String UPDATE_TEXTCHAT = "textchat";
    public static final String REPLY_UPLOAD = "upload";

    private Socket mSocket;
    public final static String IP = "ip";
    public final static String PORT = "port";
    private String serverIp;
    private int serverPort;
    private HandlerThread mMainThread;
    private Handler mMainLoopHandler;

    public static final int mNotificationId = 888888;
    public static final String CHANNEL_ID = "friend_locator_incoming_message";
    private boolean serviceRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceRunning = true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) { // intent null vid återstart vid START_STICKY
            Bundle bundle = intent.getExtras();
            serverIp = bundle.getString(IP);
            serverPort = bundle.getInt(PORT);

            mMainThread = new MainThread(serverIp, serverPort);
            mMainThread.start();
//            mMainLoopHandler = new Handler(mMainThread.getLooper());
            // TODO: send messages via handler
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalService();
    }


    public class LocalService extends Binder { // Binder implementerar IBinder
        public IncomingMessageService getService() {
            return IncomingMessageService.this; // referens till Service-klassen
        }
    }



    public void requestRegister(String groupName, String memberName) {
        StringWriter wr = new StringWriter();
        JsonWriter jWr = new JsonWriter(wr);
        String res;
        try {
            jWr.beginObject().name("type").value("register")
                    .name("group").value(groupName)
                    .name("member").value(memberName)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        res = wr.toString();
        Message msg = new Message();
        msg.obj = res;
        mMainLoopHandler.sendMessage(msg);
    }

    public void requestUnregister(String groupName, String myId) {
        Log.i("TO HANDLE", "requestUnregister()" );
        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("unregister")
                    .name("id").value(myId).
                    endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message msg = new Message();
        msg.obj = result;
        mMainLoopHandler.sendMessage(msg);
    }

    public void requestAllGroupMembers(String groupName) {
        Log.i("TO HANDLE", "requestAllGroupMembers()" );
        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("members")
                    .name("id").value(groupName).
                    endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message msg = new Message();
        msg.obj = result;
        mMainLoopHandler.sendMessage(msg);
    }

    public void requestAllGroupsList()  {
        Log.i("TO HANDLE", "requestAllGroupsList()" );
        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("groups").endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message msg = new Message();
        msg.obj = result;
        mMainLoopHandler.sendMessage(msg);
    }

    public void sendPosition(String myId, String longitude, String latitude) {
        Log.i("TO HANDLE", "sendPosition(()" );
        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("location")
                    .name("id").value(myId)
                    .name("longitude").value(longitude)
                    .name("latitude").value(latitude)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message msg = new Message();
        msg.obj = result;
        mMainLoopHandler.sendMessage(msg);
    }

    public void sendTextMessage(String userId, String text) {
        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("textchat")
                    .name("id").value(userId)
                    .name("text").value(text).endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message msg = new Message();
        msg.obj = result;
        mMainLoopHandler.sendMessage(msg);
    }

    public void sendPictureMessage(String userId, String text, String longitude, String latitude) {
        // TODO: will then respond with port where to save img so add String bitmap to a queue in service
        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("imagechat")
                    .name("id").value(userId)
                    .name("text").value(text)
                    .name("longitude").value(longitude)
                    .name("latitude").value(latitude)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message msg = new Message();
        msg.obj = result;
        mMainLoopHandler.sendMessage(msg);

    }

    public void setHandlerToParent(Handler handler) {
        mMainLoopHandler = handler;
    }

    // TODO: Note: HandlerThread needs to call myHandlerThread.quit() to free the resources and stop the execution of the thread.
    private class MainThread extends HandlerThread {
        private String ip;
        private int port;
        private Socket socket;
        private DataOutputStream outputStream;
        private Handler handler;

        public MainThread(String ip, int port) {
            super("MainHandlerThread");
            this.ip = ip;
            this.port = port;
        }

        @Override
        protected void onLooperPrepared() {
            try {
                socket = new Socket(ip, port);
                Thread receiver = new ReceiverThread(socket);
                receiver.start();
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    // process incoming messages here
                    // this will run in non-ui/background thread
                    String toSend = (String) msg.obj;
                    Log.i("HANDLER", ": message received");
                    try {
                        outputStream.writeUTF(toSend);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            setHandlerToParent(handler);
        }
    }

    private class ReceiverThread extends Thread {
        Socket socket;
        DataInputStream inputStream;

        public ReceiverThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Can't connect to server: Can't get INPUT stream " + e);
                e.printStackTrace();
            }
            while (socket.isConnected()) {
//                Log.i("RECEIVER THREAD", " IS RUNNING");
                try {
                    String received = inputStream.readUTF();
                    Log.i("READ UTF: ", "SOMETHING RECEIVED");
                    JSONObject json = new JSONObject(received);
                    Log.i("IncomingMessageService", json.toString() );
                    String type = json.getString(JSON_TYPE);

                    Log.i("IncomingMessage TYPE", type);

                    switch (type) {
                        case REPLY_REGISTER:
                            // TODO: first register -- this is the reply after registration
                            String groupName = json.getString("group");
                            String memberIdInGroup = json.getString("id"); // your id in the group you just registered
                            Log.i("REPLY REGISTER", groupName + " " + memberIdInGroup);
                            break;
                        case REPLY_UNREGISTER:
                            // TODO: first unregister, the reply is the same form as request
                            String unregisterId = json.getString("id");
                            break;
                        case REPLY_MEMBERS:
                            // list of all members registered in the given group
                            // TODO: first sent a question
                            String groupNameMembers = json.getString("group");
                            JSONArray membersArray = json.getJSONArray("members");
                            JSONObject member;
                            ArrayList<String> memberNameList = new ArrayList<>();
                            for (int i=0; i<membersArray.length(); i++) {
                                member = membersArray.getJSONObject(i);
                                String memberName = member.getString("member");
                                memberNameList.add(memberName);
                            }
                            break;
                        case REPLY_GROUPS:
                            // list of all groups registered on the server right now
                            // TODO: first sent a question
                            JSONArray groupsArray = json.getJSONArray("groups");
                            JSONObject group;
                            ArrayList<String> groupNameList = new ArrayList<>();
                            for (int i=0; i<groupsArray.length(); i++) {
                                group = groupsArray.getJSONObject(i);
                                String groupNameGroups = group.getString("group");
                                groupNameList.add(groupNameGroups);
                            }
                            Log.i("GROUPS REPLY: ", groupNameList.toString());
                            break;
                        case REPLY_LOCATION:
                            // TODO: first send an update with the same form
                            String idInGroup = json.getString("id");
                            String longitude = json.getString("longitude");
                            String latitude = json.getString("latitude");

                            break;
                        case UPDATE_LOCATIONS:
                            // TODO: unclear; the server just sends it after what request?..
                            String groupNameLocations = json.getString("group");
                            JSONArray locationsArray = json.getJSONArray("location");
                            JSONObject memberLocation;
                            HashMap<String, String[]> memberLocationLatLong = new HashMap<>();
                            for (int i = 0; i < locationsArray.length(); i++) {
                                memberLocation = locationsArray.getJSONObject(i);
                                String[] latlong = new String[2];
                                latlong[0] = memberLocation.getString("longitude");
                                latlong[1] = memberLocation.getString("latitude");
                                memberLocationLatLong.put(memberLocation.getString("member"), latlong);
                            }
                            break;
                        case UPDATE_EXCEPTION:
                            String exception = json.getString("message");
                            Log.i("SERVICE-SERVER ERROR: ", exception);
                            break;
                        case UPDATE_TEXTCHAT:
                            // TODO: are the answers the same if I send a text chat?
                            String memberId = json.getString("id");
                            String messageText = json.getString("text");
                            break;
                        case REPLY_IMAGECHAT:
                            // TODO: get the img in an asynk task and show to user
                            String imgChatGroup = json.getString("group");
                            String imgChatFrom = json.getString("member");
                            String imgChatText = json.getString("text");
                            String imgLong = json.getString("longitude");
                            String imgLat = json.getString("latitude");
                            String imgId = json.getString("imageid");
                            String imgPort = json.getString("port");
                            break;
                        case REPLY_UPLOAD:
                            String imageId = json.getString("imageid");
                            String port = json.getString("port");
                            // TODO: start new asynctask where you upload a pic to server knowing the port
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("INPUT STREAM PROBLEM: ", e.toString());
//                } catch (IOException e) {
////                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // https://developer.android.com/guide/topics/ui/notifiers/notifications.html
    public void notifyNewMessage(String title, String text) {
        // The id of the channel.
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_mail_orange_24dp)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setChannelId(CHANNEL_ID);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    @Override
    public void onDestroy() {
//        The system invokes this method when the service is no longer used and is being destroyed. Your service should implement
//        this to clean up any resources such as threads, registered listeners, or receivers. This is the last call that the service
//        receives.
        serviceRunning = false;
        // TODO: fix closing sockets,, this one is null
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
