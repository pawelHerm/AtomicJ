
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

package atomicJ.gui.rois;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.profile.ProfilePolyLine;


public class ROIFreeHand extends ROIPolygon
{
    private static final long serialVersionUID = 1L;

    public ROIFreeHand(Path2D shape, Integer key, ROIStyle style) 
    {
        super(shape, key, style);
    }

    public ROIFreeHand(Path2D shape, Integer key, String label, ROIStyle style) 
    {
        super(shape, key, label, style);
    }

    public ROIFreeHand(ROIFreeHand that)
    {
        super(that); 
    }

    public ROIFreeHand(ROIFreeHand that, ROIStyle style)
    {
        super(that, style); 
    }

    public ROIFreeHand(ROIFreeHand that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label); 
    }

    @Override
    public ROIFreeHand copy()
    {
        return new ROIFreeHand(this);
    }

    @Override
    public ROIFreeHand copy(ROIStyle style)
    {
        return new ROIFreeHand(this, style);
    }

    @Override
    public ROIFreeHand copy(ROIStyle style, Integer key, String label)
    {
        return new ROIFreeHand(this, style, key, label);
    }

    @Override
    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        if(modifiers.contains(ModifierKey.CONTROL))
        {
            Point2D lastFixedPoint = getLastFixedPoint();
            Point2D endNew = ProfilePolyLine.correctPointCoordinates(lastFixedPoint.getX(), lastFixedPoint.getY(), x, y, modifiers);
            lineTemporailyTo(endNew.getX(), endNew.getY(), true);
        }
        else
        {
            lineTo(x, y, true); 
        }
    }

    @Override
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        if(modifiers.contains(ModifierKey.CONTROL))
        {
            Point2D lastFixedPoint = getLastFixedPoint();
            Point2D endNew = ProfilePolyLine.correctPointCoordinates(lastFixedPoint.getX(), lastFixedPoint.getY(), x, y, modifiers);
            lineTo(endNew.getX(), endNew.getY(), true);
        }
    }

    @Override
    public List<ROIDrawable> split(double[][] polylineVertices)
    {
        List<Path2D> paths = ROIPolygon.evaluateCrosssectioning(getROIShape(), polylineVertices);
        if(paths == null)
        {
            return Collections.<ROIDrawable>singletonList(this);
        }

        List<ROIDrawable> splitROIs = new ArrayList<>();

        for(Path2D path : paths)
        {
            ROIFreeHand r = new ROIFreeHand(path, -1, getStyle());
            splitROIs.add(r);
        }

        return splitROIs;
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode)
    {
        if(oldMode.isROI())
        {
            return oldMode;
        }
        return MouseInputModeStandard.FREE_HAND_ROI;
    }

    @Override
    public ROIProxy getProxy()
    {
        return new ROIFreeHandPathSerializationProxy(getFixedPartOfModifiableShape(), getCustomLabel(), isFinished());
    }

    private static class ROIFreeHandPathSerializationProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D roiShape;
        private final String customLabel;
        private final boolean finished;

        private ROIFreeHandPathSerializationProxy(Path2D roiShape, String customLabel, boolean finished)
        {
            this.roiShape = roiShape;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIFreeHand recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            ROIFreeHand roi = (customLabel != null) ? new ROIFreeHand(roiShape, key, customLabel, roiStyle) : new ROIFreeHand(roiShape, key, roiStyle);
            roi.setFinished(finished);

            return roi;
        }     
    }
}
