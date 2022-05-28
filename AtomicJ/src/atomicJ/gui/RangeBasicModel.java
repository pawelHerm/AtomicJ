
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

package atomicJ.gui;

public class RangeBasicModel extends AbstractModel implements RangeModel
{
    private double lowerBound;
    private double upperBound;

    public RangeBasicModel(double lowerBound, double upperBound)
    {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public double getLowerBound()
    {
        return lowerBound;
    }

    @Override
    public void setLowerBound(double lowerBoundNew)
    {
        if(lowerBoundNew > this.upperBound)
        {
            return;
        }

        double lowerBoundOld = this.lowerBound;
        this.lowerBound = lowerBoundNew;

        firePropertyChange(RangeModel.LOWER_BOUND, lowerBoundOld, lowerBoundNew);
    }

    @Override
    public double getUpperBound()
    {
        return upperBound;
    }

    @Override
    public void setUpperBound(double upperBoundNew)
    {
        if(upperBoundNew < this.lowerBound)
        {
            return;
        }

        double upperBoundOld = this.upperBound;
        this.upperBound = upperBoundNew;

        firePropertyChange(RangeModel.UPPER_BOUND, upperBoundOld, upperBoundNew);
    }

    @Override
    public void setGradientRangeSelector(GradientRangeSelector selector) {
        // TODO Auto-generated method stub

    }

}
