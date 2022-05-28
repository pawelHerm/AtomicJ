
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


import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

import javax.swing.event.EventListenerList;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;


public abstract class RoamingTitleLegend implements RoamingLegend, TitleChangeListener, AnnotationChangeListener
{
    private final Title outsideTitle;
    private final RoamingTitleAnnotation insideTitle;
    private double insideX;
    private double insideY;
    private boolean visible;
    private boolean inside;

    private boolean frameVisible;
    private Stroke frameStroke;
    private Paint framePaint;

    private RectangleInsets insets;

    private final Preferences pref;
    private final String key;

    private final String name;

    private final ChartStyleSupplier supplier;

    private final EventListenerList listenerList = new EventListenerList();

    public RoamingTitleLegend(String name, Title outsideLegend, PreferredRoamingTitleLegendStyle style)
    {
        this.name = name;

        this.outsideTitle = outsideLegend;
        this.insideTitle = new RoamingTitleAnnotation(0.5, 0.5, this);

        this.outsideTitle.addChangeListener(this);
        this.insideTitle.addChangeListener(this);

        this.key = style.getStyleKey();
        this.pref = style.getPreferences();
        this.supplier = style.getSupplier();

        setPreferredStyle(style);
    }

    public RoamingTitleLegend(RoamingTitleLegend oldLegend)
    {
        try {
            this.outsideTitle = (Title) oldLegend.outsideTitle.clone();
        } catch (CloneNotSupportedException e) 
        {
            throw new IllegalArgumentException("'oldLegend' contains unclonable field");
        }
        this.name = oldLegend.name;

        this.insideX = oldLegend.insideX;
        this.insideY = oldLegend.insideY;

        this.insideTitle = new RoamingTitleAnnotation(insideX, insideY, this);

        this.outsideTitle.addChangeListener(this);
        this.insideTitle.addChangeListener(this);

        this.visible = oldLegend.visible;
        this.inside = oldLegend.inside;

        this.frameVisible = oldLegend.frameVisible;
        this.frameStroke = oldLegend.frameStroke;
        this.framePaint = oldLegend.framePaint;

        this.insets = new RectangleInsets(oldLegend.insets.getTop(), oldLegend.insets.getLeft(), oldLegend.insets.getBottom(),  oldLegend.insets.getRight());

        this.pref = oldLegend.pref;
        this.key = oldLegend.key;
        this.supplier = oldLegend.supplier;

        fireRoamingTitleChanged();
    }

    @Override
    public String getName()
    {
        return name;
    }

    protected void setPreferredStyle(PreferredRoamingTitleLegendStyle style)
    {			    
        this.frameVisible = style.frameVisible();
        boolean legendVisible = style.legendVisible();
        boolean legendInside = style.legendInside();
        double legendInsideX = style.legendInsideX();
        double legendInsideY = style.legendInsideY();
        RectangleEdge outsidePosition = style.outsidePosition();

        setInsidePosition(legendInsideX, legendInsideY);
        setOutsidePosition(outsidePosition);
        setInside(legendInside);
        setVisible(legendVisible);

        RectangleInsets marginInsets = style.marginInsets();
        if( marginInsets != null)
        {
            outsideTitle.setMargin(marginInsets);
        }		

        this.framePaint = style.framePaint();
        this.frameStroke = style.frameStroke();

        Title outsideTitle = getOutsideTitle();
        insets = outsideTitle.getFrame().getInsets();

        BlockFrame newFrame = frameVisible ? new LineBorder(framePaint, frameStroke, insets)
                :BlockBorder.NONE;

        outsideTitle.setFrame(newFrame);
        fireRoamingTitleChanged();	
    }


    @Override
    public void addRoamingTitleChangeListener(RoamingTitleChangeListener listener) 
    {
        this.listenerList.add(RoamingTitleChangeListener.class, listener);
    }

    @Override
    public void removeRoamingTitleChangeListener(RoamingTitleChangeListener listener) 
    {
        this.listenerList.remove(RoamingTitleChangeListener.class, listener);
    }

    protected void fireRoamingTitleChanged() 
    { 
        notifyListeners(new RoamingTitleChangeEvent(this));   
    }

