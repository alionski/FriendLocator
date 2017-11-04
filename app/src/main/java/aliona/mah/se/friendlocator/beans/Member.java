package aliona.mah.se.friendlocator.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aliona on 2017-10-30.
 */

public class Member implements Parcelable {
    private String memberName;
    private String longitude;
    private String latitude;

    public Member(String memberName) {
        this.memberName = memberName;
    }

    public Member(String name, String latitude, String longitude) {
        memberName = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Member(Parcel in) {
        memberName = in.readString();
        longitude = in.readString();
        latitude = in.readString();
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(memberName);
        parcel.writeString(longitude);
        parcel.writeString(latitude);
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
