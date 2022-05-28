
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

import atomicJ.data.Channel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.Datasets;
import atomicJ.data.units.Quantity;

public class GradientStackFunction implements ProcessedStackingPackFunction
{
    private final double depth;

    public GradientStackFunction(double depth)
    {
        this.depth = depth;
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

        int n = forceIndentationChannel.getItemCount();
        int index = (forceIndentationChannel.getX(n - 1) > depth) ? forceIndentationChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(depth) : n - 1;


        int indexIncremented = Math.min(index + 1, n - 1);

        double indentStart = forceIndentationChannel.getX(index);
        double fStart = forceIndentationChannel.getY(index);

        double indentEnd = forceIndentationChannel.getX(indexIncremented);
        double fEnd = forceIndentationChannel.getY(indexIncremented);

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
        return Quantities.INDENTATION_MICRONS;
    }

    public static class GradientStackFunctionFactory implements StackingPackFunctionFactory<GradientStackFunction>
    {
        private static final String stackType = Datasets.FORCE_GRADIENT_STACK;

        @Override
        public GradientStackFunction getFunction(double level) 
        {
            return new GradientStackFunction(level);
        }

        @Override
        public Quantity getEvaluatedQuantity() 
        {
            return Quantities.FORCE_GRADIENTS_NANONEWTONS_MICROMETERS;
        }

        @Override
        public Quantity getStackingQuantity() 
        {
            return Quantities.INDENTATION_MICRONS;
        }

        @Override
        public String getStackType() 
        {
            return stackType;
        }
    }
}
