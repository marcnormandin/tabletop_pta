package edu.utrgv.cgwa.metrec;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class DoubleMetronomeAnalysisListFragment extends android.support.v4.app.Fragment {
    private static final String TAG = "AnalysisListFragment";
    private OnFragmentInteractionListener mListener;
    private RecyclerView mAnalysisList;
    private MyAdapter mAdapter;

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private LayoutInflater mInflater;
        private DoubleMetronomeAnalysisManager mManager;
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
            mManager = new DoubleMetronomeAnalysisManager(context);
            mChecked = new Checked(mManager.getNumRecords());
        }

        public ArrayList<Long> getSelectedIDs() {
            ArrayList<Long> ids = new ArrayList<>();

            for (int i = 0; i < mChecked.size(); i++) {
                if (mChecked.get(i)) {
                    // The checked object keeps track of positions, not ids,
                    // so we need to convert from positions to ids.
                    long analysisID = mManager.getEntryByPosition(i).id();
                    ids.add(analysisID);
                }
            }
            return ids;
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowView = mInflater.inflate(R.layout.fragment_double_metronome_analysis_listview_row, parent, false);
            MyViewHolder holder = new MyViewHolder(rowView);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            DbDoubleMetronomeAnalysisTable.Entry entry = mManager.getEntryByPosition(position);

            // Fixme
            // There is not view element for the IDs
            holder.analysisID = entry.id();

            holder.date.setText(entry.date());
            holder.time.setText(entry.time());
            holder.recordID.setText("ID: (" + entry.id() + ")");

            // Fixme This causes a null-pointer exception
            //holder.checkbox.setChecked( mChecked.get(position) );
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    mChecked.set(position, cb.isChecked());
                    mListener.onCheckboxChanged(position, holder.analysisID, cb.isChecked());
                }
            });

            holder.viewPulses.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onViewPulseOverlayClicked(position, holder.analysisID);
                }
            });

            holder.viewResiduals.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onViewResidualsClicked(position, holder.analysisID);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mManager.getNumRecords();
        }

        public void deleteSelectedIDs() {
            boolean moreToCheck = true;
            while (moreToCheck) {
                int i;
                for (i = 0; i < mChecked.size(); i++) {
                    if (mChecked.get(i)) {
                        // The checked object keeps track of positions, not ids,
                        // so we need to convert from positions to ids.
                        long analysisID = mManager.getEntryByPosition(i).id();
                        int position = i;

                        mManager.deleteEntryByID(analysisID);
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
            mChecked = new Checked(mManager.getNumRecords());
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView time, date, recordID;
        public CheckBox checkbox;
        public Button viewPulses, viewResiduals;
        public long analysisID;

        public MyViewHolder(View itemView) {
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.listview_row_time);
            date = (TextView) itemView.findViewById(R.id.listview_row_date);
            recordID = (TextView) itemView.findViewById(R.id.listview_row_recordid);
            checkbox = (CheckBox) itemView.findViewById(R.id.listview_row_checkbox);
            viewPulses = (Button) itemView.findViewById(R.id.buttonViewPulses);
            viewResiduals = (Button) itemView.findViewById(R.id.buttonViewResiduals);

            analysisID = -1;
        }
    }

    public DoubleMetronomeAnalysisListFragment() {
    }

    public void deleteSelectedIDs() {
        if (mAdapter != null) {
            mAdapter.deleteSelectedIDs();
        }
    }

    public long[] getSelectedIDs() {
        ArrayList<Long> idsa = mAdapter.getSelectedIDs();
        long[] ids = new long[idsa.size()];
        for (int i = 0; i < idsa.size(); i++) {
            ids[i] = idsa.get(i);
        }
        return ids;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_double_metronome_analysis_listview, container, false);

        Log.d(TAG, "Creating the listview");

        // Find the list view
        mAnalysisList = (RecyclerView) rootView.findViewById(R.id.analysislist);

        mAdapter = new MyAdapter(getActivity());
        mAnalysisList.setAdapter(mAdapter);
        mAnalysisList.setLayoutManager(new LinearLayoutManager(getActivity()));

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
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void refresh() {
        if (mAdapter != null) {
            mAdapter.refresh();
        }
    }

    public interface OnFragmentInteractionListener {
        void onCheckboxChanged(final int position, final long analysisID, final boolean isChecked);

        void onViewPulseOverlayClicked(final int position, final long analysisID);

        void onViewResidualsClicked(final int position, final long analysisID);
    }
}
