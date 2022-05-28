package atomicJ.gui.curveProcessing;


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


import static atomicJ.gui.curveProcessing.ProcessingBatchModel.PLOT_INDENTATION;
import static atomicJ.gui.curveProcessing.ProcessingBatchModel.PLOT_INDENTATION_FIT;
import static atomicJ.gui.curveProcessing.ProcessingBatchModel.PLOT_MODULUS;
import static atomicJ.gui.curveProcessing.ProcessingBatchModel.PLOT_MODULUS_FIT;
import static atomicJ.gui.curveProcessing.ProcessingBatchModel.PLOT_RECORDED_CURVE;
import static atomicJ.gui.curveProcessing.ProcessingBatchModel.PLOT_RECORDED_CURVE_FIT;

import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.analysis.VisualizationChartSettings;
import atomicJ.analysis.VisualizationSettings;
import atomicJ.gui.generalProcessing.OperationModel;
import atomicJ.gui.rois.ROI;

public class CurveReplottingModel extends OperationModel
{
    private final Preferences pref = Preferences.userNodeForPackage(CurveReplottingModel.class).node(CurveReplottingModel.class.getName());

    private boolean plotRecordedCurve;
    private boolean plotRecordedCurveFit;
    private boolean plotIndentation;
    private boolean plotIndentationFit;
    private boolean plotModulus;    
    private boolean plotModulusFit;

    private boolean applied;

    public CurveReplottingModel(Map<Object, ROI> rois, ROI roiUnion)
    {
        super(rois, roiUnion);
        initDefaults();
    }

    private void initDefaults()
    { 
        this.plotRecordedCurve = pref.getBoolean(PLOT_RECORDED_CURVE, true);
        this.plotRecordedCurveFit = pref.getBoolean(PLOT_RECORDED_CURVE_FIT, true);
        this.plotIndentation = pref.getBoolean(PLOT_INDENTATION, true);
        this.plotIndentationFit = pref.getBoolean(PLOT_INDENTATION_FIT, true);
        this.plotModulus = pref.getBoolean(PLOT_MODULUS, true); 
        this.plotModulusFit = pref.getBoolean(PLOT_MODULUS_FIT, true);  
    }

    public VisualizationSettings getVisualizationSettings()
    {
        VisualizationChartSettings recordedCurveSettings = new VisualizationChartSettings(plotRecordedCurve, plotRecordedCurveFit);
        VisualizationChartSettings indentationSettings = new VisualizationChartSettings(plotIndentation, plotIndentationFit);
        VisualizationChartSettings modulusSettings = new VisualizationChartSettings(plotModulus, plotModulusFit);

        return new VisualizationSettings(recordedCurveSettings, indentationSettings, modulusSettings);
    }

    public boolean isPlotRecordedCurve()
    {
        return plotRecordedCurve;
    }

    public void setPlotRecordedCurve(boolean plotRecordedCurveNew)
    {
        boolean plotRecordedCurveOld = this.plotRecordedCurve;
        this.plotRecordedCurve = plotRecordedCurveNew;

        firePropertyChange(PLOT_RECORDED_CURVE, plotRecordedCurveOld, plotRecordedCurveNew);

        checkIfApplyEnabled();

        pref.putBoolean(PLOT_RECORDED_CURVE, this.plotRecordedCurve);

        try
        {
            pref.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isPlotRecordedCurveFit()
    {
        return plotRecordedCurveFit;
    }

    public void setPlotRecordedCurveFit(boolean plotRecordedCurveFitNew)
    {
        boolean plotRecordedCurveFitOld = this.plotRecordedCurveFit;
        this.plotRecordedCurveFit = plotRecordedCurveFitNew;

        firePropertyChange(PLOT_RECORDED_CURVE_FIT, plotRecordedCurveFitOld, plotRecordedCurveFitNew);

        pref.putBoolean(PLOT_RECORDED_CURVE_FIT, this.plotRecordedCurveFit);

        try
        {
            pref.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isPlotIndentation()
    {
        return plotIndentation;
    }

    public void setPlotIndentation(boolean plotIndentationNew)
    {
        boolean plotIndentationOld = this.plotIndentation;
        this.plotIndentation = plotIndentationNew;

        firePropertyChange(PLOT_INDENTATION, plotIndentationOld, plotIndentationNew);

        checkIfApplyEnabled();

        pref.putBoolean(PLOT_INDENTATION, this.plotIndentation);

        try
        {
            pref.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }


    public boolean isPlotIndentationFit()
    {
        return plotIndentationFit;
    }


    public void setPlotIndentationFit(boolean plotIndentationFitNew)
    {
        boolean plotIndentationFitOld = this.plotIndentationFit;
        this.plotIndentationFit = plotIndentationFitNew;

        firePropertyChange(PLOT_INDENTATION_FIT, plotIndentationFitOld, plotIndentationFitNew);

        pref.putBoolean(PLOT_INDENTATION_FIT, this.plotIndentationFit);

        try
        {
            pref.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isPlotModulus()
    {
        return plotModulus;
    }

    public void setPlotModulus(boolean plotModulusNew)
    {
        boolean plotModulusOld = this.plotModulus;
        this.plotModulus = plotModulusNew;

        firePropertyChange(PLOT_MODULUS, plotModulusOld, plotModulusNew);

        checkIfApplyEnabled();

        pref.putBoolean(PLOT_MODULUS, this.plotModulus);

        try
        {
            pref.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isPlotModulusFit()
    {
        return plotModulusFit;
    }

    public void setPlotModulusFit(boolean plotModulusFitNew)
    {
        boolean plotModulusFitOld = this.plotModulusFit;
        this.plotModulusFit = plotModulusFitNew;

        firePropertyChange(PLOT_MODULUS_FIT, plotModulusFitOld, plotModulusFitNew);

        pref.putBoolean(PLOT_MODULUS_FIT, this.plotModulusFit);

        try
        {
            pref.flush();
        } catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && (plotRecordedCurve || plotIndentation || plotModulus);
        return applyEnabled;
    }

    @Override
    public boolean isApplied()
    {
        return applied;
    }

    @Override
    public void apply()
    {
        this.applied = true;
    }
}

