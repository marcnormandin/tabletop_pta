package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ViewDoubleAnalysisResidualsActivity extends AppCompatActivity {
    private static final String TAG = "ViewDoubleResiduals";
    public static final String ARG_ANALYSIS_ID = "analysisID";

    private Toolbar mToolbar;
    private long mAnalysisID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_double_analysis_residuals);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mAnalysisID = intent.getLongExtra(ARG_ANALYSIS_ID, -1);

        if (mAnalysisID == -1) {
            Log.d(TAG, "Attempt to view residuals, but no analysis ID given in intent.");
        } else {
            Log.d(TAG, "Adding residuals fragment");

            DoubleMetronomeAnalysisManager manager = new DoubleMetronomeAnalysisManager(this);
            DbDoubleMetronomeAnalysisTable.Entry entry = manager.getEntryByID(mAnalysisID);

            Log.d(TAG, "Creating fragment for result filename: " + entry.filenameResultOne());
            Log.d(TAG, "Creating fragment for result filename: " + entry.filenameResultTwo());

            // First residual plot
            AnalysisResidualsFragment frag1 = AnalysisResidualsFragment.newInstance(entry.filenameResultOne());
            AnalysisResidualsFragment frag2 = AnalysisResidualsFragment.newInstance(entry.filenameResultTwo());

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.containerOne, frag1, "fragmentOne");
            ft.add(R.id.containerTwo, frag2, "fragmentTwo");
            ft.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_residuals, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_screenshot:
                Screenshot.send(this);
                return true;
        }
        return false;
    }
}
