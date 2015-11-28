package edu.utrgv.cgwa.tabletoppta;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PulseProfile {
    public TimeSeries ts;
    public double T;
    public double bpm;

    public PulseProfile(TimeSeries ts, double T, double bpm) {
        this.ts = ts;
        this.T = T;
        this.bpm = bpm;
    }

    public PulseProfile(String filename) {
        loadFromFile(filename);
    }

    public void saveToFile(String filename) {
        DataOutputStream os = null;
        try {
            os = new DataOutputStream(new FileOutputStream(filename));

            // Write the period T
            os.writeDouble(T);

            // Write the beats per minute
            os.writeDouble(bpm);

            // Write the common length of the arrays
            os.writeInt(ts.t.length);

            for (int i = 0; i < ts.t.length; i++) {
                os.writeDouble(ts.t[i]);
            }
            for (int i = 0; i < ts.h.length; i++) {
                os.writeDouble(ts.h[i]);
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

            // Read in the period
            this.T = is.readDouble();

            // Read in the beats per minute
            this.bpm = is.readDouble();

            // Read common length of the arrays
            int len = is.readInt();

            double t[] = new double[len];
            for (int i = 0; i < len; i++) {
                t[i] = is.readDouble();
            }

            double h[] = new double[len];
            for (int i = 0; i < len; i++) {
                h[i] = is.readDouble();
            }

            this.ts = new TimeSeries(t, h);

            is.close();
        } catch (Exception e) {
            // Fixme
        } finally {
            // Fixme
        }
    }

    private void loadFromFile(String filename) {
        try {
            RandomAccessFile rFile = new RandomAccessFile(filename, "r");

            // Read in the period
            this.T = rFile.readDouble();

            // Read in the beats per minute
            this.bpm = rFile.readDouble();

            // Get the length of the two arrays
            int len = rFile.readInt();

            // Allocate the two arrays
            double[] t = new double[len];
            double[] h = new double[len];

            FileChannel inChannel = rFile.getChannel();

            // Allocate a buffer for the size of one array
            final int desiredSized = len * Double.SIZE / Byte.SIZE;
            ByteBuffer buf_in = ByteBuffer.allocate(desiredSized);
            if (buf_in.capacity() != desiredSized) {
                // Fixme
            }

            // Read in the time array
            inChannel.read(buf_in);
            buf_in.flip();
            buf_in.asDoubleBuffer().get(t);
            buf_in.clear();

            // Read in the value array
            inChannel.read(buf_in);
            buf_in.flip();
            buf_in.asDoubleBuffer().get(h);
            buf_in.clear();

            this.ts = new TimeSeries(t, h);

            inChannel.close();

        }
        catch (OutOfMemoryError e) {
            // Fixme
        }
        catch (Exception e) {
            // Fixme
        }
        finally {
            // Fixme
        }
    }
}