package aliona.mah.se.friendlocator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import aliona.mah.se.friendlocator.beans.ImageMessage;

/**
 * AsyncTask that downloads an image that has arrived from the server and returns it to MainActivity, to be
 * stored in messages and passed to ChatFragment, if it's active
 * Created by aliona on 2017-11-02.
 */

public class Downloader extends AsyncTask<String, Void, Bitmap> {
    private static String TAG = Downloader.class.getName();
    private DownloadListener listener;
    private ImageMessage msg;

    public Downloader(DownloadListener listener, ImageMessage msg, String imageId, String port) {
        this.listener = listener;
        this.msg = msg;
        execute(port, imageId);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap result = null;
        ObjectInputStream input = null;
        ObjectOutputStream output = null;
        byte[] downloadArray;
        Socket socket = null;
        try {
            socket = new Socket(ServerService.IP, Integer.valueOf(params[0]));
            input = new ObjectInputStream(socket.getInputStream());
            output= new ObjectOutputStream(socket.getOutputStream());

            output.writeUTF(params[1]);
            output.flush();
            downloadArray = (byte[])input.readObject();
            result = BitmapFactory.decodeByteArray(downloadArray, 0, downloadArray.length);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        msg.setImage(result);
        Log.d(TAG, "MESSAGE RECEIVED " + msg.getImage().getByteCount());
        listener.imageDownloaded(msg);
    }

    public interface DownloadListener {
        void imageDownloaded(ImageMessage msg);
    }

}
