
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.statistics;


import gnu.trove.list.TDoubleList;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.FDistribution;
import org.apache.commons.math.distribution.FDistributionImpl;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jfree.data.Range;

import atomicJ.functions.AbstractFittedUnivariateFunction;
import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.MultipleSelector;
import atomicJ.utilities.Selector;
import atomicJ.utilities.Validation;


public class DescriptiveStatistics 
{
    private static final double TOLERANCE = 1e-12;

    private final String sampleName;

    private final int n;
    private final double mean;
    private final double mean05;
    private final double median;
    private final double lowerQuartile;
    private final double upperQuartile;
    private final double iql;
    private final double variance;
    private final double sum;
    private final double sumOfSquares;
    private final double standardDeviation;
    private final double standardError;
    private final double skewness;
    private final double kurtosis;


    public DescriptiveStatistics(double[] data, String sampleName)
    {
        double[] dataCleared = clearOfNaNs(data);

        this.sampleName = sampleName;

        // 1 calculate variance
        int n = dataCleared.length;
        double mean = Double.NaN;
        double sum = 0;
        double sum2 = 0;
        double sum3 = 0;
        double sum4 = 0;    

        double variance = Double.NaN;
        double skewness = Double.NaN;
        double kurtosis = Double.NaN;
        double sd = Double.NaN;
        double se = Double.NaN;

        if(n>0)
        {  

            for(int i = 0;i<n;i++)
            {
                double x = dataCleared[i];
                sum = sum + x;
            }

            mean = sum/n;

            for (double x : dataCleared) 
            {
                double d = x - mean;
                sum2 += d*d;
                sum3 += d*d*d;
                sum4 += d*d*d*d;
            }   
        }


        if(n>1)
        {
            variance = sum2/(n - 1);
            sd = Math.sqrt(variance);
            se = sd/Math.sqrt(n);

            if(n>2)
            {
                double standardizedAccum3 = sum3/(variance * sd);               

                skewness = (n / ((n - 1.) * (n - 2.))) * standardizedAccum3;

            }  
            if(n>3)
            {
                double coeffKurt1 = sum4/ (variance*variance);

                double coeffKurt2 = (n * (n + 1.)) / ((n - 1.) * (n - 2.) * (n - 3.));
                double termTwo = (3. * (n - 1.)*(n - 1.)) / ((n - 2.) * (n - 3.));

                // Calculate kurtosis
                kurtosis = (coeffKurt2 * coeffKurt1) - termTwo;
            }
        }

        this.n = n;
        this.mean = mean;

        this.variance = variance;
        this.sum = sum;
        this.sumOfSquares = sum2;
        this.standardDeviation = sd;
        this.standardError = se;

        QuantileSelector qSelector = new QuantileSelector(dataCleared, new double[] {0.05, 0.25, 0.5, 0.75, 0.95});
        double[] quantiles = qSelector.getQuantiles();
        this.lowerQuartile = quantiles[1];
        this.median = quantiles[2];
        this.upperQuartile = quantiles[3];
        this.iql = upperQuartile - lowerQuartile;

        int[] ranks = qSelector.getRanks();

        int trimmedFrom = Math.max(0, ranks[0]);
        int trimmedTo = Math.min(n, ranks[9] + 1);

        this.mean05 = arithmeticMean(qSelector.getSortedData(), trimmedFrom, trimmedTo);

        this.skewness = skewness;
        this.kurtosis = kurtosis;
    }

    public static double[] clearOfNaNs(double[] data)
    {        
        int n = data.length;

        double[] clear = new double[n];

        int clearCount = 0;

        for(int i = 0; i<n; i++)
        {
            double x = data[i];

            if(!Double.isNaN(x))
            {             
                clear[clearCount++] = x;
            }
        }

        double[] result = clearCount == n ? clear : Arrays.copyOf(clear, clearCount);    

        return result;
    }

    public String getSampleName()
    {
        return sampleName;
    }

    public int getSize()
    {
        return n;
    }

    public double getArithmeticMean()
    {
        return mean;
    }

    public double getTrimmedArithmeticMean()
    {
        return mean05;
    }

    public double getVariance()
    {
        return variance;
    }

    public double getSum()
    {
        return sum;
    }

