package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class PreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // This is safe to call upon first application launch,
        // and subsequent launches. The default values will only
        // be used upon first creation.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();
    }
}
