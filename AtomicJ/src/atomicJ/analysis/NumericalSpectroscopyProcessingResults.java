
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

import java.util.Collections;
import java.util.List;


public final class NumericalSpectroscopyProcessingResults 
{
    private final double youngModulus;
    private final double contactDistance;
    private final double contactDeflection;
    private final double contactForce;
    private final double transitionIndentation;
    private final double transitionForce;
    private final double maximalDeformation;
    private final double rSquared;

    private final List<ForceEventEstimate> adhesionEvents;
    private final List<ForceEventEstimate> jumpEvents;

    public NumericalSpectroscopyProcessingResults(double youngModulus, double transitionIndentation, double transitionForce, double contactDistance, double contactDeflection, double contactForce, List<ForceEventEstimate> adhesionEstimates, List<ForceEventEstimate> jumpEvents, double maximalDeformation, double rSquared)
    {
        this.youngModulus = youngModulus;
        this.transitionIndentation = transitionIndentation;
        this.transitionForce = transitionForce;
        this.contactDistance = contactDistance;
        this.contactDeflection = contactDeflection;
        this.contactForce = contactForce;
        this.adhesionEvents = Collections.unmodifiableList(adhesionEstimates);
        this.jumpEvents = Collections.unmodifiableList(jumpEvents);
        this.maximalDeformation = maximalDeformation;
        this.rSquared = rSquared;
    }

    public double getYoungModulus()
    {
        return youngModulus;
    }

    public double getTransitionIndentation()
    {
        return transitionIndentation;
    }

    public double getTransitionForce()
    {
        return transitionForce;
    }

    public double getContactDisplacement()
    {
        return contactDistance;
    }

    public double getContactDeflection()
    {
        return contactDeflection;
    }

    public double getMaximalDefomation()
    {
        return maximalDeformation;
    }

    public double getContactForce()
    {
        return contactForce;
    }

    public int getAdhesionForceEstimateCount()
    {
        return adhesionEvents.size();
    }

    public List<ForceEventEstimate> getAdhesionForceEstimates()
    {
        return adhesionEvents;
    }

    public ForceEventEstimate getAdhesionForceEstimate(int index)
    {
        return adhesionEvents.get(index);
    }

    public double getAdhesionForce()
    {
        double adhesionForce = !adhesionEvents.isEmpty() ? adhesionEvents.get(0).getForceMagnitude() : Double.NaN;
        return adhesionForce;
    }

    public int getJumpForceEstimateCount()
    {
        return jumpEvents.size();
    }

    public List<ForceEventEstimate> getJumpForceEstimates()
    {
        return jumpEvents;
    }

    public ForceEventEstimate getJumpForceEstimate(int index)
    {
        return jumpEvents.get(index);
    }

    public double getRSquared()
    {
        return rSquared;
    }
}
