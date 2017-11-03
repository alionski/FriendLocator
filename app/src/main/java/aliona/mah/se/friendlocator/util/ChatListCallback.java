package aliona.mah.se.friendlocator.util;

import android.os.Parcelable;

import java.util.ArrayList;

import beans.Group;

/**
 * Created by aliona on 2017-10-31.
 */

public interface ChatListCallback {
    void notifyGroupChosen(String groupName);
    void onSendTextMessage(String myId, String messageText);
    void onSendImageMessage(String myId, String messageText);
    void startImgUpload();
    boolean imgIsReady();
    ArrayList<Group> requestJoinedGroups();
    ArrayList<Parcelable> requestReadMessages(String groupName);
}
