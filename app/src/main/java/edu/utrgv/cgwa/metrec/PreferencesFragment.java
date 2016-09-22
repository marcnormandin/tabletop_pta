package edu.utrgv.cgwa.metrec;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class PreferencesFragment extends PreferenceFragment {
    private static final String TAG = "PreferencesFragment";
    private static final String EXPORT_ARCHIVE = "export.zip";

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
        } else if (preference.getKey().equalsIgnoreCase("exportDataFiles")) {
            exportDataFiles();
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

    private void exportDataFiles() {
        final String zipname = Environment.getExternalStorageDirectory()
                + File.separator + EXPORT_ARCHIVE;
        boolean archiveCreated = createExportArchive(zipname);
        if (archiveCreated) {
            Intent ei = new Intent(android.content.Intent.ACTION_SEND);
            //ei.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "normandin.utb@gmail.com" });
            ei.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tabletop PTA DATA EXPORT");
            ei.setType("file/*");

            Uri uri = Uri.fromFile(new File(zipname));
            ei.putExtra(Intent.EXTRA_STREAM, uri);

            getActivity().startActivity(Intent.createChooser(ei, "Export archive..."));
        }
    }

    private boolean createExportArchive(String zipname) {
        OutputStream os;
        try {
            os = new FileOutputStream( zipname );
        }
        catch (FileNotFoundException e ) {
            Toast.makeText(getActivity(), "Error: Unable to create zip file.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));

        ArrayList<File> listOfFileNames = new ArrayList<>();

        // Get the internal files (except the databases)
        File folder = getActivity().getFilesDir();
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                listOfFileNames.add(file);
            } else if (file.isDirectory()) {
                // noop
            }
        }

        // Add any databases
        for (String fn : getActivity().databaseList()) {
            listOfFileNames.add( getActivity().getDatabasePath(fn) );
        }


        int fileCount = listOfFileNames.size();

        try {
            for (int i = 0; i < fileCount; ++i) {
                String filename = listOfFileNames.get(i).getName();
                if (filename != EXPORT_ARCHIVE) {
                    try {
                        byte[] bytes = FileUtils.readFileToByteArray( listOfFileNames.get(i) );

                        ZipEntry entry = new ZipEntry(filename);
                        zos.putNextEntry(entry);
                        zos.write(bytes);
                        zos.closeEntry();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "Error: Unable to compress file.",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            }
        } finally {
            try {
                zos.close();
            }
            catch (IOException e) {
                Toast.makeText(getActivity(), "Error: Encountered an error file closing the zip.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        Toast.makeText(getActivity(), "Export archive created successfully",
                Toast.LENGTH_LONG).show();

        return true;
    }
}

