package aliona.mah.se.friendlocator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aliona on 2017-11-02.
 */

public class BitmapResiser {

    public static Bitmap getScaled(String pathToPicture, int targetW, int targetH) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true; // No memory allocation, outWidth, outHeight, outMimeType
        BitmapFactory.decodeFile(pathToPicture, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(pathToPicture, bmOptions);
        return bitmap;
    }

    public static Uri generateURI() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "FriendLocator_" + timeStamp + ".jpg";
        File dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return Uri.fromFile(new File(dir, imageFileName));
    }

    public static byte[] fromBitmapToBytes(Bitmap toConvert) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        toConvert.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap fromBytesToBitmap(byte[] toConvert) {
        return BitmapFactory.decodeByteArray(toConvert, 0, toConvert.length);
    }
}
