package edu.utrgv.cgwa.metrec;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    // PCM File is (LOW, HIGH), (LOW, HIGH), (LOW, HIGH)
    // convert two bytes to one double in the range -1 to 1
    private static short bytesToShort(byte lowByte, byte highByte) {
        // convert two bytes to one short (big endian)
        //short s = (short) ((highByte << 8) | lowByte);
        // Credit: http://stackoverflow.com/questions/736815/2-bytes-to-short-java
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(lowByte);
        bb.put(highByte);

        // convert to range from -1 to (just below) 1
        //return s;
        return bb.getShort(0);
    }

    // PCM File is (LOW, HIGH), (LOW, HIGH), (LOW, HIGH)
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
            // PCM File is (LOW, HIGH), (LOW, HIGH), (LOW, HIGH)
            data[i] = (double) bytesToShort(wav[pos], wav[pos + 1]);
            pos += 2;
            i++;
        }
        return data;
    }
}
