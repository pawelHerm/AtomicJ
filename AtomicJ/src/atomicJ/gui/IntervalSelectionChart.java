
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Layer;

import atomicJ.data.Datasets;


public class IntervalSelectionChart extends Channel1DChart<Channel1DPlot>
{
    private static final long serialVersionUID = 1L;

    public static final String DOMAIN_START = "domainStart";
    public static final String DOMAIN_END = "domainEnd";
    public static final String RANGE_START = "rangeStart";
    public static final String RANGE_END = "rangeEnd";	
    public static final String DOMAIN_MARKER_VISIBLE = "domainMarkerVisible";
    public static final String RANGE_MARKER_VISIBLE = "rangeMarkerVisible";

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final CustomizableIntervalMarker domainMarker; 
    private final CustomizableIntervalMarker rangeMarker;	

    private boolean domainMarkerVisible = false;
    private boolean rangeMarkerVisible = false;

    public IntervalSelectionChart(Channel1DPlot plot)
    {
        super(plot, Datasets.TRIMMING_PLOT);

        this.domainMarker = new CustomizableIntervalMarker("Domain interval",0, 0, getPreferences().node("MarkerDomain"), 0.2f);
        this.rangeMarker = new CustomizableIntervalMarker("Range interval", 0, 0, getPreferences().node("MarkerRange"), 0.2f);
    }

    public void getLeftBound(double t)
    {
        domainMarker.setStartValue(t);		
    }

    public void setLeftBound(double newVal)
    {
        double oldVal = domainMarker.getStartValue();
        if(oldVal != newVal)
        {
            domainMarker.setStartValue(newVal);		
            propertyChangeSupport.firePropertyChange(DOMAIN_START, oldVal, newVal);
        }
    }

    public void getRightBound(double t)
    {
        domainMarker.setEndValue(t);
    }

    public void setRightBound(double newVal)
    {
        double oldVal = domainMarker.getEndValue();
        if(oldVal != newVal)
        {
            domainMarker.setEndValue(newVal);		
            propertyChangeSupport.firePropertyChange(DOMAIN_END, oldVal, newVal);
        }	
    }

    public void getLowerBound(double t)
    {
        rangeMarker.setStartValue(t);
    }

    public void setLowerBound(double newVal)
    {
        double oldVal = rangeMarker.getStartValue();
        if(oldVal != newVal)
        {
            rangeMarker.setStartValue(newVal);		
            propertyChangeSupport.firePropertyChange(RANGE_START, oldVal, newVal);
        }	
    }

    public void getUpperBound(double t)
    {
        rangeMarker.setEndValue(t);
    }

    public void setUpperBound(double newVal)
    {
        double oldVal = rangeMarker.getEndValue();
        if(oldVal != newVal)
        {
            rangeMarker.setEndValue(newVal);		
            propertyChangeSupport.firePropertyChange(RANGE_END, oldVal, newVal);
        }	
    }

    public boolean isDomainMarkerVisible()
    {
        return domainMarkerVisible;
    }

    public void setDomainMarkerVisible(boolean newVal)
    {
        boolean oldVal = domainMarkerVisible;
        if(oldVal != newVal)
        {
            domainMarkerVisible = newVal;

            XYPlot plot = getCustomizablePlot();

            if(domainMarkerVisible)
            {
                plot.addDomainMarker(domainMarker, Layer.FOREGROUND);
            }
            else
            {
                plot.removeDomainMarker(domainMarker);
            }


            propertyChangeSupport.firePropertyChange(DOMAIN_MARKER_VISIBLE, oldVal, newVal);
        }
    }

    public boolean isRangeMarkerVisible()
    {
        return rangeMarkerVisible;
    }

    public void setRangeMarkerVisible(boolean newVal)
    {
        boolean oldVal = rangeMarkerVisible;
        if(oldVal != newVal)
        {
            rangeMarkerVisible = newVal;

            XYPlot plot = getCustomizablePlot();

            if(rangeMarkerVisible)
            {
                plot.addRangeMarker(rangeMarker, Layer.FOREGROUND);
            }
            else
            {
                plot.removeRangeMarker(rangeMarker);
            }

            propertyChangeSupport.firePropertyChange(RANGE_MARKER_VISIBLE, oldVal, newVal);
        }
    }


    public boolean isLeftBoundCaught(double x, double reach)
    {
        ValueAxis domainAxis = getCustomizablePlot().getDomainAxis();

        double leftMarkerValue = domainMarker.getStartValue();

        double domainLength = domainAxis.getRange().getLength();
        double reachDomain = reach * domainLength;

        boolean caught = Math.abs(x - leftMarkerValue) < reachDomain;        
        return caught;
    }

    public boolean isRightBoundCaught(double x, double reach)
    {
        ValueAxis domainAxis = getCustomizablePlot().getDomainAxis();

        double rightMarkerValue = domainMarker.getEndValue();

        double domainLength = domainAxis.getRange().getLength();
        double reachDomain = reach * domainLength;

        boolean caught = Math.abs(x - rightMarkerValue) < reachDomain;     
        return caught;
    }

    public boolean isUpperBoundCaught(double y, double reach)
    {
        ValueAxis rangeAxis = getCustomizablePlot().getRangeAxis();

        double upperMarkerValue = rangeMarker.getEndValue();

        double rangeLength = rangeAxis.getRange().getLength();
        double reachRrange = reach * rangeLength;

        boolean caught = Math.abs(y - upperMarkerValue) < reachRrange;
        return caught;
    }

    public boolean isLowerBoundCaught(double y, double reach)
    {
        ValueAxis rangeAxis = getCustomizablePlot().getRangeAxis();

        double lowerMarkerValue = rangeMarker.getStartValue();

        double rangeLength = rangeAxis.getRange().getLength();
        double reachRrange = reach * rangeLength;

        boolean caught = Math.abs(y - lowerMarkerValue) < reachRrange;      
        return caught;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
