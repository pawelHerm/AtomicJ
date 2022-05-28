
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

package atomicJ.gui.curveProcessing;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;

import javax.swing.JOptionPane;

import org.jfree.data.Range;
import atomicJ.analysis.BatchUtilities;
import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel2D;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIWizardReceiver;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;


public class CalibrationModel extends AbstractModel implements ROIWizardReceiver
{
    static final String APPLY_ENABLED = "Apply enabled";
    static final String NEXT_ENABLED = "Skip enabled";
    static final String FINISH_ENABLED = "Finish enabled";
    static final String CLEAR_ENABLED = "Clear enabled";
    static final String CURRENT_SOURCE = "Current curve";
    static final String CALIBRATION_SOURCES = "Sources still to process";
    static final String MEAN_SENSITIVITY = "Mean sensitivity";

    static final String FORCE_CURVE_BRANCH = "Force curve branch";
    static final String AVAILABLE_FORCE_CURVE_BRANCHES = "AvailableForceCurveBranches";
    static final String SUBSTRATE_SELECTION_ENABLED = "Substrate selection enabled";
    static final String LAST_SENSITIVITY_MEASUREMENT = "Last sensitivity measurement";
    static final String SENSITIVITY_MEASUREMENT_COUNT = "Sensitivity measurement count";

    static final String CURRENT_RANGE = "CurrentRange"; 
    static final String PHOTODIODE_SIGNAL_TYPE = "Photodiode signal type";

    private boolean applyEnabled = false;
    private boolean nextEnabled = false;
    private boolean finishEnabled = false;
    private boolean clearEnabled = false;
    private boolean substrateSelectionEnabled = false;

    private Range currentMaximumRange;
    private Range currentRange;

    private double minRangeWidth;

    private double currentSensitivity = Double.NaN;
    private double meanSensitivity = Double.NaN;

    private PhotodiodeSignalType photodiodeSignalType = PhotodiodeSignalType.VOLTAGE;

    private SimpleSpectroscopySource currentSource = null;
    private List<SimpleSpectroscopySource> calibrationSources = new ArrayList<>();
    private final List<Double> sensitivityMeasurements = new ArrayList<>();

    private ForceCurveBranch forceCurveBranch = ForceCurveBranch.APPROACH;
    private Set<ForceCurveBranch> availableBranches = new LinkedHashSet<>(Arrays.asList(ForceCurveBranch.values()));


    public boolean isSubstrateSelectionEnabled()
    {
        return substrateSelectionEnabled;
    }

    private void setSubstrateSelectionEnabled(boolean enabledNew)
    {
        boolean enabledOld = this.substrateSelectionEnabled;
        this.substrateSelectionEnabled = enabledNew;

        firePropertyChange(SUBSTRATE_SELECTION_ENABLED, enabledOld, enabledNew);                   
    }

    public SimpleSpectroscopySource getCurrentSource()
    {
        return currentSource;
    }

    public void setCurrentSource(SimpleSpectroscopySource currentSourceNew)
    {
        if(!Objects.equals(currentSourceNew, currentSource) && (calibrationSources.contains(currentSourceNew) || currentSourceNew == null))
        {
            SimpleSpectroscopySource currentSourceOld = currentSource;
            this.currentSource = currentSourceNew;

            //we need first to refresh available batches
            //and then to fire CURRENT_SOURCE event
            refreshAvailableBranches();            
            refreshRange();
            refreshSensitivity();

            firePropertyChange(CURRENT_SOURCE, currentSourceOld, currentSourceNew);
        }
    }

    private void refreshSensitivity()
    {
        double sensitivityNew = calculateSensitivity(this.currentRange);
        setSensitivity(sensitivityNew);
    }

    private void setConsistentWithMaximumRange(Range maxRange)
    {
        this.currentMaximumRange = maxRange;
        this.minRangeWidth = this.currentMaximumRange != null ? 0.001*currentMaximumRange.getLength() : 0;
    }

    private void refreshRange()
    {
        Range maximumRange = calculateCurrentMaximumRange();
        setConsistentWithMaximumRange(maximumRange);

        Range rangeNew = null;

        if(currentRange == null)
        {            
            rangeNew = maximumRange;
        }
        else if(maximumRange != null)
        {
            if(maximumRange.intersects(currentRange))
            {
                double lowerBoundNew = Math.max(maximumRange.getLowerBound(), currentRange.getLowerBound());
                double upperBoundNew = Math.min(maximumRange.getUpperBound(), currentRange.getUpperBound());

                rangeNew = new Range(lowerBoundNew, upperBoundNew);
            }
            else
            {
                //                double maximumRangeLength = maximumRange.getLength();
                //                double maximumLowerBound = maximumRange.getLowerBound();
                //                rangeNew = new Range(maximumLowerBound,  maximumLowerBound + 0.1*maximumRangeLength);
                rangeNew = maximumRange;

            }
        }

        setRange(rangeNew);
    }

