
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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;


import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.AbstractContinuousDistribution;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.jfree.data.statistics.HistogramBin;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import atomicJ.data.Datasets;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.StandardSample;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.StandardStyleTag;
import atomicJ.statistics.BinningMethod;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.DistributionFit;
import atomicJ.statistics.DistributionType;
import atomicJ.statistics.FitType;
import atomicJ.statistics.HistogramType;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.Validation;

import static atomicJ.gui.histogram.HistogramModelProperties.*;

public class HistogramSampleModel  extends AbstractModel
{
    private final static double TOLERANCE = 1e-14;
    private final static double MINIMAL_EXTENSIVE_RANGE_LENGTH = 1e-14;

    private boolean initFullRange;

    private String initName;
    private boolean initEntitled;

    private double[] initDataToPlot;
    private double initDataAbsoluteMin;
    private double initDataAbsoluteMax;	

    private double initBinCount;
    private double initBinWidth;
    private double initRangeMin;
    private double initRangeMax;
    private Double initTrimSmallest;
    private Double initTrimLargest;
    private boolean initFitted;
    private double initLastSpecifiedRangeMax;
    private double initLastSpecifiedBinWidth;
    private BinningMethod initBinningMethod;
    private HistogramType initHistogramType;
    private DistributionType initDistributionType;
    private FitType initFitType;

    private QuantitativeSample sample;
    private int allDataCount;
    private Quantity domainQuantity;
    private String task;

    private boolean containsNonPositiveValues;
    private double dataAbsoluteMin;
    private double dataAbsoluteMax;	
    private boolean entitled;	
    private boolean fullRange;
    private boolean fitted;
    private boolean inputProvided;
    private DistributionFit fit;
    private double[] dataToPlot;
    private int retainedDataCount;
    private int dataDiscardedCount;

    private final String type;
    private String name;

    private Double trimSmallest;
    private Double trimLargest;
    private Double binCount;
    private Double binWidth;
    private Double rangeMin;
    private Double rangeMax;
    private Double rangeLength;

    private Double lastSpecifiedRangeMax;
    private Double lastSpecifiedBinWidth;

    private BinningMethod binningMethod;
    private FitType fitType;
    private HistogramType histogramType;
    private DistributionType distributionType;	

    public HistogramSampleModel(QuantitativeSample sample)
    {
        this(sample.getKey(), sample);
    }

    public HistogramSampleModel(String identifier, QuantitativeSample sample)
    {
        Validation.requireNonNullParameterName(sample, "sample"); 

        if(sample.getMagnitudes().length<1)
        {
            throw new IllegalArgumentException("'Sample' is empty");
        }

        this.sample = new StandardSample(DescriptiveStatistics.clearOfNaNs(sample.getMagnitudes()), sample.getQuantityName(), sample.getQuantity(), sample.getSampleName());
        this.dataToPlot = this.sample.getMagnitudes();
        this.allDataCount = dataToPlot.length;
        this.containsNonPositiveValues = ArrayUtilities.containsNonPositiveValues(dataToPlot);

        this.domainQuantity = this.sample.getQuantity();

        this.name = this.sample.getQuantityName();

        this.type = identifier;

        this.task = "<html>Specify histogram plotting options for <b>" + name +  "</b></html>";
        initDefaults();
    }


    public HistogramSampleModel(String type, QuantitativeSample sample, String collectionName)
    {
        Validation.requireNonNullParameterName(sample, "sample"); 

        this.sample = new StandardSample(DescriptiveStatistics.clearOfNaNs(sample.getMagnitudes()), sample.getQuantityName(), sample.getQuantity(), sample.getSampleName());
        this.dataToPlot = this.sample.getMagnitudes();
        this.allDataCount = dataToPlot.length;
        this.containsNonPositiveValues = ArrayUtilities.containsNegativeValues(dataToPlot);

        this.domainQuantity = this.sample.getQuantity();

        this.name = this.sample.getQuantityName();

        this.type = type;

        this.task = "<html>Specify histogram plotting options for <b>" + name +  "</b> for: <b>" + collectionName + "</b></html>";
        initDefaults();
    }

    public static HistogramSampleModel getSampleModelFromCopy(QuantitativeSample sample)
    {
        double[] data = DescriptiveStatistics.clearOfNaNs(sample.getMagnitudes());

        QuantitativeSample sampleCopy = new StandardSample(data, sample.getQuantity());
        HistogramSampleModel sampleModel = new HistogramSampleModel(sampleCopy);

        sampleModel.specifyBinWidth(DescriptiveStatistics.getFreedmanDiaconisBinWidth(data));

        return sampleModel;
    }

    public void setSample(QuantitativeSample sample)
    {
        setSample(sample, true);
    }


    public void setSample(QuantitativeSample sample, boolean notify)
    {
        Validation.requireNonNullParameterName(sample, "sample"); 

        this.sample = new StandardSample(DescriptiveStatistics.clearOfNaNs(sample.getMagnitudes()), sample.getQuantityName(), sample.getQuantity(), sample.getSampleName());
        double[] dataToPlot = this.sample.getMagnitudes();
        this.allDataCount = dataToPlot.length;
        this.containsNonPositiveValues = ArrayUtilities.containsNonPositiveValues(dataToPlot);

        this.domainQuantity = this.sample.getQuantity();
        this.name = this.sample.getQuantityName();

        this.task = "<html>Specify histogram plotting options for <b>" + name;

        this.dataAbsoluteMin = ArrayUtilities.getMinimum(dataToPlot);
        this.dataAbsoluteMax = ArrayUtilities.getMaximum(dataToPlot);

        if(this.binWidth <TOLERANCE)
        {
            this.binWidth = 0.1;
            this.lastSpecifiedBinWidth = 0.1;
        }

        updateDataToPlot();

        if(notify)
        {
            firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
        }		
    }

    public void applyFunction(UnivariateFunction f)
    {
        QuantitativeSample sample = this.sample.applyFunction(f);
        setSample(sample);
    }

    public void applyFunction(UnivariateFunction f, String functionName)
    {
        QuantitativeSample sample = this.sample.applyFunction(f, functionName);

        setSample(sample);
    }

    public void fixZero()
    {
        QuantitativeSample sample = this.sample.fixZero();
        setSample(sample);
    }

