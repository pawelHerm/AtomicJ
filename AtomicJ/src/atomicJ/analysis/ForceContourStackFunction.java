
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

import atomicJ.data.Channel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.Datasets;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.ArrayUtilities;

public class ForceContourStackFunction implements ProcessedStackingPackFunction
{
    private final double force;

    public ForceContourStackFunction(double force)
    {
        this.force = force;
    }

    public static double getIndentation(double[][] forceIndentation, double force)
    {
        int index = ArrayUtilities.getIndexOfPointWithYCoordinateClosestTo(forceIndentation, force);
        return forceIndentation[index][0];
    }

    public static double getIndentation(double[] forceIndentationYs, double[] forceIndentationXs, double force)
    {
        int index = ArrayUtilities.getIndexOfValueClosestTo(forceIndentationYs, force);   
        return forceIndentationXs[index];
    }

    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {
        if(pack == null)
        {
            return 0;
        }

        Channel1DData forceIndentationChannel = pack.getForceIndentationData();
        double[][] forceIndentation = forceIndentationChannel.getPoints();

        int index = -1;
        double distance = Double.POSITIVE_INFINITY;		

        if(forceIndentation[forceIndentation.length - 1][1]>force)
        {
            for(int i = 0; i<forceIndentation.length; i++)
            {
                double[] p = forceIndentation[i];
                double distanceCurrent = Math.abs(p[1] - force);
                if(distanceCurrent<distance)
                {
                    index = i;
                    distance = distanceCurrent;
                }

            }
        }
        else 
        {
            index = forceIndentation.length - 1;
        }

        return forceIndentation[index][0];

    }

    /*
     *     public double evaluate(ProcessedPack pack) 
    {
        double result = 0;
        if(pack == null)
        {
            return 0;
        }
        double[][] forceIndentation = pack.getForceIndentationData();

        int index = -1;

        if(forceIndentation[forceIndentation.length - 1][1]>force)
        {
            for(int i = 1; i<forceIndentation.length; i++)
            {
                double[] pMax = forceIndentation[i];
                double forceMax = pMax[1];
                if(forceMax>force)
                {
                    double indentMax = pMax[0];

                    double[] pMin = forceIndentation[i - 1];
                    double forceMin = pMin[1];
                    double indentMin = pMin[0];
                    result = indentMin + ((force - forceMin)/(forceMax - forceMin))*indentMax;
                    break;
                }

            }
        }
        else 
        {
            result = forceIndentation[forceIndentation.length - 1][0];
        }

        return result;

    }
     */

    @Override
    public Quantity getEvaluatedQuantity() 
    {
        return Quantities.INDENTATION_MICRONS;
    }

    @Override
    public Quantity getStackingQuantity() 
    {
        return Quantities.FORCE_NANONEWTONS;
    }

    public static class IndentationStackFunctionFactory implements StackingPackFunctionFactory<ForceContourStackFunction>
    {
        private static final String stackType = Datasets.FORCE_CONTOUR_MAPPING;

        @Override
        public ForceContourStackFunction getFunction(double level) 
        {
            return new ForceContourStackFunction(level);
        }

        @Override
        public Quantity getEvaluatedQuantity() {
            return Quantities.INDENTATION_MICRONS;
        }

        @Override
        public Quantity getStackingQuantity() 
        {
            return Quantities.FORCE_NANONEWTONS;
        }

        @Override
        public String getStackType() 
        {
            return stackType;
        }	
    }
}
