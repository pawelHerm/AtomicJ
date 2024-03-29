
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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

public class GradientSeparationStackFunction implements ProcessedStackingPackFunction
{
    private final double depth;  
    private final double sampleHeighestPoint;

    public GradientSeparationStackFunction(double depth, double sampleHeighestPoint)
    {
        this.depth = depth;
        this.sampleHeighestPoint = sampleHeighestPoint;
    }

    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {

        if(pack == null)
        {
            return 0;
        }

        Channel1DData forceIndentationChannel = pack.getForceIndentationData();

        if(forceIndentationChannel.isEmpty())
        {
            return Double.POSITIVE_INFINITY;
        }

        double[][] forceIndentation = forceIndentationChannel.getPoints();
        int n = forceIndentation.length;

        int index = -1;
        double distance = Double.POSITIVE_INFINITY;		

        double contactDisplacement = pack.getResults().getContactDisplacement();    
        double denivelation = (sampleHeighestPoint - contactDisplacement);

        if(denivelation>depth)
        {
            return 0;
        }

        if(forceIndentation[n - 1][0] + denivelation > depth)
        {
            for(int i = 0; i<n; i++)
            {
                double[] p = forceIndentation[i];
                double separation = p[0] + denivelation;

                double distanceCurrent = Math.abs(separation - depth);
                if(distanceCurrent<distance)
                {
                    index = i;
                    distance = distanceCurrent;
                }				
            }
        }
        else 
        {
            index = n - 1;
        }

        int indexIncremented = Math.min(index + 1, n - 1);

        double indentStart = forceIndentation[index][0];
        double fStart = forceIndentation[index][1];

        double indentEnd = forceIndentation[indexIncremented][0];
        double fEnd = forceIndentation[indexIncremented][1];

        double indentDiff = indentEnd - indentStart;
        double fDiff = fEnd - fStart;

        double gradient = fDiff/indentDiff;

        return gradient;
    }

    @Override
    public Quantity getEvaluatedQuantity() 
    {
        return Quantities.FORCE_GRADIENTS_NANONEWTONS_MICROMETERS;
    }

    @Override
    public Quantity getStackingQuantity() 
    {
        return Quantities.TIP_DISPLACEMENT_MICRONS;
    }

    public static class GradientSeparationStackFunctionFactory implements StackingPackFunctionFactory<GradientSeparationStackFunction>
    {
        private static final String stackType = Datasets.FORCE_GRADIENT_STACK;

        private final double sampleHeighestPoint;

        public GradientSeparationStackFunctionFactory(double sampleHeighestPoint)
        {
            this.sampleHeighestPoint = sampleHeighestPoint;
        }

        @Override
        public GradientSeparationStackFunction getFunction(double level) 
        {
            return new GradientSeparationStackFunction(level, sampleHeighestPoint);
        }

        @Override
        public Quantity getEvaluatedQuantity() {
            return Quantities.FORCE_GRADIENTS_NANONEWTONS_MICROMETERS;
        }

        @Override
        public Quantity getStackingQuantity() 
        {
            return Quantities.TIP_DISPLACEMENT_MICRONS;
        }

        @Override
        public String getStackType() 
        {
            return stackType;
        }
    }
}
