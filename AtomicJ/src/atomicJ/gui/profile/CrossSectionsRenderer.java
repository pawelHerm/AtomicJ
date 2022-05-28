
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

package atomicJ.gui.profile;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import atomicJ.gui.ContinuousSeriesRenderer;
import atomicJ.gui.CurveMarker;
import atomicJ.gui.MarkerRenderer;
import atomicJ.gui.PreferredContinuousSeriesRendererStyle;
import atomicJ.gui.StyleTag;

public class CrossSectionsRenderer extends ContinuousSeriesRenderer implements MarkerRenderer
{
    private final List<CurveMarker> markers = new ArrayList<>();

    public CrossSectionsRenderer(PreferredContinuousSeriesRendererStyle preferredStyle, Object layerKey, StyleTag styleKey, String name) 
    {
        super(preferredStyle, layerKey, styleKey, name);
    }

    @Override
    public void  setBasePaint(Paint paint)
    {
        //this check for null is necessary, because this method is called - indirectly - from the constructor
        //of the ancestor of this class
        if(markers != null)
        {			
            for(CurveMarker marker : markers)
            {
                marker.setFillPaint(paint, false);
            }
        }


        super.setBasePaint(paint);
    }

    public void addMarker(CurveMarker marker)
    {
        Paint markerPaint = getBasePaint();
        /*
		if(markerPaint instanceof Color)
		{
			markerPaint = ((Color)markerPaint).darker();
		}
         */
        marker.setFillPaint(markerPaint);
        markers.add(marker);
        addAnnotation(marker);	
    }

    public void removeMarker(CurveMarker marker)
    {
        markers.remove(marker);
        removeAnnotation(marker);	
    }

    @Override
    public boolean getIncludeInDataBounds()
    {
        return getBaseSeriesVisible();
    }

    //margin that must be left for annotations beneath the point of lowest vale / above the point of highest value
    // for annotations; this value is in pixels (i.e. in Java 2D space, not the data space)
    @Override
    public int getMarkerMarginHeight()
    {
        int height = 0;

        for(CurveMarker marker : markers)
        {
            height = Math.max(height, marker.getPixelHeight());
        }

        return height;
    }  

    @Override
    public int getMarkerMarginWidth()
    {
        int width = 0;

        for(CurveMarker marker : markers)
        {
            width = Math.max(width, marker.getPixelWidth());
        }

        return width;
    }  
}
