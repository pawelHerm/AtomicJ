
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


import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;
import org.jfree.ui.Layer;

import atomicJ.data.Datasets;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.CustomizableIntervalMarker;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.Channel1DPlot;
import atomicJ.gui.Channel1DPlot.Channel1DPlotFactory;

public class CroppingChart extends Channel1DChart<CustomizableXYPlot>
{
    private static final long serialVersionUID = 1L;

    private final CustomizableIntervalMarker markerDomain;
    private final CustomizableIntervalMarker markerRange;	

    private final boolean trimDomain;
    private final boolean trimRange;

    private final Dataset1DCroppingModel<?> model;

    public CroppingChart(Channel1DPlotFactory plotFactory, Dataset1DCroppingModel<?> model)
    {
        this(plotFactory.getPlot(model.getCurve()),model);
    }

    private CroppingChart(Channel1DPlot plot, Dataset1DCroppingModel<?> model)
    {
        super(plot, Datasets.TRIMMING_PLOT);

        this.model = model;
        this.trimDomain = model.isDomainToBeCropped();
        this.trimRange = model.isRangeToBeCropped();

        if(trimDomain)
        {
            this.markerDomain = buildMarkerDomain();
            plot.addDomainMarker(markerDomain, Layer.BACKGROUND);
        }
        else
        {
            this.markerDomain = null;
        }
        if(trimRange)
        {
            this.markerRange = buildMarkerRange();
            plot.addRangeMarker(markerRange, Layer.BACKGROUND);
        }
        else
        {
            this.markerRange = null;
        }
    }

    private CustomizableIntervalMarker buildMarkerDomain()
    {
        Range fullDomainRange = model.getFullDomainRange();
        double domainStart = fullDomainRange.getLowerBound();
        double domainEnd = fullDomainRange.getUpperBound();
        double leftTrimming = model.getLeftCropping();
        double rightTrimming = model.getRightCropping();

        double markerStart = domainStart;
        double markerEnd = domainEnd;
        if(!Double.isNaN(leftTrimming))
        {
            markerStart = markerStart + leftTrimming;
        }
        if(!Double.isNaN(rightTrimming))
        {
            markerEnd = markerEnd - rightTrimming;
        }

        return new CustomizableIntervalMarker("Domain interval",markerStart, markerEnd, getPreferences().node("MarkerDomain"), 0.2f);
    }

    private CustomizableIntervalMarker buildMarkerRange()
    {
        Range fullRangeRange = model.getFullRangeRange();
        double rangeStart = fullRangeRange.getLowerBound();
        double rangeEnd = fullRangeRange.getUpperBound();

        double lowerTrimming = model.getLowerCropping();
        double upperTrimming = model.getUpperCropping();

        double markerStart = rangeStart;
        double markerEnd = rangeEnd;

        if(!Double.isNaN(lowerTrimming))
        {
            markerStart = markerStart + lowerTrimming;
        }
        if(!Double.isNaN(upperTrimming))
        {
            markerEnd = markerEnd - upperTrimming;
        }

        return new CustomizableIntervalMarker("Range interval",markerStart, markerEnd, getPreferences().node("MarkerRange"), 0.2f);             
    }


    public double getLeftTrimming()
    {	
        double leftTrimming = Double.NaN;

        if(trimDomain)
        {
            Range fullDomainRange = model.getFullDomainRange();
            leftTrimming = markerDomain.getStartValue() - fullDomainRange.getLowerBound();
        }

        return leftTrimming;
    }

    public void setLeftTrimming(double t)
    {
        if(trimDomain)
        {
            Range fullDomainRange = model.getFullDomainRange();
            setLeftLimit(fullDomainRange.getLowerBound() + t);
        }
    }

    public double getRightTrimming()
    {
        double rightTrimming = Double.NaN;

        if(trimDomain)
        {
            Range fullDomainRange = model.getFullDomainRange();
            rightTrimming = fullDomainRange.getUpperBound() - markerDomain.getEndValue();
        }

        return rightTrimming;
    }

