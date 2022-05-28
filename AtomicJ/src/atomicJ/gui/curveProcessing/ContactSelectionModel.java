
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

import static atomicJ.data.Quantities.FORCE_NANONEWTONS;
import static atomicJ.data.Quantities.INDENTATION_MICRONS;
import static atomicJ.data.Quantities.POINTWISE_MODULUS_KPA;
import static atomicJ.data.Datasets.INDENTATION;
import static atomicJ.data.Datasets.INDENTATION_DATA;
import static atomicJ.data.Datasets.POINTWISE_MODULUS;
import static atomicJ.data.Datasets.POINTWISE_MODULUS_DATA;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import atomicJ.analysis.ContactEstimator;
import atomicJ.analysis.ProcessableSpectroscopyPack;
import atomicJ.analysis.StandardSpectroscopyProcessor;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.Datasets;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.IndentationCurve;
import atomicJ.data.Point1DData;
import atomicJ.data.PointwiseModulusCurve;
import atomicJ.data.Quantities;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.ManualEstimatorType;
import atomicJ.utilities.Validation;


public class ContactSelectionModel extends AbstractModel
{
    static final String LIVE_PREVIEW = "LivePreview";
    static final String APPLY_TO_ALL_ENABLED = "ApplyToAllEnabled";
    static final String APPLY_ENABLED = "ApplyEnabled";
    static final String SKIP_ENABLED = "SkipEnabled";
    static final String FINISH_ENABLED = "FinishEnabled";
    static final String ESTIMATOR_TYPE = "EstimatorType";
    static final String CURRENT_PACK = "CurrentPack";
    static final String PACKS_STILL_TO_PROCESS = "PacksStillToProcess";
    static final String CURRENT_PACK_INDEX = "CurrentPackIndex";
    static final String SELECTED_X = "SelectedX";
    static final String SELECTED_Y = "SelectedY";

    private final List<ProcessableSpectroscopyPack> allPacks;
    private final List<ProcessableSpectroscopyPack> packsStillToProcess;
    private final List<ProcessableSpectroscopyPack> finishedPacks;

    private ProcessableSpectroscopyPack currentPack;
    private ManualEstimatorType estimatorType = ManualEstimatorType.POINT_ESTIMATOR;

    private UnitExpression selectedX = new UnitExpression(Double.NaN, Units.MICRO_METER_UNIT);
    private UnitExpression selectedY = new UnitExpression(Double.NaN, Units.NANO_NEWTON_UNIT);

    private boolean applyToAllEnabled = false;
    private boolean applyEnabled = false;
    private boolean skipEnabled = true;
    private boolean finishEnabled = false;


    public ContactSelectionModel(List<ProcessableSpectroscopyPack> packs)
    {
        Validation.requireNonNullAndNonEmptyParameterName(packs, "packs");

        this.allPacks = packs;
        this.packsStillToProcess = new ArrayList<>(packs);
        this.finishedPacks = new ArrayList<>();

        this.currentPack = allPacks.get(0);
    }

    public boolean isApplyEnabled()
    {
        return applyEnabled;
    }

    public void setApplyEnabled(boolean applyEnabledNew)
    {
        if(this.applyEnabled != applyEnabledNew)
        {
            boolean applyEnabledOld = applyEnabled;
            this.applyEnabled = applyEnabledNew;

            firePropertyChange(APPLY_ENABLED, applyEnabledOld, applyEnabledNew);
        }
    }

    public boolean isApplyToAllEnabled()
    {
        return applyToAllEnabled;
    }

    public void setApplyToAllEnabled(boolean applyToAllEnabledNew)
    {
        if(this.applyToAllEnabled != applyToAllEnabledNew)
        {
            boolean applyToAllEnabledOld = applyToAllEnabled;
            this.applyToAllEnabled = applyToAllEnabledNew;

            firePropertyChange(APPLY_TO_ALL_ENABLED, applyToAllEnabledOld, applyToAllEnabledNew);
        }
    }

    public boolean isSkipEnabled()
    {
        return skipEnabled;
    }