    private void refreshAvailableBranches()
    {
        Set<ForceCurveBranch> availableBranchesNew = new LinkedHashSet<>();

        if(currentSource != null)
        {
            if(currentSource.containsApproachData())
            {
                availableBranchesNew.add(ForceCurveBranch.APPROACH);
            }
            if(currentSource.containsWithdrawData())
            {
                availableBranchesNew.add(ForceCurveBranch.WITHDRAW);
            }
        }       

        Set<ForceCurveBranch> availableBranchesOld = this.availableBranches;
        this.availableBranches = availableBranchesNew;

        firePropertyChange(AVAILABLE_FORCE_CURVE_BRANCHES, availableBranchesOld, this.availableBranches);

        if(!this.availableBranches.isEmpty() && !this.availableBranches.contains(forceCurveBranch))
        {
            setForceCurveBranch(availableBranches.iterator().next());
        }
    }

    public void removeCurrentSource()
    {
        List<SimpleSpectroscopySource> sourcesNew = new ArrayList<>(calibrationSources);
        boolean removed = sourcesNew.remove(currentSource);

        if(removed)
        {
            setSourcesWithoutChecking(sourcesNew);
        }
    }	

    public PhotodiodeSignalType getPhotodiodeSignalType()
    {
        return photodiodeSignalType;
    }

    public void setPhotodiodeSignalType(PhotodiodeSignalType photodiodeSignalTypeNew)
    {
        PhotodiodeSignalType photodiodeSignalTypeOld = this.photodiodeSignalType;

        if(!photodiodeSignalTypeOld.equals(photodiodeSignalTypeNew))
        {
            this.photodiodeSignalType = photodiodeSignalTypeNew;
            firePropertyChange(PHOTODIODE_SIGNAL_TYPE, photodiodeSignalTypeOld, photodiodeSignalTypeNew);

            setSourcesWithoutChecking(new ArrayList<SimpleSpectroscopySource>());
        }
    }

    public List<SimpleSpectroscopySource> getSources()
    {
        List<SimpleSpectroscopySource> sourcesCopy = new ArrayList<>(calibrationSources);
        return sourcesCopy;
    }

    /*
     * Sets the sources for calibration WITHOUT CHECKING whether their can be used for this purpose.
     * That's why it is a private method. Clients should call addSources(), which first checks whether
     * sources can be used for calibration and then calls setSourcesWithoutChecking()
     */

    private void setSourcesWithoutChecking(List<SimpleSpectroscopySource> sourcesNew)
    {
        List<SimpleSpectroscopySource> sourcesOld = new ArrayList<>(calibrationSources);
        this.calibrationSources = new ArrayList<>(sourcesNew);

        firePropertyChange(CALIBRATION_SOURCES, sourcesOld, sourcesNew);

        setFirstSourceAsCurrent();
        checkIfSubstrateSelectionEnabled();
    }

    /*
     * If the calibrationSources list is empty, the currentSource is set to null
     */
    private void setFirstSourceAsCurrent()
    {        
        SimpleSpectroscopySource firstSource = !calibrationSources.isEmpty() ? calibrationSources.get(0) : null;
        setCurrentSource(firstSource);        
    }

    public void addSources(List<SimpleSpectroscopySource> sourcesToAdd)
    {        
        List<SimpleSpectroscopySource> sourcesNew = new ArrayList<>(calibrationSources);

        int failuresCount = 0;

        for(SimpleSpectroscopySource source : sourcesToAdd)
        {
            boolean canBeUsed = source.canBeUsedForCalibration(photodiodeSignalType);
            if(canBeUsed)
            {
                sourcesNew.add(source);
            }
            else
            {
                failuresCount++;
            }
        }

        if(failuresCount>0)
        {
            JOptionPane.showMessageDialog(null, failuresCount + " files could not be used for calibration", "",
                    JOptionPane.ERROR_MESSAGE);
        }

        setSourcesWithoutChecking(sourcesNew);

        checkIfButtonsEnabled();
    }

    public void filterSources(List<Shape> shapes)
    {
        List<SimpleSpectroscopySource> sourcesNew = new ArrayList<>();

        if(!shapes.isEmpty())
        {
            Shape currentShape = shapes.get(0);

            for(SimpleSpectroscopySource source : getSources())
            {
                Point2D recordingPoint = source.getRecordingPoint();

                if(recordingPoint == null || currentShape.contains(recordingPoint))
                {
                    sourcesNew.add(source);
                }           
            }

            setSourcesWithoutChecking(sourcesNew);
        }
    }

