package aliona.mah.se.friendlocator.beans;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Bean representing an image message.
 * Created by aliona on 2017-11-01.
 */

public class ImageMessage implements Parcelable {
    private String group;
    private String from;
    private String text;
    private String latitude;
    private String longitude;
    private Bitmap image;

    public ImageMessage() {}

    protected ImageMessage(Parcel in) {
        group = in.readString();
        from = in.readString();
        text = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(group);
        dest.writeString(from);
        dest.writeString(text);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeParcelable(image, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageMessage> CREATOR = new Creator<ImageMessage>() {
        @Override
        public ImageMessage createFromParcel(Parcel in) {
            return new ImageMessage(in);
        }

        @Override
        public ImageMessage[] newArray(int size) {
            return new ImageMessage[size];
        }
    };

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

}
