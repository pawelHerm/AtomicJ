
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

import java.awt.Window;
import java.awt.geom.Point2D;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.resources.MapImageResource;
import atomicJ.sources.SimpleSpectroscopySource;


public abstract class SpectroscopySupervisorAdapter implements SpectroscopySupervisor
{
    @Override
    public void jumpToResults(Point2D p) {}
    @Override
    public void jumpToFigures(Point2D p) {}
    @Override
    public void jumpToResults(SimpleSpectroscopySource source) {}
    @Override
    public void jumpToFigures(SimpleSpectroscopySource source) {}

    @Override
    public void recalculate(ROI roi, ROIRelativePosition position) {}
    @Override
    public void recalculate(MapImageResource resource, ROI roi, ROIRelativePosition position) {}
    @Override
    public void recalculate(Point2D p) {}
    @Override
    public void recalculate(MapImageResource resource, Point2D p) {}
    @Override
    public void recalculate(SimpleSpectroscopySource source) {}
    @Override
    public void recalculateFullDialog() {}

    @Override
    public abstract Window getPublicationSite();
}
