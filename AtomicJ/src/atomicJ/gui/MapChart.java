
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

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;


import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.plot.PlotRenderingInfo;

import atomicJ.data.Datasets;


public class MapChart <E extends Channel2DPlot> extends Channel2DChart<E> 
{
    private static final long serialVersionUID = 1L;

    protected static final String MOUSE_TRIGGERED_JUMP_TO_FIGURES = "MOUSE_TRIGGERED_JUMP_TO_FIGURES";
    private SpectroscopySupervisor spectroscopySupervisor;

    public MapChart(E plot, String key)
    {
        super(plot, key);
    }	

    public MapChart(E plot, String key, boolean addLegend)
    {
        super(plot, key, addLegend);
    }	

    public void setSpectroscopySupervisor(SpectroscopySupervisor supervisor)
    {
        this.spectroscopySupervisor = supervisor;
    }

    public void jumpToFiguresFor(Point2D p)
    {        
        if(spectroscopySupervisor != null)
        {
            spectroscopySupervisor.jumpToFigures(p);
        }		
    }

    @Override
    public void chartMouseClicked(CustomChartMouseEvent event) 
    {			
        MouseEvent mouseEvent = event.getTrigger();
        boolean multiple = mouseEvent.getClickCount()>=2;

        super.chartMouseClicked(event);

        if(((!event.isConsumed(MOUSE_TRIGGERED_JUMP_TO_FIGURES)) && (!event.isConsumed(CustomizableXYBaseChart.CHART_EDITION))) && isNormalMode())
        {
            if(multiple)
            {			
                ChartEntity entity = event.getEntity();

                if(entity instanceof LightweightXYItemEntity || entity instanceof PlotEntity)
                {
                    ChartRenderingInfo chartInfo = event.getRenderingInfo();
                    PlotRenderingInfo info = chartInfo.getPlotInfo();
                    Point2D pointData = getDataPoint(event.getJava2DPoint(), info);

                    if(pointData != null)
                    {          
                        jumpToFiguresFor(pointData);
                    }

                    event.setConsumed(MOUSE_TRIGGERED_JUMP_TO_FIGURES, true);
                }													
            }		
        }
    }

    @Override
    public MapChart<E> createChart()
    {
        E originalPlot = getCustomizablePlot();                 
        E plot = (E) originalPlot.clone();
        return  new MapChart<>(plot, Datasets.MAP_PLOT, false);
    }
}
