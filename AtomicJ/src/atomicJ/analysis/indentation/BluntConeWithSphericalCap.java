
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

package atomicJ.analysis.indentation;


public class BluntConeWithSphericalCap implements Indenter 
{
    private static double TOLERANCE = 10e-12;
    private final double halfAngle;
    private final double transitionRadius;
    private final double apexRadius;

    //angle has to be specified in radians
    public BluntConeWithSphericalCap(double halfAngle, double apexRadius)
    {
        this.halfAngle = halfAngle;
        this.apexRadius = apexRadius;
        this.transitionRadius = apexRadius*Math.cos(halfAngle);
    }

    public BluntConeWithSphericalCap(double halfAngle, double apexRadius, double transitionRadius)
    {
        this.halfAngle = halfAngle;
        this.apexRadius = apexRadius;
        this.transitionRadius = transitionRadius;
    }

    public boolean isSmooth()
    {
        boolean smooth = Math.abs(this.transitionRadius - apexRadius*Math.cos(halfAngle)) < TOLERANCE;

        return smooth;
    }

    public double getHalfAngle()
    {
        return halfAngle;
    }

    public double getApexRadius()
    {
        return apexRadius;
    }

    public double getTransitionRadius()
    {
        return transitionRadius;
    }

    @Override
    public String getName() 
    {
        return "Blunt cone";
    }
}
