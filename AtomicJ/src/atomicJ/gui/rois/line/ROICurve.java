
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

package atomicJ.gui.rois.line;

import static atomicJ.gui.profile.PreferredProfileStyle.*;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.DistanceShapeFactors.DirectedPosition;
import atomicJ.gui.annotations.AbstractCustomizableAnnotation;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.utilities.GeometryUtilities;

public abstract class ROICurve extends AbstractCustomizableAnnotation implements Cloneable, PublicCloneable
{
    private static final long serialVersionUID = 1L;

    public static final float DEFAULT_ARROW_LENGTH = 9.0f;
    public static final float DEFAULT_ARROW_WIDTH = 8.0f; 

    private boolean arrowheadVisibleUnfinished;
    private boolean arrowheadVisibleFinished;
    private float arrowheadLengthUnfinished;
    private float arrowheadLengthFinished;
    private float arrowheadWidthUnfinishedStandard;
    private float arrowheadWidthFinishedStandard;


    public ROICurve(Integer key, String label, ProfileStyle style) 
    {	
        super(key, label, style);


        setConsistentWithStyle(style, false);    
    }

    public ROICurve(ROICurve that)
    {
        this(that, that.getStyle());
    }

    public ROICurve(ROICurve that, ProfileStyle style)
    {
        super(that, style);

        setConsistentWithStyle(style, false);
    }

    public double[][] getVertices()
    {
        Shape distanceShape = getDistanceShape();
        double[][] vertices = GeometryUtilities.getPolygonVertices(distanceShape.getPathIterator(null, 0.));

        return vertices;
    }

