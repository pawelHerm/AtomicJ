
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

import org.jfree.util.PublicCloneable;

import atomicJ.gui.annotations.AnnotationAnchorSigned;


public class ROIPolygonHole extends ROIPolygon implements Cloneable, PublicCloneable 
{
    private static final long serialVersionUID = 1L;

    private final Area datasetArea;

    public ROIPolygonHole(Area datasetArea, Path2D shape, Integer key, ROIStyle style) 
    {
        super(shape, key, style);

        this.datasetArea = datasetArea;
    }

    public ROIPolygonHole(Area datasetArea, Path2D shape, Integer key, String label, ROIStyle style) 
    {
        super(shape, key, label, style);

        this.datasetArea = datasetArea;
    }

    public ROIPolygonHole(ROIPolygonHole that)
    {
        this(that, that.getStyle());
    }

    public ROIPolygonHole(ROIPolygonHole that, ROIStyle style)
    {
        super(that, style);

        this.datasetArea = new Area(that.datasetArea);
    }

    public ROIPolygonHole(ROIPolygonHole that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);

        this.datasetArea = new Area(that.datasetArea);
    }

    @Override
    public ROIPolygonHole copy()
    {
        return new ROIPolygonHole(this);
    }

    @Override
    public ROIPolygonHole copy(ROIStyle style)
    {
        return new ROIPolygonHole(this, style);
    }

    @Override
    public ROIPolygonHole copy(ROIStyle style, Integer key, String label)
    {
        return new ROIPolygonHole(this, style, key, label);
    }

    @Override
    public Path2D getModifiableShape()
    {
        return super.getROIShape();
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
    public ROIProxy getProxy()
    {
        return new ROIPathHoleSerializationProxy(getFixedPartOfModifiableShape(), datasetArea, getCustomLabel(), isFinished());
    }

    private static class ROIPathHoleSerializationProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D datasetArea;
        private final Path2D holeShape;
        private final String customLabel;
        private final boolean finished;

        private ROIPathHoleSerializationProxy(Path2D holeShape, Area datasetArea, String customLabel, boolean finished)
        {
            this.holeShape = holeShape;
            this.datasetArea = new Path2D.Double(datasetArea);
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIPolygonHole recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            ROIPolygonHole roi = (customLabel != null) ? new ROIPolygonHole(new Area(datasetArea), holeShape, key, customLabel, roiStyle) : new ROIPolygonHole(new Area(datasetArea), holeShape, key, roiStyle);
            roi.setFinished(finished);

            return roi;
        }     
    }
}
