
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

package atomicJ.gui.profile;

import static atomicJ.gui.profile.PreferredProfileStyle.*;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.DistanceShapeFactors.DirectedPosition;
import atomicJ.gui.DistanceShapeFactors.KnobPositionTest;
import atomicJ.gui.annotations.AbstractCustomizableAnnotation;
import atomicJ.gui.annotations.AnnotationAnchorSigned;

public abstract class Profile extends AbstractCustomizableAnnotation implements Cloneable, PublicCloneable
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-12;

    public static final float DEFAULT_ARROW_LENGTH = 9.0f;
    public static final float DEFAULT_ARROW_WIDTH = 8.0f; 

    private volatile int currentKnobKey = 0;

    private boolean arrowheadVisibleUnfinished;
    private boolean arrowheadVisibleFinished;
    private float arrowheadLengthUnfinished;
    private float arrowheadLengthFinished;
    private float arrowheadWidthUnfinishedStandard;
    private float arrowheadWidthFinishedStandard;

    private List<Knob> knobs = new ArrayList<>();


    private boolean knobVisible;
    private KnobOrientation knobOrientation;
    private int knobWidth;
    private int knobHeight;

    public Profile(Integer key, String label, ProfileStyle style) 
    {	
        super(key, label, style);


        setConsistentWithStyle(style, false);    
    }

    public Profile(Profile that)
    {
        this(that, that.getStyle());
    }

    public Profile(Profile that, ProfileStyle style)
    {
        super(that, style);

        this.currentKnobKey = that.currentKnobKey;
        this.knobs = new ArrayList<>();

        for(Knob oldKnob : that.knobs)
        {
            Knob newKnob = new Knob(oldKnob);
            this.knobs.add(newKnob);
        }

        setConsistentWithStyle(style, false);
    }

    public abstract void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers);
    public abstract void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers);

    public abstract boolean isComplex();

    @Override
    public void setFinished(boolean finished, boolean notify)
    {
        double length = DistanceShapeFactors.getLength(getDistanceShape(), 0.f);

        double knobPosition = 0.5*length;

        Knob knob = new Knob(knobPosition, currentKnobKey++);
        this.knobs.add(knob);

        super.setFinished(finished, notify);
    }

    public boolean addKnob(double d)
    {		
        boolean newPosition = !knobPositionAlreadyPresent(d);
        if(newPosition)
        {
            Knob newKnob = new Knob(d, currentKnobKey++);
            this.knobs.add(newKnob);
            fireAnnotationChanged();
        }

        return newPosition;
    }

    private boolean knobPositionAlreadyPresent(double position)
    {
        boolean present = false;

        for(Knob knob : knobs)
        {
            double currentPosition = knob.getPosition();
            boolean found = Math.abs(currentPosition - position) < TOLERANCE;
            if(found)
            {
                present = found;
                break;
            }
        }

        return present;
    }

    public boolean moveKnob(int knobIndex, double positionNew)
    {
        boolean positionChanged = false;

        int n = knobs.size();
        boolean withinRange = (knobIndex >= 0 && knobIndex <n);

        if(withinRange)
        {
            Knob knob = knobs.get(knobIndex);

            double positionOld = knob.getPosition();
            positionChanged = Math.abs(positionNew - positionOld)>TOLERANCE;

            if(positionChanged)
            {
                knob.setPosition(positionNew);       
                fireAnnotationChanged();
            }			
        }	

        return positionChanged;
    }

    public boolean removeKnob(double knobPosition)
    {
        int n = knobs.size();

        boolean removed = false;
        int index = -1;

        for(int i = 0; i<n; i++)
        {
            Knob knob = knobs.get(i);
            double currentPosition = knob.getPosition();
            boolean found = Math.abs(currentPosition - knobPosition) < TOLERANCE;
            if(found)
            {
                index = i;
                break;
            }
        }

        if(index>=0)
        {	
            removed = true;
            this.knobs.remove(index);

            fireAnnotationChanged();
        }

        return removed;
    }

    public int getKnobCount()
    {
        int knobCount = knobs.size();
        return knobCount;
    }

    public double getKnobPosition(int index)
    {
        double position = Double.NaN;

        if(index >= 0 && index < knobs.size())
        {
            Knob knob = knobs.get(index);
            position = knob.getPosition();
        }

        return position;
    }

    public List<Double> getKnobPositions()
    {
        List<Double> positions =  new ArrayList<>();

        for(Knob knob : knobs)
        {
            Double position = knob.getPosition();
            positions.add(position);
        }

        return positions;
    }

    public boolean setKnobPositions(List<Double> knobPositionsNew)
    {		     
        boolean refreshed = knobsShouldBeRefreshed(knobPositionsNew);

        if(refreshed)
        {            
            this.knobs = new ArrayList<>();
            for(Double position : knobPositionsNew)
            {
                Knob knob = new Knob(position, currentKnobKey++);
                this.knobs.add(knob);
            }

            fireAnnotationChanged();
        }

        return refreshed;
    }

    private boolean knobsShouldBeRefreshed(List<Double> knobPositionsNew)
    {
        if(knobPositionsNew == null)
        {
            return false;
        }
        if(this.knobs == null)
        {
            return true;
        }

        int n = this.knobs.size();
        int m = knobPositionsNew.size();

        if(n != m)
        {
            return true;
        }

        boolean refresh = false;

        for(int i = 0; i<n; i++)
        {
            Knob knob = this.knobs.get(i);
            refresh = Math.abs(knob.getPosition() - knobPositionsNew.get(i))>TOLERANCE;

            if(refresh)
            {
                break;
            }

        }
        return refresh;
    }

    private void setConsistentWithStyle(ProfileStyle style, boolean notify)
    {
        this.knobVisible = style.isKnobVisible();
        this.knobOrientation = style.getKnobOrientation();
        this.knobWidth = style.getKnobWidth();
        this.knobHeight = style.getKnobHeight();

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

    public abstract Profile copy();

    public abstract Profile copy(ProfileStyle style);

    @Override
    public abstract ProfileProxy getProxy();

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

    public KnobSpecification getCaughtKnob(Point2D java2DPoint)
    {
        KnobSpecification knobPosition = null;

        int n = knobs.size();

        for(int i = 0; i<n; i++)
        {
            Knob knob = knobs.get(i);
            Shape hotspot = knob.getKnobHotSpot();

            if(hotspot != null && hotspot.contains(java2DPoint))
            {
                knobPosition = new KnobSpecification(getKey(), i, knob.getPosition());
                break;
            }
        }

        return knobPosition;
    }

    public abstract AnnotationModificationOperation setPosition(AnnotationAnchorSigned caughtProfileAnchor, Point2D pressedPoint, Point2D startPoint, Point2D endPoint, Set<ModifierKey> modifierKeys);

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

    public boolean isKnobVisible()
    {
        return knobVisible;
    }

    public void setKnobVisble(boolean knobVisibleNew)
    {
        setKnobVisble(knobVisibleNew, true);
    }

    public void setKnobVisble(boolean knobVisibleNew, boolean notify)
    {
        this.knobVisible = knobVisibleNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public KnobOrientation getKnobOrientation()
    {
        return knobOrientation;
    }

    public void setKnobOrientation(KnobOrientation knobOrientationNew)
    {
        setKnobOrientation(knobOrientationNew, true);
    }

    public void setKnobOrientation(KnobOrientation knobOrientationNew, boolean notify)
    {
        this.knobOrientation = knobOrientationNew;
        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public void setKnobWidth(int knobWidthNew)
    {
        setKnobWidth(knobWidthNew, true);
    }

    public void setKnobWidth(int knobWidthNew, boolean notify)
    {
        this.knobWidth = knobWidthNew;        
        if(notify)
        {
            fireAnnotationChanged();
        }	          
    }

    public int getKnobHeight()
    {
        return knobHeight;
    }

    public void setKnobHeight(int knobHeightNew)
    {
        setKnobHeight(knobHeightNew, true);
    }

    public void setKnobHeight(int knobHeightNew, boolean notify)
    {
        this.knobHeight = knobHeightNew;       
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
        else if(PROFILE_KNOB_VISIBLE.equals(property))
        {
            boolean knobVisible = (boolean)evt.getNewValue();
            setKnobVisble(knobVisible);
        }
        else if(PROFILE_KNOB_ORIENTATION.equals(property))
        {
            KnobOrientation knobOrientation = (KnobOrientation)evt.getNewValue();
            setKnobOrientation(knobOrientation);
        }
        else if(PROFILE_KNOB_WIDTH.equals(property))
        {
            int knobWidth = (int)evt.getNewValue();
            setKnobWidth(knobWidth);
        }
        else if(PROFILE_KNOB_HEIGHT.equals(property))
        {
            int knobHeight = (int)evt.getNewValue();
            setKnobHeight(knobHeight);
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
            clearKnobHotspots();
            return;
        }

        AffineTransform tr = getDataToJava2DTransformation(g2, plot, dataArea, domainAxis, rangeAxis, info);

        Shape distanceShapeTransformed = tr.createTransformedShape(getDistanceShape());
        Point2D j2DStartPoint = tr.transform(getStartPoint(), null);
        Point2D j2DEndPoint = tr.transform(getEndPoint(), null);

        g2.setPaint(getStrokePaint());
        g2.setStroke(getStroke());

        drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, j2DStartPoint, j2DEndPoint);

        Shape distanceShapeTransformedStroke = getStroke().createStrokedShape(distanceShapeTransformed);

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

        if(isKnobVisible() && isFinished())
        {
            for(int i = 0 ; i<knobs.size(); i++)
            {
                Knob knob = knobs.get(i);
                drawKnob(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, knob, tr);
            }
        }
        else
        {
            clearKnobHotspots();
        }
    }

    private Shape getArrowShape()
    {
        float arrowheadWidth = getArrowheadWidth();
        float arrowheadLength = getArrowheadLength();

        float halfWidth = 0.5f*arrowheadWidth;

        Path2D arrowhead = new GeneralPath();
        arrowhead.moveTo(0, 0);
        arrowhead.lineTo(-arrowheadLength, halfWidth);
        arrowhead.lineTo(-arrowheadLength, -halfWidth);
        arrowhead.closePath();

        return arrowhead;
    }

    private Shape getKnobShape()
    {
        Path2D knob = new GeneralPath();
        knob.moveTo(0, 0);
        knob.lineTo(-0.5*knobWidth, -knobHeight);
        knob.lineTo(0.5*knobWidth, -knobHeight);
        knob.closePath();

        return knob;
    }

    protected abstract void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint); 

    private void clearKnobHotspots()
    {
        for(Knob d: knobs)
        {
            d.setKnobHotSpot(null);
        }
    }

    private double getPerpendicularAngle(Line2D segment, boolean vertical, int key)
    {
        double dx = segment.getX2() - segment.getX1();
        double dy = segment.getY2() - segment.getY1();

        double angle =  vertical ? Math.atan2(dy, dx) : Math.atan2(dx, -dy) - Math.PI/2;        
        angle += knobOrientation.getRotationAngle(key);


        return angle;
    }

    private DirectedPosition getControlPoint(double position)
    {			        
        DirectedPosition dirPos = DistanceShapeFactors.getDirectedPosition(getDistanceShape(), position);
        return dirPos;
    }

    private void drawKnob(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,ValueAxis domainAxis, ValueAxis rangeAxis,
            int rendererIndex, PlotRenderingInfo info, Knob knob, AffineTransform tr) 
    {
        if(isVisible())
        {	        
            DirectedPosition position = getControlPoint(knob.getPosition()).transform(tr);
            Point2D controlPoint = position.getPoint();

            double controlX = controlPoint.getX();
            double controlY = controlPoint.getY();

            boolean isVertical = (plot.getOrientation() == PlotOrientation.VERTICAL);
            AffineTransform trKnob =
                    AffineTransform.getRotateInstance(getPerpendicularAngle(position.getSegment(),isVertical, knob.getKey()), controlX, controlY);
            trKnob.translate(controlX, controlY);


            Shape rotatedKnobShape = trKnob.createTransformedShape(getKnobShape());

            g2.setPaint(getStrokePaint());
            g2.fill(rotatedKnobShape);

            knob.setKnobHotSpot(rotatedKnobShape);
        }
    }

    //calculates the corresponding knob position by projection of point on the profile
    public double getCorrespondingKnobPosition(Point2D dataPoint)
    {
        KnobPositionTest knobPosition = DistanceShapeFactors.getKnobPosition(getDistanceShape(), dataPoint);

        return knobPosition.getLengthPosition();
    }



    public boolean equalsUpToStyle(Profile that)
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

        if(!this.knobs.equals(that.knobs))
        {
            return false;
        }

        return true;
    }
}

