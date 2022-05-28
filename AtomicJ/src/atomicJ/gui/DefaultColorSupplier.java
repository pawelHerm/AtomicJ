
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
import java.awt.Paint;
import java.util.Hashtable;

import static atomicJ.data.Datasets.*;

public class DefaultColorSupplier implements ColorSupplier
{
    public static final Paint[] DEFAULT_PAINT_SEQUENCE = new Paint[] {new Color(62, 224, 47), new Color(255, 12, 28), new Color(30, 33, 155), 
            new Color(255, 0, 246), new Color(0, 222, 219), new Color(255, 183, 30),new Color(210, 210, 210), new Color(10, 10, 10), new Color(120, 36, 4), new Color(123, 111, 118), new Color(255, 255, 100), new Color(254, 80, 128)};

    private static final DefaultColorSupplier INSTANCE = new DefaultColorSupplier();

    private final Hashtable<String, ColorGradient> defaultColorGradients = new Hashtable<>();
    private final Hashtable<String, Color> defaultColors = new Hashtable<>();

    private final Hashtable<String, Color> defaultGradientUnderflowPaints = new Hashtable<>();
    private final Hashtable<String, Color> defaultGradientOverflowPaints = new Hashtable<>();

    private  DefaultColorSupplier()
    {
        defaultColorGradients.put(FORCE_CONTOUR_MAPPING, GradientColorsBuiltIn.getGradients().get("Revealing"));
        defaultColorGradients.put(POINTWISE_MODULUS_STACK, GradientColorsBuiltIn.getGradients().get("Revealing"));
        defaultColorGradients.put(FORCE_STACK, GradientColorsBuiltIn.getGradients().get("Revealing"));

        defaultColors.put(YOUNG_MODULUS, new Color(102, 3, 102));
        defaultColors.put(TRANSITION_INDENTATION, new Color(51,153,0));
        defaultColors.put(TRANSITION_FORCE, new Color(0,102,102));
        defaultColors.put(CONTACT_POSITION, new Color(102,0,153));
        defaultColors.put(CONTACT_FORCE, new Color(102,8,17));
        defaultColors.put(DEFORMATION, new Color(240, 227, 46));
        defaultColors.put(ADHESION_FORCE, new Color(199, 21, 8));

        defaultColors.put(TOPOGRAPHY_TRACE, new Color(5,5,207));
        defaultColors.put(TOPOGRAPHY_RETRACE, new Color(5,5,207));
        defaultColors.put(DEFLECTION_TRACE, new Color(5,128,2));
        defaultColors.put(DEFLECTION_RETRACE, new Color(5,128,2));
        defaultColors.put(FRICTION_TRACE, new Color(179,27,0));
        defaultColors.put(FRICTION_RETRACE, new Color(179,27,0));

        defaultColors.put(RED, new Color(255,0,0));
        defaultColors.put(GREEN, new Color(29,138,13));
        defaultColors.put(BLUE, new Color(0,0,255));
        defaultColors.put(GRAY, new Color(80,80,80));

        defaultGradientUnderflowPaints.put(YOUNG_MODULUS, new Color(102, 0, 102));
        defaultGradientUnderflowPaints.put(TRANSITION_INDENTATION, new Color(0,74,26));
        defaultGradientUnderflowPaints.put(TRANSITION_FORCE, new Color(0, 5, 120));
        defaultGradientUnderflowPaints.put(CONTACT_POSITION, new Color(10, 2, 10));
        defaultGradientUnderflowPaints.put(CONTACT_FORCE, new Color(152, 0, 0));
        defaultGradientUnderflowPaints.put(IMAGE_PLOT, new Color(146, 1, 0));

        defaultGradientOverflowPaints.put(YOUNG_MODULUS, new Color(51, 182, 250));
        defaultGradientOverflowPaints.put(TRANSITION_INDENTATION, new Color(146, 250,52));
        defaultGradientOverflowPaints.put(TRANSITION_FORCE, new Color(172, 248, 219));
        defaultGradientOverflowPaints.put(CONTACT_POSITION, new Color(255, 23, 16));
        defaultGradientOverflowPaints.put(CONTACT_FORCE, new Color(244, 248, 109));
        defaultGradientOverflowPaints.put(IMAGE_PLOT,  new Color(255, 252, 19));
    }

    public static DefaultColorSupplier getSupplier()
    {
        return INSTANCE;
    }

    @Override
    public ColorGradient getGradient(StyleTag style)
    {
        ColorGradient gradient = defaultColorGradients.get(style.getInitialStyleKey());

        ColorGradient paint = (gradient != null) ? gradient : GradientColorsBuiltIn.getGradients().get("Golden");
        return paint;
    }

    @Override
    public Color getColor(StyleTag style)
    {
        Color storedColor = defaultColors.get(style.getInitialStyleKey());
        Color color = (storedColor != null) ? storedColor : Color.blue;
        return color;
    }

    @Override
    public Color getGradientUnderflow(StyleTag style) 
    {
        Color storedColor = defaultGradientUnderflowPaints.get(style.getInitialStyleKey());
        Color color = (storedColor != null) ? storedColor : Color.black;
        return color;   
    }

    @Override
    public Color getGradientOverflow(StyleTag style) 
    {
        Color storedColor = defaultGradientOverflowPaints.get(style.getInitialStyleKey());
        Color color = (storedColor != null) ? storedColor : Color.white;
        return color;	
    }

    public static Paint getPaint(int index)
    {
        int n = DEFAULT_PAINT_SEQUENCE.length;
        int i = index % n;

        return DEFAULT_PAINT_SEQUENCE[i];
    }
}
