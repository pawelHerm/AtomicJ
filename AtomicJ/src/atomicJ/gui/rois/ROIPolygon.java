
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

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

import atomicJ.data.ArraySupport2D;
import atomicJ.data.Grid2D;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.ModifierKey;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.PathSegment;
import atomicJ.gui.PathShiftIterator;
import atomicJ.gui.ShapeFactors;
import atomicJ.gui.annotations.AnnotationAnchorCore;
import atomicJ.gui.annotations.AnnotationAnchorSigned;
import atomicJ.gui.annotations.AnnotationAnchorSourceSigned;
import atomicJ.gui.annotations.AnnotationHotSpotAnchor;
import atomicJ.gui.annotations.BasicAnnotationAnchor;
import atomicJ.gui.profile.AnnotationModificationOperation;
import atomicJ.gui.profile.ProfilePolyLine;
import atomicJ.gui.rois.Crossing2D.CrossingsHierarchy;
import atomicJ.gui.rois.region.Region;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.GeometryUtilities;

public class ROIPolygon extends ROIDrawable implements Cloneable, PublicCloneable 
{
    private static final long serialVersionUID = 1L;

    private Path2D roiShape;
    private Path2D fixedPartOfShape;
    private Point2D lastFixedPoint;

    private int segmentCount = 0;

    private final float hotSpotRadius = 4.f;
    private List<Shape> reshapeHotSpots = new ArrayList<>();

    public ROIPolygon(Path2D shape, Integer key, ROIStyle style) 
    {
        super(key, style);

        this.roiShape = new Path2D.Double(shape);
        this.fixedPartOfShape = new Path2D.Double(shape);     
        this.lastFixedPoint = DistanceShapeFactors.getLastPoint(shape);
    }

    public ROIPolygon(Path2D shape, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        this.roiShape = new Path2D.Double(shape);
        this.fixedPartOfShape = new Path2D.Double(shape);
        this.lastFixedPoint = DistanceShapeFactors.getLastPoint(shape);
    }

    public ROIPolygon(ROIPolygon that)
    {
        this(that, that.getStyle());
    }

    public ROIPolygon(ROIPolygon that, ROIStyle style)
    {
        super(that, style);

        this.roiShape = new Path2D.Double(that.roiShape);
        this.fixedPartOfShape = new Path2D.Double(that.fixedPartOfShape); 
        this.lastFixedPoint = new Point2D.Double(that.lastFixedPoint.getX(), that.lastFixedPoint.getY());
    }

    public ROIPolygon(ROIPolygon that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);

