package aliona.mah.se.friendlocator.interfaces;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import aliona.mah.se.friendlocator.beans.Member;

/**
 * Callback interface that MainActivity has to implement to allow MapFragment to talk to it.
 * Created by aliona on 2017-10-31.
 */

public interface MapFragmentCallback {
    LatLng requestLocationUpdate();
    ArrayList<Member> requestMembersUpdate(String groupName);
}
