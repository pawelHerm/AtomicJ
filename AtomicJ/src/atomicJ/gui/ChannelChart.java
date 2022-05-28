
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

/*
 * The method draw() is a modification of a corresponding method from JFreeChart class, part of JFreeChart library, copyright by Object Refiner Limited and other contributors.
 * The source code of JFreeChart can be found through the API doc at the page http://www.jfree.org/jfreechart/api/javadoc/index.html 
 */

package atomicJ.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceMeasurementSupervisor;


public abstract class ChannelChart<E extends CustomizableXYPlot> 
extends CustomizableXYBaseChart<E> implements PreferencesSource, RoamingTitleChangeListener
{	
    private static final long serialVersionUID = 1L;

    ///DISTANCE MEASUREMENTS

    private DistanceMeasurementStyle distanceMeasurementStyle; // we should use late initialization, because when large force map is analyzed, lots of instances of the chart are created
    private final DistanceMeasurementManager measurementManager = new DistanceMeasurementManager(this);
    private final Map<Object, DataModificationManager> dataModificationManagers = new LinkedHashMap<>();
    private final Map<Object, DrawingManager> dataDrawingManagers = new LinkedHashMap<>();
    private Channel1DModificationSupervisor dataItemSupervisor;

    private ScaleBar caughtScaleBar;

    public ChannelChart(CustomizableXYPlot plot, String styleKey) 
    {
        super(plot, styleKey);
    }

    @Override
    public void setMode(MouseInputMode mode)
    {
        super.setMode(mode);
        setDistanceMeasurementsVisible(isDistanceMeasurementMode());
        handleDataModificationMode(mode); 
        handleDataDrawingMode(mode);
    }

    private void handleDataModificationMode(MouseInputMode mode)
    {
        if(mode instanceof DataModificationMouseInputMode)
        {
            Set<Object> keys = ((DataModificationMouseInputMode) mode).getDatasetKeys();
            for(Object key : keys)
            {
                if(!dataModificationManagers.containsKey(key))
                {
                    DataModificationManager manager = new DataModificationManager(this, key);
                    manager.setDataItemSupervisor(dataItemSupervisor);
                    dataModificationManagers.put(key, manager);
                }
            }            
        }
    }

    private void handleDataDrawingMode(MouseInputMode mode)
    {
        if(mode instanceof DataDrawingMouseInputMode)
        {
            DataDrawingMouseInputMode drawingMode = (DataDrawingMouseInputMode) mode;
            Object key = drawingMode.getDatasetGroupTag();

            if(!dataDrawingManagers.containsKey(key))
            {
                DrawingManager manager = new DrawingManager(this, key, drawingMode.getModificationConstraint(), drawingMode.getMaxDataPointCount());
                manager.setDataItemSupervisor(dataItemSupervisor);
                dataDrawingManagers.put(key, manager);
            }
        }
    }

    public void setDataItemSupervisor(Channel1DModificationSupervisor supervisor)
    {
        this.dataItemSupervisor = supervisor;

        for(DataModificationManager manager : dataModificationManagers.values())
        {
            manager.setDataItemSupervisor(supervisor);
        }
    }

    public DistanceMeasurementStyle getDistanceMeasurementStyle()
    {
        if(distanceMeasurementStyle == null)
        {
            CustomizableXYPlot plot = getCustomizablePlot();
            this.distanceMeasurementStyle = new DistanceMeasurementStyle(plot.getPreferences().node("LineMeasurements"), 
                    plot.getDefaultAnnotationPaint());
        }

        return distanceMeasurementStyle;
    }

    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        measurementManager.removeDistanceMeasurement(measurement);
    }

    public void setDistanceMeasurements(Map<Object, DistanceMeasurementDrawable> measurements2)
    {       
        measurementManager.setDistanceMeasurements(measurements2);
    }

    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        measurementManager.addOrReplaceDistanceMeasurement(measurement);
    }

    public int getCurrentMeasurementIndex()
    {
        return measurementManager.getCurrentMeasurementIndex();
    }

    public int getDistanceMeasurementCount()
    {
        return measurementManager.getDistanceMeasurementCount();
    }

    public List<DistanceShapeFactors> getDistanceMeasurementShapeFactors()
    {
        return measurementManager.getDistanceMeasurementShapeFactors();
    }

    public void setDistanceMeasurementsVisible(boolean visibleNew)
    {
        measurementManager.setDistanceMeasurementsVisible(visibleNew);
    }

    public void setDistanceMeasurementSupervisor(DistanceMeasurementSupervisor measurementSupervisor)
    {
        measurementManager.setDistanceMeasurementSupervisor(measurementSupervisor);
    }

    public DistanceMeasurementSupervisor getDistanceMeasurementSupervisor()
    {
        return measurementManager.getDistanceMeasurementSupervisor();
    }

    public boolean isPopupDisplayable(Point2D dataPoint)
    {
        Rectangle2D dataArea = getDataSquare(dataPoint, 0.005);  

        boolean displayable = !measurementManager.isRightClickReserved(dataArea, dataPoint);

        return displayable;
    }

    private void scaleBarMouseDragged(CustomChartMouseEvent event)
    {        
        if(getCaughtPoint() != null)
        {           
            CustomizableXYPlot plot = getCustomizablePlot();
            ValueAxis domainAxis = plot.getDomainAxis();
            ValueAxis rangeAxis = plot.getRangeAxis();

            if(caughtScaleBar != null)
            {
                Point2D dataPoint = event.getDataPoint();
                Rectangle2D dataArea = event.getRenderingInfo().getChartArea();

                caughtScaleBar.move(getCaughtPoint(), dataPoint, getCustomizablePlot(), dataArea, domainAxis, rangeAxis);    
            }
        }
    }


    private void scaleBarMousePressed(CustomChartMouseEvent event)
    {
        ChartEntity entity = event.getEntity();

        boolean isScaleBarEntity = entity instanceof ScaleBarEntity;

        if(isScaleBarEntity)
        {
            ScaleBarEntity scaleBarEntity = (ScaleBarEntity)entity;
            this.caughtScaleBar = scaleBarEntity.getScaleBar();    
        }
        else
        {
            this.caughtScaleBar = null;             
        }        
    }

    private void scaleBarMouseReleased(CustomChartMouseEvent event)
    {
        this.caughtScaleBar = null;
    }

    private void scaleBarMouseClicked(CustomChartMouseEvent event)
    {
        if(event.isSingle() || event.isConsumed(CHART_EDITION))
        {
            return;
        }

        ChartEntity entity = event.getEntity();

        boolean isScaleBarEntity = entity instanceof ScaleBarEntity;
        if(!isScaleBarEntity)
        { 
            return;
        }

        ScaleBar scaleBar = ((ScaleBarEntity)entity).getScaleBar();
        CustomizableXYPlot plot = getCustomizablePlot();

        if(scaleBar == plot.getDomainScaleBar())
        {
            getChartSupervisor().doEditDomainScaleBarProperties();
            event.setConsumed(CHART_EDITION, true);
        }
        else if(scaleBar == plot.getRangeScaleBar())
        {
            getChartSupervisor().doEditRangeScaleBarProperties();
            event.setConsumed(CHART_EDITION, true);
        }
    }

    private void scaleBarMouseMoved(CustomChartMouseEvent event)
    {}


    @Override
    public void chartMouseDragged(CustomChartMouseEvent event) 
    {
        if(event.getChart() != this)
        {
            return;
        }

        boolean inside = event.isInsideDataArea();

        if(!inside)
        {
            return;
        }

        scaleBarMouseDragged(event);
        measurementManager.mouseDragged(event);  

        for(MouseInputResponse manager : dataModificationManagers.values())
        {
            manager.mouseDragged(event);
        }

        for(MouseInputResponse manager : dataDrawingManagers.values())
        {
            manager.mouseDragged(event);
        }

        //this must be called last,because of the caught point issues
        super.chartMouseDragged(event);
    }


    @Override
    public void chartMousePressed(CustomChartMouseEvent event)
    {
        super.chartMousePressed(event);

        if(event.getChart() != this)
        {
            return;
        }

        boolean isInside = event.isInsideDataArea();
        if(!isInside)
        {   
            return;
        }   

        scaleBarMousePressed(event);
        measurementManager.mousePressed(event);

        for(MouseInputResponse manager : dataModificationManagers.values())
        {
            manager.mousePressed(event);
        }

        for(MouseInputResponse manager : dataDrawingManagers.values())
        {
            manager.mousePressed(event);
        }
    }

    @Override
    public void chartMouseReleased(CustomChartMouseEvent event)
    {
        super.chartMouseReleased(event);
        if(event.getChart() != this)
        {
            return;
        }

        scaleBarMouseReleased(event);
        measurementManager.mouseReleased(event);        

        for(MouseInputResponse manager : dataModificationManagers.values())
        {
            manager.mouseReleased(event);
        }

        for(MouseInputResponse manager : dataDrawingManagers.values())
        {
            manager.mouseReleased(event);
        }
    }

    @Override
    public void chartMouseClicked(CustomChartMouseEvent event) 
    {
        super.chartMouseClicked(event);

        if(getChartSupervisor() == null)
        {
            return;
        }

        boolean isInsideDataArea = event.isInsideDataArea();

        if(!isInsideDataArea)
        {
            return;
        }

        scaleBarMouseClicked(event);
        measurementManager.mouseClicked(event); 

        for(MouseInputResponse manager : dataModificationManagers.values())
        {
            manager.mouseClicked(event);
        }

        for(MouseInputResponse manager : dataDrawingManagers.values())
        {
            manager.mouseClicked(event);
        }
    }

    @Override
    public void chartMouseMoved(CustomChartMouseEvent event) 
    {
        super.chartMouseMoved(event);

        if(event.getChart() != this)
        {
            return;
        }

        scaleBarMouseMoved(event);
        measurementManager.mouseMoved(event);

        for(MouseInputResponse manager : dataModificationManagers.values())
        {
            manager.mouseMoved(event);
        }

        for(MouseInputResponse manager : dataDrawingManagers.values())
        {
            manager.mouseMoved(event);
        }
    }

    @Override
    protected boolean isChartElementCaught()
    {
        boolean chartElementCaught = super.isChartElementCaught();

        chartElementCaught = chartElementCaught || (caughtScaleBar != null);
        chartElementCaught = chartElementCaught || measurementManager.isChartElementCaught();

        for(MouseInputResponse manager : dataModificationManagers.values())
        {
            chartElementCaught = chartElementCaught || manager.isChartElementCaught();
        }

        for(MouseInputResponse manager : dataDrawingManagers.values())
        {
            chartElementCaught = chartElementCaught || manager.isChartElementCaught();
        }

        return  chartElementCaught;
    }  

    @Override
    protected boolean isComplexChartElementUnderConstruction()
    {
        boolean complexUnderConstruction = super.isComplexChartElementUnderConstruction();

        complexUnderConstruction = complexUnderConstruction || measurementManager.isComplexElementUnderConstruction();

        return complexUnderConstruction;
    }
}

