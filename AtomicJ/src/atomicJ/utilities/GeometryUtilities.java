package atomicJ.utilities;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jfree.data.Range;

import atomicJ.algorithms.GrahamScan;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.PathSegment;

public class GeometryUtilities {

    public static Rectangle2D getBoundingRectangle(double[][] points)
    {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for(double[] p : points)
        {
            double x = p[0];
            double y = p[1];

            if(x < minX)
            {
                minX = x;
            }
            else if(x > maxX)
            {
                maxX = x;
            }
            if(y < minY)
            {
                minY = y;
            }
            else if(y > maxY)
            {
                maxY = y;
            }
        }

        double width = maxX - minX;
        double height = maxY - minY;

        Rectangle2D rectangle = new Rectangle2D.Double(minX, minY, width, height);

        return rectangle;
    }

    public static Rectangle2D getBoundingRectangle(double[] xs, double[] ys)
    {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for(double y : ys)
        {
            if(y < minY)
            {
                minY = y;
            }
            else if(y > maxY)
            {
                maxY = y;
            }
        }

        for(double x : xs)
        {
            if(x < minX)
            {
                minX = x;
            }
            else if(x > maxX)
            {
                maxX = x;
            }
        }

        double width = maxX - minX;
        double height = maxY - minY;

        Rectangle2D rectangle = new Rectangle2D.Double(minX, minY, width, height);

        return rectangle;
    }

    public static Rectangle2D getBoundingRectangle(List<double[][]> pointSeries)
    {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for(double[][] points :  pointSeries)
        {
            for(double[] p : points)
            {
                double x = p[0];
                double y = p[1];

                if(x < minX)
                {
                    minX = x;
                }
                else if(x > maxX)
                {
                    maxX = x;
                }
                if(y < minY)
                {
                    minY = y;
                }
                else if(y > maxY)
                {
                    maxY = y;
                }
            }
        }

        double width = maxX - minX;
        double height = maxY - minY;

        Rectangle2D rectangle = new Rectangle2D.Double(minX, minY, width, height);

        return rectangle;
    }

    public static boolean equalNaNPermissive(Point2D p1, Point2D p2)
    {
        Double x1 = Double.valueOf(p1.getX());
        Double y1 = Double.valueOf(p1.getY());

        Double x2 = Double.valueOf(p2.getX());
        Double y2 = Double.valueOf(p2.getY());

        boolean xsEquals = x1.equals(x2);
        boolean ysEquals = y1.equals(y2);

        boolean equals = xsEquals && ysEquals;

        return equals;
    }

    public static boolean almostEqual(double val1, double val2, double tolerance)
    {
        if(Double.isNaN(val1) && Double.isNaN(val2))
        {
            return true;
        }

        double diff = Math.abs(val1 - val2);
        boolean almostEqual = diff < tolerance;

        return almostEqual;
    }

    public static boolean almostEqual(UnitExpression expr1, UnitExpression expr2, double tolerance)
    {
        UnitExpression expr2SameUnit = expr2.derive(expr1.getUnit());

        double val1 = expr1.getValue();
        double val2 = expr2SameUnit.getValue();

        return almostEqual(val1, val2, tolerance);
    }

    public static List<double[]> extractCoordinates(List<Point2D> points)
    {
        int n = points.size();
        List<double[]> coordinates = new ArrayList<>();

        for(int i = 0; i<n; i++)
        {
            Point2D p = points.get(i);
            coordinates.add(new double[] {p.getX(), p.getY()});
        }

        return coordinates;
    }

    public static boolean isWellFormedPoint(Point2D pt)
    {
        boolean wellFormed = (pt != null && !Double.isNaN(pt.getX()) && !Double.isNaN(pt.getY()));
        return wellFormed;
    }

    public static String getCoordinateString(Point2D pt, NumberFormat format)
    {
        String string = "(" + format.format(pt.getX()) + ", " + format.format(pt.getY()) + ")";

        return string;
    }

    public static Shape getSingleShape(Collection<? extends Shape> shapes)
    {
        if(shapes.size() == 1)
        {
            return shapes.iterator().next();
        }

        Area area = new Area();
        for(Shape shape : shapes)
        {
            area.add(new Area(shape));
        }

        return area;
    }

