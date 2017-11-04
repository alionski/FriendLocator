package layout;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import aliona.mah.se.friendlocator.R;
import aliona.mah.se.friendlocator.interfaces.ChatListCallback;
import aliona.mah.se.friendlocator.beans.Group;
import aliona.mah.se.friendlocator.beans.ImageMessage;
import aliona.mah.se.friendlocator.beans.TextMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = ChatFragment.class.getName();

    private static final String USERNAME = "username";
    private static final String GROUP = "group";

    private Group mGroup;
    private String mMyName;
    private BubblesAdapter mBubblesAdapter;

    private ListView mChatBubblesList;
    private ChatListCallback mParent;

    private ArrayList<Parcelable> mReadMessages = new ArrayList<>();

    private ImageButton mButtonSend;
    private ImageButton mButtonAttachPic;
    private EditText mEnterTextField;

    private boolean imgMessage = false;

    public ChatFragment() {  }

    public static ChatFragment newInstance(String username, Group group) {
        ChatFragment frag = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        args.putParcelable(GROUP, group);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMyName = args.getString(USERNAME);
            mGroup = args.getParcelable(GROUP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "ON CREATE VIEW");
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initialiseUI(view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "ON ATTACH" );
        super.onAttach(context);
        if (context instanceof ChatListCallback) {
            mParent = (ChatListCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ChatListCallback");
        }
    }

    private void initialiseUI(View view) {
        mChatBubblesList = view.findViewById(R.id.list_view_chat_bubbles);

        mEnterTextField = view.findViewById(R.id.et_enter_chat_text);

        mButtonAttachPic = view.findViewById(R.id.button_attach_pic);
        mButtonAttachPic.setOnClickListener(this);

        mButtonSend = view.findViewById(R.id.button_send_message);
        mButtonSend.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "ON RESUME" );
        super.onResume();
        updateBubblesAdapter();
    }

    public String getCurrentGroup() {
        return mGroup.getGroupName();
    }

    public void updateBubblesAdapter() {
        ArrayList<Parcelable> updatedMessages = mParent.requestReadMessages(mGroup.getGroupName());

        if(updatedMessages != null && updatedMessages.size() != 0) {
            mChatBubblesList.setAdapter(null);
            mBubblesAdapter = new BubblesAdapter(getContext(), R.id.list_view_chat_bubbles, updatedMessages);
            mChatBubblesList.setAdapter(mBubblesAdapter);
        } else {
            mChatBubblesList.setAdapter(null);
        }
    }

    private class BubblesAdapter extends ArrayAdapter<Parcelable> {

        public BubblesAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Parcelable> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "GOT NEW MESSAGE! ");

            Parcelable message = getItem(position);
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());

            if (message instanceof TextMessage) {
                Log.d(TAG, "MESSAGE IS TEXT");

                convertView = vi.inflate(R.layout.list_view_item_chat_bubbles, null);

                TextView sender = convertView.findViewById(R.id.tv_chat_sender);
                TextView text = convertView.findViewById(R.id.tv_chat_text);

                sender.setText( ((TextMessage) message).getFrom());
                text.setText(((TextMessage) message).getText());

                if (!((TextMessage) message).getFrom().equals(mMyName)) {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_blue));
                } else {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_orange));
                }

            } else if (message instanceof ImageMessage) {
                Log.d(TAG, "MESSAGE IS IMAGE");

                convertView = vi.inflate(R.layout.list_view_item_chat_image_bubble, null);

                TextView sender = convertView.findViewById(R.id.tv_chat_sender_image);
                TextView text = convertView.findViewById(R.id.tv_chat_text_image);
                ImageView image = convertView.findViewById(R.id.chat_image_view);

                sender.setText( ((ImageMessage) message).getFrom());
                text.setText(((ImageMessage) message).getText());

                if (!((ImageMessage) message).getFrom().equals(mMyName)) {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_blue));
                } else {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_orange));
                }

                image.setImageBitmap(((ImageMessage) message).getImage());

            } else {
                return null;
            }
            return convertView;
        }

    }

    @Override
    public void onClick(View view) {

        if (view == mButtonAttachPic) {

            mParent.startImgUpload();
            imgMessage = true;

        } else if (view == mButtonSend) {

            String text = mEnterTextField.getText().toString();
            if (text.length() == 0) {
                Snackbar.make(this.getView(), R.string.empty_message, Snackbar.LENGTH_SHORT);
                return;
            }

            if (imgMessage) {
                if (mParent.imgIsReady()) {
                    mParent.onSendImageMessage(mGroup.getMyGroupId(), text);
                    imgMessage = false;
                }
            } else {
                mParent.onSendTextMessage(mGroup.getMyGroupId(), text);

            }

            mEnterTextField.setText("");
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "ON STOP");
        super.onStop();
    }


    @Override
    public void onPause() {
        Log.d(TAG, "ON PAUSE");
        super.onPause();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "ON DETACH");
        super.onDetach();
        mParent = null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ON DESTROY");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}
