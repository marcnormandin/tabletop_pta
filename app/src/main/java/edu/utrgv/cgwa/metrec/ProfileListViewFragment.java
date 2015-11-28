package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ProfileListViewFragment extends Fragment {
    private static final String TAG = "ProfileListViewFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ListView mListView;

    private class ProfileListAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private ProfileManager mManager = null;

        public ProfileListAdapter(Activity activity) {
            //mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mInflater = activity.getLayoutInflater();
            mManager = new ProfileManager(activity);
        }

        @Override
        public int getCount() {
            Log.d(TAG, "getCount() called");
            final int count = mManager.getNumProfiles();
            Log.d(TAG, "There are " + count + " profiles in the database");

            return count;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // We are not reusing a view, so we need to create a new one
                convertView = mInflater.inflate(R.layout.fragment_profilelist_listview_row, null);
            }

            DbProfileTable.ProfileEntry profile = mManager.getProfileEntryByPosition(position);

            TextView tv = (TextView) convertView.findViewById(R.id.listview_row_date);
            tv.setText(profile.date());

            TextView tv2 = (TextView) convertView.findViewById(R.id.listview_row_time);
            tv2.setText(profile.time());

            ImageButton button = (ImageButton) convertView.findViewById(R.id.listview_row_button_profile);
            button.setOnClickListener(mProfileButtonListener);

            ImageButton button2 = (ImageButton) convertView.findViewById(R.id.listview_row_button_timeseries);
            button2.setOnClickListener(mTimeSeriesButtonListener);

            TextView tv3 = (TextView) convertView.findViewById(R.id.listview_row_bpm);
            tv3.setText("BPM: " + profile.beatsPerMinute());

            return convertView;
        }
    }

    Button.OnClickListener mProfileButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = mListView.getPositionForView(v);
            if (position != ListView.INVALID_POSITION) {
                mListener.onDisplayProfileClicked(position);
            }
        }
    };

    Button.OnClickListener mTimeSeriesButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = mListView.getPositionForView(v);
            if (position != ListView.INVALID_POSITION) {
                mListener.onDisplayTimeSeriesClicked(position);
            }
        }
    };

    // TODO: Rename and change types of parameters
    public static ProfileListViewFragment newInstance(String param1, String param2) {
        ProfileListViewFragment fragment = new ProfileListViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileListViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profilelist_listview, container, false);

        Log.d(TAG, "Creating the listview");

        // Find the list view
        mListView = (ListView) rootView.findViewById(R.id.listView);
        mListView.setAdapter(new ProfileListAdapter(getActivity()));
        mListView.setChoiceMode(mListView.CHOICE_MODE_MULTIPLE);

        // Set the listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Full Profile selected", Toast.LENGTH_SHORT);
            }
        });


        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*
    public void onDeleteSelectedProfiles() {
        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
        if (checkedItemPositions != null) {
            Log.d(TAG, "There are " + checkedItemPositions.size() + " items to delete.");
            for (int i = 0; i < checkedItemPositions.size(); i++) {
                Log.d(TAG, "" + checkedItemPositions.get(i));
            }
        } else {
            Log.d(TAG, "There is nothing to delete!");
        }
    }*/
    public void onDeleteSelectedProfiles() {

    }

    /*
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
        public void onDisplayProfileClicked(int position);
        public void onDisplayTimeSeriesClicked(int position);
    }

}
