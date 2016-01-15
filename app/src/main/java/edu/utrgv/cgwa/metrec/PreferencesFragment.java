package edu.utrgv.cgwa.metrec;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;


public class PreferencesFragment extends PreferenceFragment {
    private static final String TAG = "PreferencesFragment";

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Check if the user wants to restore the default settings
        if (preference.getKey().equalsIgnoreCase("restoredefaultsettings")) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("WARNING!!!");
                alert.setMessage("Are you sure that you restore to default settings?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetToDefault();
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
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
}

