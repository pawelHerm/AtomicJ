
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

package atomicJ.gui.measurements;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Set;

import atomicJ.gui.ModifierKey;
import atomicJ.gui.profile.ProfilePolyLine;

public class DistanceFreeMeasurement extends DistancePolyMeasurement
{
    private static final long serialVersionUID = 1L;

    public DistanceFreeMeasurement(Point2D startPoint, Integer key, DistanceMeasurementStyle style) 
    {	
        super(startPoint, key, style);
    }

    public DistanceFreeMeasurement(Point2D startPoint, Integer key, String label, DistanceMeasurementStyle style) 
    {
        super(startPoint, key, label, style);
    }

    protected DistanceFreeMeasurement(Path2D fixedPartOfShape, Point2D startPoint, Point2D lastFixedPoint, Point2D endPoint, int segmentCount, DistanceMeasurementStyle style, Integer key, String label)
    {
        super(fixedPartOfShape, startPoint, lastFixedPoint, endPoint, segmentCount, style, key, label);
    }

    public DistanceFreeMeasurement(DistanceFreeMeasurement that)
    {
        super(that, that.getStyle());
    }

    public DistanceFreeMeasurement(DistanceFreeMeasurement that, DistanceMeasurementStyle style)
    {
        super(that, style);    
    }

    public DistanceFreeMeasurement(DistanceFreeMeasurement that, DistanceMeasurementStyle style, Integer key, String label)
    {
        super(that, style, key, label);    
    }

    @Override
    public DistanceFreeMeasurement copy()
    {
        return new DistanceFreeMeasurement(this);
    }

    @Override
    public DistanceFreeMeasurement copy(DistanceMeasurementStyle style)
    {
        return new DistanceFreeMeasurement(this, style);
    }

    @Override
    public DistanceFreeMeasurement copy(DistanceMeasurementStyle style, Integer key, String label)
    {
        return new DistanceFreeMeasurement(this, style, key, label);
    }

    @Override
    public boolean acceptsNodes()
    {
        return true;
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
    public MeasurementProxy getProxy()
    {
        return new FreeMeasurementSerializationProxy(getModifiableShape(), getStartPoint(), getLastFixedPoint(), getEndPoint(), getSegmentCount(), getCustomLabel(), isFinished());
    }

    private static class FreeMeasurementSerializationProxy implements MeasurementProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D fixedPartOfShape;
        private final Point2D startPoint;
        private final Point2D lastFixedPoint;
        private final Point2D endPoint;
        private final int segmentCount;
        private final String customLabel;
        private final boolean finished;

        private FreeMeasurementSerializationProxy(Path2D fixedPartOfShape, Point2D startPoint, Point2D lastFixedPoint, Point2D endPoint, int segmentCount, String customLabel, boolean finished)
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
        public DistanceFreeMeasurement recreateOriginalObject(DistanceMeasurementStyle style, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();
            DistanceFreeMeasurement measurement = new DistanceFreeMeasurement(fixedPartOfShape, startPoint, lastFixedPoint, endPoint, segmentCount, style, key, label);
            measurement.setFinished(finished);

            return measurement;
        }
    }
}

