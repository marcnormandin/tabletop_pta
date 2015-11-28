package edu.utrgv.cgwa.tabletoppta;

public class Utility {
    public static double[] linspace(double x1, double x2, int N) {
        double[] x = new double[N];
        double spacing = (x2 - x1) / (N-1);
        for (int i = 0; i < N; i++) {
            x[i] = x1 + spacing*i;
        }
        return x;
    }

    public static double[] zeros(int N) {
        double[] x = new double[N];
        for (int i = 0; i < N; i++) {
            x[i] = 0.0;
        }
        return x;
    }

    public static double maxabs(double[] x) {
        double max = x[0]; // Assumes we have at least 2 elements
        for (int i = 1; i < x.length; i++) {
            double curr = Math.abs(x[i]);
            if (curr > max) {
                max = curr;
            }
        }
        return max;
    }

    public static int argmax(double[] x) {
        double max = x[0];
        int idx = 0;
        for (int i = 1; i < x.length; i++) {
            if (x[i] > max) {
                idx = i;
                max = x[i];
            }
        }
        return idx;
    }

    // range(1,4,1) returns [1,2,3]
    // range(0, 10, 3) returns [0, 3, 6, 9]
    public static int[] range(int start, int stop, int step) {
        final int numValues = (int) Math.ceil((stop-start)/(1.0*step));
        int[] indices = new int[numValues];
        for (int i = 0; i < numValues; i++) {
            indices[i] = start + (i-1)*step;
        }

        return indices;
    }

    public static int[] range(int start, int stop) {
        return range(start, stop, 1);
    }

    public static int[] range(int stop) {
        return range(0,stop,1);
    }
}