    private void initDefaults()
    {
        this.dataAbsoluteMin = ArrayUtilities.getMinimum(dataToPlot);
        this.dataAbsoluteMax = ArrayUtilities.getMaximum(dataToPlot);
        this.retainedDataCount = allDataCount;
        this.dataDiscardedCount = 0;
        this.entitled = true;
        this.binCount = Double.NaN;
        this.binWidth = Double.NaN;
        this.fullRange = true;
        this.fitted = false;
        this.trimSmallest = Double.valueOf(0);
        this.trimLargest = Double.valueOf(0);
        this.rangeMin = dataAbsoluteMin;
        this.rangeMax = dataAbsoluteMax;
        this.lastSpecifiedRangeMax = rangeMax;
        this.lastSpecifiedBinWidth = binWidth;

        this.binningMethod = BinningMethod.MANUAL;
        this.histogramType = HistogramType.COUNT;
        this.distributionType = DistributionType.ORDINARY;
        this.fitType = FitType.NORMAL;

        this.rangeLength = rangeMax - rangeMin;
        this.inputProvided = false;
    }

    public void setUndoPoint()
    {
        this.initName = name;
        this.initEntitled = entitled;

        this.initDataToPlot = Arrays.copyOf(dataToPlot, dataToPlot.length);
        this.initDataAbsoluteMin = dataAbsoluteMin;
        this.initDataAbsoluteMax = dataAbsoluteMax;
        this.initBinCount = binCount;
        this.initBinWidth = binWidth;
        this.initFullRange = fullRange;
        this.initRangeMin = rangeMin;
        this.initRangeMax = rangeMax;
        this.initTrimSmallest = trimSmallest;
        this.initTrimLargest = trimLargest;
        this.initLastSpecifiedRangeMax = lastSpecifiedRangeMax;
        this.initLastSpecifiedBinWidth = lastSpecifiedBinWidth;
        this.initFitted = fitted;
        this.initBinningMethod = binningMethod;
        this.initHistogramType = histogramType;
        this.initDistributionType = distributionType;
        this.initFitType = fitType;
    }

    public void undo()
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        this.dataToPlot = initDataToPlot;
        this.fit = null;
        this.dataAbsoluteMin = initDataAbsoluteMin;
        this.dataAbsoluteMax = initDataAbsoluteMax;
        this.lastSpecifiedRangeMax = initLastSpecifiedRangeMax;
        this.lastSpecifiedBinWidth = initLastSpecifiedBinWidth;

        allEvents.add(setRetainedDataCountSilent(dataToPlot.length));
        allEvents.add(setDiscardedDataCountSilent(allDataCount - dataToPlot.length));
        allEvents.add(setNameSilent(initName));
        allEvents.add(setEntitledSilent(initEntitled));
        allEvents.add(setBinWidthSilent(initBinWidth));
        allEvents.add(setBinCountSilent(initBinCount));
        allEvents.add(setFullRangSilent(initFullRange));
        allEvents.add(setRangeMinimumSilent(initRangeMin));
        allEvents.add(setRangeMaximumSilent(initRangeMax));
        allEvents.addAll(updateRangeLengthSilent());
        allEvents.add(setFractionfSmallestTrimmedSilent(initTrimSmallest));
        allEvents.add(setFractionOfLargestTrimmedSilent(initTrimLargest));
        allEvents.add(setBinningMethodSilent(initBinningMethod));

        allEvents.add(setFitted(initFitted));
        allEvents.add(setHistogramTypeSilent(initHistogramType));
        allEvents.add(setDistributionTypeSilent(initDistributionType));
        allEvents.add(setFitTypeSilent(initFitType));
        allEvents.add(updateInputProvidedSilent());

        PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);
        allEvents.add(eventDataChanged);

        for(PropertyChangeEvent event : allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    public QuantitativeSample getAllDataSample()
    {
        return sample;
    }

    public QuantitativeSample getSample()
    {
        QuantitativeSample currentSample = new StandardSample(dataToPlot, domainQuantity);
        return currentSample;
    }

    public double getDataMean()
    {
        return DescriptiveStatistics.arithmeticMean(dataToPlot);
    }

    public double getDataSD()
    {
        return DescriptiveStatistics.standardDeviationSample(dataToPlot);
    }

    public DistributionFit getDistributionFit()
    {
        if(fitted)
        {
            if(fit == null)
            {
                fit = fitType.getFittedDistribution(dataToPlot);
            }
            return fit;
        }
        else
        {
            return null;
        }
    }

    public double getFitLocation()
    {
        if(fitted)
        {
            if(fit == null)
            {
                fit = fitType.getFittedDistribution(dataToPlot);
            }
            return fit.getLocation();
        }
        else
        {
            return Double.NaN;
        }
    }

    public double getFitScale()
    {
        if(fitted)
        {
            if(fit == null)
            {
                fit = fitType.getFittedDistribution(dataToPlot);
            }
            return fit.getScale();
        }
        else
        {
            return Double.NaN;
        }
    }

    public String getTask()
    {
        return task;
    }

    public boolean containsNonpositive()
    {
        return containsNonPositiveValues;
    }

    public double getBinWidth()
    {
        return binWidth;
    }

    public void specifyBinWidth(Double binWidthNew)
    {			
        if(binWidth.equals(binWidthNew))
        {
            return;
        }

        lastSpecifiedBinWidth = binWidthNew;

        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventWidth = setBinWidthSilent(binWidthNew);
        List<PropertyChangeEvent> eventsCount = setCountConsistentWithWidthSilent();
        PropertyChangeEvent eventInputProvided = updateInputProvidedSilent();
        PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);

        allEvents.add(eventWidth);
        allEvents.addAll(eventsCount);
        allEvents.add(eventInputProvided);
        allEvents.add(eventDataChanged);

        for(PropertyChangeEvent event : allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    public void setBinWidth(Double binWidthNew)
    {
        PropertyChangeEvent event = setBinWidthSilent(binWidthNew);
        firePropertyChange(event);
    }

    private PropertyChangeEvent setBinWidthSilent(Double binWidthNew)
    {
        Validation.requireNonNullParameterName(binWidthNew, "binWidthNew");
        Validation.requireValueGreaterOrEqualToParameterName(binWidthNew.doubleValue(),0, "binWidthNew");

        double binWidthOld = binWidth;
        this.binWidth = binWidthNew;
        PropertyChangeEvent event = new PropertyChangeEvent(this, BIN_WIDTH, binWidthOld, binWidthNew);

        return event;
    }

    public void specifyBinCount(Number binCountNew)
    {	
        if(binCount.equals(binCountNew))
        {
            return;
        }

        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventCount = setBinCountSilent(binCountNew);
        PropertyChangeEvent eventWidth = setWidthConsistentWithRangeAndCountSilent();
        lastSpecifiedBinWidth = binWidth;
        PropertyChangeEvent eventInputProvided = updateInputProvidedSilent();
        PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);

        allEvents.add(eventCount);
        allEvents.add(eventWidth);
        allEvents.add(eventInputProvided);
        allEvents.add(eventDataChanged);

        for(PropertyChangeEvent event : allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    public void setBinCount(Number binCount)
    {
        PropertyChangeEvent event = setBinCountSilent(binCount);
        firePropertyChange(event);
    }

    private PropertyChangeEvent setBinCountSilent(Number count)
    {
        Validation.requireNonNullParameterName(count, "count");
        Validation.requireValueGreaterOrEqualToParameterName(count.doubleValue(),0, "count");

        Double binCountNew = Math.ceil(count.doubleValue());

        double binCountOld = binCount;
        this.binCount = binCountNew;

        PropertyChangeEvent event = new PropertyChangeEvent(this, BIN_COUNT, binCountOld, binCountNew);		
        return event;
    }

    public void specifyRangeMinimum(Double rangeMinNew)
    {
        if(rangeMin.equals(rangeMinNew))
        {
            return;
        }

        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventRangeMinimum = setRangeMinimumSilent(rangeMinNew);
        List<PropertyChangeEvent> eventRange = updateRangeLengthSilent();
        List<PropertyChangeEvent> consistencyEvents = setCountAndWidthConsistentWithRangeSilent();
        PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);

        allEvents.add(eventRangeMinimum);
        allEvents.addAll(eventRange);
        allEvents.addAll(consistencyEvents);
        allEvents.add(eventDataChanged);

        for(PropertyChangeEvent event: allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    public void setRangeMinimum(Double rangeMinNew)
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();
        PropertyChangeEvent eventRangeMin = setRangeMinimumSilent(rangeMinNew);
        List<PropertyChangeEvent> eventsRangeLength = updateRangeLengthSilent();

        allEvents.add(eventRangeMin);	
        allEvents.addAll(eventsRangeLength);

        for(PropertyChangeEvent event: allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    private PropertyChangeEvent setRangeMinimumSilent(Double rangeMinNew)
    {
        Validation.requireNonNullParameterName(rangeMinNew, "rangeMinNew");

        double rangeMinOld = rangeMin;
        this.rangeMin = rangeMinNew;
        PropertyChangeEvent event = new PropertyChangeEvent(this, RANGE_MIN, rangeMinOld, rangeMinNew);

        return event;
    }

    public double getRangeMaximum()
    {
        return rangeMax;
    }

    public void specifyRangeMaximum(Double rangeMaxNew)
    {
        if(rangeMax.equals(rangeMaxNew))
        {
            return;
        }

        this.lastSpecifiedRangeMax = rangeMaxNew;

        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventRangeMaximum = setRangeMaximumSilent(rangeMaxNew);
        List<PropertyChangeEvent> eventRange = updateRangeLengthSilent();
        List<PropertyChangeEvent> consistencyEvents = setCountAndWidthConsistentWithRangeSilent();
        PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);

        allEvents.add(eventRangeMaximum);
        allEvents.addAll(eventRange);
        allEvents.addAll(consistencyEvents);
        allEvents.add(eventDataChanged);

        for(PropertyChangeEvent event: allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    public void setRangeMaximum(Double rangeMaxNew)
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventRangeMax = setRangeMaximumSilent(rangeMaxNew);
        List<PropertyChangeEvent> eventsRangeLength = updateRangeLengthSilent();

        allEvents.add(eventRangeMax);
        allEvents.addAll(eventsRangeLength);

        for(PropertyChangeEvent event: allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }

    }


    private PropertyChangeEvent setRangeMaximumSilent(Double rangeMaxNew)
    {
        Validation.requireNonNullParameterName(rangeMaxNew, "rangeMaxNew");

        double rangeMaxOld = rangeMax;
        this.rangeMax = rangeMaxNew;

        PropertyChangeEvent event = new PropertyChangeEvent(this, RANGE_MAX, rangeMaxOld, rangeMaxNew);

        return event;
    }

    public boolean isFullRange()
    {
        return fullRange;
    }

    public void specifyFullRange(boolean fullRangeNew)
    {
        if(fullRange == fullRangeNew)
        {
            return;
        }

        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventFullRange = setFullRangSilent(fullRangeNew);
        allEvents.add(eventFullRange);

        if(fullRange)
        {
            List<PropertyChangeEvent> eventsFullRange = updateFullRange(true);
            allEvents.addAll(eventsFullRange);
        }

        PropertyChangeEvent eventUpdateInputProvided = updateInputProvidedSilent();
        allEvents.add(eventUpdateInputProvided);

        PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);
        allEvents.add(eventDataChanged);

        for(PropertyChangeEvent event: allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    private PropertyChangeEvent setFullRangSilent(boolean fullRangeNew)
    {
        boolean fullRangeOld = fullRange;
        this.fullRange = fullRangeNew;

        PropertyChangeEvent eventFullRange = new PropertyChangeEvent(this, FULL_RANGE, fullRangeOld, fullRangeNew);
        return eventFullRange;
    }

    private List<PropertyChangeEvent> updateFullRange(boolean updateCountAndWidth)
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventRangeMinimum = setRangeMinimumSilent(dataAbsoluteMin);
        PropertyChangeEvent eventRangeMaximum = setRangeMaximumSilent(dataAbsoluteMax);

        this.lastSpecifiedRangeMax = dataAbsoluteMax;

        List<PropertyChangeEvent> eventRangeLength = updateRangeLengthSilent();

        allEvents.add(eventRangeMinimum);
        allEvents.add(eventRangeMaximum);
        allEvents.addAll(eventRangeLength);

        if(updateCountAndWidth)
        {
            List<PropertyChangeEvent> consistencyEvents = setCountAndWidthConsistentWithRangeSilent();	
            allEvents.addAll(consistencyEvents);
        }	

        return allEvents;
    }

    public double getRangeMininmum()
    {
        return rangeMin;
    }	

    private List<PropertyChangeEvent> updateRangeLengthSilent()
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        double rangeNew = rangeMax - rangeMin;

        if(!rangeLength.equals(rangeNew))
        {
            double rangeOld = rangeLength;
            rangeLength = rangeNew;

            allEvents.add(new PropertyChangeEvent(this, RANGE_LENGTH, rangeOld, rangeNew));
            allEvents.add(new PropertyChangeEvent(this, RANGE_EXTENSIVE, rangeOld>MINIMAL_EXTENSIVE_RANGE_LENGTH, rangeNew>MINIMAL_EXTENSIVE_RANGE_LENGTH));
        }
        return allEvents;
    }

    public double getRangeLength()
    {
        return rangeLength;
    }

    public boolean isRangeExtensive()
    {
        boolean rangeExtensive = this.rangeLength > MINIMAL_EXTENSIVE_RANGE_LENGTH;
        return rangeExtensive;
    }

    public Double getFractionOfSmallestTrimmed()
    {
        return trimSmallest;
    }

    public void specifyFractionfSmallestTrimmed(Double trimSmallestNew)
    {
        Validation.requireNonNullParameterName(trimSmallestNew, "trimSmallestNew");
        Validation.requireValueEqualToOrBetweenBounds(trimSmallestNew.doubleValue(), 0, 1, "trimSmallestNew");

        if(!this.trimSmallest.equals(trimSmallestNew))
        {	
            List<PropertyChangeEvent> allEvents = new ArrayList<>();

            PropertyChangeEvent eventTrimSmallest = setFractionfSmallestTrimmedSilent(trimSmallestNew);
            List<PropertyChangeEvent> eventsUpdateDataToPlot = updateDataToPlot();
            PropertyChangeEvent eventUpdateNegativeValues = updateNonpositiveValues();
            PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);

            allEvents.add(eventTrimSmallest);
            allEvents.addAll(eventsUpdateDataToPlot);
            allEvents.add(eventUpdateNegativeValues);
            allEvents.add(eventDataChanged);

            for(PropertyChangeEvent event: allEvents)
            {
                if(event != null)
                {
                    firePropertyChange(event);
                }
            }	
        }	
    }

    private PropertyChangeEvent setFractionfSmallestTrimmedSilent(Double trimSmallestNew)
    {
        double trimSmallestOld = trimSmallest;
        this.trimSmallest = trimSmallestNew;

        PropertyChangeEvent eventTrimSmallest = new PropertyChangeEvent(this, TRIM_SMALLEST, trimSmallestOld, trimSmallestNew);
        return eventTrimSmallest;
    }

    public Double getFractionOfLargestTrimmed()
    {
        return trimLargest;
    }

    public void specifyFractionOfLargestTrimmed(Double trimLargestNew)
    {
        Validation.requireNonNullParameterName(trimLargestNew, "trimLargestNew");
        Validation.requireValueEqualToOrBetweenBounds(trimLargestNew.doubleValue(), 0, 1, "trimLargestNew");

        if(!this.trimLargest.equals(trimLargestNew))
        {
            List<PropertyChangeEvent> allEvents = new ArrayList<>();

            PropertyChangeEvent eventTrimLargest = setFractionOfLargestTrimmedSilent(trimLargestNew);
            List<PropertyChangeEvent> eventsUpdateDataToPlot = updateDataToPlot();
            PropertyChangeEvent eventUpdateNegativeValues = updateNonpositiveValues();
            PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);

            allEvents.add(eventTrimLargest);
            allEvents.addAll(eventsUpdateDataToPlot);
            allEvents.add(eventUpdateNegativeValues);
            allEvents.add(eventDataChanged);

            for(PropertyChangeEvent event: allEvents)
            {
                if(event != null)
                {
                    firePropertyChange(event);
                }
            }
        }		
    }

    private PropertyChangeEvent setFractionOfLargestTrimmedSilent(Double trimLargestNew)
    {
        double trimLargestOld = trimLargest;
        this.trimLargest = trimLargestNew;

        PropertyChangeEvent eventTrimLargest = new PropertyChangeEvent(this, TRIM_LARGEST, trimLargestOld, trimLargestNew);		
        return eventTrimLargest;
    }

    private List<PropertyChangeEvent> updateDataToPlot()
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventDataToPlot = setDataToPlot(findDataToPlot());
        List<PropertyChangeEvent> eventsAbsoluteRange = updateAbsoluteRangeExtrema();
        PropertyChangeEvent eventRetainedCount = setRetainedDataCountSilent(dataToPlot.length);
        PropertyChangeEvent eventDiscardedCount = setDiscardedDataCountSilent(allDataCount - dataToPlot.length);
        PropertyChangeEvent eventFitted = updateFitted();	

        allEvents.add(eventDataToPlot);
        allEvents.addAll(eventsAbsoluteRange);
        allEvents.add(eventRetainedCount);
        allEvents.add(eventDiscardedCount);
        allEvents.add(eventFitted);

        return allEvents;
    }

    private PropertyChangeEvent setDataToPlot(double[] dataToPlotNew)
    {
        double[] dataToPlotOld = dataToPlot;
        this.dataToPlot = dataToPlotNew;

        if(dataToPlotOld != dataToPlotNew)
        {
            fit = fitted ? fitType.getFittedDistribution(dataToPlotNew) : null;
        }

        PropertyChangeEvent eventDataToPlot = new PropertyChangeEvent(this, DATA_TO_PLOT, dataToPlotOld, dataToPlotNew);
        return eventDataToPlot;
    }

    private PropertyChangeEvent setDiscardedDataCountSilent(int discardedDataCountNew)
    {
        int discardedDataCountOld = dataDiscardedCount;
        this.dataDiscardedCount = discardedDataCountNew;
        PropertyChangeEvent eventDiscardedCount = new PropertyChangeEvent(this, DISCARDED_COUNT, discardedDataCountOld, discardedDataCountNew);

        return eventDiscardedCount;
    }

    private PropertyChangeEvent setRetainedDataCountSilent(int retainedDataCountNew)
    {
        int retainedDataCountOld = retainedDataCount;
        this.retainedDataCount = retainedDataCountNew;
        PropertyChangeEvent eventRetainedCount = new PropertyChangeEvent(this, RETAINED_COUNT, retainedDataCountOld, retainedDataCountNew);

        return eventRetainedCount;
    }

    private double[] findDataToPlot()
    {
        if(trimSmallest<TOLERANCE && trimLargest<TOLERANCE)
        {
            return sample.getMagnitudes(); 
        }
        else
        {
            return sample.trim(trimSmallest, trimLargest).getMagnitudes();
        }
    }

    private List<PropertyChangeEvent> updateAbsoluteRangeExtrema()
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        this.dataAbsoluteMin = ArrayUtilities.getMinimum(dataToPlot);
        this.dataAbsoluteMax = ArrayUtilities.getMaximum(dataToPlot);

        if(fullRange)
        {
            List<PropertyChangeEvent> eventsFullRange = updateFullRange(false);
            allEvents.addAll(eventsFullRange);
        }

        List<PropertyChangeEvent> consistencyEvents = setCountAndWidthConsistentWithRangeSilent();
        allEvents.addAll(consistencyEvents);
        allEvents.addAll(allEvents);

        return allEvents;
    }


    private List<PropertyChangeEvent> setCountAndWidthConsistentWithRangeSilent()
    {
        if(binningMethod.equals(BinningMethod.MANUAL))
        {
            return setCountAndWidthConsistentWithRangeForManualSilent();
        }
        else if(binningMethod.equals(BinningMethod.STURGES))
        {
            return setCountAndWidthConsistentWithRangeForSturgesSilent();
        }
        else if(binningMethod.equals(BinningMethod.SCOTTS))
        {
            return setCountAndWidthConsistentWithRangeForScottsSilent();
        }
        else if(binningMethod.equals(BinningMethod.FREEDMAN_DIACONIS))
        {
            return setCountAndWidthConsistentWithRangeForFreedmanDiaconisSilent();
        }
        else if(binningMethod.equals(BinningMethod.SQUARE_ROOT))
        {
            return setCountAndWidthConsistentWithRangeForSquareRootSilent();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private List<PropertyChangeEvent> setCountAndWidthConsistentWithRangeForManualSilent()//OK
    {
        List<PropertyChangeEvent> events = new ArrayList<>();
        if(!binWidth.equals(Double.NaN))
        {
            events.addAll(setCountConsistentWithRangeSilent());
        }
        else if(!binCount.equals(Double.NaN))
        {
            PropertyChangeEvent eventWidth = setWidthConsistentWithRangeAndCountSilent();
            events.add(eventWidth);
        }
        return events;
    }

    private List<PropertyChangeEvent> setCountAndWidthConsistentWithRangeForSturgesSilent()
    {
        List<PropertyChangeEvent> events = new ArrayList<>();
        double bincountNew = Math.ceil(1 + Math.log(retainedDataCount)/Math.log(2)); 

        PropertyChangeEvent eventCount = setBinCountSilent(bincountNew);
        PropertyChangeEvent eventWidth = setWidthConsistentWithRangeAndCountSilent();

        events.add(eventCount);
        events.add(eventWidth);

        return events;
    }

    private List<PropertyChangeEvent> setCountAndWidthConsistentWithRangeForScottsSilent()//OK
    {
        List<PropertyChangeEvent> events = new ArrayList<>();

        double sd = DescriptiveStatistics.standardDeviationSample(dataToPlot);
        double cubic = Math.cbrt(retainedDataCount);

        double binWidthNew = (3.5*sd/cubic);
        PropertyChangeEvent eventWidth = setBinWidthSilent(binWidthNew);

        List<PropertyChangeEvent> eventsCount = setCountConsistentWithWidthSilent();

        events.add(eventWidth);
        events.addAll(eventsCount);

        return events;
    }

    private List<PropertyChangeEvent> setCountAndWidthConsistentWithRangeForSquareRootSilent()
    {
        List<PropertyChangeEvent> events = new ArrayList<>();

        double binCountNew = Math.rint(Math.sqrt(retainedDataCount));
        PropertyChangeEvent eventCount = setBinCountSilent(binCountNew);

        PropertyChangeEvent eventWidth = setWidthConsistentWithRangeAndCountSilent();
        events.add(eventCount);
        events.add(eventWidth);

        return events;
    }

    private List<PropertyChangeEvent> setCountAndWidthConsistentWithRangeForFreedmanDiaconisSilent()//OK
    {
        List<PropertyChangeEvent> events = new ArrayList<>();

        double iqr = DescriptiveStatistics.interquartileLength(dataToPlot);
        double cubic = Math.cbrt(retainedDataCount);

        double binWidthNew = 2*iqr/cubic;

        if(binWidthNew>TOLERANCE)
        {
            PropertyChangeEvent eventWidth = setBinWidthSilent(binWidthNew);

            List<PropertyChangeEvent> eventsCount = setCountConsistentWithWidthSilent();
            events.add(eventWidth);
            events.addAll(eventsCount);
        }
        else 
        {
            PropertyChangeEvent eventCount = setWidthConsistentWithRangeAndCountSilent();
            events.add(eventCount);
        }

        return events;
    }

    public void setCountConsistentWithRange()
    {
        List<PropertyChangeEvent> allEvents = setCountConsistentWithRangeSilent();

        for(PropertyChangeEvent event : allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
                firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
            }
        }
    }

    private List<PropertyChangeEvent> setCountConsistentWithRangeSilent()
    {
        List<PropertyChangeEvent> events = new ArrayList<>();
        if(!Double.isNaN(lastSpecifiedBinWidth) && !Double.isNaN(rangeLength))
        {			
            Double binCountNew = Math.max(1,rangeLength/lastSpecifiedBinWidth);

            double r = Math.ceil(binCountNew.doubleValue()) - binCountNew;

            if(Math.abs(r) > TOLERANCE)
            {
                PropertyChangeEvent eventCount = setBinCountSilent(binCountNew);
                PropertyChangeEvent eventWidth = setWidthConsistentWithRangeAndCountSilent();

                events.add(eventCount);
                events.add(eventWidth);
            }
            else
            {
                PropertyChangeEvent eventCount = setBinCountSilent(binCountNew);
                events.add(eventCount);
            }
        }	
        return events;
    }

    public void setCountConsistentWithWidth(Double binWidth)
    {
        List<PropertyChangeEvent> events = setCountConsistentWithWidthSilent();
        for(PropertyChangeEvent event: events)
        {
            if(event != null)
            {
                firePropertyChange(event);
                firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
            }
        }
    }

    private List<PropertyChangeEvent> setCountConsistentWithWidthSilent()
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();
        if(!binWidth.equals(Double.NaN) && !rangeLength.equals(Double.NaN))
        {
            Double binCountNew = Math.max(1,(lastSpecifiedRangeMax - rangeMin)/binWidth);
            double r = Math.ceil(binCountNew.doubleValue()) - binCountNew;

            PropertyChangeEvent eventCount = setBinCountSilent(binCountNew);
            allEvents.add(eventCount);

            if(Math.abs(r) > TOLERANCE)
            {
                double rangeMaxNew = lastSpecifiedRangeMax + (r*binWidth);
                PropertyChangeEvent eventRangeMaximum = setRangeMaximumSilent(rangeMaxNew);
                List<PropertyChangeEvent> eventRangeLength = updateRangeLengthSilent();
                allEvents.add(eventRangeMaximum);
                allEvents.addAll(eventRangeLength);
            }
        }	

        return allEvents;
    }

    public void setCountConsistentWithWidth()
    {
        List<PropertyChangeEvent> allEvents = new ArrayList<>();
        if(!binWidth.equals(Double.NaN) && !Double.isNaN((lastSpecifiedRangeMax - rangeMin)))
        {
            Double binCountNew = (lastSpecifiedRangeMax - rangeMin)/binWidth;
            double r = Math.ceil(binCountNew.doubleValue()) - binCountNew;;

            PropertyChangeEvent eventCount = setBinCountSilent(binCountNew);
            allEvents.add(eventCount);


            if(Math.abs(r) > TOLERANCE)
            {
                double rangeMaxNew = lastSpecifiedRangeMax + (r*binWidth);
                PropertyChangeEvent eventRangeMaximum = setRangeMaximumSilent(rangeMaxNew);
                List<PropertyChangeEvent> eventRangeLength = updateRangeLengthSilent();
                allEvents.add(eventRangeMaximum);
                allEvents.addAll(eventRangeLength);
            }				

            for(PropertyChangeEvent event: allEvents)
            {
                if(event != null)
                {
                    firePropertyChange(event);
                }
            }
        }	
    }

    public void setWidthConsistentWithRangeAndCount()
    {
        if(!binCount.equals(Double.NaN) && !rangeLength.equals(Double.NaN))
        {
            Double binWidthNew = rangeLength.doubleValue()/binCount.doubleValue();
            PropertyChangeEvent event = setBinWidthSilent(binWidthNew);
            firePropertyChange(event);
            firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
        }
    }


    private PropertyChangeEvent setWidthConsistentWithRangeAndCountSilent()
    {
        if(dataToPlot.length >0 && !binCount.equals(Double.NaN) && !rangeLength.equals(Double.NaN))
        {
            Double binWidthNew = rangeLength.doubleValue()/binCount.doubleValue();	

            PropertyChangeEvent eventWidth = setBinWidthSilent(binWidthNew);

            return eventWidth;
        }
        else
        {
            return null;
        }
    }

    public boolean isInputProvided()
    {
        return inputProvided;
    }

    private PropertyChangeEvent updateInputProvidedSilent()
    {
        PropertyChangeEvent event = null;

        boolean isNameProvided = name.length()>0;
        boolean isBinWidthProvided = !binWidth.isNaN();
        boolean isBinCountProvided = !binCount.isNaN();
        boolean isRangeProvided = !rangeLength.isNaN();

        boolean inputProvidedNew = (isNameProvided && isBinWidthProvided && isBinCountProvided && isRangeProvided);

        if(inputProvidedNew != inputProvided)
        {
            boolean inputProvidedOld = inputProvided;
            inputProvided = inputProvidedNew; 
            event = new PropertyChangeEvent(this, INPUT_PROVIDED, inputProvidedOld, inputProvidedNew);
        }

        return event;
    }

    public boolean isFitted()
    {
        return fitted;
    }

    public void specifyFitted(boolean fittedNew)
    {
        PropertyChangeEvent eventFitted = setFitted(fittedNew);
        firePropertyChange(eventFitted);
        firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
    }

    private PropertyChangeEvent setFitted(boolean fittedNew)
    {
        boolean fittedOld = fitted;
        this.fitted = fittedNew;		
        PropertyChangeEvent eventFitted = new PropertyChangeEvent(this, FITTED, fittedOld, fittedNew);
        return eventFitted;
    }

    private PropertyChangeEvent updateFitted()
    {
        boolean fittedOld = fitted;
        this.fitted = fittedOld && (!containsNonPositiveValues || !fitType.requiresPositiveValues());		
        PropertyChangeEvent eventFitted = new PropertyChangeEvent(this, FITTED, fittedOld, this.fitted);
        return eventFitted;
    }

    public HistogramType getHistogramType()
    {
        return histogramType;
    }

    public void specifyHistogramType(HistogramType typeNew)
    {
        PropertyChangeEvent eventHistogramType = setHistogramTypeSilent(typeNew);
        firePropertyChange(eventHistogramType);
        firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
    }

    private PropertyChangeEvent setHistogramTypeSilent(HistogramType typeNew)
    {
        HistogramType typeOld = histogramType;
        this.histogramType = typeNew;
        PropertyChangeEvent eventHistogramType = new PropertyChangeEvent(this, HISTOGRAM_TYPE, typeOld, typeNew);

        return eventHistogramType;
    }

    public DistributionType getDistributionType()
    {
        return distributionType;
    }

    public void specifyDistributionType(DistributionType typeNew)
    {		
        PropertyChangeEvent eventDistributionType = setDistributionTypeSilent(typeNew);
        firePropertyChange(eventDistributionType);
        firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
    }

    private PropertyChangeEvent setDistributionTypeSilent(DistributionType typeNew)
    {
        if(typeNew == null)
        {
            throw new NullPointerException("Null 'type' argument");
        }

        DistributionType typeOld = distributionType;
        this.distributionType = typeNew;		
        PropertyChangeEvent eventDistributionType = new PropertyChangeEvent(this, DISTRIBUTION_TYPE, typeOld, typeNew);

        return eventDistributionType;
    }

    public FitType getFitType()
    {
        return fitType;
    }

    public void specifyFitType(FitType typeNew)
    {
        PropertyChangeEvent eventFitType = setFitTypeSilent(typeNew);
        firePropertyChange(eventFitType);
        firePropertyChange(HISTOGRAM_DATA_CHANGED, false, true);
    }

    private PropertyChangeEvent setFitTypeSilent(FitType typeNew)
    {
        if(typeNew == null)
        {
            throw new NullPointerException("Null 'type' argument");
        }
        if(containsNonPositiveValues && typeNew.equals(FitType.LOG_NORMAL))
        {
            throw new IllegalArgumentException("Lognnormal distribution cannot be fitted, because dataset contains negative values");
        }

        FitType typeOld = fitType;
        this.fitType = typeNew;

        if(!typeOld.equals(typeNew))
        {
            fit = fitted ? fitType.getFittedDistribution(dataToPlot) : null;
        }

        PropertyChangeEvent eventFitType = new PropertyChangeEvent(this, FIT_TYPE, typeOld, typeNew);
        return eventFitType;
    }

    public BinningMethod getBinningMethod()
    {
        return binningMethod;
    }

    public void specifyBinningMethod(BinningMethod methodNew)
    {
        Validation.requireNonNullParameterName(methodNew, "methodNew"); 

        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventBinningMehod = setBinningMethodSilent(methodNew);
        List<PropertyChangeEvent> consistencyEvents = setCountAndWidthConsistentWithRangeSilent();
        PropertyChangeEvent inputProvidedEvent = updateInputProvidedSilent();
        PropertyChangeEvent eventDataChanged = new PropertyChangeEvent(this, HISTOGRAM_DATA_CHANGED, false, true);

        allEvents.add(eventBinningMehod);
        allEvents.addAll(consistencyEvents);
        allEvents.add(inputProvidedEvent);
        allEvents.add(eventDataChanged);

        for(PropertyChangeEvent event: allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    private PropertyChangeEvent setBinningMethodSilent(BinningMethod methodNew)
    {
        BinningMethod methodOld = binningMethod;
        this.binningMethod = methodNew;
        PropertyChangeEvent eventBinningMehod = new PropertyChangeEvent(this, BINNING_METHOD, methodOld, methodNew);	

        return eventBinningMehod;
    }

    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public void specifyName(String nameNew)
    {
        if(name.equals(nameNew))
        {
            return;
        }

        List<PropertyChangeEvent> allEvents = new ArrayList<>();

        PropertyChangeEvent eventName = setNameSilent(nameNew);
        PropertyChangeEvent eventInputProvided = updateInputProvidedSilent();
        allEvents.add(eventName);
        allEvents.add(eventInputProvided);
        for(PropertyChangeEvent event: allEvents)
        {
            if(event != null)
            {
                firePropertyChange(event);
            }
        }
    }

    private PropertyChangeEvent setNameSilent(String nameNew)
    {
        String nameOld = name;
        this.name = nameNew;

        PropertyChangeEvent eventName = new PropertyChangeEvent(this, NAME, nameOld, nameNew);

        return eventName;
    }

    public boolean isEntitled()
    {
        return entitled;
    }

    public void specifyEntitled(boolean entitledNew)
    {
        PropertyChangeEvent eventEntitled = setEntitledSilent(entitledNew);
        firePropertyChange(eventEntitled);
    }

    private PropertyChangeEvent setEntitledSilent(boolean entitledNew)
    {
        boolean entitledOld = entitled;
        this.entitled = entitledNew;

        PropertyChangeEvent eventEntitled = new PropertyChangeEvent(this, ENTITLED, entitledOld, entitledNew);

        return eventEntitled;
    }
    public double getBinCount()
    {
        return binCount;
    }

    public int getAllDataCount()
    {
        return allDataCount;
    }	

    public int getDataCount()
    {
        return retainedDataCount;
    }

    public int getDiscardedDataCount()
    {
        return dataDiscardedCount;
    }

    private PropertyChangeEvent updateNonpositiveValues()
    {
        boolean negativeOld = containsNonPositiveValues;
        boolean negativeNew = ArrayUtilities.containsNonPositiveValues(dataToPlot);
        this.containsNonPositiveValues = negativeNew;

        PropertyChangeEvent eventNonPositiveValues = new PropertyChangeEvent(this, NONPOSITIVE_VALUES, negativeOld, negativeNew);
        return eventNonPositiveValues;
    }

    private List<HistogramBin> populateBinList()
    {				
        List<HistogramBin> bins = new ArrayList<>();

        updateInputProvidedSilent();

        if(dataToPlot.length>0 && inputProvided)
        {
            boolean isCumulative = distributionType.equals(DistributionType.CUMULATIVE);
            int count = this.binCount.isInfinite()? 1 : Math.max(1, (int) Math.rint(this.binCount));

            double upper;
            double lower = rangeMin;

            for (int i = 0; i < count; i++) 
            {
                HistogramBin bin;
                if (i == count - 1) 
                {
                    bin = new HistogramBin(lower, rangeMax);
                }
                else 
                {
                    upper = rangeMin + (i + 1) * binWidth;
                    bin = new HistogramBin(lower, upper);
                    lower = upper;
                }
                bins.add(bin);
            }

            for (int i = 0; i < retainedDataCount; i++) 
            {
                double x = dataToPlot[i];
                if (rangeMin <= x && x <= rangeMax)
                { 	
                    double fraction = (x - rangeMin) / rangeLength;
                    int binIndex = (int) Math.min(fraction * count,count - 1);
                    if(isCumulative)
                    {
                        List<HistogramBin> previousBins = bins.subList(binIndex, count);
                        for(HistogramBin bin: previousBins)
                        {
                            bin.incrementCount();
                        }
                    }
                    else
                    {
                        HistogramBin bin = bins.get(binIndex);
                        bin.incrementCount();
                    }
                }
            }
        }	
        return bins;

    }

    private double[][] getFitPoints()
    {
        int n = 300;
        double[][] points = new double[n][2];
        double increment = rangeLength/(n - 1);

        if(fit == null)
        {
            fit = fitType.getFittedDistribution(dataToPlot);
        }

        AbstractContinuousDistribution distr = fit.getFittedDistribution();

        if(distributionType.equals(DistributionType.ORDINARY))
        {
            for(int i = 0; i<n;i++)
            {
                double x = rangeMin + i*increment;
                points[i][0] = x;
                points[i][1] = distr.density(x);
            }
            if(histogramType.equals(HistogramType.PROBABILITY))
            {
                for(int i = 0;i<n;i++)
                {
                    points[i][1] = binWidth*points[i][1];
                }
            }
            else if(histogramType.equals(HistogramType.COUNT))
            {
                for(int i = 0;i<n;i++)
                {
                    points[i][1] = retainedDataCount*binWidth*points[i][1];
                }
            }
            else if(histogramType.equals(HistogramType.LOG_COUNT))
            {
                for(int i = 0;i<n;i++)
                {
                    points[i][1] = Math.log10(retainedDataCount*binWidth*points[i][1]);
                }
            }
            return points;
        }
        else
        {
            try 
            {
                for(int i = 0; i<n;i++)
                {
                    double x = rangeMin + i*increment;

                    points[i][0] = x;
                    points[i][1] = distr.cumulativeProbability(x);
                } 
            }
            catch (MathException e) 
            {
                e.printStackTrace();
            }
            if(histogramType.equals(HistogramType.COUNT))
            {
                for(int i = 0;i<n;i++)
                {
                    points[i][1] = retainedDataCount*points[i][1];
                }
            }
            else if(histogramType.equals(HistogramType.LOG_COUNT))
            {
                for(int i = 0;i<n;i++)
                {
                    points[i][1] = Math.log10(retainedDataCount*points[i][1]);
                }
            }
            else if(histogramType.equals(HistogramType.PROBABILITY_DENSITY))
            {
                throw new IllegalStateException("Cumulative distribution is inconsistent with probability density");
            }
            return points;
        }
    }

    public FlexibleHistogramDataset getHistogramDataset()
    {
        List<HistogramBin> bins = populateBinList();
        FlexibleHistogramDataset dataset = null;
        if(bins != null)
        {
            dataset = new FlexibleHistogramDataset(histogramType);
            dataset.addSeries(domainQuantity.getName(), bins, getBinWidth(), retainedDataCount);
        }
        return dataset;
    }

    public XYSeriesCollection getFitDataset()
    {
        XYSeriesCollection fitCollection = null;
        if(fitted)
        {
            fitCollection = new XYSeriesCollection();	
            double[][] data = getFitPoints();
            XYSeries series = new XYSeries(Datasets.FIT);
            for(double[] p: data)
            {
                XYDataItem item = new XYDataItem(p[0], p[1]);
                series.add(item);
            }
            fitCollection.addSeries(series);
        }
        return fitCollection;
    }

    public boolean isOnlyIntegerOnRange()
    {
        return histogramType.equals(HistogramType.COUNT);
    }

    public Quantity getDomainQuantity()
    {
        return domainQuantity;
    }

    public Quantity getRangeQuantity()
    {
        return new DimensionlessQuantity(histogramType.toString());
    }

    public HistogramPlot getPlot()
    {
        List<HistogramBin> bins = populateBinList();
        if(bins != null)
        {
            Preferences pref = Preferences.userNodeForPackage(getClass()).node(getClass().getName()).node(sample.getKey());

            FlexibleHistogramDataset dataset = new FlexibleHistogramDataset(histogramType);
            dataset.addSeries(name, bins, getBinWidth(), allDataCount);

            Quantity rangeAxisQuantity = getRangeQuantity();  
            boolean onlyIntegersOnY = isOnlyIntegerOnRange();

            HistogramPlot plot = new HistogramPlot(dataset, new StandardStyleTag(sample.getKey()), sample.getQuantity().getName(), pref, domainQuantity, rangeAxisQuantity,onlyIntegersOnY);

            if(fitted)
            {
                XYSeriesCollection fitCollection = getFitDataset();					
                plot.setFitDataset(fitCollection);
            }

            return plot;
        }
        else
        {
            throw new IllegalStateException("Histogram cannot be returned, because not all necessary input has been provided");
        }
    }

    public Channel1DChart<HistogramPlot> getHistogram()
    {
        List<HistogramBin> bins = populateBinList();
        if(bins != null)
        {
            Preferences pref = Preferences.userNodeForPackage(getClass()).node(getClass().getName()).node(sample.getKey());

            FlexibleHistogramDataset dataset = new FlexibleHistogramDataset(histogramType);
            dataset.addSeries(name, bins, getBinWidth(), allDataCount);
            Quantity rangeAxisQuantity = getRangeQuantity();  
            boolean onlyIntegersOnY = isOnlyIntegerOnRange();

            HistogramPlot plot = new HistogramPlot(dataset, new StandardStyleTag(sample.getKey()), sample.getQuantity().getName(), pref, domainQuantity, rangeAxisQuantity,onlyIntegersOnY);

            if(fitted)
            {
                XYSeriesCollection fitCollection = getFitDataset();					
                plot.setFitDataset(fitCollection);
            }

            Channel1DChart<HistogramPlot> chart = new Channel1DChart<>(plot, Datasets.HSTOGRAM_PLOT);

            if(entitled)
            {
                chart.setRoamingTitleText(name);
            }
            return chart;
        }
        else
        {
            throw new IllegalStateException("Histogram cannot be returned, because not all necessary input has been provided");
        }
    }

    public HistogramAtomicModel getAtomicModel()
    {
        DistributionFit distributionFit = fitted ? getDistributionFit() : null;

        HistogramAtomicModel model = new HistogramAtomicModel(distributionFit, name, sample);

        return model;
    }
}
