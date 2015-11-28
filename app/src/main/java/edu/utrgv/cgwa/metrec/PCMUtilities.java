package edu.utrgv.cgwa.metrec;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PCMUtilities {
    private static byte[] readFileIntoByteArray(String filePath) throws IOException
    {
        RandomAccessFile f = new RandomAccessFile(new File(filePath), "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    // convert two bytes to one double in the range -1 to 1
    private static double bytesToDouble(byte firstByte, byte secondByte) {
        // convert two bytes to one short (little endian)
        short s = (short) ((secondByte << 8) | firstByte);
        // convert to range from -1 to (just below) 1
        return s;
    }

    public static double[] readFileIntoArray(String fileName) {
        byte[] wav = null;
        try {
            wav = readFileIntoByteArray(fileName);
        } catch(IOException e) {
        }

        int length = wav.length;
        int samples = (wav.length)/2;     // 2 bytes per sample (16 bit sound mono)

        // Allocate memory (right will be null if only mono sound)
        double[] data = new double[samples];

        // Write to double array/s:
        int i=0;
        int pos = 0;
        while (pos < length) {
            data[i] = bytesToDouble(wav[pos], wav[pos + 1]);
            pos += 2;
            i++;
        }
        return data;
    }
}
