
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

package atomicJ.sources;


import java.awt.Shape;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2D;
import atomicJ.data.DataAxis1D;
import atomicJ.data.QuantitativeSample;
import atomicJ.gui.profile.ChannelSectionLine;
import atomicJ.gui.rois.ROI;
import atomicJ.readers.regularImage.Channel2DSourceMetadata;


public interface Channel2DSource <E extends Channel2D> extends ChannelSource
{
    public Channel2DSourceMetadata getMetadata();
    @Override
    public List<E> getChannels();
    public E getChannel(String identifier);
    @Override
    public List<E> getChannels(Collection<String> identifiers);

    public List<E> getChannelCopies();
    public List<E> getChannelCopies(Collection<String> channels);

    public void setChannels(Collection<E> channels);

    public E duplicateChannel(String identifier);
    public E duplicateChannel(String identifier, String identifierNew);

    public void removeChannel(String identifier);
    public void retainAll(Collection<String> identifiers);
    public void addChannel(E channel);

    public String getUniversalROIKey(Object roiKey);

    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod);

    public List<ChannelSectionLine> getHorizontalCrossSections(DataAxis1D verticalAxis, Object key, String name, String identifier);
    public List<ChannelSectionLine> getVerticalCrossSections(DataAxis1D horizontalAxis, Object key, String name, String identifier);   

    public Map<String, ChannelSectionLine> getVerticalCrossSections(double level, Object key, String name, String identifier);
    public Map<String, ChannelSectionLine> getVerticalCrossSections(double level, Object key, String name);
    public Map<String, ChannelSectionLine> getHorizontalCrossSections(double level, Object key, String name, String identifier);
    public Map<String, ChannelSectionLine> getHorizontalCrossSections(double level, Object key, String name);
    public Map<String, ChannelSectionLine> getCrossSections(Shape profile, Object key, String name, String identifier);
    public Map<String, ChannelSectionLine> getCrossSections(Shape profile, Object key, String name);
    public Map<String, ChannelSectionLine> getCrossSections(Shape profile, Object crossSectionKey, String crossSectionName, Set<String> identifiers);
    public Map<String, QuantitativeSample> getSamples(boolean includeCoordinates);
    public QuantitativeSample getSample(String identifier);

    public Map<Object, QuantitativeSample> getROISamples(Collection<ROI> rois, String identifier);
    public Map<String, Map<Object, QuantitativeSample>> getROISamples(Collection<? extends ROI> rois, boolean includeCoordinates);
    public Map<String, Map<Object, QuantitativeSample>> getROISamples(Collection<? extends ROI> rois, List<String> identifiers, boolean includeCoordinates);

    @Override
    public Channel2DSource<E> copy();
    public Channel2DSource<E> copy(Collection<String> identifiers);
}