    public double getSumOfSquares()
    {
        return sumOfSquares;
    }

    public double getStandardDeviation()
    {
        return standardDeviation;
    }

    public double getStandardError()
    {
        return standardError;
    }

    public double getSkewness()
    {
        return skewness;
    }

    public double getKurtosis()
    {
        return kurtosis;
    }

    public double getMedian()
    {
        return median;
    }

    public double getLowerQuartile()
    {
        return lowerQuartile;
    }

    public double getUpperQuartile()
    {
        return upperQuartile;
    }

    public double getInterquartileLength()
    {
        return iql;
    }

    public static double arithmeticMean(double[] data)
    {
        int n = data.length;
        double mean = Double.NaN;

        if(n>0)
        {
            double sum = 0;
            for(int i = 0;i<n;i++)
            {
                double x = data[i];
                sum = sum + x;
            }

            mean = sum/n;
        }

        return mean;
    }

    //from inclusive, to exclusive
    public static double arithmeticMean(double[] data, int from, int to)
    {       
        int n = to - from;
        double mean = Double.NaN;

        if(n>0)
        {
            double sum = 0;
            for(int i = from;i<to;i++)
            {
                double x = data[i];
                sum = sum + x;
            }

            mean = sum/n;
        }

        return mean;
    }

    public static double arithmeticMean(List<Double> data)
    {
        int n = data.size();
        double mean = Double.NaN;
        if(n>0)
        {
            double sum = 0;
            for(int i = 0;i<n;i++)
            {
                double x = data.get(i);
                sum = sum + x;
            }

            mean = sum/n;		
        }
        return mean;
    }

    public static double arithmeticMean(TDoubleList data)
    {
        int n = data.size();
        double mean = Double.NaN;
        if(n>0)
        {
            double sum = 0;
            for(int i = 0;i<n;i++)
            {
                double x = data.get(i);
                sum = sum + x;
            }

            mean = sum/n;       
        }
        return mean;
    }

    public static double[] trimSort(double[] data, double trimLeft, double trimRight)
    {
        int n = data.length;

        if(n>0)
        { 
            int kLeft = (int)Math.round(trimLeft * n);
            int kRight = (int)Math.round(trimRight * n);

            if(kLeft + kRight < n)
            {
                double[] dataCopy = Arrays.copyOf(data, n);		
                Arrays.sort(dataCopy);

                double[] dataRange = Arrays.copyOfRange(dataCopy, kLeft, n - kRight);

                return dataRange;
            }           
        }

        return new double[] {};
    }

    public static double[] trimQuickSelect(double[] data, double trimLeft, double trimRight)
    {
        int n = data.length;

        if(n>0)
        { 
            int kLeft = (int)Math.round(trimLeft * n);
            int kRight = (int)Math.round(trimRight * n);

            if(kLeft + kRight < n)
            {
                double[] dataCopy = Arrays.copyOf(data, n);
                MultipleSelector.sortSmallest(dataCopy, new int[] {kLeft, n - kRight});

                double[] dataRange = Arrays.copyOfRange(dataCopy, kLeft, n - kRight + 1);
                return dataRange;
            }
        }

        return new double[] {};
    }

    public static double[] trimQuickSelect(List<Double> data, double trimLeft, double trimRight)
    {
        int n = data.size();

        if(n>0)
        { 
            int kLeft = (int)Math.round(trimLeft * n);
            int kRight = (int)Math.round(trimRight * n);

            if(kLeft + kRight < n)
            {
                double[] dataCopy = ArrayUtilities.getDoubleArray(data);
                MultipleSelector.sortSmallest(dataCopy, new int[] {kLeft, n - kRight});

                double[] dataRange = Arrays.copyOfRange(dataCopy, kLeft, n - kRight + 1);
                return dataRange;
            }           
        }
        return new double[] {};
    }

    public static double trimmedMeanSort(double[] data, double trimLeft, double trimRight)
    {
        return arithmeticMean(trimSort(data, trimLeft, trimRight));
    }

