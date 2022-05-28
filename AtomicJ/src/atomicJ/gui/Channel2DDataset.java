
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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.util.PublicCloneable;

import atomicJ.data.Channel2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;

public class Channel2DDataset extends AbstractXYZDataset implements ProcessableXYZDataset, PublicCloneable, DomainInfo, RangeInfo
{
    private static final long serialVersionUID = 1L;

    private boolean dataRecentlyChanged = false;

    private final Object key;
    private final Channel2D originalChannel;   

    public Channel2DDataset(Channel2D channel, Object key)
    {
        this.originalChannel = channel;
        this.key = key;
    }

    public static Channel2DDataset getDataset(Channel2D channel, String sourceName)
    {
        Object key = channel.getIdentifier() + sourceName;
        Channel2DDataset dataset = new Channel2DDataset(channel, key);

        return dataset;
    }

    public static List<Channel2DDataset> getDatasets(List<Channel2D> channels, String sourceName)
    {
        List<Channel2DDataset> datasets = new ArrayList<>();
        for(Channel2D channel: channels)
        {
            Channel2DDataset dataset = Channel2DDataset.getDataset(channel, sourceName);
            datasets.add(dataset);
        }

        return datasets;
    }


    public Channel2D getDisplayedChannel()
    {
        return originalChannel;
    }

    @Override
    public UnitExpression getToolTipExpression(Point2D dataPoint)
    {
        UnitExpression tooltipExpression = null;
        if(originalChannel.isWithinDataDomain(dataPoint))
        {
            double zValue = originalChannel.getValue(dataPoint);

            tooltipExpression = new UnitExpression(zValue, originalChannel.getZQuantity().getUnit());
        }   

        return tooltipExpression;
    }

    @Override
    public Rectangle2D getDataArea()
    {
        return originalChannel.getDataArea();
    }

    @Override
    public Number getZ(int series, int item) 
    {
        return originalChannel.getZ(item);
    }

    @Override
    public int getItemCount(int series) 
    {
        return originalChannel.getItemCount();
    }

    @Override
    public Number getX(int series, int item) 
    {
        return originalChannel.getX(item);
    }

    @Override
    public Number getY(int series, int item) 
    {
        return originalChannel.getY(item);
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
            fireDatasetChanged();
        }   
    }

    @Override
    public double[][] getDataCopy(int seriesNo)
    {
        return originalChannel.getChannelData().getPointsCopy();
    }

    @Override
    public double[][] getData(int seriesNo)
    {
        return originalChannel.getPoints();
    }

    @Override
    public Range getZRange() 
    {
        return originalChannel.getZRange();
    }

    @Override
    public Object getKey() 
    {
        return key;
    }

    @Override
    public int getSeriesCount() 
    {
        return 1;
    }

    @Override
    public Comparable<?> getSeriesKey(int series) 
    {
        return (Comparable<?>) key;
    }

    @Override
    public double getXDataDensity()
    {
        double xDensity = originalChannel.getXDataDensity();
        return xDensity;
    }

    @Override
    public double getYDataDensity()
    {
        double yDensity = originalChannel.getYDataDensity();
        return yDensity;
    }


    @Override
    public Range getAutomaticZRange() 
    {
        return originalChannel.getAutomaticZRange();
    }  

    @Override
    public Quantity getXQuantity()
    {
        Quantity quantity = originalChannel.getXQuantity();
        return quantity;
    }

    @Override
    public Quantity getYQuantity()
    {
        Quantity quantity = originalChannel.getYQuantity();
        return quantity;
    }

    @Override
    public Quantity getZQuantity()
    {
        return originalChannel.getZQuantity();
    }

    @Override
    public QuantitativeSample getSample(int series)
    {
        return originalChannel.getZSample();
    }

    @Override
    public boolean isDataRecentlyChanged()
    {
        return dataRecentlyChanged;
    }

    @Override
    public void setDataRecentlyChanged(boolean dataRecentlyChanged) 
    {
        this.dataRecentlyChanged = dataRecentlyChanged;
    }

    @Override
    public Range getXRange() 
    {
        return originalChannel.getXRange();
    }

    @Override
    public Range getYRange()
    {
        return originalChannel.getYRange();
    }

    @Override
    public Range getRangeBounds(boolean includeInterval)
    {
        return this.originalChannel.getYRange();
    }

    @Override
    public double getRangeLowerBound(boolean includeInterval)
    {
        return this.originalChannel.getYRange().getLowerBound();
    }

    @Override
    public double getRangeUpperBound(boolean includeInterval) 
    {
        return this.originalChannel.getYRange().getUpperBound();
    }

    @Override
    public Range getDomainBounds(boolean includeInterval) 
    {
        return this.originalChannel.getXRange();
    }

    @Override
    public double getDomainLowerBound(boolean includeInterval) 
    {
        return this.originalChannel.getXRange().getLowerBound();
    }

    @Override
    public double getDomainUpperBound(boolean includeInterval) 
    {
        return this.originalChannel.getXRange().getUpperBound();
    }
}
