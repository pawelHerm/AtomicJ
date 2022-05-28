
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

import static java.lang.Double.NaN;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import atomicJ.utilities.GeometryUtilities;


public class DistanceShapeFactors 
{
    private double startX = Double.NaN;
    private double startY = Double.NaN;

    private double endX = Double.NaN;
    private double endY = Double.NaN;

    private double lengthX = Double.NaN;
    private double lengthY = Double.NaN;
    private double angle = Double.NaN;

    private double length;

    private DistanceShapeFactors()
    {}


    public static DistanceShapeFactors getShapeFactors(Line2D line)
    {
        DistanceShapeFactors shapeFactors = new DistanceShapeFactors();

        double x0 = line.getX1();
        double y0 = line.getY1();

        double x1 = line.getX2();
        double y1 = line.getY2();

        double dx = (x1 - x0);
        double dy = (y1 - y0);

        shapeFactors.startX = x0;
        shapeFactors.startY = y0;
        shapeFactors.endX = x1;
        shapeFactors.endY = y1;
        shapeFactors.lengthX = Math.abs(dx);
        shapeFactors.lengthY = Math.abs(dy);
        shapeFactors.length = Math.sqrt(dx*dx + dy*dy);
        shapeFactors.angle = 180*Math.atan2(dy, dx)/Math.PI;

        return shapeFactors;
    }

    private static double getAngleInRadians(Line2D line)
    {
        double x0 = line.getX1();
        double y0 = line.getY1();

        double x1 = line.getX2();
        double y1 = line.getY2();

        double dx = (x1 - x0);
        double dy = (y1 - y0);

        double angle = Math.atan2(dy, dx);

        return angle;
    }

    public static double getLength(Line2D line)
    {
        double dx = line.getX2() - line.getX1();
        double dy = line.getY2() - line.getY1();

        double length = Math.sqrt(dx*dx + dy*dy);

        return length;
    }

    public static double getLength(Shape shape, double flatness)
    {
        PathIterator it = shape.getPathIterator(new AffineTransform(), flatness);

        double length = 0.0;

        double[] coords = new double[6];

        double startX = NaN;
        double startY = NaN;

        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);

        while (!it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);

            double x = coords[0];
            double y = coords[1];

            switch (segmentType) 
            {
            case PathIterator.SEG_CLOSE:
            {                   
                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);

                length += perimeterAddend(segment);

                startX = NaN;
                startY = NaN;
                segment.setLine(NaN, NaN, NaN, NaN);
                break;
            }


            case PathIterator.SEG_LINETO:
            {                    
                segment.setLine(segment.getX2(), segment.getY2(), x, y);

                length += perimeterAddend(segment);

                break;
            }


            case PathIterator.SEG_MOVETO:
            {
                startX = x;
                startY = y;

                segment.setLine(NaN, NaN, x, y);
                break;
            }

