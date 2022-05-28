
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


import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.ObjectUtilities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.CustomizableNumberAxis;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.DistanceShapeFactors.DirectedPosition;
import atomicJ.gui.annotations.AbstractCustomizableAnnotation;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.utilities.GeometryUtilities;

import static atomicJ.gui.measurements.PreferredDistanceMeasurementStyle.*;

public abstract class DistanceMeasurementDrawable extends AbstractCustomizableAnnotation implements DistanceMeasurement
{
    private static final long serialVersionUID = 1L;

    private boolean drawAbscissaMeasurementUnfinished;
    private boolean drawAbscissaMeasurementFinished;

    private boolean drawOrdinateMeasurementUnfinished;
    private boolean drawOrdinateMeasurementFinished;

    private boolean diagonalDrawn;
    private boolean abscissaDrawn;
    private boolean ordinateDrawn;
    private double drawnAbscissaAngle;
    private double drawnOrdinateAngle;

    public DistanceMeasurementDrawable(Integer key, DistanceMeasurementStyle style) 
    {	
        super(key, style);
        setConsistentWithStyle(style, false);       
    }

    public DistanceMeasurementDrawable(String label, Integer key, DistanceMeasurementStyle style) 
    {   
        super(key, label, style);
        setConsistentWithStyle(style, false);       
    }

    public DistanceMeasurementDrawable(DistanceMeasurementDrawable that)
    {
        this(that, that.getStyle());
    }

    public DistanceMeasurementDrawable(DistanceMeasurementDrawable that, DistanceMeasurementStyle style)
    {
        super(that, style);    
        setConsistentWithStyle(style, false);      
    }

    public DistanceMeasurementDrawable(DistanceMeasurementDrawable that, DistanceMeasurementStyle style, Integer key, String label)
    {
        super(that, style, key, label);
        setConsistentWithStyle(style, false);
    }

    @Override
    public abstract DistanceMeasurementDrawable copy();
    public abstract DistanceMeasurementDrawable copy(DistanceMeasurementStyle style);
    public abstract DistanceMeasurementDrawable copy(DistanceMeasurementStyle style, Integer key, String label);

