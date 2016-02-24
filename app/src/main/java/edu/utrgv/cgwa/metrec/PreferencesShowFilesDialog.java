package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by tabletoppta on 2/24/16.
 */
public class PreferencesShowFilesDialog extends DialogFragment implements View.OnClickListener {
    public static final String ARG_LISTOFFILENAMES = "LISTOFFILENAMES";

    Button buttonClose;
    ArrayList<String> listOfFileNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listOfFileNames = getArguments().getStringArrayList(ARG_LISTOFFILENAMES);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false); // Force user to click a button
        View v = inflater.inflate(R.layout.fragment_preferencesshowfilesdialog, null);

        buttonClose = (Button) v.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(this);

        // Set the title
        getDialog().setTitle("List of files");

        ViewGroup g = (ViewGroup) v.findViewById(R.id.fileContainer);
        for (String filename : listOfFileNames) {
            TextView tv = new TextView(getActivity());
            tv.setText(filename);
            g.addView(tv);
        }

        return v;
    }

    @Override
    public void onClick(View v) {
        // If close button was clicked
        if (v.getId() == R.id.buttonClose) {
            dismiss();
        }
    }
}
