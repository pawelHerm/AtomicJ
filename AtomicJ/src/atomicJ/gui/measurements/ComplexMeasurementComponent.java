
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

package atomicJ.gui.measurements;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.PublicCloneable;

import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorWrappedSigned;

public class ComplexMeasurementComponent extends DistanceMeasurementDrawable implements MeasurementComponent, Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final Map<Object, MeasurementComponent> componentMeasurements = new LinkedHashMap<>();

    public ComplexMeasurementComponent(Set<? extends MeasurementComponent> measurementComponents, Integer key, String label, DistanceMeasurementStyle style) 
    {
        super(label, key, style);

        for(MeasurementComponent component : measurementComponents)
        {
            componentMeasurements.put(component.getKey(), component);
        }
    }

    public ComplexMeasurementComponent(ComplexMeasurementComponent that, DistanceMeasurementStyle style)
    {
        super(that, style);

        for(MeasurementComponent measurement : componentMeasurements.values())
        {
            componentMeasurements.put(measurement.getKey(), (MeasurementComponent) measurement.copy());
        }
    }

    public ComplexMeasurementComponent(ComplexMeasurementComponent that)
    {
        this(that, that.getStyle());
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorSigned anchor = null;

        for(MeasurementComponent component : componentMeasurements.values())
        {
            AnnotationAnchorSigned currentAnchor = component.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);

            if(currentAnchor != null)
            {
                anchor  = new AnnotationAnchorWrappedSigned(currentAnchor, getKey());
                break;
            }
        }

        return anchor;
    }

    @Override
    public AnnotationAnchorSigned setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();
        Object key = innerAnchor.getKey();

        MeasurementComponent component = componentMeasurements.get(key);

        AnnotationAnchorSigned newAnchor = null;

        if(component != null)
        {
            AnnotationAnchorSigned currentAnchor = component.setPosition(innerAnchor,modifierKeys, startPoint, endPoint);
            newAnchor = (currentAnchor != null  && Objects.equals(getKey(), anchor.getKey())) ? new AnnotationAnchorWrappedSigned(currentAnchor, getKey()) : null;
        }     

        return newAnchor;
    }

    @Override
    public AnnotationAnchorSigned setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        for(MeasurementComponent component : componentMeasurements.values())
        {
            AnnotationAnchorSigned currentAnchor = component.setPositionInAll(innerAnchor, modifierKeys, startPoint, endPoint);
            newAnchor = (currentAnchor != null && Objects.equals(getKey(), anchor.getKey())) ? new AnnotationAnchorWrappedSigned(currentAnchor, getKey()) : null;
        }

        return newAnchor;       
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        boolean modified = false;

        for(MeasurementComponent r : componentMeasurements.values())
        {
            modified = modified || r.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);
            if(modified)
            {
                break;
            }
        }

        return modified;
    }

    @Override
    public ComplexMeasurementComponent copy()
    {
        return new ComplexMeasurementComponent(this);
    }

    @Override
    public ComplexMeasurementComponent copy(DistanceMeasurementStyle style)
    {
        return new ComplexMeasurementComponent(this, style);
    }

    @Override
    public ComplexMeasurementComponent copy(DistanceMeasurementStyle style, Integer key, String label)
    {
        return new ComplexMeasurementComponent(this, style);
    }

    @Override
    public Shape getDistanceShape()
    {
        return buildDistanceShape();
    }

    private Shape buildDistanceShape()
    {
        GeneralPath shape = new GeneralPath();

        for(MeasurementComponent component : componentMeasurements.values())
        {
            shape.append(component.getDistanceShape(), false);
        }

        return shape;
    }

    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isMeasurement())
        {
            return oldMode;
        }
        return MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE;
    }

    @Override
    public List<DistanceMeasurementDrawable> getDistanceMeasurements() 
    {
        List<DistanceMeasurementDrawable> mesurementsDrawable = new ArrayList<>();
        mesurementsDrawable.add(this);

        for(MeasurementComponent component : componentMeasurements.values())
        {
            mesurementsDrawable.addAll(component.getDistanceMeasurements());
        }

        return mesurementsDrawable;
    }

    @Override
    public boolean isComplex() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DistanceShapeFactors getDistanceShapeFactors() {
        return DistanceShapeFactors.getShapeFactors(getDistanceShape(), 0);
    }

    @Override
    public Point2D getStartPoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Point2D getEndPoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getStartX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getStartY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getEndX() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getEndY() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot,
            Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,
            int rendererIndex, PlotRenderingInfo info, Point2D j2DStartPoint,
            Point2D j2DEndPoint, Point2D j2DCornerPoint,
            boolean forcedToDrawAbscissa, boolean forcedToDrawOrdinate)
    {
        for(MeasurementComponent component : componentMeasurements.values())
        {
            for(DistanceMeasurementDrawable mesurement: component.getDistanceMeasurements())
            {
                mesurement.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, j2DStartPoint, j2DEndPoint, j2DCornerPoint, forcedToDrawAbscissa, forcedToDrawOrdinate);
            }
        }        
    }

    @Override
    public MeasurementProxy getProxy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers) {
        // TODO Auto-generated method stub

    }
}
