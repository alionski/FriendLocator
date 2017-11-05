package aliona.mah.se.friendlocator.beans;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;


/**
 * Created by aliona on 2017-10-23.
 */

public class Group implements Parcelable, Serializable {
    private String groupName;
    private String myGroupId;

    public Group() {}

    public Group(String groupName) {
        this.groupName = groupName;
    }


    protected Group(Parcel in) {
        groupName = in.readString();
        myGroupId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeString(myGroupId);
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

    public String getMyGroupId() {
        return myGroupId;
    }

    public void setMyGroupId(String myGroupId) {
        this.myGroupId = myGroupId;
    }
}
