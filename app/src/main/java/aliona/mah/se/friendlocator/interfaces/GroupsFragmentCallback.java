package aliona.mah.se.friendlocator.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import aliona.mah.se.friendlocator.beans.Group;
import aliona.mah.se.friendlocator.beans.Member;

/**
 * Created by aliona on 2017-10-30.
 */

public interface GroupsFragmentCallback {
    void notifyJoinedStatusChanged(String groupName, boolean isJoined);
    void startNewGroup(String groupName);
    void showChat(String groupName);
    ArrayList<Group> requestUpdateGroups();
    void showMap(String groupName);
    HashMap<String, ArrayList<Member>> getMembers();
}