    public ForceCurveBranch getForceCurveBranch()
    {
        return forceCurveBranch;
    }

    public void setForceCurveBranch(ForceCurveBranch forceCurveBranchNew)
    {
        ForceCurveBranch forceCurveBranchOld = this.forceCurveBranch;
        this.forceCurveBranch = forceCurveBranchNew;

        firePropertyChange(FORCE_CURVE_BRANCH, forceCurveBranchOld, forceCurveBranchNew);

        double sensitivity = calculateSensitivity(currentRange);
        setSensitivity(sensitivity);
    }

    public Set<ForceCurveBranch> getAvailableBranches()
    {
        return new LinkedHashSet<>(availableBranches);
    }

    public void addCurrentMeasurement()
    {
        addSensitivityMeasurement(currentSensitivity);
    }

    private void addSensitivityMeasurement(double s)
    {
        double meanSensitivityOld = meanSensitivity;
        double lastSensitivityMeasurementOld = getLastSensitivity();
        int measurementCountOld = sensitivityMeasurements.size();

        sensitivityMeasurements.add(Double.valueOf(s));

        double meanSensitivityNew = getMeanSensitivity();
        double lastSensitivityMeasurementNew = getLastSensitivity();
        this.meanSensitivity = meanSensitivityNew;

        firePropertyChange(SENSITIVITY_MEASUREMENT_COUNT, measurementCountOld, measurementCountOld + 1);
        firePropertyChange(MEAN_SENSITIVITY, meanSensitivityOld, meanSensitivityNew);
        firePropertyChange(LAST_SENSITIVITY_MEASUREMENT, lastSensitivityMeasurementOld, lastSensitivityMeasurementNew);

        checkIfButtonsEnabled();
    }

    public double getMeanSensitivity()
    {
        double mean = DescriptiveStatistics.arithmeticMean(sensitivityMeasurements);
        return mean;
    }

    public double getCurrentSensitivity()
    {
        return currentSensitivity;
    }

    public double getLastSensitivity()
    {
        int n = sensitivityMeasurements.size();
        double lastSensitivity = (n == 0) ?  Double.NaN : sensitivityMeasurements.get(n - 1);
        return lastSensitivity;
    }

    public int getSensitivityMeansurementCount()
    {
        return sensitivityMeasurements.size();
    }

    public List<Double> getSensitivtyMeasurements()
    {
        return sensitivityMeasurements;
    }

    public void apply()
    {
        addCurrentMeasurement();
        next();
    }

    public void next()
    {        
        removeCurrentSource();       
        checkIfButtonsEnabled();
    }

    public void clear()
    {        
        clearMeasurements();
        clearSources();

        checkIfButtonsEnabled();
    }

    private void clearMeasurements()
    {
        double meanSensitivityOld = meanSensitivity;
        int measurementCount = sensitivityMeasurements.size();

        sensitivityMeasurements.clear();

        this.meanSensitivity = Double.NaN;

        firePropertyChange(SENSITIVITY_MEASUREMENT_COUNT, measurementCount, 0);
        firePropertyChange(MEAN_SENSITIVITY, meanSensitivityOld,  Double.NaN);

        setSensitivity(Double.NaN);
        checkIfButtonsEnabled();
    }

    private void clearSources()
    {
        setSourcesWithoutChecking(new ArrayList<SimpleSpectroscopySource>());
        setCurrentSource(null);
    }

    private void checkIfButtonsEnabled()
    {
        checkIfApplyEnabled();
        checkIfNextEnabled();
        checkIfFinishEnabled();
        checkIfClearEnabled();
    }

    private void checkIfApplyEnabled()
    {
        boolean applyEnabledNew = (currentSource != null);
        setApplyEnabled(applyEnabledNew);
    }

    private void checkIfNextEnabled()
    {
        int n = calibrationSources.size();
        boolean nextEnabledNew = (n>1) || (currentSource == null && n>0);
        setNextEnabled(nextEnabledNew);
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledNew = (!Double.isNaN(meanSensitivity));
        setFinishEnabled(finishEnabledNew);
    }	

    private void checkIfClearEnabled()
    {
        boolean clearEnabledNew = (calibrationSources.size()>0 ) || (currentSource != null) || (!Double.isNaN(meanSensitivity)) || (!Double.isNaN(getLastSensitivity()));
        setClearEnabled(clearEnabledNew);
    }

    public File getCommonSourceDirectory() 
    {
        return BatchUtilities.findLastCommonSourceDirectory(calibrationSources);
    }

