
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

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.prefs.Preferences;


import org.jfree.chart.renderer.xy.GradientXYBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;

import atomicJ.utilities.SerializationUtilities;

public class CustomizableXYBarRenderer extends XYBarRenderer implements ChannelRenderer
{
    private static final long serialVersionUID = 1L;

    private static final ColorSupplier DEFAULT_SUPPLIER = DefaultColorSupplier.getSupplier();

    private static final StandardXYBarPainter STANDARD_PAINTER = new StandardXYBarPainter();
    private static final GradientXYBarPainter GRADIENT_PAINTER = new GradientXYBarPainter();

    private final StyleTag styleKey;
    private String name;
    private final Preferences pref;	
    private ColorSupplier supplier;

    public CustomizableXYBarRenderer(StyleTag styleKey, String name)
    {
        this.styleKey = styleKey;
        this.name = name;
        this.pref = Preferences.userNodeForPackage(getClass()).node(styleKey.getPreferredStyleKey());

        setAutoPopulateSeriesPaint(false);				
        setPreferredStyle();
    }

    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }

    protected void setPreferredStyle()
    {
        Paint defFillPaint = getSupplier().getColor(styleKey);

        Paint defOutlinePaint = Color.black;
        Stroke defaultOutlineStroke = new BasicStroke(1.f);

        Paint fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, defFillPaint);
        Paint outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, BAR_OUTLINE_PAINT, defOutlinePaint);
        Stroke outlineStroke = SerializationUtilities.getStroke(pref, BAR_OUTLINE_STROKE, defaultOutlineStroke);

        double margin = pref.getDouble(BAR_MARGIN, 0);
        boolean shadowVisble = pref.getBoolean(BAR_SHADOW_VISIBLE, false);
        boolean gradient = pref.getBoolean(GRADIENT, false);
        boolean outlineVisible = pref.getBoolean(BAR_OUTLINE_VISIBLE, true);


        setBasePaint(fillPaint);
        setMargin(margin);
        setShadowVisible(shadowVisble);
        setGradientPainted(gradient);
        setBaseOutlineStroke(outlineStroke);
        setBaseOutlinePaint(outlinePaint);
        setDrawBarOutline(outlineVisible);
    }

    @Override
    public StyleTag getStyleKey()
    {
        return styleKey;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isGradientPainted()
    {
        XYBarPainter painter = getBarPainter();
        boolean gradientPainted = (painter instanceof GradientXYBarPainter);

        return gradientPainted;
    }

    public void setGradientPainted(boolean gradient)
    {
        XYBarPainter barPainter = gradient ? GRADIENT_PAINTER : STANDARD_PAINTER;
        setBarPainter(barPainter);
    }

    public ColorSupplier getSupplier()
    {
        ColorSupplier colorSupplier = (supplier != null) ? supplier : DEFAULT_SUPPLIER;

        return colorSupplier;
    }

    public void setSupplier(ColorSupplier supplier)
    {
        this.supplier = supplier;
    }
}
