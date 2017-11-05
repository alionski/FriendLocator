package aliona.mah.se.friendlocator.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import aliona.mah.se.friendlocator.beans.Group;
import aliona.mah.se.friendlocator.beans.Member;

/**
 * Callback interface that MainActivity must implement to enable GroupsFragment to talk to it.
 * Created by aliona on 2017-10-30.
 */

public interface GroupsFragmentCallback {
    void notifyGroupJoinStatusChanged(String groupName, boolean isJoined);
    void showChat(String groupName);
    ArrayList<Group> requestUpdateGroups();
    void showMap(String groupName);
    HashMap<String, ArrayList<Member>> getMembers();
}
