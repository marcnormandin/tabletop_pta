package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import edu.utrgv.cgwa.metrec.R;

public class BuiltinMetronomeActivity extends Activity {
    private final String TAG = "BuiltinMetronome";

    BuiltinMetronomeService mService;
    boolean mBound = false;

    private SeekBar skbpm, skv;
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

        Intent intent = new Intent(this, BuiltinMetronomeService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setupSeekbarBPM(savedInstanceState);
        setupSwitchFrequency(savedInstanceState);
        setupSeekbarVolume(savedInstanceState);


        if (savedInstanceState != null) {
            skbpm.setProgress( savedInstanceState.getInt("bpm"));
            skv.setProgress( savedInstanceState.getInt("volume"));
        } else {
            skbpm.setProgress( convertNumberToProgress(pbpm, pbpm.initial) );
            skv.setProgress( convertNumberToProgress(pv, pv.initial) );
        }
    }

    private void setupSeekbarBPM(Bundle savedInstanceState) {
        pbpm = new Property();
        pbpm.min = 10;
        pbpm.max = 240;
        pbpm.numDivisions = 230;
        pbpm.initial = 120;

        // Beats per minute SeekBar
        skbpm=(SeekBar) findViewById(R.id.beatsPerMinuteSeekBar);
        skbpm.setMax(pbpm.numDivisions);

        skbpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                final int bpm = (int) convertProgressToNumber(pbpm.min, pbpm.max, progress, pbpm.numDivisions);

                if (mService != null) {
                    mService.setBeatsPerMinute(bpm);
                }

                // Update the text
                TextView tv = (TextView) findViewById(R.id.textBeatsPerMinute);
                tv.setText("Beats per minute: " + bpm);
            }
        });
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

        final SeekBar skbpm=(SeekBar) findViewById(R.id.beatsPerMinuteSeekBar);
        outState.putInt("bpm", skbpm.getProgress());

        final Switch sf=(Switch) findViewById(R.id.frequencySwitch);
        outState.putBoolean("frequency_checked", sf.isChecked());

        final SeekBar skv=(SeekBar) findViewById(R.id.volumeSeekBar);
        outState.putInt("volume", skv.getProgress());
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
        final int bpm = (int) convertProgressToNumber(pbpm.min, pbpm.max, skbpm.getProgress(), pbpm.numDivisions);
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
