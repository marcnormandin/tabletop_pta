package edu.utrgv.cgwa.metrec;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Screenshot {
    public static void send(Activity activity) {
        Bitmap screenshot = takeScreenshot(activity);
        File filename = saveScreenshot(screenshot);
        emailScreenshot(activity, filename);
    }

    private static Bitmap takeScreenshot(Activity activity) {
        //View rootView = findViewById(android.R.id.content);
        //View rootView = findViewById(R.id.root_view);
        //rootView.setDrawingCacheEnabled(true);
        //Bitmap screenshot = rootView.getDrawingCache();
        //rootView.setDrawingCacheEnabled(false);
        View v1 = activity.getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        //v1.setDrawingCacheBackgroundColor(Color.BLACK);
        Bitmap screenshot = v1.getDrawingCache();
        //v1.setDrawingCacheEnabled(false);

        return screenshot;
    }

    private static File saveScreenshot(Bitmap screenshot) {
        String filename = Environment.getExternalStorageDirectory()
                + File.separator + "Pictures/screenshot.png";

        File file = new File(filename);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, out);
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

    private static void emailScreenshot(Activity activity, File file) {
        Intent ei = new Intent(android.content.Intent.ACTION_SEND);
        //ei.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "normandin.utb@gmail.com" });
        ei.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tabletop PTA Screenshot");
        ei.putExtra(android.content.Intent.EXTRA_TEXT, "The attached screenshot was taken of the Tabletop PTA phone app.");
        ei.setType("image/png");

        Uri uri = Uri.fromFile(file);
        ei.putExtra(Intent.EXTRA_STREAM, uri);

        activity.startActivity(Intent.createChooser(ei, "Send mail..."));
    }
}
