
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

import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.rois.ROI;


public interface GradientPaintReceiver extends ColorGradientReceiver, RangeModel
{
    public static final String GRADIENT_COLOR = "GRADIENT_COLOR";
    public static final String UNDERFLOW_COLOR = "UNDERFLOW_COLOR";
    public static final String OVERFLOW_COLOR = "OVERFLOW_COLOR";
    public static final String USE_OUTSIDE_RANGE_COLORS = "USE_OUTSIDE_RANGE_COLORS";
    public static final String GRADIENT_RANGE_SELECTOR = "GRADIENT_RANGE_SELECTOR";
    public static final String GRADIENT_MASK_SELECTOR = "GRADIENT_MASK_SELECTOR";
    public static final String MASK_COLOR = "MASK_COLOR";
    public static final String BOUNDS_UNIT = "BOUNDS_UNIT";

    public boolean isFullRange();
    public boolean isAutomaticRange();
    public boolean isColorROIFullRange();

    public void setMaskedRegion(ROI roi);
    public Color getMaskColor();
    public void setMaskColor(Color maskColor);
    public GradientMaskSelector getGradientMaskSelector();
    public void setGradientMaskSelector(GradientMaskSelector selector);

    public GradientRangeSelector getGradientRangeSelector();

    public QuantitativeSample getPaintedSample();

    public double getLowerFullBound();
    public double getUpperFullBound();

    public double getLowerAutomaticBound();
    public double getUpperAutomaticBound();

    public double getLowerROIBound();
    public void setLowerROIBound(double loweLensBoundNew);
    public double getUpperROIBound();
    public void setUpperROIBound(double upperLenBoundNew);
    public void setLensToFull();

    public void setGradientBounds(double lb, double ub);

    public PrefixedUnit getDataUnit();
    public PrefixedUnit getDisplayedUnit();

    public boolean getUseOutsideRangeColors();
    public void setUseOutsideRangeColors(boolean useEndcolors);

    public Color getGradientUnderflowColor();
    public Color getGradientOverflowColor();

    public void setGradientUnderflowColor(Color ufc);
    public void setGradientOverflowColor(Color ofc);
    boolean areROISamplesNeeded();
}
