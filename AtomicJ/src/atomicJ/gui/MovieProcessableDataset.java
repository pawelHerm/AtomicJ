
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

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.xy.AbstractXYZDataset;

import atomicJ.data.Channel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;


public class MovieProcessableDataset extends AbstractXYZDataset implements ProcessableXYZDataset, Movie2DDataset
{
    private static final long serialVersionUID = 1L;

    private final String key;

    private final int frameCount;
    private int currentFrameIndex = 0;
    private final List<ProcessableXYZDataset> frames;

    private boolean recentlyChanged;

    public MovieProcessableDataset(List<? extends ProcessableXYZDataset> frames, String key)
    {     
        this.frames = new ArrayList<>(frames);
        this.frameCount = frames.size();
        this.key = key;     
    }

    @Override
    public UnitExpression getToolTipExpression(Point2D dataPoint)
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);

        return currentFrame.getToolTipExpression(dataPoint);
    }

    @Override
    public int getCurrentFrameIndex()
    {
        return currentFrameIndex;
    }

    @Override
    public ProcessableXYZDataset getCurrentFrame()
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame;
    }

    @Override
    public int getFrameCount()
    {
        return frameCount;
    }

    @Override
    public void showNextFrame()
    {
        showNextFrame(true);
    }

    @Override
    public void showNextFrame(boolean notify)
    {
        int index = (currentFrameIndex + 1)%frameCount;

        showFrame(index, notify);
    }

    @Override
    public void showPreviousFrame()
    {
        showPreviousFrame(true);
    }

    @Override
    public void showPreviousFrame(boolean notify)
    {
        int decremented = (currentFrameIndex - 1);
        int index = decremented <0 ? frameCount + decremented : decremented;

        showFrame(index, notify);
    }

    @Override
    public void showFrame(int index)
    {
        showFrame(index, true);	
    }

    @Override
    public void showFrame(int index, boolean notify)
    {   
        if(index>= 0 && index < frameCount)
        {							    		    
            this.currentFrameIndex = index;	

            if(notify)
            {
                fireDatasetChanged();
            }
        }		
    }

    @Override
    public ProcessableXYZDataset getFrame(int index)
    {
        return frames.get(index);
    }

    @Override
    public Number getZ(int arg0, int arg1) 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getZ(arg0, arg1);
    }

    @Override
    public double getZValue(int arg0, int arg1) 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getZValue(arg0, arg1);
    }

    @Override
    public DomainOrder getDomainOrder() 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getDomainOrder();
    }

    @Override
    public int getItemCount(int arg0) 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getItemCount(arg0);
    }

    @Override
    public Number getX(int arg0, int arg1) 
    {    
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getX(arg0, arg1);
    }

    @Override
    public double getXValue(int arg0, int arg1) 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getXValue(arg0, arg1);
    }

    @Override
    public Number getY(int arg0, int arg1) 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getY(arg0, arg1);
    }

    @Override
    public double getYValue(int arg0, int arg1) 
    {      
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getYValue(arg0, arg1);
    }

    @Override
    public int getSeriesCount() 
    {	    
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getSeriesCount();
    }

    @Override
    public Comparable getSeriesKey(int arg0) 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.getSeriesKey(arg0);
    }

    @Override
    public int indexOf(Comparable arg0) 
    {
        ProcessableXYZDataset currentFrame = frames.get(currentFrameIndex);
        return currentFrame.indexOf(arg0);
    }

    @Override
    public double[][] getDataCopy(int seriesNo)
    {
        return new double[][] {};
    }

    @Override
    public double[][] getData(int seriesNo) 
    {
        return new double[][] {};
    }

    @Override
    public void setData(int seriesNo, Channel2DData data) 
    {
        setData(seriesNo, data, true);
    }

    @Override
    public void setData(int seriesNo, Channel2DData data, boolean notify)
    {
        setDataRecentlyChanged(true);

        for(ProcessableXYZDataset frame : frames)
        {
            frame.setData(seriesNo, data, false);
        }

        if(notify)
        {
            fireDatasetChanged();
        }   
    }

    @Override
    public void notifyOfDataChange(boolean notifyListeners)
    {
        setDataRecentlyChanged(true);

        for(ProcessableXYZDataset frame : frames)
        {
            frame.notifyOfDataChange(false);
        }

        if(notifyListeners)
        {
            fireDatasetChanged();
        }   
    }

    @Override
    public Object getKey() 
    {
        return key;
    }

    @Override
    public Rectangle2D getDataArea()
    {        
        Area area = new Area();

        for(ProcessableXYZDataset frame : frames)
        {
            area.add(new Area(frame.getDataArea()));
        }

        Rectangle2D datasetArea = area.getBounds2D();

        return datasetArea;
    }

    @Override
    public Range getXRange()
    {
        Range range = null;
        for(ProcessableXYZDataset frame : frames)
        {
            range = Range.combine(range, frame.getXRange());
        }

        return range;
    }

    @Override
    public Range getYRange() 
    {        
        Range range = null;
        for(ProcessableXYZDataset frame : frames)
        {
            range = Range.combine(range, frame.getYRange());
        }

        return range;  
    }

    @Override
    public Range getZRange() 
    {
        Range range = null;
        for(ProcessableXYZDataset frame : frames)
        {
            range = Range.combine(range, frame.getZRange());
        }

        return range;
    }

    @Override
    public Range getAutomaticZRange()
    {
        Range range = null;
        for(ProcessableXYZDataset frame : frames)
        {
            range = Range.combine(range, frame.getAutomaticZRange());
        }
        return range;
    }

    @Override
    public QuantitativeSample getSample(int seriesNo) 
    {
        ProcessableXYZDataset currentFrameDataset = frames.get(currentFrameIndex);
        QuantitativeSample sample = currentFrameDataset.getSample(seriesNo);
        return sample;
    }

    @Override
    public boolean isDataRecentlyChanged() 
    {
        return recentlyChanged;
    }

    @Override
    public void setDataRecentlyChanged(boolean recentlyChanged) 
    {
        this.recentlyChanged = recentlyChanged;
    }

    @Override
    public Quantity getXQuantity()
    {
        ProcessableXYZDataset currentFrameDataset = frames.get(currentFrameIndex);
        return currentFrameDataset.getXQuantity();
    }

    @Override
    public Quantity getYQuantity()
    {
        ProcessableXYZDataset currentFrameDataset = frames.get(currentFrameIndex);
        return currentFrameDataset.getYQuantity();
    }

    @Override
    public Quantity getZQuantity()
    {
        ProcessableXYZDataset currentFrameDataset = frames.get(currentFrameIndex);
        return currentFrameDataset.getZQuantity();
    }

    @Override
    public double getXDataDensity() 
    {        
        return frames.get(currentFrameIndex).getXDataDensity();
    }

    @Override
    public double getYDataDensity() 
    {
        return frames.get(currentFrameIndex).getYDataDensity();
    }

    @Override
    public Range getRangeBounds(boolean arg0) {
        return getYRange();
    }

    @Override
    public double getRangeLowerBound(boolean arg0) {
        return getYRange().getLowerBound();
    }

    @Override
    public double getRangeUpperBound(boolean arg0) {
        return getYRange().getUpperBound();
    }

    @Override
    public Range getDomainBounds(boolean arg0) 
    {
        return getXRange();
    }

    @Override
    public double getDomainLowerBound(boolean arg0) {
        return getXRange().getLowerBound();
    }

    @Override
    public double getDomainUpperBound(boolean arg0) 
    {
        return getXRange().getUpperBound();
    }
}