    public static double trimmedMean(double[] data, double trimLeft, double trimRight)
    {
        int n = data.length;

        double arithmeticMean = Double.NaN;

        if(n>0)
        { 
            int kLeft = (int)Math.round(trimLeft * n);
            int kRight = (int)Math.round(trimRight * n);

            if(kLeft + kRight < n)
            {
                double[] dataCopy = Arrays.copyOf(data, n);
                MultipleSelector.sortSmallest(dataCopy, new int[] {kLeft, n - kRight});

                arithmeticMean = arithmeticMean(dataCopy, kLeft, n - kRight + 1);
            }
        }

        return arithmeticMean;
    }

    public static double trimmedMeanQuickSelect(List<Double> data, double trimLeft, double trimRight)
    {
        int n = data.size();
        double arithmeticMean = Double.NaN;

        if(n>0)
        { 
            int kLeft = (int)Math.round(trimLeft * n);
            int kRight = (int)Math.round(trimRight * n);

            if(kLeft + kRight < n)
            {
                double[] dataCopy = ArrayUtilities.getDoubleArray(data);
                MultipleSelector.sortSmallest(dataCopy, new int[] {kLeft, n - kRight});
                arithmeticMean = arithmeticMean(dataCopy, kLeft, n - kRight + 1);
            }           
        }

        return arithmeticMean;
    }

    public static double trimmedMeanQuickSelect(TDoubleList data, double trimLeft, double trimRight)
    {
        int n = data.size();
        double arithmeticMean = Double.NaN;

        if(n>0)
        { 
            int kLeft = (int)Math.round(trimLeft * n);
            int kRight = (int)Math.round(trimRight * n);

            if(kLeft + kRight < n)
            {
                double[] dataCopy = data.toArray();
                MultipleSelector.sortSmallest(dataCopy, new int[] {kLeft, n - kRight});
                arithmeticMean = arithmeticMean(dataCopy, kLeft, n - kRight + 1);
            }           
        }

        return arithmeticMean;
    }

    public static double[] fixZero(double[] data)
    {
        int n = data.length;
        double[] dataNew = new double[n];

        double minimum = ArrayUtilities.getNumericMinimum(data);

        if (Math.abs(minimum) > TOLERANCE) 
        {
            for(int i = 0; i<n; i++)
            {
                dataNew[i] = data[i] - minimum;
            }
        }
        return dataNew;
    }

    public static double[] applyFunction(double[] data, UnivariateFunction f)
    {
        int n = data.length;
        double[] dataNew = new double[n];


        for(int i = 0; i<n; i++)
        {
            double oldVal = data[i];
            dataNew[i] = f.value(oldVal);
        }

        return dataNew;
    }

    //From Knuth D. "Art of Computer Programming", vol. 2. "Seminumerical Algorithms", p. 232.
    public static double sumOfSquares(double[] data)
    {
        int n = 0;
        double mean = 0;
        double squares = 0;

        for(double x: data)
        {
            n = n + 1;
            double delta = x - mean;
            mean = mean + delta/n;
            squares = squares + delta*(x - mean);
        }

        return squares;
    }


    //From Knuth D. "Art of Computer Programming", vol. 2. "Seminumerical Algorithms", p. 232.
    public static double variancePopulation(double[] data)
    {
        int n = data.length;       
        double variance = Double.NaN;

        if(n>0)
        {
            double squares = sumOfSquares(data);
            variance = squares/n;
        }

        return variance;
    }

    //From Knuth D. "Art of Computer Programming", vol. 2. "Seminumerical Algorithms", p. 232.
    public static double varianceSample(double[] data)
    {
        if(data.length<2)
        {
            return Double.NaN;
        }

        int n = 0;
        double mean = 0;
        double squares = 0;

        for(double x: data)
        {
            n = n + 1;
            double delta = x - mean;
            mean = mean + delta/n;
            squares = squares + delta*(x - mean);
        }

        double variance = Double.NaN;
        if(n>1)
        {
            variance = squares/(n - 1);
        }

        return variance;
    }

    public static double standardDeviationSample(double[] data)
    {
        double var = varianceSample(data);
        double sd = Math.sqrt(var);
        return sd;
    }

    public static double standardDeviationPopulation(double[] data)
    {
        double var = variancePopulation(data);
        double sd = Math.sqrt(var);
        return sd;
    }

    public static double standardErrorSample(double[] data)
    {
        double sd = standardDeviationSample(data);
        double sqrt = Math.sqrt(data.length);
        double se = sd/sqrt;

        return se;
    }