    public static boolean overlap(Collection<? extends Shape> shapes)
    {
        List<Shape> shapeList = new ArrayList<>(shapes);
        int n = shapes.size();

        for(int i = 0; i<n - 1; i++)
        {
            Shape currentShape = shapeList.get(i);

            for(int j = i + 1; j<n; j++)
            {
                boolean intersect = isShapeAIntersectingB(currentShape, shapeList.get(j));
                if(intersect)
                {
                    return true;
                }
            }
        }

        return false;
    }


    public static Set<Pair<Shape>> getOverlapingPairs(Collection<? extends Shape> shapes)
    {
        List<Shape> shapeList = new ArrayList<>(shapes);
        int n = shapes.size();

        Set<Pair<Shape>> overlappingPairs = new LinkedHashSet<>();

        for(int i = 0; i<n - 1; i++)
        {
            Shape firstShape = shapeList.get(i);

            for(int j = i + 1; j<n; j++)
            {
                Shape secondShape = shapeList.get(j);
                boolean intersect = isShapeAIntersectingB(firstShape, secondShape);
                if(intersect)
                {
                    overlappingPairs.add(new UnOrderedPair<>(firstShape, secondShape));
                }
            }
        }

        return overlappingPairs;
    }

    public static double getLength(Line2D line)
    {
        double x1 = line.getX1();
        double x2 = line.getX2();
        double dx = x2 - x1;

        double y1 = line.getY1();
        double y2 = line.getY2();
        double dy = y2 - y1;

        double length = Math.sqrt(dx*dx + dy*dy);
        return length;
    }

    public static Point2D interpolate(Line2D line, double t)
    {
        double x1 = line.getX1();
        double y1 = line.getY1();

        double vx = line.getX2() - x1;
        double vy = line.getY2() - y1;

        Point2D centralPoint = new Point2D.Double(x1 + t*vx, y1 + t*vy);

        return centralPoint;
    }

    public static double getDistance(Line2D line, Point2D pt)
    {
        return getDistance(line.getP1(), line.getP2(), pt);
    }

    public static double getDistance(Point2D p1, Point2D p2, Point2D pt)
    {
        double x1 = p1.getX();
        double x2 = p2.getX();
        double y1 = p1.getY();
        double y2 = p2.getY();
        double x3 = pt.getX();
        double y3 = pt.getY();

        double dx = x2 - x1;
        double dy = y2 - y1;

        double length2 = dx*dx + dy*dy;

        if(length2 == 0)
        {
            return getDistance(x1, y1, x3, y3);
        }

        double r = ((y1-y3)*(y1-y2)-(x1-x3)*(x2-x1))/length2;

        if(r > 1)
        {
            return getDistance(x2,y2,x3,x3);
        }
        if(r < 0)
        {
            return getDistance(x1,y1,x3,y3);
        }

        return getDistance(x1 + r*dx, y1+r*dy, x3, y3);
    }

    public static double getDistance(Point2D p1, Point2D p2)
    {
        double x1 = p1.getX();
        double x2 = p2.getX();
        double dx = x2 - x1;

        double y1 = p1.getY();
        double y2 = p2.getY();
        double dy = y2 - y1;

        double length = Math.sqrt(dx*dx + dy*dy);
        return length;
    }

    public static double getDistance(double x1, double y1, double x2, double y2)
    {
        double dx = x2 - x1;
        double dy = y2 - y1;

        double length = Math.sqrt(dx*dx + dy*dy);
        return length;
    }

    public static boolean isShapeAIntersectingB(Shape shapeA, Shape shapeB)
    {
        Area areaA = new Area(shapeA);
        areaA.intersect(new Area(shapeB));
        return !areaA.isEmpty();
    }

    public static boolean isShapeAContainedInB(Shape shapeA, Shape shapeB)
    {
        Area areaA = new Area(shapeA);
        areaA.subtract(new Area(shapeB));

        return areaA.isEmpty();
    }

    //from inclusive, to exclusive
    public static double[] translate(double[] data, int from, int to, double dy)
    {
        int n = data.length;

        double[] transformed = new double[n];

        System.arraycopy(data, 0, transformed, 0, Math.max(from, 0));

        for(int i = Math.max(0, from); i< Math.min(n, to); i++)
        {
            transformed[i] = data[i] + dy;
        }

        System.arraycopy(data, Math.max(from, 0) + 1, transformed, Math.max(from, 0) + 1, n - Math.min(n, to));

        return transformed;
    }


    public static double[][] translatePointsX(double[][] points, double x)
    {
        int n = points.length;
        double[][] translated = new double[n][];

        for(int i = 0; i<n;i++)
        {
            double[] p = points[i];
            translated[i] = new double[] {p[0] + x, p[1]};
        }

        return translated;
    }

