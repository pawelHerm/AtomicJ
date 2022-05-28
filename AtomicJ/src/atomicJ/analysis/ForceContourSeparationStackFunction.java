
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

public class ForceContourSeparationStackFunction implements ProcessedStackingPackFunction
{
    private final double sampleLowestPoint;
    private final double force;

    public ForceContourSeparationStackFunction(double force, double sampleHeighestPoint)
    {
        this.force = force;
        this.sampleLowestPoint = sampleHeighestPoint;
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

        double contactPointDisplacement = pack.getResults().getContactDisplacement();
        double separation = (contactPointDisplacement - sampleLowestPoint) - forceIndentation[index][0];
        return separation;
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
        return Quantities.HEIGHT_MICRONS;
    }

    @Override
    public Quantity getStackingQuantity() 
    {
        return Quantities.FORCE_NANONEWTONS;
    }

    public static class IndentationStackFunctionFactory implements StackingPackFunctionFactory<ForceContourSeparationStackFunction>
    {
        private static final String stackType = Datasets.FORCE_CONTOUR_MAPPING;

        private final double sampleHeighestPoint;

        public IndentationStackFunctionFactory(double sampleHighestPoint)
        {
            this.sampleHeighestPoint = sampleHighestPoint;
        }

        @Override
        public ForceContourSeparationStackFunction getFunction(double level) 
        {
            return new ForceContourSeparationStackFunction(level, sampleHeighestPoint);
        }

        @Override
        public Quantity getEvaluatedQuantity() {
            return Quantities.HEIGHT_MICRONS;
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
