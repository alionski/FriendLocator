package aliona.mah.se.friendlocator.interfaces;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import aliona.mah.se.friendlocator.beans.Member;

/**
 * Created by aliona on 2017-10-31.
 */

public interface MapFragmentCallback {
    LatLng requestLocationUpdate();
    ArrayList<Member> requestMembersUpdate(String groupName);
}
