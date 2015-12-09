package edu.utrgv.cgwa.metrec;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.utrgv.cgwa.tabletoppta.Routines;

public class ViewAnalysisPulsesActivity extends AppCompatActivity {
    private static final String TAG = "ViewAnalysis";
    public static final String ARG_ANALYSIS_ID = "analysisID";

    private Toolbar mToolbar;
    private long mAnalysisID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_analysis);
        findViewById(android.R.id.content).setDrawingCacheEnabled(true);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mAnalysisID = intent.getLongExtra(ARG_ANALYSIS_ID, -1);
        if (mAnalysisID == -1) {
            Log.d(TAG, "Attempt to view an analysis, but no analysis ID given in intent.");
        } else {
            Log.d(TAG, "Adding pulse overlay fragment");
            AnalysisPulseOverlayFragment frag = AnalysisPulseOverlayFragment.newInstance(mAnalysisID);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.container, frag);
            ft.commit();


            SingleMetronomeAnalysisManager manager = new SingleMetronomeAnalysisManager(this);
            DbSingleMetronomeAnalysisTable.Entry entry = manager.getEntryByID(mAnalysisID);
            Routines.CalMeasuredTOAsResult result = new Routines.CalMeasuredTOAsResult(entry.filenameResult());

            TextView date = (TextView) findViewById(R.id.analysisdate);
            date.setText("" + entry.date());

            TextView time = (TextView) findViewById(R.id.analysistime);
            time.setText("" + entry.time());

            TextView referencePulseNumber = (TextView) findViewById(R.id.analysisreferencepulsenumber);
            referencePulseNumber.setText("" + result.n0());

            TextView numPulses = (TextView) findViewById(R.id.analysisnumpulses);
            numPulses.setText("" + result.numPulses());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_analysis_pulses, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_send_screenshot:
                Bitmap screenshot = takeScreenshot();
                File filename = saveScreenshot(screenshot);
                emailScreenshot(filename);
                return true;
        }
        return false;
    }

    private Bitmap takeScreenshot() {
        //View rootView = findViewById(android.R.id.content);
        //View rootView = findViewById(R.id.root_view);
        //rootView.setDrawingCacheEnabled(true);
        //Bitmap screenshot = rootView.getDrawingCache();
        //rootView.setDrawingCacheEnabled(false);
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        //v1.setDrawingCacheBackgroundColor(Color.BLACK);
        Bitmap screenshot = v1.getDrawingCache();
        //v1.setDrawingCacheEnabled(false);

        return screenshot;
    }

    private File saveScreenshot(Bitmap screenshot) {
        String filename = Environment.getExternalStorageDirectory()
                + File.separator + "Pictures/screenshot.png";

        File file = new File(filename);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            Log.d(TAG, "Saving screenshot as: " + file);
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private void emailScreenshot(File file) {
        Intent ei = new Intent(android.content.Intent.ACTION_SEND);
        ei.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "normandin.utb@gmail.com", "martin.beroiz@gmail.com" });
        ei.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tabletop PTA Screenshot");
        ei.putExtra(android.content.Intent.EXTRA_TEXT, "The attached screenshot was taken of the Tabletop PTA phone app.");
        ei.setType("image/png");

        // URI of the screenshot image
        //Uri myUri = Uri.parse("file:/" + filename);
        Uri uri = Uri.fromFile(file);
        ei.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(ei, "Send mail..."));
    }
}
