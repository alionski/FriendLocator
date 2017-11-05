package aliona.mah.se.friendlocator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Util class with static method for manipulations of photos taken with camera.
 * Created by aliona on 2017-11-02.
 */

public class BitmapHelper {

    /**
     * Used before saving the bitmap for dispatching to the server. Is resied to max 300 on both saides,
     * will be further compressed in Uploader.
     * @param pathToPicture
     * @return
     */
    public static Bitmap getScaled(String pathToPicture) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true; // No memory allocation, outWidth, outHeight, outMimeType
        BitmapFactory.decodeFile(pathToPicture, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / 300, photoH / 300);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFile(pathToPicture, bmOptions);
    }

    /**
     * Generates a URI for storing on the device
     * @return uri for the photo from chat
     */
    public static Uri generateURI() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "FriendLocator_" + timeStamp + ".jpg";
        File dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return Uri.fromFile(new File(dir, imageFileName));
    }

    /**
     * Used when temporarily saving a Bitmapp in saved-instance bundle
     * @param toConvert -- bitmap to convert
     * @return -- byte array
     */
    public static byte[] fromBitmapToBytes(Bitmap toConvert) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        toConvert.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Used when converting from the saved instance back into Bitmap
     * @param toConvert -- byte array to convert to Bitmap
     * @return -- ready bitmap
     */
    public static Bitmap fromBytesToBitmap(byte[] toConvert) {
        return BitmapFactory.decodeByteArray(toConvert, 0, toConvert.length);
    }
}
