package aliona.mah.se.friendlocator.util;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.JsonWriter;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import aliona.mah.se.friendlocator.beans.Group;
import aliona.mah.se.friendlocator.beans.ImageMessage;
import aliona.mah.se.friendlocator.beans.Member;
import aliona.mah.se.friendlocator.beans.TextMessage;

/**
 * Service that is responsible for communication with the server.
 * It is both started _and_ bound. Started because we want to keep the service alive while the activity is rotating,
 * and bound because we want to be able to interact with it from MainActivity.
 * One it gets te first end-of-stream exception, it kills itself (it is only possible if the server has closed the socket,
 * and this is only possible if we haven't been updating our location for 2 min, which means the app is closed.
 * Created by aliona on 2017-10-22.
 */

public class ServerService extends Service {
    public static final String TAG = ServerService.class.getName();
    public static final String JSON_TYPE = "type";
    public final static String IP = "195.178.227.53";
    public final static int PORT = 7117;
    private HandlerThread mMainThread;
    private Handler mMainLoopHandler;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        Log.d(TAG, "ON CREATE");
        mMainThread = new MainThread(IP, PORT);
        mMainThread.start();
        mMainLoopHandler = new Handler(mMainThread.getLooper());
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ON START COMMAND");
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public ServerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServerService.this;
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

    public void getGroupMembers(String groupName) {
        Log.i("TO HANDLE", "getGroupMembers()" );
        StringWriter writer = new StringWriter();
        JsonWriter jWriter = new JsonWriter(writer);
        String result;
        try {
            jWriter.beginObject()
                    .name("type").value("members")
                    .name("group").value(groupName).
                    endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = writer.toString();
        Message msg = new Message();
        msg.obj = result;
        mMainLoopHandler.sendMessage(msg);
    }

    public void getGroupsList()  {
        Log.i("TO HANDLE", "getGroupsList()" );
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
        Log.d(TAG, "sendPosition(()" );
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

    private class MainThread extends HandlerThread {
        private String ip;
        private int port;
        private Socket socket;
        private DataOutputStream outputStream;
        private Handler handler;

        private MainThread(String ip, int port) {
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
                    String toSend = (String) msg.obj;
                    Log.d(TAG, " HANDLER : message received");
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

        private ReceiverThread(Socket socket) {

            this.socket = socket;
            broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
        }

        @Override
        public void run() {
            Log.d(TAG, "Receiver thread is starting");
            try {
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Can't connect to server: Can't get INPUT stream " + e);
                e.printStackTrace();
            }
            while (socket.isConnected()) {
                try {
                    String received = inputStream.readUTF();
                    Log.i(TAG, "SOMETHING RECEIVED");
                    JSONObject json = new JSONObject(received);
                    Log.i(TAG, json.toString() );
                    String type = json.getString(JSON_TYPE);

                    Log.i(TAG, type);

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

                        case Config.REPLY_LOCATION:

                            // reacting to it only not to get IOException
                            Log.d(TAG, "My locations has been registered, how nice");

                            break;

                        case Config.UPDATE_LOCATIONS:

                            String groupNameLocations = json.getString("group");
                            JSONArray locationsArray = json.getJSONArray("location");
                            JSONObject memberLocation;

                            ArrayList<Member> memberLocations = new ArrayList<>();
                            for (int i = 0; i < locationsArray.length(); i++) {
                                memberLocation = locationsArray.getJSONObject(i);
                                Member loc = new Member(
                                        memberLocation.getString(Config.MEMBER),
                                        memberLocation.getString(Config.LAT),
                                        memberLocation.getString(Config.LONG)
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

                            Log.i(TAG, "SERVICE ERROR: " + exception);

                            break;
                        case Config.UPDATE_TEXTCHAT:

                            if (!json.has(Config.GROUP) || !json.has(Config.MEMBER)) {
                                // means it's my own message that I just sent and I don't need to deal with at all
                                // I will receive it as a message to the chat anyway
                                // Results in System.err if not checked.
                                break;
                            }

                            String memberGroup = json.getString(Config.GROUP);
                            String memberName = json.getString(Config.MEMBER);

                            String messageText = json.getString(Config.TEXT);

                            Intent intentNewMessage = new Intent(ServerService.TAG);
                            intentNewMessage.setAction(Config.UPDATE_TEXTCHAT);
                            intentNewMessage.putExtra(Config.TEXT_OBJ,
                                    new TextMessage(memberGroup, memberName, messageText));
                            broadcaster.sendBroadcast(intentNewMessage);

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
                    Log.d(TAG, e.toString());
                } catch (IOException  exc) {
                    if (exc instanceof EOFException) {
                        try {
                            inputStream.close();
                            socket.close();
                            broadcaster = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // means the server has closed the socket bc the app hasn't been sending location to groups for a while
                        // and this service can peacefully die. R.I.P.
                        wrapItUp();
                    }
                }
            }
        }
    }

    public void wrapItUp() {
        this.stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ON DESTROY");
        mMainThread.quit();
        super.onDestroy();
    }
}
