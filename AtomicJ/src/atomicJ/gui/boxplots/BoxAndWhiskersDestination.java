
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

package atomicJ.gui.boxplots;

import java.awt.Window;
import java.io.File;
import java.util.Map;

import atomicJ.data.QuantitativeSample;
import atomicJ.gui.CustomizableXYBaseChart;


public interface BoxAndWhiskersDestination 
{
    public Window getBoxPlotPublicationSite();
    public boolean isEmpty();

    public void publishBoxPlots(File defaultOutputLocation, String shortName, String longName,
            Map<String, Map<Object, QuantitativeSample>> samples, boolean show); 

    public void publishBoxPlots(Map<? extends BoxAndWhiskerResource,
            Map<String,CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>> boxPlots, boolean show);

    public void showBoxPlots(boolean show);
}

