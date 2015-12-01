package edu.utrgv.cgwa.metrec;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.utrgv.cgwa.tabletoppta.PulseProfile;
import edu.utrgv.cgwa.tabletoppta.Routines;
import edu.utrgv.cgwa.tabletoppta.TimeSeries;

public class SingleMetronomeAnalysisActivity extends AppCompatActivity implements ViewPulseOverlay.OnFragmentInteractionListener {
    static private final String TAG = "SingleMetronomeActivity";
    static public final int PICK_PROFILE_REQUEST = 1;  // The request code
    private AudioRecordingModel mAudioRecording = null;
    private long mAudioID = -1;
    private long mProfileID = -1;

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_metronome_analysis);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudioID == -1 || mProfileID == -1) {
                    Snackbar.make(view, "Please set the profile and record audio", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    runAnalysisButton();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PROFILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                long[] ids = data.getLongArrayExtra(SelectProfilesActivity.RESULT_SELECTED_PROFILES);

                if (ids.length != 1) {
                    Toast.makeText(this, "SELECT ONLY ONE PROFILE!", Toast.LENGTH_LONG).show();
                } else {
                    mProfileID = ids[0];
                    Toast.makeText(this, "Selected profile: " + mProfileID, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void buttonSelectProfile(View v) {
        Intent pickProfileIntent = new Intent(this, SelectProfilesActivity.class);
        startActivityForResult(pickProfileIntent, PICK_PROFILE_REQUEST);
    }

    public void buttonRecordAudio(View v) {
        if (mAudioID == -1) {
            recordAudio();
        } else {
            playAudio();
        }
    }

    // Fixme
    // This is hackish
    private String getFilenamePrefix() {
        String uniquePrefix = new SimpleDateFormat("MM-dd-yyyy-hh-mm-ss").format(new Date());
        String commonPrefix = "/metronome_";
        String filesDirectory = getFilesDir() + "";
        String filenamePrefix = filesDirectory + commonPrefix + uniquePrefix;

        return filenamePrefix;
    }

    public void playAudio() {
        if (mAudioRecording != null) {
            AudioRecordingManager manager = new AudioRecordingManager(this);
            mAudioRecording.playRecording(manager.getEntryByID(mAudioID).samplesPerSecond());
        }
    }

    public void recordAudio() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final int sampleRate = Integer.parseInt(sp.getString("samplerate", "8000"));

        // Fixme The duration should be separate from the pulse profile duration
        final double desiredRuntime = Double.parseDouble(sp.getString("pulserecordingduration", "8.0"));

        mAudioRecording = new AudioRecordingModel(getFilenamePrefix());

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

                Button buttonRecord  = (Button) findViewById(R.id.buttonRecordAudio);
                buttonRecord.setText("Recording audio...");
                buttonRecord.setEnabled(false);

                mAudioRecording.setProgressListener(this);
            }

            @Override
            protected Void doInBackground(Void... params) {
                mAudioRecording.newRecording(sampleRate, desiredRuntime);

                publishProgress("Saving times series to the database...");

                Date date = new Date();
                String dateString = new SimpleDateFormat("MM-dd-yyyy").format(date);
                String timeString = new SimpleDateFormat("hh:mm").format(date);

                // Save the time series to the database
                AudioRecordingManager audioManager = new AudioRecordingManager(mContext);
                mAudioID = audioManager.addEntry(dateString, timeString, mAudioRecording.getFilenamePrefix(),
                        mAudioRecording.getFilenamePCM(), mAudioRecording.getFilenameTS(), sampleRate, desiredRuntime,
                        AudioRecordingManager.VALUE_NAME_TAG_SINGLE_METRONOME);

                publishProgress("All results have been saved to database!");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgress.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Button buttonRecord  = (Button) findViewById(R.id.buttonRecordAudio);
                buttonRecord.setText("Play recording");
                buttonRecord.setEnabled(true);

                mProgress.dismiss();
            }
        }

        new NewRecordingAsync(this).execute();
    }

    public PulseProfile getPulseProfile() {
        ProfileManager manager = new ProfileManager(this);
        DbProfileTable.ProfileEntry entry = manager.getEntryByID(mProfileID);
        ProfileModel model = new ProfileModel(entry.filenamePrefix());
        // Fixme We should load the pulse profile directly from file using the stored filename
        return model.getPulseProfile();
    }

    public void runAnalysisButton() {
        final Context context = this;

        class AnalysisAsync extends AsyncTask<Void, String, Void> {
            private ProgressDialog mProgress;

            @Override
            protected void onPreExecute() {
                mProgress = ProgressDialog.show(context, "Performing analysis", "Please be patient");
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgress.dismiss();

                // Add the viewing fragment

            }

            @Override
            protected void onProgressUpdate(String... values) {
                mProgress.setMessage(values[0]);
            }

            @Override
            protected Void doInBackground(Void... params) {
                // Perform the long computation and get the result
                Routines.CalMeasuredTOAsResult result = runAnalysis();

                // Save the result to a unique filename
                String filenameResult = getFilenamePrefix() + ".sar";
                result.saveToFile(filenameResult);

                // Save the analysis results in the database
                Date date = new Date();
                String dateString = new SimpleDateFormat("MM-dd-yyyy").format(date);
                String timeString = new SimpleDateFormat("hh:mm").format(date);
                SingleMetronomeAnalysisManager manager = new SingleMetronomeAnalysisManager(context);
                long analysisID = manager.addEntry(mAudioID, mProfileID, dateString, timeString, filenameResult, "");

                // Show the fragment
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.container, ViewPulseOverlay.newInstance(analysisID));
                ft.commit();

                return null;
            }
        }

        new AnalysisAsync().execute();
    }

    public Routines.CalMeasuredTOAsResult runAnalysis() {
        // Pulse profile to be extended as the template
        PulseProfile pulseProfile = getPulseProfile();

        // Audio recording as the time series
        TimeSeries singleMovingMetronome = mAudioRecording.getTimeSeries();

        // Create the template
        TimeSeries template = Routines.caltemplate(pulseProfile, singleMovingMetronome);

        Routines.CalMeasuredTOAsResult result = Routines.calmeasuredTOAs(singleMovingMetronome, template, pulseProfile.T);

        return result;
    }
}
