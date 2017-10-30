package aliona.mah.se.friendlocator;

/**
 * Created by aliona on 2017-10-23.
 */

public class Group {
    private String groupName;
    private boolean joined = false;
    private boolean onMap = false;

    public Group() {
    }

    public Group(String groupName) {
        this.groupName = groupName;
    }

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
}
