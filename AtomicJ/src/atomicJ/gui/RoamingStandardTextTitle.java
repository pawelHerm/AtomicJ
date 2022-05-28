
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

import static atomicJ.gui.PreferenceKeys.TITLE_AUTOMATIC_TEXT_TYPE;
import static atomicJ.gui.PreferenceKeys.TITLE_FONT;
import static atomicJ.gui.PreferenceKeys.TITLE_FRAME_PAINT;
import static atomicJ.gui.PreferenceKeys.TITLE_FRAME_STROKE;
import static atomicJ.gui.PreferenceKeys.TITLE_FRAME_VISIBLE;
import static atomicJ.gui.PreferenceKeys.TITLE_INSIDE;
import static atomicJ.gui.PreferenceKeys.TITLE_INSIDE_X;
import static atomicJ.gui.PreferenceKeys.TITLE_INSIDE_Y;
import static atomicJ.gui.PreferenceKeys.TITLE_MARGIN_BOTTOM;
import static atomicJ.gui.PreferenceKeys.TITLE_MARGIN_LEFT;
import static atomicJ.gui.PreferenceKeys.TITLE_MARGIN_RIGHT;
import static atomicJ.gui.PreferenceKeys.TITLE_MARGIN_TOP;
import static atomicJ.gui.PreferenceKeys.TITLE_OUTSIDE_POSITION;
import static atomicJ.gui.PreferenceKeys.TITLE_PADDING_BOTTOM;
import static atomicJ.gui.PreferenceKeys.TITLE_PADDING_LEFT;
import static atomicJ.gui.PreferenceKeys.TITLE_PADDING_RIGHT;
import static atomicJ.gui.PreferenceKeys.TITLE_PADDING_TOP;
import static atomicJ.gui.PreferenceKeys.TITLE_PAINT;
import static atomicJ.gui.PreferenceKeys.TITLE_TEXT_AUTOMATIC;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import atomicJ.utilities.SerializationUtilities;


public class RoamingStandardTextTitle implements RoamingTextTitle, TitleChangeListener, AnnotationChangeListener
{
    private static final ChartStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultChartStyleSupplier.getSupplier();

    private final TextTitle outsideTitle;
    private final RoamingTitleAnnotation insideTitle;

    private double insideX;
    private double insideY;
    private boolean visible;
    private boolean inside;

    private boolean frameVisible;
    private Stroke frameStroke;
    private Paint framePaint;

    private boolean useAutomaticTitle;
    private String automaticTitleType;

    private Map<String, String> automaticTitlesNew = Collections.emptyMap();

    private final EventListenerList listenerList = new EventListenerList();

    private final Preferences pref;
    private final String key;

    public RoamingStandardTextTitle(TextTitle outsideTitle, String key, Preferences pref)
    {
        this.outsideTitle = outsideTitle;
        this.insideTitle = new RoamingTitleAnnotation(0.5, 0.5, this);

        this.outsideTitle.addChangeListener(this);
        this.insideTitle.addChangeListener(this);

        this.key = key;
        this.pref = pref;
        setPreferredStyle(key, pref, DEFAULT_STYLE_SUPPLIER);
    }

    public RoamingStandardTextTitle(RoamingStandardTextTitle oldTitle)
    {
        try {
            this.outsideTitle = (TextTitle) oldTitle.outsideTitle.clone();
        } catch (CloneNotSupportedException e) 
        {
            throw new IllegalArgumentException("'oldLegend' contains unclonable field");
        }
        this.insideX = oldTitle.insideX;
        this.insideY = oldTitle.insideY;
        this.insideTitle = new RoamingTitleAnnotation(insideX, insideY, this);

        this.outsideTitle.addChangeListener(this);
        this.insideTitle.addChangeListener(this);

        this.visible = oldTitle.visible;
        this.inside = oldTitle.inside;

        this.frameVisible = oldTitle.frameVisible;
        this.frameStroke = oldTitle.frameStroke;
        this.framePaint = oldTitle.framePaint;

        this.useAutomaticTitle = oldTitle.useAutomaticTitle;
        this.automaticTitleType = oldTitle.automaticTitleType;

        this.pref = oldTitle.pref;
        this.key = oldTitle.key;

        fireRoamingTitleChanged();
    }


    @Override
    public RoamingTextTitle copy()
    {
        return new RoamingStandardTextTitle(this);
    }

    private void setPreferredStyle(String key, Preferences pref, ChartStyleSupplier supplier)
    {		
        setVisible(true);

        //SETS DEFAULT TEXT STYLE
        Font textFont = (Font)SerializationUtilities.getSerializableObject(pref, TITLE_FONT, new Font("Dialog", Font.BOLD, 16));
        Paint textPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TITLE_PAINT, Color.black);

        this.useAutomaticTitle = pref.getBoolean(TITLE_TEXT_AUTOMATIC, useAutomaticTitle);
        this.automaticTitleType = pref.get(TITLE_AUTOMATIC_TEXT_TYPE, automaticTitleType);

        setFont(textFont);
        setPaint(textPaint);

        ensureConsistencyWithAutomaticText();

        //SETS DEFAULT FRAME STYLE

