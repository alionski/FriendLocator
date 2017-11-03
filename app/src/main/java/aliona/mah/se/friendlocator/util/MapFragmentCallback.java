package aliona.mah.se.friendlocator.util;

import com.google.android.gms.maps.model.LatLng;

import beans.Group;

/**
 * Created by aliona on 2017-10-31.
 */

public interface MapFragmentCallback {
    LatLng requestLocationUpdate();
    Group[] requestGroupsUpdate();
}
