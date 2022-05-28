
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

package atomicJ.gui.curveProcessing;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import atomicJ.gui.HandCursors;
import atomicJ.gui.MultipleXYChartPanel;



public class CroppingPanel extends MultipleXYChartPanel<CroppingChart>
{
    private static final long serialVersionUID = 1L;

    private static final int RIGHT_CAUGHT = 0;
    private static final int LEFT_CAUGHT = 1;
    private static final int LOWER_CAUGHT = 2;
    private static final int UPPER_CAUGHT = 3;
    private static final int NONE_CAUGHT = -1;

    private int caughtMarker;

    public CroppingPanel()
    {
        super(null);
    }

    public CroppingPanel(CroppingChart chart)
    {
        super(chart);
    }

    public double getLeftCropping()
    {
        CroppingChart chart = getSelectedChart();
        double t = chart.getLeftTrimming();
        return t;
    }

    public double getRightCropping()
    {
        CroppingChart chart = getSelectedChart();
        double t = chart.getRightTrimming();
        return t;
    }

    public double getLowerCropping()
    {
        CroppingChart chart = getSelectedChart();
        double t = chart.getLowerTrimming();
        return t;
    }

    public double getUpperCropping()
    {
        CroppingChart chart = getSelectedChart();
        double t = chart.getUpperTrimming();
        return t;
    }

    @Override
    public void mouseDragged(MouseEvent evt)
    {
        super.mouseDragged(evt);

        Point screenPoint = evt.getPoint();
        Point2D dataPoint = getDataPoint(screenPoint);
        double chartX = dataPoint.getX();
        double chartY = dataPoint.getY();
        CroppingChart chart = getSelectedChart();

        switch(caughtMarker)
        {
        case NONE_CAUGHT: {break;}
        case LEFT_CAUGHT: {chart.setLeftLimit(chartX);break;}
        case RIGHT_CAUGHT: {chart.setRightLimit(chartX);break;}
        case LOWER_CAUGHT: {chart.setLowerLimit(chartY);break;}
        case UPPER_CAUGHT: {chart.setUpperLimit(chartY);break;}
        }
        return;
    }

    @Override
    public void mouseMoved(MouseEvent evt)
    {
        super.mouseMoved(evt);

        Point screenPoint = evt.getPoint();
        Point2D dataPoint = getDataPoint(screenPoint);
        double chartX = dataPoint.getX();
        double chartY = dataPoint.getY();
        CroppingChart chart = getSelectedChart();

        if(chart.isLeftLimitCaught(chartX, 0.01)||chart.isRightLimitCaught(chartX, 0.01)
                ||chart.isLowerLimitCaught(chartY, 0.01)||chart.isUpperLimitCaught(chartY, 0.01))
        {
            setCursor(HandCursors.getOpenHand());
        }
        else 
        {
            setCursor(Cursor.getDefaultCursor());
        }				
    }

    @Override
    public void mousePressed(MouseEvent evt)
    {
        super.mousePressed(evt);

        Point screenPoint = evt.getPoint();
        Point2D dataPoint = getDataPoint(screenPoint);
        double chartX = dataPoint.getX();
        double chartY = dataPoint.getY();
        CroppingChart chart = getSelectedChart();

        if(chart.isLeftLimitCaught(chartX, 0.01))
        {
            caughtMarker = LEFT_CAUGHT; setCursor(HandCursors.getGrabbedHand());
            return;
        }
        if(chart.isRightLimitCaught(chartX, 0.01))
        {
            caughtMarker = RIGHT_CAUGHT; setCursor(HandCursors.getGrabbedHand());
            return;
        }
        if(chart.isLowerLimitCaught(chartY, 0.01))
        {
            caughtMarker = LOWER_CAUGHT;setCursor(HandCursors.getGrabbedHand());
            return;
        }
        if(chart.isUpperLimitCaught(chartY, 0.01))
        {
            caughtMarker = UPPER_CAUGHT;setCursor(HandCursors.getGrabbedHand());
            return;
        }
        else
        {
            caughtMarker = NONE_CAUGHT;
        }
        return;
    }
}