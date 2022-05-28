
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ProcessedStaticSpectroscopyCurve extends BasicSpectroscopyCurve<Channel1DCollection>
{
    private final Channel1D contactPoint;
    private final Channel1D modelTransitionPoint;
    private final List<Channel1D> adhesionMarkers;
    private final List<Channel1D> jumpMarkers;

    private final Map<String, Channel1D> additionalChannels = new LinkedHashMap<>();

    public ProcessedStaticSpectroscopyCurve(Channel1DCollection approach, Channel1DCollection withdraw, Channel1D contactPoint, List<Channel1D> adhesionMarkers, List<Channel1D> jumpMarkers, Channel1D modelLimitPoint, String name)
    {
        super(approach, withdraw, name, name);
        this.contactPoint = contactPoint;
        this.adhesionMarkers = adhesionMarkers;
        this.jumpMarkers = jumpMarkers;
        this.modelTransitionPoint = modelLimitPoint;
    }

    public void setFit(Channel1DData fit)
    {
        addChannels(Datasets.FIT, new Channel1DStandard(fit, Datasets.FIT));
    }

    public void addChannels(String key, Channel1D channel)
    {
        if(channel != null)
        {
            additionalChannels.put(key, channel);
        }
    }

    @Override
    public List<Channel1D> getChannels()
    {
        List<Channel1D> channels = super.getChannels();

        channels.addAll(additionalChannels.values());
        channels.addAll(adhesionMarkers);
        channels.addAll(jumpMarkers);
        channels.add(contactPoint);
        channels.add(modelTransitionPoint);

        return channels;
    }

    @Override
    public ProcessedStaticSpectroscopyCurve getCopy(double s)
    {
        Channel1DCollection scaledApproach = getApproach().getCopy(s);
        Channel1DCollection scaledWithdraw = getWithdraw().getCopy(s);
        Channel1D scaledContactPoint = contactPoint.getCopy(s);
        Channel1D scaledTransitionPoint = modelTransitionPoint.getCopy(s);

        List<Channel1D> scaledAdhesionMarkers = new ArrayList<>();
        for(Channel1D adhesionMarker : adhesionMarkers)
        {
            scaledAdhesionMarkers.add(adhesionMarker.getCopy(s));
        }

        List<Channel1D> scaledJumpMarkers = new ArrayList<>();
        for(Channel1D jumpMarker : jumpMarkers)
        {
            scaledJumpMarkers.add(jumpMarker.getCopy(s));
        }

        ProcessedStaticSpectroscopyCurve scaledCurve = new ProcessedStaticSpectroscopyCurve(scaledApproach, scaledWithdraw, scaledContactPoint, scaledAdhesionMarkers, scaledJumpMarkers, scaledTransitionPoint, getIdentifier());

        for(Entry<String, Channel1D> entry : additionalChannels.entrySet())
        {
            String key = entry.getKey();
            Channel1D scaledDataset = entry.getValue().getCopy(s);
            scaledCurve.addChannels(key, scaledDataset);
        }

        return scaledCurve;
    }
}