
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


import java.awt.print.Printable;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.event.OverlayChangeListener;

import atomicJ.gui.measurements.DistanceMeasurementDrawable;
import atomicJ.gui.measurements.DistanceMeasurementSupervisor;


public class MultipleXYChartPanel<E extends ChannelChart<?>> extends MultipleChartPanel<E> implements ChartSupervisor,
OverlayChangeListener, Printable, Serializable 
{	
    private static final long serialVersionUID = 1L;

    private DistanceMeasurementSupervisor distanceMeasurementSupervisor;
    private Channel1DModificationSupervisor itemMovementSuprvisor;

    public MultipleXYChartPanel() 
    {
        super();
    }

    public MultipleXYChartPanel(E chart) 
    {
        super(chart);
    }

    public MultipleXYChartPanel(E chart, boolean buildPopup) 
    {
        super(chart, buildPopup);
    }

    public MultipleXYChartPanel(E chart, boolean buildPopup, boolean tooltips, boolean mouseWheel) 
    {
        super(chart, buildPopup, tooltips, mouseWheel);
    }

    public MultipleXYChartPanel(E chart, int width, int height, boolean useBuffer, boolean buildPopup,
            boolean tooltips, boolean mouseWheel) {
        super(chart, width, height, useBuffer, buildPopup, tooltips, mouseWheel);
    }

    @Override
    public void addChart(E newChart)
    {
        super.addChart(newChart);
    }

    @Override
    protected void handleChartAddition(E chart)
    {
        super.handleChartAddition(chart);

        if(chart != null)
        {
            chart.setDistanceMeasurementSupervisor(distanceMeasurementSupervisor);
            chart.setDataItemSupervisor(itemMovementSuprvisor);
        }
    }

    @Override
    protected void handleChartAddition(List<E> charts)
    {
        super.handleChartAddition(charts);

        for(E chart : charts)
        {
            if(chart != null)
            {
                chart.setDistanceMeasurementSupervisor(distanceMeasurementSupervisor);
                chart.setDataItemSupervisor(itemMovementSuprvisor);
            }
        }       
    }

    public static class MultipleChartPanelFactory implements AbstractChartPanelFactory<MultipleXYChartPanel<ChannelChart<?>>>
    {
        private static final MultipleChartPanelFactory INSTANCE = new MultipleChartPanelFactory();

        public static MultipleChartPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public MultipleXYChartPanel<ChannelChart<?>> buildEmptyPanel() 
        {
            return new MultipleXYChartPanel<>();
        }    	
    }

    ///////// DATA ITEM MOVEMENTS ////////////////


    public void setDataModificationSupervisor(Channel1DModificationSupervisor itemMovementSuprvisor)
    {
        this.itemMovementSuprvisor = itemMovementSuprvisor;

        for(E chart : getCharts())
        {
            if(chart != null)
            {
                chart.setDataItemSupervisor(itemMovementSuprvisor);
            }
        }
    }


    /////////DISTANCE MEASUREMENTS////////////////////////////


    public void setDistanceMeasurementSupervisor(DistanceMeasurementSupervisor distanceMeasurementSupervisor)
    {
        this.distanceMeasurementSupervisor = distanceMeasurementSupervisor;

        for(E chart : getCharts())
        {
            if(chart != null)
            {
                chart.setDistanceMeasurementSupervisor(distanceMeasurementSupervisor);
            }
        }

    }


    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        E selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addOrReplaceDistanceMeasurement(measurement);
        }   
    }

    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        E selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.removeDistanceMeasurement(measurement);
        }
    }

    public List<DistanceShapeFactors> getDistanceMeasurementShapeFactors()
    {
        E selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            return selectedChart.getDistanceMeasurementShapeFactors();
        }   
        return Collections.emptyList();
    }

    public void setDistanceMeasurementsAvailable(boolean available)
    {}
}