    public abstract void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers);
    public abstract void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers);

    @Override
    public abstract MeasurementProxy getProxy();

    @Override
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

    private void setConsistentWithStyle(DistanceMeasurementStyle style, boolean notify)
    {
        this.drawAbscissaMeasurementUnfinished = style.isDrawAbscissaMeasurementUnfinished();
        this.drawAbscissaMeasurementFinished = style.isDrawAbscissaMeasurementFinished();

        this.drawOrdinateMeasurementUnfinished = style.isDrawOrdinateMeasurementUnfinished();
        this.drawOrdinateMeasurementFinished = style.isDrawOrdinateMeasurementFinished();

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    @Override
    public DistanceMeasurementStyle getStyle()
    {
        return (DistanceMeasurementStyle)super.getStyle();
    }

    public boolean isDrawAbscissaMeasurement()
    {
        boolean drawAbscissaMeasurement = isFinished() ? drawAbscissaMeasurementFinished : drawAbscissaMeasurementUnfinished;
        return drawAbscissaMeasurement;
    }

    public void setDrawAbsicssaMeasurementUnfinished(boolean drawNew)
    {
        this.setDrawAbsicssaMeasurementUnfinished(drawNew, true);
    }

    public void setDrawAbsicssaMeasurementUnfinished(boolean drawNew, boolean notify)
    {
        this.drawAbscissaMeasurementUnfinished = drawNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public void setDrawAbsicssaMeasurementFinished(boolean drawNew)
    {
        this.setDrawAbsicssaMeasurementFinished(drawNew, true);
    }

    public void setDrawAbsicssaMeasurementFinished(boolean drawNew, boolean notify)
    {
        this.drawAbscissaMeasurementFinished = drawNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public boolean isDrawOrdinateMeasurement()
    {
        boolean drawOrdinateMeasurement = isFinished() ? drawOrdinateMeasurementFinished : drawOrdinateMeasurementUnfinished;
        return drawOrdinateMeasurement;
    }

    public void setDrawOrdinateMeasurementUnfinished(boolean drawNew)
    {
        this.setDrawOrdinateMeasurementUnfinished(drawNew, true);
    }

    public void setDrawOrdinateMeasurementUnfinished(boolean drawNew, boolean notify)
    {
        this.drawOrdinateMeasurementUnfinished = drawNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public void setDrawOrdinateMeasurementFinished(boolean drawNew)
    {
        this.setDrawOrdinateMeasurementFinished(drawNew, true);
    }

    public void setDrawOrdinateMeasurementFinished(boolean drawNew, boolean notify)
    {
        this.drawOrdinateMeasurementFinished = drawNew;

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    public DecimalFormat getDecimalFormat()
    {
        return getStyle().getDecimalFormat(isFinished());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(DRAW_ABSCISSA_MEASUREMENT_UNFINISHED.equals(property))
        {
            boolean drawNew = (boolean)evt.getNewValue();
            setDrawAbsicssaMeasurementUnfinished(drawNew);
        }
        else if(DRAW_ABSCISSA_MEASUREMENT_FINISHED.equals(property))
        {            
            boolean drawNew = (boolean)evt.getNewValue();
            setDrawAbsicssaMeasurementFinished(drawNew);
        }
        else if(DRAW_ORDINATE_MEASUREMENT_UNFINISHED.equals(property))
        {
            boolean drawNew = (boolean)evt.getNewValue();
            setDrawOrdinateMeasurementUnfinished(drawNew);
        }
        else if(DRAW_ORDINATE_MEASUREMENT_FINISHED.equals(property))
        {
            boolean drawNew = (boolean)evt.getNewValue();
            setDrawOrdinateMeasurementFinished(drawNew);
        }
        else
        {
            fireAnnotationChanged();
        }
    }

    public Line2D getAbsicssaLine()
    {
        Line2D line = new Line2D.Double(getStartX(), getStartY(), getEndX(), getStartY());

        return line;
    }

    public Line2D getOrdinateLine()
    {
        Line2D line = new Line2D.Double(getEndX(), getStartY(),getEndX(), getEndY());
        return line;
    }

    public Point2D getCornerPoint()
    {
        Point2D corner = new Point2D.Double(getEndX(), getStartY());
        return corner;
    }

    public abstract Point2D getStartPoint();
    public abstract Point2D getEndPoint();

    public abstract double getStartX();

    public abstract double getStartY();

    public abstract double getEndX();

    public abstract double getEndY();

    public boolean areUnitsDifferent(ValueAxis domainAxis, ValueAxis rangeAxis)
    {
        PrefixedUnit domainUnit = getDataUnit(domainAxis);
        PrefixedUnit rangeUnit = getDataUnit(rangeAxis);
        boolean unitsDifferent = !ObjectUtilities.equal(domainUnit, rangeUnit);

        return unitsDifferent;
    }



    public String getDistanceLabel(ValueAxis domainAxis, ValueAxis rangeAxis)
    {
        double distance = getDistanceShapeFactors().getLength();

        PrefixedUnit dataDomainUnit = getDataUnit(domainAxis);
        PrefixedUnit dataRangeUnit = getDataUnit(rangeAxis);

        boolean unitsEqual = ObjectUtilities.equal(dataDomainUnit, dataRangeUnit);

        if(!unitsEqual || dataDomainUnit ==  null)
        {
            return getDecimalFormat().format(distance);
        }

        PrefixedUnit displayedDomainUnit = getDisplayedUnit(domainAxis);
        double conversionFactor = dataDomainUnit.getConversionFactorTo(displayedDomainUnit);

        String label = getDecimalFormat().format(conversionFactor*distance) + " " + displayedDomainUnit.getFullName();

        return label;
    }

    public String getAbscissaLabel(ValueAxis domainAxis)
    {
        double abscissaLength = GeometryUtilities.getLength(getAbsicssaLine());
        PrefixedUnit dataUnit = getDataUnit(domainAxis);
        if(dataUnit == null)
        {
            return getDecimalFormat().format(abscissaLength);
        }

        PrefixedUnit displayedUnit = getDisplayedUnit(domainAxis);
        double conversionFactor = dataUnit.getConversionFactorTo(displayedUnit);

        String label = getDecimalFormat().format(conversionFactor*abscissaLength) + " " + displayedUnit.getFullName();

        return label;
    }

    public String getOrdinateLabel(ValueAxis rangeAxis)
    {
        double ordinateLength = GeometryUtilities.getLength(getOrdinateLine());
        PrefixedUnit dataUnit = getDataUnit(rangeAxis);
        if(dataUnit == null)
        {
            return getDecimalFormat().format(ordinateLength);
        }

        PrefixedUnit displayedUnit = getDisplayedUnit(rangeAxis);
        double conversionFactor = dataUnit.getConversionFactorTo(displayedUnit);

        String label = getDecimalFormat().format(conversionFactor*ordinateLength) + " " + displayedUnit.getFullName();

        return label;
    }

    private PrefixedUnit getDisplayedUnit(ValueAxis axis)
    {
        PrefixedUnit unit = null;
        if(axis instanceof CustomizableNumberAxis)
        {
            Quantity quantity = ((CustomizableNumberAxis) axis).getDisplayedQuantity();
            if(quantity.hasDimension())
            {
                unit = quantity.getUnit();
            }                      
        }

        return unit;
    }


    private PrefixedUnit getDataUnit(ValueAxis axis)
    {
        PrefixedUnit unit = null;
        if(axis instanceof CustomizableNumberAxis)
        {
            Quantity quantity = ((CustomizableNumberAxis) axis).getDataQuantity();
            if(quantity.hasDimension())
            {
                unit = quantity.getUnit();
            }                      
        }

        return unit;
    }

    public abstract AnnotationAnchorSigned setPosition(AnnotationAnchorSigned caughtDistanceMeasurementAchor, Set<ModifierKey> modifierKeys, Point2D startPoint, Point2D endPoint);

    @Override
    public boolean isClicked(Rectangle2D dataRectangle)
    {
        boolean clicked = false;

        if(isVisible())
        {
            if(diagonalDrawn)
            {
                clicked = clicked || isBoundaryClicked(dataRectangle); 
            }                      
            if(abscissaDrawn)
            {
                clicked = clicked || dataRectangle.intersectsLine(getAbsicssaLine());
            }
            if(ordinateDrawn)
            {
                clicked = clicked || dataRectangle.intersectsLine(getOrdinateLine());
            }
        }

        return clicked;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,int rendererIndex, PlotRenderingInfo info) 
    {
        if(!isVisible())
        {
            return;
        }


        AffineTransform tr  = getDataToJava2DTransformation(g2, plot, dataArea, domainAxis, rangeAxis, info);

        g2.setPaint(getStrokePaint());
        g2.setStroke(getStroke());

        Point2D j2DStartPoint = tr.transform(getStartPoint(), null);
        Point2D j2DEndPoint = tr.transform(getEndPoint(), null);
        Point2D j2DCornerPoint = tr.transform(getCornerPoint(), null);

        boolean diagonalPossible = !areUnitsDifferent(domainAxis, rangeAxis);

        Shape distanceShapeTransformed = tr.createTransformedShape(getDistanceShape());

        boolean diagonalVisible = distanceShapeTransformed.intersects(dataArea);

        diagonalDrawn = diagonalVisible && diagonalPossible;
        if (diagonalDrawn) 
        {
            g2.draw(distanceShapeTransformed);
        }

        Shape lineAbscissa = tr.createTransformedShape(getAbsicssaLine());
        Shape lineOrdinate = tr.createTransformedShape(getOrdinateLine());

        double abscissaLength = DistanceShapeFactors.getLength(lineAbscissa, 0);
        double ordinateLength = DistanceShapeFactors.getLength(lineOrdinate, 0);

        boolean forcedToDrawAbscissa = (!diagonalPossible && (abscissaLength >= ordinateLength));
        boolean forcedToDrawOrdinate = (!diagonalPossible && (ordinateLength > abscissaLength));

        this.abscissaDrawn = forcedToDrawAbscissa || isDrawAbscissaMeasurement();
        this.ordinateDrawn = forcedToDrawOrdinate || isDrawOrdinateMeasurement();


        drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, j2DStartPoint, j2DEndPoint, j2DCornerPoint, forcedToDrawAbscissa, forcedToDrawOrdinate);

        boolean abscissaLeftSomeSpace = drawAbscissa(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, tr);
        boolean ordinateLeftSomeSpace = drawOrdinate(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, tr);

        boolean enoughSpaceForKeyLabel = abscissaLeftSomeSpace && ordinateLeftSomeSpace;

        /////////////////////CALCULATES POSITON OF THE DISATANCE LABEL///////////////////////////////////

        if(diagonalDrawn)
        {
            boolean distanceLabelLeftSpace = drawDistanceLabel(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, distanceShapeTransformed, j2DStartPoint, j2DEndPoint, j2DCornerPoint);
            enoughSpaceForKeyLabel = enoughSpaceForKeyLabel && distanceLabelLeftSpace; 
        }

        if(enoughSpaceForKeyLabel)
        {

            Shape supportingShape = forcedToDrawAbscissa ? tr.createTransformedShape(getAbsicssaLine()) :
                (forcedToDrawOrdinate ? tr.createTransformedShape(getOrdinateLine()) : distanceShapeTransformed);

            drawKeyLabel(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, supportingShape);
        }   	

        String toolTip = getToolTipText();
        String url = getURL();
        if (toolTip != null || url != null) 
        {
            Shape entityShape = getStroke().createStrokedShape(distanceShapeTransformed);
            addEntity(info, entityShape, rendererIndex, toolTip, url);
        }      
    }


    protected abstract void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Point2D j2DStartPoint, Point2D j2DEndPoint, Point2D j2DCornerPoint, boolean forcedToDrawAbscissa, boolean forcedToDrawOrdinate);



    protected boolean drawDistanceLabel(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, Shape distanceShapeTransformed, Point2D j2DStartPoint, Point2D j2DEndPoint, Point2D j2DCornerPoint)
    {
        boolean labelSpaceLeft = true;

        if(isLabelVisible())
        {         

            double measurementLength = DistanceShapeFactors.getLength(distanceShapeTransformed, 0.f);

            DirectedPosition directedPosition = DistanceShapeFactors.getDirectedPosition(distanceShapeTransformed, 0.5*measurementLength);

            double angle = directedPosition.getAngle();

            FontMetrics fontMetrics = g2.getFontMetrics();
            double offset = getLabelOffset() + fontMetrics.getDescent(); 

            /////////////////////CALCULATES POSITON OF THE DISATANCE LABEL///////////////////////////////////
            String distanceLabel = getDistanceLabel(domainAxis, rangeAxis);     
            Rectangle2D distanceLabelBounds = fontMetrics.getStringBounds(distanceLabel, g2).getBounds2D();

            labelSpaceLeft = (distanceLabelBounds.getWidth()  + 15 < measurementLength);

            Point2D centerPoint = directedPosition.getPoint();

            int j2DCenterX = (int)Math.rint(centerPoint.getX());
            int j2DCenterY = (int)Math.rint(centerPoint.getY());

            double distanceLabelHalfWidth = distanceLabelBounds.getWidth()/2.;          

            int xDistanceLabel = (int) Math.rint(j2DCenterX - Math.cos(angle)*distanceLabelHalfWidth + Math.sin(angle)*offset);
            int yDistanceLabel = (int) Math.rint(j2DCenterY - Math.sin(angle)*distanceLabelHalfWidth - Math.cos(angle)*offset);//fontMetrics.getAscent() + lineBounds.getHeight()/2);

            AffineTransform trOld = g2.getTransform();

            g2.setPaint(getLabelPaint());

            AffineTransform transformNew = new AffineTransform(trOld);      
            transformNew.rotate(angle, xDistanceLabel, yDistanceLabel);         
            g2.setTransform(transformNew);
            g2.drawString(distanceLabel, xDistanceLabel, yDistanceLabel);   

            g2.setTransform(trOld);
        }

        return labelSpaceLeft;
    }

    protected boolean drawAbscissa(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, AffineTransform tr) 
    {
        boolean labelSpaceLeft = true;

        this.drawnAbscissaAngle = Double.NaN;

        if(abscissaDrawn)
        {
            Shape lineAbscissa = tr.createTransformedShape(getAbsicssaLine());

            double abscissaLength = DistanceShapeFactors.getLength(lineAbscissa, 0);

            boolean abscissaVisible = lineAbscissa.intersects(dataArea);

            if (abscissaVisible) 
            {
                g2.setPaint(getStrokePaint());
                g2.draw(lineAbscissa);

                if(isLabelVisible())
                {
                    //DRAWS ABSICSSA LABel
                    String abscissaLabel = getAbscissaLabel(domainAxis);

                    FontMetrics fontMetrics = g2.getFontMetrics();
                    Rectangle2D abscissaLabelBounds = fontMetrics.getStringBounds(abscissaLabel, g2).getBounds2D();

                    DirectedPosition directedPosition = DistanceShapeFactors.getDirectedPosition(lineAbscissa, 0.5*abscissaLength);
                    Point2D centralPoint = directedPosition.getPoint();

                    labelSpaceLeft = ((abscissaLabelBounds.getWidth() + 10) < (abscissaLength));
                    drawnAbscissaAngle = directedPosition.getAngle();

                    int j2DCenterX = (int)Math.rint(centralPoint.getX());
                    int j2DCenterY = (int)Math.rint(centralPoint.getY());

                    double abscissaLabelHalfWidth = abscissaLabelBounds.getWidth()/2.;          
                    double offset = getLabelOffset() + fontMetrics.getDescent(); 

                    int xAbscissaLabel = (int) Math.rint(j2DCenterX - Math.cos(drawnAbscissaAngle)*abscissaLabelHalfWidth + Math.sin(drawnAbscissaAngle)*offset);
                    int yAbscissaLabel = (int) Math.rint(j2DCenterY - Math.sin(drawnAbscissaAngle)*abscissaLabelHalfWidth - Math.cos(drawnAbscissaAngle)*offset);//fontMetrics.getAscent() + lineBounds.getHeight()/2);

                    AffineTransform trOld = g2.getTransform();
                    AffineTransform transformNew = new AffineTransform();  

                    g2.setPaint(getLabelPaint());

                    transformNew.rotate(drawnAbscissaAngle, xAbscissaLabel, yAbscissaLabel);         
                    g2.setTransform(transformNew);
                    g2.drawString(abscissaLabel, xAbscissaLabel, yAbscissaLabel);

                    g2.setTransform(trOld);
                }
            }
        }
        return labelSpaceLeft;
    }

    protected boolean drawOrdinate(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, 
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, 
            PlotRenderingInfo info, AffineTransform tr) 
    {
        boolean labelSpaceLeft = true;

        this.drawnOrdinateAngle = Double.NaN;

        if(ordinateDrawn)
        {
            Shape line = tr.createTransformedShape(getOrdinateLine());

            double length = DistanceShapeFactors.getLength(line, 0);

            boolean lineVisible = line.intersects(dataArea);

            if (lineVisible) 
            {
                g2.setPaint(getStrokePaint());

                g2.draw(line);

                if(isLabelVisible())
                {
                    //DRAWS ORDINATE LABEL
                    String ordinateLabel = getOrdinateLabel(rangeAxis);
                    FontMetrics fontMetrics = g2.getFontMetrics();
                    Rectangle2D ordinateLabelBounds = fontMetrics.getStringBounds(ordinateLabel, g2).getBounds2D();

                    DirectedPosition directedPosition = DistanceShapeFactors.getDirectedPosition(line, 0.5*length);
                    Point2D centralPoint = directedPosition.getPoint();
                    this.drawnOrdinateAngle = directedPosition.getAngle();

                    labelSpaceLeft = ((ordinateLabelBounds.getWidth() + 15) < length);

                    int j2DCenterX = (int)Math.rint(centralPoint.getX());
                    int j2DCenterY = (int)Math.rint(centralPoint.getY());

                    double ordinateLabelHalfWidth = ordinateLabelBounds.getWidth()/2.;          
                    double offset = getLabelOffset() + fontMetrics.getDescent(); 

                    int xOrdinateLabel = (int) Math.rint(j2DCenterX - Math.cos(drawnOrdinateAngle)*ordinateLabelHalfWidth + Math.sin(drawnOrdinateAngle)*offset);
                    int yOrdinateLabel = (int) Math.rint(j2DCenterY - Math.sin(drawnOrdinateAngle)*ordinateLabelHalfWidth - Math.cos(drawnOrdinateAngle)*offset);//fontMetrics.getAscent() + lineBounds.getHeight()/2);

                    AffineTransform trOld = g2.getTransform();
                    AffineTransform transformNew = new AffineTransform();  

                    g2.setPaint(getLabelPaint());

                    transformNew.rotate(drawnOrdinateAngle, xOrdinateLabel, yOrdinateLabel);         
                    g2.setTransform(transformNew);
                    g2.drawString(ordinateLabel, xOrdinateLabel, yOrdinateLabel);

                    g2.setTransform(trOld);
                }
            }
        }
        return labelSpaceLeft;
    }

    public boolean equalsUpToStyle(DistanceMeasurementDrawable that)
    {
        if(that == null)
        {
            return false;
        }

        boolean equalsUpToStyle = ObjectUtilities.equal(getDistanceShape(), that.getDistanceShape());      
        return equalsUpToStyle;
    }

    public static Integer getUnionKey(Collection<DistanceMeasurementDrawable> measurements)
    {
        Integer key = Integer.MAX_VALUE;

        for(DistanceMeasurementDrawable measurement : measurements)
        {
            Integer currentKey = measurement.getKey();
            if(currentKey < key)
            {
                key = currentKey;
            }
        }

        return key;
    }
}

