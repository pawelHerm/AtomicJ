
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

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static atomicJ.data.Datasets.*;

public class DefaultSeriesStyleSupplier implements SeriesStyleSupplier
{
    private static final DefaultSeriesStyleSupplier INSTANCE = new DefaultSeriesStyleSupplier();

    private static final Stroke DEFAULT_STROKE = new BasicStroke(2.f);

    private final Map<String, Paint[]> paintArrays = new HashMap<>();
    private final Map<String, Paint> paints = new HashMap<>();
    private final Map<String, Integer> markerSizes = new HashMap<>();
    private final Map<String, Integer> markerIndices = new HashMap<>();
    private final Map<String, Boolean> markerVisibilities = new HashMap<>();
    private final Map<String, Boolean> lineVisibilities = new HashMap<>();
    private final Map<String, Stroke> lineStrokes = new HashMap<>();

    private  DefaultSeriesStyleSupplier()
    {
        paintArrays.put(CROSS_SECTION, new Paint[] {new Color(211, 116, 214), new Color(211, 200, 22), new Color(243, 237, 190),  new Color(143, 231, 226), new Color(89, 216, 14), new Color(205, 81, 215), new Color(255, 121, 40), new Color(177, 148, 164), new Color(255, 255, 100), new Color(254, 80, 128)});
        paintArrays.put(ADHESION_FORCE, new Paint[] {new Color(255, 153, 0), new Color(45, 106, 6), new Color(22, 17, 161), new Color(113, 58, 13),  new Color(69, 160, 117), new Color(105, 10, 239), new Color(0, 3, 140), new Color(145, 54, 0), new Color(69, 50, 61), new Color(85, 85, 0), new Color(192, 5, 95)});

        paints.put(APPROACH, new Color(153, 0, 102));
        paints.put(AVERAGED_APPROACH, new Color(153, 0, 102));
        paints.put(WITHDRAW, new Color(51, 153, 0));
        paints.put(AVERAGED_WITHDRAW, new Color(51, 153, 0));
        paints.put(APPROACH_TRIMMED, new Color(153, 0, 102));
        paints.put(WITHDRAW_TRIMMED, new Color(51, 153, 0));
        paints.put(APPROACH_SMOOTHED, new Color(51, 204, 0));
        paints.put(WITHDRAW_SMOOTHED, new Color(230, 10, 5));
        paints.put(INDENTATION_DATA, new Color(38, 117, 48));
        paints.put(AVERAGED_INDENTATION_DATA, new Color(38, 117, 48));
        paints.put(INDENTATION_FIT, new Color(245, 0, 16));
        paints.put(POINTWISE_MODULUS_FIT, new Color(245, 0, 16));
        paints.put(POINTWISE_MODULUS_DATA, new Color(153, 0, 0));
        paints.put(AVERAGED_POINTWISE_MODULUS_DATA, new Color(153, 0, 0));

        paints.put(CONTACT_POINT, new Color(255, 0, 0));
        paints.put(PULL_OFF_POINT, new Color(175, 175, 21));
        paints.put(MODEL_TRANSITION_POINT, new Color(21, 177, 210));
        paints.put(MODEL_TRANSITION_POINT_FORCE_CURVE, new Color(21, 177, 210));
        paints.put(MODEL_TRANSITION_POINT_INDENTATION_CURVE, new Color(21, 177, 210));
        paints.put(MODEL_TRANSITION_POINT_POINTWISE_MODULUS, new Color(21, 177, 210));
        paints.put(ADHESION_FORCE, new Color(255, 153, 0));
        paints.put(SMOOTHED, new Color(245, 0, 16));
        paints.put(FIT, new Color(245, 0, 16));

        paints.put(YOUNG_MODULUS, new Color(57, 136, 138));
        paints.put(TRANSITION_INDENTATION, new Color(51,153,0));
        paints.put(TRANSITION_FORCE, new Color(0,102,102));
        paints.put(CONTACT_POSITION, new Color(102,0,153));
        paints.put(CONTACT_FORCE, new Color(102,8,17));
        paints.put(DEFORMATION, new Color(240, 227, 46));
        paints.put(ADHESION_FORCE, new Color(199, 21, 8));

        paints.put(TOPOGRAPHY_TRACE, new Color(5,5,207));
        paints.put(TOPOGRAPHY_RETRACE, new Color(5,5,207));
        paints.put(DEFLECTION_TRACE, new Color(5,128,2));
        paints.put(DEFLECTION_RETRACE, new Color(5,128,2));
        paints.put(FRICTION_TRACE, new Color(179,27,0));
        paints.put(FRICTION_RETRACE, new Color(179,27,0));

        paints.put(RED, new Color(255,0,0));
        paints.put(GREEN, new Color(29,138,13));
        paints.put(BLUE, new Color(0,0,255));
        paints.put(GRAY, new Color(80, 80, 80));

        markerSizes.put(APPROACH, 3);
        markerSizes.put(AVERAGED_APPROACH, 7);
        markerSizes.put(WITHDRAW, 3);
        markerSizes.put(AVERAGED_WITHDRAW, 7);
        markerSizes.put(APPROACH_TRIMMED, 3);
        markerSizes.put(WITHDRAW_TRIMMED, 3);
        markerSizes.put(APPROACH_SMOOTHED, 3);
        markerSizes.put(WITHDRAW_SMOOTHED, 3);
        markerSizes.put(INDENTATION_DATA, 3);
        markerSizes.put(AVERAGED_INDENTATION_DATA, 7);
        markerSizes.put(INDENTATION_FIT, 3);
        markerSizes.put(POINTWISE_MODULUS_FIT, 3);
        markerSizes.put(POINTWISE_MODULUS_DATA, 5);
        markerSizes.put(AVERAGED_POINTWISE_MODULUS_DATA, 8);
        markerSizes.put(CONTACT_POINT, 9);
        markerSizes.put(PULL_OFF_POINT, 9);
        markerSizes.put(MODEL_TRANSITION_POINT, 9);
        markerSizes.put(MODEL_TRANSITION_POINT_FORCE_CURVE, 9);
        markerSizes.put(MODEL_TRANSITION_POINT_INDENTATION_CURVE, 9);
        markerSizes.put(MODEL_TRANSITION_POINT_POINTWISE_MODULUS, 9);
        markerSizes.put(ADHESION_FORCE, 9);
        markerIndices.put(SMOOTHED, 3);
        markerIndices.put(FIT, 3);

        markerIndices.put(APPROACH, 1);
        markerIndices.put(AVERAGED_APPROACH, 1);
        markerIndices.put(WITHDRAW, 1);
        markerIndices.put(AVERAGED_WITHDRAW, 1);
        markerIndices.put(APPROACH_TRIMMED, 1);
        markerIndices.put(WITHDRAW_TRIMMED, 1);
        markerIndices.put(APPROACH_SMOOTHED, 1);
        markerIndices.put(WITHDRAW_SMOOTHED, 1);
        markerIndices.put(INDENTATION, 1);
        markerIndices.put(INDENTATION_FIT, 1);
        markerIndices.put(POINTWISE_MODULUS_FIT, 1);
        markerIndices.put(POINTWISE_MODULUS_DATA, 5);
        markerIndices.put(AVERAGED_POINTWISE_MODULUS_DATA, 5);
        markerIndices.put(CONTACT_POINT, 1);
        markerIndices.put(PULL_OFF_POINT, 1);
        markerIndices.put(ADHESION_FORCE, 1);
        markerIndices.put(MODEL_TRANSITION_POINT, 1);
        markerIndices.put(MODEL_TRANSITION_POINT_FORCE_CURVE, 1);
        markerIndices.put(MODEL_TRANSITION_POINT_INDENTATION_CURVE, 1);
        markerIndices.put(MODEL_TRANSITION_POINT_POINTWISE_MODULUS, 1);
        markerIndices.put(SMOOTHED, 1);
        markerIndices.put(FIT, 1);

        markerVisibilities.put(APPROACH, false);
        markerVisibilities.put(AVERAGED_APPROACH, true);
        markerVisibilities.put(WITHDRAW, false);
        markerVisibilities.put(AVERAGED_WITHDRAW, true);
        markerVisibilities.put(APPROACH_TRIMMED, false);
        markerVisibilities.put(WITHDRAW_TRIMMED, false);
        markerVisibilities.put(APPROACH_SMOOTHED, false);
        markerVisibilities.put(WITHDRAW_SMOOTHED, false);
        markerVisibilities.put(INDENTATION_DATA, true);
        markerVisibilities.put(AVERAGED_INDENTATION_DATA, true);
        markerVisibilities.put(INDENTATION_FIT, false);
        markerVisibilities.put(POINTWISE_MODULUS_FIT, false);
        markerVisibilities.put(POINTWISE_MODULUS_DATA, true);
        markerVisibilities.put(AVERAGED_POINTWISE_MODULUS_DATA, true);
        markerVisibilities.put(CONTACT_POINT, true);
        markerVisibilities.put(PULL_OFF_POINT, true);
        markerVisibilities.put(MODEL_TRANSITION_POINT, true);
        markerVisibilities.put(MODEL_TRANSITION_POINT_FORCE_CURVE, true);
        markerVisibilities.put(MODEL_TRANSITION_POINT_INDENTATION_CURVE, true);
        markerVisibilities.put(MODEL_TRANSITION_POINT_POINTWISE_MODULUS, true);
        markerVisibilities.put(SMOOTHED, false);
        markerVisibilities.put(FIT, false);

        lineVisibilities.put(APPROACH, true);
        lineVisibilities.put(AVERAGED_APPROACH, false);
        lineVisibilities.put(WITHDRAW, true);
        lineVisibilities.put(AVERAGED_WITHDRAW, false);
        lineVisibilities.put(APPROACH_TRIMMED, true);
        lineVisibilities.put(WITHDRAW_TRIMMED, true);
        lineVisibilities.put(APPROACH_SMOOTHED, true);
        lineVisibilities.put(WITHDRAW_SMOOTHED, true);
        lineVisibilities.put(INDENTATION_DATA, false);
        lineVisibilities.put(AVERAGED_INDENTATION_DATA, false);
        lineVisibilities.put(INDENTATION_FIT, true);
        lineVisibilities.put(POINTWISE_MODULUS_FIT, true);
        lineVisibilities.put(POINTWISE_MODULUS_DATA, false);
        lineVisibilities.put(AVERAGED_POINTWISE_MODULUS_DATA, false);
        lineVisibilities.put(CONTACT_POINT, false);
        lineVisibilities.put(PULL_OFF_POINT, false);
        lineVisibilities.put(MODEL_TRANSITION_POINT, false);
        lineVisibilities.put(MODEL_TRANSITION_POINT_FORCE_CURVE, false);
        lineVisibilities.put(MODEL_TRANSITION_POINT_INDENTATION_CURVE, false);
        lineVisibilities.put(MODEL_TRANSITION_POINT_POINTWISE_MODULUS, false);
        lineVisibilities.put(SMOOTHED, true);
        lineVisibilities.put(FIT, true);

        Stroke thickStroke = new BasicStroke(2.0f);
        Stroke thinStroke = new BasicStroke(0f);

        lineStrokes.put(APPROACH, thickStroke);
        lineStrokes.put(AVERAGED_APPROACH, thickStroke);
        lineStrokes.put(WITHDRAW, thickStroke);
        lineStrokes.put(AVERAGED_WITHDRAW, thickStroke);
        lineStrokes.put(APPROACH_TRIMMED, thickStroke);
        lineStrokes.put(WITHDRAW_TRIMMED, thickStroke);
        lineStrokes.put(APPROACH_SMOOTHED, thickStroke);
        lineStrokes.put(WITHDRAW_SMOOTHED, thickStroke);
        lineStrokes.put(INDENTATION_DATA, thickStroke);
        lineStrokes.put(AVERAGED_INDENTATION_DATA, thickStroke);
        lineStrokes.put(INDENTATION_FIT, thickStroke);
        lineStrokes.put(POINTWISE_MODULUS_FIT, thickStroke);
        lineStrokes.put(POINTWISE_MODULUS_DATA, thickStroke);
        lineStrokes.put(AVERAGED_POINTWISE_MODULUS_DATA, thickStroke);
        lineStrokes.put(CONTACT_POINT, thinStroke);
        lineStrokes.put(PULL_OFF_POINT, thinStroke);
        lineStrokes.put(MODEL_TRANSITION_POINT, thinStroke);
        lineStrokes.put(SMOOTHED, thickStroke);
        lineStrokes.put(FIT, thickStroke);
    }

