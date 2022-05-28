
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

package atomicJ.resources;


import java.awt.Shape;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.Range;

import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.PropertyChangeSource;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.Channel2DDataTransformation;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.ImageSource;
import atomicJ.utilities.MultiMap;


public interface BasicChannel2DResource extends ChannelResource<Channel2D, Channel2DData, String>, PropertyChangeSource
{
    public Set<String> getAllIdentifiers();
    public Map<String, Channel2D> getChannels(String type);

    public Map<String, PrefixedUnit> getIdentifierUnitMap();
    public void registerChannel(String type, Channel2DSource<?> source, String identifier);

    public MultiMap<Channel2DSource<?>, String> getSourceChannelIdentifierMaps();
    public Map<Channel2DSource<?>, List<Channel2D>> getSourceChannelMap(String type);

    public String duplicate(String type);

    public Set<Channel2DSource<?>> getChannel2DSources();
    public Set<ImageSource> getImageSources();

    public Range getChannelDataRange(String type);

    public Map<String, PrefixedUnit> getDataUnits(String type);
    public PrefixedUnit getSingleDataUnit(String type);

    //DATA MODIFICATION

    public Map<String, Channel2D> transform(String type, Channel2DDataTransformation tr);
    public Map<String, Channel2D> transform(String type, Set<String> identifiersToTransform, Channel2DDataTransformation tr);
    public Map<String, Channel2D> transform(String type, Channel2DDataInROITransformation tr, ROIRelativePosition position);
    public Map<String, Channel2D> transform(String type, Channel2DDataInROITransformation tr, ROI roi, ROIRelativePosition position);
    public Map<String, Channel2D> transform(String type, Set<String> identifiersToTransform, Channel2DDataInROITransformation tr, ROI roi, ROIRelativePosition position);

    //ROIS

    public boolean areROIsAvailable();
    public Map<Object, ROIDrawable> getROIs();
    public List<Shape> getROIShapes();

    public void addOrReplaceROI(ROIDrawable roi);
    public void removeROI(ROIDrawable roi);
    public void setROIs(Map<Object, ROIDrawable> rois);

    public void setAllROIsLagging(boolean allROIsLagging);

    public Map<String, Map<Object, QuantitativeSample>> addOrReplaceROIAndUpdate(ROIDrawable roi);
    public Map<String, Map<Object, QuantitativeSample>> removeROIAndUpdate(ROIDrawable roi);
    public Map<String, Map<Object, QuantitativeSample>> setROIsAndUpdate(Map<Object, ROIDrawable> rois);
    public Map<String, Map<Object, QuantitativeSample>> refreshLaggingROISamples();

    public void notifyAboutROISampleChange(String type);
}
