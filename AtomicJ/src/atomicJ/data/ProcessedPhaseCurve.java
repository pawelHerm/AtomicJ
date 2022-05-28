
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

package atomicJ.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ProcessedPhaseCurve extends BasicSpectroscopyCurve<Channel1DCollection>
{
    private final Channel1D contactPoint;
    private final Channel1D modelTransitionPoint;

    private final Map<String, Channel1D> additionalPoints = new LinkedHashMap<>();

    public ProcessedPhaseCurve(
            Channel1DCollection approach, Channel1DCollection withdraw, Channel1D contactPoint, Channel1D modelLimitPoint, String name)
    {
        super(approach, withdraw, name, name);
        this.contactPoint = contactPoint;
        this.modelTransitionPoint = modelLimitPoint;
    }

    @Override
    public Channel1DCollection getApproach()
    {
        return super.getApproach();
    }

    @Override
    public Channel1DCollection getWithdraw()
    {
        return super.getWithdraw();
    }

    public void addPoint(String key, Channel1D point)
    {
        if(point != null)
        {
            additionalPoints.put(key, point);
        }
    }

    @Override
    public List<Channel1D> getChannels()
    {
        List<Channel1D> leaves = super.getChannels();

        leaves.add(contactPoint);
        leaves.add(modelTransitionPoint);

        for(Channel1D p : additionalPoints.values())
        {
            leaves.add(p);
        }

        return leaves;
    }

    @Override
    public ProcessedPhaseCurve getCopy(double s)
    {
        Channel1DCollection scaledApproach = getApproach().getCopy(s);
        Channel1DCollection scaledWithdraw = getWithdraw().getCopy(s);
        Channel1D scaledContactPoint = contactPoint.getCopy(s);
        Channel1D scaledTransitionPoint = modelTransitionPoint.getCopy(s);

        ProcessedPhaseCurve scaledCurve = new ProcessedPhaseCurve(scaledApproach, scaledWithdraw, scaledContactPoint, scaledTransitionPoint, getIdentifier());

        for(Entry<String, Channel1D> entry : additionalPoints.entrySet())
        {
            String key = entry.getKey();
            Channel1D scaledPoint = entry.getValue().getCopy(s);
            scaledCurve.addPoint(key, scaledPoint);
        }

        return scaledCurve;
    }
}