    protected void notifyListeners(RoamingTitleChangeEvent event) 
    {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == RoamingTitleChangeListener.class) {
                ((RoamingTitleChangeListener) listeners[i + 1]).roamingTitleChanged(
                        event);
            }
        }

    }

    @Override
    public Title getOutsideTitle()
    {
        return outsideTitle;
    }

    @Override
    public XYAnnotation getInsideTitle()
    {
        return insideTitle;
    }

    @Override
    public boolean isVisible()
    {
        return visible;
    }

    @Override
    public void setVisible(boolean legendVisibleNew)
    {
        if(visible != legendVisibleNew)
        {
            this.visible = legendVisibleNew;

            fireRoamingTitleChanged();
        }		
    }

    @Override
    public boolean isInside()
    {
        return inside;
    }

    @Override
    public void setInside(boolean inside)
    {
        this.inside = inside;
        fireRoamingTitleChanged();
    }	

    @Override
    public double getInsideX()
    {
        return insideX;
    }

    @Override
    public double getInsideY()
    {
        return insideY;
    }

    @Override
    public void setInsideX(double x)
    {
        this.insideX = x;
        this.insideTitle.setX(insideX);	 
    }

    @Override
    public void setInsideY(double y)
    {
        this.insideY = y;		
        this.insideTitle.setY(insideY);
    }

    @Override
    public void setInsidePosition(double x, double y)
    {
        this.insideX = x;
        this.insideY = y;		
        this.insideTitle.setPosition(x, y);
    }

    @Override
    public void move(Point2D caughtPoint, Point2D currentPoint, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis)
    {
        double dX = currentPoint.getX() - caughtPoint.getX();
        double dY = currentPoint.getY() - caughtPoint.getY();

        Range xRange = domainAxis.getRange();
        Range yRange = rangeAxis.getRange();

        double dx = dX/xRange.getLength();
        double dy = dY/yRange.getLength();

        double x = this.insideX + dx;
        double y = this.insideY + dy;

        setInsidePosition(x, y);
    }


    @Override
    public RectangleEdge getOutsidePosition()
    {
        RectangleEdge position = outsideTitle.getPosition();
        return position;
    }

    @Override
    public void setOutsidePosition(RectangleEdge position)
    {
        outsideTitle.setPosition(position);
    }

    @Override
    public boolean containsText()
    {
        if(outsideTitle instanceof LegendTitle)
        {
            return true;
        }
        else 
        {
            return false;
        }
    }

    @Override
    public Paint getLegendItemPaint()
    {
        if(outsideTitle instanceof LegendTitle)
        {
            return ((LegendTitle) outsideTitle).getItemPaint();
        }
        return null;	
    }

    @Override
    public void setLegendItemPaint(Paint paintNew)
    {
        if(outsideTitle instanceof LegendTitle)
        {
            ((LegendTitle) outsideTitle).setItemPaint(paintNew);
        }
    }

    @Override
    public Font getLegendItemFont()
    {
        if(outsideTitle instanceof LegendTitle)
        {
            return ((LegendTitle) outsideTitle).getItemFont();
        }
        return null;
    }

    @Override
    public void setLegendItemFont(Font fontNew)
    {
        if(outsideTitle instanceof LegendTitle)
        {
            ((LegendTitle) outsideTitle).setItemFont(fontNew);
        }		
    }

    @Override
    public double getBottomPadding()
    {
        RectangleInsets padding = outsideTitle.getPadding();
        return padding.getBottom();
    }

    @Override
    public void setBottomPadding(double bottomPadding)
    {
        RectangleInsets paddingOld = outsideTitle.getPadding();
        RectangleInsets padingNew = new RectangleInsets(paddingOld.getTop(), paddingOld.getLeft(), bottomPadding, paddingOld.getRight());
        outsideTitle.setPadding(padingNew);

        fireRoamingTitleChanged();	
    }

    @Override
    public double getTopPadding()
    {
        RectangleInsets padding = outsideTitle.getPadding();
        return padding.getTop();
    }	

    @Override
    public void setTopPadding(double topPadding)
    {
        RectangleInsets paddingOld = outsideTitle.getPadding();
        RectangleInsets padingNew = new RectangleInsets(topPadding, paddingOld.getLeft(), paddingOld.getBottom(), paddingOld.getRight());
        outsideTitle.setPadding(padingNew);

        fireRoamingTitleChanged();	
    }

    @Override
    public double getLeftPadding()
    {
        RectangleInsets padding = outsideTitle.getPadding();
        return padding.getLeft();
    }

    @Override
    public void setLeftPadding(double leftPadding)
    {
        RectangleInsets paddingOld = outsideTitle.getPadding();
        RectangleInsets padingNew = new RectangleInsets(paddingOld.getTop(), leftPadding, paddingOld.getBottom(), paddingOld.getRight());
        outsideTitle.setPadding(padingNew);
        fireRoamingTitleChanged();	

    }

    @Override
    public double getRightPadding()
    {
        RectangleInsets padding = outsideTitle.getPadding();
        return padding.getRight();
    }

    @Override
    public void setRightPadding(double rightPadding)
    {
        RectangleInsets paddingOld = outsideTitle.getPadding();
        RectangleInsets padingNew = new RectangleInsets(paddingOld.getTop(), paddingOld.getLeft(), paddingOld.getBottom(), rightPadding);
        outsideTitle.setPadding(padingNew);
        fireRoamingTitleChanged();	

    }	

    @Override
    public double getBottomMargin()
    {
        RectangleInsets margin = outsideTitle.getMargin();
        return margin.getBottom();
    }

    @Override
    public void setBottomMargin(double bottomMargin)
    {	
        RectangleInsets marignOld = outsideTitle.getMargin();
        RectangleInsets marginNew = new RectangleInsets(marignOld.getTop(), marignOld.getLeft(), bottomMargin, marignOld.getRight());
        outsideTitle.setMargin(marginNew);
        fireRoamingTitleChanged();	

    }

    @Override
    public double getTopMargin()
    {
        RectangleInsets margin = outsideTitle.getMargin();
        return margin.getTop();
    }	

    @Override
    public void setTopMargin(double topMargin)
    {
        RectangleInsets marginOld = outsideTitle.getMargin();
        RectangleInsets marginNew = new RectangleInsets(topMargin, marginOld.getLeft(), marginOld.getBottom(), marginOld.getRight());
        outsideTitle.setMargin(marginNew);
        fireRoamingTitleChanged();	

    }

    @Override
    public double getLeftMargin()
    {
        RectangleInsets margin = outsideTitle.getMargin();
        return margin.getLeft();
    }

    @Override
    public void setLeftMargin(double leftMargin)
    {
        RectangleInsets marginOld = outsideTitle.getMargin();
        RectangleInsets marginNew = new RectangleInsets(marginOld.getTop(), leftMargin, marginOld.getBottom(), marginOld.getRight());
        outsideTitle.setMargin(marginNew);
        fireRoamingTitleChanged();	

    }

    @Override
    public double getRightMargin()
    {
        RectangleInsets margin = outsideTitle.getMargin();
        return margin.getRight();
    }

    @Override
    public void setRightMargin(double rightMargin)
    {
        RectangleInsets marginOld = outsideTitle.getMargin();
        RectangleInsets marginNew = new RectangleInsets(marginOld.getTop(), marginOld.getLeft(), marginOld.getBottom(), rightMargin);
        outsideTitle.setMargin(marginNew);
        fireRoamingTitleChanged();	

    }

    @Override
    public boolean isFrameVisible()
    {
        return frameVisible;
    }

    @Override
    public void setFrameVisible(boolean frameVisible)
    {
        this.frameVisible = frameVisible;

        BlockFrame newFrame;
        if(frameVisible)
        {
            newFrame = new LineBorder(framePaint, frameStroke, insets);
        }
        else
        {
            newFrame = BlockBorder.NONE;
        }

        outsideTitle.setFrame(newFrame);
        fireRoamingTitleChanged();	

    }

    @Override
    public Paint getFramePaint()
    {
        return framePaint;
    }

    @Override
    public void setFramePaint(Paint paint)
    {
        this.framePaint = paint;
        Title outsideLegend = getOutsideTitle();

        RectangleInsets insets = outsideLegend.getFrame().getInsets();
        LineBorder newBorder = new LineBorder(framePaint, frameStroke, insets);
        outsideLegend.setFrame(newBorder);
        fireRoamingTitleChanged();	
    }

    @Override
    public Stroke getFrameStroke()
    {
        return frameStroke;
    }

    @Override
    public void setFrameStroke(Stroke stroke)
    {
        this.frameStroke = stroke;

        Title outsideLegend = getOutsideTitle();
        RectangleInsets insets = outsideLegend.getFrame().getInsets();
        LineBorder newBorder = new LineBorder(framePaint, frameStroke, insets);
        outsideLegend.setFrame(newBorder);

        fireRoamingTitleChanged();	
    }

    @Override
    public abstract void setBackgroundPaint(Paint paintNew);

    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }

    @Override
    public ChartStyleSupplier getSupplier()
    {
        return supplier;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public void titleChanged(TitleChangeEvent event)
    {
        fireRoamingTitleChanged();
    }

    @Override
    public void annotationChanged(AnnotationChangeEvent event)
    {
        fireRoamingTitleChanged();
    }
}