    public static double standardErrorPopulation(double[] data)
    {
        double sd = standardDeviationPopulation(data);
        double sqrt = Math.sqrt(data.length);
        double se = sd/sqrt;

        return se;
    }

    //Uses Kahan's method of compensated summation to reduce numerical error
    public static double meanCompensatedSummation(double[] data)
    {
        double sum = 0;
        double c = 0;   
        int n = data.length;
        double mean = Double.NaN;
        if(n>0)
        {
            for(int i = 0; i<n; i++)
            {
                double y = data[i] - c;    
                double t = sum + y;         
                c = (t - sum) - y;  
                sum = t ;          
            }

            mean = sum/n;
        }

        return mean;

    }

    public static double lowerMedian(double[] data)
    {
        int n = data.length;
        double median = Double.NaN;

        if(n>0)
        {
            double[] dataCopy = Arrays.copyOf(data, n);

            double kM = n*0.5 + 0.5;
            int kMfloor = (int)Math.floor(kM) - 1;

            Selector.sortSmallest(dataCopy, kMfloor);
            median = dataCopy[kMfloor];
        }
        return median;
    }

    public static double upperMedian(double[] data)
    {
        int n = data.length;
        double median = Double.NaN;
        if(n>0)
        {
            double[] dataCopy = Arrays.copyOf(data, n);

            double kM = n*0.5 + 0.5;
            int kMceil = (int)Math.ceil(kM) - 1;

            Selector.sortSmallest(dataCopy, kMceil);
            median = dataCopy[kMceil];
        }
        return median;
    }

    public static double median(double[] data)
    {        
        int n = data.length;
        double median = Double.NaN;

        if(n>0)
        {
            double[] dataCopy = Arrays.copyOf(data, n);

            double kM = n*0.5 + 0.5;

            int kMfloor = (int)Math.floor(kM) - 1;
            int kMceil = (int)Math.ceil(kM) - 1;

            MultipleSelector.sortSmallest(dataCopy, new int[] {kMfloor, kMceil});

            double m1 = dataCopy[kMfloor];
            double m2 = dataCopy[kMceil];

            median = (m1 + m2)/2.;

        }     

        return median;		
    }

    public static double median(List<Double> data)
    {
        int n = data.size();
        double median = Double.NaN;

        if(n>0)
        {
            double[] dataCopy = ArrayUtilities.getDoubleArray(data);

            double kM = n*0.5 + 0.5;

            int kMfloor = (int)Math.floor(kM) - 1;
            int kMceil = (int)Math.ceil(kM) - 1;

            MultipleSelector.sortSmallest(dataCopy, new int[] {kMfloor, kMceil});

            double m1 = dataCopy[kMfloor];
            double m2 = dataCopy[kMceil];

            median = (m1 + m2)/2.;
        }
        return median;      
    }

    public static double median(TDoubleList data)
    {
        int n = data.size();
        double median = Double.NaN;

        if(n>0)
        {
            double[] dataCopy = data.toArray();

            double kM = n*0.5 + 0.5;

            int kMfloor = (int)Math.floor(kM) - 1;
            int kMceil = (int)Math.ceil(kM) - 1;

            MultipleSelector.sortSmallest(dataCopy, new int[] {kMfloor, kMceil});

            double m1 = dataCopy[kMfloor];
            double m2 = dataCopy[kMceil];

            median = (m1 + m2)/2.;
        }
        return median;      
    }

