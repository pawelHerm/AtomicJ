
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

import java.awt.Shape;
import java.awt.geom.Point2D;
import org.jfree.data.Range;
import org.jfree.data.xy.XYZDataset;

import atomicJ.data.Channel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitExpression;


public interface ProcessableXYZDataset extends XYZDataset, ProcessableDataset<Channel2DData>
{
    public boolean isDataRecentlyChanged();
    @Override
    public void setDataRecentlyChanged(boolean recentlyChanged);

    public double getXDataDensity();
    public double getYDataDensity();

    public Shape getDataArea();

    public Range getXRange();
    public Range getYRange();
    public Range getZRange();
    public Range getAutomaticZRange();
    public QuantitativeSample getSample(int seriesNo);

    public Quantity getZQuantity();

    public UnitExpression getToolTipExpression(Point2D dataPoint);
}