    public static double[][] translatePointsY(double[][] points, double y)
    {
        return translatePointsY(points, 0, points.length, y);
    }

    //from inclusive, to exclusive
    public static double[][] translatePointsY(double[][] points, int from, int to, double y)
    {
        int n = points.length;
        double[][] translated = new double[n][];

        System.arraycopy(points, 0, translated, 0, Math.max(from, 0));

        for(int i = Math.max(from, 0); i<Math.min(n, to); i++)
        {
            double[] p = points[i];
            translated[i] = new double[] {p[0], p[1] + y};
        }

        System.arraycopy(points, Math.max(from, 0) + 1, translated, Math.max(from, 0) + 1, n - Math.min(n, to));

        return translated;
    }

    public static double[][] translatePointsXY(double[][] points, double x, double y)
    {
        int n = points.length;
        double[][] translated = new double[n][];

        for(int i = 0; i<n;i++)
        {
            double[] p = points[i];
            translated[i] = new double[] {p[0] + x, p[1] + y};
        }

        return translated;
    }

    //this method is designed for list of points 
    public static Range getBoundedXRange(List<Point2D> points)
    {
        if(points.isEmpty())
        {
            return new Range(0, 0);
        }

        int n = points.size();

        double firstX = points.get(0).getX();
        double min = firstX;
        double max = firstX;

        for(int i = 1;i<n;i++)
        {
            double x = points.get(i).getX();
            if(!Double.isInfinite(x) && !Double.isNaN(x))
            {
                if(x>max)
                {
                    max = x;
                }
                else if(x<min)
                {
                    min = x;
                }
            }         
        }

        Range range = new Range(min, max);      
        return range;
    }

    //this method is designed for list of points 
    public static Range getBoundedYRange(List<Point2D> points)
    {
        int n = points.size();

        if(points.isEmpty())
        {
            return new Range(0, 0);
        }

        double firstY = points.get(0).getY();
        double min = firstY;
        double max = firstY;

        for(int i = 1;i<n;i++)
        {
            double y = points.get(i).getY();
            if(!Double.isInfinite(y) && !Double.isNaN(y))
            {
                if(y>max)
                {
                    max = y;
                }
                else if(y<min)
                {
                    min = y;
                }
            }         
        }

        Range range = new Range(min, max);      
        return range;
    }

    public static double[] getDiameters(PathIterator i)
    {
        double[][] points = getPolygonVertices(i);

        double[][] convexHull = getConvexHull(points);
        double[] diameters = getDiameters(convexHull);

        return diameters;
    }

    public static double[][] getConvexHull(double[][] points)
    {
        int n = points.length;

        atomicJ.algorithms.Point2D[] points2D = new atomicJ.algorithms.Point2D[n];

        for(int k = 0; k<n;k++)
        {
            points2D[k] = new atomicJ.algorithms.Point2D(points[k][0], points[k][1]);
        }

        double[][] convexHull = new GrahamScan(points2D).hull();

        return convexHull;
    }

    public static Path2D convertToOpenPath(double[][] points)
    {
        Path2D path = new GeneralPath();

        if(points == null || points.length == 0)
        {
            return path;
        }

        path.moveTo(points[0][0], points[0][1]);

        for(double[] p : points)
        {
            path.lineTo(p[0], p[1]);
        }

        return path;
    }

    public static Path2D convertToClosedPath(double[][] points)
    {
        Path2D path = new GeneralPath();

        if(points == null || points.length == 0)
        {
            return path;
        }

        path.moveTo(points[0][0], points[0][1]);

        for(double[] p : points)
        {
            path.lineTo(p[0], p[1]);
        }

        path.closePath();

        return path;
    }

    //polygon vertices should be in counterclockwise orientation
    //adapted from Shamos, Franco P. Preparata, Michael Ian (1985). Computational Geometry An Introduction. New York, NY: Springer New York. ISBN 978-1-4612-7010-2.

