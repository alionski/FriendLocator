package aliona.mah.se.friendlocator.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable bean representing a text message.
 * Created by aliona on 2017-10-31.
 */

public class TextMessage implements Parcelable {
    private String group;
    private String from;
    private String text;

    public TextMessage(String group, String from, String text) {
        this.group = group;
        this.from = from;
        this.text = text;
    }

    protected TextMessage(Parcel in) {
        group = in.readString();
        from = in.readString();
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(group);
        dest.writeString(from);
        dest.writeString(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TextMessage> CREATOR = new Creator<TextMessage>() {
        @Override
        public TextMessage createFromParcel(Parcel in) {
            return new TextMessage(in);
        }

        @Override
        public TextMessage[] newArray(int size) {
            return new TextMessage[size];
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
}
