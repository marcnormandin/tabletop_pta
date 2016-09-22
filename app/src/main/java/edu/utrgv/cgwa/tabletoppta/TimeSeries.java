package edu.utrgv.cgwa.tabletoppta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

public class TimeSeries {
    public double[] h;
    public double[] t;

    public TimeSeries(double[] t, double[] h) {
        this.h = h;
        this.t = t;
    }

    public TimeSeries(TimeSeries cpy) {
        this.h = new double[cpy.h.length];
        this.t = new double[cpy.t.length];
        System.arraycopy(cpy.h, 0, this.h, 0, this.h.length);
        System.arraycopy(cpy.t, 0, this.t, 0, this.t.length);
    }

    public TimeSeries(String filename) {
        loadFromFileFast(filename);
    }

    public int getSampleRate() {
        return (int) Math.round( 1.0 / (this.t[1] - this.t[0]) );
    }

    public void saveToFile(String filename) {
        saveToFileFast(filename);
    }

    private void saveToFileFast(String filename) {
        try {
            RandomAccessFile rFile = new RandomAccessFile(filename, "rw");

            // Get the length of the two arrays
            final int len = this.t.length;

            // Write out the array length
            rFile.writeInt(len);

            FileChannel inChannel = rFile.getChannel();

            // Allocate a buffer for the size of one array
            final int desiredSized = len * Double.SIZE / Byte.SIZE;
            ByteBuffer buf = ByteBuffer.allocate(desiredSized);
            if (buf.capacity() != desiredSized) {
                // Fixme
            }

            // Write the time array
            buf.asDoubleBuffer().put(this.t);
            inChannel.write(buf);
            buf.flip();
            buf.clear();

            // Write the value array
            buf.asDoubleBuffer().put(this.h);
            inChannel.write(buf);
            buf.flip();
            buf.clear();

            inChannel.close();

        } catch (Exception e) {
            // Fixme
        }
        finally {
            // Fixme
        }
    }

    // Fixme This is slow!
    public void saveToFileSlow(String filename) {
        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new FileOutputStream(filename));

            // Write the common length of the arrays
            os.writeInt(t.length);

            for (int i = 0; i < t.length; i++) {
                os.writeDouble(t[i]);
            }
            for (int i = 0; i < h.length; i++) {
                os.writeDouble(h[i]);
            }
            os.close();
        } catch (Exception e) {
            // Fixme
        } finally {
            // Fixme
        }
    }


    private void loadFromFileSlow(String filename) {
        DataInputStream is = null;

        try {
            is = new DataInputStream(new FileInputStream(filename));

            int len = is.readInt();

            this.t = new double[len];
            for (int i = 0; i < len; i++) {
                this.t[i] = is.readDouble();
            }

            this.h = new double[len];
            for (int i = 0; i < len; i++) {
                this.h[i] = is.readDouble();
            }
            is.close();
        } catch (Exception e) {
            // Fixme
        } finally {
            // Fixme
        }
    }

    private void loadFromFileFast(String filename) {
        try {
            RandomAccessFile rFile = new RandomAccessFile(filename, "rw");

            // Get the length of the two arrays
            int len = rFile.readInt();

            FileChannel inChannel = rFile.getChannel();

            // Allocate the two arrays
            this.t = new double[len];
            this.h = new double[len];

            // Allocate a buffer for the size of one array
            final int desiredSized = len * Double.SIZE / Byte.SIZE;
            ByteBuffer buf_in = ByteBuffer.allocate(desiredSized);
            if (buf_in.capacity() != desiredSized) {
                // Fixme
            }

            // Read in the time array
            inChannel.read(buf_in);
            buf_in.flip();
            buf_in.asDoubleBuffer().get(this.t);
            buf_in.clear();

            // Read in the value array
            inChannel.read(buf_in);
            buf_in.flip();
            buf_in.asDoubleBuffer().get(this.h);
            buf_in.clear();

            inChannel.close();

        } catch (Exception e) {
            // Fixme
        }
        finally {
            // Fixme
        }
    }


    // This tests whether or not this class saves and loads its data from file correctly
    static public boolean test(String filename) {
        int N = 8000 * 8;
        double[] t = new double[N];
        double[] h = new double[N];

        Random rn = new Random();
        for (int i = 0; i < t.length; i++) {
            t[i] = rn.nextDouble();
            h[i] = rn.nextDouble();
        }

        // Create a time series and write it to file
        TimeSeries ts_saved = new TimeSeries(t, h);
        ts_saved.saveToFile(filename);

        // Load a time series from file
        TimeSeries ts_loaded = new TimeSeries(filename);

        if (ts_saved.t.length != ts_loaded.t.length) {
            return false;
        }

        for (int i = 0; i < ts_saved.t.length; i++) {
            if (ts_saved.t[i] != ts_loaded.t[i]) {
                return false;
            }
            if (ts_saved.h[i] != ts_loaded.h[i]) {
                return false;
            }
        }

        return true;
    }
}

