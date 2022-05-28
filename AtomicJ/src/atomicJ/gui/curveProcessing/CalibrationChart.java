
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

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;
import org.jfree.ui.Layer;

import atomicJ.data.Datasets;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.CustomChartMouseEvent;
import atomicJ.gui.CustomizableIntervalMarker;
import atomicJ.gui.HandCursors;
import atomicJ.gui.Channel1DPlot;

public class CalibrationChart extends Channel1DChart<Channel1DPlot>
{
    private static final long serialVersionUID = 1L;

    private static final int RIGHT_CAUGHT = 0;
    private static final int LEFT_CAUGHT = 1;
    private static final int NONE_CAUGHT = -1;

    private final CustomizableIntervalMarker marker;
    private int caughtMarker;

    private CalibrationSupervisor supervisor;

    public CalibrationChart(Channel1DPlot plot)
    {
        super(plot, Datasets.CALIBRATION_PLOT);

        this.marker = new CustomizableIntervalMarker("Range marker", 0, 0, plot.getPreferences().node("Marker"),0.5f); 
        plot.addDomainMarker(marker, Layer.BACKGROUND);
    }

    public void setCalibrationSupervisor(CalibrationSupervisor supervisor)
    {
        this.supervisor = supervisor;

        if(supervisor != null)
        {
            setRange(supervisor.getRange());
        }
    }

    @Override
    public void chartMouseMoved(CustomChartMouseEvent event)
    {
        super.chartMouseMoved(event);

        double chartX = event.getDataPoint().getX();

        boolean limitCaught = isLeftLimitCaught(chartX, 0.01) || isRightLimitCaught(chartX, 0.01);
        Cursor cursor = limitCaught ? HandCursors.getOpenHand() : Cursor.getDefaultCursor();
        supervisor.requestCursorChange(cursor);
    }

    @Override
    public void chartMouseDragged(CustomChartMouseEvent event)
    {
        super.chartMouseDragged(event);

        double chartX = event.getDataPoint().getX();

        switch(caughtMarker)
        {
        case LEFT_CAUGHT: {setLowerLimit(chartX);break;}
        case RIGHT_CAUGHT: {setUpperLimit(chartX);break;}
        }
    }

    @Override
    public void chartMousePressed(CustomChartMouseEvent event)
    {
        super.chartMousePressed(event);

        double chartX = event.getDataPoint().getX();

        if(isLeftLimitCaught(chartX, 0.01))
        {
            caughtMarker = LEFT_CAUGHT;
            supervisor.requestCursorChange(HandCursors.getGrabbedHand());
        }
        else if(isRightLimitCaught(chartX, 0.01))
        {
            caughtMarker = RIGHT_CAUGHT;
            supervisor.requestCursorChange(HandCursors.getGrabbedHand());
        }
        else
        {
            caughtMarker = NONE_CAUGHT;
        }
    }

    private void setLowerLimit(double t)
    {
        supervisor.requestLowerRangeBound(t);
    }

    private void setUpperLimit(double limit)
    {     
        supervisor.requestUpperRangeBound(limit);
    }

    public void setRange(Range range)
    {
        if(range != null)
        {
            marker.setStartValue(range.getLowerBound());              
            marker.setEndValue(range.getUpperBound()); 

            supervisor.setRange(marker.getRange());
        }
    }

    public boolean isLeftLimitCaught(double x, double reach)
    {
        ValueAxis domainAxis = getCustomizablePlot().getDomainAxis();

        double leftLimit = marker.getStartValue();

        double domainLength = domainAxis.getRange().getLength();
        double reachRadius = reach * domainLength;

        boolean caught = Math.abs(x - leftLimit)<reachRadius;
        return caught;
    }

    public boolean isRightLimitCaught(double x, double reach)
    {		
        ValueAxis domainAxis = getCustomizablePlot().getDomainAxis();
        double rightLimit = marker.getEndValue();

        double domainLength = domainAxis.getRange().getLength();
        double reachRadius = reach * domainLength;

        boolean caught = Math.abs(x - rightLimit)<reachRadius;
        return caught;
    }

    @Override
    protected boolean isChartElementCaught()
    {
        boolean superCaught = super.isChartElementCaught();
        boolean caught = superCaught || (caughtMarker != NONE_CAUGHT);

        return caught;
    }
}
