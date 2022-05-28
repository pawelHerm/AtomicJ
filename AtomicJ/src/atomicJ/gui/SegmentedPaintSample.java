
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SegmentedPaintSample extends JPanel implements MouseMotionListener, MouseListener
{
    private static final long serialVersionUID = 1L;

    private final int thumbWidth = 12;
    private final int thumbHeight = 12;

    private final int preferredStripWidth = 80;
    private final int preferredStripHeight = 14;

    private int caughtKnobIndex = -1;

    private List<GeneralPath> knobShapes;
    private List<Float> knobLocations = new ArrayList<>();

    private Paint solidPartPaint = Color.black;
    private Paint gapsPaint = Color.white;
    private final Paint solidPaintDisabled = new Color(120,120,120);
    private final Paint gapPaintDisabled = new Color(220,220,220);
    private final Paint knobPaint = Color.darkGray;
    private final Dimension preferredSize;

    private final Set<ChangeListener> changeListeners = new LinkedHashSet<>();

    public SegmentedPaintSample()
    {
        this.preferredSize = new Dimension(preferredStripWidth, preferredStripHeight + thumbHeight + 2);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public Paint getSolidPartPaint()
    {
        return solidPartPaint;
    }

    public void setSolidPartPaint(Paint paint)
    {
        this.solidPartPaint = paint;
        repaint();
    }

    public Paint getGapPaint()
    {
        return gapsPaint;
    }

    public void setGapPaint(Paint paint)
    {
        this.gapsPaint = paint;
        repaint();
    }

    public void addChangeListener(ChangeListener listener)
    {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener)
    {
        changeListeners.remove(listener);
    }

    private void fireChangeEvent()
    {
        ChangeEvent event = new ChangeEvent(this);

        for(ChangeListener listener : changeListeners)
        {
            listener.stateChanged(event);
        }
    }

    public float[] getPattern(float patternLength)
    {
        int knobCount = knobLocations.size();
        float[] pattern = new float[knobCount + 1];

        float previousLocation = 0;

        for(int i = 0; i<knobCount; i++)
        {
            float currentLocation = knobLocations.get(i);
            pattern[i] = (currentLocation - previousLocation)*patternLength;
            previousLocation = currentLocation;
        }

        pattern[knobCount] = patternLength*(1 - previousLocation);
        return pattern;
    }

    public void removeKnobs()
    {
        setKnobs(Collections.<Float>emptyList());
    }

    public void setKnobs(List<Float> knobLocation)
    {    	
        this.knobLocations = knobLocation;   	
        repaint();
    }

    @Override
    public Dimension getPreferredSize() 
    {
        return this.preferredSize;
    }

    @Override
    public Dimension getMaximumSize() 
    {
        return this.preferredSize;
    }

    private float getKnobLocation(int x, int index)
    {
        Dimension size = getSize();
        Insets insets = getInsets();
        double x0 = insets.left;
        double d_X = x - x0;
        double width = size.getWidth() - insets.left - insets.right - 1;
        double xScaled = d_X/width;

        double previousKnobLocation = index == 0 ? 0: knobLocations.get(index - 1);
        double nextKnobLocation = index == knobLocations.size() - 1 ? 1: knobLocations.get(index + 1);


        double result = Math.min(Math.max(xScaled, previousKnobLocation), nextKnobLocation);
        return (float)result;

    }

    private Paint getKnobPaint()
    {
        if(isEnabled())
        {
            return knobPaint;
        }
        else
        {
            return Color.gray;
        }
    }

    private Paint getStripPaint(int index)
    {
        boolean isEven = (index % 2 == 0);

        if(isEnabled())
        {
            if(isEven)
            {
                return solidPartPaint;
            }
            else
            {
                return gapsPaint;
            }
        }
        else
        {
            if(isEven)
            {
                return solidPaintDisabled;
            }
            else
            {
                return gapPaintDisabled;
            }
        }
    }

    @Override
    public void paintComponent(final Graphics g) 
    {
        final Graphics2D g2 = (Graphics2D) g;

        super.paintComponent(g);

        //builds stripArea

        Dimension size = getSize();
        Insets insets = getInsets();
        double x0 = insets.left;
        double y0 = insets.top;

        double width = size.getWidth() - insets.left - insets.right;
        double availableHeightForStrip = size.getHeight() - insets.top - insets.bottom - thumbWidth - 2;       

        double stripHeight = Math.min(preferredStripHeight, availableHeightForStrip);
        double freeSpace = availableHeightForStrip - stripHeight;
        //builds thumbShape

        int knobCount = knobLocations.size();

        double previousKnobPosition = x0;

        List<GeneralPath> knobShapes = new ArrayList<>();

        Paint knobPaint = getKnobPaint();
        Paint strokePaint = isEnabled() ? Color.black : Color.lightGray;

        for(int i = 0; i<knobCount; i++)
        {
            double f = knobLocations.get(i);
            double x1 = x0 + f*width;
            double y1 = y0 + availableHeightForStrip;

            GeneralPath knob = new GeneralPath();
            knob.moveTo(x1, y1);
            knob.lineTo(x1 + thumbWidth/2., y1 + thumbHeight);
            knob.lineTo(x1 - thumbWidth/2., y1 + thumbHeight);

            knob.closePath();

            knobShapes.add(knob);

            g2.setPaint(knobPaint);
            g2.fill(knob);
            g2.setPaint(strokePaint);
            g2.draw(knob);

            Rectangle2D stripSegment = new Rectangle2D.Double(previousKnobPosition, y0 + freeSpace, x1 - previousKnobPosition , stripHeight);

            Paint stripSegmentPaint = getStripPaint(i);
            g2.setPaint(stripSegmentPaint);
            g2.fill(stripSegment);
            g2.setPaint(strokePaint);
            g2.draw(stripSegment);

            previousKnobPosition = x1;
        }

        this.knobShapes = knobShapes;

        //drawLastStripSegment

        Rectangle2D stripSegment = new Rectangle2D.Double(previousKnobPosition, y0 + freeSpace, width - previousKnobPosition , stripHeight);

        Paint stripSegmentPaint = getStripPaint(knobCount);
        g2.setPaint(stripSegmentPaint);
        g2.fill(stripSegment);
        g2.setPaint(strokePaint);
        g2.draw(stripSegment);   

        g2.dispose();
    }

    private int getCaughtKnobIndex(int x, int y)
    {
        for(int i = 0; i<knobShapes.size(); i++)
        {
            Shape shape = knobShapes.get(i);
            if(shape.contains(x,y))
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void mouseDragged(MouseEvent evt) 
    {
        if(isEnabled() && caughtKnobIndex>= 0)
        {
            int x = evt.getX();	        

            float caughtKnobLocationNew =  getKnobLocation(x, caughtKnobIndex);

            knobLocations.set(caughtKnobIndex,caughtKnobLocationNew);
            fireChangeEvent();

            repaint();
        }        
    }

    @Override
    public void mouseMoved(MouseEvent evt) 
    {
    }

    @Override
    public void mouseClicked(MouseEvent evt) 
    {		
    }

    @Override
    public void mouseEntered(MouseEvent evt) 
    {	
    }

    @Override
    public void mouseExited(MouseEvent evt) 
    {
    }

    @Override
    public void mousePressed(MouseEvent evt) 
    {
        if(isEnabled())
        {
            int x = evt.getX();
            int y = evt.getY();
            caughtKnobIndex = getCaughtKnobIndex(x, y);		
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) 
    {
        if(isEnabled())
        {
            caughtKnobIndex = -1;
        }
    }
}