        boolean defaultFrameVisible = supplier.getDefaultTitleFrameVisible(key);
        frameVisible = pref.getBoolean(TITLE_FRAME_VISIBLE, defaultFrameVisible);

        framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, TITLE_FRAME_PAINT, Color.black);
        frameStroke = SerializationUtilities.getStroke(pref, TITLE_FRAME_STROKE, new BasicStroke(1.f));

        BlockFrame newFrame = frameVisible ? new LineBorder(framePaint, frameStroke, new RectangleInsets(4, 4, 4, 4)) : BlockBorder.NONE;
        outsideTitle.setFrame(newFrame);


        //SET DEFAULT POSITION
        boolean defaultLegendInside = supplier.getDefaultTitleInside(key);
        double defaultInsideX = supplier.getDefaultTitleInsideX(key);
        double defaultInsideY = supplier.getDefaultTitleInsideY(key);

        boolean inside = pref.getBoolean(TITLE_INSIDE, defaultLegendInside);
        double insideX = pref.getDouble(TITLE_INSIDE_X, defaultInsideX);
        double insideY = pref.getDouble(TITLE_INSIDE_Y, defaultInsideY);
        setInsidePosition(insideX, insideY);

        RectangleEdge outsidePosition = (RectangleEdge)SerializationUtilities.getSerializableObject(pref, TITLE_OUTSIDE_POSITION, RectangleEdge.TOP);
        setOutsidePosition(outsidePosition);

        setInside(inside);

        //SET DEFAULT MARGINS
        double marginTop = pref.getDouble(TITLE_MARGIN_TOP, 5);
        double marginBottom = pref.getDouble(TITLE_MARGIN_BOTTOM, 5);
        double marginLeft = pref.getDouble(TITLE_MARGIN_LEFT, 5);
        double marginRight = pref.getDouble(TITLE_MARGIN_RIGHT, 5);


        if(marginTop + marginBottom + marginLeft + marginRight > 0.001)
        {
            RectangleInsets marginInsets = new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight);
            outsideTitle.setMargin(marginInsets);
        }		

        //SETS DEFAULT PADDING
        double paddingTop = pref.getDouble(TITLE_PADDING_TOP, 4);
        double paddingBottom = pref.getDouble(TITLE_PADDING_BOTTOM, 4);
        double paddingLeft = pref.getDouble(TITLE_PADDING_LEFT, 4);
        double paddingRight = pref.getDouble(TITLE_PADDING_RIGHT, 4);

        RectangleInsets padingNew = new RectangleInsets(paddingTop, paddingLeft, paddingBottom, paddingRight);
        outsideTitle.setPadding(padingNew);

        fireRoamingTitleChanged();	
    }

    public Map<String, String> getAutomaticTitles()
    {
        Map<String, String> automaticTitles = new LinkedHashMap<>(automaticTitlesNew);

        return automaticTitles;
    }

    private String getAutomaticText(String type)
    {
        String text = automaticTitlesNew.get(type);
        return text;
    }

    @Override
    public void setUseAutomaticTitle(boolean useAutomaticTitleNew)
    {
        this.useAutomaticTitle = useAutomaticTitleNew;
        ensureConsistencyWithAutomaticText();
    }

    @Override
    public void setAutomaticTitleType(String automaticTitleTypeNew)
    {
        this.automaticTitleType = automaticTitleTypeNew;
        ensureConsistencyWithAutomaticText();
    }

    @Override
    public void setAutomaticTitles(Map<String, String> automaticTitlesNew)
    {
        this.automaticTitlesNew = automaticTitlesNew;
        ensureConsistencyWithAutomaticText();
    }

    private void ensureConsistencyWithAutomaticText()
    {
        if(useAutomaticTitle)
        {
            String automaticText = getAutomaticText(automaticTitleType);
            if(automaticText != null)
            {
                setText(automaticText);
            }
        }
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
    public void setVisible(boolean legendShowNew)
    {
        if(visible != legendShowNew)
        {
            this.visible = legendShowNew;
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
    public Paint getPaint()
    {
        return outsideTitle.getPaint();
    }

    @Override
    public void setPaint(Paint paintNew)
    {
        outsideTitle.setPaint(paintNew);	
    }

    @Override
    public Font getFont()
    {
        return outsideTitle.getFont();
    }

    @Override
    public void setFont(Font fontNew)
    {
        outsideTitle.setFont(fontNew);	
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

        BlockFrame newFrame = frameVisible ? new LineBorder(framePaint, frameStroke, new RectangleInsets(4, 4, 4, 4)) :  BlockBorder.NONE;      
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
    public void setBackgroundPaint(Paint paintNew)
    {
        outsideTitle.setBackgroundPaint(paintNew);
    }

    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }

    @Override
    public ChartStyleSupplier getSupplier()
    {
        return DEFAULT_STYLE_SUPPLIER;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public Paint getBackgroundPaint() 
    {
        return outsideTitle.getBackgroundPaint();
    }

    @Override
    public String getText() 
    {
        return outsideTitle.getText();
    }

    @Override
    public void setText(String textNew) 
    {        
        outsideTitle.setText(textNew);
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