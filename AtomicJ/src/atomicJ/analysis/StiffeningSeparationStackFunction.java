
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

public class StiffeningSeparationStackFunction implements ProcessedStackingPackFunction
{
    private final double depth;
    private final double sampleHeighestPoint;

    public StiffeningSeparationStackFunction(double depth, double sampleHeighestPoint)
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

        Channel1DData pointwiseModulusChannel = pack.getPointwiseModulus();
        if(pointwiseModulusChannel.isEmpty())
        {
            return Double.POSITIVE_INFINITY;
        }

        double[][] pointwiseModulus = pointwiseModulusChannel.getPoints();
        int n = pointwiseModulus.length;
        int index = -1;
        double distance = Double.POSITIVE_INFINITY;		

        double contactDisplacement = pack.getResults().getContactDisplacement();       
        double denivelation = (sampleHeighestPoint - contactDisplacement);       

        if(denivelation>depth)
        {
            return 0;
        }

        if(pointwiseModulus[n - 1][0] + denivelation > depth)
        {
            for(int i = 0; i<n; i++)
            {
                double[] p = pointwiseModulus[i];
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

        double modulus = pack.getResults().getYoungModulus();

        return pointwiseModulus[index][1] - modulus;
    }

    @Override
    public Quantity getEvaluatedQuantity() 
    {
        return Quantities.STIFFENING_KPA;
    }

    @Override
    public Quantity getStackingQuantity() 
    {
        return Quantities.TIP_DISPLACEMENT_MICRONS;
    }

    public static class StiffeningSeparationFunctionFactory implements StackingPackFunctionFactory<StiffeningSeparationStackFunction>
    {
        private static final String stackType = Datasets.STIFFENING_STACK;

        private final double sampleHeighestPoint;

        public StiffeningSeparationFunctionFactory(double sampleHeighestPoint)
        {
            this.sampleHeighestPoint = sampleHeighestPoint;
        }
        @Override
        public StiffeningSeparationStackFunction getFunction(double level) 
        {
            return new StiffeningSeparationStackFunction(level, sampleHeighestPoint);
        }

        @Override
        public Quantity getEvaluatedQuantity() 
        {
            return Quantities.STIFFENING_KPA;
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
