
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

package atomicJ.gui.stack;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.PlotRenderingInfo;

import atomicJ.gui.CustomChartMouseEvent;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.Channel2DPlot;
import atomicJ.gui.MapChart;

public class StackMapChart<E extends Channel2DPlot> extends MapChart<E>
{
    private static final long serialVersionUID = 1L;

    public StackMapChart(E plot, String key)
    {
        super(plot, key);
    }	

    public StackMapChart(E plot, String key, boolean addLegend)
    {
        super(plot, key, addLegend);
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
