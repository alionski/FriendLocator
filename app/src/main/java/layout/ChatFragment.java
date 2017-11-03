package layout;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import aliona.mah.se.friendlocator.R;
import aliona.mah.se.friendlocator.util.ChatListCallback;
import aliona.mah.se.friendlocator.util.OnFragmentVisibleListener;
import beans.Group;
import beans.ImageMessage;
import beans.TextMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements OnFragmentVisibleListener, View.OnClickListener {
    public static final String TAG = ChatFragment.class.getName();

    private static final String USERNAME = "username";

    private String mCurrentGroup;
    private String mCurrentId;
    private String mMyName;
    private ChatListAdapter mGroupsAdapter;
    private BubblesAdapter mBubblesAdapter;

    private ArrayList<Group> mJoinedGroups;

    private ListView mGroupsList;
    private ListView mChatBubblesList;
    private LinearLayout mChatBubbles;
    private ChatListCallback callback;

    private ArrayList<Parcelable> mReadMessages = new ArrayList<>();

    private ImageButton mButtonSend;
    private ImageButton mButtonAttachPic;
    private EditText mEnterTextField;

    private boolean imgMessage = false;

    public ChatFragment() {  }

    public static ChatFragment newInstance(String username) {
        ChatFragment frag = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMyName = args.getString(USERNAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "ON CREATE VIEW");

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initialiseUI(view);
        updateGroupsAdapter();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatListCallback) {
            callback = (ChatListCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void initialiseUI(View view) {

        mGroupsList = view.findViewById(R.id.list_available_joined_groups);
        mChatBubbles = view.findViewById(R.id.view_with_chat_bubbles);
        mChatBubblesList = view.findViewById(R.id.list_view_chat_bubbles);

        mGroupsList.setVisibility(View.VISIBLE);
        mChatBubbles.setVisibility(View.GONE);

        mEnterTextField = view.findViewById(R.id.et_enter_chat_text);

        mButtonAttachPic = view.findViewById(R.id.button_attach_pic);
        mButtonAttachPic.setOnClickListener(this);

        mButtonSend = view.findViewById(R.id.button_send_message);
        mButtonSend.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "GROUP NAMES ON RESUME CHAT" );
    }

    @Override
    public void fragmentBecameVisible() {
        Log.d(TAG, "FRAGMENT VISIBLE");
        updateGroupsAdapter();
    }

    @Override
    public void onClick(View view) {

        if (view == mButtonAttachPic) {

            callback.startImgUpload();
            imgMessage = true;

        } else if (view == mButtonSend) {
            String text = mEnterTextField.getText().toString();
            if (text.length() == 0) {
                // TODO: show snack bar
                return;
            }

            if (imgMessage) {
                if (callback.imgIsReady()) {
                    callback.onSendImageMessage(mCurrentId, text);
                    imgMessage = false;
                }
            } else {
                callback.onSendTextMessage(mCurrentId, text);

            }

            mEnterTextField.setText("");
        }
    }

    public void goBackToChatList() {
        mChatBubbles.setVisibility(View.GONE);
        mGroupsList.setVisibility(View.VISIBLE);

        mCurrentGroup = null;
        mCurrentId = null;

        updateGroupsAdapter();
    }

    private void showBubbles() {

        mGroupsList.setVisibility(View.GONE);
        mChatBubbles.setVisibility(View.VISIBLE);

        updateBubblesAdapter();
    }

    public boolean bubblesVisible() {
        return mChatBubbles.getVisibility() == View.VISIBLE;
    }

    public void updateGroupsAdapter() {
        mJoinedGroups = callback.requestJoinedGroups();
        mGroupsAdapter = new ChatListAdapter(getContext(), R.id.list_available_joined_groups, mJoinedGroups);
//        mGroupsList.setAdapter(null);
        mGroupsList.invalidateViews();
        mGroupsList.setAdapter(mGroupsAdapter);
    }

    public String getCurrentGroup() {
        return mCurrentGroup;
    }

    public void updateBubblesAdapter() {
        mReadMessages = callback.requestReadMessages(mCurrentGroup);

        if (mReadMessages == null) {
            mChatBubblesList.setAdapter(null);
            mChatBubblesList.invalidateViews();
            Log.d(TAG, "MESSAGES ARE NULL");
            return;
        }

        Log.d(TAG, "SIZE : " + mReadMessages.size() + "CURRENT GROUP " + mCurrentGroup);

        mBubblesAdapter = new BubblesAdapter(getContext(), R.id.list_view_chat_bubbles, mReadMessages);
        mChatBubblesList.invalidateViews();
        mChatBubblesList.setAdapter(mBubblesAdapter);
    }


    private class ChatListAdapter extends ArrayAdapter<Group> {

        public ChatListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Group> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Group group = getItem(position);

            if (!group.isJoined()) {
                return null;
            }

            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = vi.inflate(R.layout.list_view_item_chat_list, null);
            }

            TextView groupNameTV = convertView.findViewById(R.id.chat_list_group_name);
            groupNameTV.setText(group.getGroupName());

            TextView unreadMsgNumber = convertView.findViewById(R.id.chat_list_unread_msgs_number);

            convertView.setTag(R.string.chat_current_group, group.getGroupName());
            convertView.setTag(R.string.chat_current_id, group.getMyGroupId());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCurrentGroup = (String) view.getTag(R.string.chat_current_group);
                    mCurrentId = (String) view.getTag(R.string.chat_current_id);
                    showBubbles();
                }
            });

            return convertView;
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
                    convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                } else {
                    convertView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
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
                    convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                } else {
                    convertView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
                }

                image.setImageBitmap(((ImageMessage) message).getImage());

            } else {
                return null;
            }
            return convertView;
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
        callback = null;
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
