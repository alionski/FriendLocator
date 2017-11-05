package aliona.mah.se.friendlocator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import aliona.mah.se.friendlocator.beans.Group;
import aliona.mah.se.friendlocator.util.BitmapResiser;
import aliona.mah.se.friendlocator.interfaces.ChatListCallback;
import aliona.mah.se.friendlocator.util.Config;
import aliona.mah.se.friendlocator.util.Downloader;
import aliona.mah.se.friendlocator.interfaces.GroupsFragmentCallback;
import aliona.mah.se.friendlocator.interfaces.MapFragmentCallback;
import aliona.mah.se.friendlocator.util.ServerService;
import aliona.mah.se.friendlocator.util.Uploader;
import aliona.mah.se.friendlocator.beans.ImageMessage;
import aliona.mah.se.friendlocator.beans.Member;
import aliona.mah.se.friendlocator.beans.TextMessage;
import layout.ChatFragment;
import layout.GroupsFragment;
import layout.MapFragment;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GroupsFragmentCallback,
        MapFragmentCallback,
        ChatListCallback,
        Downloader.DownloadListener {

    public static final String TAG = MainActivity.class.getName();
    public static final String SAVED_GROUPS = "saved_groups";
    public static final String SAVED_MEMBERS = "saved_members";
    public static final String SAVED_MESSAGES = "saved_messages";
    public static final String SAVED_PHOTO = "saved_photo";
    public static final String SAVED_URI = "photo_uri";

    public static final String GROUPS_TAG = "groups_fragment";
    public static final String CHAT_TAG = "chat_fragment";
    public static final String MAP_TAG = "map_fragment";

    public static final int GROUPS_ID = 0;
    public static final int CHAT_ID = 1;
    public static final int MAP_ID = 2;
    private static int CURRENT_FRAGMENT;

    static final int REQUEST_IMAGE_CAPTURE = 7776;
    static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 7777;
    static final int REQUEST_CHECK_LOCATION_SETTINGS = 7778;

    private IncomingServiceConnection mServiceConnection;
    private ServerService mService;

    private BroadcastReceiver mMessageReceiver;
    private LocalBroadcastManager mListener;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private HashMap<String, Group> mGroups = new HashMap<>();
    private HashMap<String, ArrayList<Member>> mMembers = new HashMap<>();
    private HashMap<String, ArrayList<Parcelable>> mMessages = new HashMap<>();
    private Bitmap mImageToDispatch;
    private Uri mPictureUri;

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // Set up action bar.
        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.default_username);
        mUsername = sharedPref.getString(getString(R.string.username), defaultValue);

        Intent intent = new Intent(this, ServerService.class);
        startService(intent);

        createBroadcastReceiver();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, null)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreInstanceState(Bundle savedInstanceState) {
        mGroups = (HashMap<String, Group>) savedInstanceState.getSerializable(SAVED_GROUPS);
        mMessages = (HashMap<String, ArrayList<Parcelable>>) savedInstanceState.getSerializable(SAVED_MESSAGES);
        mMembers = (HashMap<String, ArrayList<Member>>) savedInstanceState.getSerializable(SAVED_MEMBERS);
        mPictureUri = savedInstanceState.getParcelable(SAVED_URI);
        if (savedInstanceState.getByteArray(SAVED_PHOTO) != null) {
            mImageToDispatch = BitmapResiser.fromBytesToBitmap(savedInstanceState.getByteArray(SAVED_PHOTO));
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mServiceConnection = new IncomingServiceConnection();

        registerBroadcastListeners();

        Intent intent = new Intent(this, ServerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (mGoogleApiClient.isConnected()) {
            checkLocationSettings();
        }

        setFragment(CURRENT_FRAGMENT,  null);
    }


    private void setFragment(int idNumber, Group group) {
        FragmentManager fm = getSupportFragmentManager();
        CURRENT_FRAGMENT = idNumber;

        switch(idNumber) {
            case GROUPS_ID:

                if (mService != null) {
                    mService.getGroupsList();
                }

                GroupsFragment groups = (GroupsFragment) fm.findFragmentByTag(GROUPS_TAG);

                if (groups == null) {
                    groups = new GroupsFragment();
                }

                fm.beginTransaction()
                        .replace(R.id.fragment_main_holder, groups, GROUPS_TAG)
                        .commit();

                setTitle(R.string.app_name);
                break;

            case CHAT_ID:
                ChatFragment chat = (ChatFragment) fm.findFragmentByTag(CHAT_TAG);

                if (chat == null) {
                    chat = ChatFragment.newInstance(mUsername, group);
                }

                fm.beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.fragment_main_holder, chat, CHAT_TAG)
                        .addToBackStack(null)
                        .commit();
                break;

            case MAP_ID:
                MapFragment map = (MapFragment) fm.findFragmentByTag(MAP_TAG);

                if (map == null) {
                    map = MapFragment.newInstance(group);
                }

                fm.beginTransaction()
                        .replace(R.id.fragment_main_holder, map, MAP_TAG)
                        .addToBackStack(null)
                        .commit();

                setTitle(R.string.tab_map);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appbar_button_settings:

                showChangeNameDialog();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showChangeNameDialog() {

        final EditText enterName = new EditText(this);
        enterName.setPadding(40, 40, 40, 40);
        enterName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        enterName.setTextSize(22);
        enterName.setHint(getUsername());

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder
                .setTitle(R.string.change_name_text)
                .setView(enterName)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .setPositiveButton(R.string.change_name_done,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = enterName.getText().toString();
                                if (!name.equals("")) {
                                    setUsername(name);
                                }
                                dialogInterface.dismiss();
                            }
                        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void setUsername(String name) {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.username), name);
        editor.apply();

        mUsername = name;
    }

    public String getUsername() {
        return mUsername;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_tollbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            CURRENT_FRAGMENT = GROUPS_ID;
            setTitle(R.string.app_name);
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    ///////////////////////////////////// INTERFACES METHODS /////////////////////////////////////////////////////////////


    @Override
    public void notifyJoinedStatusChanged(String groupName, boolean isJoined) {
        Group group = mGroups.get(groupName);
        if (!isJoined) {
            mService.requestUnregister(group.getMyGroupId());
        } else {
            mService.requestRegister(groupName, getUsername());
        }
    }

    @Override
    public void startNewGroup(String groupName) {
        mService.requestRegister(groupName, getUsername());
    }

    @Override
    public void showChat(String groupName) {
        setFragment(CHAT_ID, mGroups.get(groupName));
    }

    @Override
    public ArrayList<Group> requestUpdateGroups() {
        return new ArrayList<Group>(mGroups.values());
    }

    @Override
    public void showMap(String groupName) {
        setFragment(MAP_ID, mGroups.get(groupName));
    }

    @Override
    public HashMap<String, ArrayList<Member>> getMembers() {
        return mMembers;
    }

    @Override
    public LatLng requestLocationUpdate() {
        if (mLastLocation != null) {
            return new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
        return null;
    }

    @Override
    public ArrayList<Member> requestMembersUpdate(String groupName) {
        return mMembers.get(groupName);
    }

    @Override
    public void onSendTextMessage(String myId, String messageText) {
        mService.sendTextMessage(myId, messageText);
    }

    @Override
    public void onSendImageMessage(String myId, String messageText) {
        ImageMessage imgMsg = new ImageMessage();
        imgMsg.setFrom(myId);
        imgMsg.setText(messageText);
        imgMsg.setLatitude(String.valueOf(mLastLocation.getLatitude()));
        imgMsg.setLongitude(String.valueOf(mLastLocation.getLongitude()));
        imgMsg.setImage(mImageToDispatch);

        mService.sendPictureMessage(imgMsg);
    }

    @Override
    public void startImgUpload() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPictureUri = BitmapResiser.generateURI();
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String pathToPicture = mPictureUri.getPath();
            mImageToDispatch = BitmapResiser.getScaled(pathToPicture,
                    300, 300);
            ChatFragment frag = (ChatFragment) getSupportFragmentManager().findFragmentByTag(CHAT_TAG);
            if (frag != null) {
                frag.notifyPhotoIsReady();
            }
        }
    }

    @Override
    public boolean imgIsReady() {
        return mImageToDispatch != null;
    }

    @Override
    public ArrayList<Parcelable> requestReadMessages(String groupName) {
        return mMessages.get(groupName);
    }

    @Override
    public void imageDownloaded(ImageMessage msg) {
        receiveMessage(msg.getGroup(), msg);
    }

    /////////////////////////////////////////////// SERVICE SPECIFIC ///////////////////////////////////////////////////////////////////

    private void createBroadcastReceiver() {
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch(action) {
                    case Config.REPLY_REGISTER:

                        String groupName = intent.getStringExtra(Config.GROUP);
                        String groupId = intent.getStringExtra(Config.ID);

                        if (mMembers.get(groupName) == null) {
                            mMembers.put(groupName, new ArrayList<Member>());
                        }
                        mMembers.get(groupName).add(new Member(getUsername()));

                        if (mGroups.get(groupName) == null) {
                            mGroups.put(groupName, new Group(groupName));
                        }
                        mGroups.get(groupName).setMyGroupId(groupId);

                        mService.getGroupsList();

                        break;

                    case Config.REPLY_UNREGISTER:

                        String idToDelete = intent.getStringExtra(Config.ID);

                        String groupToLeave = null;

                        for (Group groupToRemove : mGroups.values()) {
                            if (idToDelete.equals(groupToRemove.getMyGroupId())) {
                                groupToRemove.setMyGroupId(null);
                                groupToLeave = groupToRemove.getGroupName();
                                mGroups.put(groupToRemove.getGroupName(), groupToRemove);
                            }
                        }

                        ArrayList<Member> membs = mMembers.get(groupToLeave);

                        for (Member memb : membs) {
                            if (memb.getMemberName().equals(getUsername())) {
                                mMembers.get(groupToLeave).remove(memb);
                                break;
                            }
                        }

                        mService.getGroupsList();

                        break;

                    case Config.REPLY_MEMBERS:

                        String groupName2 = intent.getStringExtra(Config.GROUP);
                        ArrayList<String> names = intent.getStringArrayListExtra(Config.MEMBERS_ARRAY);

                        if (mMembers.get(groupName2) == null) {
                            mMembers.put(groupName2, new ArrayList<Member>());
                        }

                        ArrayList<Member> newMembers = new ArrayList<>();

                        for (String name : names) {
                            boolean found = false;
                            for (Member memb : mMembers.get(groupName2)) {
                                if (memb.getMemberName().equals(name)) {
                                    newMembers.add(memb);
                                    found = true;
                                }
                            }
                            if (!found) {
                                newMembers.add(new Member(name));
                            }
                        }

                        mMembers.put(groupName2, newMembers);

                        updateGroupsUI();

                        break;

                    case Config.REPLY_GROUPS:

                        ArrayList<Group> groups = intent.getParcelableArrayListExtra(Config.GROUPS_ARRAY);
                        HashMap<String, Group> tempMap = new HashMap<>();

                        if (!mGroups.isEmpty()) {
                            // swapping the groups from the server, adding new ones and getting rid of non-existent
                            for (Group serverGroup : groups) {

                                String serverName = serverGroup.getGroupName();
                                if (mGroups.get(serverName) == null) {
                                    tempMap.put(serverName, serverGroup);

                                } else {
                                    tempMap.put(serverName, mGroups.get(serverName));
                                }
                            }

                            mGroups = tempMap;

                        } else {
                            // initialising after the first update
                            for (Group serverGroup : groups) {
                                mGroups.put(serverGroup.getGroupName(), serverGroup);
                            }
                        }

                        for (Group group : mGroups.values()) {
                            mService.getGroupMembers(group.getGroupName());
                        }
                        break;

                    case Config.UPDATE_LOCATIONS:

                        String locGroupName = intent.getStringExtra(Config.GROUP);
                        ArrayList<Member> memberLocations = intent.getParcelableArrayListExtra(Config.GROUP_LOCATIONS);
                        // overwriting all members with new locations, thus also deleting non-existing users;
                        mMembers.put(locGroupName, memberLocations);

                        if (CURRENT_FRAGMENT == MAP_ID) {
                            MapFragment frag = (MapFragment) getSupportFragmentManager().findFragmentByTag(MAP_TAG);
                            frag.updateLocations();
                        }

                        mService.getGroupsList();

                        break;

                    case Config.UPDATE_EXCEPTION:

                        String error = intent.getStringExtra(Config.EXCEPTION_MSG);
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                        break;

                    case Config.UPDATE_IMAGECHAT:

                        ImageMessage imgMsg = intent.getParcelableExtra(Config.IMG_OBJECT);
                        String id = intent.getStringExtra(Config.IMG_ID);
                        String port = intent.getStringExtra(Config.IMG_PORT);

                        startDownload(imgMsg, id, port);
                        break;

                    case Config.UPDATE_TEXTCHAT:

                        Parcelable msg = intent.getParcelableExtra(Config.TEXT_OBJ);
                        String textGroupName = ((TextMessage)msg).getGroup();

                        receiveMessage(textGroupName, msg);
                        break;

                    case Config.REPLY_UPLOAD:

                        String imageId = intent.getStringExtra(Config.IMG_ID);
                        String portUp = intent.getStringExtra(Config.IMG_PORT);
                        new Uploader(imageId, portUp, mImageToDispatch);
                        mImageToDispatch = null;
                        mPictureUri = null;
                        break;
                }
//                debugPrintMap();
            }
        };
    }

    private void startDownload(ImageMessage imgMsg, String id, String port) {
        new Downloader(this, imgMsg, id, port);
    }

    private void receiveMessage(String groupName, Parcelable msg) {

        if (mMessages.get(groupName) == null) {
            mMessages.put(groupName, new ArrayList<Parcelable>());
        }

        mMessages.get(groupName).add(msg);

        ChatFragment frag = (ChatFragment) getSupportFragmentManager().findFragmentByTag(CHAT_TAG);
        if (CURRENT_FRAGMENT == CHAT_ID && frag.getCurrentGroup().equals(groupName)) {
            frag.updateBubblesAdapter();
        }
    }

    private void updateGroupsUI() {

        if (CURRENT_FRAGMENT != GROUPS_ID) {
            return;
        }
        GroupsFragment frag = (GroupsFragment) getSupportFragmentManager().findFragmentByTag(GROUPS_TAG);
        frag.updateGroupsList();
    }

    private void registerBroadcastListeners() {

        mListener = LocalBroadcastManager.getInstance(this);

        IntentFilter filter = new IntentFilter(ServerService.TAG);
        filter.addAction(Config.REPLY_REGISTER);
        filter.addAction(Config.REPLY_UNREGISTER);
        filter.addAction(Config.REPLY_MEMBERS);
        filter.addAction(Config.REPLY_GROUPS);
        filter.addAction(Config.REPLY_LOCATION);
        filter.addAction(Config.REPLY_UPLOAD);
        filter.addAction(Config.UPDATE_LOCATIONS);
        filter.addAction(Config.UPDATE_EXCEPTION);
        filter.addAction(Config.UPDATE_IMAGECHAT);
        filter.addAction(Config.UPDATE_TEXTCHAT);

        mListener.registerReceiver(
                mMessageReceiver, filter);
    }

    private void unregisterBroadcastListeners() {
        mListener.unregisterReceiver(mMessageReceiver);
    }

    private class IncomingServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            ServerService.LocalBinder ls = (ServerService.LocalBinder) binder;
            mService = ls.getService();

            mService.getGroupsList();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            String cryingFace = new String(Character.toChars(0x1F622));
            Toast.makeText(getApplicationContext(), cryingFace + cryingFace + cryingFace
                    + "\n" + getResources().getString(R.string.disconnection_notice), Toast.LENGTH_LONG).show();
        }
    }

    /////////////////////////////////// LOCATION SPECIFIC ////////////////////////////////////////////////////

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GOOGLE API CLIENT CONNECTED");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null && mService != null && !mGroups.isEmpty()) {
            for (Group group : mGroups.values()) {
                if (group.getMyGroupId() != null) {
                    mService.sendPosition(
                            group.getMyGroupId(),
                            String.valueOf(mLastLocation.getLongitude()),
                            String.valueOf(mLastLocation.getLatitude()));
                }
            }
        }
        checkLocationSettings();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkLocationSettings();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

                return;
            case REQUEST_CHECK_LOCATION_SETTINGS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startLocationUpdates();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

                return;
        }

    }

    private void checkLocationSettings() {

        Log.i(TAG, "CHECKING LOCATION SETTINGS");

        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        startLocationUpdates();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG,  "LOCATION CHANGED: " + location.toString());
        mLastLocation = location;

        if (!mGroups.isEmpty() && mService != null) {
            for (Group group : mGroups.values()) {
                if (group.getMyGroupId() != null) {
                    mService.sendPosition(
                            group.getMyGroupId(),
                            String.valueOf(mLastLocation.getLongitude()),
                            String.valueOf(mLastLocation.getLatitude()));
                }
            }
        }
    }

    @SuppressWarnings("Deprecated")
    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onPause() {
        unregisterBroadcastListeners();
        unbindService(mServiceConnection);
        // going in the background will cause the app to lose the service
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        Log.i(TAG, "GoogleApiClient in disconnected");
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SAVED_GROUPS, mGroups);
        outState.putSerializable(SAVED_MESSAGES, mMessages);
        outState.putSerializable(SAVED_MEMBERS, mMembers);
        outState.putParcelable(SAVED_URI, mPictureUri);
        if (mImageToDispatch != null) {
            outState.putByteArray(SAVED_PHOTO, BitmapResiser.fromBitmapToBytes(mImageToDispatch));
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * GoogleApiClient keeps leaking the activity. No easy solution present as it's a bug on Google's side.
     * https://github.com/googlesamples/android-play-location/issues/26
     */

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient = null;
        }
        super.onDestroy();
    }

    private void debugPrintMap() {
        Iterator it = mGroups.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            Log.d(TAG, "+++++++++++++++++++++++++++++ DEBUG PRINT MAP ++++++++++++++++++++++++++++++");
            Log.d(TAG, "HASH MAP KEY: " + pair.getKey());
            Group group = (Group) pair.getValue();
            Log.d(TAG, "---GROUP NAME: " + group.getGroupName());
            Log.d(TAG, "---GROUP JOINED: " + (group.getMyGroupId() == null ? "not joined" : group.getMyGroupId()));
            Log.d(TAG, "---GROUP MY ID: " + group.getMyGroupId());
        }
    }
}
