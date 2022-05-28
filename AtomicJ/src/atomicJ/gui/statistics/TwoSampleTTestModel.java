
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

package atomicJ.gui.statistics;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.apache.commons.math.MathException;

import atomicJ.analysis.Batch;
import atomicJ.analysis.BatchUtilities;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.TwoSampleTTest;



import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class TwoSampleTTestModel <E extends Processed1DPack<E,?>> extends AbstractModel implements PropertyChangeListener
{
    private double alpha = 0.05; 
    private boolean twoTailed = true;
    private boolean variancesEqual = true;

    private final List<Batch<E>> availableData;
    private final ProcessedPackSampleModel<E> firstSample ;
    private final ProcessedPackSampleModel<E> secondSample;

    private boolean inputProvided;

    public  TwoSampleTTestModel(Collection<ProcessedPackFunction<? super E>> availableFunctions, List<Batch<E>> availableData)
    {
        this.availableData = availableData;

        this.firstSample = new ProcessedPackSampleModel<>(availableFunctions, "Sample 1");
        this.secondSample = new ProcessedPackSampleModel<>(availableFunctions, "Sample 2");
        firstSample.addPropertyChangeListener(this);
        secondSample.addPropertyChangeListener(this);
        checkIfInputProvided();
    }

    public List<Batch<E>> getAvailableData()
    {
        return availableData;
    }

    public boolean isTwoTailed()
    {
        return twoTailed;
    }   

    public void setTwoTailed(boolean twoTailedNew) 
    {
        boolean twoTailedOld = twoTailed;
        this.twoTailed = twoTailedNew;

        firePropertyChange(TWO_TAILED, twoTailedOld, twoTailedNew);
    }

    public boolean isVariancesEqual()
    {
        return variancesEqual;
    }

    public void setVariancesEqual(boolean variancesEqualNew) 
    {
        boolean variancesEqualOld = variancesEqual;
        this.variancesEqual = variancesEqualNew;

        firePropertyChange(VARIANCES_EQUAL, variancesEqualOld, variancesEqualNew);
    }

    public double getSignificanceLevel()
    {
        return alpha;
    }

    public void setSignificanceLevel(double alphaNew) 
    {
        double alphaOld = alpha;
        this.alpha = alphaNew;

        firePropertyChange(SIGNIFICANCE_LEVEL, alphaOld, alphaNew);

        checkIfInputProvided();
    }

    public ProcessedPackSampleModel<E> getFirstSampleModel()
    {
        return firstSample;
    }

    public ProcessedPackSampleModel<E> getSecondSampleModel()
    {
        return secondSample;
    }

    private void checkIfInputProvided()
    {
        boolean inputProvidedOld = inputProvided;
        inputProvided = !Double.isNaN(alpha)&&(firstSample.isAllInputProvided())&&(secondSample.isAllInputProvided());

        firePropertyChange(INPUT_PROVIDED, inputProvidedOld, inputProvided);
    }

    public boolean isInputProvided()
    {
        return inputProvided;
    }

    public TwoSampleTTest run() throws UserCommunicableException
    {
        if(!inputProvided)
        {
            throw new UserCommunicableException("T test cannot be run. Necessary input is not provided");
        }

        try 
        {               
            DescriptiveStatistics firstSampleStats = firstSample.getDescriptiveStatics();
            DescriptiveStatistics secondSampleStats = secondSample.getDescriptiveStatics();

            TwoSampleTTest tTest = new TwoSampleTTest(firstSampleStats, secondSampleStats, alpha, twoTailed, variancesEqual);

            return tTest;
        } 
        catch (MathException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured. T test cannot be performed");
        }
    }

    public File getDefaultOutputDirectory()
    {
        List<E> allPacks = new ArrayList<>();
        allPacks.addAll(firstSample.getProcessedPacks());
        allPacks.addAll(secondSample.getProcessedPacks());

        File defaultOutputDirectory = BatchUtilities.findLastCommonDirectory(allPacks);
        return defaultOutputDirectory;
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(SAMPLE_INPUT_PROVIDED.equals(name))
        {
            checkIfInputProvided();
        }
    }
}
