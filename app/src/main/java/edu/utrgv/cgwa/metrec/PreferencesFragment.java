package edu.utrgv.cgwa.metrec;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.io.File;
import java.util.ArrayList;


public class PreferencesFragment extends PreferenceFragment {
    private static final String TAG = "PreferencesFragment";

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Check if the user wants to restore the default settings
        if (preference.getKey().equalsIgnoreCase("restoredefaultsettings")) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("WARNING!!!");
            alert.setMessage("Are you sure that you erase data and restore to default settings?");
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
        } else if (preference.getKey().equalsIgnoreCase("listFiles")) {
            showListOfFilesDialog();
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

        // Reset all preference settings to default
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .clear()
                .commit();
        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);

        // Erase all data
        eraseInternalStorage();
        eraseCache();
        eraseDatabase();
    }

    private void eraseInternalStorage() {
        // Erase all internal data files
        File folder = getActivity().getFilesDir();
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                // noop
            }
        }
    }

    private void eraseCache() {
        // Erase all internal data files
        File folder = getActivity().getCacheDir();
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                // noop
            }
        }
    }

    private void eraseDatabase() {
        String[] databaseList = getActivity().databaseList();
        for (String name : databaseList) {
            getActivity().deleteDatabase(name);
        }
    }

    private void showListOfFilesDialog() {
        // Get a list of the data files
        File folder = getActivity().getFilesDir();
        File[] listOfFiles = folder.listFiles();

        // Add the filenames to the list
        ArrayList<String> listOfFileNames = new ArrayList<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                listOfFileNames.add(file.getName());
            } else if (file.isDirectory()) {
                // noop
            }
        }

        // Add any database files to the list
        for (String fn : getActivity().databaseList()) {
            listOfFileNames.add(fn);
        }

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(PreferencesShowFilesDialog.ARG_LISTOFFILENAMES, listOfFileNames);

        FragmentManager manager = getFragmentManager();
        PreferencesShowFilesDialog d = new PreferencesShowFilesDialog();
        d.setArguments(bundle);

        d.show(manager, "filesDialog");
    }
}

