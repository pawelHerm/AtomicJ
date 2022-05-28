
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
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.PlotRenderingInfo;

import atomicJ.data.units.UnitPoint2D;

public class CustomChartMouseEvent
{
    public static final String CURSOR_CHANGE_CONSUMED = "CursorChangeConsumed";
    public static final String MOUSE_DRAGGED_CONSUMED = "MouseDraggedConsumed";

    private final JFreeChart chart;
    private final ChartRenderingInfo info;
    private final MouseEvent trigger;
    private final ChartEntity entity;
    private final Point2D java2DPoint;
    private final Point2D dataPoint;
    private final UnitPoint2D unitPoint;

    private final boolean insideDataArea;
    private final boolean left;
    private final boolean right;
    private final boolean multiple;

    private final Set<ModifierKey> modifierKeys;
    private final HashMap<String, Boolean> consumptionsMap = new HashMap<>();

    public CustomChartMouseEvent(JFreeChart chart, ChartRenderingInfo info, MouseEvent trigger,
            ChartEntity entity, Point2D java2DPoint) 
    {
        this.chart = chart;
        this.info = info;
        this.trigger = trigger;
        this.entity = entity;
        this.java2DPoint = java2DPoint;
        this.modifierKeys = ModifierKey.getModifierKeys(trigger);

        if(chart instanceof CustomizableXYBaseChart && chart != null && info != null)
        {
            PlotRenderingInfo plotInfo = info.getPlotInfo(); 

            CustomizableXYBaseChart<?> customizableChart = (CustomizableXYBaseChart<?>)chart;
            this.dataPoint = customizableChart.getDataPoint(java2DPoint, plotInfo);
            this.unitPoint = customizableChart.getUnitDataPoint(java2DPoint, plotInfo);

            Rectangle2D dataArea = plotInfo.getDataArea();
            this.insideDataArea = dataArea.contains(java2DPoint);
        }
        else
        {
            this.dataPoint = null;
            this.unitPoint = null;
            this.insideDataArea = false;
        }

        this.multiple = trigger.getClickCount() >= 2;
        this.left = SwingUtilities.isLeftMouseButton(trigger);
        this.right = SwingUtilities.isRightMouseButton(trigger);

    }

    public JFreeChart getChart()
    {
        return chart;
    }

    public MouseEvent getTrigger()
    {
        return trigger;
    }

    public Set<ModifierKey> getModifierKeys()
    {
        return EnumSet.copyOf(modifierKeys);
    }

    public boolean isLeft()
    {
        return left;
    }

    public boolean isRight()
    {
        return right;
    }

    public boolean isSingle()
    {
        return !multiple;
    }

    public boolean isMultiple()
    {
        return multiple;
    }

    public ChartRenderingInfo getRenderingInfo()
    {
        return info;
    }

    public ChartEntity getEntity()
    {
        return entity;
    }

    public Point2D getJava2DPoint()
    {
        return java2DPoint;
    }

    public Rectangle2D getJava2DHotRectangle(double fraction)
    {        
        Rectangle2D dataArea = info.getChartArea();

        double widthNew = fraction*dataArea.getWidth();
        double heightNew = fraction*dataArea.getHeight();

        Rectangle2D hotRectangle = new Rectangle2D.Double(java2DPoint.getX() - widthNew/2, java2DPoint.getY() - heightNew/2, widthNew, heightNew);
        return hotRectangle;
    }

    public Rectangle2D getDataRectangle(double axisFraction)
    {
        if(chart instanceof CustomizableXYBaseChart)
        {
            return ((CustomizableXYBaseChart) chart).getDataSquare(dataPoint, axisFraction);
        }

        return null;
    }

    public Point2D getDataPoint()
    {
        return dataPoint;
    }

    public UnitPoint2D getUnitPoint()
    {
        return unitPoint;
    }

    public boolean isInsideDataArea()
    {
        return insideDataArea;
    }

    public boolean isConsumed(String consumptionType)
    {
        Boolean registeredConsumption = consumptionsMap.get(consumptionType);
        boolean consumed = (registeredConsumption == null) ? false : registeredConsumption;
        return consumed;
    }

    public void setConsumed(String consumptionType, boolean consumed)
    {
        consumptionsMap.put(consumptionType, consumed);
    }
}
