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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.design.widget.TabLayout;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.widget.EditText;

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

import aliona.mah.se.friendlocator.util.BitmapResiser;
import aliona.mah.se.friendlocator.util.ChatListCallback;
import aliona.mah.se.friendlocator.util.Config;
import aliona.mah.se.friendlocator.util.Downloader;
import aliona.mah.se.friendlocator.util.GroupsFragmentCallback;
import aliona.mah.se.friendlocator.util.MapFragmentCallback;
import aliona.mah.se.friendlocator.util.ServerService;
import aliona.mah.se.friendlocator.util.Uploader;
import beans.Group;
import beans.ImageMessage;
import beans.Member;
import beans.MemberLocation;
import beans.TextMessage;
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
    public static final int GROUPS_ID = 0;
    public static final int CHAT_ID = 1;
    public static final int MAP_ID = 2;
    private static int CURRENT_FRAGMENT;
    static final int REQUEST_IMAGE_CAPTURE = 7776;
    static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 7777;
    static final int REQUEST_CHECK_LOCATION_SETTINGS = 7778;

    private ViewPager mViewPager;
    private SwipeAdapter mAdapter;

    private IncomingServiceConnection mServiceConnection;
    private ServerService mIncMsgService;
    public final static String IP = "195.178.227.53";
    public final static int PORT = 7117;

    private BroadcastReceiver mMessageReceiver;
    private LocalBroadcastManager mListener;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private HashMap<String, Group> mGroups = new HashMap<>();
    private HashMap<String, ArrayList<Parcelable>> mMessages = new HashMap<>();
    private Bitmap mImageToDispatch;
    private Uri mPictureUri;

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.default_username);
        mUsername = sharedPref.getString(getString(R.string.username), defaultValue);

        Intent intent = new Intent(this, ServerService.class);
        intent.putExtra(ServerService.IP, IP);
        intent.putExtra(ServerService.PORT, PORT);
        startService(intent);

        createBroadcastReceiver();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        initialiseUI();

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
    }

    private void initialiseUI() {

        CURRENT_FRAGMENT = GROUPS_ID;

        mAdapter = new SwipeAdapter(getSupportFragmentManager());

        // Set up action bar.
        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_groups)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_chat)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_map)));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        // TODO: remove listener in onPause
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                CURRENT_FRAGMENT = tab.getPosition();
                mViewPager.setCurrentItem(tab.getPosition());

                if (CURRENT_FRAGMENT == GROUPS_ID) {

                    GroupsFragment frag = (GroupsFragment) getSupportFragmentManager().findFragmentByTag(
                            getFragmentTag(R.id.pager, GROUPS_ID));
                    frag.fragmentBecameVisible();

                } else if (CURRENT_FRAGMENT == CHAT_ID) {
                    if (mIncMsgService != null) {
                        mIncMsgService.requestAllGroupsList();
                    }

                    ChatFragment frag = (ChatFragment) getSupportFragmentManager().findFragmentByTag(
                            getFragmentTag(R.id.pager, CHAT_ID));

                    frag.fragmentBecameVisible();

                } else if (CURRENT_FRAGMENT == MAP_ID) {

                    MapFragment frag = (MapFragment) getSupportFragmentManager().findFragmentByTag(
                            getFragmentTag(R.id.pager, MAP_ID));

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
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
                                    saveNewToSharedPreferences(name);
                                }
                                dialogInterface.dismiss();
                            }
                        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void saveNewToSharedPreferences(String name) {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.username), name);
        editor.commit();

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

    ///////// LOCATION SPECIFIC

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

