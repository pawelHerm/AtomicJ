
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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

package atomicJ.gui.rois.line;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Set;

import org.jfree.util.PublicCloneable;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.profile.ProfilePolyLine;
import atomicJ.gui.profile.ProfileStyle;

public class ROIFreeHandCurve extends ROIPolyLine implements Cloneable, PublicCloneable
{
    private static final long serialVersionUID = 1L;


    public ROIFreeHandCurve(Point2D startPoint, Integer key, ProfileStyle style) 
    {
        super(startPoint, key, key.toString(), style); 
    }

    public ROIFreeHandCurve(Point2D startPoint, Integer key, String label, ProfileStyle style) 
    {	
        super(startPoint, key, label, style);
    }

    protected ROIFreeHandCurve(Path2D fixedPartOfShape, Point2D startPoint, Point2D lastFixedPoint, Point2D endPoint, int segmentCount, ProfileStyle style, Integer key, String label)
    {
        super(fixedPartOfShape, startPoint, lastFixedPoint, endPoint, segmentCount, style, key, label);
    }

    public ROIFreeHandCurve(ROIFreeHandCurve that)
    {
        super(that, that.getStyle());
    }

    public ROIFreeHandCurve(ROIFreeHandCurve that, ProfileStyle style)
    {
        super(that, style);
    }


    @Override
    public ROIFreeHandCurve copy()
    {
        return new ROIFreeHandCurve(this);
    }

    @Override
    public ROIFreeHandCurve copy(ProfileStyle style)
    {
        return new ROIFreeHandCurve(this, style);
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isProfile())
        {
            return oldMode;
        }
        return MouseInputModeStandard.ROI_FREE_HAND_SPLIT;
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
    public ROICurveSerializationProxy getProxy()
    {
        return new ProfileFreeHandSerializationProxy(getModifiableShape(), getStartPoint(), getLastFixedPoint(), getEndPoint(), getSegmentCount() , getCustomLabel(), isFinished());
    }

    private static class ProfileFreeHandSerializationProxy implements ROICurveSerializationProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D fixedPartOfShape;
        private final Point2D startPoint;
        private final Point2D lastFixedPoint;
        private final Point2D endPoint;
        private final int segmentCount;
        private final String customLabel;
        private final boolean finished;

        private ProfileFreeHandSerializationProxy(Path2D fixedPartOfShape, Point2D startPoint, Point2D lastFixedPoint, Point2D endPoint, int segmentCount, String customLabel, boolean finished)
        {
            this.fixedPartOfShape = fixedPartOfShape;
            this.startPoint = startPoint;
            this.lastFixedPoint = lastFixedPoint;
            this.endPoint = endPoint;
            this.segmentCount = segmentCount;
            this.customLabel = customLabel;
            this.finished = finished;
        }

        @Override
        public ROIFreeHandCurve recreateOriginalObject(ProfileStyle profileStyle, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();
            ROIFreeHandCurve profile = new ROIFreeHandCurve(fixedPartOfShape, startPoint, lastFixedPoint, endPoint, segmentCount, profileStyle, key, label);
            profile.setFinished(finished);

            return profile;
        }        
    }
}

