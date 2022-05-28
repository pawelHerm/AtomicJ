
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

import java.util.HashMap;
import java.util.Map;

import static atomicJ.data.Datasets.*;

public class DefaultChartStyleSupplier implements ChartStyleSupplier
{
    private static final DefaultChartStyleSupplier INSTANCE = new DefaultChartStyleSupplier();

    private final Map<String, Boolean> legendFrameVisible = new HashMap<>();
    private final Map<String, Boolean> legendVisible = new HashMap<>();
    private final Map<String, Boolean> legendInside = new HashMap<>();
    private final Map<String, Double> legendInsideX = new HashMap<>();
    private final Map<String, Double> legendInsideY = new HashMap<>();

    private  DefaultChartStyleSupplier()
    {	
        legendFrameVisible.put(MAP_PLOT, false);
        legendFrameVisible.put(DENSITY_PLOT, false);
        legendFrameVisible.put(IMAGE_PLOT, false);
        legendFrameVisible.put(SLICE_PLOT, false);

        legendVisible.put(CALIBRATION_PLOT, true);
        legendVisible.put(TRIMMING_PLOT, true);
        legendVisible.put(HSTOGRAM_PLOT, true);
        legendVisible.put(RANGE_SELECTION_HSTOGRAM_PLOT, true);
        legendVisible.put(MAP_PLOT, true);
        legendVisible.put(DENSITY_PLOT, true);
        legendVisible.put(LIVE_PREVIEW_INDENTATION_PLOT, true);
        legendVisible.put(AFM_CURVE_PREVIEW_PLOT, true);
        legendVisible.put(FORCE_CURVE_PLOT, true);
        legendVisible.put(AVERAGED_FORCE_CURVE_PLOT, true);
        legendVisible.put(INDENTATION_PLOT, true);
        legendVisible.put(AVERAGED_INDENTATION_PLOT, true);
        legendVisible.put(POINTWISE_PLOT, true);
        legendVisible.put(AVERAGED_POINTWISE_PLOT, true);
        legendVisible.put(LIVE_PREVIEW_POINTWISE_MODULUS_PLOT, true);
        legendVisible.put(CONTACT_SELECTION_PLOT, true);
        legendVisible.put(BOX_AND_WHISKER_PLOT, false);

        legendInside.put(CALIBRATION_PLOT, true);
        legendInside.put(TRIMMING_PLOT, true);
        legendInside.put(HSTOGRAM_PLOT, true);
        legendInside.put(RANGE_SELECTION_HSTOGRAM_PLOT, true);
        legendInside.put(MAP_PLOT, false);
        legendInside.put(LIVE_PREVIEW_INDENTATION_PLOT, true);
        legendInside.put(AFM_CURVE_PREVIEW_PLOT, false);
        legendInside.put(FORCE_CURVE_PLOT, true);
        legendInside.put(AVERAGED_FORCE_CURVE_PLOT, true);
        legendInside.put(INDENTATION_PLOT, true);
        legendInside.put(AVERAGED_INDENTATION_PLOT, true);
        legendInside.put(POINTWISE_PLOT, true);
        legendInside.put(AVERAGED_POINTWISE_PLOT, true);
        legendInside.put(LIVE_PREVIEW_POINTWISE_MODULUS_PLOT, true);
        legendInside.put(CONTACT_SELECTION_PLOT, true);
        legendInside.put(CROSS_SECTION_PLOT, false);
        legendInside.put(STACK_SLICE, true);
        legendInside.put(BOX_AND_WHISKER_PLOT, false);

        legendInsideX.put(CALIBRATION_PLOT, 0.75);
        legendInsideX.put(TRIMMING_PLOT, 0.75);
        legendInsideX.put(HSTOGRAM_PLOT, 0.75);
        legendInsideX.put(RANGE_SELECTION_HSTOGRAM_PLOT, 0.75);
        legendInsideX.put(MAP_PLOT, 0.75);
        legendInsideX.put(DENSITY_PLOT, 0.75);
        legendInsideX.put(LIVE_PREVIEW_INDENTATION_PLOT, 0.25);
        legendInsideX.put(AFM_CURVE_PREVIEW_PLOT, 0.75);
        legendInsideX.put(FORCE_CURVE_PLOT, 0.75);
        legendInsideX.put(AVERAGED_FORCE_CURVE_PLOT, 0.75);
        legendInsideX.put(INDENTATION_PLOT, 0.3);
        legendInsideX.put(AVERAGED_INDENTATION_PLOT, 0.3);
        legendInsideX.put(POINTWISE_PLOT, 0.3);
        legendInsideX.put(AVERAGED_POINTWISE_PLOT, 0.3);
        legendInsideX.put(LIVE_PREVIEW_POINTWISE_MODULUS_PLOT, 0.25);
        legendInsideX.put(CONTACT_SELECTION_PLOT, 0.75);
        legendInsideX.put(CROSS_SECTION_PLOT, 0.75);
        legendInsideX.put(BOX_AND_WHISKER_PLOT, 0.75);

        legendInsideY.put(CALIBRATION_PLOT, 0.8);
        legendInsideY.put(TRIMMING_PLOT, 0.8);
        legendInsideY.put(HSTOGRAM_PLOT, 0.8);
        legendInsideY.put(RANGE_SELECTION_HSTOGRAM_PLOT, 0.8);
        legendInsideY.put(MAP_PLOT, 0.5);
        legendInsideY.put(DENSITY_PLOT, 0.5);
        legendInsideY.put(LIVE_PREVIEW_INDENTATION_PLOT, 0.8);
        legendInsideY.put(AFM_CURVE_PREVIEW_PLOT, 0.8);
        legendInsideY.put(FORCE_CURVE_PLOT, 0.75);
        legendInsideY.put(AVERAGED_FORCE_CURVE_PLOT, 0.75);
        legendInsideY.put(INDENTATION_PLOT, 0.75);
        legendInsideY.put(AVERAGED_INDENTATION_PLOT, 0.75);
        legendInsideY.put(POINTWISE_PLOT, 0.75);
        legendInsideY.put(AVERAGED_POINTWISE_PLOT, 0.75);
        legendInsideY.put(LIVE_PREVIEW_POINTWISE_MODULUS_PLOT, 0.8);
        legendInsideX.put(CONTACT_SELECTION_PLOT, 0.8);
        legendInsideX.put(CROSS_SECTION_PLOT, 0.8);
        legendInsideX.put(BOX_AND_WHISKER_PLOT, 0.8);
    }

