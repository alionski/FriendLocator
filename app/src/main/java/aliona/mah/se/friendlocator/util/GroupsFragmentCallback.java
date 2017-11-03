package aliona.mah.se.friendlocator.util;

import beans.Group;

/**
 * Created by aliona on 2017-10-30.
 */

public interface GroupsFragmentCallback {
    void notifyJoinedStatusChanged(String groupName, boolean isJoined);
    void notifyMapVisibilityChanged(String groupName, boolean isOnMap);
    void startNewGroup(String groupName);
    Group[] requestUpdateGroups();
}
