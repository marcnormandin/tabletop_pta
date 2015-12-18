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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class AudioRecordListFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "AudioRecordListFragment";
    private OnFragmentInteractionListener mListener;
    private RecyclerView mList;
    private MyAdapter mAdapter;

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private LayoutInflater mInflater;
        private AudioRecordingManager mManager;
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
            mManager = new AudioRecordingManager(context);
            mChecked = new Checked(mManager.getNumRecordings());
        }

        public ArrayList<Long> getSelectedEntryIDs() {
            ArrayList<Long> selectedEntryIDs = new ArrayList<>();

            for (int i = 0; i < mChecked.size(); i++) {
                if (mChecked.get(i)) {
                    // The checked object keeps track of positions, not profileIDs,
                    // so we need to convert from positions to profileIDs.
                    long entryID = mManager.getEntryByPosition(i).id();
                    selectedEntryIDs.add(entryID);
                }
            }
            return selectedEntryIDs;
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
                        long id = mManager.getEntryByPosition(i).id();

                        int position = i;

                        mManager.deleteEntryByID(id);
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
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowView = mInflater.inflate(R.layout.fragment_audiorecordlist_list_row, parent, false);
            MyViewHolder holder = new MyViewHolder(rowView);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            DbAudioRecordingTable.AudioRecordingEntry data = mManager.getEntryByPosition(position);

            // Fixme
            // There is not view element for the IDs
            holder.audioID = data.id();

            holder.date.setText(data.date());
            holder.time.setText(data.time());

            holder.audioType.setText(data.tag());

            holder.buttonTimeSeries.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onDisplayTimeSeriesClicked(holder.audioID);
                }
            });

            // Fixme This causes a null-pointer exception
            //holder.checkbox.setChecked( mChecked.get(position) );
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    mChecked.set(position, cb.isChecked());
                    mListener.onCheckboxChanged(position, holder.audioID, cb.isChecked());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mManager.getNumRecordings();
        }

        /*
        public void deleteProfile(int position, long profileID) {
            mManager.deleteEntryByID(profileID);
            mChecked.remove(position);
            //notifyItemRemoved(position);
            notifyDataSetChanged();
        }*/
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time, date, audioType;
        public ImageButton buttonTimeSeries;
        public CheckBox checkbox;

        public long audioID;

        public MyViewHolder(View itemView) {
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.listview_row_time);
            date = (TextView) itemView.findViewById(R.id.listview_row_date);
            audioType = (TextView) itemView.findViewById(R.id.audiotype);

            buttonTimeSeries = (ImageButton) itemView.findViewById(R.id.listview_row_button_timeseries);
            checkbox = (CheckBox) itemView.findViewById(R.id.listview_row_checkbox);

            audioID = -1;
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AudioRecordListFragment() {
    }

    public long[] getSelectedEntryIDs() {
        ArrayList<Long> entryIDs = mAdapter.getSelectedEntryIDs();
        long[] ids = new long[entryIDs.size()];
        for (int i = 0; i < entryIDs.size(); i++) {
            ids[i] = entryIDs.get(i);
        }
        return ids;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio_record_list, container, false);

        Log.d(TAG, "Creating the listview");

        // Find the list view
        mList = (RecyclerView) rootView.findViewById(R.id.audiorecordlist);

        mAdapter = new MyAdapter(getActivity());
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));

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

    public interface OnFragmentInteractionListener {
        void onDisplayTimeSeriesClicked(final long audioID);
        void onCheckboxChanged(final int position, final long audioID, final boolean isChecked);
    }
}
