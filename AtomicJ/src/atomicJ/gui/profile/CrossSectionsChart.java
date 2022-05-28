
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

package atomicJ.gui.profile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

import atomicJ.data.Datasets;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.CurveMarker;
import atomicJ.gui.CustomChartMouseEvent;


public class CrossSectionsChart extends Channel1DChart<CrossSectionPlot>
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-12;

    public static final String USERS_POINT_SELECTION = "USERS_POINT_SELECTION";
    public static final String SELECTION_VISIBLE = "SELECTION_VISIBLE";

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private CurveMarker selectedMarker = null;
    private final Map<Object, List<CurveMarker>> markers = new LinkedHashMap<>();

    private CrossSectionsSupervisor supervisor;

    public CrossSectionsChart(CrossSectionPlot plot)
    {
        super(plot, Datasets.CROSS_SECTION_PLOT);
    }

    public CrossSectionsSupervisor getCrossSectionsSupervisor()
    {
        return supervisor;
    }

    public void setCrossSectionsSupervisor(CrossSectionsSupervisor supervisorNew)
    {
        this.supervisor = supervisorNew;
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
        boolean zoomable = (selectedMarker == null) && super.isDomainZoomable();

        return zoomable;
    }

    @Override
    public boolean isRangeZoomable()
    {
        boolean zoomable = (selectedMarker == null) && super.isRangeZoomable();

        return zoomable;
    }


    public void addMarker(CrossSectionMarkerParameters markerParameters)
    {		
        if(markerParameters != null)
        {
            Point2D controlPoint = markerParameters.getControlPoint();
            Object profileKey = markerParameters.getProfileKey();

            CurveMarker marker = new CurveMarker(controlPoint, profileKey, 15, 25, new BasicStroke(1.5f), Color.black, Color.white);

            List<CurveMarker> markersForProfile = markers.get(profileKey);

            if(markersForProfile == null)
            {
                markersForProfile= new ArrayList<>();
                markers.put(profileKey, markersForProfile);
            }

            markersForProfile.add(marker);

            CrossSectionPlot crossSectionPlot = getCustomizablePlot();
            crossSectionPlot.addMarker(profileKey, marker);
        }
    }

    public void moveMarker(CrossSectionMarkerParameters markerParameters, int markerIndex)
    {
        if(markerParameters != null)
        {
            Object profileKey = markerParameters.getProfileKey();
            Point2D controlPoint = markerParameters.getControlPoint();

            List<CurveMarker> markersForProfile = markers.get(profileKey);
            int n = markersForProfile.size();

            boolean withinRange = markerIndex >= 0 && markerIndex <n;

            if(withinRange)
            {
                CurveMarker marker = markersForProfile.get(markerIndex);
                marker.setControlPoint(controlPoint);
            }	
        }	
    }

    public void removeMarker(Object profileKey, double markerPosition)
    {
        List<CurveMarker> markersForProfile = markers.get(profileKey);

        if(markersForProfile != null)
        {			
            CurveMarker markersToRemove = null;

            for(CurveMarker marker : markersForProfile)
            {
                double currentMarkerPosition = marker.getControlPoint().getX();

                boolean found = Math.abs(currentMarkerPosition - markerPosition) < TOLERANCE;
                if(found)
                {
                    markersToRemove = marker;
                    break;
                }
            }

            if(markersToRemove != null)
            {
                markersForProfile.remove(markersToRemove);

                CrossSectionPlot crossSectionPlot = getCustomizablePlot();
                crossSectionPlot.removeMarker(profileKey, markersToRemove);
            }
        }	
    }

    public void removeAllMarkers(Object profileKey)
    {
        CrossSectionPlot crossSectionPlot = getCustomizablePlot();

        List<CurveMarker> markersForProfile = markers.get(profileKey);

        if(markersForProfile != null)
        {
            for(CurveMarker marker : markersForProfile)
            {
                crossSectionPlot.removeMarker(profileKey, marker);
            }

            markersForProfile.clear();
        }	
    }

    @Override
    public void chartMouseDragged(CustomChartMouseEvent event) 
    {
        super.chartMouseDragged(event);
        if(supervisor == null || selectedMarker == null)
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();  
        Point2D dataPoint = event.getDataPoint();

        PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();

        Rectangle2D dataArea = info.getDataArea();

        if(dataArea.contains(java2DPoint))
        {
            Object profileKey = selectedMarker.getSeriesKey();
            List<CurveMarker> m = markers.get(profileKey);
            int markerIndex = m.indexOf(selectedMarker);
            double positionNew = dataPoint.getX();

            supervisor.requestMarkerMovement(profileKey, markerIndex, positionNew);
        }       
    }

    @Override
    public void chartMousePressed(CustomChartMouseEvent event)
    {
        super.chartMousePressed(event);

        Point2D java2DPoint = event.getJava2DPoint();
        selectedMarker = getClickedMarker(java2DPoint);
    }

    private CurveMarker getClickedMarker(Point2D java2DPoint)
    {
        CurveMarker clickedMarker = null;

        for(Entry<Object, List<CurveMarker>> entry : markers.entrySet())
        {
            List<CurveMarker> markersForProfile = entry.getValue();

            for(CurveMarker marker : markersForProfile)
            {
                boolean clicked = marker.isClicked(java2DPoint);

                if(clicked)
                {
                    clickedMarker = marker;
                }
            }
        }

        return clickedMarker;
    }

    @Override
    public void chartMouseReleased(CustomChartMouseEvent event)
    {
        super.chartMouseReleased(event);

        selectedMarker = null;			
    }

    @Override
    public void chartMouseMoved(CustomChartMouseEvent event) 
    {   
        super.chartMouseMoved(event);

        if(supervisor == null)
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        selectedMarker = getClickedMarker(java2DPoint);

        PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();

        Rectangle2D dataArea = info.getDataArea();
        Point2D dataPoint = getDataPoint(java2DPoint, info);

        Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

        if(dataArea.contains(java2DPoint))
        {
            if(selectedMarker == null)
            {
                double d = dataPoint.getX();
                double zSelected = dataPoint.getY();
                double margin = getMargin();

                boolean canBeAdded = supervisor.canNewMarkerBeAdded(zSelected, d, margin);

                if(canBeAdded)
                {
                    cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                }
            }
        }
        supervisor.requestCursorChange(cursor);     
    }

    @Override
    public void chartMouseClicked(CustomChartMouseEvent event) 
    {	
        super.chartMouseClicked(event);

        if(supervisor == null)
        {
            return;
        }

        Point2D java2DPoint = event.getJava2DPoint();
        selectedMarker = getClickedMarker(java2DPoint);

        PlotRenderingInfo info = event.getRenderingInfo().getPlotInfo();

        Rectangle2D dataArea = info.getDataArea();
        Point2D dataPoint = getDataPoint(java2DPoint, info);

        if(dataArea.contains(java2DPoint))
        {
            if(selectedMarker == null)
            {
                double d = dataPoint.getX();
                double zSelected = dataPoint.getY();
                double margin = getMargin();

                supervisor.attemptToAddNewMarker(zSelected, d, margin);
            }
            else
            {
                MouseEvent trigger = event.getTrigger();        

                boolean multiple = trigger.getClickCount()>=2;
                if(multiple)
                {
                    Object profileKey = selectedMarker.getSeriesKey();
                    double markerPosition = selectedMarker.getControlPoint().getX();

                    supervisor.attemptToRemoveMarker(profileKey, markerPosition);
                }             
            }
        }
    }

    private double getMargin()
    {
        XYPlot plot = getCustomizablePlot();
        ValueAxis rangeAxis = plot.getRangeAxis();

        double yRangeLength = rangeAxis.getRange().getLength();

        double margin = 0.05*yRangeLength;

        return margin;
    }
}
