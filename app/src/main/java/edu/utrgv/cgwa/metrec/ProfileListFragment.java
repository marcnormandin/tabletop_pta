package edu.utrgv.cgwa.metrec;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ProfileListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private RecyclerView mProfileList;
    private MyAdapter mAdapter;

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private LayoutInflater mInflater;
        private ProfileManager mManager;
        private Checked mChecked;

        private class Checked {
            private ArrayList<Boolean> mSelected;

            public Checked(final int numCheckboxes) {
                mSelected = new ArrayList<>();
                for (int i = 0; i < numCheckboxes; i++) {
                    mSelected.add(Boolean.FALSE);
                }
            }

            public boolean get(int position) {
                return mSelected.get(position);
            }

            public void set(int position, boolean checked) {
                mSelected.set(position, checked);
            }

            public void remove(int position) {
                mSelected.remove(position);
            }

            public long size() {
                return mSelected.size();
            }
        }

        public MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mManager = new ProfileManager(context);
            mChecked = new Checked(mManager.getNumProfiles());
        }

        public ArrayList<Long> getSelectedProfileIDs() {
            ArrayList<Long> selectedProfileIDs = new ArrayList<>();

            for (int i = 0; i < mChecked.size(); i++) {
                if (mChecked.get(i)) {
                    // The checked object keeps track of positions, not profileIDs,
                    // so we need to convert from positions to profileIDs.
                    long profileID = mManager.getEntryByPosition(i).profileID();
                    selectedProfileIDs.add(profileID);
                }
            }
            return selectedProfileIDs;
        }

        public void deleteSelectedIDs() {
            boolean moreToCheck = true;
            while(moreToCheck) {
                int i;
                for (i = 0; i < mChecked.size(); i++) {
                    if (mChecked.get(i)) {
                        // The checked object keeps track of positions, not ids,
                        // so we need to convert from positions to ids.
                        // long audioID = mManager.getEntryByPosition(i).audioID();
                        long profileID = mManager.getEntryByPosition(i).profileID();

                        int position = i;

                        mManager.deleteEntryByID(profileID);
                        mChecked.remove(position);
                        moreToCheck = true;
                        break;
                    }
                }
                if (i == mChecked.size()) {
                    moreToCheck = false;
                }
            }

            //notifyItemRemoved(position);
            //notifyDataSetChanged();
            refresh();
        }

        public void refresh() {
            mAdapter.notifyDataSetChanged();
            mChecked = new Checked(mManager.getNumProfiles());
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

            holder.recordID.setText("ID: (" + data.profileID() + ")");
            holder.date.setText(data.date());
            holder.time.setText(data.time());
            holder.bpm.setText("" + data.beatsPerMinute());
            holder.frequency.setText("" + data.frequency());

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

            // Fixme This causes a null-pointer exception
            //holder.checkbox.setChecked( mChecked.get(position) );
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       CheckBox cb = (CheckBox) v;
                       mChecked.set(position, cb.isChecked());
                       mListener.onCheckboxChanged(position, holder.profileID, holder.audioID, cb.isChecked());
                   }
            });
        }

        @Override
        public int getItemCount() {
            return mManager.getNumProfiles();
        }

        public void deleteProfile(int position, long profileID) {
            mManager.deleteEntryByID(profileID);
            mChecked.remove(position);
            //notifyItemRemoved(position);
            notifyDataSetChanged();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time, date, bpm, frequency, recordID;
        public ImageButton buttonTimeSeries, buttonProfile;
        public CheckBox checkbox;

        public long audioID, profileID;

        public MyViewHolder(View itemView) {
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.listview_row_time);
            date = (TextView) itemView.findViewById(R.id.listview_row_date);
            bpm  = (TextView) itemView.findViewById(R.id.listview_row_bpm);
            frequency = (TextView) itemView.findViewById(R.id.listview_row_frequency);
            recordID = (TextView) itemView.findViewById(R.id.listview_row_recordid);
            buttonTimeSeries = (ImageButton) itemView.findViewById(R.id.listview_row_button_timeseries);
            buttonProfile = (ImageButton) itemView.findViewById(R.id.listview_row_button_profile);
            checkbox = (CheckBox) itemView.findViewById(R.id.listview_row_checkbox);

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

    public long[] getSelectedProfileIDs() {
        ArrayList<Long> profileIDs = mAdapter.getSelectedProfileIDs();
        long[] ids = new long[profileIDs.size()];
        for (int i = 0; i < profileIDs.size(); i++) {
            ids[i] = profileIDs.get(i);
        }
        return ids;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profilelist_listview, container, false);

        // Find the list view
        mProfileList = (RecyclerView) rootView.findViewById(R.id.profilelist);

        mAdapter = new MyAdapter(getActivity());
        mProfileList.setAdapter(mAdapter);
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

    public void deleteSelectedIDs() {
        if (mAdapter != null) {
            mAdapter.deleteSelectedIDs();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    public void refresh() {
        if (mAdapter != null) {
            mAdapter.refresh();
        }
    }

    public interface OnFragmentInteractionListener {
        void onDisplayProfileClicked(final long profileID);
        void onDisplayTimeSeriesClicked(final long audioID);
        void onCheckboxChanged(final int position, final long audioID, final long profileID, final boolean isChecked);
    }
}
