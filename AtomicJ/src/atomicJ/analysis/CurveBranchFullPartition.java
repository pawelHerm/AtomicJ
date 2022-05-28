
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
import atomicJ.statistics.FittedLinearUnivariateFunction;



public class CurveBranchFullPartition
{
    private final int precontactPosition;

    public CurveBranchFullPartition(Channel1DData channel, Point2D recordingPoint, int deg, double k, ContactEstimationGuide model, double springConstant)
    {	
        PrecontactFit offContact = new PrecontactFit(channel, recordingPoint, deg, model, springConstant);
        FittedLinearUnivariateFunction f = offContact.getBestFit();

        double limit = k*offContact.getNoiseEstimation();

        double[][] points = channel.getPoints();
        int precontactPosition = 0;
        for(double[] p: points)
        {
            double r = f.residual(p);
            if(r <= limit)
            {
                precontactPosition++;
            }
        }	

        this.precontactPosition = precontactPosition;
    }

    public int getLowForcePointCount()
    {
        return precontactPosition;
    }
}

