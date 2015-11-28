package edu.utrgv.cgwa.metrec;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class BuiltinMetronomeActivity extends AppCompatActivity {
    private final String TAG = "BuiltinMetronome";

    BuiltinMetronomeService mService;
    boolean mBound = false;

    private Toolbar mToolbar;

    private SeekBar skv;
    private Spinner mBPM;
    private Switch sf;

    private Property pbpm, pf, pv;

    class Property {
        double min;
        double max;
        double initial;
        int numDivisions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_builtinmetronome);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = new Intent(this, BuiltinMetronomeService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setupSpinnerBPM(savedInstanceState);
        setupSwitchFrequency(savedInstanceState);
        setupSeekbarVolume(savedInstanceState);


        if (savedInstanceState != null) {
            mBPM.setSelection(savedInstanceState.getInt("bpm_position", 0));
            skv.setProgress( savedInstanceState.getInt("volume"));
        } else {
            mBPM.setSelection(0);
            skv.setProgress(convertNumberToProgress(pv, pv.initial));
        }
    }

    private void setupSwitchFrequency(Bundle savedInstanceState) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final double modeA = Double.parseDouble(sp.getString("mode_a_frequency", "900"));
        final double modeB = Double.parseDouble(sp.getString("mode_b_frequency", "1200"));

        // Frequency SeekBar
        sf=(Switch) findViewById(R.id.frequencySwitch);
        sf.setTextOn(modeB + "Hz");
        sf.setTextOff(modeA + "Hz");

        if (savedInstanceState != null) {
            sf.setChecked( savedInstanceState.getBoolean("frequency_checked"));
        } else {
            sf.setChecked(false);
        }

        sf.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    // mode A
                    if (mService != null) {
                        mService.setFrequency(modeB);
                    }
                } else {
                    // mode B
                    if (mService != null) {
                        mService.setFrequency(modeA);
                    }
                }
            }
        });

        double mode;
        if (sf.isChecked()) {
            mode = modeB;
        } else {
            mode = modeA;
        }

        if(mService != null) {
            mService.setFrequency(mode);
        }
    }

    private void setupSpinnerBPM(Bundle savedInstanceState) {
        mBPM = (Spinner) findViewById(R.id.beatsPerMinuteSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.beats_per_minute, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBPM.setAdapter(adapter);
        mBPM.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the value
                int bpm = Integer.parseInt((String) mBPM.getSelectedItem());
                if (mService != null) {
                    mService.setBeatsPerMinute(bpm);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupSeekbarVolume(Bundle savedInstanceState) {
        pv = new Property();
        pv.min = 0.10;
        pv.max = 1.0;
        pv.numDivisions = 9;
        pv.initial = 0.5;

        // Volume SeekBar
        skv=(SeekBar) findViewById(R.id.volumeSeekBar);

        skv.setMax(pv.numDivisions);

        skv.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final double volume = convertProgressToNumber(pv.min, pv.max, progress, pv.numDivisions);

                if (mService != null) {
                    mService.setVolume((float) volume);
                }

                // Update the text
                TextView tv = (TextView) findViewById(R.id.textVolume);
                float percent = (float) volume * 100.0f;
                tv.setText("Volume: " + Math.round(percent) + " %");
            }
        });
    }


    private double convertProgressToNumber(double min, double max, double progress, double numDivisions) {
        double scaled = min + (max - min) * ( progress / numDivisions );
        return scaled;
    }

    private int convertNumberToProgress(double min, double max, double number, double numDivisions) {
        int progress = (int) (numDivisions * (number - min) / (max - min));
        return progress;
    }

    private int convertNumberToProgress(Property p, double number) {
        int progress = convertNumberToProgress(p.min, p.max, number, p.numDivisions);
        return progress;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final Switch sf=(Switch) findViewById(R.id.frequencySwitch);
        outState.putBoolean("frequency_checked", sf.isChecked());

        final SeekBar skv=(SeekBar) findViewById(R.id.volumeSeekBar);
        outState.putInt("volume", skv.getProgress());

        outState.putInt("bpm_position", mBPM.getSelectedItemPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");


        // Update the view to the values used by the sound service

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BuiltinMetronomeService.LocalBinder binder = (BuiltinMetronomeService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            sendSettingsToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private void sendSettingsToService() {
        if (mService == null) {
            return;
        }

        // Beats per minute
        String s = (String) mBPM.getSelectedItem();
        final int bpm = Integer.parseInt(s);
        mService.setBeatsPerMinute(bpm);

        // Frequency
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final double modeA = Double.parseDouble(sp.getString("mode_a_frequency", "900"));
        final double modeB = Double.parseDouble(sp.getString("mode_b_frequency", "1200"));
        Switch sf = (Switch) findViewById(R.id.frequencySwitch);
        if (sf.isChecked()) {
            mService.setFrequency( modeB );
        } else {
            mService.setFrequency( modeA );
        }

        // Volume
        final float volume = (float) convertProgressToNumber(pv.min, pv.max, skv.getProgress(), pv.numDivisions);
        mService.setVolume(volume);

    }

    public void onButtonToggle(View v) {
        if (mBound) {
            if (mService.isPlaying()) {
                mService.stopPlaying();
            } else {
                // Update the frequency here
                mService.startPlaying();
            }
        }

        updateStatusText();
    }

    public void updateStatusText() {
        final Button button = (Button) findViewById(R.id.toggleButton);


        if (mBound) {
            if (mService.isPlaying()) {
                button.setText("stop");
            } else {
                button.setText("start");
            }
        } else {
            button.setText("start");
        }
    }
}