    private static double[] getDiameters(double[][] polygon)
    {        
        int n = polygon.length;

        if(n <= 1)
        {
            return new double [] {0,0};
        }

        double minDiameter = Double.POSITIVE_INFINITY;
        double maxDiameter = -1;

        int p = n - 1;
        int q = 0;

        while(area(polygon[p],polygon[(p + 1)%n],polygon[(q + 1)%n]) > area(polygon[p],polygon[(p + 1)%n], polygon[q]))
        {
            q = (q + 1)%n;
        }

        int q0 = q;
        double currentDistance;

        while(q != 0)
        {
            p = (p + 1)%n;

            currentDistance = getDistance(polygon[p][0], polygon[p][1], polygon[q][0], polygon[q][1]);


            minDiameter = Math.min(minDiameter, currentDistance);
            maxDiameter = Math.max(maxDiameter, currentDistance);

            while(area(polygon[p],polygon[(p + 1)%n],polygon[(q + 1)%n]) > area(polygon[p],polygon[(p + 1)%n],polygon[q]))
            {
                q = (q + 1)%n;
                if(p != q0 && q != 0)
                {
                    currentDistance = getDistance(polygon[p][0], polygon[p][1], polygon[q][0], polygon[q][1]);
                    minDiameter = Math.min(minDiameter, currentDistance);
                    maxDiameter = Math.max(maxDiameter, currentDistance);

                }
                else
                {
                    return new double[] {minDiameter, maxDiameter};
                }
            }
            if(area(polygon[p],polygon[(p+1)%n],polygon[(q+1)%n]) == area(polygon[p],polygon[(p+1)%n],polygon[q]))
            {
                if(p != q0 && q != (n - 1))
                {
                    currentDistance = getDistance(polygon[p][0], polygon[p][1], polygon[q][0], polygon[q][1]);
                    minDiameter = Math.min(minDiameter, currentDistance);
                    maxDiameter = Math.max(maxDiameter, currentDistance);
                }
            }
        }
        return new double[] {minDiameter, maxDiameter};
    }

    private static double area(double[] pt1, double[] pt2, double[] pt3)
    {
        double area = 0.5*(pt2[0]-pt1[0])*(pt3[1]-pt1[1])-0.5*(pt2[1]-pt1[1])*(pt3[0]-pt1[0]);
        return area;
    }

    //assumes that the iterator contains only linear segments
    public static double[][] getPolygonVertices(PathIterator it)
    {            
        List<double[]> points = new  ArrayList<>();

        double[] coords = new double[6];

        while (!it.isDone()) 
        {
            int segmentType = it.currentSegment(coords);
            double x = coords[0];
            double y = coords[1];

            if(segmentType != PathIterator.SEG_CLOSE)
            {
                points.add(new double[] {x,y});
            }

            it.next();
        }    

        return points.toArray(new double[][] {});
    }

    public static Path2D convertPolygonVerticesToPath(List<double[]> vertices)
    {
        Path2D path = new Path2D.Double();
        if(vertices.isEmpty())
        {
            return path;
        }

        double[] origin = vertices.get(0);
        path.moveTo(origin[0], origin[1]);

        int vertexCount = vertices.size();

        for(int i = 1; i<vertexCount;i++)
        {
            double[] vertex = vertices.get(i);
            path.lineTo(vertex[0],vertex[1]);
        }
        path.closePath();

        return path;
    }

    public static double[] getFeretDiametersForArbitrary(Shape shape)
    {
        Rectangle2D shapeBounds = shape.getBounds2D();
        double cx = shapeBounds.getCenterX();
        double cy = shapeBounds.getCenterY();

        AffineTransform at = new AffineTransform();
        at.translate(cx, cy);

        double feretMax = 0;
        double feretMin = Double.POSITIVE_INFINITY;

        for (int i=0; i<181; i++) 
        {
            at.rotate(Math.PI/180.0);
            Shape rotatedShape = new Area(at.createTransformedShape(shape));
            Rectangle2D rotatedShapeBounds = rotatedShape.getBounds2D();

            double max2 = Math.max(rotatedShapeBounds.getWidth(), rotatedShapeBounds.getHeight());
            feretMax = Math.max(feretMax, max2);

            double min2 = Math.min(rotatedShapeBounds.getWidth(), rotatedShapeBounds.getHeight());
            feretMin = Math.min(feretMin, min2);
        }

        double[] ferets = new double[2];
        ferets[0] = feretMin;
        ferets[1] = feretMax;

        return ferets;
    }

