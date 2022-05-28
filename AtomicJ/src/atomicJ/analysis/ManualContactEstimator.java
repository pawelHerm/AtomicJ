
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

public class ManualContactEstimator implements ContactEstimator
{
    private final double contactPointX;
    private final double contactPointY;

    public ManualContactEstimator(double contactPointX, double contactPointY)
    {
        this.contactPointX = contactPointX;
        this.contactPointY = contactPointY;
    }

    @Override
    public double[] getContactPoint(Channel1DData curveBranch, Point2D recordingPosition, double springConstant) 
    {
        double[] contactPoint = new double[] {contactPointX, contactPointY};
        return contactPoint;
    }

    @Override
    public boolean isAutomatic()
    {
        return false;
    }
}
