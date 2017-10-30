package aliona.mah.se.friendlocator;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.design.widget.TabLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.squareup.leakcanary.LeakCanary;

import aliona.mah.se.friendlocator.util.IncomingMessageService;
import aliona.mah.se.friendlocator.util.PictureSenderService;
import layout.ChatFragment;
import layout.GroupsFragment;
import layout.MapFragment;

public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private SwipeAdapter mAdapter;

    private IncomingServiceConnection mServiceConnection;
    private IncomingMessageService mIncMsgService;
    public final static String IP = "195.178.227.53";
    public final static int PORT = 7117;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseUI();

        Intent intent = new Intent(this, IncomingMessageService.class);
        intent.putExtra(IncomingMessageService.IP, IP);
        intent.putExtra(IncomingMessageService.PORT, PORT);
        startService(intent);
        registerBroadcastListeners();

    }

    @Override
    public void onResume() {
        super.onResume();
        mServiceConnection = new IncomingServiceConnection();
        Intent intent = new Intent(this, IncomingMessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initialiseUI() {
        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        //
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mAdapter = new SwipeAdapter(getSupportFragmentManager());

        // Set up action bar.
        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);

//        LinearLayout tabLinearLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.tab_text, null);
//        TextView tabContent = tabLinearLayout.findViewById(R.id.tab_content);
//
//        tabContent.setText("Groups"); //tab label txt
//        tabContent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_group_white_24dp, 0, 0, 0);
//        tabLayout.addTab(tabLayout.newTab().setCustomView(tabContent));
//
//        TextView tabContent2 = tabLinearLayout.findViewById(R.id.tab_content2);
//        tabContent2.setText("Chat"); //tab label txt
//        tabContent2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chat_white_24dp, 0, 0, 0);
//        tabLayout.addTab(tabLayout.newTab().setCustomView(tabContent2));
//
//        TextView tabContent3 = tabLinearLayout.findViewById(R.id.tab_content3);
//        tabContent3.setText("Map"); //tab label txt
//        tabContent3.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_map_white_24dp, 0, 0, 0);
//        tabLayout.addTab(tabLayout.newTab().setCustomView(tabContent3));

        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_groups)));
//        .setIcon(R.drawable.ic_group_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_chat)));
//        setIcon(R.drawable.ic_chat_white_24dp));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_map)));
//        setIcon(R.drawable.ic_map_white_24dp));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        // TODO: remove listener in onPause
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
//                int tabColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
//                tab.getIcon().setColorFilter(tabColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//                tab.getIcon().clearColorFilter();
            }

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
                // TODO: start settings fragment
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_tollbar, menu);
        return true;
    }

    private class SwipeAdapter extends FragmentPagerAdapter {
        private final static String MAP_TAG = "maps_fragment";
        private final static String GROUPS_TAG = "groups_fragment";
        private final static String CHAT_TAG = "chat_fragment";
        private MapFragment mapFragment;
        private GroupsFragment groupsFragment;
        private ChatFragment chatFragment;
        private FragmentManager fm;

        public SwipeAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    groupsFragment = (GroupsFragment) fm.findFragmentByTag(GROUPS_TAG);
                    if (groupsFragment == null) {
                        groupsFragment = new GroupsFragment();
                    }
                    fragment = groupsFragment;
                    break;
                case 1:
                    chatFragment = (ChatFragment) fm.findFragmentByTag(CHAT_TAG);
                    if (chatFragment == null) {
                        chatFragment = new ChatFragment();
                    }
                    fragment = chatFragment;
                    break;
                case 2:
                    mapFragment = (MapFragment) fm.findFragmentByTag(MAP_TAG);
                    if (mapFragment == null) {
                        mapFragment = new MapFragment();
                    }
                    fragment = mapFragment;
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    // SERVICE SPECIFIC //////////////////////////////////////////////////////////////////////

    private void registerBroadcastListeners() {
        LocalBroadcastManager listener = LocalBroadcastManager.getInstance(this);
        listener.registerReceiver(
                mMessageReceiver, new IntentFilter("incoming_message"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Double currentSpeed = intent.getDoubleExtra("currentSpeed", 20);
            Double currentLatitude = intent.getDoubleExtra("latitude", 0);
            Double currentLongitude = intent.getDoubleExtra("longitude", 0);
            //  ... react to local broadcast message
        }
    };


    private void checkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // Context
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo!=null) {
            if(networkInfo.isAvailable()) {
                // enhet uppkopplad mot nätverk
                if(networkInfo.isConnected()) {
                    // nätverk tillgängligt på enhet
                }
            }

        }
    }

    private class IncomingServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            IncomingMessageService.LocalService ls = (IncomingMessageService.LocalService) binder;
            mIncMsgService = ls.getService(); // serviceD tilldelas referens till Service-instansen
            // TODO: testy test
            mIncMsgService.requestAllGroupsList();
            mIncMsgService.requestRegister("Alionas Group", "Aliona");
            mIncMsgService.requestAllGroupsList();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    public void startPictureUpload() {
        Intent intent = new Intent(this, PictureSenderService.class);
        startService(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
//        Då activityn hamnar i bakgrunden ska man anropa metoden
//        unbindService( ServiceConnection ). Detta sker med fördel i onPause().
        unbindService(mServiceConnection);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO: stop service here, starting onCreate
        Intent intent = new Intent(MainActivity.this, IncomingMessageService.class);
        stopService(intent);
    }
}
