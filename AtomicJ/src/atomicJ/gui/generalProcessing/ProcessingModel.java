
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

package atomicJ.gui.generalProcessing;

import java.util.Map;
import java.util.Objects;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.imageProcessing.UnitManager;
import atomicJ.gui.rois.ROI;

public abstract class ProcessingModel extends OperationModel
{
    public static final String VALUE_AXIS_DISPLAYED_UNIT = "ValueAxisDisplayedUnit";

    private final PrefixedUnit dataUnit;
    private PrefixedUnit displayedUnit;


    public ProcessingModel(Map<Object, ROI> rois, ROI roiUnion, UnitManager valueUnitManager)
    {
        super(rois, roiUnion);

        this.dataUnit = valueUnitManager.getSingleDataUnit();
        this.displayedUnit = valueUnitManager.getSingleDisplayedUnit();
    }

    public final PrefixedUnit getDataUnit()
    {
        return dataUnit;
    }

    public final PrefixedUnit getValueAxisDisplayedUnit()
    {
        return displayedUnit;
    }

    public void setValueAxisDisplayedUnit(PrefixedUnit displayedUnitNew)
    {
        if(!Objects.equals(this.displayedUnit, displayedUnitNew))
        {
            PrefixedUnit displayedUnitOld = this.displayedUnit;
            this.displayedUnit = displayedUnitNew;

            firePropertyChange(VALUE_AXIS_DISPLAYED_UNIT, displayedUnitOld, displayedUnitNew);
        }
    }

    public double getDataToDisplayedConversionFactor()
    {
        double conversionFactor = dataUnit.getConversionFactorTo(displayedUnit);
        return conversionFactor;
    }

    public double getDisplayedToDataConversionFactor()
    {
        double conversionFactor = displayedUnit.getConversionFactorTo(dataUnit);
        return conversionFactor;
    }
}
