package aliona.mah.se.friendlocator.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by aliona on 2017-11-02.
 */

public class Uploader extends AsyncTask<String, Void, Void> {
    private Bitmap toSend;


    public Uploader(String imageId, String port, Bitmap toSend) {
        this.toSend = toSend;
        execute(port, imageId);
    }

    @Override
    protected Void doInBackground(String... strings) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        toSend.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        Socket socket;
        try {
            socket = new Socket(ServerService.IP, Integer.valueOf(strings[0]));
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            output.writeUTF(strings[1]);
            output.flush();
            output.writeObject(byteArray);
            Log.d("UPLOADER ARRAY SIZE", "" + byteArray.length);
            output.flush();
            socket.close();
            Log.d("UPLOADER", "DONE UPLOADING");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