            default:
            {
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            }
            it.next();
        }        

        return length;
    }

    public static boolean intersects(Path2D path, Rectangle2D hotSpot)
    {
        PathIterator it = path.getPathIterator(new AffineTransform(), 0);

        double[] coords = new double[6];

        double startX = NaN;
        double startY = NaN;

        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);

        while (!it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);

            double x = coords[0];
            double y = coords[1];

            switch (segmentType) 
            {
            case PathIterator.SEG_CLOSE:
            {                   
                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);

                if(hotSpot.intersectsLine(segment))
                {
                    return true;
                }

                startX = NaN;
                startY = NaN;
                segment.setLine(NaN, NaN, NaN, NaN);
                break;
            }


            case PathIterator.SEG_LINETO:
            {                    
                segment.setLine(segment.getX2(), segment.getY2(), x, y);

                if(hotSpot.intersectsLine(segment))
                {
                    return true;
                }

                break;
            }


            case PathIterator.SEG_MOVETO:
            {
                startX = x;
                startY = y;

                segment.setLine(NaN, NaN, x, y);
                break;
            }

            default:
            {
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            }
            it.next();
        }        

        return false;
    }

    public static DirectedPosition getDirectedPositionFromLengthFraction(Shape shape, double lengthFraction)
    {
        double length = getLength(shape, 0.);
        double lengthPosition = length*lengthFraction;

        return getDirectedPosition(shape, lengthPosition);
    }

    public static DirectedPosition getDirectedPositionAtPathEnd(Shape shape)
    {
        Line2D line = getLastLinearSegment(shape);

        double x1 = line.getX2();
        double y1 = line.getY2();

        DirectedPosition position = new DirectedPosition(line, new Point2D.Double(x1, y1));

        return position;
    }

    public static Line2D getLastLinearSegment(Shape shape)
    {
        PathIterator it = shape.getPathIterator(null, 0.f);

        double[] coords = new double[6];

        double startX = NaN;
        double startY = NaN;

        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);

        while (!it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);

            double x = coords[0];
            double y = coords[1];

            switch (segmentType) 
            {
            case PathIterator.SEG_CLOSE:
            {
                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);

                startX = NaN;
                startY = NaN;

                break; 
            }
            case PathIterator.SEG_LINETO:
            {
                segment.setLine(segment.getX2(), segment.getY2(), x, y);

                break; 
            }
            case PathIterator.SEG_MOVETO:
            {
                startX = x;
                startY = y;

                segment.setLine(NaN, NaN, x, y);
                break;
            }

            default:
            {
                throw new IllegalArgumentException("PathIterator contains curved segments");

            }
            }
            it.next();
        } 

        return segment;
    }


    public static DirectedPosition getDirectedPosition(Shape shape, double lengthPosition)
    {
        PathIterator it = shape.getPathIterator(null, 0.f);

        double length = 0.0;

        double[] coords = new double[6];

        double startX = NaN;
        double startY = NaN;

        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);

        while (!it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);

            double x = coords[0];
            double y = coords[1];

            switch (segmentType) 
            {
            case PathIterator.SEG_CLOSE:
            {
                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);

                double segmentLength = perimeterAddend(segment);
                length += segmentLength;  

                if(length >= lengthPosition)
                {
                    double t = (lengthPosition - (length - segmentLength))/segmentLength;  

                    Point2D centralPoint = GeometryUtilities.interpolate(segment, t);
                    DirectedPosition positionP = new DirectedPosition(segment, centralPoint);
                    return positionP;
                }

                startX = NaN;
                startY = NaN;

                segment.setLine(NaN, NaN, NaN, NaN);
                break; 
            }
            case PathIterator.SEG_LINETO:
            {
                segment.setLine(segment.getX2(), segment.getY2(), x, y);

                double segmentLength = perimeterAddend(segment);
                length += segmentLength;  

                if(length >= lengthPosition)
                {
                    double t = (lengthPosition - (length - segmentLength))/segmentLength;
                    Point2D centralPoint = GeometryUtilities.interpolate(segment, t);
                    DirectedPosition positionP = new DirectedPosition(segment, centralPoint);
                    return positionP;
                }

                break; 
            }
            case PathIterator.SEG_MOVETO:
            {
                startX = x;
                startY = y;

                segment.setLine(NaN, NaN, x, y);
                break;
            }

            default:
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            it.next();
        } 

        return new DirectedPosition(new Line2D.Double(), new Point2D.Double());
    }

    public static KnobPositionTest getKnobPosition(Shape shape, Point2D clickedPoint)
    {
        PathIterator it = shape.getPathIterator(null, 0.f);

        double length = 0.0;

        double[] coords = new double[6];

        double startX = NaN;
        double startY = NaN;

        Line2D segment = new Line2D.Double(NaN, NaN, NaN, NaN);

        double minDistance = Double.POSITIVE_INFINITY;

        double currentPositionLength = 0;
        Point2D currentPoint = getFirstPoint(shape);

        while (!it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);

            double x = coords[0];
            double y = coords[1];

            switch (segmentType) 
            {
            case PathIterator.SEG_CLOSE:
            {
                segment.setLine(segment.getX2(), segment.getY2(), startX, startY);

                double dist = segment.ptSegDist(clickedPoint);

                double segmentLength = perimeterAddend(segment);

                if(dist < minDistance)
                {
                    KnobPositionTest segmentPosition = getCorrespondingPosition(segment, clickedPoint);
                    currentPositionLength = length + segmentPosition.getLengthPosition();
                    currentPoint = segmentPosition.getPoint();

                    minDistance = dist;
                }

                length += segmentLength;  

                startX = NaN;
                startY = NaN;

                segment.setLine(NaN, NaN, NaN, NaN);
                break; 
            }
            case PathIterator.SEG_LINETO:
            {
                segment.setLine(segment.getX2(), segment.getY2(), x, y);
                double dist = segment.ptSegDist(clickedPoint);

                double segmentLength = perimeterAddend(segment);

                if(dist < minDistance)
                {
                    KnobPositionTest segmentPosition = getCorrespondingPosition(segment, clickedPoint);
                    currentPositionLength = length + segmentPosition.getLengthPosition();
                    currentPoint = segmentPosition.getPoint();

                    minDistance = dist;
                }

                length += segmentLength;  

                break; 
            }
            case PathIterator.SEG_MOVETO:
            {
                startX = x;
                startY = y;

                segment.setLine(NaN, NaN, x, y);
                break;
            }

            default:
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            it.next();
        } 

        double lengthPosition = length > 0 ? currentPositionLength : 0;

        return new KnobPositionTest(currentPoint, lengthPosition);
    }

    public static class KnobPositionTest
    {
        private final double lengthPosition;
        private final Point2D point;

        public KnobPositionTest(Point2D point, double lengthPosition)
        {
            this.lengthPosition = lengthPosition;
            this.point = point;
        }

        public Point2D getPoint()
        {
            return point;
        }

        public double getLengthPosition()
        {
            return lengthPosition;
        }
    }

    private static KnobPositionTest getCorrespondingPosition(Line2D line, Point2D pt)
    {
        double x1 = line.getX1();
        double x2 = line.getX2();
        double y1 = line.getY1();
        double y2 = line.getY2();

        double x3 = pt.getX();
        double y3 = pt.getY();

        double length = GeometryUtilities.getLength(line);

        //max and min ensures that the knob will be within the range of the profile
        double r = Math.min(1, Math.max(0, ((y1-y3)*(y1-y2)-(x1-x3)*(x2-x1))/(length*length)));


        double xr = (1 - r)*x1 + r*x2;
        double yr = (1 - r)*y1 + r*y2;

        KnobPositionTest knobPosition = new KnobPositionTest(new Point2D.Double(xr, yr), r*length);

        return knobPosition;
    }

    public static Point2D getFirstPoint(Shape shape)
    {
        PathIterator it = shape.getPathIterator(null);

        Point2D firstPoint;
        if(!it.isDone())
        {
            double[] coords = new double[6];
            it.currentSegment(coords);

            firstPoint = new Point2D.Double(coords[0], coords[1]);
        }
        else{
            firstPoint = new Point2D.Double(Double.NaN, Double.NaN);
        }

        return firstPoint;
    }

    public static Point2D getLastPoint(Shape shape)
    {
        PathIterator it = shape.getPathIterator(null);

        double[] coords = new double[6];

        double startX = NaN;
        double startY = NaN;

        double lastPointX = NaN;
        double lastPointY = NaN;

        while (!it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);

            switch (segmentType) 
            {
            case PathIterator.SEG_CLOSE:
            {
                lastPointX = startX;
                lastPointY = startY;
                break;
            }
            case PathIterator.SEG_LINETO:
            {
                lastPointX = coords[0];
                lastPointY = coords[1];
                break;
            }
            case PathIterator.SEG_QUADTO:
            {
                lastPointX = coords[2];
                lastPointY = coords[3];
                break;
            }
            case PathIterator.SEG_CUBICTO:
            {
                lastPointX = coords[4];
                lastPointY = coords[5];
                break;
            }
            case PathIterator.SEG_MOVETO:
            {
                startX = coords[0];
                startY = coords[1];
                lastPointX = startX;
                lastPointY = startY;
                break;
            }
            default:
            {
                throw new IllegalArgumentException("PathIterator contains curved segments");
            }
            }
            it.next();
        }  

        Point2D lastPoint = new Point2D.Double(lastPointX, lastPointY);
        return lastPoint;
    }

    public static DistanceShapeFactors getShapeFactors(Shape shape, double flatness)
    {
        DistanceShapeFactors shapeFactors = new DistanceShapeFactors();

        shapeFactors.length = getLength(shape, flatness);

        //we call the geLastPoint() method, because it does not use the flattening iterator we
        //used in the getLength() method
        Point2D lastPoint = getLastPoint(shape);
        shapeFactors.endX = lastPoint.getX();
        shapeFactors.endY = lastPoint.getY();

        //we cannot use the values 'moveToX' and 'moveToY', because the path may contain
        //several segments 'MOVE_TO', e.x. if it is a dashed line
        Point2D firstPoint = getFirstPoint(shape);
        shapeFactors.startX = firstPoint.getX();
        shapeFactors.startY = firstPoint.getY();

        Rectangle2D bounds = shape.getBounds2D();
        shapeFactors.lengthX = bounds.getWidth();
        shapeFactors.lengthY = bounds.getHeight();

        return shapeFactors;
    }


    private static double perimeterAddend(Line2D s)
    {
        double dx = s.getX2() - s.getX1();
        double dy = s.getY2() - s.getY1();

        double addend = Math.sqrt(dx*dx + dy*dy);
        return addend;
    }


    public double getLength()
    {
        return length;
    }

    public double getLengthX()
    {
        return lengthX;
    }

    public double getLengthY()
    {
        return lengthY;
    }

    public double getAngle()
    {
        return angle;
    }

    public double getStartX()
    {
        return startX;
    }

    public double getStartY()
    {
        return startY;
    }

    public double getEndX()
    {
        return endX;
    }

    public double getEndY()
    {
        return endY;
    }

    public static double[][] getXYDTriples(Shape profile, int pointCount)
    {
        double[][] profilePoints = new double[pointCount][3];

        double length = getLength(profile, 0);


        double segmentCount = pointCount - 1;
        double segmentLength = length/segmentCount;


        for (int i = 0; i < pointCount; i++) 
        {
            double d = i*segmentLength;
            DirectedPosition pos = getDirectedPosition(profile, d);
            Point2D pt = pos.getPoint();

            profilePoints[i] = new double[] {pt.getX(), pt.getY(), d};
        }
        return profilePoints;
    }


    public static double[][] getXYDTriples(Line2D line, int pointCount)
    {
        double[][] profilePoints = new double[pointCount][3];

        Point2D anchor = line.getP1();
        Point2D endpoint = line.getP2();

        double x0 = anchor.getX();
        double y0 = anchor.getY();

        double x1 = endpoint.getX();
        double y1 = endpoint.getY();

        double dx = x1 - x0;
        double dy = y1 - y0;

        double length = Math.sqrt(dx*dx + dy*dy);

        double segmentCount = pointCount - 1;
        double segmentLength = length/segmentCount;

        for(int i = 0; i<pointCount; i++)
        {           
            double x = x0 + dx*i/segmentCount;
            double y = y0 + dy*i/segmentCount;
            double d = i*segmentLength;

            profilePoints[i] = new double[] {x, y, d};
        }

        return profilePoints;
    }

    public static double[][] getProfilePoints(Line2D line, int pointCount)
    {
        double[][] profilePoints = new double[pointCount][];

        Point2D anchor = line.getP1();
        Point2D endpoint = line.getP2();

        double x0 = anchor.getX();
        double y0 = anchor.getY();

        double x1 = endpoint.getX();
        double y1 = endpoint.getY();

        double dx = x1 - x0;
        double dy = y1 - y0;

        double segmentCount = pointCount - 1;

        for (int i = 0; i < pointCount; i++) 
        {
            double x = x0 + dx*i/segmentCount;
            double y = y0 + dy*i/segmentCount;
            profilePoints[i] = new double[] {x, y};
        }

        return profilePoints;
    }

    public static double[][] getProfilePoints(Shape profile, int pointCount)
    {
        double[][] profilePoints = new double[pointCount][3];

        double length = getLength(profile, 0);


        double segmentCount = pointCount - 1;
        double segmentLength = length/segmentCount;


        for (int i = 0; i < pointCount; i++) 
        {
            double d = i*segmentLength;
            DirectedPosition pos = getDirectedPosition(profile, d);
            Point2D pt = pos.getPoint();

            profilePoints[i] = new double[] {pt.getX(), pt.getY()};
        }
        return profilePoints;
    }

    public static class DirectedPosition
    {
        private final Line2D segment;
        private final Point2D point;

        public DirectedPosition(Line2D segment, Point2D point)
        {
            this.segment = segment;
            this.point = point;
        }

        public double getAngle()
        {
            return getAngleInRadians(segment);
        }

        public Point2D getPoint()
        {
            return point;
        }

        public Line2D getSegment()
        {
            return segment;
        }

        public DirectedPosition transform(AffineTransform tr)
        {
            Point2D pointNew = tr.transform(point, null);
            Line2D segmentNew = new Line2D.Double(tr.transform(segment.getP1(), null), tr.transform(segment.getP2(), null));

            return new DirectedPosition(segmentNew, pointNew);
        }
    }
}
