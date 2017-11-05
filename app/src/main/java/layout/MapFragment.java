package layout;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;

import aliona.mah.se.friendlocator.MainActivity;
import aliona.mah.se.friendlocator.R;
import aliona.mah.se.friendlocator.beans.Member;
import aliona.mah.se.friendlocator.interfaces.MapFragmentCallback;
import aliona.mah.se.friendlocator.beans.Group;

/**
 * MapFragment which shows members' positions on the map.
 * Can show only one group at once and is constantly updated with arriving location updates from the server.
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private final static String TAG = MapFragment.class.getName();
    private static final String GROUP = "map_group";
    private final int MY_PERMISSIONS_REQUEST_GPS = 5555;
    private boolean mPermissionGranted = false;
    private MapView mMapView;
    private GoogleMap mMap;
    private Group mGroup;
    private MapFragmentCallback mParent;


    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(Group group) {
        MapFragment frag = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelable(GROUP, group);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(GROUP, mGroup);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mGroup = args.getParcelable(GROUP);
        }
        // if args are null, it means we are restoring after rotation and our group is there
        if (savedInstanceState != null) {
            mGroup = savedInstanceState.getParcelable(GROUP);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "ON CREATE VIEW");

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "ON ATTACH");
        super.onAttach(context);
        if (context instanceof MapFragmentCallback) {
            mParent = (MapFragmentCallback) context;
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "ON RESUME");
        super.onResume();

        MainActivity.CURRENT_FRAGMENT = MainActivity.MAP_ID;
        getActivity().setTitle(R.string.tab_map);

        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);
    }

    /**
     * Method called by MainActivity to update markers.
     */
    public void updateLocations() {
        if (mMap != null) {
            addMarkers();
        }
    }

    /**
     * Manipulates the map once available.
     * This mParent is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "MAP IS READY");
        mMap = googleMap;
        addSelf();
        addMarkers();

    }

    /**
     * Positions the user on the map.
     */
    private void addSelf() {
        if (ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_GPS);
        }
        if (mPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    /**
     * Cleans the map and adds users to the map
     */
    private void addMarkers() {

        mMap.clear();

        LatLng myPosition = mParent.requestLocationUpdate();
        ArrayList<Member> members = mParent.requestMembersUpdate(mGroup.getGroupName());

        if (members == null || members.size() == 0) {
            Log.d(TAG, "LOCATIONS ARE NULL OR EMPTY");
            return;
        }

        if (myPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f)); // should be between min = 2.0 and max = 21.0

        for (Member member : members) {
            double longitude, latitude;

            if (member.getLatitude() == null || member.getLongitude() == null) {
                continue;
            }

            try {
                longitude = Double.parseDouble(member.getLongitude());
                latitude = Double.parseDouble(member.getLatitude());
            } catch (NumberFormatException locationNonAvailable) {
                Log.d(TAG, locationNonAvailable.toString());
                continue;
            }

            LatLng memberPosition = new LatLng(latitude, longitude);

            mMap.addMarker( new MarkerOptions()
                    .position(memberPosition)
                    .title(member.getMemberName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_circle_black_48dp))
                    .snippet(getResources().getString(R.string.group_label) + " " + mGroup.getGroupName()
            ));
        }
    }

    /**
     * Method that is called after the user has decided whether to grant permission to access device location or not.
     * @param requestCode -- the final int identifying the request.
     * @param permissions -- the type of permission that user was asked for
     * @param grantResults -- the results, i.e. user's decision
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GPS: {
                // If request is cancelled, the result arrays are empty.
                mPermissionGranted =  (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            }
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "ON PAUSE");
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "ON DETACH");
        super.onDetach();
        mParent = null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ON DESTROY");
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
