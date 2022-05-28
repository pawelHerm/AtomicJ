
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

package atomicJ.gui.histogram;

import atomicJ.data.QuantitativeSample;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.DistributionFit;
import atomicJ.statistics.FitType;

public class HistogramAtomicModel
{
    private final String name;
    private final DistributionFit fit;
    private final QuantitativeSample sample;

    private final boolean fitted;
    private final int dataCount;
    private final double fitLocation;
    private final double fitScale;
    private final double dataMean;
    private final double dataSD;

    public HistogramAtomicModel(DistributionFit fit, String histogramName, QuantitativeSample sample)
    {
        this.name = histogramName;
        this.fit = fit;
        this.sample = sample;


        double[] data = DescriptiveStatistics.clearOfNaNs(sample.getMagnitudes());
        if(fit == null)
        {
            this.fitted = false;
            this.fitLocation = Double.NaN;
            this.fitScale = Double.NaN;
        }
        else
        {
            this.fitted = true;
            this.fitLocation = fit.getLocation();
            this.fitScale = fit.getScale();
        }	


        this.dataCount = data.length;
        this.dataMean = DescriptiveStatistics.arithmeticMean(data);
        this.dataSD = DescriptiveStatistics.standardDeviationSample(data);
    }

    public String getHistogramName()
    {
        return name;
    }

    public FitType getFitType()
    {
        if(fit == null)
        {
            return null;
        }
        return fit.getFitType();
    }

    public boolean isFitted()
    {
        return fitted;
    }

    public double getFitLocation()
    {
        return fitLocation;
    }

    public double getFitScale()
    {
        return fitScale;
    }

    public int getDataCount()
    {
        return dataCount;
    }

    public double getDataMean()
    {
        return dataMean;
    }

    public double getDataSD()
    {
        return dataSD;
    }

    public QuantitativeSample getSample()
    {
        return sample;
    }

    @Override
    public String toString()
    {
        return name;
    }
}