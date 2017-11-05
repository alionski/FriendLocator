package layout;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

import aliona.mah.se.friendlocator.MainActivity;
import aliona.mah.se.friendlocator.beans.Member;
import aliona.mah.se.friendlocator.interfaces.GroupsFragmentCallback;
import aliona.mah.se.friendlocator.beans.Group;
import aliona.mah.se.friendlocator.R;

/**
 * Fragment that is first loaded upon app launch. Shows all available groups on the server and
 * displays which the user has joined or not.
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = GroupsFragment.class.getName();
    private ArrayAdapter mGroupsAdapter;
    private FloatingActionButton mButtonNewGroup;

    private GroupsFragmentCallback mParent;
    private ArrayList<Group> mGroups = new ArrayList<>();
    private HashMap<String, ArrayList<Member>> mMembers = new HashMap<>();

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "ON CREATE VIEW");
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        mButtonNewGroup = view.findViewById(R.id.groups_add_group);
        mButtonNewGroup.setOnClickListener(this);

        ListView listView = view.findViewById(R.id.groups_list_view);
        mGroupsAdapter = new GroupsAdapter(getContext(), R.layout.list_view_item_groups, mGroups, mMembers);
        listView.setAdapter(mGroupsAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "ON ATTACH");
        super.onAttach(context);
        if (context instanceof GroupsFragmentCallback) {
            mParent = (GroupsFragmentCallback) context;
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "ON RESUME");

        MainActivity.CURRENT_FRAGMENT = MainActivity.GROUPS_ID;
        getActivity().setTitle(R.string.app_name);

        updateGroupsList();

        super.onResume();
    }

    /**
     * Called by MainActivity if the fragment is visible to updte the groups list.
     */
    public void updateGroupsList() {
        if (mParent != null) {
            ArrayList<Group> updatedGroups = mParent.requestUpdateGroups();
            HashMap<String, ArrayList<Member>> members = mParent.getMembers();

            mGroupsAdapter.clear();
            mGroups.clear();
            mGroups.addAll(updatedGroups);
            mMembers.clear();
            mMembers.putAll(members);
            mGroupsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mButtonNewGroup) {
            addNewGroup();
        }
    }

    private class GroupsAdapter extends ArrayAdapter<Group> {
        HashMap<String, ArrayList<Member>> members;

        private GroupsAdapter(Context context, int resource, ArrayList<Group> items,
                             HashMap<String, ArrayList<Member>> members) {
            super(context, resource, items);
            this.members = members;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                convertView = vi.inflate(R.layout.list_view_item_groups, parent, false);
            }

            Group group = getItem(position);

            convertView.setTag(position);

            boolean joined = group.getMyGroupId() != null;

            TextView groupName = convertView.findViewById(R.id.groups_text_view_group_name);
            Button buttonJoin = convertView.findViewById(R.id.groups_join_button);

            buttonJoin.setTag(position);
            buttonJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (Integer) view.getTag();
                    mParent.notifyGroupJoinStatusChanged(getItem(pos).getGroupName(),
                            getItem(pos).getMyGroupId() == null);
                }
            });

            groupName.setText(group.getGroupName());

            if (joined) {
                buttonJoin.setText(getResources().getString(R.string.groups_button_leave));
                buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.groups_rounded_corner_joined));
            } else {
                buttonJoin.setText(getResources().getString(R.string.groups_button_join));
                buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                convertView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.groups_rounded_corner_not_joined));
            }

            if (joined) {
                convertView.findViewById(R.id.joined_group_options).setVisibility(View.VISIBLE);
                ImageButton buttonChat = convertView.findViewById(R.id.button_start_chat);
                buttonChat.setTag(position);

                buttonChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (Integer) view.getTag();
                        mParent.showChat(getItem(pos).getGroupName());
                    }
                });

                ImageButton buttonMap = convertView.findViewById(R.id.button_see_on_map);
                buttonMap.setTag(position);

                buttonMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (Integer) view.getTag();
                        mParent.showMap(getItem(pos).getGroupName());
                    }
                });

            } else {
                convertView.findViewById(R.id.joined_group_options).setVisibility(View.GONE);
            }

            if (members.get(group.getGroupName()) != null) {

                TextView memberView = convertView.findViewById(R.id.members_names);

                StringBuilder builder = new StringBuilder();
                ArrayList<Member> names = members.get(getItem(position).getGroupName());
                builder.append(getResources().getString(R.string.group_members)).append(" ");
                for (int i = 0; i < names.size(); i++) {
                    builder.append(names.get(i).getMemberName());
                    if (i == names.size()-1) {
                        break;
                    }
                    builder.append(", ");
                }
                memberView.setText(builder.toString());
            }

            return convertView;
        }
    }

    /**
     * Called when the user clicks the floating action button.
     */
    private void addNewGroup() {

        final EditText enterName = new EditText(getContext());
        enterName.setPadding(40, 40, 40, 40);
        enterName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        enterName.setTextSize(22);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder
                .setTitle(R.string.add_new_group)
                .setView(enterName)
                .setNegativeButton(R.string.cancel_option,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                .setPositiveButton(R.string.add_new_group_done,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = enterName.getText().toString();
                                if (!name.equals("")) {
                                    mParent.notifyGroupJoinStatusChanged(enterName.getText().toString(), true);
                                }
                                dialogInterface.dismiss();
                            }
                        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
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