    public static double quantile(double[] data, double p)
    {
        int n = data.length;

        if(n==0)
        {
            return Double.NaN;
        }

        if(Math.floor(n*p + 0.5)<= 0 || Math.ceil(n*p + 0.5)>n)
        {
            return Double.NaN;
        }

        if(0<=p && p<1)
        {
            double quantile;
            double[] dataCopy = Arrays.copyOf(data, n);

            double k = n*p + 0.5;
            int k1 = (int)Math.ceil(k);
            int k2 = (int)Math.floor(k);


            Arrays.sort(dataCopy);

            quantile = 0.5*(dataCopy[k1 - 1] + dataCopy[k2 - 1]);

            return quantile;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public static double interquartileLength(double[] data)
    {
        return interquantileLength(data, 0.25, 0.75);	
    }

    public static double interquantileLength(double[] data, double p1, double p2)
    {
        int n = data.length;

        if(n==0)
        {
            return Double.NaN;
        }

        if(Math.floor(n*p1 + 0.5)<= 0 || Math.ceil(n*p1 + 0.5)>n)
        {
            return Double.NaN;
        }

        if(Math.floor(n*p2 + 0.5)<= 0 || Math.ceil(n*p2 + 0.5)>n)
        {
            return Double.NaN;
        }
        if(0<=p1 && p1<1 && 0<=p1 && p2<1)
        {
            double[] dataCopy = Arrays.copyOf(data, n);

            double k1 = n*p1 + 0.5;

            int k1floor = (int)Math.floor(k1) - 1;
            int k1ceil = (int)Math.ceil(k1) - 1;        

            double k2 = n*p2 + 0.5;

            int k2floor = (int)Math.floor(k2) - 1;
            int k2ceil = (int)Math.ceil(k2) - 1;

            MultipleSelector.sortSmallest(dataCopy, new int[] {k1floor, k1ceil, k2floor, k2ceil});

            double q1Lower = dataCopy[k1floor];
            double q1Upper = dataCopy[k1ceil];

            double q2Lower = dataCopy[k2floor];
            double q2Upper = dataCopy[k2ceil];

            double q1 = 0.5*(q1Upper + q1Lower);
            double q2 = 0.5*(q2Upper + q2Lower);

            double iql = q2 - q1;

            return iql;           
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }



    private static double[] absoluteDeviationsFrom(double[] data, double center)
    {

        int n = data.length;
        double[] absoluteDeviations = new double[n];

        for(int i = 0; i<n; i++)
        {
            double x = data[i];
            absoluteDeviations[i] = Math.abs(x - center);
        }

        return absoluteDeviations;
    }

    public static double[] absoluteDeviationsFromMedian(double[] data)
    {
        double median = median(data);
        return absoluteDeviationsFrom(data, median);
    }

    public static double meanAbsoluteDeviationFromMedian(double[] data)
    {
        double[] absoluteDeviations = absoluteDeviationsFromMedian(data);

        double mad = arithmeticMean(absoluteDeviations);

        return mad;
    }

    public static double medianAbsoluteDeviation(double[] data)
    {
        double[] absoluteDeviations = absoluteDeviationsFromMedian(data);

        double mad = median(absoluteDeviations);

        return mad;
    }

    public static Range getMADBasedRange(double[] data, double widthFactor)
    {
        double median = median(data);
        double[] absoluteDeviations = absoluteDeviationsFrom(data, median);

        double mad = median(absoluteDeviations);

        double min = Math.max(median - widthFactor*mad, ArrayUtilities.getMinimum(data));
        double max = Math.min(median + widthFactor*mad, ArrayUtilities.getMaximum(data));

        Range range = new Range(min, max);

        return range;
    }

    public static double getKurtosis(double[] sample) 
    {
        int n = sample.length;
        double kurt = Double.NaN;

        if (n > 3) 
        {		     
            // Compute the mean and standard deviation
            double mean = arithmeticMean(sample);
            double stdDev = standardDeviationSample(sample);

            // Sum the ^4 of the distance from the mean divided by the
            // standard deviation
            double accum3 = 0.0;
            for (int i = 0; i < n; i++) {
                accum3 += MathUtilities.intPow(sample[i] - mean, 4);
            }
            accum3 = accum3/ MathUtilities.intPow(stdDev, 4);

            // Get N
            double n0 = n;

            double coefficientOne =
                    (n0 * (n0 + 1)) / ((n0 - 1) * (n0 - 2) * (n0 - 3));
            double termTwo =
                    (3 * Math.pow(n0 - 1, 2.0)) / ((n0 - 2) * (n0 - 3));

            // Calculate kurtosis
            kurt = (coefficientOne * accum3) - termTwo;
        }
        return kurt;
    }

    public static double getSkewness(double[] sample)
    {
        double n = sample.length;
        double skewness = Double.NaN;

        if (n > 2 )
        {
            double mean = arithmeticMean(sample);
            // Get the mean and the standard deviation

            // Calc the std, this is implemented here instead
            // of using the standardDeviation method eliminate
            // a duplicate pass to get the mean

            double variance = varianceSample(sample);

            double accum3 = 0.0;
            for (int i = 0; i < n; i++) 
            {
                final double d = sample[i] - mean;
                accum3 += d * d * d;
            }
            accum3 = accum3/(variance * Math.sqrt(variance));		     

            // Calculate skewness
            skewness = (n / ((n - 1) * (n - 2))) * accum3;
        }
        return skewness;
    }



    public static double getFreedmanDiaconisBinWidth(double[] data)
    {        
        data = clearOfNaNs(data);

        int n = data.length;
        double iqr = DescriptiveStatistics.interquartileLength(data);
        double cubic = Math.cbrt(n);

        double binWidthNew = 2*iqr/cubic;

        return binWidthNew;
    }

    private static double homoscedasticT(double m1, double m2, double v1, double v2, double n1, double n2)  
    {
        double pooledVariance = ((n1  - 1) * v1 + (n2 -1) * v2 ) / (n1 + n2 - 2);
        double standardError = Math.sqrt(pooledVariance * (1/ n1 + 1/ n2)); 
        double t = (m1 - m2) / standardError;
        return t;
    }

    private static double heteroscedasticT(double m1, double m2, double v1, double v2, double n1, double n2)  
    {
        return (m1 - m2) / Math.sqrt((v1 / n1) + (v2 / n2));
    }

    public static double homoscedasticTtest(double m1, double m2, double v1, double v2, double n1, final double n2) throws MathException 
    { 
        double t = Math.abs(homoscedasticT(m1, m2, v1, v2, n1, n2));
        double df = n1 + n2 - 2;
        TDistribution distribution = new TDistributionImpl(df);
        return 2.0 * distribution.cumulativeProbability(-t);
    }

    public static double heteroscedasticTtest(double m1, double m2, double v1, double v2, double n1, final double n2) throws MathException 
    { 
        double t = Math.abs(heteroscedasticT(m1, m2, v1, v2, n1, n2));

        double df = 0;

        df = welchDf(v1, v2, n1, n2);
        TDistribution distribution = new TDistributionImpl(df);
        return 2.0 * distribution.cumulativeProbability(-t);
    }

    private static double homoscedasticTtest(DescriptiveStatistics stat1, DescriptiveStatistics stat2, boolean twoSided) throws MathException 
    { 
        double m1 = stat1.getArithmeticMean();
        double m2 = stat2.getArithmeticMean();
        double v1 = stat1.getVariance();
        double v2 = stat2.getVariance();
        double n1 = stat1.getSize();
        double n2 = stat2.getSize();

        double t = Math.abs(homoscedasticT(m1, m2, v1, v2, n1, n2));
        double df = n1 + n2 - 2;

        TDistribution distribution = new TDistributionImpl(df);
        double factor = twoSided ? 2.0 : 1.0;		
        return factor * distribution.cumulativeProbability(-t);
    }


    private static void t(DescriptiveStatistics stat1, DescriptiveStatistics stat2, double significanceLevel, boolean twoSided) throws MathException 
    { 
        double m1 = stat1.getArithmeticMean();
        double m2 = stat2.getArithmeticMean();
        double v1 = stat1.getVariance();
        double v2 = stat2.getVariance();
        double n1 = stat1.getSize();
        double n2 = stat2.getSize();

        // calculates standard error for the difference between means
        double pooledVariance = ((n1  - 1) * v1 + (n2 -1) * v2 ) / (n1 + n2 - 2);
        double standardError = Math.sqrt(pooledVariance * (1/ n1 + 1/ n2)); 
        double t = (m1 - m2) / standardError;

        // finds the right t distribution
        double df = n1 + n2 - 2;

        TDistribution distribution = new TDistributionImpl(df);

        //calculates confidence interval for the difference between mean

        double tMin = distribution.inverseCumulativeProbability(significanceLevel);
        double confidenceMin = (m1 - m2) + tMin*standardError;
        double confidenceMax = (m1 - m2) - tMin*standardError;

        //calculates type I error probability (i.e. p value)

        double factor = twoSided ? 2.0 : 1.0;		
        double p = factor * distribution.cumulativeProbability(-t);
    }

    private static double heteroscedasticTtest(DescriptiveStatistics stat1, DescriptiveStatistics stat2, boolean twoSided) throws MathException 
    { 
        double m1 = stat1.getArithmeticMean();
        double m2 = stat2.getArithmeticMean();
        double v1 = stat1.getVariance();
        double v2 = stat2.getVariance();
        double n1 = stat1.getSize();
        double n2 = stat2.getSize();

        double t = Math.abs(heteroscedasticT(m1, m2, v1, v2, n1, n2));

        double df = 0;

        df = welchDf(v1, v2, n1, n2);
        TDistribution distribution = new TDistributionImpl(df);
        double factor = twoSided ? 2.0 : 1.0;
        return factor * distribution.cumulativeProbability(-t);
    }

    private static double welchDf(double v1, double v2, double n1, double n2) 
    {
        return (((v1 / n1) + (v2 / n2)) * ((v1 / n1) + (v2 / n2))) /
                ((v1 * v1) / (n1 * n1 * (n1 - 1d)) + (v2 * v2) /
                        (n2 * n2 * (n2 - 1d)));
    }

    public static double Ttest(DescriptiveStatistics stat1, DescriptiveStatistics stat2, boolean twoSided, boolean variancesEqual) throws MathException
    {
        double p = variancesEqual ? homoscedasticTtest(stat1, stat2, twoSided) : heteroscedasticTtest(stat1, stat2, twoSided);
        return p;
    }

    private double oneWayAnova(List<DescriptiveStatistics> groups) throws UserCommunicableException, MathException
    {
        Validation.requireNonNullParameterName(groups, "groups");

        // check if we have enough categories
        if (groups.size() < 2) 
        {
            throw new UserCommunicableException("Two or more groups are necessary to run ANOVA");
        }

        // check if each category has enough data and all is double[]
        for (DescriptiveStatistics stats : groups) 
        {
            if (stats.getSize() <= 1) 
            {
                throw new UserCommunicableException("Two or more observations in each group are necessary to run ANOVA");

            }
        }

        int p = groups.size();

        double betweenGroupSSE = 0;
        double residualSSE= 0;
        int totalCount = 0;
        double grandMean = 0;
        double grandSum = 0;


        //calculates the total number of observations in all groups and the grand mean
        for (DescriptiveStatistics stats : groups) 
        {
            int count = stats.getSize();
            totalCount += count;

            double sum = stats.getSum();
            grandSum += sum;
        }

        grandMean = grandSum/totalCount;

        //calculates between group and residual sums of squares

        for (DescriptiveStatistics stats : groups) 
        {
            int count = stats.getSize();
            double groupMean = stats.getArithmeticMean();        	
            double d = (groupMean - grandMean);
            betweenGroupSSE += count*d*d;

            residualSSE += stats.getSumOfSquares();
        }

        //degrees of freedom
        int betweenGroupDF = p - 1;
        int residualDF = totalCount - p;

        double betweenGroupMSE = betweenGroupSSE/betweenGroupDF;
        double residualMSE = residualSSE/residualDF;
        double F = betweenGroupMSE/residualMSE;

        FDistribution distribution = new FDistributionImpl(betweenGroupDF, residualDF);

        return distribution.cumulativeProbability(F);
    }

    public static double getCoefficientOfDetermination(double[][] dataPoints, UnivariateFunction fittedFunction) 
    {
        double sumOfSquaredResiduals = AbstractFittedUnivariateFunction.squaresSum(dataPoints, fittedFunction);
        int n = dataPoints.length;

        double[] ys = new double[n];
        for(int i = 0; i<n;i++)
        {
            ys[i] = dataPoints[i][1];
        }

        double totalSumOfSquares = sumOfSquares(ys);

        double rSquared = 1 - sumOfSquaredResiduals/totalSumOfSquares;

        return rSquared;
    }

    public static double getCoefficientOfDetermination(double[] ys, double[] xs, UnivariateFunction fittedFunction) 
    {
        double sumOfSquaredResiduals = AbstractFittedUnivariateFunction.squaresSum(ys, xs, fittedFunction);      
        double totalSumOfSquares = sumOfSquares(ys);

        double rSquared = 1 - sumOfSquaredResiduals/totalSumOfSquares;

        return rSquared;
    }
}
