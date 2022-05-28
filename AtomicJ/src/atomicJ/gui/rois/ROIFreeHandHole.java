
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

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.profile.ProfilePolyLine;


public class ROIFreeHandHole extends ROIPolygon
{
    private static final long serialVersionUID = 1L;

    private final Area datasetArea;

    public ROIFreeHandHole(Area datasetArea, Path2D shape, Integer key, ROIStyle style) 
    {
        super(shape, key, style);

        this.datasetArea = datasetArea;
    }

    public ROIFreeHandHole(Area datasetArea, Path2D shape, Integer key, String label, ROIStyle style) 
    {
        super(shape, key, label, style);

        this.datasetArea = datasetArea;
    }

    public ROIFreeHandHole(ROIFreeHandHole that)
    {
        this(that, that.getStyle()); 
    }

    public ROIFreeHandHole(ROIFreeHandHole that, ROIStyle style)
    {
        super(that, style); 

        this.datasetArea = new Area(that.datasetArea);
    }

    public ROIFreeHandHole(ROIFreeHandHole that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label); 

        this.datasetArea = new Area(that.datasetArea);
    }

    @Override
    public ROIFreeHandHole copy()
    {
        return new ROIFreeHandHole(this);
    }

    @Override
    public ROIFreeHandHole copy(ROIStyle style)
    {
        return new ROIFreeHandHole(this, style);
    }

    @Override
    public ROIFreeHandHole copy(ROIStyle style, Integer key, String label)
    {
        return new ROIFreeHandHole(this, style, key, label);
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
    public Point2D getDefaultRotationCenter(AnnotationAnchorSigned anchor)
    {
        Rectangle2D shape = getModifiableShape().getBounds2D();

        double rotCenterX = shape.getCenterX();
        double rotCenterY = shape.getCenterY();

        return new Point2D.Double(rotCenterX, rotCenterY);
    }

    @Override
    public Path2D getROIShape()
    {
        Area roiShape = new Area(this.datasetArea);
        roiShape.subtract(new Area(super.getROIShape()));

        return new GeneralPath(roiShape);
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
        return new ROIFreeHandPathHoleSerializationProxy(getFixedPartOfModifiableShape(), datasetArea, getCustomLabel(), isFinished());
    }

    private static class ROIFreeHandPathHoleSerializationProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D datasetArea;
        private final Path2D holeShape;
        private final String customLabel;
        private final boolean finished;

        private ROIFreeHandPathHoleSerializationProxy(Path2D holeShape, Area datasetArea, String customLabel, boolean finished)
        {
            this.holeShape = holeShape;
            this.datasetArea = new Path2D.Double(datasetArea); //java.awt.area is not serializable, so we need to convert it Path2D.Double
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIFreeHandHole recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            ROIFreeHandHole roi = (customLabel != null) ?  new ROIFreeHandHole(new Area(datasetArea), holeShape, key, customLabel, roiStyle) : new ROIFreeHandHole(new Area(datasetArea), holeShape, key, roiStyle);
            roi.setFinished(finished);

            return roi;
        }     
    }
}
