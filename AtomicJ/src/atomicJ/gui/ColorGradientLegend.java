
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

//THIS CLASS IS A HEAVY MODIFICATION OF A CLASS PAINTSCALELEGEND BY DAVID GILBERT, WHICH IS A PART OF JFREECHART LIBRARY
//BELOW THE ORIGINAL COPYRIGHT NOTICE
/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ---------------------
 * PaintScaleLegend.java
 * ---------------------
 * (C) Copyright 2007, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * 
 * Pawel Hermanowicz introduced changes to draw() method, so that now PaintScaleLegend is added to EntityCollection and is drawn with SkewedGradientPaint
 * -------
 * 22-Jan-2007 : Version 1 (DG);
 * 
 */

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.block.BlockResult;
import org.jfree.chart.block.EntityBlockParams;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.Size2D;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

import atomicJ.data.units.PrefixedUnit;


public class ColorGradientLegend extends Title implements AxisChangeListener, PublicCloneable {

    static final long serialVersionUID = 1L;

    private GradientPaintScale scale;   
    private CustomizableNumberAxis axis;  
    private AxisLocation axisLocation;
    private double axisOffset;    
    private double stripWidth;   
    private boolean stripOutlineVisible;    
    private transient Paint stripOutlinePaint;    
    private transient Stroke stripOutlineStroke;    
    private transient Paint backgroundPaint;

    private Rectangle2D hotspotStripArea;
    private Shape hotspotAxisArea;

    public ColorGradientLegend(GradientPaintScale gradientScale, CustomizableNumberAxis axis) 
    {
        if (axis == null) 
        {
            throw new IllegalArgumentException("Null 'axis' argument.");
        }

        this.scale = gradientScale;

        this.axis = axis;
        this.axis.addChangeListener(this);

        this.axisLocation = AxisLocation.BOTTOM_OR_LEFT;
        this.axisOffset = 0.0;
        this.axis.setRange(gradientScale.getLowerBound(), gradientScale.getUpperBound());
        this.axis.setPreferredAxisUnit();
        this.stripWidth = 15.0;
        this.stripOutlineVisible = true;
        this.stripOutlinePaint = Color.gray;
        this.stripOutlineStroke = new BasicStroke(0.5f);
        this.backgroundPaint = Color.white;

        RectangleInsets insets = new RectangleInsets(10, 10, 10, 10);
        LineBorder newBorder = new LineBorder(Color.black, new BasicStroke(0.5f), insets);
        setFrame(newBorder);
    }

    @Override
    public Object clone()
    {
        try {            
            ColorGradientLegend clone = (ColorGradientLegend)super.clone();    

            clone.axis = (CustomizableNumberAxis) this.axis.clone();
            clone.axis.addChangeListener(clone);

            return clone;

        } catch (CloneNotSupportedException e) 
        {
            e.printStackTrace();
        }

        return null;
    }

    public double getClickedPositionRatio(Point2D java2DPoint)
    {
        Rectangle2D legendBounds = getStripHotspotArea();

        RectangleEdge legendPosition = getPosition();

        boolean isLegendVertical = RectangleEdge.isLeftOrRight(legendPosition);

        double clickedPosition = isLegendVertical ? java2DPoint.getY() : java2DPoint.getX();

        double legendLength = isLegendVertical ? legendBounds.getHeight() : legendBounds.getWidth();
        double legendOrgin = isLegendVertical ? (legendBounds.getY() + legendBounds.getHeight()) : legendBounds.getX();
        double ratio = Math.abs(clickedPosition - legendOrgin)/legendLength;

        return ratio;
    }

    public void requestScaleChange(Point2D java2DPoint, double percent)
    {}

    public PrefixedUnit getDataUnit()
    {
        return axis.getDataQuantity().getUnit();
    }

    public GradientPaintScale getScale() {
        return this.scale;    
    }

    public void setScale(GradientPaintScale scale) {
        if (scale == null) {
            throw new IllegalArgumentException("Null 'scale' argument.");
        }

        this.scale = scale;

        //updates the axis range
        double lowerGradientBound = scale.getLowerBound();
        double upperGradientBound = scale.getUpperBound();
        axis.setRange(lowerGradientBound, upperGradientBound);

        notifyListeners(new TitleChangeEvent(this));
    }

    public CustomizableNumberAxis getAxis() {
        return this.axis;
    }


    public void setAxis(CustomizableNumberAxis axisNew)
    {
        if (axisNew == null) {
            throw new IllegalArgumentException("Null 'axisNew' argument.");
        }
        this.axis.removeChangeListener(this);
        this.axis = axisNew;

        this.axis.addChangeListener(this);

        notifyListeners(new TitleChangeEvent(this));
    }

    public AxisLocation getAxisLocation() {
        return this.axisLocation;
    }