        this.roiShape = new Path2D.Double(that.roiShape);
        this.fixedPartOfShape = new Path2D.Double(that.fixedPartOfShape);
        this.lastFixedPoint = new Point2D.Double(that.lastFixedPoint.getX(), that.lastFixedPoint.getY());
    }

    public ROIPolygon(ROIDrawable otherROI)
    {
        super(otherROI.getKey(), otherROI.getLabel(), otherROI.getStyle());

        Path2D shape = new Path2D.Double(otherROI.getROIShape());

        this.roiShape = shape;
        this.fixedPartOfShape = new Path2D.Double(shape);   
        this.lastFixedPoint = DistanceShapeFactors.getLastPoint(shape);
    }

    private ROIPolygon(Shape roiShape, Shape fixedPartOfShape, List<Shape> reshapeHotSpots, ROIStyle style, Integer key, String label)
    {
        super(key, label, style);

        this.roiShape = new Path2D.Double(roiShape);
        this.fixedPartOfShape = new Path2D.Double(fixedPartOfShape);   
        this.reshapeHotSpots.addAll(reshapeHotSpots);

        this.lastFixedPoint = DistanceShapeFactors.getLastPoint(roiShape);
    }

    @Override
    public ROIPolygon getRotatedCopy(double angleInRadians, double anchorX, double anchorY)
    {
        AffineTransform rotationTransform = AffineTransform.getRotateInstance(angleInRadians, anchorX, anchorY);

        Shape rotatedROIShape = rotationTransform.createTransformedShape(this.roiShape);
        Shape fixedPartOfShape = rotationTransform.createTransformedShape(this.fixedPartOfShape);

        List<Shape> rotatedReshapeHotSpots = new ArrayList<>();

        for(Shape hotSpot : reshapeHotSpots)
        {
            rotatedReshapeHotSpots.add(rotationTransform.createTransformedShape(hotSpot));
        }

        return new ROIPolygon(rotatedROIShape, fixedPartOfShape, rotatedReshapeHotSpots, getStyle(), getKey(), getLabel());
    }

    public int getCaughtReshapeSpot(Point2D java2DPoint)
    {
        int index = -1;

        int n = reshapeHotSpots.size();

        for(int i = 0; i<n; i++)
        {
            Shape hotspot = reshapeHotSpots.get(i);

            if(hotspot.contains(java2DPoint))
            {
                index = i;
                break;
            }
        }

        return index;
    }

    @Override
    public boolean reshapeInResponseToMouseClick(Set<ModifierKey> modifierKeys, Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {     
        if(modifierKeys.contains(ModifierKey.CONTROL) && reshapeHotSpots.size() > 3)
        {           
            int caughtReshapeSpot = getCaughtReshapeSpot(java2DPoint);

            if(caughtReshapeSpot >= 0)
            {            
                this.roiShape = GeometryUtilities.removePoint(roiShape, caughtReshapeSpot);
                this.fixedPartOfShape = new Path2D.Double(roiShape); 

                return true;
            }
        }
        else if(modifierKeys.contains(ModifierKey.SHIFT))
        {
            int caughtReshapeSpot = getCaughtReshapeSpot(java2DPoint);

            if(caughtReshapeSpot >= 0)
            {            
                this.roiShape = GeometryUtilities.splitPoint(roiShape, caughtReshapeSpot, true);
                this.fixedPartOfShape = new Path2D.Double(roiShape); 

                return true;
            }
        }

        return false;
    }

    @Override
    public AnnotationAnchorSigned getCaughtAnchor(Point2D java2DPoint, Point2D dataPoint, Rectangle2D dataRectangle)
    {
        AnnotationAnchorCore coreAnchor = null;

        int caughtReshapeSpot = getCaughtReshapeSpot(java2DPoint);

        if(caughtReshapeSpot >= 0)
        {
            coreAnchor = new AnnotationHotSpotAnchor(caughtReshapeSpot);
        }
        else if(isLabelClicked(java2DPoint))
        {
            coreAnchor = BasicAnnotationAnchor.LABEL;
        }
        else if(isFinished() && getModifiableShape().intersects(dataRectangle))
        {
            coreAnchor = BasicAnnotationAnchor.CENTER;
        }

        AnnotationAnchorSigned anchor = (coreAnchor != null) ? new AnnotationAnchorSourceSigned(coreAnchor, getKey()) : null;
        return anchor;
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D rotationCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        if(BasicAnnotationAnchor.CENTER.equals(anchor.getCoreAnchor()))
        {            
            double centerX = rotationCenter.getX();
            double centerY = rotationCenter.getY();

            double angleInRadians = Math.atan2(endPoint.getY() - centerY, endPoint.getX() - centerX) - Math.atan2(startPoint.getY() - centerY, startPoint.getX() - centerX);

            AffineTransform rotationTransform = AffineTransform.getRotateInstance(angleInRadians, centerX, centerY);
            Point2D finalPosition = rotationTransform.transform(startPoint, null);

            this.roiShape = new Path2D.Double(rotationTransform.createTransformedShape(this.roiShape));
            this.fixedPartOfShape = new Path2D.Double(rotationTransform.createTransformedShape(this.roiShape));

            fireAnnotationChanged();

            AnnotationAnchorSigned newAnchor = (Objects.equals(getKey(), anchor.getKey())) ? new AnnotationAnchorSourceSigned(anchor.getCoreAnchor(), getKey()) : null;        
            AnnotationModificationOperation currentModificationOperation = new AnnotationModificationOperation(newAnchor, pressedPoint, finalPosition);

            return currentModificationOperation;
        }

        return setPosition(anchor, modifierKeys, pressedPoint, startPoint, endPoint);
    }

    @Override
    public AnnotationModificationOperation rotate(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D caughtROICenter, Point2D caughtROICompositeCenter, Point2D pressedPoint, Point2D startPoint, Point2D endPoint)
    {
        return rotate(anchor, modifierKeys, caughtROICenter, pressedPoint, startPoint, endPoint);
    }

    @Override
    public AnnotationModificationOperation setPosition(AnnotationAnchorSigned anchor, Set<ModifierKey> modifierKeys, Point2D pressedPoint, Point2D previousPosition, Point2D endPoint)
    {
        if(anchor == null)
        {
            return null;
        }	

        Point2D finalPosition = previousPosition;  

        AnnotationAnchorCore coreAnchor = anchor.getCoreAnchor();

        if(coreAnchor instanceof AnnotationHotSpotAnchor)
        {
            finalPosition = ProfilePolyLine.correctPointCoordinates(pressedPoint.getX(),  pressedPoint.getY(), endPoint.getX(), endPoint.getY(), (Math.PI/2.), modifierKeys);

            double tx = finalPosition.getX() - previousPosition.getX();
            double ty = finalPosition.getY() - previousPosition.getY();

            int index = ((AnnotationHotSpotAnchor)coreAnchor).getIndex();
            Path2D roiShapeOld = roiShape;

            this.roiShape = new Path2D.Double();
            this.roiShape.append(new PathShiftIterator(roiShapeOld.getPathIterator(null), index, tx, ty), true);
            this.fixedPartOfShape = new Path2D.Double(roiShape); 

            fireAnnotationChanged();
        }
        else if(BasicAnnotationAnchor.CENTER.equals(coreAnchor))
        {
            finalPosition = ProfilePolyLine.correctPointCoordinates(pressedPoint.getX(),  pressedPoint.getY(), endPoint.getX(), endPoint.getY(), (Math.PI/2.), modifierKeys);

            double tx = finalPosition.getX() - previousPosition.getX();
            double ty = finalPosition.getY() - previousPosition.getY();

            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(tx, ty);

            this.roiShape = new Path2D.Double(roiShape.createTransformedShape(transform));
            this.fixedPartOfShape = new Path2D.Double(roiShape); 

            fireAnnotationChanged();
        }

        AnnotationAnchorSigned returnedAnchor = Objects.equals(anchor.getKey(), getKey()) ? new AnnotationAnchorSourceSigned(anchor.getCoreAnchor(), getKey()) : null;
        Point2D endPointNew = Objects.equals(anchor.getKey(), getKey()) ? finalPosition : endPoint;
        AnnotationModificationOperation currentModificationOperation = new AnnotationModificationOperation(returnedAnchor, pressedPoint, endPointNew);

        return currentModificationOperation;
    }

    @Override
    public ROIPolygon copy()
    {
        return new ROIPolygon(this);
    }

    @Override
    public ROIPolygon copy(ROIStyle style)
    {
        return new ROIPolygon(this, style);
    }

    @Override
    public ROIPolygon copy(ROIStyle style, Integer key, String label)
    {
        return new ROIPolygon(this, style, key, label);
    }

    @Override
    public ROIDrawable getConvexHull(Integer key, String label) 
    {
        PathIterator it = getROIShape().getPathIterator(null, 0);
        double[][] points = GeometryUtilities.getPolygonVertices(it);
        double[][] hull = GeometryUtilities.getConvexHull(points);

        Path2D hullShape = GeometryUtilities.convertToClosedPath(hull);

        ROIPolygon roiHull = new ROIPolygon(hullShape, key, label, getStyle());
        roiHull.setFinished(isFinished());

        return roiHull;
    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        return segmentCount > 0;
    }

    public Point2D getLastFixedPoint()
    {
        return lastFixedPoint;
    }

    @Override
    public void mouseMovedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.lastFixedPoint.getX(),  this.lastFixedPoint.getY(), x, y, modifiers);
        lineTemporailyTo(endNew.getX(), endNew.getY(), true);
    }

    @Override
    public void mousePressedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        Point2D endNew = ProfilePolyLine.correctPointCoordinates(this.lastFixedPoint.getX(),  this.lastFixedPoint.getY(), x, y, modifiers);
        lineTo(endNew.getX(), endNew.getY(), true); 
    }

    public boolean respondToMouseRightClickedDuringConstruction(double x, double y, Set<ModifierKey> modifiers)
    {
        closePath();
        setFinished(true); 

        return isCorrectlyConstructed();
    }

    protected void moveTo(double x, double y, boolean notify)
    {
        roiShape.moveTo(x, y);	
        fixedPartOfShape.moveTo(x, y);
        this.lastFixedPoint = new Point2D.Double(x, y);

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    protected void lineTo(double x, double y, boolean notify)
    {
        fixedPartOfShape.lineTo(x, y);
        roiShape = new Path2D.Double(fixedPartOfShape);	
        segmentCount++;

        this.lastFixedPoint = new Point2D.Double(x, y);

        if(notify)
        {
            fireAnnotationChanged();
        }
    }

    protected void lineTemporailyTo(double x, double y, boolean notify)
    {
        roiShape = new Path2D.Double(fixedPartOfShape);
        roiShape.lineTo(x, y);

        if(notify)
        {
            fireAnnotationChanged();
        }
    }


    public void closePath()
    {
        closePath(true);
    }

    public void closePath(boolean notify)
    {
        roiShape.closePath();
        fixedPartOfShape.closePath();

        if(notify)
        {
            fireAnnotationChanged();
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
            ROIPolygon r = new ROIPolygon(path, -1, getStyle());
            splitROIs.add(r);
        }

        return splitROIs;
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness)
    {
        return ShapeFactors.getShapeFactorsForPolygon(getROIShape());
    }

    public Path2D getModifiableShape()
    {
        return roiShape;
    }

    protected Path2D getFixedPartOfModifiableShape()
    {
        return fixedPartOfShape;
    }

    @Override
    public Path2D getROIShape()
    {
        return roiShape;
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {
        this.reshapeHotSpots = new ArrayList<>();

        if(isVisible() && isHighlighted())
        {
            PathIterator iterator = roiShape.getPathIterator(null);

            PlotOrientation orientation = plot.getOrientation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

            while(!iterator.isDone())
            {
                float[] coords = new float[6];

                int segmentType = iterator.currentSegment(coords);
                PathSegment pathSegment =  PathSegment.instanceFor(segmentType);
                int pointCount =  pathSegment.getPointCount();

                boolean isVertical = (PlotOrientation.VERTICAL.equals(orientation));

                for(int i = 0; i<2*pointCount; i = i + 2)
                {
                    float j2DX = (float) domainAxis.valueToJava2D(coords[i], dataArea, domainEdge);
                    float j2DY = (float) rangeAxis.valueToJava2D(coords[i + 1], dataArea, rangeEdge);

                    Shape  startHotSpot = isVertical ? new Ellipse2D.Float(j2DX - hotSpotRadius, j2DY - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius)
                            : new Ellipse2D.Float(j2DY - hotSpotRadius, j2DX - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius);                   

                    g2.setPaint(getStrokePaint());
                    g2.fill(startHotSpot);      

                    reshapeHotSpots.add(startHotSpot);                    
                }

                iterator.next();              
            }
        }  
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info, Region excludedShape) 
    {
        this.reshapeHotSpots = new ArrayList<>();

        if(isVisible() && isHighlighted())
        {
            PathIterator iterator = roiShape.getPathIterator(null);

            PlotOrientation orientation = plot.getOrientation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

            while(!iterator.isDone())
            {
                float[] coords = new float[6];

                int segmentType = iterator.currentSegment(coords);
                PathSegment pathSegment = PathSegment.instanceFor(segmentType);

                boolean isVertical = (PlotOrientation.VERTICAL.equals(orientation));

                int pointCount = pathSegment.getPointCount();
                for(int i = 0; i<2*pointCount; i = i + 2)
                {
                    double x = coords[i];
                    double y = coords[i + 1];

                    if(!excludedShape.contains(x, y))
                    {
                        float j2DX = (float) domainAxis.valueToJava2D(x, dataArea, domainEdge);
                        float j2DY = (float) rangeAxis.valueToJava2D(y, dataArea, rangeEdge);

                        Shape  startHotSpot = isVertical ? new Ellipse2D.Float(j2DX - hotSpotRadius, j2DY - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius)
                                :new Ellipse2D.Float(j2DY - hotSpotRadius, j2DX - hotSpotRadius, 2*hotSpotRadius, 2*hotSpotRadius);                     

                        g2.setPaint(getStrokePaint());
                        g2.fill(startHotSpot);      

                        reshapeHotSpots.add(startHotSpot); 
                    }     
                    else
                    {
                        //adds empty shape - it is necessary to add empty shape to hot spots, because the i-th hot spot should always correspond to i-th point 
                        //in the path
                        reshapeHotSpots.add(new Ellipse2D.Float());
                    }
                }

                iterator.next();              
            }
        }  
    }


    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isROI())
        {
            return oldMode;
        }
        return MouseInputModeStandard.POLYGON_ROI;
    }

    @Override
    public int getPointsInsideCountUpperBound(ArraySupport2D grid)
    {
        return GridPositionCalculator.getInsidePointCountUpperBound(grid, getROIShape());
    }

    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient)
    {
        Path2D roiShape = getROIShape();
        addPointsInside(roiShape, grid, recepient);
    }

    public static void addPointsInside(Path2D roiShape, ArraySupport2D grid, GridPointRecepient recepient)
    {
        Rectangle2D bounds = roiShape.getBounds2D();
        int columnCount = grid.getColumnCount();

        double minY = bounds.getMinY();
        double maxY = bounds.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI bounds

        int minRow = Math.max(0, grid.getRowCeiling(minY));
        int maxRow = Math.min(grid.getRowCount() - 1, grid.getRowFloor(maxY));

        for(int i = minRow; i< maxRow + 1; i++)
        {
            double y = grid.getY(i);
            TDoubleList crossings = evaluateCrossingsWindingNonzero(roiShape, y);

            int n = crossings.size()/2;

            for(int j = 0; j<n; j++)
            {
                double min = crossings.get(2*j);
                double max = crossings.get(2*j + 1);

                int columnMin = Math.max(0, grid.getColumnCeilingWithinBounds(min));
                int columnMax = Math.min(columnCount - 1, grid.getColumnFloorWithinBounds(max));

                recepient.addBlock(i, i + 1, columnMin, columnMax + 1);
            }
        }
    }

    @Override
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient)
    {
        Path2D roiShape = getROIShape();
        addPointsOutside(roiShape, grid, recepient);
    }

    public static void addPointsOutside(Path2D roiShape, ArraySupport2D grid, GridPointRecepient recepient)
    {
        Rectangle2D bounds = roiShape.getBounds2D();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double minY = bounds.getMinY();
        double maxY = bounds.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI bounds

        int roiMinRow = Math.max(0, grid.getRowCeiling(minY));
        int roiMaxRow = Math.min(rowCount - 1, grid.getRowFloor(maxY));

        for(int i = roiMinRow; i <= roiMaxRow; i++)
        {
            double y = grid.getY(i);
            TDoubleList crossings = evaluateCrossingsWindingNonzero(roiShape, y);            
            int n = crossings.size()/2;

            int outsideLeft = 0;

            for(int j = 0; j<n; j++)
            {
                double min = crossings.get(2*j);
                double max = crossings.get(2*j + 1);

                int columnMin = Math.min(columnCount - 1, Math.max(0, grid.getColumnCeilingWithinBounds(min)));
                int columnMax = Math.max(0, Math.min(columnCount - 1, grid.getColumnFloorWithinBounds(max)));

                recepient.addBlock(i, i + 1, outsideLeft, columnMin);

                outsideLeft = columnMax + 1;
            }

            recepient.addBlock(i, i + 1, outsideLeft, columnCount);      
        }

        //adds points that are outside bounding box

        recepient.addBlock(0, Math.min(roiMinRow, rowCount), 0, columnCount);
        recepient.addBlock(Math.max(roiMaxRow + 1, 0), rowCount, 0, columnCount);
    }


    //imageMinRow, imageMaxRow, imageMinColumn and imageMaxColumn inclusive
    @Override
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {        
        Path2D roiShape = getROIShape();
        dividePoints(roiShape, grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
    }

    //imageMinRow, imageMaxRow, imageMinColumn and imageMaxColumn inclusive
    public static void dividePoints(Path2D roiShape, ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        Rectangle2D bounds = roiShape.getBounds2D();

        double minY = bounds.getMinY();
        double maxY = bounds.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI bounds

        int roiMinRow = Math.max(imageMinRow, grid.getRowCeiling(minY));
        int roiMaxRow = Math.min(imageMaxRow, grid.getRowFloor(maxY));

        for(int i = roiMinRow; i <= roiMaxRow; i++)
        {
            double y = grid.getY(i);
            TDoubleList crossings = evaluateCrossingsWindingNonzero(roiShape, y);            
            int n = crossings.size()/2;

            int outsideLeft = imageMinColumn;

            for(int j = 0; j<n; j++)
            {
                double min = crossings.get(2*j);
                double max = crossings.get(2*j + 1);

                int columnMin = Math.min(imageMaxColumn, Math.max(imageMinColumn, grid.getColumnCeilingWithinBounds(min))); //we need to make sure that min is not greater than imageMaxColumn, even if it may appear unnecessary, if - we didn't, then the second for loop can produce exception
                int columnMax = Math.max(imageMinColumn, Math.min(imageMaxColumn, grid.getColumnFloorWithinBounds(max)));

                for(int k = columnMin; k<= columnMax; k++)
                {
                    recepient.addPointInside(i, k);
                }    

                for(int k = outsideLeft; k< columnMin; k++)
                {
                    recepient.addPointOutside(i, k);
                } 

                outsideLeft = columnMax + 1;
            }

            for(int k = outsideLeft; k <= imageMaxColumn; k++)
            {
                recepient.addPointOutside(i, k);
            }              
        }

        //adds points that are outside bounding box

        for(int i = imageMinRow; i<Math.min(roiMinRow, imageMaxRow + 1); i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }

        for(int i = Math.max(roiMaxRow + 1, imageMinRow); i <= imageMaxRow; i++)
        {
            for(int j = imageMinColumn; j <= imageMaxColumn; j++)
            {
                recepient.addPointOutside(i, j);
            }           
        }
    }  

    public static void addEveryPoint(Path2D path, Grid2D grid, GridPointRecepient recepient)
    {
        recepient.addBlock(0, grid.getRowCount(), 0, grid.getColumnCount());
    }

    public static void addPoints(Path2D path, Grid2D grid, ROIRelativePosition position, GridPointRecepient recepient)
    {
        if(ROIRelativePosition.INSIDE.equals(position))
        {
            addPointsInside(path,grid, recepient);
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            addPointsOutside(path, grid, recepient);
        }
        else if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            addEveryPoint(path, grid, recepient);
        }
    }

    private static TDoubleList evaluateCrossingsWindingNonzero(Shape path, double y)
    {
        SortedSet<Crossing> cr = buildCrossingsWindingNonzero(path, y);
        return Crossing.resolveInsidness(cr);
    }

    private static SortedSet<Crossing> buildCrossingsWindingNonzero(Shape path, double y)
    {

        //it has too be SortedSet, sorting a list is not enough
        //

        SortedSet<Crossing> cr = new TreeSet<>();

        double[] coords = new double[6];

        double cx = 0;
        double cy = 0;

        double startX = 0;
        double startY = 0;

        for(PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next())
        {
            int type = iterator.currentSegment(coords);

            if(PathIterator.SEG_LINETO == type)
            {
                double y1 = cy;               
                double y2 = coords[1];

                double x1 = cx;
                double x2 = coords[0];

                boolean outside = (y > y1 && y > y2) || (y < y1 && y < y2);

                if(!outside)
                {               
                    boolean upwards = y2 > y1; 

                    double dx = x2 - x1;
                    double dy = y2 - y1;

                    double xCrossing = x1 + (dx/dy)*(y - y1);

                    //we do not want autoboxing
                    // because it will call Double.valueOf()
                    //this reduce memory usage, but performance is worse
                    //memory usage is not priority here, as there area usually few crossings
                    cr.add(new Crossing(xCrossing, upwards));
                }

                cx = x2;
                cy = y2;
            }
            else if(PathIterator.SEG_MOVETO == type)
            {
                cx = coords[0];
                cy = coords[1];

                startX = cx;
                startY = cy;
            }
            else if(PathIterator.SEG_CLOSE == type)
            {
                double y1 = cy;               
                double y2 = startY;

                double x1 = cx;
                double x2 = startX;

                boolean outside = (y > y1 && y > y2) || (y < y1 && y < y2);

                if(!outside)
                {      
                    boolean upwards = y2 > y1; 

                    double dx = x2 - x1;
                    double dy = y2 - y1;

                    double a = dy/dx;
                    double b = y2 - a*x2;

                    double xCrossing = (y - b)/a;

                    //we do not want autoboxing
                    // because it will call Double.valueOf()
                    //this reduce memory usage, but performance is worse
                    //memory usage is not priority here, as there area usually few crossings
                    cr.add(new Crossing(xCrossing, upwards));
                }

                cx = x2;
                cy = y2;
            }
        }

        return cr;
    }

    private static TDoubleList evaluateCrossingsEvenOdd(Shape path, double y)
    {
        TDoubleList crossings = new TDoubleArrayList();

        double[] coords = new double[6];

        double cx = 0;
        double cy = 0;

        double startX = 0;
        double startY = 0;

        for(PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next())
        {
            int type = iterator.currentSegment(coords);

            if(PathIterator.SEG_LINETO == type)
            {
                double y1 = cy;               
                double y2 = coords[1];

                double x1 = cx;
                double x2 = coords[0];

                boolean outside = (y > y1 && y > y2) || (y < y1 && y < y2);

                if(!outside)
                {                   
                    double dx = x2 - x1;
                    double dy = y2 - y1;

                    double xCrossing = x1 + (dx/dy)*(y - y1);

                    //we do not want autoboxing
                    // because it will call Double.valueOf()
                    //this reduce memory usage, but performance is worse
                    //memory usage is not priority here, as there area usually few crossings
                    crossings.add(xCrossing);
                }

                cx = x2;
                cy = y2;
            }
            else if(PathIterator.SEG_MOVETO == type)
            {
                cx = coords[0];
                cy = coords[1];

                startX = cx;
                startY = cy;
            }
            else if(PathIterator.SEG_CLOSE == type)
            {
                double y1 = cy;               
                double y2 = startY;

                double x1 = cx;
                double x2 = startX;

                boolean outside = (y > y1 && y > y2) || (y < y1 && y < y2);

                if(!outside)
                {                   
                    double dx = x2 - x1;
                    double dy = y2 - y1;

                    double a = dy/dx;
                    double b = y2 - a*x2;

                    double xCrossing = (y - b)/a;

                    //we do not want autoboxing
                    // because it will call Double.valueOf()
                    //this reduce memory usage, but performance is worse
                    //memory usage is not priority here, as there area usually few crossings
                    //comment no longer relevant since we are now using Trove collections for primitives
                    crossings.add(xCrossing);
                }

                cx = x2;
                cy = y2;
            }
        }

        crossings.sort();

        return crossings;
    }



    @Override
    public ROIProxy getProxy()
    {
        return new ROIPathProxy(this.fixedPartOfShape, getCustomLabel(), isFinished());
    }

    public static class ROIPathProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D roiShape;
        private final String customLabel;
        private final boolean finished;

        public ROIPathProxy(Path2D roiShape, String customLabel, boolean finished)
        {
            this.roiShape = roiShape;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIPolygon recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            ROIPolygon roi = (customLabel != null) ? new ROIPolygon(roiShape, key, customLabel, roiStyle) : new ROIPolygon(roiShape, key, roiStyle);
            roi.setFinished(finished);

            return roi;
        }     
    }

    //returns null if the shape was not split
    public static List<Path2D> evaluateCrosssectioning(Shape path, double sx0, double sy0, double sx1, double sy1)
    {
        PolygonCrossectionedBoundary segments = new PolygonCrossectionedBoundary();
        BoundarySegment currentSegment = null;

        List<double[]> initialVertices = new ArrayList<>();

        double[] coords = new double[6];

        double cx = 0;
        double cy = 0;

        double startX = 0;
        double startY = 0;

        double sx = sx1 - sx0;
        double sy = sy1 - sy0;

        //we use PathIterator from the Area object to convert self-intersecting polygons into simple ones

        for(PathIterator iterator = path.getPathIterator(null, 0.); !iterator.isDone(); iterator.next())
        {
            int type = iterator.currentSegment(coords);

            if(PathIterator.SEG_MOVETO == type)
            {
                cx = coords[0];
                cy = coords[1];

                startX = cx;
                startY = cy;

                continue;
            }
            else if(PathIterator.SEG_LINETO == type)
            {
                double y1 = cy;               
                double y2 = coords[1];

                double x1 = cx;
                double x2 = coords[0];

                double edgeStartPosition = isLeft(sx0, sy0, sx1, sy1, x1, y1);
                double edgeEndPosition = isLeft(sx0, sy0, sx1, sy1, x2, y2);

                boolean forSureNotCrossing = (edgeStartPosition > 0 && edgeEndPosition > 0) || (edgeStartPosition < 0 && edgeEndPosition < 0);


                if(!forSureNotCrossing)
                {               
                    boolean rightToLeft = edgeStartPosition < 0 || (edgeStartPosition == 0 && edgeEndPosition > 0); 

                    double dx = x2 - x1;
                    double dy = y2 - y1;

                    double t = (dy*(x1 - sx0) + dx*(sy0 - y1))/(dy*sx - dx*sy);
                    if(t>=0 && t <= 1)
                    {
                        double xCrossing = sx0 + t*sx;
                        double yCrossing = sy0 + t*sy;
                        double r = (dx != 0) ? (xCrossing - x1)/dx :  (yCrossing - y1)/dy;

                        segments.addSegment(currentSegment);                 

                        Crossing2D currentCrossing = new Crossing2D(r, t, 0, 0, xCrossing, yCrossing, rightToLeft);      
                        segments.setFinalCrossingForLastAddedSegment(currentCrossing);

                        currentSegment = new BoundarySegment(currentCrossing);
                    }
                }

                if(currentSegment != null)
                {
                    currentSegment.addVertex(new double[] {x2, y2});
                }
                else
                {
                    initialVertices.add(new double[] {x2, y2});
                }

                cx = x2;
                cy = y2;
            }          
            else if(PathIterator.SEG_CLOSE == type)
            {
                double y1 = cy;               
                double y2 = startY;

                double x1 = cx;
                double x2 = startX;

                double edgeStartPosition = isLeft(sx0, sy0, sx1, sy1, x1, y1);
                double edgeEndPosition = isLeft(sx0, sy0, sx1, sy1, x2, y2);

                boolean forSureNotCrossing = (edgeStartPosition > 0 && edgeEndPosition > 0) || (edgeStartPosition < 0 && edgeEndPosition < 0);

                if(!forSureNotCrossing)
                {               
                    boolean rightToLeft = edgeStartPosition < 0 || (edgeStartPosition == 0 && edgeEndPosition > 0); 

                    double dx = x2 - x1;
                    double dy = y2 - y1;

                    double t = (dy*(x1 - sx0) + dx*(sy0 - y1))/(dy*sx - dx*sy);
                    if(t>=0 && t <= 1)
                    {
                        double xCrossing = sx0 + t*sx;
                        double yCrossing = sy0 + t*sy;
                        double r = (dx != 0) ? (xCrossing - x1)/dx :  (yCrossing - y1)/dy;

                        segments.addSegment(currentSegment);

                        Crossing2D currentCrossing = new Crossing2D(r, t, 0, 0, xCrossing, yCrossing, rightToLeft);    
                        segments.setFinalCrossingForLastAddedSegment(currentCrossing);

                        currentSegment = new BoundarySegment(currentCrossing);
                    }                  
                }

                if(currentSegment != null)
                {
                    currentSegment.addVertex(new double[] {x2, y2});
                    currentSegment.addVertices(initialVertices);                  
                }
                else
                {
                    initialVertices.add(new double[] {x2, y2});
                }

                cx = x2;
                cy = y2;
            }            

        }

        segments.addSegment(currentSegment); 
        segments.close();

        if(segments.isEmpty())
        {
            return null;
        }

        List<List<double[]>> polygonVerticesAll = segments.buildPolygons(sx0, sy0, sx1, sy1);
        List<Path2D> splitPaths = new ArrayList<>();
        for(List<double[]> polygonVertices : polygonVerticesAll)
        {
            Path2D p = GeometryUtilities.convertPolygonVerticesToPath(polygonVertices);
            splitPaths.add(p);
        }

        return splitPaths;
    }


    //returns null if the shape was not split
    public static List<Path2D> evaluateCrosssectioning(Shape path, double[][] polyline)
    {     
        //        Path2D path = new Path2D.Double();
        //        path.append(new Area(path).getPathIterator(null, 0.), false);
        //        return Collections.singletonList(path);

        int n = polyline.length;

        if(n < 2)
        {
            return null;
        }

        if(n == 2)
        {
            double[] startPoint = polyline[0];
            double[] endPoint = polyline[1];
            return evaluateCrosssectioning(path, startPoint[0], startPoint[1], endPoint[0], endPoint[1]);
        }

        PolygonCrossectionedBoundary segments = new PolygonCrossectionedBoundary();

        BoundarySegment currentPolygonSegment = null;

        List<double[]> initialVertices = new ArrayList<>();

        double[] coords = new double[6];

        double cx = 0;
        double cy = 0;

        double startX = 0;
        double startY = 0;


        //we may use PathIterator from the Area object to convert self-intersecting polygons into simple ones
        for(PathIterator iterator = path.getPathIterator(null, 0.); !iterator.isDone(); iterator.next())
        {
            int type = iterator.currentSegment(coords);

            if(PathIterator.SEG_MOVETO == type)
            {
                cx = coords[0];
                cy = coords[1];

                startX = cx;
                startY = cy;
                continue;
            }

            if(PathIterator.SEG_LINETO == type)
            {
                double y1 = cy;               
                double y2 = coords[1];

                double x1 = cx;
                double x2 = coords[0];

                double polyLineX0 = polyline[0][0];
                double polyLineY0 = polyline[0][1];

                double polylineCoveredDistance = 0;
                List<BoundarySegment> segmentsStartingNow = new ArrayList<>();

                for(int i = 1; i<n;i++)
                {
                    double[] currentPolylinePoint = polyline[i];

                    double polyLineX1 = currentPolylinePoint[0];
                    double polyLineY1 = currentPolylinePoint[1];

                    double sx = polyLineX1 - polyLineX0;
                    double sy = polyLineY1 - polyLineY0;

                    double edgeStartPosition = isLeft(polyLineX0, polyLineY0, polyLineX1, polyLineY1, x1, y1);
                    double edgeEndPosition = isLeft(polyLineX0, polyLineY0, polyLineX1, polyLineY1, x2, y2);

                    boolean forSureNotCrossing = (edgeStartPosition > 0 && edgeEndPosition > 0) || (edgeStartPosition < 0 && edgeEndPosition < 0);

                    double polyLineSegmentLegth = Math.sqrt(sx*sx + sy*sy);

                    if(!forSureNotCrossing)
                    {               
                        boolean rightToLeft = edgeStartPosition < 0 || (edgeStartPosition == 0 && edgeEndPosition > 0); 

                        double dx = x2 - x1;
                        double dy = y2 - y1;
                        double t = (dy*(x1 - polyLineX0) + dx*(polyLineY0 - y1))/(dy*sx - dx*sy);

                        if(t>=0 && t <= 1)
                        {
                            double xCrossing = polyLineX0 + t*sx;
                            double yCrossing = polyLineY0 + t*sy;
                            double r = (dx != 0) ? (xCrossing - x1)/dx :  (yCrossing - y1)/dy;

                            double crossingPosition = polylineCoveredDistance + t*polyLineSegmentLegth;
                            Crossing2D currentCrossing = new Crossing2D(r, crossingPosition, polylineCoveredDistance, i - 1, xCrossing, yCrossing, rightToLeft);      

                            segmentsStartingNow.add(new BoundarySegment(currentCrossing));
                        }
                    }

                    polyLineX0 = polyLineX1;
                    polyLineY0 = polyLineY1;
                    polylineCoveredDistance = polylineCoveredDistance + polyLineSegmentLegth;
                }

                Collections.sort(segmentsStartingNow, new Comparator<BoundarySegment>() {

                    @Override
                    public int compare(BoundarySegment o1,
                            BoundarySegment o2)
                    {
                        return Double.compare(o1.getInitialCrossing().getCurrentPolygonSegmentPosition(), o2.getInitialCrossing().getCurrentPolygonSegmentPosition());
                    }
                });


                int newSegmentsCount = segmentsStartingNow.size();

                if(newSegmentsCount > 0)
                {
                    segments.addSegment(currentPolygonSegment);

                    for(int i = 0; i < (newSegmentsCount - 1) ; i++)
                    {   
                        BoundarySegment s = segmentsStartingNow.get(i);
                        segments.setFinalCrossingForLastAddedSegment(s.getInitialCrossing());
                        segments.addSegment(s);                     
                    }
                    currentPolygonSegment = segmentsStartingNow.get(segmentsStartingNow.size() - 1);
                    segments.setFinalCrossingForLastAddedSegment(currentPolygonSegment.getInitialCrossing());
                }


                if(currentPolygonSegment != null)
                {
                    currentPolygonSegment.addVertex(new double[] {x2, y2});
                }
                else
                {
                    initialVertices.add(new double[] {x2, y2});
                }

                cx = x2;
                cy = y2;
            }          
            else if(PathIterator.SEG_CLOSE == type)
            {
                double y1 = cy;               
                double y2 = startY;

                double x1 = cx;
                double x2 = startX;

                double polyLineX0 = polyline[0][0];
                double polyLineY0 = polyline[0][1];

                double polylineCoveredDistance = 0;

                List<BoundarySegment> segmentsStartingNow = new ArrayList<>();

                for(int i = 1; i<n;i++)
                {
                    double[] currentPolylinePoint = polyline[i];

                    double polyLineX1 = currentPolylinePoint[0];
                    double polyLineY1 = currentPolylinePoint[1];

                    double sx = polyLineX1 - polyLineX0;
                    double sy = polyLineY1 - polyLineY0;

                    double edgeStartPosition = isLeft(polyLineX0, polyLineY0, polyLineX1, polyLineY1, x1, y1);
                    double edgeEndPosition = isLeft(polyLineX0, polyLineY0, polyLineX1, polyLineY1, x2, y2);

                    boolean forSureNotCrossing = (edgeStartPosition > 0 && edgeEndPosition > 0) || (edgeStartPosition < 0 && edgeEndPosition < 0);

                    double polyLineSegmentLegth = Math.sqrt(sx*sx + sy*sy);

                    if(!forSureNotCrossing)
                    {               
                        boolean rightToLeft = edgeStartPosition < 0 || (edgeStartPosition == 0 && edgeEndPosition > 0); 

                        double dx = x2 - x1;
                        double dy = y2 - y1;
                        double t = (dy*(x1 - polyLineX0) + dx*(polyLineY0 - y1))/(dy*sx - dx*sy);

                        if(t>=0 && t <= 1)
                        {
                            double xCrossing = polyLineX0 + t*sx;
                            double yCrossing = polyLineY0 + t*sy;
                            double r = (dx != 0) ? (xCrossing - x1)/dx :  (yCrossing - y1)/dy;

                            double crossingPosition = polylineCoveredDistance + t*polyLineSegmentLegth;
                            Crossing2D currentCrossing = new Crossing2D(r, crossingPosition, polylineCoveredDistance, i - 1, xCrossing, yCrossing, rightToLeft);      

                            segmentsStartingNow.add(new BoundarySegment(currentCrossing));
                        }
                    }

                    polyLineX0 = polyLineX1;
                    polyLineY0 = polyLineY1;

                    polylineCoveredDistance = polylineCoveredDistance + polyLineSegmentLegth;
                }

                Collections.sort(segmentsStartingNow, new Comparator<BoundarySegment>() {

                    @Override
                    public int compare(BoundarySegment o1,
                            BoundarySegment o2)
                    {
                        return Double.compare(o1.getInitialCrossing().getCurrentPolygonSegmentPosition(), o2.getInitialCrossing().getCurrentPolygonSegmentPosition());
                    }
                });

                int newSegmentsCount = segmentsStartingNow.size();

                if(newSegmentsCount > 0)
                {
                    segments.addSegment(currentPolygonSegment);

                    for(int i = 0; i < (newSegmentsCount - 1) ; i++)
                    {   
                        BoundarySegment s = segmentsStartingNow.get(i);
                        segments.setFinalCrossingForLastAddedSegment(s.getInitialCrossing());
                        segments.addSegment(s);                     
                    }
                    currentPolygonSegment = segmentsStartingNow.get(segmentsStartingNow.size() - 1);
                    segments.setFinalCrossingForLastAddedSegment(currentPolygonSegment.getInitialCrossing());
                }


                if(currentPolygonSegment != null)
                {
                    currentPolygonSegment.addVertex(new double[] {x2, y2});
                    currentPolygonSegment.addVertices(initialVertices);                  
                }
                else
                {
                    initialVertices.add(new double[] {x2, y2});
                }

                cx = x2;
                cy = y2;
            }            

        }

        segments.addSegment(currentPolygonSegment); 
        segments.close();

        if(segments.isEmpty())
        {
            return null;
        }

        List<List<double[]>> polygonVerticesAll = segments.buildPolygons(polyline);
        List<Path2D> splitPaths = new ArrayList<>();
        for(List<double[]> polygonVertices : polygonVerticesAll)
        {
            Path2D p = GeometryUtilities.convertPolygonVerticesToPath(polygonVertices);
            splitPaths.add(p);
        }

        segments.printSortedCrossings();
        segments.printSegments();

        return splitPaths;
    }


    //  test if a point is Left|On|Right of an infinite 2D line.
    //  Input:  three points P0, P1, and P2
    //  Return: >0 for P2 left of the line through P0 to P1
    //        =0 for P2 on the line
    //        <0 for P2 right of the line
    public static double isLeft(double x0, double y0, double x1, double y1, double x2, double y2)
    {
        return ((x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0));
    }

    public static class PolygonCrossectionedBoundary
    {
        private final List<BoundarySegment> segments = new ArrayList<>();
        private CrossingsHierarchy crossingHierarchy;
        private List<Crossing2D> sortedInOutCrossings;

        private BoundarySegment findSegmentByInitialCrossing(Crossing2D crossing)
        {
            BoundarySegment segment = null;

            for(BoundarySegment currentSegment : segments)
            {
                if(Objects.equals(currentSegment.getInitialCrossing(), crossing))
                {
                    segment = currentSegment;
                    break;
                }
            }

            return segment;
        }

        public int getSegmentCount()
        {
            return segments.size();
        }

        public boolean isEmpty()
        {
            return segments.isEmpty();
        }

        public void addSegment(BoundarySegment segment)
        {
            if(segment != null)
            {
                segments.add(segment);
            }
        }

        public void setFinalCrossingForLastAddedSegment(Crossing2D crossing)
        {
            if(!segments.isEmpty())
            {
                BoundarySegment lastSegment = segments.get(segments.size() - 1);
                lastSegment.setFinalCrossing(crossing);
            }
        }

        public void close()
        {
            if(!segments.isEmpty())
            {
                Crossing2D firstCrossing = segments.get(0).getInitialCrossing();
                BoundarySegment lastSegment = segments.get(segments.size() - 1);
                lastSegment.setFinalCrossing(firstCrossing);
            }

            buildCrossingsList();
        }

        private void buildCrossingsList()
        {
            //it has too be SortedSet, sorting a list is not enough
            SortedSet<Crossing2D> crossings = new TreeSet<>();

            for(BoundarySegment segment : segments)
            {
                crossings.add(segment.getInitialCrossing());
            }

            this.crossingHierarchy = CrossingsHierarchy.buildInstance(crossings);
            //internal crossings are only for self-intersecting polygons
            this.sortedInOutCrossings = crossingHierarchy.getCrossings(0);//Crossing2D.removeInternal(crossings);
        }

        public List<List<double[]>> buildPolygons(double sx0, double sy0, double sx1, double sy1)
        {
            List<List<double[]>> polygons = new ArrayList<>();            
            List<BoundarySegment> unusedSegments = new ArrayList<>(segments);

            while(!unusedSegments.isEmpty())
            {                
                List<double[]> currentPolygon = new ArrayList<>();
                polygons.add(currentPolygon);

                BoundarySegment currentSegment = unusedSegments.get(0);                                         
                Crossing2D polygonInitialCrossing = currentSegment.getInitialCrossing();            

                while(true)
                {      
                    Crossing2D currentInitialCrossing = currentSegment.getInitialCrossing();
                    Crossing2D currentFinalCrossing = currentSegment.getFinalCrossing();

                    currentPolygon.add(currentInitialCrossing.getVertex());
                    currentPolygon.addAll(currentSegment.getVertices());

                    unusedSegments.remove(currentSegment);               

                    Crossing2D nextInitialCrossing = crossingHierarchy.getNextCrossing(currentFinalCrossing);    
                    currentPolygon.add(currentFinalCrossing.getVertex());

                    currentSegment = findSegmentByInitialCrossing(nextInitialCrossing);

                    if(Objects.equals(polygonInitialCrossing, currentSegment.getInitialCrossing()))
                    {
                        break;
                    }
                }               
            }   

            return polygons;
        }

        public List<List<double[]>> buildPolygons(double[][] polyline)
        {                  
            List<List<double[]>> polygons = new ArrayList<>();            
            List<BoundarySegment> unusedSegments = new ArrayList<>(segments);

            while(!unusedSegments.isEmpty())
            {                
                List<double[]> currentPolygon = new ArrayList<>();
                polygons.add(currentPolygon);

                BoundarySegment currentSegment = unusedSegments.get(0);                                         
                Crossing2D polygonInitialCrossing = currentSegment.getInitialCrossing();            

                while(true)
                {      
                    Crossing2D currentInitialCrossing = currentSegment.getInitialCrossing();
                    Crossing2D currentFinalCrossing = currentSegment.getFinalCrossing();

                    currentPolygon.add(currentInitialCrossing.getVertex());
                    currentPolygon.addAll(currentSegment.getVertices());

                    unusedSegments.remove(currentSegment);
                    Crossing2D nextInitialCrossing = crossingHierarchy.getNextCrossing(currentFinalCrossing);    

                    currentPolygon.add(currentFinalCrossing.getVertex());

                    currentSegment = findSegmentByInitialCrossing(nextInitialCrossing);

                    //dodajemy wierzcho³ki split line pomiêdzy currentFinalCrossingIndex i nextCrossingIndex
                    int polyLineSegmentIndexA = currentFinalCrossing.getCuttingCurveSegmentIndex();
                    int polyLineSegmentIndexB = nextInitialCrossing.getCuttingCurveSegmentIndex();

                    if(polyLineSegmentIndexA < polyLineSegmentIndexB)
                    {
                        for(int i = polyLineSegmentIndexA + 1; i < polyLineSegmentIndexB + 1; i++)
                        {
                            currentPolygon.add(polyline[i]);
                        }
                    }
                    else
                    {
                        for(int i = polyLineSegmentIndexA; i > polyLineSegmentIndexB; i--)
                        {
                            currentPolygon.add(polyline[i]);
                        }
                    }

                    currentSegment = findSegmentByInitialCrossing(nextInitialCrossing);

                    if(Objects.equals(polygonInitialCrossing, currentSegment.getInitialCrossing()))
                    {
                        break;
                    }
                }               
            }   

            return polygons;
        }


        private static SegmentedPolyline calculatePolylineSelfCrosssections(double[][] polylineVertices, Collection<Crossing2D> polygonPolylineCrossection)
        {
            Polyline polyline = new Polyline(polylineVertices);
            int polylinePointCount = polylineVertices.length;

            Path2D polylinePath = GeometryUtilities.convertToOpenPath(polylineVertices);

            Map<Crossing2D, PolylineSegment> polylineSegments = new LinkedHashMap<>();

            for(Iterator<Crossing2D> it = polygonPolylineCrossection.iterator(); it.hasNext(); )
            {
                Crossing2D firstCrossection = it.next();

                if(!it.hasNext())
                {
                    break;
                }

                Crossing2D secondCrossection = it.next();

                int polyLineSegmentIndexA = firstCrossection.getCuttingCurveSegmentIndex();
                int polyLineSegmentIndexB = secondCrossection.getCuttingCurveSegmentIndex();

                double crossingPositionA = firstCrossection.getCuttingCurvePosition();
                double crossingPositionB = secondCrossection.getCuttingCurvePosition();

                double polylineCoveredDistanceOld = firstCrossection.getPreviousPolylineSegmentsLength();

                Crossing2D currentFirstPolylineSegmentEdge = firstCrossection;

                for(int i = polyLineSegmentIndexA; i <= polyLineSegmentIndexB; i++)
                {
                    double[] p0 = polylineVertices[i];
                    double[] p1 = polylineVertices[i + 1];

                    double x0 = p0[0];
                    double y0 = p0[1];
                    double x1 = p1[0];
                    double y1 = p1[1];


                    double polyLineSegmentLegthOld = polyline.getLength(i);

                    double polylineCoveredDistanceNew = 0;

                    for(int j = 0; j < polylinePointCount -1; j++)
                    {
                        double[] sp0 = polylineVertices[j];
                        double sx0 = sp0[0];
                        double sy0 = sp0[1]; 

                        double[] sp1 = polylineVertices[j + 1];

                        double sx1 = sp1[0];
                        double sy1 = sp1[1];

                        double polyLineSegmentLegthNew = polyline.getLength(j);

                        if(Math.abs(i - j) > 1)
                        {      
                            double edgeStartPosition = isLeft(sx0, sy0, sx1, sy1, x0, y0);
                            double edgeEndPosition = isLeft(sx0, sy0, sx1, sy1, x1, y1);

                            boolean forSureNotCrossing = (edgeStartPosition > 0 && edgeEndPosition > 0) || (edgeStartPosition < 0 && edgeEndPosition < 0);

                            if(!forSureNotCrossing)
                            {       

                                double sx = sx1 - sx0;
                                double sy = sy1 - sy0;

                                double dx = x1 - x0;
                                double dy = y1 - y0;

                                boolean rightToLeft = edgeStartPosition < 0 || (edgeStartPosition == 0 && edgeEndPosition > 0); 


                                double t = (dy*(x1 - sx0) + dx*(sy0 - y1))/(dy*sx - dx*sy);

                                if(t>=0 && t <= 1)
                                {
                                    double xCrossing = sx0 + t*sx;
                                    double yCrossing = sy0 + t*sy;
                                    double r = (dx != 0) ? (xCrossing - x0)/dx :  (yCrossing - y0)/dy;

                                    double crossingPositionOld = polylineCoveredDistanceOld + r*polyLineSegmentLegthOld;
                                    double crossingPossitionNew = polyline.getCumulatedLength(j) + r*polyline.getLength(j);

                                    if(crossingPositionOld >= crossingPositionA && crossingPositionOld <= crossingPositionB)
                                    {
                                        //"old" segment is treated as the cutting one
                                        Crossing2D currentCrossing = new Crossing2D(crossingPossitionNew, crossingPositionOld, polylineCoveredDistanceOld, i, xCrossing, yCrossing, rightToLeft);      
                                        currentCrossing.setPolylineSelfCrossing(true);

                                        PolylineSegment polylineSegment = PolylineSegment.buildInstance(polylinePath, polylineVertices, currentFirstPolylineSegmentEdge, currentCrossing);
                                        polylineSegments.put(currentFirstPolylineSegmentEdge, polylineSegment);

                                        currentFirstPolylineSegmentEdge = currentCrossing;
                                    }               
                                }
                            } 
                        }

                        polylineCoveredDistanceNew = polylineCoveredDistanceNew + polyLineSegmentLegthNew;
                    }

                    polylineCoveredDistanceOld = polylineCoveredDistanceOld + polyLineSegmentLegthOld;
                }

                PolylineSegment polylineSegment = PolylineSegment.buildInstance(polylinePath, polylineVertices, currentFirstPolylineSegmentEdge, secondCrossection);
                polylineSegments.put(currentFirstPolylineSegmentEdge, polylineSegment);

            }

            return new SegmentedPolyline(polylineSegments);
        }

        private static class Polyline
        {
            private final double[][] polyline;
            private final double[] lengths;
            private final double[] cumulatedLengths;

            private Polyline(double[][] polyline)
            {
                this.polyline = polyline;


                int n = polyline.length;

                double[] lengths = new double[n - 1];
                double[] cumulatedLengths = new double[n - 1];

                double[] p0 = polyline[0];

                double x0 = p0[0];
                double y0 = p0[1];


                double currentCumulatedLength = 0;
                for(int i = 1; i<n;i++)
                {
                    double[] p1 = polyline[i];

                    double x1 = p1[0];
                    double y1 = p1[1];

                    double dx = x1 - x0;
                    double dy = y1 - y0;

                    double segmentLength = Math.sqrt(dx*dx + dy*dy);

                    lengths[i - 1] = segmentLength;
                    currentCumulatedLength += segmentLength;
                    cumulatedLengths[i - 1] = currentCumulatedLength;

                    x0 = x1;
                    y0 = y1;
                }

                this.lengths = lengths;
                this.cumulatedLengths = cumulatedLengths;
            }

            private double getLength(int index)
            {
                return lengths[index];
            }

            private double getCumulatedLength(int index)
            {
                return cumulatedLengths[index];
            }

            private double[] get(int index)
            {
                return polyline[index];
            }
        }

        private static class SegmentedPolyline
        {
            private final Map<Crossing2D, PolylineSegment> segments;

            private SegmentedPolyline(Map<Crossing2D, PolylineSegment> segments)
            {
                this.segments = segments;
            }


            private Crossing2D traceFromInitialCrossing(Crossing2D initialCrossing)
            {
                PolylineSegment initialSegment = findByInitialCrossing(initialCrossing);

                return null;
            }

            private PolylineSegment findByInitialCrossing(Crossing2D initialCrossing)
            { 
                return segments.get(initialCrossing);
            }

            private PolylineSegment findByFinalCrossing(Crossing2D lastCrossing)
            {
                for(PolylineSegment seg : segments.values())
                {
                    if(Objects.equals(lastCrossing, seg.getFinalCrossing()))
                    {
                        return seg;
                    }
                }

                return null;
            }
        }

        private static class PolylineSegment
        {
            private static final double TOLERANCE = 1e-10;

            private final Crossing2D initialCrossing;
            private final Crossing2D finalCrossing;

            private final int levelLeft;
            private final int levelRight;

            private PolylineSegment(Crossing2D initialCrossing, Crossing2D finalCrossing, int levelLeft, int levelRight)
            {
                this.initialCrossing = initialCrossing;
                this.finalCrossing = finalCrossing;
                this.levelLeft = levelLeft;
                this.levelRight = levelRight;
            }                        

            public Crossing2D getInitialCrossing()
            {
                return initialCrossing;
            }

            public Crossing2D getFinalCrossing()
            {
                return finalCrossing;
            }

            public int getLevelLeft()
            {
                return levelLeft;
            }

            public int getLevelRight()
            {
                return levelRight;
            }

            public List<double[]> getAssociatedVertices(double[][] polyline)
            {
                List<double[]> vertices = new ArrayList<>();

                int polyLineSegmentIndexA = initialCrossing.getCuttingCurveSegmentIndex();
                int polyLineSegmentIndexB = finalCrossing.getCuttingCurveSegmentIndex();

                vertices.add(initialCrossing.getVertex());

                for(int i = polyLineSegmentIndexA + 1; i < polyLineSegmentIndexB + 1; i++)
                {
                    vertices.add(polyline[i]);
                }

                vertices.add(finalCrossing.getVertex());

                return vertices;
            }

            private static PolylineSegment buildInstance(Path2D polylinePath, double[][] polylineVertices, Crossing2D initialCrossing, Crossing2D finalCrossing)
            {
                int initSegmentIndex = initialCrossing.getCuttingCurveSegmentIndex();
                int finalSegmentIndex = finalCrossing.getCuttingCurveSegmentIndex();

                double[] initSegmentP0 = polylineVertices[initSegmentIndex];
                double[] initSegmentP1 = polylineVertices[initSegmentIndex + 1];

                double initSegmentX0 = initSegmentP0[0];
                double initSegmentY0 = initSegmentP0[1];

                double dx = initSegmentP1[0] - initSegmentX0;
                double dy = initSegmentP1[1] - initSegmentY0;


                double initSegmentLength = Math.sqrt(dx*dx + dy*dy);
                double segmentBeforeInitSegmentLength = initialCrossing.getPreviousPolylineSegmentsLength();

                double testPointPosition = (finalSegmentIndex == initSegmentIndex) ? 0.5*(initialCrossing.getCuttingCurvePosition() + finalCrossing.getCuttingCurvePosition())
                        : 0.5*(initialCrossing.getCuttingCurvePosition() + (segmentBeforeInitSegmentLength + initSegmentLength));

                double t = (testPointPosition - segmentBeforeInitSegmentLength)/initSegmentLength;

                double testPointX = initSegmentX0 + t*dx;
                double testPointY = initSegmentY0 + t*dy;

                int levelLeft = 0;
                int levelRight = 0;

                SortedSet<Crossing> crossings = buildCrossingsWindingNonzero(polylinePath, testPointY);

                int windingNumber = 0;

                for(Crossing crossing : crossings)
                {
                    int w = crossing.isUpwards() ? 1 : -1;
                    int windingNumberNew = windingNumber + w;

                    double val = crossing.getValue();
                    if(GeometryUtilities.almostEqual(val, testPointX, TOLERANCE))
                    {
                        levelLeft = windingNumber;
                        levelRight = windingNumberNew;
                        break;
                    }

                    windingNumber = windingNumberNew;
                }

                return new PolylineSegment(initialCrossing, finalCrossing, levelLeft, levelRight);
            }
        }

        private void printSortedCrossings()
        {
            for(Crossing2D crossing : sortedInOutCrossings)
            {
                System.out.println("x " + crossing.getVertex()[0] + " y " + crossing.getVertex()[1] + " position " + crossing.getCuttingCurvePosition() + " index " + crossing.getCuttingCurveSegmentIndex());
            }
        }

        private void printSegments()
        {
            for(BoundarySegment segment : segments)
            {
                System.out.println("---------------------------");
                System.out.println("VERTICES COUNT " + segment.getVertices().size());
                System.out.println("INITIAL CROSSING INDEX " + sortedInOutCrossings.indexOf(segment.getInitialCrossing()) + " X " + segment.getInitialCrossing().getVertex()[0]);
                System.out.println("FINAL CROSSING INDEX " + sortedInOutCrossings.indexOf(segment.getFinalCrossing()) + " X " + segment.getFinalCrossing().getVertex()[0] );

                ArrayUtilities.print(segment.getVertices().toArray(new double[][] {}));
                System.out.println("---------------------------");
            }
        }
    }

    private static class BoundarySegment
    {
        private final Crossing2D initialCrossing;
        private Crossing2D finalCrossing;

        private final List<double[]> vertices = new ArrayList<>();

        public BoundarySegment(Crossing2D initialCrossing)
        {
            this.initialCrossing = initialCrossing;
        }

        public Crossing2D getInitialCrossing()
        {
            return initialCrossing;
        }

        public Crossing2D getFinalCrossing()
        {
            return finalCrossing;
        }

        public void setFinalCrossing(Crossing2D finalCrossing)
        {
            this.finalCrossing = finalCrossing;
        }

        public List<double[]> getVertices()
        {
            return Collections.unmodifiableList(this.vertices);
        }

        public void addVertex(double[] vertex)
        {
            vertices.add(vertex);
        }

        public void addVertices(Collection<double[]> verticesNew)
        {
            vertices.addAll(verticesNew);
        }
    }
}