    public static Path2D removePoint(Path2D originalPath, int indexToRemove)
    {
        Path2D pathNew = new Path2D.Double();

        int pointsReadInSoFarCount = 0;
        boolean moveToJustRemoved = false;

        double[] originalCoordinates = new double[6];

        for (PathIterator pi = originalPath.getPathIterator(null); !pi.isDone(); pi.next())
        {            
            int segmentType = pi.currentSegment(originalCoordinates);
            PathSegment segment = PathSegment.instanceFor(segmentType);

            int currentSegmentPointCount = segment.getPointCount();
            int indexToRemoveRelative = (indexToRemove - pointsReadInSoFarCount);
            boolean pointToRemoveInCurrentSegment = indexToRemoveRelative >= 0 && indexToRemoveRelative < (currentSegmentPointCount);

            if(moveToJustRemoved)
            {
                List<PathSegment> splitSegments = segment.getSegmentsAfterPrecedingMoveToRemoval();

                int pos = 0;
                for(PathSegment sg : splitSegments)
                {
                    double[] coords = new double[6];
                    System.arraycopy(originalCoordinates, pos, coords, 0, 2*sg.getPointCount());
                    pos += sg.getPointCount();

                    sg.addSegment(pathNew, coords);
                }    

                moveToJustRemoved = false;
            }
            else if(pointToRemoveInCurrentSegment)
            {             
                PathSegment segmentAfterRemoval = segment.getSegmentAfterPointRemoval();
                if(segmentAfterRemoval != null)
                {
                    double[] doubleCoordinatesModified = new double[6];
                    System.arraycopy(originalCoordinates, 0, doubleCoordinatesModified, 0, 2*indexToRemoveRelative);
                    System.arraycopy(originalCoordinates, 2*(indexToRemoveRelative + 1), doubleCoordinatesModified, 2*indexToRemoveRelative, 6 - 1 - indexToRemoveRelative);

                    segmentAfterRemoval.addSegment(pathNew, doubleCoordinatesModified);
                }

                moveToJustRemoved = PathSegment.MOVETO.equals(segment);
            }
            else
            {                    
                segment.addSegment(pathNew, originalCoordinates);
                moveToJustRemoved = false;
            }

            pointsReadInSoFarCount += currentSegmentPointCount;          
        }

        return pathNew;
    }    

