package edu.utrgv.cgwa.metrec;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProfileListFragment extends Fragment {
    private static final String TAG = "ProfileListFragment";
    private OnFragmentInteractionListener mListener;
    private RecyclerView mProfileList;

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
            DbProfileTable.ProfileEntry data = mManager.getEntryByPosition(position);

            // Fixme
            // There is not view element for the IDs
            holder.audioID = data.audioID();
            holder.profileID = data.profileID();

            holder.date.setText(data.date());
            holder.time.setText(data.time());
            holder.bpm.setText(""+data.beatsPerMinute());
            holder.frequency.setText(""+data.frequency());

            holder.buttonTimeSeries.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDisplayTimeSeriesClicked(holder.audioID);
                }
            });

            holder.buttonProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDisplayProfileClicked(holder.profileID);
                }
            });

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
            mManager.deleteEntryByID(profileID);
            //notifyItemRemoved(position);
            notifyDataSetChanged();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time, date, bpm, frequency;
        public ImageButton buttonTimeSeries, buttonProfile, buttonDelete;

        public long audioID, profileID;

        public MyViewHolder(View itemView) {
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.listview_row_time);
            date = (TextView) itemView.findViewById(R.id.listview_row_date);
            bpm  = (TextView) itemView.findViewById(R.id.listview_row_bpm);
            frequency = (TextView) itemView.findViewById(R.id.listview_row_frequency);
            buttonTimeSeries = (ImageButton) itemView.findViewById(R.id.listview_row_button_timeseries);
            buttonProfile = (ImageButton) itemView.findViewById(R.id.listview_row_button_profile);
            buttonDelete = (ImageButton) itemView.findViewById(R.id.listview_row_button_delete);

            audioID = -1;
            profileID = -1;
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileListFragment() {
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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onDisplayProfileClicked(final long position);
        void onDisplayTimeSeriesClicked(final long position);
    }

}
