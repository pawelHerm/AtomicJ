
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


import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.Range;

import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.data.Channel2D;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.readers.ReadingPack;



public interface MapSource<E extends Channel2D> extends SpectroscopySource, Channel2DSource<E>
{
    public double getProbingDensity();

    public void registerProcessedPack(ProcessedSpectroscopyPack pack);
    public ProcessedSpectroscopyPack getProcessedPack(int i);

    public SimpleSpectroscopySource getSimpleSpectroscopySource(Point2D p);

    public ProcessedSpectroscopyPack getProcessedPack(Point2D p);
    public Map<Point2D, ProcessedSpectroscopyPack> getProcessedPacksMap();
    public Set<ProcessedSpectroscopyPack> getProcessedPacks();
    public Set<ProcessedSpectroscopyPack> getProcessedPacks(ROI roi, ROIRelativePosition position);
    public List<SimpleSpectroscopySource> getSimpleSources(ROI roi, ROIRelativePosition position);
    public ReadingPack<ImageSource> getMapAreaImageReadingPack();
    public boolean isMapAreaImagesAvailable();
    public void setMapAreaImageReadingPack(ReadingPack<ImageSource> scanAreaImageInfo);

    public Channel2D getChannel(ProcessedPackFunction<? super ProcessedSpectroscopyPack> f);
    public Channel2D getChannel(ROI roi, ROIRelativePosition position, ProcessedPackFunction<? super ProcessedSpectroscopyPack> f);

    public Range getHeightRange();

    public boolean isRecordedAsGrid();
    public boolean isProcessed();
    public boolean isProcessedAsFullGrid();

    public boolean isSealed();
    public void seal();
    public void replaceSpectroscopySource(SimpleSpectroscopySource source, int index);

    @Override
    public MapSource<E> copy();    
    @Override
    public MapSource<E> copy(Collection<String> identifiers);

    public ProcessedPackReplacementResults replaceProcessedPacks(List<ProcessedSpectroscopyPack> packs);
}
