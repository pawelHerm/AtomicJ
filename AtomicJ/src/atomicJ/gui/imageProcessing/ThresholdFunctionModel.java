
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

package atomicJ.gui.imageProcessing;

import org.jfree.data.Range;

import atomicJ.data.Channel2D;
import atomicJ.data.ChannelFilter2;
import atomicJ.data.units.UnitExpression;
import atomicJ.functions.ThresholdFunction;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.ReplaceDataTransformation;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.BasicChannel2DResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.GeometryUtilities;

public class ThresholdFunctionModel extends ImageBatchROIProcessingModel
{
    private static final double TOLERANCE = 1e-10;

    public static final String LOWER_VALUE = "LowerValue";
    public static final String UPPER_VALUE = "UpperValue";
    public static final String LOWER_THRESHOLD = "LowerThreshold";
    public static final String UPPER_THRESHOLD = "UpperThreshold";
    public static final String USE_UPPER_THRSHOLD = "UseUpperThreshold";
    public static final String USE_LOWER_THRESHOLD = "UseLowerThreshold";

    private final Range channelDataRange;

    private UnitExpression lowerValue;
    private UnitExpression upperValue;
    private UnitExpression lowerThreshold;
    private UnitExpression upperThreshold;

    private boolean useLowerThreshold = true;
    private boolean useUpperThreshold = true;

    public ThresholdFunctionModel(ResourceView<Channel2DResource, Channel2D, String> manager, ChannelFilter2<Channel2D> channelFilter)
    {
        super(manager, channelFilter, true, true);

        BasicChannel2DResource resource = manager.getSelectedResource();
        String type = manager.getSelectedType();

        this.channelDataRange = resource.getChannelDataRange(type);

        this.lowerThreshold = new UnitExpression(channelDataRange.getLowerBound(), getDataUnit()).derive(getValueAxisDisplayedUnit());
        this.upperThreshold = new UnitExpression(channelDataRange.getUpperBound(), getDataUnit()).derive(getValueAxisDisplayedUnit());

        this.lowerValue = new UnitExpression(0, getValueAxisDisplayedUnit());
        this.upperValue = new UnitExpression(0, getValueAxisDisplayedUnit());
    }

    public boolean isUseLowerThreshold()
    {
        return useLowerThreshold;
    }

    public void setUseLowerThreshold(boolean useLowerThresholdNew)
    {
        if(this.useLowerThreshold != useLowerThresholdNew)
        {
            boolean useLowerThresholdOld = this.useLowerThreshold;
            this.useLowerThreshold = useLowerThresholdNew;

            firePropertyChange(USE_LOWER_THRESHOLD, useLowerThresholdOld, useLowerThresholdNew);

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public boolean isUseUpperThreshold()
    {
        return useUpperThreshold;
    }

    public void setUseUpperThreshold(boolean useUpperThresholdNew)
    {
        if(this.useUpperThreshold != useUpperThresholdNew)
        {
            boolean useUpperThresholdOld = this.useUpperThreshold;
            this.useUpperThreshold = useUpperThresholdNew;

            firePropertyChange(USE_UPPER_THRSHOLD, useUpperThresholdOld, useUpperThresholdNew);

            checkIfApplyEnabled();
            updatePreview();
        }
    }

    public UnitExpression getLowerValue()
    {
        return lowerValue;
    }

    public void setLowerValue(UnitExpression lowerValueNew)
    {
        if(!GeometryUtilities.almostEqual(this.lowerValue, lowerValueNew, TOLERANCE))
        {
            UnitExpression lowerValueOld = this.lowerValue;
            this.lowerValue = lowerValueNew;

            firePropertyChange(LOWER_VALUE, lowerValueOld, lowerValueNew);          

            checkIfApplyEnabled();
            updatePreview();
        }    
    }

    public UnitExpression getLowerThreshold()
    {
        return lowerThreshold;
    }

    public void setLowerThreshold(UnitExpression lowerThresholdNew)
    {
        if(!GeometryUtilities.almostEqual(this.lowerThreshold, lowerThresholdNew, TOLERANCE))
        {
            UnitExpression lowerThresholdOld = this.lowerThreshold;
            this.lowerThreshold = lowerThresholdNew;

            firePropertyChange(LOWER_THRESHOLD, lowerThresholdOld, lowerThresholdNew);   

            checkIfApplyEnabled();
            updatePreview();
        }    
    }

    public UnitExpression getUpperValue()
    {
        return upperValue;
    }

    public void setUpperValue(UnitExpression upperValueNew)
    {
        if(!GeometryUtilities.almostEqual(this.upperValue, upperValueNew, TOLERANCE))
        {
            UnitExpression upperValueOld = this.upperValue;
            this.upperValue = upperValueNew;

            firePropertyChange(UPPER_VALUE, upperValueOld, upperValueNew);  

            checkIfApplyEnabled();
            updatePreview();
        }    
    }

    public UnitExpression getUpperThreshold()
    {
        return upperThreshold;
    }

    public void setUpperThreshold(UnitExpression upperThresholdNew)
    {
        if(!GeometryUtilities.almostEqual(this.upperThreshold, upperThresholdNew, TOLERANCE))
        {
            UnitExpression upperThresholdOld = this.upperThreshold;
            this.upperThreshold = upperThresholdNew;

            firePropertyChange(UPPER_THRESHOLD, upperThresholdOld, upperThresholdNew); 

            checkIfApplyEnabled();
            updatePreview();
        }    
    }

    protected ThresholdFunction getFunction()
    {
        double lowerThresholdInDataUnits = useLowerThreshold ? lowerThreshold.derive(getDataUnit()).getValue() : Double.NEGATIVE_INFINITY;
        double lowerValueInDataUnits = lowerValue.derive(getDataUnit()).getValue();
        double upperThresholdInDataUnits = useUpperThreshold ? upperThreshold.derive(getDataUnit()).getValue() : Double.POSITIVE_INFINITY;
        double upperValueInDataUnits = upperValue.derive(getDataUnit()).getValue();

        ThresholdFunction f = new ThresholdFunction(lowerThresholdInDataUnits, lowerValueInDataUnits, upperThresholdInDataUnits, upperValueInDataUnits);

        return f;
    }

    public double getDataMinimum() 
    {
        return channelDataRange.getLowerBound();
    }

    public double getDataMaximum() 
    {
        return channelDataRange.getUpperBound();
    }

    @Override
    protected boolean calculateApplyEnabled()
    {
        boolean applyEnabled = super.calculateApplyEnabled() && !(useLowerThreshold && Double.isNaN(lowerValue.getValue())) && !(useLowerThreshold && ( lowerThreshold == null || Double.isNaN(lowerThreshold.getValue())))
                && !(useUpperThreshold && Double.isNaN(upperValue.getValue())) && !(useUpperThreshold && (upperThreshold == null || Double.isNaN(upperThreshold.getValue())));

        return applyEnabled;
    }

    @Override
    protected Channel2DDataInROITransformation buildTransformation() 
    {
        if(!isApplyEnabled())
        {
            return null;
        }

        Channel2DDataInROITransformation tr = new ReplaceDataTransformation(getFunction(), true);
        return tr;
    }
}