    public void setSkipEnabled(boolean skipEnabledNew)
    {
        if(this.skipEnabled != skipEnabledNew)
        {
            boolean skipEnabledOld = skipEnabled;
            this.skipEnabled = skipEnabledNew;

            firePropertyChange(SKIP_ENABLED, skipEnabledOld, skipEnabledNew);
        }
    }

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    public void setFinishEnabled(boolean enabledNew)
    {
        if(this.finishEnabled != enabledNew)
        {
            boolean enabledOld = finishEnabled;
            this.finishEnabled = enabledNew;

            firePropertyChange(FINISH_ENABLED, enabledOld, enabledNew);
        }
    }

    public ManualEstimatorType getEstimatorType()
    {
        return estimatorType;
    }

    public void setEstimatorType(ManualEstimatorType estimatorTypeNew)
    {
        if(!Objects.equals(this.estimatorType, estimatorTypeNew))
        {
            ManualEstimatorType estimatorTypeOld = estimatorType;
            this.estimatorType = estimatorTypeNew;

            firePropertyChange(ESTIMATOR_TYPE, estimatorTypeOld, estimatorTypeNew);

            checkIfAppliesEnabled();
        }
    }

    public ProcessableSpectroscopyPack getCurrentPack()
    {
        return currentPack;
    }

    public List<ProcessableSpectroscopyPack> getPacks()
    {
        List<ProcessableSpectroscopyPack> packsCopy = new ArrayList<>(allPacks);

        return packsCopy;
    }

    public void setCurrentPack(ProcessableSpectroscopyPack packNew)
    {
        int indexOld = allPacks.indexOf(currentPack);
        int indexNew = allPacks.indexOf(packNew);

        if(indexNew > -1 )
        {
            ProcessableSpectroscopyPack packOld = currentPack;
            this.currentPack = packNew;

            UnitExpression selectedXOld = selectedX;
            this.selectedX = null;

            UnitExpression selectedYOld = selectedY;
            this.selectedX = null;                          

            checkIfAppliesEnabled();

            firePropertyChange(SELECTED_X, selectedXOld, this.selectedX);	
            firePropertyChange(SELECTED_Y, selectedYOld, this.selectedY); 

            firePropertyChange(CURRENT_PACK, packOld, packNew);
            firePropertyChange(CURRENT_PACK_INDEX, indexOld, indexNew);

        }
        else
        {
            checkIfAppliesEnabled();
        }

    }

    public int getCurrentPackIndex()
    {
        int index = allPacks.indexOf(currentPack);
        return index;
    }

    public UnitExpression getX()
    {
        return selectedX;
    }

    public void setX(UnitExpression selectedXNew)
    {
        if(!Objects.equals(this.selectedX, selectedXNew))
        {
            UnitExpression selectedXOld = this.selectedX;
            this.selectedX = selectedXNew;

            checkIfAppliesEnabled();
            firePropertyChange(SELECTED_X, selectedXOld, selectedXNew);	
        }
    }

    public UnitExpression getY()
    {
        return selectedY;
    }


    public void setY(UnitExpression selectedYNew)
    {
        if(!Objects.equals(this.selectedY, selectedYNew))
        {
            UnitExpression selectedYOld = this.selectedY;
            this.selectedY = selectedYNew;

            checkIfAppliesEnabled();
            firePropertyChange(SELECTED_Y, selectedYOld, selectedYNew);  
        }
    }

    public List<ProcessableSpectroscopyPack> getFinishedProcessablePacks()
    {
        return finishedPacks;
    }

    private void removePack(ProcessableSpectroscopyPack processablePack)
    {
        List<ProcessableSpectroscopyPack> packs = new ArrayList<>();
        packs.add(processablePack);

        removePacks(packs);
    }

    private void removePacks(List<ProcessableSpectroscopyPack> processedPacks)
    {
        List<ProcessableSpectroscopyPack> packsStillToProcessOld = new ArrayList<>(packsStillToProcess);
        packsStillToProcess.removeAll(processedPacks);

        firePropertyChange(PACKS_STILL_TO_PROCESS, packsStillToProcessOld, new ArrayList<>(packsStillToProcess));
    }

