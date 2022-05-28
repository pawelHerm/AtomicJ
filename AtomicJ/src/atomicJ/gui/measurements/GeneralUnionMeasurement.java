
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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorWrappedSigned;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.measurements.SimpleMeasurementComponent.MeasurementSimpleComponentSerializationProxy;

public class GeneralUnionMeasurement extends DistanceMeasurementDrawable implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final Map<Object, SimpleMeasurementComponent> simpleComponents = new LinkedHashMap<>();   
    private List<MeasurementComponent> componentMeasurements = new ArrayList<>();

    public GeneralUnionMeasurement(Collection<DistanceMeasurementDrawable> rois, Integer key, String label, DistanceMeasurementStyle style) 
    {
        super(label, key, style);

        for(DistanceMeasurementDrawable measurement : rois)
        {     
            SimpleMeasurementComponent simpleComponent = new SimpleMeasurementComponent(measurement, key, label);
            this.simpleComponents.put(simpleComponent.getKey(), simpleComponent);
        }

        buildComponentMeasurements();
    }

    public GeneralUnionMeasurement(GeneralUnionMeasurement that)
    {
        this(that, that.getStyle());
    }

    public GeneralUnionMeasurement(GeneralUnionMeasurement that, DistanceMeasurementStyle style)
    {
        this(that, style, that.getKey(), that.getLabel());
    }

    public GeneralUnionMeasurement(GeneralUnionMeasurement that, DistanceMeasurementStyle style, Integer key, String label)
    {
        super(that, style, key, label);

        for(SimpleMeasurementComponent component : that.simpleComponents.values())
        {
            SimpleMeasurementComponent componentCopy = component.copy(style, key, label);
            this.simpleComponents.put(componentCopy.getKey(), componentCopy);
        }

        buildComponentMeasurements();
    }

    public GeneralUnionMeasurement(Collection<SimpleMeasurementComponent> simpleComponents, DistanceMeasurementStyle style, Integer key, String label)
    {
        super(label, key, style);

        for(SimpleMeasurementComponent component : simpleComponents)
        {
            SimpleMeasurementComponent componentCopy = component.copy(style, key, label);
            this.simpleComponents.put(componentCopy.getKey(), componentCopy);
        }

        buildComponentMeasurements();
    }


    private void buildComponentMeasurements()
    {
        this.componentMeasurements = new ArrayList<>(simpleComponents.values());
    }


    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorSigned caughtAnchor = null;

        for(MeasurementComponent component : componentMeasurements)
        {
            AnnotationAnchorSigned currentAnchor = component.getCaughtAnchor(java2DPoint, dataPoint, dataRectangle);

            if(currentAnchor != null)
            {
                caughtAnchor = new AnnotationAnchorWrappedSigned(currentAnchor, getKey()); 
            }
        }

        return caughtAnchor;
    }

    @Override
    public AnnotationAnchorSigned setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();
        if(BasicAnnotationAnchor.CENTER.equals(coreAnchor) && modifierKeys.contains(ModifierKey.CONTROL))
        {
            return setPositionInAll(anchor, modifierKeys, startPoint, endPoint);
        }

        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();      
        Object key = innerAnchor.getKey();

        MeasurementComponent component = simpleComponents.get(key);

        AnnotationAnchorSigned currentAnchor = null;

        if(component != null)
        {
            currentAnchor = component.setPosition(innerAnchor, modifierKeys, startPoint, endPoint);

            buildComponentMeasurements();
            fireAnnotationChanged();
        }      

        AnnotationAnchorSigned newAnchor = (currentAnchor != null && Objects.equals(anchor.getKey(), getKey())) ? new AnnotationAnchorWrappedSigned(currentAnchor, getKey()) : null;
        return newAnchor;
    }

    public AnnotationAnchorSigned setPositionInAll(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint)
    {
        AnnotationAnchorSigned newAnchor = null;
        AnnotationAnchorSigned innerAnchor = anchor.getInnerAnchor();

        for(MeasurementComponent component : simpleComponents.values())
        {
            AnnotationAnchorSigned currentAnchor = component.setPositionInAll(innerAnchor, modifierKeys, startPoint, endPoint);
            newAnchor = (currentAnchor != null && Objects.equals(anchor.getKey(), getKey())) ? new AnnotationAnchorWrappedSigned(currentAnchor, getKey()) : null;
        }

        return newAnchor;       
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        boolean reshaped = false;

        for(MeasurementComponent component : simpleComponents.values())
        {
            reshaped = component.reshapeInResponseToMouseClick(modifierKeys, java2DPoint, dataPoint, dataRectangle);

            if(reshaped)
            {
                buildComponentMeasurements();
                fireAnnotationChanged();

                break;
            }
        }

        return reshaped;
    }

    @Override
    public GeneralUnionMeasurement copy()
    {
        return new GeneralUnionMeasurement(this);
    }

    @Override
    public GeneralUnionMeasurement copy(DistanceMeasurementStyle style)
    {
        return new GeneralUnionMeasurement(this, style);
    }

    @Override
    public GeneralUnionMeasurement copy(DistanceMeasurementStyle style, Integer key, String label) {
        return new GeneralUnionMeasurement(this, style, key, label);
    }


    @Override
    public Shape getDistanceShape()
    {     
        return buildDistanceShape();
    }

    private Shape buildDistanceShape()
    {
        GeneralPath shape = new GeneralPath();

        for(MeasurementComponent component : simpleComponents.values())
        {
            shape.append(component.getDistanceShape(), false);
        }

        return shape;
    }

    @Override
    public boolean isBoundaryClicked(Rectangle2D dataRectangle)
    {
        boolean clicked = false;

        for(MeasurementComponent component : simpleComponents.values())
        {
            clicked = clicked || component.isBoundaryClicked(dataRectangle);
        }

        return clicked;
    }

    @Override
    public boolean isClicked(Rectangle2D dataRectangle)
    {
        boolean clicked = false;

        for(MeasurementComponent component : simpleComponents.values())
        {
            clicked = clicked || component.isClicked(dataRectangle);
        }

        return clicked;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {        
        for(MeasurementComponent component : componentMeasurements)
        {
            component.draw(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info);
        }
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint, Point2D j2DCornerPoint, boolean forcedToDrawAbscissa, boolean forcedToDrawOrdinate)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            component.drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, j2DStartPoint, j2DEndPoint, j2DCornerPoint, forcedToDrawAbscissa, forcedToDrawOrdinate);
        }
    }

    @Override
    public void setLabel(String labelNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setLabel(labelNew, notify);
            }
        }

        super.setLabel(labelNew, notify);
    }

    @Override
    public void setVisible(boolean visibleNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setVisible(visibleNew, notify);
            }
        }

        super.setVisible(visibleNew, notify);
    }

    @Override
    public void setHighlighted(boolean highlightedNew, boolean notify)
    {        
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setHighlighted(highlightedNew, notify);
            }
        }

        super.setHighlighted(highlightedNew, notify);
    }

    @Override
    public void setLabelUnfinishedVisible(boolean visible, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setLabelUnfinishedVisible(visible, notify);
            }
        }

        super.setLabelUnfinishedVisible(visible, notify);
    }

    @Override
    public void setLabelFinishedVisible(boolean visible, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setLabelFinishedVisible(visible, notify);
            }
        }

        super.setLabelFinishedVisible(visible, notify);
    }

    @Override
    public void setPaintLabelFinished(Paint paintNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setPaintLabelFinished(paintNew, notify);
            }
        }

        super.setPaintLabelFinished(paintNew, notify);
    }

    @Override
    public void setPaintLabelUnfinished(Paint paintNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setPaintLabelUnfinished(paintNew, notify);
            }
        }

        super.setPaintLabelUnfinished(paintNew, notify);
    }

    @Override
    public void setPaintFinished(Paint paintNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setPaintFinished(paintNew, notify);
            }
        }

        super.setPaintFinished(paintNew, notify);
    }

    @Override
    public void setPaintUnfinished(Paint paintNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setPaintUnfinished(paintNew, notify);
            }
        }

        super.setPaintUnfinished(paintNew, notify);
    }

    @Override
    public void setStrokeFinished(Stroke strokeNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setStrokeFinished(strokeNew, notify);
            }
        }

        super.setStrokeFinished(strokeNew, notify);
    }

    @Override
    public void setStrokeUnfinished(Stroke strokeNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setStrokeUnfinished(strokeNew, notify);
            }
        }

        super.setStrokeUnfinished(strokeNew, notify);
    }

    @Override
    public void setLabelFontUnfinished(Font fontNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setLabelFontUnfinished(fontNew, notify);
            }
        }

        super.setLabelFontUnfinished(fontNew, notify);
    }



    @Override
    public void setLabelFontFinished(Font fontNew, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setLabelFontFinished(fontNew, notify);
            }
        }

        super.setLabelFontFinished(fontNew, notify);
    }




    @Override
    public void setLabelLengthwisePosition(float lengthwisePosition, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setLabelLengthwisePosition(lengthwisePosition, notify);
            }
        }

        super.setLabelLengthwisePosition(lengthwisePosition, notify);
    }



    @Override
    public void setLabelOffset(float labelOffset, boolean notify)
    {
        for(MeasurementComponent component : componentMeasurements)
        {
            for(DistanceMeasurementDrawable measurement : component.getDistanceMeasurements())
            {
                measurement.setLabelOffset(labelOffset, notify);
            }
        }

        super.setLabelOffset(labelOffset, notify);
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
    public boolean isComplex() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public DistanceShapeFactors getDistanceShapeFactors() 
    {
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
    public MeasurementProxy getProxy()
    {
        List<MeasurementSimpleComponentSerializationProxy> componentSerInfos = new ArrayList<>();

        for(SimpleMeasurementComponent component : simpleComponents.values())
        {
            componentSerInfos.add(component.getSerializationProxy());
        }

        return new MeasurementGeneralUnionSerializationProxy(componentSerInfos, getCustomLabel(), isFinished());
    }

    static class MeasurementGeneralUnionSerializationProxy implements MeasurementProxy
    {
        private static final long serialVersionUID = 1L;

        private final Collection<MeasurementSimpleComponentSerializationProxy> componentsSerInfos;
        private final boolean finished;
        private final String customLabel;

        private MeasurementGeneralUnionSerializationProxy(Collection<MeasurementSimpleComponentSerializationProxy> components, String customLabel, boolean finished)
        {
            this.componentsSerInfos = components;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public GeneralUnionMeasurement recreateOriginalObject(DistanceMeasurementStyle style, Integer unionKey) 
        {
            List<SimpleMeasurementComponent> simpleComponents = new ArrayList<>();

            for(MeasurementSimpleComponentSerializationProxy info : componentsSerInfos)
            {
                simpleComponents.add(info.recreateOriginalObject(style, unionKey));
            }

            String label = (customLabel != null) ? customLabel : unionKey.toString();
            GeneralUnionMeasurement measurement = new GeneralUnionMeasurement(simpleComponents, style, unionKey, label);
            measurement.setFinished(finished);

            return measurement;
        }     
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
