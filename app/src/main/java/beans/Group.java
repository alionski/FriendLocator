package beans;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


/**
 * Created by aliona on 2017-10-23.
 */

public class Group implements Parcelable {
    private String groupName;
    private String myGroupId;
    private boolean joined = false;
    private boolean onMap = false;
    private ArrayList<Member> members;

    public Group() {}

    public Group(String groupName) {
        this.groupName = groupName;
        members = new ArrayList<>();
    }


    protected Group(Parcel in) {
        groupName = in.readString();
        myGroupId = in.readString();
        joined = in.readByte() != 0;
        onMap = in.readByte() != 0;
        members = in.createTypedArrayList(Member.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeString(myGroupId);
        dest.writeByte((byte) (joined ? 1 : 0));
        dest.writeByte((byte) (onMap ? 1 : 0));
        dest.writeTypedList(members);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    public boolean isOnMap() {
        return onMap;
    }

    public void setOnMap(boolean onMap) {
        this.onMap = onMap;
    }

    public String getMyGroupId() {
        return myGroupId;
    }

    public void setMyGroupId(String myGroupId) {
        this.myGroupId = myGroupId;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }
}
