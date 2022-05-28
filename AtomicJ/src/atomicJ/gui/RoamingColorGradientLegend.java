
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

import org.jfree.chart.axis.AxisLocation;
import org.jfree.ui.RectangleEdge;


public class RoamingColorGradientLegend extends RoamingTitleLegend
{
    private Paint backgroundPaint;

    private Paint stripOutlinePaint;
    private Stroke stripOutlineStroke;
    private boolean stripOutlineVisible;
    private double stripWidth;

    public RoamingColorGradientLegend(String name, ColorGradientLegend outsideLegend, PreferredRoamingPaintScaleLegendStyle preferredStyle)
    {
        super(name, outsideLegend, preferredStyle);		
        setPreferredFrameStyle(preferredStyle);
    }

    public RoamingColorGradientLegend(RoamingColorGradientLegend oldLegend)
    {
        super(oldLegend);

        this.backgroundPaint = oldLegend.backgroundPaint;
        this.stripOutlinePaint = oldLegend.stripOutlinePaint;
        this.stripOutlineStroke = oldLegend.stripOutlineStroke;
        this.stripOutlineVisible = oldLegend.stripOutlineVisible;
        this.stripWidth = oldLegend.stripWidth;
    }

    public RoamingColorGradientLegend copy()
    {
        return new RoamingColorGradientLegend(this);
    }

    protected void setPreferredFrameStyle(PreferredRoamingPaintScaleLegendStyle preferredStyle)
    {		
        backgroundPaint = preferredStyle.getBackgroundPaint();	
        stripOutlinePaint = preferredStyle.getStripOutlinePaint();
        stripOutlineStroke = preferredStyle.getStripOutlineStroke();
        stripOutlineVisible = preferredStyle.isStripOutlineVisible();
        stripWidth = preferredStyle.getStripWidth();

        ColorGradientLegend outsideLegend = getOutsideTitle();
        outsideLegend.setBackgroundPaint(backgroundPaint);
        outsideLegend.setStripOutlineVisible(stripOutlineVisible);
        outsideLegend.setStripOutlinePaint(stripOutlinePaint);
        outsideLegend.setStripOutlineStroke(stripOutlineStroke);
        outsideLegend.setStripWidth(stripWidth);		
    }

    @Override
    public ColorGradientLegend getOutsideTitle()
    {
        ColorGradientLegend outsideLegend = (ColorGradientLegend)super.getOutsideTitle();
        return outsideLegend;
    }

    @Override
    public void setOutsidePosition(RectangleEdge position)
    {
        super.setOutsidePosition(position);
        ColorGradientLegend outsideLegend = (ColorGradientLegend)super.getOutsideTitle();
        if(RectangleEdge.RIGHT.equals(position) || RectangleEdge.BOTTOM.equals(position))
        {
            outsideLegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        }
        else
        {
            outsideLegend.setAxisLocation(AxisLocation.TOP_OR_LEFT);
        }
    }

    @Override
    public Paint getBackgroundPaint() 
    {
        return backgroundPaint;
    }

    @Override
    public void setBackgroundPaint(Paint backgroundPaint) 
    {
        this.backgroundPaint = backgroundPaint;
        getOutsideTitle().setBackgroundPaint(backgroundPaint);
    }

    public Paint getStripOutlinePaint()
    {
        return stripOutlinePaint;
    }

    public void setStripOutlinePaint(Paint stripOutlinePaint)
    {
        this.stripOutlinePaint = stripOutlinePaint;
        getOutsideTitle().setStripOutlinePaint(stripOutlinePaint);
    }

    public Stroke getStripOutlineStroke()
    {
        return stripOutlineStroke;
    }

    public void setStripOutlineStroke(Stroke stripOutlineStroke)
    {
        this.stripOutlineStroke = stripOutlineStroke;
        getOutsideTitle().setStripOutlineStroke(stripOutlineStroke);
    }

    public double getStripWidth()
    {
        return stripWidth;
    }

    public void setStripWidth(double stripWidth)
    {
        this.stripWidth = stripWidth;
        getOutsideTitle().setStripWidth(stripWidth);
    }

    public boolean isStripOutlineVisible()
    {
        return stripOutlineVisible;
    }

    public void setStripOutlineVisible(boolean stripOutlineVisible)
    {
        this.stripOutlineVisible = stripOutlineVisible;
        getOutsideTitle().setStripOutlineVisible(stripOutlineVisible);
    }
}