    @Override
    public void setSelectedROIs(Channel2DSource<?> image, Channel2D channel, List<ROI> substrate) 
    {        
        List<Shape> shapes = new ArrayList<>();

        for(ROI roi : substrate )
        {
            shapes.add(roi.getROIShape());
        }

        filterSources(shapes);
    }

    private void checkIfSubstrateSelectionEnabled()
    {
        boolean enabledNew = false;

        for(SimpleSpectroscopySource source : calibrationSources)
        {
            enabledNew = enabledNew || source.isFromMap();

            if(enabledNew)
            {
                break;
            }
        }

        setSubstrateSelectionEnabled(enabledNew);
    }

    private void setSensitivity(double sensitivityNew)
    {
        double sensitivityOld = currentSensitivity;
        this.currentSensitivity = sensitivityNew;

        firePropertyChange(LAST_SENSITIVITY_MEASUREMENT, sensitivityOld, sensitivityNew);

    }

    private double calculateSensitivity(Range range)
    {
        if(range == null)
        {
            return Double.NaN;
        }

        double x1 = range.getLowerBound();
        double x2 = range.getUpperBound();

        double y1 = getYValue(x1);
        double y2 = getYValue(x2);

        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);
        double s = dx/dy; 

        return s;
    } 

    public Range calculateCurrentMaximumRange()
    {
        Channel1D channel = getCurrentPhotodiodeCurve();

        Range range = null;
        if(channel != null)
        {
            range = channel.getXRange();
        }

        return range;
    }

    private double getYValue(double x)
    {   
        double y = Double.NaN;
        Channel1D curve = getCurrentPhotodiodeCurve();
        if(curve != null)
        {
            y = ArrayUtilities.getBestValue(curve.getPoints(), x);      
        }


        return y;
    }

    private Channel1D getCurrentPhotodiodeCurve()
    {
        Channel1D curve = null;
        if(currentSource != null)
        {
            try {
                curve = ForceCurveBranch.APPROACH.equals(forceCurveBranch) ?
                        currentSource.getRecordedPhotodiodeApproachCurve(Double.NaN, Double.NaN) : currentSource.getRecordedPhotodiodeWithdrawCurve(Double.NaN, Double.NaN);

            } catch (UserCommunicableException e) {
                e.printStackTrace();
            }
        }


        return curve;
    }

    public Range getRange()
    {
        return currentRange;
    }

    public void setRange(Range range)
    {
        Range oldRange = this.currentRange;
        this.currentRange = range;

        firePropertyChange(CURRENT_RANGE, oldRange, this.currentRange);

        double sensitivity = calculateSensitivity(currentRange);
        setSensitivity(sensitivity);
    }

    public void requestLowerRangeBound(double lowerBound)
    {
        double upperBound = currentRange.getUpperBound();       
        double lowerBoundAdjusted = Math.min(Math.max(lowerBound, currentMaximumRange.getLowerBound()), upperBound - minRangeWidth);

        Range rangeNew = new Range(lowerBoundAdjusted, upperBound);
        setRange(rangeNew);
    }

    public void requestUpperRangeBound(double upperBound)
    {
        double lowerBound = currentRange.getLowerBound();

        double upperBoundAdjusted = Math.max(Math.min(upperBound, currentMaximumRange.getUpperBound()), lowerBound + minRangeWidth);

        Range rangeNew = new Range(lowerBound, upperBoundAdjusted);
        setRange(rangeNew);
    }

    public boolean isApplyEnabled()
    {
        return applyEnabled;
    }

    private void setApplyEnabled(boolean enabledNew)
    {
        boolean enabledOld = applyEnabled;
        applyEnabled = enabledNew;

        firePropertyChange(APPLY_ENABLED, enabledOld, enabledNew);
    }

    public boolean isNextEnabled()
    {
        return nextEnabled;
    }

    private void setNextEnabled(boolean enabledNew)
    {
        boolean enabledOld = nextEnabled;
        this.nextEnabled = enabledNew;

        firePropertyChange(NEXT_ENABLED, enabledOld, enabledNew);
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    private void setFinishEnabled(boolean enabledNew)
    {
        boolean enabledOld = finishEnabled;
        this.finishEnabled = enabledNew;

        firePropertyChange(FINISH_ENABLED, enabledOld, enabledNew);
    }

    public boolean isClearEnabled()
    {
        return clearEnabled;
    }

    private void setClearEnabled(boolean enabledNew)
    {
        boolean enabledOld = clearEnabled;
        this.clearEnabled = enabledNew;

        firePropertyChange(CLEAR_ENABLED, enabledOld, enabledNew);
    }
}
