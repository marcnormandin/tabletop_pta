package edu.utrgv.cgwa.tabletoppta;

import android.util.Log;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.util.Precision;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import ca.uol.aig.fftpack.Complex1D;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class Routines {
    private static final String TAG = "Routines";
    public static PulseProfile calpulseprofile(TimeSeries ts, double bpm) {
        // calculated pulse period
        double Tp = calpulseperiod(ts, 60 / bpm);

        // fold time series using calculated period
        TimeSeries fs = foldtimeseries(ts, Tp);

        // find the index of the maximum value
        int maxidx = Utility.argmax(fs.h);
        final double prePercent = 0.1; // What percentage of the total values to include before the peak
        int pivotidx = (int) Math.floor(maxidx - (prePercent * fs.h.length));
        if (pivotidx < 0) {
            pivotidx = 0;
        }


        // Now circularly shift the time series
        // since we want the peak towards the initial time
        double[] fss = new double[fs.t.length];
        // Values after the peak
        for (int i = 0, j = pivotidx; j < fs.h.length; i++, j++) {
            fss[i] = fs.h[j];
        }
        // Values before the peak
        for (int i = fs.h.length - pivotidx, j = 0; i < fss.length; i++, j++) {
            fss[i] = fs.h[j];
        }

        // Determine the maximum absolute value
        double norm = Utility.maxabs(fss);

        // Now copy back to the old array
        for (int i = 0; i < fss.length; i++) {
            fs.h[i] = fss[i] / norm;
        }

        PulseProfile pf = new PulseProfile(fs, Tp, bpm);

        return pf;
    }

    public static double calpulseperiod(TimeSeries ts, double T) {
        // array of trial periods
        double eps = 1e-4;
        int Ntrials = 101;

        double[] Tt = Utility.linspace(T - eps, T + eps, Ntrials);
        double[] yf_max = Utility.zeros(Ntrials);

        for (int i = 0; i < Ntrials; i++) {
            TimeSeries fs = foldtimeseries(ts, Tt[i]);
            yf_max[i] = Utility.maxabs(fs.h);
        }

        int ndx = Utility.argmax(yf_max);
        double Tp = Tt[ndx];
        return Tp;
    }


    public static TimeSeries foldtimeseries(TimeSeries ts, double T) {
        double deltaT = ts.t[1] - ts.t[0];
        double tbin = deltaT;
        int Nbins = (int) Math.floor((T + 0.5 * tbin) / tbin) + 1;

        TimeSeries fs = new TimeSeries(Utility.linspace(0, tbin * (Nbins - 1), Nbins), Utility.zeros(Nbins));

        int[] bincounts = new int[Nbins];

        for (int i = 0; i < ts.t.length; i++) {
            double foldedTime = (ts.t[i] - ts.t[0]) % T;
            int b = (int) Math.floor((foldedTime + 0.5 * tbin) / tbin);

            fs.h[b] += ts.h[i];
            bincounts[b] += 1;
        }

        for (int b = 0; b < fs.t.length; b++) {
            if (bincounts[b] != 0) {
                fs.h[b] = fs.h[b] / bincounts[b];
            }
        }

        return fs;
    }


    /*
        def calcorrcoeff(x,y):

        '''
        calculate correlation coefficient of x and y, relative to x, y, or z
        '''

        rhox = np.sum(x*y)/np.sum(x*x)
        rhoy = np.sum(x*y)/np.sum(y*y)
        rhoxy = np.sum(x*y)/np.sqrt(np.sum(x*x) * np.sum(y*y))

        return rhox, rhoy, rhoxy
     */
    public static void calcorrcoeff(double[] x, double[] y) {

    }


    /*
        def calexpectedTOAs(t0, n0, Np, Tp):

                '''
        calculated expected TOAs given reference TOA and estimated pulse period
        '''

        expectedTOAs = t0 + np.transpose(np.linspace((1-n0)*Tp, (Np-n0)*Tp, Np));

        return expectedTOAs
    */
    public static double[] calexpectedTOAs(double t0, int n0, int Np, double Tp) {
        double[] expectedTOAs = Utility.linspace(t0 + (1 - n0) * Tp, t0 + (Np - n0) * Tp, Np);
        return expectedTOAs;
    }


    static private class MyUnivariateFunction implements UnivariateFunction {
        private TimeSeries mTimeSeries;
        private TimeSeries mTemplate;
        private double mNorm;

        public MyUnivariateFunction(TimeSeries ts, TimeSeries template, double norm) {
            mTimeSeries = ts;
            mTemplate = template;
            mNorm = norm;
        }

        @Override
        public double value(double tau) {
            return correlate(tau, new TimeSeries(mTimeSeries), new TimeSeries(mTemplate), mNorm);
        }
    }

    static public class CalMeasuredTOAsResult {
        private double[] mMeasuredTOAs;
        private double[] mUncertainties;
        private int mN0;
        private double[] mExpectedTOAs;
        private double[] mResiduals;
        private double[] mDetrendedResiduals;

        private double[] mCorrelation;
        private int mInd0;
        private double mComputationTimeSeconds;

        public CalMeasuredTOAsResult(double[] measuredTOAs, double[] uncertainties, int n0, double[] expectedTOAs,
                                     double[] correlationArray, int ind0, double computationTimeSeconds) {
            mMeasuredTOAs = measuredTOAs;
            mUncertainties = uncertainties;
            mN0 = n0;
            mExpectedTOAs = expectedTOAs;

            mCorrelation = correlationArray;
            mInd0 = ind0;

            mComputationTimeSeconds = computationTimeSeconds;

            computeResiduals();
            computeDetrendedResiduals();

            Log.d("CREATED RESULT", "Corrleation array length = " + mCorrelation.length);

        }

        public CalMeasuredTOAsResult(final String filename) {
            loadFromFile(filename);
            computeResiduals();
            computeDetrendedResiduals();
        }

        private void computeResiduals() {
            mResiduals = new double[mMeasuredTOAs.length];
            for (int i = 0; i < mMeasuredTOAs.length; i++) {
                mResiduals[i] = mMeasuredTOAs[i] - mExpectedTOAs[i];
            }
        }

        private void computeDetrendedResiduals() {
            mDetrendedResiduals = new double[mResiduals.length];
            for (int i = 0; i < mResiduals.length; i++) {
                mDetrendedResiduals[i] = mResiduals[i];
            }
            mDetrendedResiduals = detrend(mMeasuredTOAs, mDetrendedResiduals);
        }

        public double[] measuredTOAs() { return mMeasuredTOAs; }
        public double[] uncertainties() { return mUncertainties; }
        public int n0() { return mN0; }
        public int numPulses() { return mMeasuredTOAs.length; }
        public double[] expectedTOAs() { return mExpectedTOAs; }
        public double[] residuals() { return mResiduals; }
        public double[] detrendedResiduals() { return mDetrendedResiduals; }
        public double[] correlation() { return mCorrelation; }
        public int ind0() { return mInd0; }
        public double computationTimeSeconds() { return mComputationTimeSeconds; }

        private void loadFromFile(String filename) {
            try {
                RandomAccessFile rFile = new RandomAccessFile(filename, "rw");

                // Get the common length of the arrays
                int len = rFile.readInt();
                Log.d("LOADING RESULT FILE", "LEN ARRAYS = " + len);

                // Get N0
                mN0 = rFile.readInt();

                // Get IND0
                mInd0 = rFile.readInt();

                // Get correlation size
                int lengthCorrelation = rFile.readInt();

                // Get the computation time
                mComputationTimeSeconds = rFile.readDouble();

                FileChannel inChannel = rFile.getChannel();

                // Allocate the two arrays
                this.mMeasuredTOAs = new double[len];
                this.mExpectedTOAs = new double[len];
                this.mUncertainties = new double[len];
                this.mCorrelation = new double[lengthCorrelation];

                Log.d("LOADING RESULT FILE", "LEN CORRELATION = " + mCorrelation.length);

                // Allocate a buffer for the size of one array
                final int desiredSized = len * Double.SIZE / Byte.SIZE;
                ByteBuffer buf_in = ByteBuffer.allocate(desiredSized);
                if (buf_in.capacity() != desiredSized) {
                    Log.d(TAG, "ERROR! Buffer capacity is not desired capacity. The logic will be wrong.");
                }

                // Read in mMeasuredTOAs
                inChannel.read(buf_in);
                buf_in.flip();
                buf_in.asDoubleBuffer().get(this.mMeasuredTOAs);
                buf_in.clear();

                // Read in mExpectedTOAs
                inChannel.read(buf_in);
                buf_in.flip();
                buf_in.asDoubleBuffer().get(this.mExpectedTOAs);
                buf_in.clear();

                // Read in mUncertainties
                inChannel.read(buf_in);
                buf_in.flip();
                buf_in.asDoubleBuffer().get(this.mUncertainties);
                buf_in.clear();

                Log.d("LOADING FROM FILE", "Correlation array length = " + mCorrelation.length);


                // Allocate a buffer for the size of one array
                final int desiredSizeda = lengthCorrelation * Double.SIZE / Byte.SIZE;
                ByteBuffer buf_ina = ByteBuffer.allocate(desiredSizeda);
                if (buf_ina.capacity() != desiredSizeda) {
                    Log.d(TAG, "ERROR! Buffer capacity (for correlation array) is not desired capacity. The logic will be wrong.");
                }

                // Read in mCorrelation
                inChannel.read(buf_ina);
                buf_ina.flip();
                buf_ina.asDoubleBuffer().get(this.mCorrelation);
                buf_ina.clear();

                inChannel.close();

            } catch (Exception e) {
                // Fixme
            }
            finally {
                // Fixme
            }
        }

        // Fixme This is slow
        private void saveToFileSlow(String filename) {
            DataOutputStream os = null;
            try {
                os = new DataOutputStream(new FileOutputStream(filename));

                // Write the common length of the arrays
                os.writeInt(mMeasuredTOAs.length);

                // Write N0
                os.writeInt(mN0);

                // Write IND0
                os.writeInt(mInd0);

                // Write the length of the correlation array
                os.writeInt(mCorrelation.length);

                // Write the computation time
                os.writeDouble(mComputationTimeSeconds);

                // Write mMeasuredTOAs
                for (int i = 0; i < mMeasuredTOAs.length; i++) {
                    os.writeDouble(mMeasuredTOAs[i]);
                }

                // Write mExpectedTOAs
                for (int i = 0; i < mExpectedTOAs.length; i++) {
                    os.writeDouble(mExpectedTOAs[i]);
                }

                // Write mResiduals
                for (int i = 0; i < mResiduals.length; i++) {
                    os.writeDouble(mResiduals[i]);
                }

                Log.d("SAVING TO FILE", "Correlation array length = " + mCorrelation.length);


                // Write mCorrelation
                for (int i = 0; i < mCorrelation.length; i++) {
                    os.writeDouble(mCorrelation[i]);
                }

                os.close();
            } catch (Exception e) {
                // Fixme
            } finally {
                // Fixme
            }
        }

        public void saveToFile(String filename) {
            try {
                RandomAccessFile rFile = new RandomAccessFile(filename, "rw");

                // Write the common length of the residual-related arrays
                final int len = this.mMeasuredTOAs.length;
                rFile.writeInt(len);

                // Write N0
                rFile.writeInt(mN0);

                // Write IND0
                rFile.writeInt(mInd0);

                // Write correlation array length
                final int lengthCorrelation = this.mCorrelation.length;
                rFile.writeInt(lengthCorrelation);

                // Get the computation time
                rFile.writeDouble(mComputationTimeSeconds);

                FileChannel inChannel = rFile.getChannel();

                // Allocate a buffer for the size of one array
                final int desiredSized = len * Double.SIZE / Byte.SIZE;
                ByteBuffer buf_in = ByteBuffer.allocate(desiredSized);
                if (buf_in.capacity() != desiredSized) {
                    Log.d(TAG, "ERROR! Buffer capacity is not desired capacity. The logic will be wrong.");
                }

                // Write mMeasuredTOAs
                buf_in.asDoubleBuffer().put(this.mMeasuredTOAs);
                inChannel.write(buf_in);
                buf_in.flip();
                buf_in.clear();

                // Write mExpectedTOAs
                buf_in.asDoubleBuffer().put(this.mExpectedTOAs);
                inChannel.write(buf_in);
                buf_in.flip();
                buf_in.clear();

                // Write mUncertainties
                buf_in.asDoubleBuffer().put(this.mUncertainties);
                inChannel.write(buf_in);
                buf_in.flip();
                buf_in.clear();


                // Allocate a buffer for the size of one array
                final int desiredSizeda = lengthCorrelation * Double.SIZE / Byte.SIZE;
                ByteBuffer buf_ina = ByteBuffer.allocate(desiredSizeda);
                if (buf_ina.capacity() != desiredSizeda) {
                    Log.d(TAG, "ERROR! Buffer capacity (for correlation array) is not desired capacity. The logic will be wrong.");
                }

                // Write mCorrelation
                buf_ina.asDoubleBuffer().put(this.mCorrelation);
                inChannel.write(buf_ina);
                buf_ina.flip();
                buf_ina.clear();

                inChannel.close();

            } catch (Exception e) {
                // Fixme
            }
            finally {
                // Fixme
            }
        }
    }

    /*
    def calmeasuredTOAs(ts, template, Tp):

        '''
        calculated measured TOAs and corresponding uncertainties by
        correlating time-series with template
        '''

        DEBUG = 0

                # extract relevant time-domain quantities
        N = len(ts[:,0])
        deltaT = ts[1,0]-ts[0,0]
        deltaF = 1/(N*deltaT)
        fNyq = 1/(2*deltaT)

                # +/- indices for searching around peaks of correlation function
        Tcorr = 4e-4 # approx width of correlation peaks
        m = int(np.floor(Tcorr/deltaT))

                # fourier transform time-series and template data
        ytilde = deltaT * np.fft.fft(ts[:,1])
        ptilde = deltaT * np.fft.fft(template[:,1])

                # correlate data (maxima give TOAs)
        # normalization implies that values of C at maxima are estimates of pulse amplitudes
                C = N * deltaF * np.fft.ifft(ytilde * np.conj(ptilde))
        norm = np.real(1/sum(deltaF * ptilde * np.conj(ptilde)))
        C = np.real(norm*C) # take real part to avoid imaginary round-off components

        # find location of max correlation for reference pulse TOA
                ind0 = np.argmax(C)
        C0 = C[ind0]

        # use brent's method to find max
        print 'calculating reference TOA'

                if ind0 - m < 0:
        indices = range(0, ind0+m+1)
        elif ind0 + m > len(C):
        indices = range(ind0-m, len(C))
                else:
        indices = range(ind0-m, ind0+m+1)

        brack = (ts[indices[0],0], ts[ind0,0], ts[indices[-1],0])
        # scipy.optimize.brent(func, args=(), brack=None, tol=1.48e-08, full_output=0, maxiter=500)

        t0 = opt.brent(correlate, (ts, template, -norm), brack, 1e-07, 0, 500)
        A0 = correlate(t0, ts, template, norm)

        # calculate expected number of pulses
        N1 = int(np.floor(t0/Tp))
        N2 = int(np.floor((ts[-1,0]-t0)/Tp))
        Np = N1 + N2 + 1
        n0 = N1 + 1; # reference pulse number
        print 'reference TOA (n=', n0, ') has correlation=', A0

        # calculate expected indices of TOAs
                tmp = ind0 + np.round(np.linspace((Tp/deltaT)*(1-n0), (Tp/deltaT)*(Np-n0), Np));
        expected_ind = tmp.astype(int)

                # initialize variables
        tauhat = np.zeros(Np)
        Ahat = np.zeros(Np)

                # loop to find measured TOAs and amplitudes
        for ii in range(0,Np):

        print 'calculating TOA', ii+1
        expected = expected_ind[ii]

                # search in a small region around expected arrival time
        if expected - m < 0:
        indices = range(0,expected+m+1)
        elif expected + m > len(C):
        indices = range(expected-m, len(C))
                else:
        indices = range(expected-m, expected+m+1)

        # use brent's method to find max
        idx = indices[0] + np.argmax(C[indices])

        brack = (ts[indices[0],0], ts[idx,0], ts[indices[-1],0])

                try:
        tauhat[ii] = opt.brent(correlate, (ts, template, -norm), brack, 1e-07, 0, 500)
        badTOA = 0
        except:
        badTOA = 1
        print 'bad TOA'

                # set tauhat, Ahat to nan if brent's method can't find maximum
        if badTOA:
        tauhat[ii] = np.nan
        Ahat[ii] = np.nan
        else:
        Ahat[ii] = correlate(tauhat[ii], ts, template, norm)

        # error estimate for TOAs (based on correlation curve)
        # basically, we can determine the max of the correlation to +/- 0.5 deltaT;
        # multiply by 1/sqrt(Ahat(ii)) to increase error bar for small correlations

        ##Ahat_max = np.max(Ahat) # nan is max (which i don't want)
                Ahat_max = max(Ahat)

        error_tauhat = np.zeros(len(tauhat))
                for ii in range(0, len(tauhat)):
        error_tauhat[ii] = 0.5*deltaT/np.sqrt(Ahat[ii])

                # assign output variables (only TOAs and their uncertainties needed)
        measuredTOAs = tauhat
                uncertainties = error_tauhat

        return measuredTOAs, uncertainties, n0
    */

    public static CalMeasuredTOAsResult calmeasuredTOAs(TimeSeries ts, TimeSeries template, double Tp) {
        final long startTime = System.nanoTime();
        boolean useBrent = false;

        int N = ts.t.length;
        double deltaT = ts.t[1] - ts.t[0];
        double deltaF = 1.0 / (N * deltaT); // Sampling frequency
        //double fNyq = 1.0 / (2.0 * deltaT); // Nyquist frequency

        // Magic number
        final double Tcorr = 4e-4;
        final int m = (int) Math.floor( Tcorr / deltaT ); // Number of indices less than a correlation peak

        RealDoubleFFT transform = new RealDoubleFFT(N);

        Complex1D Fy = new Complex1D();
        transform.ft(ts.h, Fy);

        Complex1D Fp = new Complex1D();
        transform.ft(template.h, Fp);

        // C = N * deltaF * np.fft.ifft(ytilde * np.conj(ptilde))
        Complex1D temp = new Complex1D();
        temp.x = new double[ Fy.x.length ];
        temp.y = new double[ Fy.y.length ];
        for (int i = 0; i < temp.x.length; i++) {
            double aR = Fy.x[i];
            double aI = Fy.y[i];
            double bR = Fp.x[i];
            double bI = (-1.0)*Fp.y[i]; // conjugation

            temp.x[i] = (aR*bR - aI*bI);
            temp.y[i] = (aR*bI + aI*bR);
        }
        double[] C = new double[N];
        transform.bt(temp, C);

        // Compute the normalization
        // norm=np.real(1/sum(deltaF*ptilde*np.conj(ptilde)))
        double psum = 0.0;
        for (int i = 0; i < Fp.x.length; i++) {
            psum += Fp.x[i]*Fp.x[i] + Fp.y[i]*Fp.y[i];
        }
        double norm = 1.0 / (deltaF * psum);

        // correlate data (maxima give TOAs)
        // normalization implies that values of C at maxima are estimates of pulse amplitudes
        // C=np.real(norm*C)
        for (int i = 0; i < C.length; i++) {
            //C[i] /= Math.pow(transform.norm_factor, 1.5);
            C[i] *= norm;
        }

        Log.d(TAG, "Correlation array (BEFORE) length = " + C.length);

        // find location of max correlation for reference pulse TOA
        int ind0 = Utility.argmax(C);
        double C0 = C[ind0];

        // Values for the reference pulse
        int indices[] = getIndicies(ind0, m, ts.t.length);
        double t0;
        if (useBrent) {
            t0 = estimatePeak(ind0, indices, ts, template, norm);
        } else {
            t0 = ts.t[ind0];
        }
        //double A0 = correlate(t0, ts, template, norm);

        // Calculate expected number of pulses
        // Number of pulses before the reference pulse
        int N1 = (int) Math.floor(t0/Tp);
        // Number of pulses after the reference pulse
        int N2 = (int) Math.floor((ts.t[ts.t.length-1]-t0)/Tp);
        // Total number of pulses
        int Np = N1 + N2 + 1;
        // Reference pulse number
        int n0 = N1 + 1;

        // Calculate expected indices of TOAs
        double[] expected_double = Utility.linspace((Tp/deltaT)*(1-n0), (Tp/deltaT)*(Np-n0), Np);
        int[] expected_ind = new int[expected_double.length];
        for (int i = 0; i < expected_ind.length; i++) {
            expected_ind[i] = ind0 + (int) Math.round( expected_double[i] );
        }

        // Initialize variables
        double[] tauhat = Utility.zeros(Np);
        double[] Ahat = Utility.zeros(Np);

        // loop to find measured TOAs and amplitudes
        for(int i = 0; i < expected_ind.length; i++) {
            int expected = expected_ind[i];

            // search in a small region around expected arrival time
            indices = getIndicies(expected, m, ts.t.length);

            double[] subC = new double[indices.length];
            for (int j = 0; j < subC.length; j++) {
                subC[j] = C[indices[j]];
            }
            int idx = indices[0] + Utility.argmax(subC);

            if (useBrent) {
                tauhat[i] = estimatePeak(idx, indices, ts, template, norm);
            } else {
                tauhat[i] = ts.t[idx];
            }
            // Original: Ahat[i] = correlate(tauhat[i], ts, template, norm);
            Ahat[i] = C[idx];
            Log.d(TAG, "Found peak " + (i+1) + " of " + Np + ": tau = " + tauhat[i]);
        }

        // error estimate for TOAs (based on correlation curve)
        // basically, we can determine the max of the correlation to +/- 0.5 deltaT;
        // multiply by 1/sqrt(Ahat(ii)) to increase error bar for small correlations
        //double Ahat_max = Utility.maxabs(Ahat); // The 'abs' shouldn't matter
        double[] error_tauhat = Utility.zeros(tauhat.length);
        for (int i = 0; i < tauhat.length; i++) {
            error_tauhat[i] = 0.5 * deltaT / Math.sqrt(Ahat[i]);
        }

        // MARC: Save time and compute the expected time of arrivals here
        double[] expectedTOA = new double[Np];
        for (int i = 0; i < expectedTOA.length; i++) {
            expectedTOA[i] = ts.t[expected_ind[i]];
        }

        Log.d(TAG, "Correlation array (AFTER) length = " + C.length);

        final long endTime = System.nanoTime();
        double elapsedTimeSeconds = (endTime - startTime);
        for (int i = 0; i < 9; i++) {
            elapsedTimeSeconds /= 10.0;
        }

        return new CalMeasuredTOAsResult(tauhat, error_tauhat, n0, expectedTOA, C, ind0, elapsedTimeSeconds);
    }


    private static int[] getIndicies(int centerIndex, int halfIndexWidth, int maxIndex) {
        // Fixme
        // maxIndex should be the length
        // search in a small region around expected arrival time
        int indices[] = null;
        if (centerIndex - halfIndexWidth < 0) {
            // Left of peak is before the time origin
            indices = Utility.range(0, centerIndex + halfIndexWidth + 1);
        } else if (centerIndex + halfIndexWidth > maxIndex) {
            // Right of the peak is after the time series
            indices = Utility.range(centerIndex - halfIndexWidth, maxIndex);
        } else {
            // Peak is fully within the time series (the nominal case)
            indices = Utility.range(centerIndex - halfIndexWidth, centerIndex + halfIndexWidth + 1);
        }

        return indices;
    }

    private static double estimatePeak(int initialIndex, int[] indices, TimeSeries ts, TimeSeries template, double norm) {
        // The objective function
        MyUnivariateFunction func = new MyUnivariateFunction(ts, template, -norm);

        // Setup the optimizer
        // https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/optim/univariate/BrentOptimizer.html
        final double rel = 1.0e-15;
        final double abs = 1.0e-15;
        BrentOptimizer brentOptimizer = new BrentOptimizer(rel, abs);

        // Create and run the optimization
        final int maxIterations = 10;
        final double lo = ts.t[indices[0]];
        final double init = ts.t[initialIndex]; //ts.t[centerIndex]; // initial
        final double hi = ts.t[indices[indices.length-1]];
        SearchInterval brack = new SearchInterval(lo,hi,init);
        UnivariatePointValuePair pair = brentOptimizer.optimize(new UnivariateObjectiveFunction(func),
                brack, GoalType.MINIMIZE, new MaxIter(maxIterations), new MaxEval(maxIterations));

        // Report the results
        Log.d("BRENT", "guess best tau = " + init);
        Log.d("BRENT", "lower bound = " + lo);
        Log.d("BRENT", "brent best tau = " + pair.getPoint());
        Log.d("BRENT", "upper bound = " + hi);

        double optimizedTime = pair.getPoint();

        return optimizedTime;
    }

    /*
        def calresiduals(measuredTOAs, expectedTOAs, uncertainties):

        '''
        calculated timing residuals and error bars from measured TOAs
        and expected TOAs (the errorbars are just the uncertainities from
        the measured TOAs)

        remove any nans in the measured TOAs
        '''

        # first remove any NaNs in the measured TOAs
        notNaN = ~np.isnan(measuredTOAs)
        measured = measuredTOAs[notNaN]
        expected = expectedTOAs[notNaN]

        # construct time-series of residuals
        N = len(measured)
        residuals = np.zeros([N, 2])
        residuals[:,0] = measured
        residuals[:,1] = measured - expected

        # construct time-series of errorbars from measured TOA uncertainties
        errorbars = np.zeros([N, 2])
        errorbars[:,0] = measured
        errorbars[:,1] = uncertainties[notNaN]

        return residuals, errorbars
     */

    static public void calresiduals(CalMeasuredTOAsResult d) {
        // not needed
    }





    /*
        def caltemplate(profile, ts):

        '''
        calculate template from pulse profile for time series ts
        '''

        # create template by extending pulse profile to have same length as data
        Nt = len(ts[:,0])
        Nprofile = len(profile[:,0])

        template = np.zeros([Nt,2])
        template[:,0] = ts[:,0]
        template[0:Nprofile,1] = profile[:,1]

        return template
     */
    public static TimeSeries caltemplate(PulseProfile pf, TimeSeries ts) {
        // We want to extend the pulse profile length
        // to match that of the time series being analyzed
        // so that the fourier transforms can be computed

        int Nt = ts.t.length;
        int NProfile = pf.ts.t.length;

        TimeSeries template = new TimeSeries(ts.t, Utility.zeros(Nt));

        // Copy the profile into the start of the new time series
        for (int i = 0; i < NProfile; i++) {
            template.h[i] = pf.ts.h[i];
        }

        return template;
    }






    /*
        def correlate(tau, x, y, norm):

        '''
        calculate correlation C(tau) for two time-series x, y,
        doing the calculation in the frequency domain.

        norm - normalization factor
        '''

        # extract relevant time-domain quantities
        deltaT = x[1,0]-x[0,0]
        N = len(x[:,0])
        deltaF = 1.0/(N*deltaT)

        # calculate discrete frequencies
        fNyq = 1.0/(2*deltaT)
        if ( np.mod(N,2)== 0 ):
            numFreqs = N/2 - 1
        else:
            numFreqs = (N-1)/2

        # discrete positive frequencies
        fp = np.linspace(deltaF, numFreqs*deltaF, numFreqs)

        # discrete frequencies (including zero and negative frequencies
        if ( np.mod(N,2)== 0 ):
            a = np.hstack( (np.array([0.]), fp) )
            b = np.hstack( (np.array([-fNyq]), np.flipud(-fp)) )
            f = np.hstack( (a, b) )
        else:
            f = np.hstack( (np.hstack(((np.array([0.]),fp))), np.flipud(-fp)) )

        # fourier transform time series
        xtilde = deltaT * np.fft.fft(x[:,1])
        ytilde = deltaT * np.fft.fft(y[:,1])

        # calculate correlation C(tau)

        phase= np.exp(np.sqrt(-1+0j)*2*np.pi*f*tau)
        C = np.sum(deltaF * phase * xtilde * np.conj(ytilde))
        C = np.real(C) # take real part to avoid imag component from round-off
        C = norm*C  # normalize

        return C
     */

    public static double correlate(double tau, TimeSeries x, TimeSeries y, double norm) {
        double deltaT = x.t[1] - x.t[0];
        int N = x.t.length;
        double deltaF = 1.0 / (N*deltaT);

        // Calculate discrete frequencies
        double fNyq = 1.0 / (2.0*deltaT);

        // Calculate number of positive frequencies (not including 0 or Nyquist)
        int numFreqs = 0;
        int nunique;
        if ( N%2 == 0) {
            numFreqs = N/2 - 1;
            nunique = (N-2)/2 + 2;
        } else {
            numFreqs = (N-1)/2;
            nunique = (N-1)/2 + 1;
        }

        // Discrete positive frequencies
        double[] fp = Utility.linspace(deltaF, numFreqs * deltaF, numFreqs);

        // Discrete frequencies (including zero and negative frequencies)
        double[] f;
        if ( N%2 == 0 ) {
            int num = 1 + 2*fp.length + 1;
            f = new double[num];
            f[0] = 0;
            for (int i = 0; i < fp.length; i++) {
                f[i+1] = fp[i];
            }
            f[fp.length] = -fNyq;
            for (int i = 0; i < fp.length; i++) {
                f[i+fp.length+2] = -1.0*fp[fp.length - i -1];
            }
        } else {
            int num = 2*fp.length + 1;
            f = new double[num];
            f[0] = 0.0;
            for (int i = 0; i < fp.length; i++) {
                f[i+1] = fp[i];
            }
            for (int i = 0; i < fp.length; i++) {
                f[i+1+fp.length] = fp[fp.length - 1 - i];
            }
        }

        if (f.length != numFreqs) {
            // Fixme
        }

        // Fourier transform the x time series
        RealDoubleFFT transformerX = new RealDoubleFFT(N);
        Complex1D Fx = new Complex1D();
        transformerX.ft(x.h, Fx);

        // Fourier transform the y time series
        RealDoubleFFT transformerY = new RealDoubleFFT(N);
        Complex1D Fy = new Complex1D();
        transformerY.ft(y.h, Fy);

        // Calculate the correlation
        double C = 0.0;
        double conjFactor = 1.0;

        // Almost half of the FFT values are found by the conjugate symmetry formula
        for (int n = 0; n < N; n++) {
            int i;
            if (n < nunique) {
                i = n;
                conjFactor = 1.0;
            } else {
                conjFactor = -1.0;
                if (N%2 == 0) {
                    i = nunique - (n - nunique) - 2;
                    // -2 since Nyquist is only once in the array
                } else {
                    i = nunique - (n - nunique) - 1;
                }
            }
            double aR = Math.cos(2.0*Math.PI*f[i]*tau);
            double aI = Math.sin(2.0*Math.PI*f[i]*tau);
            double bR = Fx.x[i];
            double bI = conjFactor * Fx.y[i];
            double cR = Fy.x[i];
            double cI = -1.0*conjFactor*Fy.y[i]; // conjugate

            C += (aR * bR - aI * bI) * cR - ( aR * bI + aI * bR ) * cI;
        }
        C *= deltaT * deltaT * deltaF * norm;

        return C;
    }







    /*
    def detrend(ts, errors):

    '''
    detrend a time-series by removing least squares linear fit

    b = best fit y-intercept
    m = best fit slope
    '''

    N = len(ts[:,0])

    # mapping matrix (from b,m to time-series values)
    M = np.zeros([N, 2])
    M[:,0] = np.ones(N)
    M[:,1] = ts[:,0]
    Mt = M.T

    # covariance matrix
    C = np.zeros([N, N])
    C = np.diag(errors[:,0]**2,0)
    Cinv = linalg.inv(C)

    # max-likelihood solution for y-intercept, slope
    aML =  np.dot(np.dot(np.dot(linalg.inv(np.dot(np.dot(Mt,Cinv),M)),Mt),Cinv),ts[:,1]);
    b = aML[0]
    m = aML[1]

    # best fit line
    fit = b + m*ts[:,0]

    # detrended time-series
    dts = np.zeros([N,2])
    dts[:,0] = ts[:,0];
    dts[:,1] = ts[:,1] - fit

    return dts, b, m
*/

    // Source: http://excerptsworld.blogspot.com/2011/06/how-to-remove-line-trend-in-java.html
    public static double[] detrend(double[] x, double[] y) {
        if (x.length != y.length)
            throw new IllegalArgumentException("The x and y data elements needs to be of the same length");

        SimpleRegression regression = new SimpleRegression();

        for (int i = 0; i < x.length; i++) {
            regression.addData(x[i], y[i]);
        }

        double slope = regression.getSlope();
        double intercept = regression.getIntercept();

        for (int i = 0; i < x.length; i++) {
            //y -= intercept + slope * x
            y[i] -= intercept + (x[i] * slope);
        }
        return y;
    }

    /*
    def errsinusoid(p, t, y, err):

    '''
    calculate function to minimize when fitting to a sinusoid plus constant

    p - parameters for model sinusoid (A, f, phi, b)
    t - discrete times
    y - array of measeured data
    err - array of error bars on measured data
    '''

    f = (y-sinusoid(p,t))/err

    return f
    */

    /*
    def sinusoid(p,t):

    '''
    calculate sine function plus constant offset in y

    p - parameters (A, f, phi, b)
    t - discrete times
    '''

    y = p[0]*np.sin(2*np.pi*p[1]*t+p[2]) + p[3]

    return y
    */


    public static double[] fitSinusoid(final double[] measuredTOAs, final double[] residuals) {

        // Initialize the starting parameters
        final int M = 4;
        double[] initialParameters = new double[M];
        initialParameters[0] = 2.0e-4; // amplitude
        initialParameters[1] = 0.6; // frequency eg. 0.4
        initialParameters[2] = 0.0; // phase
        initialParameters[3] = 0.0; // offset

        final int N = measuredTOAs.length;

        MultivariateJacobianFunction distances = new MultivariateJacobianFunction() {
            public Pair<RealVector, RealMatrix> value(final RealVector point) {

                // Parameters being checked
                double p0 = point.getEntry(0);
                double p1 = point.getEntry(1);
                double p2 = point.getEntry(2);
                double p3 = point.getEntry(3);


                RealVector value = new ArrayRealVector(N);
                RealMatrix jacobian = new Array2DRowRealMatrix(N, M);

                for (int i = 0; i < N; ++i) {
                    final double xi = measuredTOAs[i];

                    value.setEntry(i, p0*Math.sin(2.0*Math.PI*xi*p1 + p2) + p3);

                    // derivative with respect to p0
                    jacobian.setEntry(i, 0, Math.sin(2.0*Math.PI*p1*xi + p2));

                    // derivative with respect to p1
                    jacobian.setEntry(i, 1, p0*Math.cos(2.0*Math.PI*p1*xi + p2)*(2.0*Math.PI*xi));

                    // derivative with respect to p2
                    jacobian.setEntry(i, 2, p0*Math.cos(2.0*Math.PI*p1*xi + p2));

                    // derivative with respect to p3
                    jacobian.setEntry(i, 3, 1.0);
                }

                return new Pair<RealVector, RealMatrix>(value, jacobian);
            }
        };

        LeastSquaresProblem problem = new LeastSquaresBuilder().
                start(initialParameters).
                model(distances).
                target(residuals).
                lazyEvaluation(false).
                maxEvaluations(10000).
                maxIterations(10000).
                build();
        
        LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().
                withCostRelativeTolerance(2 * Precision.EPSILON).
                withParameterRelativeTolerance(2 * Precision.EPSILON).
                optimize(problem);

        // Get the fitted parameters
        double[] fittedParameters = new double[M];
        for (int i = 0; i < M; i++) {
            fittedParameters[i] = optimum.getPoint().getEntry(i);
        }

        // Print the results
        for (int i = 0; i < M; i++) {
            Log.d(TAG, "p" + i + " = " + fittedParameters[i]);
        }


        return fittedParameters;
    }

    public static void debug() {
        // These values were picked by Joe, and the math
        // solved on paper by Joe. This allows for the checking
        // of the foldtimeseries function.
        double[] t = {10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
        double[] x = {3, 1, 4, 1, 5, 9, 1, 6, 1, 8};
        double[] et = {0, 0.5, 1, 1.5, 2};
        double[] ex = {4.5, 5, 1, 3, 6};

        double T = 2.3;
        TimeSeries ts = new TimeSeries(t, x);
        TimeSeries fs = Routines.foldtimeseries(ts, T);
    }
}
