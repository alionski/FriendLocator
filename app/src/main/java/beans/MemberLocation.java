package beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aliona on 2017-10-30.
 */

public class MemberLocation implements Parcelable {
    private String member;
    private String longitude;
    private String latitude;

    public MemberLocation(String member, String longitude, String latitude) {
        this.member = member;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    protected MemberLocation(Parcel in) {
        member = in.readString();
        longitude = in.readString();
        latitude = in.readString();
    }

    public static final Creator<MemberLocation> CREATOR = new Creator<MemberLocation>() {
        @Override
        public MemberLocation createFromParcel(Parcel in) {
            return new MemberLocation(in);
        }

        @Override
        public MemberLocation[] newArray(int size) {
            return new MemberLocation[size];
        }
    };

    public String getMemberName() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(member);
        parcel.writeString(longitude);
        parcel.writeString(latitude);
    }
}
