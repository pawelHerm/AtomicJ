
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

import atomicJ.data.Data1D;
import atomicJ.gui.Channel1DPlot.Channel1DPlotFactory;


public class RawCurvePlotFactory implements Channel1DPlotFactory
{
    private static final Preferences PREF = Preferences.userNodeForPackage(RawCurvePlotFactory.class).node("Raw curve plot");

    private static final PreferredScaleBarStyle PREFERRED_DOMAIN_SCALE_BAR = new PreferredScaleBarStyle(PREF.node("DomainScaleBar"));
    private static final PreferredScaleBarStyle PREFERRED_RANGE_SCALE_BAR = new PreferredScaleBarStyle(PREF.node("RangeScaleBar"));

    private static final RawCurvePlotFactory INSTANCE = new RawCurvePlotFactory();

    private RawCurvePlotFactory()
    {}

    public static RawCurvePlotFactory getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Channel1DPlot getPlot(Data1D dataset) 
    {
        return new Channel1DPlot(dataset, PREF, PREFERRED_DOMAIN_SCALE_BAR, PREFERRED_RANGE_SCALE_BAR);
    }       
}