    public static DefaultChartStyleSupplier getSupplier()
    {
        return INSTANCE;
    }

    @Override
    public boolean getDefaultLegendVisible(String key)
    {
        Boolean visible = legendVisible.get(key);
        boolean visibleBool = (visible != null) ? visible : true;

        return visibleBool;
    }

    @Override
    public boolean getDefaultLegendInside(String key)
    {
        Boolean inside = legendInside.get(key);
        boolean insideBool = (inside != null) ? inside : false;

        return insideBool;
    }

    @Override
    public double getDefaultLegendInsideX(String key)
    {
        Double x = legendInsideX.get(key);
        double xPrimitive = (x != null) ? x: 0.75;
        return xPrimitive;
    }

    @Override
    public double getDefaultLegendInsideY(String key)
    {
        Double y = legendInsideY.get(key);
        double yPrimitive = (y != null) ? y: 0.8;
        return yPrimitive;
    }

    @Override
    public boolean getDefaultLegendFrameVisible(String key) 
    {
        Boolean visible = legendFrameVisible.get(key);
        boolean visibleBool = (visible != null) ? visible : true;

        return visibleBool;
    }

    @Override
    public boolean getDefaultTitleFrameVisible(String key) {
        return false;
    }

    @Override
    public boolean getDefaultTitleVisible(String key) {
        return false;
    }

    @Override
    public boolean getDefaultTitleInside(String key) {
        return false;
    }

    @Override
    public double getDefaultTitleInsideY(String key) {
        return 0.8;
    }

    @Override
    public double getDefaultTitleInsideX(String key) {
        return 0.5;
    }
}
