package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    private RecyclerView mProfileList;
    private LinearLayoutManager mLayoutManager;

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private LayoutInflater mInflater;
        private ProfileManager mManager;

        public MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mManager = new ProfileManager(context);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowView = mInflater.inflate(R.layout.fragment_profilelist_listview_row, parent, false);
            MyViewHolder holder = new MyViewHolder(rowView);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            DbProfileTable.ProfileEntry data = mManager.getProfileEntryByPosition(position);
            holder.date.setText(data.date());
            holder.time.setText(data.time());
            holder.bpm.setText(""+data.beatsPerMinute());

            holder.buttonTimeSeries.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDisplayTimeSeriesClicked(position);
                }
            });

            holder.buttonProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDisplayProfileClicked(position);
                }
            });

            holder.profileID = data.id();

            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteProfile(position, holder.profileID);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mManager.getNumProfiles();
        }

        public void deleteProfile(int position, long profileID) {
            mManager.deleteProfileEntryByProfileID(profileID);
            //notifyItemRemoved(position);
            notifyDataSetChanged();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time, date, bpm;
        public ImageButton buttonTimeSeries, buttonProfile, buttonDelete;

        public long profileID;

        public MyViewHolder(View itemView) {
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.listview_row_time);
            date = (TextView) itemView.findViewById(R.id.listview_row_date);
            bpm  = (TextView) itemView.findViewById(R.id.listview_row_bpm);
            buttonTimeSeries = (ImageButton) itemView.findViewById(R.id.listview_row_button_timeseries);
            buttonProfile = (ImageButton) itemView.findViewById(R.id.listview_row_button_profile);
            buttonDelete = (ImageButton) itemView.findViewById(R.id.listview_row_button_delete);

            profileID = -1;
        }


    }

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
        mProfileList = (RecyclerView) rootView.findViewById(R.id.profilelist);
        mProfileList.setAdapter(new MyAdapter(getActivity()));
        mProfileList.setLayoutManager(new LinearLayoutManager(getActivity()));

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
        SparseBooleanArray checkedItemPositions = mProfileList.getCheckedItemPositions();
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
