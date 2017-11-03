package layout;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import aliona.mah.se.friendlocator.util.GroupsFragmentCallback;
import aliona.mah.se.friendlocator.util.OnFragmentVisibleListener;
import beans.Group;
import aliona.mah.se.friendlocator.R;
import aliona.mah.se.friendlocator.util.Config;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment implements View.OnClickListener, OnFragmentVisibleListener{
    public static final String TAG = GroupsFragment.class.getName();
    private ListView mListView;
    private FloatingActionButton mButtonNewGroup;

    private GroupsFragmentCallback callback;
    private Group[] groups;



    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "ON CREATE VIEW");
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        mListView = view.findViewById(R.id.groups_list_view);
        mButtonNewGroup = view.findViewById(R.id.groups_add_group);
        mButtonNewGroup.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GroupsFragmentCallback) {
            callback = (GroupsFragmentCallback) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ON RESUME");
    }


    @Override
    public void fragmentBecameVisible() {
        Log.d(TAG, "FRAGMENT VISIBLE");
        groups = callback.requestUpdateGroups();
        mListView.setAdapter(null);
        mListView.invalidateViews();
        if (groups != null) {
            mListView.setAdapter( new GroupsAdapter(getContext(), R.layout.list_view_item_groups, groups));
        }

    }

    public void updateGroupsList(Group[] groups) {
        this.groups = groups;
        Log.d(TAG, String.valueOf(groups.length));
        mListView.setAdapter(null);
        mListView.invalidateViews();
        mListView.setAdapter( new GroupsAdapter(getContext(), R.layout.list_view_item_groups, groups));
    }

    @Override
    public void onClick(View view) {
        if (view == mButtonNewGroup) {
            addNewGroup();
        }
    }

    private class GroupsAdapter extends ArrayAdapter<Group> {
        private final String TAG = GroupsAdapter.class.getName();

        public GroupsAdapter(Context context, int resource, Group[] items) {
            super(context, resource, items);
            Log.d(TAG, "ON CREATE GROUPS ADAPTER");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.list_view_item_groups, null);
            }

            Group group = getItem(position);

            if (group != null) {
                boolean onMap = group.isOnMap();
                boolean joined = group.isJoined();

                TextView groupName = v.findViewById(R.id.groups_text_view_group_name);
                final Button buttonJoin = v.findViewById(R.id.groups_join_button);
                final ToggleButton toggleOnMap = v.findViewById(R.id.groups_toggle_on_map);

                groupName.setText(group.getGroupName());

                if (joined) {
                    buttonJoin.setText(getResources().getString(R.string.groups_button_leave));
                    buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                    toggleOnMap.setEnabled(true);
                    toggleOnMap.setChecked(onMap);
                } else {
                    buttonJoin.setText(getResources().getString(R.string.groups_button_join));
                    buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    toggleOnMap.setChecked(onMap);
                    toggleOnMap.setEnabled(false);
                }

                buttonJoin.setTag(position);
                buttonJoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (Integer) view.getTag();
                        Group group = getItem(pos);
                        group.setJoined(!group.isJoined());
                        if (group.isJoined()) {
                            buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                            buttonJoin.setText(R.string.groups_button_leave);
                            toggleOnMap.setEnabled(true);
                        } else {
                            buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                            buttonJoin.setText(R.string.groups_button_join);
                            toggleOnMap.setEnabled(false);
                        }

                        callback.notifyJoinedStatusChanged(group.getGroupName(), group.isJoined());
                    }
                });

                toggleOnMap.setTag(position);
                toggleOnMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isOnMap) {
                        int pos = (Integer) compoundButton.getTag();
                        Group group = getItem(pos);
                        group.setOnMap(isOnMap);

                        if (isOnMap) {
                            compoundButton.setChecked(true);
                        } else {
                            compoundButton.setChecked(false);
                        }

                        callback.notifyMapVisibilityChanged(group.getGroupName(), group.isOnMap());
                    }
                });
            }
            return v;
        }
    }

    private void addNewGroup() {

        final EditText enterName = new EditText(getContext());
        enterName.setPadding(40, 40, 40, 40);
        enterName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        enterName.setTextSize(22);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder
                .setTitle(R.string.add_new_group)
                .setView(enterName)
                .setNegativeButton("Cancel",
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
                                    callback.startNewGroup(enterName.getText().toString());
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
    public void onDestroy() {
        Log.d(TAG, "ON DESTROY");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}
