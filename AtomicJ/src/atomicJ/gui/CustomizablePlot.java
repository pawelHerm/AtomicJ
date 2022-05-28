
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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.List;


import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYDataset;

import atomicJ.data.units.Quantity;
import atomicJ.gui.measurements.DistanceLineMeasurement;

public interface CustomizablePlot extends PreferencesSource, RoamingTitleChangeListener
{

    public int getActiveLayer();
    public void setActiveLayer(int activeLayerNew);
    public void replaceLayerDataset(Object key, XYDataset dataset);
    public boolean removeLayer(Object key);
    public boolean removeLayer(Object key, boolean removeAxis);
    public void addOrReplaceLayer(Object key, XYDataset dataset, XYItemRenderer renderer);
    public void addOrReplaceLayer(Object key, XYDataset dataset, XYItemRenderer renderer, Quantity rangeQuantity);
    public void insertLayer(Object key, Integer index, XYDataset dataset, XYItemRenderer renderer);
    public void setLayer(Object key, XYDataset dataset, XYItemRenderer renderer);
    public boolean containsLayer(Object key);
    public void clearLayerData(Object key);	
    public void clearLayerData(Object key, boolean removeAxis);   
    public XYItemRenderer getLayerRenderer(Object key);
    public int getLayerIndex(Object key);
    public int getLayerCount();
    public RoamingLegend getRoamingLegend();

    public void setRoamingLegend(RoamingLegend legend);
    public RoamingTextTitle getRoamingTitle();	

    public void setRoamingTitle(RoamingTextTitle title);

    public ScaleBar getDomainScaleBar();

    public ScaleBar getRangeScaleBar();

    ///////////////////////////DISTANCE MEASUREMENTS////////////////////////////////

    public void removeDistanceMeasurement(DistanceLineMeasurement measurement);
    public void removeDistanceMeasurement(DistanceLineMeasurement measurement, boolean notify);
    public void addOrReplaceDistanceMeasurement(DistanceLineMeasurement measurement);
    public void addOrReplaceDistanceMeasurement(DistanceLineMeasurement measurement, boolean notify);
    public List<DistanceLineMeasurement> getDistanceMeasurements();
    public void drawDistanceMeasurements(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info);
    public void updateFixedDataAreaSize(Graphics2D g2, Rectangle2D area);
    public void drawScaleBars(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info);
    public void drawRoamingTextTitle(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info);
    public void drawRoamingLegend(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info);
    public void drawRoamingTitle(RoamingTitle roamingTitle, Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info);
    public abstract String getName();
    public Series findSeries(Comparable<?> key);
    public ChannelRenderer findRenderer(Comparable<?> key);
    public void attemptChangeSeriesKey(final Comparable<?> seriesKey);
    public Paint getDefaultAnnotationPaint();
}
