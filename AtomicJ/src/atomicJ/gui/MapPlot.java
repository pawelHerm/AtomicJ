
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

import java.util.prefs.Preferences;

import atomicJ.data.Datasets;


public class MapPlot extends Channel2DPlot 
{
    private static final long serialVersionUID = 1L;

    public MapPlot(ProcessableXYZDataset dataset, String mapName, Preferences pref) 
    {
        super(dataset, new CustomizableXYShapeRenderer(new StandardStyleTag(mapName), mapName),  Datasets.MAP_PLOT, pref);
    }

    @Override
    public MapPlot clone()
    {
        MapPlot copy = (MapPlot) super.clone();
        copy.clearAnnotations();

        return copy;
    }
}
