
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

import static atomicJ.gui.histogram.HistogramModelProperties.*;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.WizardModelProperties;
import atomicJ.statistics.BinningMethod;
import atomicJ.statistics.DistributionType;
import atomicJ.statistics.FitType;
import atomicJ.statistics.HistogramType;


public class HistogramInstantUpdateBinningModel extends AbstractModel implements  HistogramBinningModel,PropertyChangeListener
{
    private final HistogramDestination destination;

    private int currentValueModelIndex;
    private boolean finishEnabled;
    private String task;
    private final HistogramSampleModel sampleModel;
    private final ChannelChart<HistogramPlot> chart;
    private final HistogramPlot plot;

    public HistogramInstantUpdateBinningModel(HistogramDestination destination, HistogramSampleModel sampleModel, ChannelChart<HistogramPlot> chart)
    {
        this.destination = destination;
        this.sampleModel = sampleModel;
        this.chart = chart;
        this.plot = chart.getCustomizablePlot();

        sampleModel.addPropertyChangeListener(this);

        checkIfFinishEnabled();
        checkIfTaskNameChanged();
    }

    @Override
    public String getUnit()
    {
        return sampleModel.getDomainQuantity().getFullUnitName();
    }

    @Override
    public boolean containsNonpositiveValues()
    {
        return sampleModel.containsNonpositive();
    }

    @Override
    public HistogramDestination getHistogramDestination()
    {
        return destination;
    }

    @Override
    public Window getPublicationSite()
    {
        return destination.getHistogramPublicationSite();
    }

    @Override
    public String getName()
    {
        return sampleModel.getName();
    }

    @Override
    public void setName(String newName)
    {
        sampleModel.specifyName(newName);
    }

    @Override
    public boolean isEntitled()
    {
        return sampleModel.isEntitled();
    }

    @Override
    public void setEntitled(boolean entitledNew)
    {
        sampleModel.specifyEntitled(entitledNew);
    }

    @Override
    public int getAllDataCount()
    {
        return sampleModel.getAllDataCount();
    }

    @Override
    public int getTrimmedDataCount()
    {
        return sampleModel.getDataCount();
    }

    @Override
    public int getDiscardedDataCount()
    {
        return sampleModel.getDiscardedDataCount();
    }

    @Override
    public double getBinCount()
    {
        return sampleModel.getBinCount();
    }

    @Override
    public void setBinCount(Number count)
    {
        sampleModel.setBinCount(count);
    }

    @Override
    public void specifyBinCount(Number count)
    {
        sampleModel.specifyBinCount(count);
    }

    @Override
    public void setCountConsistentWithRange()
    {
        sampleModel.setCountConsistentWithRange();
    }

    @Override
    public void setCountConsistentWithWidth()
    {
        sampleModel.setCountConsistentWithWidth();
    }

    @Override
    public void setWidthConsistentWithRangeAndCount()
    {
        sampleModel.setWidthConsistentWithRangeAndCount();
    }

    @Override
    public double getBinWidth()
    {
        return sampleModel.getBinWidth();
    }

    @Override
    public void setBinWidth(Double binWidthNew)
    {
        sampleModel.setBinWidth(binWidthNew);
    }

    @Override
    public void specifyBinWidth(Double width)
    {
        sampleModel.specifyBinWidth(width);
    }

    @Override
    public boolean isFullRange()
    {
        return sampleModel.isFullRange();
    }

    @Override
    public void setFullRange(Boolean full)
    {
        sampleModel.specifyFullRange(full);
    }

    @Override
    public Double getFractionOfSmallestTrimmed()
    {
        return sampleModel.getFractionOfSmallestTrimmed();
    }

    @Override
    public void setFractionOfSmallestTrimmed(Double trimSmallestNew)
    {
        sampleModel.specifyFractionfSmallestTrimmed(trimSmallestNew);	
    }

    @Override
    public Double getFractionOfLargestTrimmed()
    {
        return sampleModel.getFractionOfLargestTrimmed();
    }