    //currently works only if the point to be split in two is part of a linear segment
    public static Path2D splitPoint(Path2D originalPath, int indexToRemove, boolean pathClosed)
    {
        Path2D pathNew = new Path2D.Double();

        int pointsReadInSoFarCount = 0;

        boolean lastMoveSegmentSplitShouldBeCompletedUponClosing = false;        
        double[] lastMoveSegmentOriginalCoords = null;

        PathSegment precedingSegmentType = null;
        double[] precedingSegmentOriginalCoords = null;

        boolean nextSplitSegmentToBeAdded = false;
        PathSegment nextSplitSegmentType = null;

        for (PathIterator pi = originalPath.getPathIterator(null); !pi.isDone(); pi.next())
        {            
            double[] currentOriginalCoordinates = new double[6];
            int segmentType = pi.currentSegment(currentOriginalCoordinates);
            PathSegment segment = PathSegment.instanceFor(segmentType);

            int currentSegmentPointCount = segment.getPointCount();
            int indexToSplitRelative = (indexToRemove - pointsReadInSoFarCount);
            boolean pointToSplitIsInCurrentSegment = indexToSplitRelative >= 0 && indexToSplitRelative < currentSegmentPointCount;

            //this need to be first code block, because moveTo segment will be added here if the preceding, original moveTo segmet was split
            //(moveTo must be the first segment added to the path)
            if(nextSplitSegmentToBeAdded)
            {
                double[] coordsNew = new double[6];

                double otherX = PathSegment.CLOSE.equals(segment) ? lastMoveSegmentOriginalCoords[0] : currentOriginalCoordinates[0];
                double otherY = PathSegment.CLOSE.equals(segment) ? lastMoveSegmentOriginalCoords[1] : currentOriginalCoordinates[1];

                //preceding point is the clicked one
                coordsNew[0] = 2*precedingSegmentOriginalCoords[2*(precedingSegmentType.getPointCount() - 1)]/3. + otherX/3.;
                coordsNew[1] = 2*precedingSegmentOriginalCoords[2*(precedingSegmentType.getPointCount() - 1) + 1]/3. +otherY/3.;

                //precedingSegmentType was either line or moveTo
                nextSplitSegmentType.addSegment(pathNew, coordsNew); 
                nextSplitSegmentToBeAdded = false;
            }   

            if(PathSegment.CLOSE.equals(segment) && lastMoveSegmentSplitShouldBeCompletedUponClosing && lastMoveSegmentOriginalCoords != null)
            {
                double[] coordsNew = new double[6];

                //point of the moveto segment was clicked, so its coordinates get greater weight than those of the neighboring, current point
                coordsNew[0] = 2*lastMoveSegmentOriginalCoords[0]/3. + precedingSegmentOriginalCoords[0]/3.;
                coordsNew[1] = 2*lastMoveSegmentOriginalCoords[1]/3. + precedingSegmentOriginalCoords[1]/3.;

                PathSegment.LINETO.addSegment(pathNew, coordsNew);
                lastMoveSegmentSplitShouldBeCompletedUponClosing = false;
            }

            if(pointToSplitIsInCurrentSegment)
            {             
                if(PathSegment.LINETO.equals(segment) && precedingSegmentOriginalCoords != null)
                {
                    double[] coordsNew = new double[6];
                    coordsNew[0] = precedingSegmentOriginalCoords[2*(precedingSegmentType.getPointCount() - 1)]/3. + 2*currentOriginalCoordinates[0]/3.;
                    coordsNew[1] = precedingSegmentOriginalCoords[2*(precedingSegmentType.getPointCount() - 1) + 1]/3. + 2*currentOriginalCoordinates[1]/3.;

                    segment.addSegment(pathNew, coordsNew);
                    nextSplitSegmentToBeAdded = true;
                    nextSplitSegmentType = PathSegment.LINETO;
                }      
                else if(PathSegment.MOVETO.equals(segment))
                {
                    if(pathClosed)
                    {
                        lastMoveSegmentSplitShouldBeCompletedUponClosing = true;
                        nextSplitSegmentType = PathSegment.MOVETO;
                        nextSplitSegmentToBeAdded = true;
                    }
                    else
                    {
                        segment.addSegment(pathNew, currentOriginalCoordinates);
                        nextSplitSegmentType = PathSegment.LINETO;
                        nextSplitSegmentToBeAdded = true;
                    }
                }
                else
                {
                    nextSplitSegmentToBeAdded = false;
                    segment.addSegment(pathNew, currentOriginalCoordinates);
                }
            }
            else
            {        
                if(PathSegment.MOVETO.equals(segment))
                {
                    lastMoveSegmentSplitShouldBeCompletedUponClosing = false;
                }
                nextSplitSegmentToBeAdded = false;
                segment.addSegment(pathNew, currentOriginalCoordinates);
            }

            pointsReadInSoFarCount += currentSegmentPointCount;  
            precedingSegmentType = segment;

            lastMoveSegmentOriginalCoords = PathSegment.MOVETO.equals(segment) ? currentOriginalCoordinates : lastMoveSegmentOriginalCoords;
            precedingSegmentOriginalCoords = !PathSegment.CLOSE.equals(segment) ? currentOriginalCoordinates : lastMoveSegmentOriginalCoords;
        }

        //the  nextSplitSegmentToBeAdded is true after the loop if the last point was to be split in an unclosed path
        if(nextSplitSegmentToBeAdded)
        {
            precedingSegmentType.addSegment(pathNew, precedingSegmentOriginalCoords); 
        }   

        return pathNew;
    }  

    //  test if a point is Left|On|Right of an infinite 2D line.
    //  Input:  three points P0, P1, and P2
    //  Return: >0 for P2 left of the line through P0 to P1
    //        =0 for P2 on the line
    //        <0 for P2 right of the line
    public static double isLeft(Point2D P0, Point2D P1, Point2D P2 )
    {
        return ( (P1.getX() - P0.getX()) * (P2.getY() - P0.getY()) - (P2.getX() - P0.getX()) * (P1.getY() - P0.getY()) );
    }

    public static Shape createTranslatedShapeForPlotting(Shape shape, double transX, double transY)
    {
        if(shape instanceof Ellipse2D.Float)
        {
            Ellipse2D.Float elipse = (Ellipse2D.Float)shape;
            float xTrans = (float) (elipse.getX() + transX);
            float yTrans = (float) (elipse.getY() + transY);
            Ellipse2D.Float elipseTranslated = new Ellipse2D.Float(xTrans, yTrans, (float)elipse.getWidth(), (float)elipse.getHeight());

            return elipseTranslated;
        }

        AffineTransform transform = AffineTransform.getTranslateInstance(transX, transY);
        return transform.createTransformedShape(shape);
    }

    public static boolean isPointInRect(Rectangle2D rect, double x, double y)
    {
        return (x >= rect.getMinX() && x <= rect.getMaxX()
                && y >= rect.getMinY() && y <= rect.getMaxY());
    }
}
