
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


import java.awt.Window;
import java.util.Map;

import atomicJ.gui.ChannelChart;
import atomicJ.gui.Channel2DChart;
import atomicJ.resources.SpectroscopyBasicResource;
import atomicJ.resources.StandardChannel2DResource;


public interface PreviewDestination 
{
    public Window getPublicationSite();	
    public void publishPreviewData(Map<SpectroscopyBasicResource, Map<String, ChannelChart<?>>> charts);
    public void publishPreviewed2DData(Map<StandardChannel2DResource, Map<String, Channel2DChart<?>>> charts);
    public void requestPreviewEnd();	
}
