package aliona.mah.se.friendlocator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.haha.perflib.Main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import aliona.mah.se.friendlocator.MainActivity;
import beans.ImageMessage;

/**
 * Created by aliona on 2017-11-02.
 */

public class Downloader extends AsyncTask<String, Void, Bitmap> {
    public static String TAG = Downloader.class.getName();
    private DownloadListener listener;
    private ImageMessage msg;

    public Downloader(DownloadListener listener, ImageMessage msg, String imageId, String port) {
        Log.d(TAG, "DOWNLOADER STARTED");
        this.listener = listener;
        this.msg = msg;
        execute(port, imageId);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Log.d(TAG, "TRYING TO DOWNLOAD");
        Bitmap result = null;
        ObjectInputStream input = null;
        ObjectOutputStream output = null;
        byte[] downloadArray;
        Socket socket = null;
        try {
            socket = new Socket(MainActivity.IP, Integer.valueOf(params[0]));
            input = new ObjectInputStream(socket.getInputStream());
            output= new ObjectOutputStream(socket.getOutputStream());

            output.writeUTF(params[1]);
            output.flush();
            downloadArray = (byte[])input.readObject();
            result = BitmapFactory.decodeByteArray(downloadArray, 0, downloadArray.length);

        return result;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
        Log.d("DOWBLOADER", "MESSAGE RECEIVED " + msg.getImage().getByteCount());
        listener.imageMessageReady(msg);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

    public interface DownloadListener {
        void imageMessageReady(ImageMessage msg);
    }

}
