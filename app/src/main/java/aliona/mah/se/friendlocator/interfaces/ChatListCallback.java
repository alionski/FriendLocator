package aliona.mah.se.friendlocator.interfaces;

import android.os.Parcelable;

import java.util.ArrayList;

import aliona.mah.se.friendlocator.beans.Group;

/**
 * Created by aliona on 2017-10-31.
 */

public interface ChatListCallback {
    void onSendTextMessage(String myId, String messageText);
    void onSendImageMessage(String myId, String messageText);
    void startImgUpload();
    boolean imgIsReady();
    ArrayList<Parcelable> requestReadMessages(String groupName);
}
