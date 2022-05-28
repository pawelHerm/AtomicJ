
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

package atomicJ.analysis;


public final class VisualizationChartSettings
{
    private final boolean shown;
    private final boolean plotFit;

    public VisualizationChartSettings()
    {
        this(true, true);
    }

    public VisualizationChartSettings(boolean show, boolean plotFit)
    {
        this.shown = show;
        this.plotFit = plotFit;
    }

    public boolean isShown()
    {
        return shown;
    }

    public boolean isFitPlotted()
    {
        return plotFit;
    }
}
