package edu.utrgv.cgwa.metrec;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class NewAudioRecordingFragment extends Fragment implements View.OnClickListener{

    private NewAudioRecordingListener mListener;
    private AutoCompleteTextView mAudioTag;

    public static final String ARG_AUDIO_TAG = "ARG_AUDIO_TAG";

    public NewAudioRecordingFragment() {
        // Required empty public constructor
    }

    public static NewAudioRecordingFragment newInstance(final String audioTag) {
        NewAudioRecordingFragment fragment = new NewAudioRecordingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AUDIO_TAG, audioTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_audio_recording, container, false);

        Button rec = (Button) rootView.findViewById(R.id.buttonRecord);
        rec.setOnClickListener(this);

        Button cancel = (Button) rootView.findViewById(R.id.buttonCancel);
        cancel.setOnClickListener(this);

        setupAudioTag(rootView);

        if (getArguments() != null) {
            String tag = getArguments().getString(ARG_AUDIO_TAG, "NULL");
            if (!tag.equals("NULL")) {
                mAudioTag.setText(tag);
            }
        }

        return rootView;
    }

    public void setupAudioTag(View rootView) {
        mAudioTag = (AutoCompleteTextView) rootView.findViewById(R.id.audioTag);

        ArrayList<String> array = new ArrayList<>();
        array.add(AudioRecordingManager.VALUE_NAME_TAG_PULSE_PROFILE);
        array.add(AudioRecordingManager.VALUE_NAME_TAG_SINGLE_METRONOME);
        array.add(AudioRecordingManager.VALUE_NAME_TAG_DOUBLE_METRONOME);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAudioTag.setAdapter(adapter);
    }


    // Fixme
    // This is hackish
    private String getFilenamePrefix() {
        String uniquePrefix = new SimpleDateFormat("MM-dd-yyyy-hh-mm-ss").format(new Date());
        String commonPrefix = "/metronome_";
        String filesDirectory = getContext().getFilesDir() + "";
        String filenamePrefix = filesDirectory + commonPrefix + uniquePrefix;

        return filenamePrefix;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonCancel:
                buttonCancel(v);
                return;
            case R.id.buttonRecord:
                buttonRecord(v);
                return;
        }
    }

    public void buttonCancel(View v) {
        mListener.cancelRecording();
    }

    public void buttonRecord(final View v) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final int sampleRate = Integer.parseInt(sp.getString("samplerate", "8000"));
        final double timeDelay = Double.parseDouble(sp.getString("recordingdelay", "4.0"));
        final double desiredRuntime = Double.parseDouble(sp.getString("recordingduration", "8.0"));



        final String tag = mAudioTag.getText().toString();

        final AudioRecordingModel mAudioRecording = new AudioRecordingModel(getFilenamePrefix());

        class NewRecordingAsync extends AsyncTask<Void, String, Void>
                implements AudioRecordingModel.ProgressListener {
            private ProgressDialog mProgress = null;
            private Context mContext;

            public NewRecordingAsync(Context context) {
                mContext = context;
            }

            @Override
            public void onRecordingStarted() {
                publishProgress("Recording audio...");
            }

            @Override
            public void onRecordingFinished() {
                publishProgress("Recording audio has ended!");
            }

            @Override
            public void onTimeSeriesStart() {
                publishProgress("Processing time series...");
            }

            @Override
            public void onTimeSeriesFinished() {
                publishProgress("Time series processed!");
            }

            @Override
            protected void onPreExecute() {
                mProgress = ProgressDialog.show(mContext, "Working...", "Please wait");

                Button buttonRecord  = (Button) v;
                buttonRecord.setText("Recording audio...");
                buttonRecord.setEnabled(false);

                mAudioRecording.setProgressListener(this);
            }

            @Override
            protected Void doInBackground(Void... params) {
                mAudioRecording.newRecording(sampleRate, timeDelay, desiredRuntime);

                publishProgress("Saving times series to the database...");

                Date date = new Date();
                String dateString = new SimpleDateFormat("MM-dd-yyyy").format(date);
                String timeString = new SimpleDateFormat("hh:mm").format(date);

                // Save the time series to the database
                AudioRecordingManager audioManager = new AudioRecordingManager(mContext);
                long mAudioID = audioManager.addEntry(dateString, timeString, mAudioRecording.getFilenamePrefix(),
                        mAudioRecording.getFilenamePCM(), mAudioRecording.getFilenameTS(), sampleRate, desiredRuntime,
                        tag);

                publishProgress("All results have been saved to database!");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgress.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Button buttonRecord  = (Button) v;
                buttonRecord.setText("Record");
                buttonRecord.setEnabled(true);

                mProgress.dismiss();

                mListener.recordingFinished();
            }
        }

        new NewRecordingAsync(getContext()).execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NewAudioRecordingListener) context;
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

    public interface NewAudioRecordingListener {
        void cancelRecording();
        void recordingFinished();
    }
}
