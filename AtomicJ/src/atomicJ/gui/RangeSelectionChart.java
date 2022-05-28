
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.ui.Layer;

public class RangeSelectionChart<E extends CustomizableXYPlot> extends Channel1DChart<E> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private boolean useReceiverGradient = true;
    private CaughtMakerEnd caughtMarker = CaughtMakerEnd.NONE;
    private final IntervalMarker markerDomain;

    private GradientPaintReceiver model;
    private final CustomizableXYPlot plot;
    private final Component parent;

    public RangeSelectionChart(Component parent, GradientPaintReceiver model, E plot, String plotName)
    {
        super(plot, plotName);

        this.parent = parent;
        this.model = model;
        this.plot = plot;		

        model.addPropertyChangeListener(this);

        double markerStart = model.getLowerBound();
        double markerEnd = model.getUpperBound();

        this.markerDomain = new CustomizableIntervalMarker("Range marker",markerStart, markerEnd, getPreferences().node("Marker"), 1.0f);	
        this.markerDomain.setPaint(new GradientPaint(model.getColorGradient()));

        plot.addDomainMarker(markerDomain, Layer.BACKGROUND);

        CustomizableNumberAxis domainAxis = ((CustomizableNumberAxis)plot.getDomainAxis());
        domainAxis.setRestoreDefaultAutoRange(true);
        domainAxis.setAutoRangeIncludesZero(false);		

        updateAxisAutoRange();
    }

    public void cleanUp()
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }
        this.model = null;
    }

    public void setRangeModel(GradientPaintReceiver rangeModel)
    {
        if(this.model != rangeModel)
        {
            this.model.removePropertyChangeListener(this);
        }
        this.model = rangeModel;
        this.model.addPropertyChangeListener(this);

        double lowerBound = model.getLowerBound();
        double upperBound = model.getUpperBound();

        markerDomain.setStartValue(lowerBound);
        markerDomain.setEndValue(upperBound);

        updateColorGradient();
        updateAxisAutoRange();
    }

    public void updateColorGradient()
    {
        ColorGradient colorGradient = model.getColorGradient();
        if(useReceiverGradient)
        {
            markerDomain.setPaint(new GradientPaint(colorGradient));
        }
    }

    public boolean isUseReceiverGradient()
    {
        return useReceiverGradient;
    }

    public void setUseReceiverGradient(boolean useReceiverGradient) 
    {
        this.useReceiverGradient = useReceiverGradient;
        if(this.useReceiverGradient)
        {
            markerDomain.setPaint(new GradientPaint(model.getColorGradient()));
        }

        fireChartChanged();
    }

    @Override
    public boolean isDomainZoomable()
    {
        boolean zoomable = (caughtMarker == CaughtMakerEnd.NONE) && super.isDomainZoomable();

        return zoomable;
    }

    @Override
    public boolean isRangeZoomable()
    {
        boolean zoomable = (caughtMarker == CaughtMakerEnd.NONE) && super.isRangeZoomable();

        return zoomable;
    }

    public boolean isLeftLimitCaught(double x, double reach)
    {			
        ValueAxis domainAxis = plot.getDomainAxis();

        double leftMarkerValue = markerDomain.getStartValue();

        double domainLength = domainAxis.getRange().getLength();
        double reachDomain = reach * domainLength;

        boolean leftCaught = Math.abs(x - leftMarkerValue) < reachDomain;
        return leftCaught;
    }

    public boolean isRightLimitCaught(double x, double reach)
    {
        ValueAxis domainAxis = plot.getDomainAxis();

        double rightMarkerValue = markerDomain.getEndValue();

        double domainLength = domainAxis.getRange().getLength();
        double reachDomain = reach * domainLength;

        boolean rightCaught = Math.abs(x - rightMarkerValue) < reachDomain;

        return rightCaught;
    }


    @Override
    public void chartMouseDragged(CustomChartMouseEvent evt)
    {
        super.chartMouseDragged(evt);

        Point2D pointJava2D = evt.getJava2DPoint();
        PlotRenderingInfo info = evt.getRenderingInfo().getPlotInfo();
        Point2D dataPoint = getDataPoint(pointJava2D, info);

        Rectangle2D dataArea = info.getDataArea();

        if(dataArea.contains(pointJava2D))
        {
            double chartX = dataPoint.getX();
            caughtMarker.respondToMouseDragged(chartX, model, markerDomain);            
        }
    }

    @Override
    public void chartMouseMoved(CustomChartMouseEvent evt)
    {
        super.chartMouseMoved(evt);

        Point2D pointJava2D = evt.getJava2DPoint();
        PlotRenderingInfo info = evt.getRenderingInfo().getPlotInfo();
        Point2D dataPoint = getDataPoint(pointJava2D, info);
        Rectangle2D dataArea = info.getDataArea();

        if(dataArea.contains(pointJava2D))
        {
            double chartX = dataPoint.getX();

            if(isLeftLimitCaught(chartX, 0.01)||isRightLimitCaught(chartX, 0.01))
            {
                parent.setCursor(HandCursors.getOpenHand());
            }
            else 
            {
                parent.setCursor(Cursor.getDefaultCursor());
            }	
        }
    }

    @Override
    public void chartMousePressed(CustomChartMouseEvent evt)
    {
        super.chartMousePressed(evt);

        Point2D pointJava2D = evt.getJava2DPoint();
        PlotRenderingInfo info = evt.getRenderingInfo().getPlotInfo();
        Point2D dataPoint = getDataPoint(pointJava2D, info);
        Rectangle2D dataArea = info.getDataArea();

        if(dataArea.contains(pointJava2D))
        {
            double chartX = dataPoint.getX();

            if(isLeftLimitCaught(chartX, 0.01))
            {
                caughtMarker = CaughtMakerEnd.LEFT; 
                parent.setCursor(HandCursors.getGrabbedHand());

                return;
            }
            if(isRightLimitCaught(chartX, 0.01))
            {
                caughtMarker = CaughtMakerEnd.RIGHT; 
                parent.setCursor(HandCursors.getGrabbedHand());			
                return;
            }
            else
            {
                caughtMarker = CaughtMakerEnd.NONE;
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if(RangeModel.LOWER_BOUND.equals(name))
        {
            double newVal = (double)evt.getNewValue();
            markerDomain.setStartValue(newVal);

            CustomizableXYPlot plot =  getCustomizablePlot();

            ValueAxis domainAxis = plot.getDomainAxis();
            double margin = 0.025*domainAxis.getRange().getLength();

            double lowerBoundOld = domainAxis.getLowerBound();
            double lowerBoundNew = newVal - margin;
            Range dataRange = plot.getDataRange(domainAxis);

            double newAutoRangeLowerBound = Math.min(lowerBoundNew, dataRange.getLowerBound());
            double newAutoRangeUpperBound = Math.max(model.getUpperBound() + margin, dataRange.getUpperBound());

            Range newAutoRange = new Range(newAutoRangeLowerBound, newAutoRangeUpperBound);
            domainAxis.setDefaultAutoRange(newAutoRange);

            if(lowerBoundNew<lowerBoundOld)
            {								
                domainAxis.setLowerBound(lowerBoundNew);
            }
        }
        else if(RangeModel.UPPER_BOUND.equals(name))
        {
            double valNew = (double)evt.getNewValue();
            markerDomain.setEndValue(valNew);

            CustomizableXYPlot plot =  getCustomizablePlot();
            ValueAxis domainAxis = plot.getDomainAxis();
            double margin = 0.025*domainAxis.getRange().getLength();

            double upperBoundOld = domainAxis.getUpperBound();
            double upperBoundNew = valNew + margin;

            Range dataRange = plot.getDataRange(domainAxis);

            double autoRangeUpperBoundNew = Math.max(upperBoundNew, dataRange.getUpperBound());
            double autoRangeLowerBoundNew = Math.min(model.getLowerBound() - margin, dataRange.getLowerBound());

            Range autoRangeNew = new Range(autoRangeLowerBoundNew, autoRangeUpperBoundNew);
            domainAxis.setDefaultAutoRange(autoRangeNew);

            if(upperBoundNew>upperBoundOld)
            {							
                domainAxis.setUpperBound(upperBoundNew);
            }
        }
        else if(GradientPaintReceiver.GRADIENT_COLOR.equals(name))
        {
            ColorGradient colorGradientNew = (ColorGradient)evt.getNewValue();
            if(useReceiverGradient)
            {
                markerDomain.setPaint(new GradientPaint(colorGradientNew) );
            }
        }
    }

    public void updateAxisAutoRange()
    {
        CustomizableXYPlot plot =  getCustomizablePlot();
        CustomizableNumberAxis domainAxis = (CustomizableNumberAxis)plot.getDomainAxis();

        Range dataRange = plot.getDataRange(domainAxis);
        if(dataRange != null)
        {
            double margin = 0.025*dataRange.getLength();

            double newAutoRangeUpperBound = Math.max(model.getUpperBound() + margin, dataRange.getUpperBound());
            double newAutoRangeLowerBound = Math.min(model.getLowerBound() - margin, dataRange.getLowerBound());

            Range newAutoRange = new Range(newAutoRangeLowerBound, newAutoRangeUpperBound);
            domainAxis.setDefaultAutoRange(newAutoRange);

            domainAxis.autoAdjustRange();
        }       
    }

    private static enum CaughtMakerEnd
    {
        RIGHT 
        {
            @Override
            public void respondToMouseDragged(double chartX, GradientPaintReceiver model, IntervalMarker markerDomain)
            {
                double rightLimit = Math.max(chartX, markerDomain.getStartValue());
                model.setUpperBound(rightLimit); 
                markerDomain.setEndValue(rightLimit);
            }
        }, 
        LEFT 
        {
            @Override
            public void respondToMouseDragged(double chartX, GradientPaintReceiver model, IntervalMarker markerDomain) 
            {
                double leftLimit = Math.min(chartX, markerDomain.getEndValue());
                model.setLowerBound(leftLimit);         
                markerDomain.setStartValue(leftLimit);
            }
        }, 
        NONE 
        {
            @Override
            public void respondToMouseDragged(double chartX, GradientPaintReceiver model, IntervalMarker markerDomain) {                
            }
        };

        public abstract void respondToMouseDragged(double chartX, GradientPaintReceiver model, IntervalMarker markerDomain);
    }
}
