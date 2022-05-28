
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
import atomicJ.data.GridChannel1DData;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.utilities.ArrayUtilities;


public final class AbscissaEstimator implements ContactEstimator 
{	
    private final UnitExpression deflection;

    public AbscissaEstimator(UnitExpression deflection)
    {
        this.deflection = deflection;
    }

    @Override
    public double[] getContactPoint(Channel1DData deflectionCurveBranch, Point2D recordingPosition, double springConstant) //data must be segregated according to x coordinate (descending order)
    {
        if(deflectionCurveBranch instanceof GridChannel1DData)
        {
            return getContactPointGridChannel1DData((GridChannel1DData) deflectionCurveBranch, recordingPosition, springConstant);
        }
        if(deflectionCurveBranch instanceof SinusoidalChannel1DData)
        {
            return getContactPointSinusoidalChannel1DData((SinusoidalChannel1DData) deflectionCurveBranch, recordingPosition, springConstant);
        }

        double[][] points = deflectionCurveBranch.getPoints();

        PrefixedUnit branchUnit = deflectionCurveBranch.getYQuantity().getUnit();
        double delfectionValIntBranchUnits = deflection.derive(branchUnit).getValue();

        int index = ArrayUtilities.getIndexOfPointWithYCoordinateClosestTo(points, delfectionValIntBranchUnits);

        return deflectionCurveBranch.getPoint(index);
    }

    private double[] getContactPointGridChannel1DData(GridChannel1DData deflectionCurveBranch, Point2D recordingPosition, double springConstant) //data must be segregated according to x coordinate (descending order)
    { 
        double[] data = deflectionCurveBranch.getData();

        PrefixedUnit branchUnit = deflectionCurveBranch.getYQuantity().getUnit();
        double delfectionValIntBranchUnits = deflection.derive(branchUnit).getValue();

        int index = ArrayUtilities.getIndexOfValueClosestTo(data, delfectionValIntBranchUnits);

        return deflectionCurveBranch.getPoint(index);
    }

    private double[] getContactPointSinusoidalChannel1DData(SinusoidalChannel1DData deflectionCurveBranch, Point2D recordingPosition, double springConstant) //data must be segregated according to x coordinate (descending order)
    { 
        double[] data = deflectionCurveBranch.getData();

        PrefixedUnit branchUnit = deflectionCurveBranch.getYQuantity().getUnit();
        double delfectionValIntBranchUnits = deflection.derive(branchUnit).getValue();

        int index = ArrayUtilities.getIndexOfValueClosestTo(data, delfectionValIntBranchUnits);

        return deflectionCurveBranch.getPoint(index);
    }

    @Override
    public boolean isAutomatic() {
        return false;
    }
}
