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
import android.widget.Toast;
import java.util.ArrayList;
import aliona.mah.se.friendlocator.R;
import aliona.mah.se.friendlocator.interfaces.ChatListCallback;
import aliona.mah.se.friendlocator.beans.Group;
import aliona.mah.se.friendlocator.beans.ImageMessage;
import aliona.mah.se.friendlocator.beans.TextMessage;

/**
 * Fragment that shows the chat for specific groups.
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = ChatFragment.class.getName();

    private static final String USERNAME = "username";
    private static final String GROUP = "group";
    private static final String IS_IMAGE_MESSAGE = "is_img_message";
    private static final String MESSAGE_TEXT = "message_text";

    private Group mGroup;
    private String mMyName;
    private BubblesAdapter mBubblesAdapter;
    private ChatListCallback mParent;

    private ArrayList<Parcelable> mReadMessages = new ArrayList<>();

    private ImageButton mButtonSend;
    private ImageButton mButtonAttachPic;
    private EditText mEnterTextField;
    private boolean isImgMessage = false;

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
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_IMAGE_MESSAGE, isImgMessage);
        outState.putParcelable(GROUP, mGroup);
        if (mEnterTextField != null) {
            outState.putString(MESSAGE_TEXT, mEnterTextField.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMyName = args.getString(USERNAME);
            mGroup = args.getParcelable(GROUP);
        }
        if (savedInstanceState != null) {
            isImgMessage = savedInstanceState.getBoolean(IS_IMAGE_MESSAGE);
            mGroup = savedInstanceState.getParcelable(GROUP);
            if (mEnterTextField != null) {
                mEnterTextField.setText(savedInstanceState.getString(MESSAGE_TEXT));
            }
        }

        getActivity().setTitle(getResources().getString(R.string.tab_chat) + ": " + mGroup.getGroupName());

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
        mEnterTextField = view.findViewById(R.id.et_enter_chat_text);

        mButtonAttachPic = view.findViewById(R.id.button_attach_pic);
        mButtonAttachPic.setOnClickListener(this);

        mButtonSend = view.findViewById(R.id.button_send_message);
        mButtonSend.setOnClickListener(this);

        ListView chatBubblesList = view.findViewById(R.id.list_view_chat_bubbles);
        mBubblesAdapter = new BubblesAdapter(getContext(), R.id.list_view_chat_bubbles, mReadMessages);
        chatBubblesList.setAdapter(mBubblesAdapter);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "ON RESUME" );
        super.onResume();

        updateBubblesAdapter();
    }

    /**
     * Called by the fragment itself and by MainActivity to update messages/deliver new ones.
     */
    public void updateBubblesAdapter() {
        ArrayList<Parcelable> updatedMessages = mParent.requestReadMessages(mGroup.getGroupName());

        if (updatedMessages != null) {
            mBubblesAdapter.clear();
            mReadMessages.clear();
            mReadMessages.addAll(updatedMessages);
            mBubblesAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Caleld by main activity to check if the message it received is for the groups chat being displayed.
     * @return
     */
    public String getCurrentGroup() {
        return mGroup.getGroupName();
    }

    /**
     * Called by MainActivity to let the user know that the photo just taken has been successfully processed and
     * is ready to be sent.
     */
    public void notifyPhotoIsReady() {
        Toast.makeText(getContext(), getResources().getString(R.string.photo_ready) + " "
                        + new String(Character.toChars(0x1F44C)), Toast.LENGTH_SHORT).show();
    }

    /**
     * Adapter for the chat itself.
     */
    private class BubblesAdapter extends ArrayAdapter<Parcelable> {

        private BubblesAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Parcelable> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            Parcelable message = getItem(position);
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());

            if (message instanceof TextMessage) {

                convertView = vi.inflate(R.layout.list_view_item_chat_bubbles, parent, false);

                TextView sender = convertView.findViewById(R.id.tv_chat_sender);
                TextView text = convertView.findViewById(R.id.tv_chat_text);

                sender.setText( ((TextMessage) message).getFrom());
                text.setText(((TextMessage) message).getText());

                if (!((TextMessage) message).getFrom().equals(mMyName)) {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.chat_rounded_corner_blue));
                } else {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.chat_rounded_corner_orange));
                }

            } else if (message instanceof ImageMessage) {

                convertView = vi.inflate(R.layout.list_view_item_chat_image_bubble, parent, false);

                TextView sender = convertView.findViewById(R.id.tv_chat_sender_image);
                TextView text = convertView.findViewById(R.id.tv_chat_text_image);
                ImageView image = convertView.findViewById(R.id.chat_image_view);

                sender.setText( ((ImageMessage) message).getFrom());
                text.setText(((ImageMessage) message).getText());

                if (!((ImageMessage) message).getFrom().equals(mMyName)) {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.chat_rounded_corner_blue));
                } else {
                    convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.chat_rounded_corner_orange));
                }

                image.setImageBitmap(((ImageMessage) message).getImage());

            }

            return convertView;
        }

    }

    @Override
    public void onClick(View view) {

        if (view == mButtonAttachPic) {

            mParent.startImgUpload();
            isImgMessage = true;

        } else if (view == mButtonSend) {

            String text = mEnterTextField.getText().toString();
            if (text.length() == 0) {
                Snackbar.make(this.getView(), R.string.empty_message, Snackbar.LENGTH_SHORT);
                return;
            }

            if (isImgMessage) {
                if (mParent.imgIsReady()) {
                    mParent.onSendImageMessage(mGroup.getMyGroupId(), text);
                    isImgMessage = false;
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
}