    @Override
    public void setFractionOfLargestTrimmed(Double trimLargestNew)
    {
        sampleModel.specifyFractionOfLargestTrimmed(trimLargestNew);	
    }

    @Override
    public boolean isFitted()
    {
        return sampleModel.isFitted();
    }

    @Override
    public void setFitted(Boolean fitted)
    {
        sampleModel.specifyFitted(fitted);
    }

    @Override
    public double getRangeMinimum()
    {
        return sampleModel.getRangeMininmum();
    }

    @Override
    public void setRangeMinimum(Double min)
    {	
        sampleModel.setRangeMinimum(min);
    }

    @Override
    public void specifyRangeMinimum(Double min)
    {
        sampleModel.specifyRangeMinimum(min);
    }

    @Override
    public double getRangeMaximum()
    {
        return sampleModel.getRangeMaximum();
    }

    @Override
    public void setRangeMaximum(Double max)
    {
        sampleModel.setRangeMaximum(max);
    }

    @Override
    public void specifyRangeMaximum(Double max)
    {
        sampleModel.specifyRangeMaximum(max);
    }

    @Override
    public boolean isRangeExtensive()
    {
        return sampleModel.isRangeExtensive();
    }

    @Override
    public HistogramType getHistogramType()
    {
        return sampleModel.getHistogramType();
    }

    @Override
    public void setHistogramType(HistogramType type)
    {
        sampleModel.specifyHistogramType(type);
    }

    @Override
    public DistributionType getDistributionType()
    {
        return sampleModel.getDistributionType();
    }

    @Override
    public void setDistributionType(DistributionType type)
    {
        sampleModel.specifyDistributionType(type);
    }

    @Override
    public FitType getFitType()
    {
        return sampleModel.getFitType();
    }

    @Override
    public void setFitType(FitType type)
    {
        sampleModel.specifyFitType(type);
    }

    @Override
    public BinningMethod getBinningMethod()
    {
        return sampleModel.getBinningMethod();
    }

    @Override
    public void setBinningMethod(BinningMethod method)
    {
        sampleModel.specifyBinningMethod(method);
    }

    public boolean isInputProvided()
    {
        return sampleModel.isInputProvided();
    }

    public boolean isAllInputProvided()
    {
        return finishEnabled;
    }

    public int getCurrentBatchIndex()
    {
        return currentValueModelIndex;
    }

    @Override
    public void setUndoPoint()
    {
        sampleModel.setUndoPoint();
    }

    @Override
    public void undo()
    {
        sampleModel.undo();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();
        String name = evt.getPropertyName();

        if(source == sampleModel)
        {
            if(ENTITLED.equals(name) || NAME.equals(name))
            {
                chart.setRoamingTitleText(sampleModel.getName());
            }
            else if(HISTOGRAM_TYPE.equals(name))
            {
                if(sampleModel.isInputProvided())
                {
                    plot.setRangeAxisDataQuantity(sampleModel.getRangeQuantity());
                    plot.setOnlyTicksOnRangeAxis(sampleModel.isOnlyIntegerOnRange());
                }			
            }
            else if(HISTOGRAM_DATA_CHANGED.equals(name))
            {
                if(sampleModel.isInputProvided())
                {
                    plot.setFitDataset(sampleModel.getFitDataset());
                    plot.setHistogramDataset(sampleModel.getHistogramDataset());
                }				
            }
            firePropertyChange(evt);
        }


        if(name.equals(INPUT_PROVIDED))
        {
            checkIfFinishEnabled();
        }
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledNew = sampleModel.isInputProvided();	

        if(finishEnabled != finishEnabledNew)
        {
            boolean finishEnabledOld = finishEnabled;
            this.finishEnabled = finishEnabledNew;

            firePropertyChange(WizardModelProperties.FINISH_ENABLED, finishEnabledOld, finishEnabledNew);
        }
    }

    private void checkIfTaskNameChanged()
    {
        String taskNew = sampleModel.getTask();
        String taskOld = task;
        this.task = taskNew;

        firePropertyChange(WizardModelProperties.TASK, taskOld, taskNew);
    }
}
