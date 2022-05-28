
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

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jfree.util.PublicCloneable;

import atomicJ.gui.annotations.AbstractCustomizableAnnotation;
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorSourceSigned;
import atomicJ.gui.annotations.AnnotationStyle;
import atomicJ.gui.annotations.BasicAnnotationAnchor;

public abstract class LineCustomizableAnnotation extends AbstractCustomizableAnnotation implements Cloneable, PublicCloneable
{
    private static final long serialVersionUID = 1L;

    private double x1;
    private double y1;
    private double x2;
    private double y2;

    private Shape startHotSpot = new Area();
    private Shape endHotSpot = new Area();

    public LineCustomizableAnnotation(Point2D anchor, Point2D end, Integer key, AnnotationStyle style) 
    {	
        super(key, Integer.toString(key), style);

        this.x1 = anchor.getX();
        this.y1 = anchor.getY();
        this.x2 = end.getX();
        this.y2 = end.getY();  
    }

    public LineCustomizableAnnotation(LineCustomizableAnnotation that)
    {
        this(that, that.getStyle());
    }

    public LineCustomizableAnnotation(LineCustomizableAnnotation that, AnnotationStyle style)
    {
        super(that, style);

        this.x1 = that.x1;
        this.y1 = that.y1;
        this.x2 = that.x2;
        this.y2 = that.y2;	    
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorCore anchor = null;

        if(isFinished())
        {
            if(startHotSpot.contains(java2DPoint))
            {
                anchor= BasicAnnotationAnchor.START;
            }
            else if(endHotSpot.contains(java2DPoint))
            {
                anchor = BasicAnnotationAnchor.END;
            }
            else if(isClicked(dataRectangle))
            {
                anchor  = BasicAnnotationAnchor.CENTER;
            }
        }

        AnnotationAnchorSigned caughtAnchor = (anchor != null) ? new AnnotationAnchorSourceSigned(anchor, getKey()) : null;
        return caughtAnchor;      
    }

    public boolean setPosition(AnnotationAnchorCore caughtProfileAnchor, Point2D startPoint, Point2D endPoint)
    {
        if(caughtProfileAnchor == null)
        {
            return false;
        }   

        if(BasicAnnotationAnchor.CENTER.equals(caughtProfileAnchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            this.x1 = x1 + tx;
            this.x2 = x2 + tx;
            this.y1 = y1 + ty;
            this.y2 = y2 + ty;

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.START.equals(caughtProfileAnchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            this.x1 = x1 + tx;
            this.y1 = y1 + ty;

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.END.equals(caughtProfileAnchor))
        {
            double tx = endPoint.getX() - startPoint.getX();
            double ty = endPoint.getY() - startPoint.getY();

            this.x2 = x2 + tx;
            this.y2 = y2 + ty;

            fireAnnotationChanged();
        }
        return false;
    }

    public double getLength()
    {
        double dx = x2 - x1;
        double dy = y2 - y1;

        double length = Math.sqrt(dx*dx + dy*dy);

        return length;
    }

    protected Shape getStartHotSpot()
    {
        return startHotSpot;
    }

    protected void setStartHotSpot(Shape startHotSpotNew)
    {
        this.startHotSpot = startHotSpotNew;
    }

    protected Shape getEndHotSpot()
    {
        return endHotSpot;
    }

    protected void setEndHotSpot(Shape endHotSpotNew)
    {
        this.endHotSpot = endHotSpotNew;
    }

    public double getStartX()
    {
        return x1;
    }

    protected void setStartX(double x1)
    {
        this.x1 = x1;
    }

    public double getStartY()
    {
        return y1;
    }

    public Point2D getStartPoint()
    {
        return new Point2D.Double(x1, y1);
    }

    protected void setStartY(double y1)
    {
        this.y1 = y1;
    }

    public double getEndX()
    {
        return x2;
    }

    public void setEndX(double x2)
    {
        this.x2 = x2;
    }

    public double getEndY()
    {
        return y2;
    }

    public void setEndY(double y2)
    {
        this.y2 = y2;
    }

    public Point2D getEndPoint()
    {
        return new Point2D.Double(x2, y2);
    }


    public Line2D.Double getLine()
    {
        Line2D.Double line = new Line2D.Double(x1,y1,x2,y2);
        return line;
    }

    public boolean isClicked(Rectangle2D hotDataArea)
    {
        boolean clicked = false;

        if(isVisible())
        {
            clicked = hotDataArea.intersectsLine(getLine());   		
        }
        return clicked;
    }

    public boolean equalsUpToStyle(LineCustomizableAnnotation that)
    {       
        if(that == null)
        {
            return false;
        }
        if(this.x1 != that.x1)
        {
            return false;
        }
        if(this.x2 != that.x2)
        {
            return false;
        }
        if(this.y1 != that.y1)
        {
            return false;
        }
        if(this.y2 != that.y2)
        {
            return false;
        }

        return true;
    }
}

