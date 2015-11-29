package edu.utrgv.cgwa.tabletoppta;

import ca.uol.aig.fftpack.Complex1D;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class Routines {
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

        # START DEBUG SECTION
        # plot correlation time series
        if DEBUG:
                plt.figure()
                plt.plot(ts[:,0], ts[:,1], '-b', ts[:,0], C, '-r')
                plt.xlabel('time (sec)')
                plt.legend(['time series', 'correlation'], 'best')
                # END DEBUG SECTION

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

        # START DEBUG SECTION
        if DEBUG:
                # plot data and correlation around max
        plt.figure()
                plt.plot(ts[indices,0], C[indices], '-*r', ts[indices,0], ts[indices,1], '-*b')
                plt.axhline(y = template[0,1], color='k') # starting value of pulse template
                TOAexp = t0 + (ii+1-n0)*Tp;
        plt.axvline(x = TOAexp, color='b')
                plt.axvline(x = tauhat[ii], color='r')
                plt.xlabel('time (sec)')
                plt.ylabel('correlation')
                plt.legend(['correlation', 'time series'], 'best')
        print tauhat[ii]-TOAexp
        #input('type any key to continue')
        # END DEBUG SECTION

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

    public static double[] calmeasuredTOAs(TimeSeries ts, TimeSeries template, double Tp) {
        int N = ts.t.length;
        double deltaT = ts.t[1] - ts.t[0];
        double deltaF = 1.0 / (N * deltaT); // Sampling frequency
        double fNyq = 1.0 / (2.0 * deltaT); // Nyquist frequency

        // Magic number
        double Tcorr = 4e-4;
        int m = (int) Math.floor( Tcorr / deltaT );

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

        // correlate data (maxima give TOAs)
        // normalization implies that values of C at maxima are estimates of pulse amplitudes
        for (int i = 0; i < C.length; i++) {
            C[i] /= Math.pow(transform.norm_factor, 1.5);
            //C[i] *= transform.norm_factor; // Not sure if this works
        }
        // norm=np.real(1/sum(deltaF*ptilde*np.conj(ptilde)))
        // C=np.real(norm*C)

        // find location of max correlation for reference pulse TOA
        int ind0 = Utility.argmax(C);
        double C0 = C[ind0];

        // use brent's method to find max
        int indices[] = null;
        if (ind0 - m < 0) {
            indices = Utility.range(0, ind0 + m + 1);
        } else if (ind0 + m > C.length) {
            indices = Utility.range(ind0 - m, C.length);
        } else{
            indices = Utility.range(ind0 - m, ind0 + m + 1);
        }

        return C;
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
            double aR = Math.cos(2*Math.PI*f[i]*tau);
            double aI = Math.sin(2*Math.PI*f[i]*tau);
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