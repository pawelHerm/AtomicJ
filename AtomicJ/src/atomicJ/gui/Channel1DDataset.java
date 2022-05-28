
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

import java.io.Serializable;
import java.util.List;

import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.util.PublicCloneable;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelGroupTag;
import atomicJ.data.units.Quantity;

public class Channel1DDataset extends AbstractXYDataset implements ProcessableXYDataset<Channel1D>, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private boolean dataRecentlyChanged = false;

    private Comparable<?> name;
    private Channel1D displayedChannel;

    public Channel1DDataset(Channel1D channel, Comparable<?> name)
    {
        this.displayedChannel = channel;
        this.name = name;
    }

    @Override
    public ChannelGroupTag getGroupTag()
    {
        return displayedChannel.getGroupTag();
    }

    public Object getGroupTagId()
    {
        ChannelGroupTag groupTag = displayedChannel.getGroupTag();
        Object groupTagId = (groupTag != null) ? groupTag.getGroupId() : null;
        return groupTagId;
    }

    @Override
    public Channel1DDataset getCopy(Comparable<?> keyNew)
    {
        return new Channel1DDataset(this.displayedChannel.getCopy(), keyNew);
    }

    public Channel1D getDisplayedChannel()
    {
        return displayedChannel;
    }

    @Override
    public Object getKey()
    {
        return this.displayedChannel.getIdentifier();
    }

    @Override
    public boolean isNamedAs(Comparable<?> key)
    {
        return this.name.equals(key);
    }

    @Override
    public int getItemCount(int series) 
    {
        return displayedChannel.getItemCount();
    }

    @Override
    public Number getX(int series, int item) 
    {
        return displayedChannel.getX(item);
    }

    @Override
    public Number getY(int series, int item) 
    {
        return displayedChannel.getY(item);
    }

    public Range getXRange()
    {
        return displayedChannel.getXRange();
    }

    public Range getYRange()
    {
        return displayedChannel.getYRange();
    }

    @Override
    public void setData(int seriesNo, Channel1D data) 
    {
        setData(seriesNo, data, true);
    }

    @Override
    public void setData(int seriesNo, Channel1D data, boolean notify) 
    {   
        this.displayedChannel = data;

        if(notify)
        {
            fireDatasetChanged();
        }	
    }

    @Override
    public void setData(int seriesNo, Channel1DData data, boolean notify) 
    {   
        this.displayedChannel.setChannelData(data);

        if(notify)
        {
            fireDatasetChanged();
        } 
    }

    @Override
    public void notifyOfDataChange(boolean notifyListeners)
    {
        setDataRecentlyChanged(true);

        if(notifyListeners)
        {
            notifyListeners(new DatasetChangeEvent(this, this));
        } 
    }

    @Override
    public double[][] getDataCopy(int seriesNo)
    {
        return displayedChannel.getPointsCopy();
    }

    @Override
    public double[][] getData(int seriesNo)
    {
        return displayedChannel.getPoints();
    }

    @Override
    public int getSeriesCount() 
    {
        return 1;
    }

    @Override
    public Comparable<?> getSeriesKey(int series) 
    {
        if ((series < 0) || (series >= getSeriesCount()))
        {
            throw new IllegalArgumentException("Series index out of bounds");
        }

        return name;
    }

    @Override
    public void changeName(Comparable<?> keyNew)
    {
        this.name = keyNew;
        fireDatasetChanged();
    }

    @Override
    public void setDataRecentlyChanged(boolean dataRecentlyChanged) 
    {
        this.dataRecentlyChanged = dataRecentlyChanged;
    }

    @Override
    public void transform(Channel1DDataTransformation tr)
    {
        this.displayedChannel.transform(tr);
        fireDatasetChanged();      
    }

    @Override
    public Quantity getXQuantity() 
    {
        return displayedChannel.getXQuantity();
    }

    @Override
    public Quantity getYQuantity() 
    {
        return this.displayedChannel.getYQuantity();
    }

    @Override
    public Range getDomainBounds(List visibleSeriesKeys, boolean includeInterval)
    {  
        Range domainBounds = visibleSeriesKeys.contains(name) ? this.displayedChannel.getXRange() : null;

        return domainBounds;
    }

    @Override
    public Range getRangeBounds(List visibleSeriesKeys, Range xRange, boolean includeInterval) 
    {   
        Range rangeBounds = visibleSeriesKeys.contains(name) ? this.displayedChannel.getYRangeIncludingErrors(xRange) : null;

        return rangeBounds;
    }

    @Override
    public Range getDomainBounds(boolean includeInterval)
    {                
        return displayedChannel.getXRange();
    }

    @Override
    public double getDomainLowerBound(boolean includeInterval)
    {
        return displayedChannel.getXMinimumIncludingErrors();
    }

    @Override
    public double getDomainUpperBound(boolean includeInterval)
    {
        return displayedChannel.getXMaximumIncludingErrors();
    }

    @Override
    public Range getRangeBounds(boolean includeInterval)
    {
        Range range = new Range(displayedChannel.getYMinimumIncludingErrors(), displayedChannel.getYMaximumIncludingErrors());
        return range;
    }

    @Override
    public double getRangeLowerBound(boolean includeInterval)
    {
        return displayedChannel.getYMinimumIncludingErrors();
    }

    @Override
    public double getRangeUpperBound(boolean includeInterval) 
    {
        return displayedChannel.getYMaximumIncludingErrors();
    }   
}