    public abstract void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers);
    public abstract void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers);

    public abstract boolean isComplex();

    private void setConsistentWithStyle(ProfileStyle style, boolean notify)
    {
        this.arrowheadVisibleUnfinished = style.isArrowheadVisibleUnfinishedStandard();
        this.arrowheadVisibleFinished = style.isArrowheadVisibleFinishedStandard();

        this.arrowheadLengthUnfinished = style.getArrowheadLengthUnfinishedStandard();
        this.arrowheadLengthFinished = style.getArrowheadLengthFinishedStandard();

        this.arrowheadWidthUnfinishedStandard = style.getArrowheadWidthUnfinishedStandard();
        this.arrowheadWidthFinishedStandard = style.getArrowheadWidthFinishedStandard();

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    @Override
    public ProfileStyle getStyle()
    {
        return (ProfileStyle)super.getStyle();
    }

    public abstract ROICurve copy();

    public abstract ROICurve copy(ProfileStyle style);

    @Override
    public abstract ROICurveSerializationProxy getProxy();

    public abstract MouseInputMode getMouseInputMode(MouseInputMode oldMode);

    public boolean isBoundaryClicked(Rectangle2D dataRectangle)
    {
        boolean clicked = false;
        if(isVisible())
        {
            //we want the dataRectangle to intersect the outline of the distance shape
            Shape distanceShape = getDistanceShape();

            //we can't use Stroke stroke = getStroke();, 
            //as the profile shape is in data units, not printer points
            //we cant't use clicked = distanceShape.intersects(dataRectangle) && !distanceShape.contains(dataRectangle);
            //because it gives true when the dataRectangle intersect the line between the first and the last
            //point of profile, which may not be part of the profile
            clicked = new BasicStroke(0.f).createStrokedShape(distanceShape).intersects(dataRectangle);
        }
        return clicked;
    }

    public boolean isClicked(Rectangle2D dataRectangle)
    {
        return isBoundaryClicked(dataRectangle);
    }

    public abstract AnnotationAnchorSigned setPosition(AnnotationAnchorSigned caughtProfileAnchor, Point2D startPoint, Point2D endPoint);

    //returns true if Profile was modified
    public abstract boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle);


    public abstract double getStartX();

    public abstract double getStartY();

    public abstract double getEndX();

    public abstract double getEndY();

    public abstract Point2D getStartPoint();

    public abstract Point2D getEndPoint();

    public abstract Shape getDistanceShape();

    public abstract DistanceShapeFactors getDistanceShapeFactors();

    public abstract double getLength();

    public boolean isArrowheadVisible()
    {
        boolean visible = isFinished() ? arrowheadVisibleFinished : arrowheadVisibleUnfinished;

        return visible;
    }

    public boolean isArrowheadVisibleUnfinished()
    {
        return arrowheadVisibleUnfinished;
    }

    public void setArrowheadVisibleUnfinishedStandard(boolean arrowheadVisible)
    {
        setArrowheadVisibleUnfinished(arrowheadVisible, true);
    }

    public void setArrowheadVisibleUnfinished(boolean arrowheadVisible, boolean notify)
    {
        this.arrowheadVisibleUnfinished = arrowheadVisible;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public boolean isArrowheadVisibleFinished()
    {
        return arrowheadVisibleFinished;
    }

    public void setArrowheadVisibleFinishedStandard(boolean arrowheadVisible)
    {
        setArrowheadVisibleFinished(arrowheadVisible, true);
    }

    public void setArrowheadVisibleFinished(boolean arrowheadVisible, boolean notify)
    {
        this.arrowheadVisibleFinished = arrowheadVisible;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }	

    public float getArrowheadLength()
    {
        float length = isFinished() ? arrowheadLengthFinished : arrowheadLengthUnfinished;
        return length;
    }

    public float getArrowheadLengthUnfinished()
    {
        return arrowheadLengthUnfinished;
    }

    public void setArrowheadLengthUnfinishedStandard(float arrowheadLength)
    {
        setArrowheadLengthUnfinished(arrowheadLength, true);
    }

    public void setArrowheadLengthUnfinished(float arrowheadLength, boolean notify)
    {
        this.arrowheadLengthUnfinished = arrowheadLength;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public float getArrowheadLengthFinished()
    {
        return arrowheadLengthFinished;
    }

    public void setArrowheadLengthFinishedStandard(float arrowheadLength)
    {
        setArrowheadLengthFinished(arrowheadLength, true);
    }

    public void setArrowheadLengthFinished(float arrowheadLength, boolean notify)
    {
        this.arrowheadLengthFinished = arrowheadLength;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public float getArrowheadWidth()
    {
        float width = isFinished() ? arrowheadWidthFinishedStandard : arrowheadWidthUnfinishedStandard;
        return width;
    }

    public float getArrowheadWidthUnfinishedStandard()
    {
        return arrowheadWidthUnfinishedStandard;
    }

    public void setArrowheadWidthUnfinishedStandard(float arrowheadWidth)
    {
        setArrowheadWidthUnfinished(arrowheadWidth, true);
    }

    public void setArrowheadWidthUnfinished(float arrowheadWidth, boolean notify)
    {
        this.arrowheadWidthUnfinishedStandard = arrowheadWidth;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public float getArrowheadWidthFinished()
    {
        return arrowheadWidthFinishedStandard;
    }

    public void setArrowheadWidthFinishedStandard(float arrowheadWidth)
    {
        setArrowheadWidthFinished(arrowheadWidth, true);
    }

    public void setArrowheadWidthFinished(float arrowheadWidth, boolean notify)
    {
        this.arrowheadWidthFinishedStandard = arrowheadWidth;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(ProfileStyle.PROFILE_STYLE_COMPLETELY_CHANGED.equals(property))
        {
            setConsistentWithStyle(getStyle(), true);
        }
        else if(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_STANDARD.equals(property))
        {
            boolean visible = (boolean)evt.getNewValue();
            setArrowheadVisibleUnfinishedStandard(visible);
        }
        else if(PROFILE_ARROWHEAD_VISIBLE_FINISHED_STANDARD.equals(property))
        {
            boolean visible = (boolean)evt.getNewValue();
            setArrowheadVisibleFinishedStandard(visible);
        }
        else if(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_STANDARD.equals(property))
        {
            float length = (float)evt.getNewValue();
            setArrowheadLengthUnfinishedStandard(length);
        }
        else if(PROFILE_ARROWHEAD_LENGTH_FINISHED_STANDARD.equals(property))
        {
            float length = (float)evt.getNewValue();
            setArrowheadLengthFinishedStandard(length);
        }		
        else if(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_STANDARD.equals(property))
        {
            float width = (float)evt.getNewValue();
            setArrowheadWidthUnfinishedStandard(width);
        }	
        else if(PROFILE_ARROWHEAD_WIDTH_FINISHED_STANDARD.equals(property))
        {
            float width = (float)evt.getNewValue();
            setArrowheadWidthFinishedStandard(width);
        }
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,int rendererIndex, PlotRenderingInfo info) 
    {     
        if(!isVisible())
        {
            return;
        }

        AffineTransform tr = getDataToJava2DTransformation(g2, plot, dataArea, domainAxis, rangeAxis, info);

        Shape distanceShapeTransformed = tr.createTransformedShape(getDistanceShape());
        Point2D j2DStartPoint = tr.transform(getStartPoint(), null);
        Point2D j2DEndPoint = tr.transform(getEndPoint(), null);

        g2.setPaint(getStrokePaint());
        g2.setStroke(getStroke());

        drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, j2DStartPoint, j2DEndPoint);

        Shape  distanceShapeTransformedStroke = getStroke().createStrokedShape(distanceShapeTransformed);

        boolean drawArrowhead = isArrowheadVisible() && !isHighlighted();
        if(drawArrowhead)
        {
            Shape arrowhead = getArrowShape();
            DirectedPosition directedPosition = DistanceShapeFactors.getDirectedPositionAtPathEnd(distanceShapeTransformed);

            Point2D centerPoint = directedPosition.getPoint();
            double angle = directedPosition.getAngle();

            AffineTransform arrowHeadTransform = AffineTransform.getRotateInstance(angle, centerPoint.getX(), centerPoint.getY());
            arrowHeadTransform.translate(centerPoint.getX(), centerPoint.getY());

            Shape arrowheadRotated = arrowHeadTransform.createTransformedShape(arrowhead);
            g2.fill(arrowheadRotated);

            Area distanceShapeTrimmedLocal = new Area(distanceShapeTransformedStroke);
            distanceShapeTrimmedLocal.subtract(new Area(arrowHeadTransform.createTransformedShape(arrowhead.getBounds2D())));

            distanceShapeTransformedStroke = distanceShapeTrimmedLocal;
        }

        boolean visible = distanceShapeTransformedStroke.intersects(dataArea);

        if (visible) 
        {
            g2.fill(distanceShapeTransformedStroke);
        }

        String toolTip = getToolTipText();
        String url = getURL();
        if (toolTip != null || url != null) 
        {
            Shape entityShape = getStroke().createStrokedShape(distanceShapeTransformed);

            addEntity(info, entityShape, rendererIndex, toolTip, url);
        }
        drawKeyLabel(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, distanceShapeTransformed);
    }

    private Shape getArrowShape()
    {
        float arrowheadWidth = getArrowheadWidth();
        float arrowheadLength = getArrowheadLength();

        float halfWidth = 0.5f*arrowheadWidth;

        GeneralPath arrowhead = new GeneralPath();
        arrowhead.moveTo(0, 0);
        arrowhead.lineTo(-arrowheadLength, halfWidth);
        arrowhead.lineTo(-arrowheadLength, -halfWidth);
        arrowhead.closePath();

        return arrowhead;
    }

    protected abstract void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint); 

    private DirectedPosition getControlPoint(double position)
    {			        
        DirectedPosition dirPos = DistanceShapeFactors.getDirectedPosition(getDistanceShape(), position);
        return dirPos;
    }

    public boolean equalsUpToStyle(ROICurve that)
    {	    
        if(that == null)
        {
            return false;
        }

        //equal methods in Line2D is useless, as it is inherited from Object
        if(!ShapeUtilities.equal(new GeneralPath(this.getDistanceShape()), new GeneralPath(that.getDistanceShape())))
        {          
            return false;
        }
        return true;
    }
}

