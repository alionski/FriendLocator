package aliona.mah.se.friendlocator.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import beans.Group;
import aliona.mah.se.friendlocator.MainActivity;
import aliona.mah.se.friendlocator.R;
import beans.ImageMessage;
import beans.MemberLocation;
import beans.TextMessage;

/**
 * Created by aliona on 2017-10-22.
 */

public class ServerService extends Service {
    public static final String TAG = ServerService.class.getName();

    public static final String JSON_TYPE = "type";

    private Socket mSocket;
    public final static String IP = "ip";
    public final static String PORT = "port";
    private String serverIp;
    private int serverPort;
    private HandlerThread mMainThread;
    private Handler mMainLoopHandler;

    public static final int mNotificationId = 888888;
    public static final String CHANNEL_ID = "friend_locator_incoming_message";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null) { // intent null vid återstart vid START_STICKY
            Bundle bundle = intent.getExtras();
            serverIp = bundle.getString(IP);
            serverPort = bundle.getInt(PORT);

            mMainThread = new MainThread(serverIp, serverPort);
            mMainThread.start();
            mMainLoopHandler = new Handler(mMainThread.getLooper());
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
        public ServerService getService() {
            return ServerService.this; // referens till Service-klassen
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

    public void requestUnregister(String myId) {
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

    // TODO: diffeent ids for different goups!
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

    public void sendPictureMessage(ImageMessage msg) {

        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("imagechat")
                    .name("id").value(msg.getFrom())
                    .name("text").value(msg.getText())
                    .name("longitude").value(msg.getLongitude())
                    .name("latitude").value(msg.getLatitude())
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message mess = new Message();
        mess.obj = result;
        mMainLoopHandler.sendMessage(mess);

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
                        if (outputStream != null) {
                            outputStream.writeUTF(toSend);
                            outputStream.flush();
                        }
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
        LocalBroadcastManager broadcaster;

        public ReceiverThread(Socket socket) {

            this.socket = socket;
            broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
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
                try {
                    String received = inputStream.readUTF();
                    Log.i("READ UTF: ", "SOMETHING RECEIVED");
                    JSONObject json = new JSONObject(received);
                    Log.i("IncomingMessageService", json.toString() );
                    String type = json.getString(JSON_TYPE);

                    Log.i("IncomingMessage TYPE", type);

                    switch (type) {
                        case Config.REPLY_REGISTER:

                            String groupName = json.getString("group");
                            String memberIdInGroup = json.getString("id"); // your id in the group you just registered
                            Log.i("REPLY REGISTER", groupName + " " + memberIdInGroup);

                            Intent intentRegister = new Intent(ServerService.TAG);
                            intentRegister.setAction(Config.REPLY_REGISTER);
                            intentRegister.putExtra(Config.GROUP, groupName);
                            intentRegister.putExtra(Config.ID, memberIdInGroup);
                            broadcaster.sendBroadcast(intentRegister);

                            break;
                        case Config.REPLY_UNREGISTER:

                            String unregisterId = json.getString("id");

                            Intent intentUnreg = new Intent(ServerService.TAG);
                            intentUnreg.setAction(Config.REPLY_UNREGISTER);
                            intentUnreg.putExtra(Config.ID, unregisterId);
                            broadcaster.sendBroadcast(intentUnreg );

                            Log.i(Config.REPLY_REGISTER, "SUCCESSFULLY UNREGISTERED");

                            break;
                        case Config.REPLY_MEMBERS:
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

                            Intent intentMembers = new Intent(ServerService.TAG);
                            intentMembers.setAction(Config.REPLY_MEMBERS);
                            intentMembers.putExtra(Config.GROUP, groupNameMembers);
                            intentMembers.putStringArrayListExtra(Config.MEMBERS_ARRAY, memberNameList);
                            broadcaster.sendBroadcast(intentMembers);

                            Log.i(Config.REPLY_REGISTER, "RECEIVED MEMBER LIST FOR GROUP " + groupNameMembers);

                            break;

                        case Config.REPLY_GROUPS:
                            // list of all groups registered on the server right now
                            // TODO: first sent a question
                            JSONArray groupsArray = json.getJSONArray("groups");
                            JSONObject groupJson;

                            Log.i(TAG, "JSON GROUPS" + groupsArray.toString());

                            ArrayList<Group> groups = new ArrayList<>();
                            for (int i=0; i<groupsArray.length(); i++) {
                                groupJson = groupsArray.getJSONObject(i);
                                String groupNameGroups = groupJson.getString(Config.GROUP);
                                Group group = new Group(groupNameGroups);
                                groups.add(group);
                            }

                            Intent intentGroupsList = new Intent(ServerService.TAG);
                            intentGroupsList.setAction(Config.REPLY_GROUPS);
                            intentGroupsList.putParcelableArrayListExtra(Config.GROUPS_ARRAY, groups);
                            broadcaster.sendBroadcast(intentGroupsList);

                            Log.i("GROUPS REPLY: ", groups.toString());
                            break;

                        case Config.UPDATE_LOCATIONS:

                            String groupNameLocations = json.getString("group");
                            JSONArray locationsArray = json.getJSONArray("location");
                            JSONObject memberLocation;
                            ArrayList<MemberLocation> memberLocations = new ArrayList<>();
                            for (int i = 0; i < locationsArray.length(); i++) {
                                memberLocation = locationsArray.getJSONObject(i);
                                MemberLocation loc = new MemberLocation(
                                        memberLocation.getString(Config.MEMBER),
                                        memberLocation.getString(Config.LONG),
                                        memberLocation.getString(Config.LAT)
                                );
                                memberLocations.add(loc);
                            }

                            Intent intentLocationsUpdate = new Intent(ServerService.TAG);
                            intentLocationsUpdate.setAction(Config.UPDATE_LOCATIONS);
                            intentLocationsUpdate.putExtra(Config.GROUP, groupNameLocations);
                            intentLocationsUpdate.putParcelableArrayListExtra(Config.GROUP_LOCATIONS, memberLocations);
                            broadcaster.sendBroadcast(intentLocationsUpdate);

                            break;
                        case Config.UPDATE_EXCEPTION:
                            String exception = json.getString("message");

                            Intent intentError = new Intent(ServerService.TAG);
                            intentError.setAction(Config.UPDATE_EXCEPTION);
                            intentError.putExtra(Config.EXCEPTION_MSG, exception);
                            broadcaster.sendBroadcast(intentError);

                            Log.i("SERVICE-SERVER ERROR: ", exception);

                            break;
                        case Config.UPDATE_TEXTCHAT:

                            String memberGroup = json.getString(Config.GROUP);
                            String memberName = json.getString(Config.MEMBER);

                            if (memberGroup == null || memberName == null) {
                                // means it's my own message that I just sent and Idon't need to deal with at all
                                // I will receive it as a message to the chat anyway
                                break;
                            }

                            String messageText = json.getString(Config.TEXT);

                            Intent intentNewMessage = new Intent(ServerService.TAG);
                            intentNewMessage.setAction(Config.UPDATE_TEXTCHAT);
                            intentNewMessage.putExtra(Config.TEXT_OBJ,
                                    new TextMessage(memberGroup, memberName, messageText));
                            broadcaster.sendBroadcast(intentNewMessage);

//                            notifyNewMessage(memberGroup + ":" + memberName, messageText);

                            break;

                        case Config.UPDATE_IMAGECHAT:

                            ImageMessage imgMsg = new ImageMessage();
                            imgMsg.setGroup(json.getString("group"));
                            imgMsg.setFrom(json.getString("member"));
                            imgMsg.setText(json.getString("text"));
                            imgMsg.setLongitude(json.getString("longitude"));
                            imgMsg.setLatitude(json.getString("latitude"));

                            Intent imageChat = new Intent(ServerService.TAG);
                            imageChat.setAction(Config.UPDATE_IMAGECHAT);
                            imageChat.putExtra(Config.IMG_OBJECT, imgMsg);
                            imageChat.putExtra(Config.IMG_ID, json.getString("imageid"));
                            imageChat.putExtra(Config.IMG_PORT, json.getString("port"));
                            broadcaster.sendBroadcast(imageChat);

                            break;

                        case Config.REPLY_UPLOAD:
                            String imageId = json.getString("imageid");
                            String port = json.getString("port");

                            Intent upload = new Intent(ServerService.TAG);
                            upload.setAction(Config.REPLY_UPLOAD);
                            upload.putExtra(Config.IMG_ID, imageId);
                            upload.putExtra(Config.IMG_PORT, port);
                            broadcaster.sendBroadcast(upload);

                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("INPUT STREAM PROBLEM: ", e.toString());
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
//        receives.;
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
