
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

import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;

public interface RoamingTitle extends PreferencesSource
{
    public boolean isVisible();	
    public void setVisible(boolean legendShowNew);
    public boolean isInside();
    public void setInside(boolean inside);
    public double getInsideX();	
    public double getInsideY();
    public void setInsideX(double x);	
    public void setInsideY(double y);
    public void setInsidePosition(double x, double y);
    public void move(Point2D caughtPoint, Point2D currentPoint, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis);

    public RectangleEdge getOutsidePosition();
    public void setOutsidePosition(RectangleEdge edge);

    public boolean isFrameVisible();
    public void setFrameVisible(boolean visibleNew);

    public Paint getFramePaint();
    public void setFramePaint(Paint paint);
    public Stroke getFrameStroke();
    public void setFrameStroke(Stroke stroke);

    public Paint getBackgroundPaint();
    public void setBackgroundPaint(Paint paintNew);

    public double getBottomPadding();	
    public void setBottomPadding(double bottomPadding);	
    public double getTopPadding();		
    public void setTopPadding(double topPadding);	
    public double getLeftPadding();	
    public void setLeftPadding(double leftPadding);	
    public double getRightPadding();	
    public void setRightPadding(double rightPadding);	
    public double getBottomMargin();	
    public void setBottomMargin(double bottomMargin);	
    public double getTopMargin();		
    public void setTopMargin(double topMargin);	
    public double getLeftMargin();	
    public void setLeftMargin(double leftMargin);	
    public double getRightMargin();	
    public void setRightMargin(double rightMargin);

    public Title getOutsideTitle();
    public XYAnnotation getInsideTitle();

    public void addRoamingTitleChangeListener(RoamingTitleChangeListener listener);
    public void removeRoamingTitleChangeListener(RoamingTitleChangeListener listener);

    public ChartStyleSupplier getSupplier();
    public String getKey();
}