    public void setAxisLocation(AxisLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("Null 'location' argument.");
        }
        this.axisLocation = location;
        notifyListeners(new TitleChangeEvent(this));
    }

    public double getAxisOffset() {
        return this.axisOffset;
    }

    public void setAxisOffset(double offset) {
        this.axisOffset = offset;
        notifyListeners(new TitleChangeEvent(this));
    }

    @Override
    public void axisChanged(AxisChangeEvent event) 
    {                
        if (this.axis == event.getAxis()) 
        {
            notifyListeners(new TitleChangeEvent(this));
        }
    }   

    public double getStripWidth() {
        return this.stripWidth;
    }

    public void setStripWidth(double width) {
        this.stripWidth = width;
        notifyListeners(new TitleChangeEvent(this));
    }

    public boolean isStripOutlineVisible() {
        return this.stripOutlineVisible;
    }

    public void setStripOutlineVisible(boolean visible) {
        this.stripOutlineVisible = visible;
        notifyListeners(new TitleChangeEvent(this));
    }


    public Paint getStripOutlinePaint() {
        return this.stripOutlinePaint;
    }

    public void setStripOutlinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.stripOutlinePaint = paint;
        notifyListeners(new TitleChangeEvent(this));
    }

    public Stroke getStripOutlineStroke() {
        return this.stripOutlineStroke;
    }

    public void setStripOutlineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.stripOutlineStroke = stroke;
        notifyListeners(new TitleChangeEvent(this));
    }

    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    public void setBackgroundPaint(Paint paint) {
        this.backgroundPaint = paint;
        notifyListeners(new TitleChangeEvent(this));
    }

    @Override
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        RectangleConstraint cc = toContentConstraint(constraint);
        LengthConstraintType w = cc.getWidthConstraintType();
        LengthConstraintType h = cc.getHeightConstraintType();
        Size2D contentSize = null;
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = new Size2D(getWidth(), getHeight()); 
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented."); 
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }            
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented."); 
            }
            else if (h == LengthConstraintType.RANGE) {
                contentSize = arrangeRR(g2, cc.getWidthRange(), 
                        cc.getHeightRange()); 
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented."); 
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented."); 
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        return new Size2D(calculateTotalWidth(contentSize.getWidth()),
                calculateTotalHeight(contentSize.getHeight()));
    }

    protected Size2D arrangeRR(Graphics2D g2, Range widthRange, 
            Range heightRange) {

        RectangleEdge position = getPosition();
        if (position == RectangleEdge.TOP || position == RectangleEdge.BOTTOM) {


            float maxWidth = (float) widthRange.getUpperBound();

            // determine the space required for the axis
            AxisSpace space = this.axis.reserveSpace(g2, null, 
                    new Rectangle2D.Double(0, 0, maxWidth, 100), 
                    RectangleEdge.BOTTOM, null);

            return new Size2D(maxWidth, this.stripWidth + this.axisOffset 
                    + space.getTop() + space.getBottom());
        }
        else if (position == RectangleEdge.LEFT || position 
                == RectangleEdge.RIGHT) {
            float maxHeight = (float) heightRange.getUpperBound();
            AxisSpace space = this.axis.reserveSpace(g2, null, 
                    new Rectangle2D.Double(0, 0, 100, maxHeight), 
                    RectangleEdge.RIGHT, null);
            return new Size2D(this.stripWidth + this.axisOffset 
                    + space.getLeft() + space.getRight(), maxHeight);
        }
        else {
            throw new RuntimeException("Unrecognised position.");
        }
    }

    public Rectangle2D getStripHotspotArea()
    {
        return hotspotStripArea;
    }

    public boolean isStripClicked(Point2D java2DPoint)
    {
        boolean clicked = false;

        if(hotspotStripArea != null)
        {
            clicked = hotspotStripArea.contains(java2DPoint);
        }
        return clicked;
    }

    public boolean isAxisClicked(Point2D java2DPoint)
    {
        boolean clicked = false;

        if(hotspotAxisArea != null)
        {
            clicked = hotspotAxisArea.contains(java2DPoint);
        }
        return clicked;
    }

    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        draw(g2, area, null);
    }

    @Override
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) 
    {
        Rectangle2D target = (Rectangle2D) area.clone();
        target = trimMargin(target);        

        BlockFrame frame = getFrame();
        frame.draw(g2, target);

        frame.getInsets().trim(target);
        target = trimPadding(target);

        SkewedGradientPaint gradientPaint;
        Rectangle2D legendStrip = null;
        RectangleEdge axisEdge = null;
        double cursor = 0;
        ColorGradient scale = this.scale.getGradient();

        double angle = axis.isInverted() ? Math.PI : 0;

        if (RectangleEdge.isTopOrBottom(getPosition())) 
        {
            axisEdge = Plot.resolveRangeAxisLocation(this.axisLocation, PlotOrientation.HORIZONTAL);

            RectangleCorner gradientOrigin = axis.isInverted() ? RectangleCorner.BOTTOM_RIGHT: RectangleCorner.UPPER_LEFT;
            gradientPaint = new SkewedGradientPaint(scale, gradientOrigin, 0);

            if (axisEdge == RectangleEdge.TOP) 
            {
                legendStrip = new Rectangle2D.Double(target.getMinX(), target.getMaxY() - this.stripWidth, target.getWidth(), this.stripWidth);         	
                cursor = target.getMaxY() - this.stripWidth - this.axisOffset;
            }
            else if (axisEdge == RectangleEdge.BOTTOM) 
            {
                legendStrip = new Rectangle2D.Double(target.getMinX(), target.getMinY(), target.getWidth(), this.stripWidth);             
                cursor = target.getMinY() + this.stripWidth + this.axisOffset;                
            }
        }
        else 
        {
            axisEdge = Plot.resolveRangeAxisLocation(this.axisLocation, PlotOrientation.VERTICAL);
            RectangleCorner gradientOrigin = axis.isInverted() ? RectangleCorner.UPPER_LEFT: RectangleCorner.BOTTOM_RIGHT;
            gradientPaint = new SkewedGradientPaint(scale, gradientOrigin, Math.PI/2);

            if (axisEdge == RectangleEdge.LEFT) 
            {
                legendStrip = new Rectangle2D.Double(target.getMaxX()  - this.stripWidth, target.getMinY(), this.stripWidth, target.getHeight());         
                cursor = target.getMaxX() - this.stripWidth  - this.axisOffset;
            }
            else if (axisEdge == RectangleEdge.RIGHT)
            {
                legendStrip = new Rectangle2D.Double(target.getMinX(),  target.getMinY(), this.stripWidth, target.getHeight());                
                cursor = target.getMinX() + this.stripWidth + this.axisOffset;
            }
        }


        if (this.backgroundPaint != null) 
        {
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setPaint(this.backgroundPaint);
            g2.fill(target);

            g2.setComposite(originalComposite);

        }

        g2.setPaint(Color.white);
        g2.fill(legendStrip);
        g2.setPaint(gradientPaint);
        g2.fill(legendStrip);
        if (isStripOutlineVisible()) 
        {
            g2.setPaint(this.stripOutlinePaint);
            g2.setStroke(this.stripOutlineStroke);
        }
        g2.draw(legendStrip);

        StandardEntityCollection entityCollection = new StandardEntityCollection();

        this.axis.draw(g2, cursor, target, target, axisEdge, null);                

        Area hotspotAxis = new Area(target);
        hotspotAxis.subtract(new Area(legendStrip));
        this.hotspotAxisArea = hotspotAxis;
        this.hotspotStripArea = legendStrip;

        BlockResult blockResult = new BlockResult();

        if (params instanceof EntityBlockParams) 
        {
            EntityBlockParams p = (EntityBlockParams) params;
            if (p.getGenerateEntities()) 
            {
                LegendEntity legendEntity = new LegendEntity(legendStrip, this, 

                        "<html><b>TIPS!</b><ul><li>Use mouse scrollwheel<li>Click to choose gradient</ul></html>");


                AxisEntity axisEntity = new AxisEntity(hotspotAxisArea, axis);

                entityCollection.add(legendEntity);
                entityCollection.add(axisEntity);

                blockResult.setEntityCollection(entityCollection);
            }			
        }

        return blockResult;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ColorGradientLegend)) {
            return false;
        }
        ColorGradientLegend that = (ColorGradientLegend) obj;
        if (!this.scale.equals(that.scale)) {
            return false;
        }
        if (!this.axis.equals(that.axis)) {
            return false;
        }
        if (!this.axisLocation.equals(that.axisLocation)) {
            return false;
        }
        if (this.axisOffset != that.axisOffset) {
            return false;
        }
        if (this.stripWidth != that.stripWidth) {
            return false;
        }
        if (this.stripOutlineVisible != that.stripOutlineVisible) {
            return false;
        }
        if (!PaintUtilities.equal(this.stripOutlinePaint, 
                that.stripOutlinePaint)) {
            return false;
        }
        if (!this.stripOutlineStroke.equals(that.stripOutlineStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
        }
        return super.equals(obj);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.backgroundPaint, stream);
        SerialUtilities.writePaint(this.stripOutlinePaint, stream);
        SerialUtilities.writeStroke(this.stripOutlineStroke, stream);
    }

    private void readObject(ObjectInputStream stream) 
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.backgroundPaint = SerialUtilities.readPaint(stream);
        this.stripOutlinePaint = SerialUtilities.readPaint(stream);
        this.stripOutlineStroke = SerialUtilities.readStroke(stream);
    }

}
