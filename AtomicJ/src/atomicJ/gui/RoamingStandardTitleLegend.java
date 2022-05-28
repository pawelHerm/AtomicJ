
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

import org.jfree.chart.title.LegendTitle;


public class RoamingStandardTitleLegend extends RoamingTitleLegend
{	
    private static final Font OUTSIDE_LEGEND_DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 14);

    public RoamingStandardTitleLegend(String name, LegendTitle outsideLegend, PreferredStandardRoamingLegendTitleStyle preferredStyle)
    {
        super(name, outsideLegend, preferredStyle);
        outsideLegend.setItemFont(OUTSIDE_LEGEND_DEFAULT_FONT);
        setPreferredFrameStyle(preferredStyle);
    }

    protected void setPreferredFrameStyle(PreferredStandardRoamingLegendTitleStyle preferredStyle)
    {		
        Paint backgroundPaint = preferredStyle.getBackgroundPaint();
        getOutsideTitle().setBackgroundPaint(backgroundPaint);
    }


    @Override
    public LegendTitle getOutsideTitle()
    {
        LegendTitle outsideLegend = (LegendTitle)super.getOutsideTitle();
        return outsideLegend;
    }

    @Override
    public Paint getBackgroundPaint() 
    {
        Paint backgroundPaint = getOutsideTitle().getBackgroundPaint();
        return backgroundPaint;
    }

    @Override
    public void setBackgroundPaint(Paint paintNew) 
    {
        getOutsideTitle().setBackgroundPaint(paintNew);
    }
}