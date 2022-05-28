
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
import java.util.Collection;
import java.util.List;

import atomicJ.analysis.Batch;
import atomicJ.analysis.BatchUtilities;
import atomicJ.analysis.Processed1DPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.OneSampleTTest;




import static atomicJ.gui.statistics.InferenceModelProperties.*;

public class OneSampleTTestModel <E extends Processed1DPack<E,?>> extends AbstractModel implements PropertyChangeListener
{
    private double nullHypothesisMean = Double.NaN;

    private double alpha = 0.05; 
    private boolean twoTailed = true;

    private final List<Batch<E>> availableData;
    private final ProcessedPackSampleModel<E> sample;

    private boolean inputProvided;

    public OneSampleTTestModel(Collection<ProcessedPackFunction<? super E>> availableFunctions, List<Batch<E>> availableData)
    {
        this.availableData = availableData;

        this.sample = new ProcessedPackSampleModel<>(availableFunctions, "Sample");
        sample.addPropertyChangeListener(this);

        checkIfInputProvided();
    }

    public double getNullHypothesisMean()
    {
        return nullHypothesisMean;
    }

    public void setNullHypothesisMean(double newMean)
    {
        double oldMean = nullHypothesisMean;
        this.nullHypothesisMean = newMean;

        firePropertyChange(NULL_HYPOTHEISIS_MEAN, oldMean, newMean);

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
        return sample;
    }

    private void checkIfInputProvided()
    {
        boolean inputProvidedOld = inputProvided;
        inputProvided = !Double.isNaN(nullHypothesisMean)&&!Double.isNaN(alpha)&&(sample.isAllInputProvided());

        firePropertyChange(INPUT_PROVIDED, inputProvidedOld, inputProvided);
    }

    public boolean isInputProvided()
    {
        return inputProvided;
    }

    public OneSampleTTest run() throws UserCommunicableException
    {
        if(!inputProvided)
        {
            throw new UserCommunicableException("T test cannot be run. Necessary input is not provided");
        }
        else
        {
            try 
            {
                DescriptiveStatistics stats = sample.getDescriptiveStatics();

                OneSampleTTest test = new OneSampleTTest(stats, nullHypothesisMean, alpha, twoTailed);

                return test;
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
                throw new UserCommunicableException("Error occured. T test cannot be performed");
            }
        }
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

    public File getDefaultOutputDirectory()
    {       
        File defaultOutputDirectory = BatchUtilities.findLastCommonDirectory(sample.getProcessedPacks());
        return defaultOutputDirectory;
    }


}
