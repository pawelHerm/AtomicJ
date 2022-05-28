
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

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;


public class MovieXYDataset extends AbstractXYDataset implements MovieDataset
{
    private static final long serialVersionUID = 1L;

    private final int frameCount;
    private int currentFrameIndex = 0;

    private XYDataset currentFrame;
    private final List<XYDataset> frames = new ArrayList<>();


    public MovieXYDataset(List<XYDataset> frames)
    {		
        this.frameCount = frames.size();
        this.frames.addAll(frames);
        this.currentFrame = frames.get(currentFrameIndex);			
    }

    @Override
    public int getCurrentFrameIndex()
    {
        return currentFrameIndex;
    }

    @Override
    public XYDataset getCurrentFrame()
    {
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
            currentFrame = frames.get(currentFrameIndex);
            fireDatasetChanged();
        }		
    }

    @Override
    public XYDataset getFrame(int index)
    {
        return frames.get(index);
    }

    @Override
    public DomainOrder getDomainOrder() 
    {
        return currentFrame.getDomainOrder();
    }

    @Override
    public int getItemCount(int arg0) 
    {
        return currentFrame.getItemCount(arg0);
    }

    @Override
    public Number getX(int arg0, int arg1) 
    {
        return currentFrame.getX(arg0, arg1);
    }

    @Override
    public double getXValue(int arg0, int arg1) 
    {
        return currentFrame.getXValue(arg0, arg1);
    }

    @Override
    public Number getY(int arg0, int arg1) 
    {
        return currentFrame.getY(arg0, arg1);
    }

    @Override
    public double getYValue(int arg0, int arg1) 
    {
        return currentFrame.getYValue(arg0, arg1);
    }

    @Override
    public int getSeriesCount() 
    {
        return currentFrame.getSeriesCount();
    }

    @Override
    public Comparable<?> getSeriesKey(int arg0) 
    {
        return currentFrame.getSeriesKey(arg0);
    }

    @Override
    public int indexOf(Comparable arg0) 
    {
        return currentFrame.indexOf(arg0);
    }
}
