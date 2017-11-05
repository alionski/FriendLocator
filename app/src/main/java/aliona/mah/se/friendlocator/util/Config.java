package aliona.mah.se.friendlocator.util;

/**
 * A class that is not even a real class but a list of static strings to be used when sending and receiving
 * broadcasts from the service to main activity. Having them here reduces the number of declarations in resepctive classed
 * and makes it all cleaner.
 * Created by aliona on 2017-10-30.
 */

public class Config {

    public static final String REPLY_REGISTER = "register";
    public static final String REPLY_UNREGISTER = "unregister";
    public static final String REPLY_MEMBERS = "members";
    public static final String REPLY_GROUPS = "groups";
    public static final String REPLY_LOCATION = "location";
    public static final String UPDATE_LOCATIONS = "locations";
    public static final String UPDATE_EXCEPTION = "exception";
    public static final String UPDATE_IMAGECHAT = "imagechat";
    public static final String UPDATE_TEXTCHAT = "textchat";
    public static final String IMG_OBJECT = "img_object";
    public static final String REPLY_UPLOAD = "upload";
    public static final String IMG_ID = "imageid";
    public static final String IMG_PORT = "image_port";
    public static final String GROUP = "group";
    public static final String GROUP_LOCATIONS = "locations";
    public static final String LONG = "longitude";
    public static final String LAT = "latitude";
    public static final String MEMBER = "member";
    public static final String TEXT = "text";
    public static final String TEXT_OBJ = "text_object";
    public static final String ID = "id";
    public static final String MEMBERS_ARRAY = "members";
    public static final String GROUPS_ARRAY = "groups";
    public static final String EXCEPTION_MSG = "exception";
}