    public static DefaultSeriesStyleSupplier getSupplier()
    {
        return INSTANCE;
    }

    @Override
    public Paint getDefaultMarkerPaint(StyleTag key)
    {
        if(key instanceof IndexedStyleTag)
        {
            IndexedStyleTag indexedStyle = (IndexedStyleTag)key;
            return getDefaultMarkerPaint(indexedStyle.getInitialStyleKey(), indexedStyle.getIndex());
        }

        Paint paint = paints.get(key.getInitialStyleKey());
        return (paint != null) ? paint : Color.blue;
    }

    private Paint getDefaultMarkerPaint(Object key, int index)
    {      
        Paint[] paints = paintArrays.get(key);
        Paint paint = (paints != null) ? paints[index % paints.length] : Color.blue;       
        return paint;
    }

    @Override
    public int getDefaultMarkerIndex(StyleTag key) 
    {
        Integer index = markerIndices.get(key.getInitialStyleKey());

        return (index != null) ? index : 0;
    }

    @Override
    public int getDefaultMarkerSize(StyleTag key) 
    {
        Integer size = markerSizes.get(key.getInitialStyleKey());

        return (size != null) ? size : 4;
    }

    @Override
    public boolean getDefaultMarkersVisible(StyleTag key)
    {
        Boolean show = markerVisibilities.get(key.getInitialStyleKey());

        return (show != null) ? show : false;
    }

    @Override
    public boolean getDefaultJoiningLineVisible(StyleTag key)
    {
        Boolean show = lineVisibilities.get(key.getInitialStyleKey());

        return (show != null) ? show : true;
    }

    @Override
    public Stroke getDefaultJoiningLineStroke(StyleTag key)
    {
        Stroke stroke = lineStrokes.get(key.getInitialStyleKey());

        return (stroke != null) ? stroke : DEFAULT_STROKE;
    }
}
