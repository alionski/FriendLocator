package layout;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import aliona.mah.se.friendlocator.Group;
import aliona.mah.se.friendlocator.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private ListView mListView;
    private FloatingActionButton mButtonNewGroup;



    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        initialiseUI(view);
        return view;
    }

    private void initialiseUI(View view) {
        mListView = view.findViewById(R.id.groups_list_view);
        ArrayList<Group> groups = new ArrayList<>();
        mListView.setAdapter( new GroupsAdapter(getContext(), R.layout.list_view_item_groups, groups));

        mButtonNewGroup = view.findViewById(R.id.groups_add_group);


    }

    private class GroupsAdapter extends ArrayAdapter<Group> {

        public GroupsAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        public GroupsAdapter(Context context, int resource, List<Group> items) {
            super(context, resource, items);
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
                Button buttonJoin = v.findViewById(R.id.groups_join_button);
                ToggleButton toggleOnMap = v.findViewById(R.id.groups_toggle_on_map);

                groupName.setText(group.getGroupName());

                if (joined) {
                    buttonJoin.setText(getResources().getString(R.string.groups_button_leave));
                    buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                    toggleOnMap.setEnabled(true);
                    toggleOnMap.setChecked(onMap);
                } else {
                    buttonJoin.setText(getResources().getString(R.string.groups_button_join));
                    buttonJoin.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    toggleOnMap.setEnabled(false);
                }

                buttonJoin.setTag(position);
                buttonJoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (Integer) view.getTag();
                        boolean joinedStatus = getItem(pos).isJoined();
                        getItem(pos).setJoined(!joinedStatus);
                        // TODO: notify the rest of fragment and activity

                    }
                });

                toggleOnMap.setTag(position);
                toggleOnMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        int pos = (Integer) compoundButton.getTag();
                        boolean onMapStatus = getItem(pos).isOnMap();
                        getItem(pos).setOnMap(!onMapStatus);
                        // TODO: notify the rest of fragment and activity
                    }
                });
            }

            return v;
        }
    }

}
