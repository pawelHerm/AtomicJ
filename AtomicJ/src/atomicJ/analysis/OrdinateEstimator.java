
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

package atomicJ.analysis;

import java.awt.geom.Point2D;

import atomicJ.data.Channel1DData;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;

public final class OrdinateEstimator implements ContactEstimator
{	
    private final UnitExpression zPosition;

    public OrdinateEstimator(UnitExpression zPosition)
    {
        this.zPosition = zPosition;
    }

    @Override
    public double[] getContactPoint(Channel1DData deflectionCurveBranch, Point2D recordingPosition, double springConstant) //data must be segregated according to x coordinate (descending order)
    {   
        PrefixedUnit branchUnit = deflectionCurveBranch.getXQuantity().getUnit();
        double zPositionValIntBranchUnits = zPosition.derive(branchUnit).getValue();

        int index = deflectionCurveBranch.getIndexWithinDataBoundsOfItemWithXClosestTo(zPositionValIntBranchUnits);
        return deflectionCurveBranch.getPoint(index);
    }

    @Override
    public boolean isAutomatic()
    {
        return false;
    }
}