    public void setRightTrimming(double t)
    {
        if(trimDomain)
        {
            Range fullDomainRange = model.getFullDomainRange();
            setRightLimit(fullDomainRange.getUpperBound() - t);
        }
    }

    public double getLowerTrimming()
    {			
        double lowerTrimming = Double.NaN;

        if(trimRange)
        {
            Range fullDomainRange = model.getFullRangeRange();
            lowerTrimming = markerRange.getStartValue() - fullDomainRange.getLowerBound();
        }

        return lowerTrimming;
    }

    public void setLowerTrimming(double t)
    {
        if(trimRange)
        {
            Range fullRangeRange = model.getFullRangeRange();
            setUpperLimit(fullRangeRange.getLowerBound() + t);
        }
    }

    public double getUpperTrimming()
    {
        double upperTrimming = Double.NaN;

        if(trimRange)
        {
            Range fullRangeRange = model.getFullRangeRange();            
            upperTrimming = fullRangeRange.getUpperBound() - markerRange.getEndValue();
        }

        return upperTrimming;
    }

    public void setUpperTrimming(double t)
    {
        if(trimRange)
        {
            Range fullRangeRange = model.getFullRangeRange();
            setUpperLimit(fullRangeRange.getUpperBound() - t);
        }
    }

    public void setLeftLimit(double t)
    {
        if(trimDomain)
        {
            markerDomain.setStartValue(t);	
        }
    }

    public void setRightLimit(double t)
    {
        if(trimDomain)
        {
            markerDomain.setEndValue(t);
        }
    }

    public void setLowerLimit(double t)
    {
        if(trimRange)
        {
            markerRange.setStartValue(t);

        }
    }
    public void setUpperLimit(double t)
    {
        if(trimRange)
        {
            markerRange.setEndValue(t);
        }
    }

    public boolean isLeftLimitCaught(double x, double reach)
    {	
        boolean caught = false;

        if(trimDomain)
        {
            ValueAxis domainAxis = getCustomizablePlot().getDomainAxis();

            double leftMarkerValue = markerDomain.getStartValue();

            double domainLength = domainAxis.getRange().getLength();
            double reachDomain = reach * domainLength;

            caught = Math.abs(x - leftMarkerValue)<reachDomain;
        }

        return caught;
    }

    public boolean isRightLimitCaught(double x, double reach)
    {
        boolean caught = false;

        if(trimDomain)
        {
            ValueAxis domainAxis = getCustomizablePlot().getDomainAxis();

            double rightMarkerValue = markerDomain.getEndValue();

            double domainLength = domainAxis.getRange().getLength();
            double reachDomain = reach * domainLength;

            caught = Math.abs(x - rightMarkerValue)<reachDomain;
        }

        return caught;
    }

    public boolean isUpperLimitCaught(double y, double reach)
    {
        boolean caught = false;

        if(trimRange)
        {
            ValueAxis rangeAxis = getCustomizablePlot().getRangeAxis();

            double upperMarkerValue = markerRange.getEndValue();

            double rangeLength = rangeAxis.getRange().getLength();
            double reachRrange = reach * rangeLength;

            caught = Math.abs(y - upperMarkerValue)<reachRrange;          
        }

        return caught;
    }

    public boolean isLowerLimitCaught(double y, double reach)
    {
        boolean caught = false;

        if(trimRange)
        {
            ValueAxis rangeAxis = getCustomizablePlot().getRangeAxis();

            double lowerMarkerValue = markerRange.getStartValue();

            double rangeLength = rangeAxis.getRange().getLength();
            double reachRrange = reach * rangeLength;

            caught = Math.abs(y - lowerMarkerValue)<reachRrange;          
        }

        return caught;
    }

    @Override
    public boolean isDomainZoomable()
    {
        return false;
    }

    @Override
    public boolean isRangeZoomable()
    {
        return false;
    }
}
