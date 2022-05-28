
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

package atomicJ.gui.editors;

import java.util.*;

import atomicJ.gui.*;
import atomicJ.gui.boxplots.CustomizableXYBoxAndWhiskerRenderer;


public class SeriesSubeditorFactory 
{
    private SeriesSubeditorFactory(){};

    public static Subeditor getSubeditor(ChannelRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        if(renderer instanceof ContinuousSeriesRenderer)
        {
            return buildContinousSeriesSubeditor((ContinuousSeriesRenderer)renderer, boundedPlots);
        }
        else if(renderer instanceof DiscreteSeriesRenderer)
        {
            return buildDiscreteSeriesSubeditor((DiscreteSeriesRenderer)renderer, boundedPlots);       
        }
        else if(renderer instanceof CustomizableXYBarRenderer)
        {
            return buildBarSeriesSubeditor((CustomizableXYBarRenderer)renderer, boundedPlots);
        }
        else if(renderer instanceof CustomizableImageRenderer)
        {
            return buildImageSeriesSubeditor((CustomizableImageRenderer)renderer, boundedPlots);
        }
        else if(renderer instanceof CustomizableXYShapeRenderer)
        {
            return buildShapeSeriesSubeditor((CustomizableXYShapeRenderer)renderer, boundedPlots);
        }
        else if(renderer instanceof CustomizableXYBoxAndWhiskerRenderer)
        {
            return buildBoxSeriesSubeditor((CustomizableXYBoxAndWhiskerRenderer)renderer, boundedPlots);
        }
        else if(renderer instanceof Channel1DWithErrorRenderer)
        {
            return buildErrorSeriesSubeditor((Channel1DWithErrorRenderer)renderer, boundedPlots);
        }
        else
        {
            return null;
        }
    }

    private static Subeditor buildContinousSeriesSubeditor(ContinuousSeriesRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<ContinuousSeriesRenderer> boundedRenderers = getBounded(renderer, boundedPlots);

        Subeditor subeditor = new ContinuousSeriesSubeditor(renderer, boundedRenderers);
        return subeditor;
    }

    private static Subeditor buildDiscreteSeriesSubeditor(DiscreteSeriesRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<DiscreteSeriesRenderer> boundedRenderers = getBounded(renderer, boundedPlots);

        Subeditor subeditor = new DiscreteSeriesSubeditor(renderer, boundedRenderers);
        return subeditor;
    }

    private static Subeditor buildErrorSeriesSubeditor(Channel1DWithErrorRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<Channel1DWithErrorRenderer> boundedRenderers = getBounded(renderer, boundedPlots);

        Subeditor subeditor = new ContinuousErrorSeriesSubeditor(renderer, boundedRenderers);
        return subeditor;
    }


    private static Subeditor buildBarSeriesSubeditor(CustomizableXYBarRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<CustomizableXYBarRenderer> boundedRenderers = getBounded(renderer, boundedPlots);

        Subeditor subeditor = new BarSeriesSubeditor(renderer, boundedRenderers);
        return subeditor;

    }

    private static Subeditor buildImageSeriesSubeditor(CustomizableImageRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<CustomizableImageRenderer> boundedRenderers = getBounded(renderer, boundedPlots);

        Subeditor subeditor = new ImageSeriesSubeditor(renderer, boundedRenderers);
        return subeditor;
    }

    private static Subeditor buildShapeSeriesSubeditor(CustomizableXYShapeRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<CustomizableXYShapeRenderer> boundedRenderers = getBounded(renderer, boundedPlots);

        Subeditor subeditor = new ShapeSeriesSubeditor(renderer, boundedRenderers);
        return subeditor;
    }

    private static Subeditor buildBoxSeriesSubeditor(CustomizableXYBoxAndWhiskerRenderer renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<CustomizableXYBoxAndWhiskerRenderer> boundedRenderers = getBounded(renderer, boundedPlots);

        Subeditor subeditor = new BoxAndWhiskerSeriesSubeditor(renderer, boundedRenderers);
        return subeditor;
    }

    @SuppressWarnings("unchecked")
    private static <E extends ChannelRenderer> List<E> getBounded(E renderer, List<CustomizableXYBasePlot> boundedPlots)
    {
        List<E> boundedRenderers = new ArrayList<>();

        for(CustomizableXYBasePlot plot: boundedPlots)
        {
            int n = plot.getRendererCount();
            for(int i = 0;i<n;i++)
            {
                ChannelRenderer r = plot.getRenderer(i);
                if(isToBeBounded(renderer, r))
                {
                    boundedRenderers.add((E)r);
                }
            }
        }

        return boundedRenderers;
    }

    private static boolean isToBeBounded(ChannelRenderer renderer1, ChannelRenderer renderer2)
    {
        boolean typesAgree = renderer1.getClass().isInstance(renderer2);

        Object key1 = renderer1.getStyleKey();
        Object key2 = renderer2.getStyleKey();       
        boolean keysAgree = Objects.equals(key1, key2);

        boolean isBounded = (typesAgree && keysAgree);	
        return isBounded;
    }
}