    private void update()
    {
        if(packsStillToProcess.isEmpty())
        {
            setFinishEnabled(true);
            setApplyEnabled(false);
            setApplyToAllEnabled(false);
            setSkipEnabled(false);
        }
        else
        {
            ProcessableSpectroscopyPack currentPackNew = packsStillToProcess.get(0);
            setCurrentPack(currentPackNew);
        }
    }

    public ProcessableSpectroscopyPack buildProcessablePack(ProcessableSpectroscopyPack pack)
    {
        double k = pack.getProcessingSettings().getSpringConstant();
        UnitExpression deflectionY = selectedY.divide(new UnitExpression(k, Units.NEWTON_PER_METER));

        ContactEstimator estimator = estimatorType.getContactEstimator(selectedX, deflectionY);
        pack.setContactEstimator(estimator);

        return pack;
    }

    public void apply()
    {
        ProcessableSpectroscopyPack processablePack = buildProcessablePack(currentPack);
        finishedPacks.add(processablePack);
        removePack(currentPack);

        update();
    }

    public void applyToAll()
    {
        for(ProcessableSpectroscopyPack pack: packsStillToProcess)
        {
            double k = pack.getProcessingSettings().getSpringConstant();
            UnitExpression deflectionY = selectedY.divide(new UnitExpression(k, Units.NEWTON_PER_METER));

            ContactEstimator estimator = estimatorType.getContactEstimator(selectedX, deflectionY);

            pack.setContactEstimator(estimator);
            finishedPacks.add(pack);
        }

        removePacks(packsStillToProcess);

        update();
    }

    public void skip()
    {
        removePack(currentPack);
        update();
    }

    public void cancel()
    {
        finishedPacks.clear();
    }

    public LivePreviewPack getLivePreviewPack()
    {        
        if(applyEnabled)
        {
            ProcessableSpectroscopyPack processable = buildProcessablePack(currentPack);
            LivePreviewPack pack = StandardSpectroscopyProcessor.getInstance().getPreviewPack(processable);

            return pack;
        }

        Channel1D transitionPointIndentation = new Channel1DStandard(new Point1DData(Double.NaN, Double.NaN, INDENTATION_MICRONS, FORCE_NANONEWTONS),Datasets.MODEL_TRANSITION_POINT_INDENTATION_CURVE, Datasets.MODEL_TRANSITION_POINT);                                          
        IndentationCurve indentationCurve = new IndentationCurve(new Channel1DStandard(FlexibleChannel1DData.getEmptyInstance(Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS),INDENTATION_DATA, INDENTATION), transitionPointIndentation); 

        Channel1D transitionPointPointwiseModulus = new Channel1DStandard(new Point1DData(Double.NaN, Double.NaN, INDENTATION_MICRONS, POINTWISE_MODULUS_KPA), Datasets.MODEL_TRANSITION_POINT_POINTWISE_MODULUS, Datasets.MODEL_TRANSITION_POINT);
        PointwiseModulusCurve pointwiseModulusCurve = new PointwiseModulusCurve(new Channel1DStandard(FlexibleChannel1DData.getEmptyInstance(Quantities.INDENTATION_MICRONS, Quantities.POINTWISE_MODULUS_KPA), POINTWISE_MODULUS_DATA, POINTWISE_MODULUS),transitionPointPointwiseModulus);

        return new LivePreviewPack(indentationCurve, pointwiseModulusCurve, Double.NaN, Double.NaN, Double.NaN);

    }

    private void checkIfAppliesEnabled()
    {
        boolean isXUnknown = selectedX == null || Double.isNaN(selectedX.getValue());
        boolean isYUnknown = selectedY == null || Double.isNaN(selectedY.getValue());

        boolean enabled = estimatorType != null && !((isXUnknown && estimatorType.isDistanceRequired()) 
                || isYUnknown && estimatorType.isDeflectionRequired());

        setApplyEnabled(enabled);
        setApplyToAllEnabled(enabled);
    }
}
