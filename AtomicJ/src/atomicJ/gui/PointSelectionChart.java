
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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;

import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;

import atomicJ.data.Channel1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DStandard;
import atomicJ.data.Datasets;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.utilities.GeometryUtilities;

public class PointSelectionChart extends Channel1DChart<Channel1DPlot>
{
    private static final long serialVersionUID = 1L;

    public static final String USERS_POINT_SELECTION = "USERS_POINT_SELECTION";
    public static final String SELECTION_VISIBLE = "SELECTION_VISIBLE";

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private boolean selectedPointCaught = false;

    private boolean selectionVisible = true;
    private Point2D selection = new Point2D.Double(Double.NaN, Double.NaN);

    private final Channel1DData datasetEmptyChannel = FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);
    private final Channel1D pointChannel = new Channel1DStandard(datasetEmptyChannel, Datasets.CONTACT_POINT);

    public PointSelectionChart(Channel1DPlot plot)
    {
        super(plot, Datasets.CONTACT_SELECTION_PLOT);

        StyleTag styleTag = new StandardStyleTag(Datasets.CONTACT_POINT);
        Preferences pref = Preferences.userNodeForPackage(Channel1DRenderer.class).node(styleTag.getPreferredStyleKey());
        plot.addOrReplaceLayer(new Channel1DDataset(pointChannel, Datasets.CONTACT_POINT), new DiscreteSeriesRenderer(PreferredDiscreteSeriesRendererStyle.getInstance(pref, styleTag), Datasets.CONTACT_POINT, styleTag, Datasets.CONTACT_POINT));
    }

    public double getSelectionX()
    {
        double selectionX = (selection != null) ? selection.getX() : Double.NaN;
        return selectionX;
    }

    public double getSelectionY()
    {
        double selectionY = (selection != null) ? selection.getY() : Double.NaN;
        return selectionY;
    }

    public Point2D getSelection()
    {
        Point2D selectionCopy = (selection != null) ? new Point2D.Double(selection.getX(), selection.getY()) : new Point2D.Double(Double.NaN, Double.NaN);
        return selectionCopy;
    }

    public void setSelectionX(double x)
    {
        double y = (selection != null) ? selection.getY() : 0;
        Point2D selectionNew = new Point2D.Double(x, y);

        setSelection(selectionNew);
    }

    public void setSelectionY(double y)
    {
        double x = (selection != null) ? selection.getX() : 0;
        Point2D selectionNew = new Point2D.Double(x, y);

        setSelection(selectionNew);
    }

    public void setSelection(double x, double y)
    {
        setSelection(new Point2D.Double(x, y));
    }

    public void setSelection(Point2D selectionNew)
    {
        Point2D selectionOld = selection;
        if(!GeometryUtilities.equalNaNPermissive(selectionOld, selectionNew))
        {
            double x = selectionNew.getX();
            double y = selectionNew.getY();

            this.selection = new Point2D.Double(x, y);
            pointChannel.setChannelData(new Point1DData(x, y,Quantities.DISTANCE_MICRONS , Quantities.FORCE_NANONEWTONS));
            propertyChangeSupport.firePropertyChange(USERS_POINT_SELECTION, selectionOld, new Point2D.Double(selectionNew.getX(),selectionNew.getY()));
        }

        boolean isPlottable = GeometryUtilities.isWellFormedPoint(selectionNew);
        if(selectionVisible && isPlottable)
        {
            showSelection(selectionNew);
        }
    }

    private void showSelection(Point2D selectionNew)
    {
        boolean visibleOld = selectionVisible;
        selectionVisible = true;

        boolean isPlottable = GeometryUtilities.isWellFormedPoint(selection);

        if(isPlottable)
        {
            double x = selectionNew.getX();
            double y = selectionNew.getY();

            pointChannel.setChannelData(new Point1DData(x,y, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS));
            notifyOfDataChange(Datasets.CONTACT_POINT);
            getCustomizablePlot().getLayerRenderer(Datasets.CONTACT_POINT).setBaseSeriesVisibleInLegend(true);
        }

        propertyChangeSupport.firePropertyChange(SELECTION_VISIBLE, visibleOld, selectionVisible);
    }

    private void hideSelection()
    {
        boolean visibleOld = selectionVisible;
        selectionVisible = false;

        pointChannel.setChannelData(datasetEmptyChannel);
        notifyOfDataChange(Datasets.CONTACT_POINT);

        propertyChangeSupport.firePropertyChange(SELECTION_VISIBLE, visibleOld, selectionVisible);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }


    @Override
    public boolean isDomainZoomable()
    {
        boolean zoomable = (!selectedPointCaught) && super.isDomainZoomable();

        return zoomable;
    }

    @Override
    public boolean isRangeZoomable()
    {
        boolean zoomable = (!selectedPointCaught) && super.isRangeZoomable();

        return zoomable;
    }

    @Override
    public void chartMouseDragged(CustomChartMouseEvent event) 
    {
        super.chartMouseDragged(event);

        if(selectedPointCaught)
        {
            Point2D dataPoint = event.getDataPoint();       
            setSelection(dataPoint.getX(), dataPoint.getY()); 
        }
    }

    @Override
    public void chartMousePressed(CustomChartMouseEvent event)
    {
        super.chartMousePressed(event);

        selectedPointCaught = false;

        Point2D java2DPoint = event.getJava2DPoint();

        ChartEntity entity = event.getEntity();
        if(entity instanceof XYItemEntity)
        {            
            CustomizableXYPlot plot = getCustomizablePlot();
            XYItemEntity itemEntity = (XYItemEntity)entity;
            if(itemEntity.getDataset() == plot.getDataset(plot.getDatasetCount() - 1))
            {
                Shape area = itemEntity.getArea();
                if(area.contains(java2DPoint))
                {
                    selectedPointCaught = true;
                }
            }
        }
    }

    @Override
    public void chartMouseReleased(CustomChartMouseEvent event)
    {
        super.chartMouseReleased(event);

        selectedPointCaught = false;			
    }

    @Override
    public void chartMouseClicked(CustomChartMouseEvent event) 
    {	
        super.chartMouseClicked(event);

        Point2D dataPoint = event.getDataPoint();       
        setSelection(dataPoint.getX(), dataPoint.getY()); 
    }
}
