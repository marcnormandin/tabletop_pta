package edu.utrgv.cgwa.metrec;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;


public class PreferencesFragment extends PreferenceFragment {
    private static final String TAG = "PreferencesFragment";

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Check if the user wants to restore the default settings
        if (preference.getKey().equalsIgnoreCase("restoredefaultsettings")) {
            deleteData();
            resetToDefault();
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    // This will reset all the preferences to their default values
    private void resetToDefault() {
        Context context = (Context) getActivity();
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .clear()
                .commit();
        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
    }

    private void deleteData() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Delete the first metronome
        String prefix = sp.getString("metronome_one_filename_prefix", "error");
        if (!prefix.equalsIgnoreCase("error")) {
            ProfileModel model = new ProfileModel(prefix);
            model.clearAll();
        }else {
            Log.d(TAG, "ERROR! Metronome one prefix not found in settings.");
        }

        // Delete the second metronome
        String prefix2 = sp.getString("metronome_two_filename_prefix", "error");
        if (!prefix2.equalsIgnoreCase("error")) {
            ProfileModel model = new ProfileModel(prefix);
            model.clearAll();
        } else {
            Log.d(TAG, "ERROR! Metronome two prefix not found in settings.");
        }
    }
}

