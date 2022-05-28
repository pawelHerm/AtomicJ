
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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import atomicJ.gui.measurements.DistanceMeasurementDrawable;


public abstract class CustomizableXYPlot extends CustomizableXYBasePlot implements PreferencesSource, AxisSource,
RoamingTitleChangeListener
{
    private static final long serialVersionUID = 1L;

    private Map<Object, DistanceMeasurementDrawable> distanceMeasurements = new LinkedHashMap<>();

    private ScaleBar domainScaleBar;
    private ScaleBar rangeScaleBar;


    public CustomizableXYPlot(Preferences pref, 
            PreferredScaleBarStyle domainScaleBarStyle, PreferredScaleBarStyle rangeScaleBarStyle)
    {
        super(pref);

        this.domainScaleBar = new ScaleBar(false, domainScaleBarStyle);
        this.rangeScaleBar = new ScaleBar(true, rangeScaleBarStyle);

        this.domainScaleBar.addChangeListener(this);
        this.rangeScaleBar.addChangeListener(this);        
    }

    public CustomizableXYPlot(Preferences pref, String styleKey)
    {
        super(pref, styleKey);

        this.domainScaleBar = new ScaleBar(false, pref.node("DomainScaleBar"));
        this.rangeScaleBar = new ScaleBar(true, pref.node("RangeScaleBar"));

        this.domainScaleBar.addChangeListener(this);
        this.rangeScaleBar.addChangeListener(this);       
    }

    public ScaleBar getDomainScaleBar()
    {	
        return domainScaleBar;
    }

    public ScaleBar getRangeScaleBar()
    {
        return rangeScaleBar;
    }

    ///////////////////////////DISTANCE MEASUREMENTS////////////////////////////////

    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        removeDistanceMeasurement(measurement, true);
    }

    public void removeDistanceMeasurement(DistanceMeasurementDrawable measurement, boolean notify)
    {
        measurement.removeChangeListener(this);
        distanceMeasurements.remove(measurement.getKey());

        if(notify)
        {
            fireChangeEvent();
        }
    }

    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement)
    {
        addOrReplaceDistanceMeasurement(measurement, true);
    }

    public void addOrReplaceDistanceMeasurement(DistanceMeasurementDrawable measurement, boolean notify)
    {
        DistanceMeasurementDrawable previousMeasurement = distanceMeasurements.get(measurement.getKey());
        if(previousMeasurement != null)
        {
            previousMeasurement.removeChangeListener(this);
        }

        measurement.addChangeListener(this);
        distanceMeasurements.put(measurement.getKey(), measurement);        

        if(notify)
        {
            fireChangeEvent();
        }
    }

    public List<DistanceMeasurementDrawable> getDistanceMeasurements()
    {
        return new ArrayList<>(distanceMeasurements.values());
    }

    public void drawDistanceMeasurements(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) 
    {        
        for(DistanceMeasurementDrawable distanceMeasurement : distanceMeasurements.values())
        {
            ValueAxis xAxis = getDomainAxis();
            ValueAxis yAxis = getRangeAxis();
            distanceMeasurement.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
        }
    }

    @Override
    public void axisChanged(AxisChangeEvent event)
    {
        Axis axis = event.getAxis();

        if(axis instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis customizableAxis = (CustomizableNumberAxis)axis;

            AxisLocation preferredLocation = customizableAxis.getPreferredAxisLocation();

            if(preferredLocation != null)
            {
                if(axis.equals(getDomainAxis()))
                {
                    setDomainAxisLocation(preferredLocation);
                }
                else if(axis.equals(getRangeAxis()))
                {
                    setRangeAxisLocation(preferredLocation);
                }
            }			
        }
        super.axisChanged(event);
    }

    @Override
    public CustomizableXYPlot clone()
    {
        CustomizableXYPlot copy = null;
        try 
        {
            copy = (CustomizableXYPlot) super.clone();

            copy.domainScaleBar = (ScaleBar) this.domainScaleBar.clone();
            copy.rangeScaleBar = (ScaleBar) this.rangeScaleBar.clone();

            copy.domainScaleBar.addChangeListener(copy);
            copy.rangeScaleBar.addChangeListener(copy);

            copy.distanceMeasurements = new LinkedHashMap<>();
        } catch (CloneNotSupportedException e) 
        {
            e.printStackTrace();
        }
        return copy;
    }

    @Override
    public void drawAnnotations(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) 
    {
        super.drawAnnotations(g2, dataArea, info);        

        drawDistanceMeasurements(g2, dataArea,  info); 
        drawScaleBars(g2, dataArea, info);
    }

    public void drawScaleBars(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) 
    {
        ValueAxis xAxis = getDomainAxis();
        ValueAxis yAxis = getRangeAxis();

        this.domainScaleBar.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
        this.rangeScaleBar.draw(g2, this, dataArea, xAxis, yAxis, 0, info);
    }
}