//        Log.i(TAG, "LAST LOCATION: " + mLastLocation.toString());

        if (mLastLocation != null) {

            if (!mGroups.isEmpty()) {
                for (Group group : mGroups.values()) {

                    if (group.getMyGroupId() != null) {
                        mIncMsgService.sendPosition(
                                group.getMyGroupId(),
                                String.valueOf(mLastLocation.getLongitude()),
                                String.valueOf(mLastLocation.getLatitude()));
                    }
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

        Group[] groups = getGroupsAsArray();
        if (groups.length != 0) {
            for (Group group : groups) {
                if (group.getMyGroupId() != null) {
                    mIncMsgService.sendPosition(
                            group.getMyGroupId(),
                            String.valueOf(mLastLocation.getLongitude()),
                            String.valueOf(mLastLocation.getLatitude()));
                }
            }
        }

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    ///////////////////////////////////// INTERFACES METHODS

    @Override
    public void notifyJoinedStatusChanged(String groupName, boolean isJoined) {
        Group group = mGroups.get(groupName);
        if (!isJoined) {
            mIncMsgService.requestUnregister(group.getMyGroupId());
        } else {
            mIncMsgService.requestRegister(groupName, getUsername());
        }
    }

    @Override
    public void notifyMapVisibilityChanged(String groupName, boolean isOnMap) {
        Log.d(TAG, "IS ON MAP " + isOnMap + " " + groupName);
        Group group = mGroups.get(groupName);
        group.setOnMap(isOnMap);
        mGroups.put(group.getGroupName(), group);
    }

    @Override
    public void startNewGroup(String groupName) {
        mIncMsgService.requestRegister(groupName, getUsername());
    }

    @Override
    public Group[] requestUpdateGroups() {
        Log.d(TAG, "requestUpdateGroups()" + mGroups.values().toArray( new Group[mGroups.size()]).length);
        debugPrintMap();
        Log.d(TAG, "ARE GROUPS NULL? " + mGroups.size());
        Group[] groups = mGroups.values().toArray( new Group[mGroups.size()]);
        return groups;
    }

    @Override
    public LatLng requestLocationUpdate() {
        if (mLastLocation != null) {
            LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            Log.d(TAG, loc.toString());
            return loc;
        }
        return null;
    }

    @Override
    public Group[] requestGroupsUpdate() {
        return getGroupsAsArray();
    }

    @Override
    public void notifyGroupChosen(String groupName) {

    }

    @Override
    public ArrayList requestJoinedGroups() {
        Log.d(TAG, "requestJoinedGroups()");

        ArrayList<Group> joined = new ArrayList<>();

        for (Group group : getGroupsAsArray()) {
            if (group.isJoined()) {
                joined.add(group);
            }
        }

        debugPrintMap();

        return joined;
    }

    public String getFragmentTag(int viewPagerId, int fragmentPosition) {
        // This is the format in which FragmentStatePagerAdapter internally set's  the fragment tag.
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }

    @Override
    public void onSendTextMessage(String myId, String messageText) {
        mIncMsgService.sendTextMessage(myId, messageText);
    }

    @Override
    public void onSendImageMessage(String myId, String messageText) {
        ImageMessage imgMsg = new ImageMessage();
        imgMsg.setFrom(myId);
        imgMsg.setText(messageText);
        imgMsg.setLatitude(String.valueOf(mLastLocation.getLatitude()));
        imgMsg.setLongitude(String.valueOf(mLastLocation.getLongitude()));
        imgMsg.setImage(mImageToDispatch);

        mIncMsgService.sendPictureMessage(imgMsg);
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
                    150, 150);
        }
    }

    @Override
    public boolean imgIsReady() {
        return mImageToDispatch != null;
    }

    @Override
    public ArrayList<Parcelable> requestReadMessages(String groupName) {
        if (mMessages != null && mMessages.get(groupName) != null) {
            Log.d(TAG, "SIZE IN MAIN" + mMessages.get(groupName).size());
        }
        return mMessages.get(groupName);
    }

    /////////////////////////////// ADAPTER SPECIFIC

    @Override
    public void onBackPressed() {

        if (mViewPager.getCurrentItem() == CHAT_ID) {
            ChatFragment frag = (ChatFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTag(R.id.pager, CHAT_ID));
            if (frag.bubblesVisible()) {
                frag.goBackToChatList();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void imageMessageReady(ImageMessage msg) {
        receiveMessage(msg.getGroup(), msg);
    }

    private class SwipeAdapter extends FragmentPagerAdapter {

        public SwipeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case GROUPS_ID:
                    return new GroupsFragment();

                case CHAT_ID:
                    return ChatFragment.newInstance(mUsername);

                case MAP_ID:
                    return new MapFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void debugPrintMap() {
        Iterator it = mGroups.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            Log.d(TAG, "+++++++++++++++++++++++++++++ DEBUG PRINT MAP ++++++++++++++++++++++++++++++");
            Log.d(TAG, "HASH MAP KEY: " + pair.getKey());
            Group group = (Group) pair.getValue();
            Log.d(TAG, "---GROUP NAME: " + group.getGroupName());
            Log.d(TAG, "---GROUP JOINED: " + group.isJoined());
            Log.d(TAG, "---GROUP ON MAP: " + group.isOnMap());
            Log.d(TAG, "---GROUP MY ID: " + group.getMyGroupId());
            Log.d(TAG, "---GROUP MEMBER COUNT: " + group.getMembers().size());
        }
    }

    // SERVICE SPECIFIC //////////////////////////////////////////////////////////////////////

    private void createBroadcastReceiver() {
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch(action) {
                    case Config.REPLY_REGISTER:

                        String groupName = intent.getStringExtra(Config.GROUP);
                        String groupId = intent.getStringExtra(Config.ID);

                        Group group = mGroups.get(groupName);
                        if (group == null) {
                            group = new Group(groupName);
                            group.setJoined(true);
                            group.setMyGroupId(groupId);
                            mGroups.put(groupName, group);
                        } else {
                            group.setJoined(true);
                            group.setMyGroupId(groupId);
                            mGroups.put(groupName, group);
                        }

                        Log.i(TAG, "WOOHOOOOOO IT WORKS " + group + groupId);

                        break;
                    case Config.REPLY_UNREGISTER:

                        String idToDelete = intent.getStringExtra(Config.ID);

                        for (Group groupToRemove : mGroups.values()) {
                            if (idToDelete.equals(groupToRemove.getMyGroupId())) {
                                groupToRemove.setMyGroupId(null);
                                groupToRemove.setJoined(false);
                                groupToRemove.setOnMap(false);
                                mGroups.put(groupToRemove.getGroupName(), groupToRemove);
                            }
                        }

                        Log.i(TAG, "UNREGISTERED FROM GROUP WITH ID " + idToDelete);

                        break;
                    case Config.REPLY_MEMBERS:

                        break;
                    case Config.REPLY_GROUPS:

                        Log.i(TAG, "GROUPS UPDATE RECEIVED");

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
                        break;

                    case Config.UPDATE_LOCATIONS:
                        Log.d(TAG, "RECEIVED LOCATIONS  ");

                        String locGroupName = intent.getStringExtra(Config.GROUP);
                        ArrayList<MemberLocation> memberLocations = intent.getParcelableArrayListExtra(Config.GROUP_LOCATIONS);

                        if (mGroups.get(locGroupName) == null) {
                            Group newGroup = new Group(locGroupName);
                            mGroups.put(locGroupName, newGroup);
                        }

                        mGroups.get(locGroupName).getMembers().clear();
                        for (MemberLocation loc : memberLocations) {
                            Member newMember = new Member(
                                    loc.getMemberName(),
                                    loc.getLatitude(),
                                    loc.getLongitude()
                            );
                            // TODO: adds new member at each update
                            mGroups.get(locGroupName).getMembers().add(newMember);
                        }

                        Log.d(TAG, "UPDATED LOCATIONS: " + mGroups.toString());

                        break;

                    case Config.UPDATE_EXCEPTION:

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

                debugPrintMap();
                updateChatListUI();
                updateGroupsUI();

            }
        };
    }

    private void startDownload(ImageMessage imgMsg, String id, String port) {
        new Downloader(this, imgMsg, id, port);
    }

    private void receiveMessage(String groupName, Parcelable msg) {

        ChatFragment frag = (ChatFragment) getSupportFragmentManager().findFragmentByTag(
                getFragmentTag(R.id.pager, CHAT_ID));

        if (mMessages.get(groupName) == null) {
            mMessages.put(groupName, new ArrayList<Parcelable>());
        }

        mMessages.get(groupName).add(msg);

        if (mViewPager.getCurrentItem() == CHAT_ID &&
                groupName.equals(frag.getCurrentGroup())) {

            frag.updateBubblesAdapter();
        }
    }


    private void updateChatListUI() {
        if (mViewPager.getCurrentItem() == CHAT_ID) {
            Log.d(TAG, "updateChatListUI");

            ChatFragment frag = (ChatFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTag(R.id.pager, CHAT_ID));
            if (frag.bubblesVisible()) {
                return;
            }
            frag.updateGroupsAdapter();
        }
    }

    private void updateGroupsUI() {
        if (mViewPager.getCurrentItem() == GROUPS_ID) {

            GroupsFragment frag = (GroupsFragment) getSupportFragmentManager().findFragmentByTag(
                    getFragmentTag(R.id.pager, GROUPS_ID));
            if (frag != null) {
                frag.updateGroupsList(getGroupsAsArray());
            }
        }
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
            ServerService.LocalService ls = (ServerService.LocalService) binder;
            mIncMsgService = ls.getService();

            mIncMsgService.requestAllGroupsList();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    private Group[] getGroupsAsArray() {
        return mGroups.values().toArray( new Group[mGroups.size()]);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcastListeners();
        unbindService(mServiceConnection);
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        Log.i(TAG, "GoogleApiClient in disconnected");
        super.onStop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: stop service here, starting onCreate
        Intent intent = new Intent(MainActivity.this, ServerService.class);
        stopService(intent);
    }
}
