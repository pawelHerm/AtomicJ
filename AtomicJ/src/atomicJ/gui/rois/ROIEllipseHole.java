
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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.Serializable;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.util.PublicCloneable;

import atomicJ.data.ArraySupport2D;
import atomicJ.data.Grid2D;
import atomicJ.gui.MouseInputMode;
import atomicJ.gui.MouseInputModeStandard;
import atomicJ.gui.ShapeFactors;
import atomicJ.gui.rois.region.NullRegion;
import atomicJ.gui.rois.region.Region;

public class ROIEllipseHole extends ROIRectangularShape implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final Area datasetArea;
    private final Ellipse2D holeShape; 
    private final float handleLength = 10.f;

    public ROIEllipseHole(Area datasetArea, Ellipse2D hole, Integer key, ROIStyle style) 
    {
        super(key, style);

        this.datasetArea = datasetArea;
        this.holeShape = new Ellipse2D.Double(hole.getX(), hole.getY(), hole.getWidth(), hole.getHeight());    
    }

    public ROIEllipseHole(Area datasetArea, Ellipse2D hole, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        this.datasetArea = datasetArea;
        this.holeShape = new Ellipse2D.Double(hole.getX(), hole.getY(), hole.getWidth(), hole.getHeight());    
    }

    public ROIEllipseHole(Area datasetArea, Ellipse2D hole, AffineTransform transform, Integer key, String label, ROIStyle style) 
    {
        super(transform, key, label, style);

        this.datasetArea = datasetArea;
        this.holeShape = new Ellipse2D.Double(hole.getX(), hole.getY(), hole.getWidth(), hole.getHeight());    
    }

    public ROIEllipseHole(ROIEllipseHole that)
    {
        this(that, that.getStyle());
    }

    public ROIEllipseHole(ROIEllipseHole that, ROIStyle style)
    {
        super(that, style);     
        this.datasetArea = new Area(that.datasetArea);
        this.holeShape = new Ellipse2D.Double(that.holeShape.getX(), that.holeShape.getY(), that.holeShape.getWidth(), that.holeShape.getHeight());
    }

    public ROIEllipseHole(ROIEllipseHole that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);     
        this.datasetArea = new Area(that.datasetArea);
        this.holeShape = new Ellipse2D.Double(that.holeShape.getX(), that.holeShape.getY(), that.holeShape.getWidth(), that.holeShape.getHeight());
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness)
    {
        return ShapeFactors.getShapeFactorsForArbitraryShape(getROIShape(), flatness);
    }

    @Override
    public ROIEllipseHole copy()
    {
        return new ROIEllipseHole(this);
    }

    @Override
    public ROIEllipseHole copy(ROIStyle style)
    {
        return new ROIEllipseHole(this, style);
    }

    @Override
    public ROIEllipseHole copy(ROIStyle style, Integer key, String label)
    {
        return new ROIEllipseHole(this, style, key, label);
    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        return true;
    }

    @Override
    public Ellipse2D getNonTransformedModifiableShape()
    {
        return holeShape;
    }

    @Override
    public Shape getROIShape() 
    {
        Area roiShape = new Area(this.datasetArea);
        Shape hole = isTransformed() ? getTransform().createTransformedShape(this.holeShape) : this.holeShape;

        roiShape.subtract(new Area(hole));
        return roiShape;
    }

    @Override
    public MouseInputMode getMouseInputMode(MouseInputMode oldMode) 
    {
        if(oldMode.isROI())
        {
            return oldMode;
        }
        return MouseInputModeStandard.ELIPTIC_ROI;
    }


    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) 
    {
        drawHotSpots(g2, plot, dataArea, domainAxis, rangeAxis, rendererIndex, info, new NullRegion());
    }

    @Override
    public void drawHotSpots(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
            ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info, Region excludedShape) 
    {        
        if(isVisible() && isHighlighted())
        {
            AffineTransform java2DTr = getDataToJava2DTransformation(g2, plot, dataArea, domainAxis, rangeAxis, info);
            AffineTransform currentTransform = getTransform();

            RectangularShape rect = getNonTransformedModifiableShape();

            Point2D nwPoint = currentTransform.transform(new Point2D.Double(rect.getMinX(), rect.getMaxY()), null);
            Point2D nePoint = currentTransform.transform(new Point2D.Double(rect.getMaxX(), rect.getMaxY()), null);
            Point2D sePoint = currentTransform.transform(new Point2D.Double(rect.getMaxX(), rect.getMinY()), null);
            Point2D swPoint = currentTransform.transform(new Point2D.Double(rect.getMinX(), rect.getMinY()), null);

            Point2D nwPointJava2D = java2DTr.transform(nwPoint, null);
            Point2D nePointJava2D = java2DTr.transform(nePoint, null);
            Point2D sePointJava2D = java2DTr.transform(sePoint, null);
            Point2D swPointJava2D = java2DTr.transform(swPoint, null);

            g2.setPaint(getStrokePaint());

            if(!excludedShape.contains(nwPoint.getX(), nwPoint.getY()))
            {
                Path2D nwHandle = buildHandle(swPointJava2D, nwPointJava2D, nePointJava2D);
                g2.draw(nwHandle);      
            }

            if(!excludedShape.contains(nePoint.getX(), nePoint.getY()))
            {
                Path2D neHandle = buildHandle(nwPointJava2D, nePointJava2D, sePointJava2D);
                g2.draw(neHandle);
            }

            if(!excludedShape.contains(sePoint.getX(), sePoint.getY()))
            {
                Path2D seHandle = buildHandle(nePointJava2D, sePointJava2D, swPointJava2D);
                g2.draw(seHandle);      
            }

            if(!excludedShape.contains(swPoint.getX(), swPoint.getY()))
            {              
                Path2D swHandle = buildHandle(sePointJava2D, swPointJava2D, nwPointJava2D);
                g2.draw(swHandle);    
            }  
        }  
    }

    private Path2D buildHandle(Point2D pointA, Point2D pointCentral, Point2D pointB)
    {
        Path2D handle = new GeneralPath();

        double dxA = pointA.getX() - pointCentral.getX();
        double dyA = pointA.getY() - pointCentral.getY();
        double dxB = pointB.getX() - pointCentral.getX();
        double dyB = pointB.getY() - pointCentral.getY();

        double lengthA = Math.sqrt(dxA*dxA + dyA*dyA);
        double lengthB = Math.sqrt(dxB*dxB + dyB*dyB);

        double tA = Math.min(0.35, handleLength/lengthA);
        double tB = Math.min(0.35, handleLength/lengthB);

        handle.moveTo(pointCentral.getX() + tA*dxA,pointCentral.getY() + tA*dyA);
        handle.lineTo(pointCentral.getX(), pointCentral.getY());
        handle.lineTo(pointCentral.getX() + tB*dxB,pointCentral.getY() + tB*dyB);

        return handle;
    }


    @Override
    public int getPointsInsideCountUpperBound(ArraySupport2D grid)
    {
        return GridPositionCalculator.getInsidePointCountUpperBound(grid, getROIShape());
    }

    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient)
    {              
        if(grid instanceof Grid2D)
        {
            addPointsInsideRegularGriOptimized((Grid2D)grid, recepient);
            return;
        }

        if(isTransformed())
        {
            addPointsInsideTransformed(grid, recepient);
            return;
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double widthNew = holeShape.getWidth();
        double heightNew = holeShape.getHeight();

        double centerXNew = holeShape.getCenterX();
        double centerYNew = holeShape.getCenterY();

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. outside the hole

        int roiMinRow = Math.max(0, grid.getRowFloor(minY));
        int roiMaxRow = Math.min(rowCount - 1, grid.getRowCeiling(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i) - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            //minColumn is -1 when the hole left edge goes to the left outside the data area
            int minColumn = Math.max(-1, grid.getColumnFloor(xMinTr));
            int maxColumn = Math.min(columnCount, grid.getColumnCeiling(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            recepient.addBlock(i, i + 1, 0, Math.min(columnCount, minColumn + 1));
            recepient.addBlock(i, i + 1, Math.max(0, maxColumn), columnCount);
        }

        recepient.addBlock(0, Math.min(roiMinRow + 1, rowCount), 0, columnCount);
        recepient.addBlock(Math.max(roiMaxRow, 0), rowCount, 0, columnCount);
    }

    private void addPointsInsideRegularGriOptimized(Grid2D grid, GridPointRecepient recepient)
    {
        if(isTransformed())
        {
            addPointsInsideTransformed(grid, recepient);
            return;
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double xIncrement = grid.getXIncrement();
        double yIncrement = grid.getYIncrement();

        double widthNew = holeShape.getWidth()/xIncrement;
        double heightNew = holeShape.getHeight()/yIncrement;

        double centerXNew = (holeShape.getCenterX() - grid.getXOrigin())/xIncrement;
        double centerYNew = (holeShape.getCenterY() - grid.getYOrigin())/yIncrement;

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. outside the hole

        int roiMinRow = Math.max(0, (int)Math.floor(minY));
        int roiMaxRow = Math.min(rowCount - 1, (int)Math.ceil(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = i - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            //minColumn is -1 when the hole left edge goes to the left outside the data area
            int minColumn = Math.max(-1, (int)Math.floor(xMinTr));
            int maxColumn = Math.min(columnCount, (int)Math.ceil(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            recepient.addBlock(i, i + 1, 0, Math.min(columnCount, minColumn + 1));
            recepient.addBlock(i, i + 1, Math.max(0, maxColumn), columnCount);
        }

        recepient.addBlock(0, Math.min(roiMinRow + 1, rowCount), 0, columnCount);
        recepient.addBlock(Math.max(roiMaxRow, 0), rowCount, 0, columnCount);
    }

    private void addPointsInsideTransformed(ArraySupport2D grid, GridPointRecepient recepient)
    {
        RectangularShape nonTransformedShape = getNonTransformedModifiableShape();

        double a = nonTransformedShape.getWidth()/2;
        double b = nonTransformedShape.getHeight()/2;
        double xc = nonTransformedShape.getCenterX();
        double yc = nonTransformedShape.getCenterY();

        AffineTransform transform = new AffineTransform(getTransform());

        double[] elements = new double[6];

        transform.getMatrix(elements);

        double a11 = elements[0];
        double a12 = elements[2];
        double b1 = elements[4];
        double a21 = elements[1];
        double a22 = elements[3];
        double b2 = elements[5];

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        Rectangle2D bounds = getROIShape().getBounds2D();

        int roiMinRow = Math.max(0, grid.getRowCeiling(bounds.getMinY()));
        int roiMaxRow = Math.min(rowCount - 1, grid.getRowFloor(bounds.getMaxY()));

        double f1 = a12*a21 - a11*a22;
        double f2 = (-a*a + xc*xc)*a21*a21;
        double f3 = (b - yc)*a22 - b2;
        double f4 = -(b + yc)*a22 - b2;
        double f5 = yc*a22 + b2;
        double denominator = a*a*a21*a21 + a22*a22*b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i);

            double sqrFactor = a*b*Math.sqrt((-f1*f1*(f2 + (y + f3)*(y + f4) + 2*xc*a21*(-y + f5))));

            double factorA = a22*b*b*(a22*(b1 + a11*xc) + a12*(-b2 - a21*xc + y)) + a*a*a21*(a21 *(b1 + a12*yc) + a11*(-b2 - a22*yc + y));
            double x1 = (factorA + sqrFactor)/denominator;
            double x2 = (factorA - sqrFactor)/denominator;

            double xMin = Math.min(x1, x2);
            double xMax = Math.max(x1,x2);
            boolean xsFound = !Double.isNaN(xMin) && !Double.isNaN(xMax);

            if(xsFound)
            {
                //minColumn is -1 when the hole left edge goes to the left outside the data area
                int minColumn = Math.max(-1, grid.getColumnFloorWithinBounds(xMin));
                int maxColumn = Math.min(columnCount, grid.getColumnCeilingWithinBounds(xMax));

                //these mins and max in for loops guard against exceptions when ROIs are completely
                //outside data area

                recepient.addBlock(i, i + 1, 0, Math.min(columnCount, minColumn + 1));
                recepient.addBlock(i, i + 1, Math.max(0, maxColumn), columnCount);
            }
            else
            {
                recepient.addBlock(i, i + 1, 0, columnCount);
            }
        }

        //adds points that are outside bounding box


        recepient.addBlock(0, Math.min(roiMinRow, rowCount), 0, columnCount);
        recepient.addBlock(Math.max(roiMaxRow + 1, 0), rowCount, 0, columnCount);
    }

    @Override
    public void addPointsOutside(ArraySupport2D grid, GridPointRecepient recepient)
    {
        if(grid instanceof Grid2D)
        {
            addPointsOutsideRegularGridOptimized((Grid2D)grid, recepient);
            return;
        }

        if(isTransformed())
        {
            addPointsOutsideTransformed(grid, recepient);
            return;
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double widthNew = holeShape.getWidth();
        double heightNew = holeShape.getHeight();

        double centerXNew = holeShape.getCenterX();
        double centerYNew = holeShape.getCenterY();

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. outside the hole

        int roiMinRow = Math.max(0, grid.getRowFloor(minY));
        int roiMaxRow = Math.min(rowCount - 1, grid.getRowCeiling(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i) - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            //minColumn and maxColumn are included in the ROI, so we will not include them
            //in the outside 
            //minColumn is -1 when the hole left edge goes to the left outside the data area
            int minColumn = Math.max(-1, grid.getColumnFloor(xMinTr));
            int maxColumn = Math.min(columnCount, grid.getColumnCeiling(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            recepient.addBlock(i, i + 1, minColumn + 1, Math.min(columnCount, maxColumn));
        }
    }

    private void addPointsOutsideRegularGridOptimized(Grid2D grid, GridPointRecepient recepient)
    {
        if(isTransformed())
        {
            addPointsOutsideTransformed(grid, recepient);
            return;
        }

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double xIncrement = grid.getXIncrement();
        double yIncrement = grid.getYIncrement();

        double widthNew = holeShape.getWidth()/xIncrement;
        double heightNew = holeShape.getHeight()/yIncrement;

        double centerXNew = (holeShape.getCenterX() - grid.getXOrigin())/xIncrement;
        double centerYNew = (holeShape.getCenterY() - grid.getYOrigin())/yIncrement;

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. outside the hole

        int roiMinRow = Math.max(0, (int)Math.floor(minY));
        int roiMaxRow = Math.min(rowCount - 1, (int)Math.ceil(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = i - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            //minColumn and maxColumn are included in the ROI, so we will not include them
            //in the outside 
            //minColumn is -1 when the hole left edge goes to the left outside the data area
            int minColumn = Math.max(-1, (int)Math.floor(xMinTr));
            int maxColumn = Math.min(columnCount, (int)Math.ceil(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            recepient.addBlock(i, i + 1, minColumn + 1, Math.min(columnCount, maxColumn));
        }
    }

    public void addPointsOutsideTransformed(ArraySupport2D grid, GridPointRecepient recepient)
    {
        RectangularShape nonTransformedShape = getNonTransformedModifiableShape();

        double a = nonTransformedShape.getWidth()/2;
        double b = nonTransformedShape.getHeight()/2;
        double xc = nonTransformedShape.getCenterX();
        double yc = nonTransformedShape.getCenterY();

        AffineTransform transform = new AffineTransform(getTransform());

        double[] elements = new double[6];

        transform.getMatrix(elements);

        double a11 = elements[0];
        double a12 = elements[2];
        double b1 = elements[4];
        double a21 = elements[1];
        double a22 = elements[3];
        double b2 = elements[5];

        Shape roiShape = getROIShape();
        Rectangle2D bounds = roiShape.getBounds2D();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        int roiMinRow = Math.max(0, grid.getRowCeiling(bounds.getMinY()));
        int roiMaxRow = Math.min(rowCount - 1, grid.getRowFloor(bounds.getMaxY()));

        double f1 = a12*a21 - a11*a22;
        double f2 = (-a*a + xc*xc)*a21*a21;
        double f3 = (b - yc)*a22 - b2;
        double f4 = -(b + yc)*a22 - b2;
        double f5 = yc*a22 + b2;
        double denominator = a*a*a21*a21 + a22*a22*b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i);

            double sqrFactor = a*b*Math.sqrt((-f1*f1*(f2 + (y + f3)*(y + f4) + 2*xc*a21*(-y + f5))));

            double factorA = a22*b*b*(a22*(b1 + a11*xc) + a12*(-b2 - a21*xc + y)) + a*a*a21*(a21 *(b1 + a12*yc) + a11*(-b2 - a22*yc + y));
            double x1 = (factorA + sqrFactor)/denominator;
            double x2 = (factorA - sqrFactor)/denominator;

            double xMin = Math.min(x1, x2);
            double xMax = Math.max(x1,x2);
            boolean xsFound = !Double.isNaN(xMin) && !Double.isNaN(xMax);

            if(xsFound)
            {
                //minColumn and maxColumn are included in the ROI, so we will not include them
                //in the outside 
                //minColumn is -1 when the hole left edge goes to the left outside the data area
                int minColumn = Math.max(-1, grid.getColumnFloorWithinBounds(xMin));
                int maxColumn = Math.min(columnCount, grid.getColumnCeilingWithinBounds(xMax));

                //these mins and max in for loops guard against exceptions when ROIs are completely
                //outside data area

                recepient.addBlock(i, i + 1, minColumn + 1, Math.min(columnCount, maxColumn));
            }
        }
    }

    @Override
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        if(grid instanceof Grid2D)
        {
            dividePointsGridOptimized((Grid2D)grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
            return;
        }

        if(isTransformed())
        {
            dividePointsTransformed(grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);

            return;
        }

        double widthNew = holeShape.getWidth();
        double heightNew = holeShape.getHeight();

        double centerXNew = holeShape.getCenterX();
        double centerYNew = holeShape.getCenterY();

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. outside the hole

        int roiMinRow = Math.max(imageMinRow, grid.getRowFloor(minY));
        int roiMaxRow = Math.min(imageMaxRow, grid.getRowCeiling(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i) - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            //minColumn and maxColumn are included in the ROI
            //minColumn is imageMinColumn - 1 when the hole left edge goes to the left outside the data area
            int minColumn = Math.max(imageMinColumn - 1, grid.getColumnFloor(xMinTr));
            int maxColumn = Math.min(imageMaxColumn + 1, grid.getColumnCeiling(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            for(int j = imageMinColumn; j<minColumn + 1; j++)
            {
                recepient.addPointInside(i, j);
            }
            for(int j = maxColumn; j<imageMaxColumn + 1; j++)
            {
                recepient.addPointInside(i, j);
            }
            for(int j = minColumn + 1; j<Math.min(imageMaxColumn + 1, maxColumn); j++)
            {
                recepient.addPointOutside(i, j);
            }
        }

        for(int i = imageMinRow; i<roiMinRow; i++)
        {
            for(int j = imageMinColumn; j<imageMaxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }
        }

        for(int i = roiMaxRow + 1; i<imageMaxRow + 1; i++)
        {
            for(int j = imageMinColumn; j<imageMaxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }
        }
    }


    public void dividePointsGridOptimized(Grid2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        if(isTransformed())
        {
            dividePointsTransformed(grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);

            return;
        }

        double xIncrement = grid.getXIncrement();
        double yIncrement = grid.getYIncrement();

        double widthNew = holeShape.getWidth()/xIncrement;
        double heightNew = holeShape.getHeight()/yIncrement;

        double centerXNew = (holeShape.getCenterX() - grid.getXOrigin())/xIncrement;
        double centerYNew = (holeShape.getCenterY() - grid.getYOrigin())/yIncrement;

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. outside the hole

        int roiMinRow = Math.max(imageMinRow, (int)Math.floor(minY));
        int roiMaxRow = Math.min(imageMaxRow, (int)Math.ceil(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = i - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            //minColumn and maxColumn are included in the ROI
            //minColumn is imageMinColumn - 1 when the hole left edge goes to the left outside the data area
            int minColumn = Math.max(imageMinColumn - 1, (int)Math.floor(xMinTr));
            int maxColumn = Math.min(imageMaxColumn + 1, (int)Math.ceil(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            for(int j = imageMinColumn; j<minColumn + 1; j++)
            {
                recepient.addPointInside(i, j);
            }
            for(int j = maxColumn; j<imageMaxColumn + 1; j++)
            {
                recepient.addPointInside(i, j);
            }
            for(int j = minColumn + 1; j<Math.min(imageMaxColumn + 1, maxColumn); j++)
            {
                recepient.addPointOutside(i, j);
            }
        }

        for(int i = imageMinRow; i<roiMinRow; i++)
        {
            for(int j = imageMinColumn; j<imageMaxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }
        }

        for(int i = roiMaxRow + 1; i<imageMaxRow + 1; i++)
        {
            for(int j = imageMinColumn; j<imageMaxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }
        }
    }

    private void dividePointsTransformed(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        RectangularShape nonTransformedShape = getNonTransformedModifiableShape();

        double a = nonTransformedShape.getWidth()/2;
        double b = nonTransformedShape.getHeight()/2;
        double xc = nonTransformedShape.getCenterX();
        double yc = nonTransformedShape.getCenterY();

        AffineTransform transform = new AffineTransform(getTransform());

        double[] elements = new double[6];

        transform.getMatrix(elements);

        double a11 = elements[0];
        double a12 = elements[2];
        double b1 = elements[4];
        double a21 = elements[1];
        double a22 = elements[3];
        double b2 = elements[5];

        Shape transformedHole = getTransformedModifiableShape();
        Rectangle2D bounds = transformedHole.getBounds2D();

        //the minimal and maximal indices of rows and columns INSIDE the ROI,
        //i.e. outside the hole

        int roiMinRow = Math.max(imageMinRow, grid.getRowCeiling(bounds.getMinY()));
        int roiMaxRow = Math.min(imageMaxRow, grid.getRowFloor(bounds.getMaxY()));

        double f1 = a12*a21 - a11*a22;
        double f2 = (-a*a + xc*xc)*a21*a21;
        double f3 = (b - yc)*a22 - b2;
        double f4 = -(b + yc)*a22 - b2;
        double f5 = yc*a22 + b2;
        double denominator = a*a*a21*a21 + a22*a22*b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i);

            double sqrFactor = a*b*Math.sqrt((-f1*f1*(f2 + (y + f3)*(y + f4) + 2*xc*a21*(-y + f5))));

            double factorA = a22*b*b*(a22*(b1 + a11*xc) + a12*(-b2 - a21*xc + y)) + a*a*a21*(a21 *(b1 + a12*yc) + a11*(-b2 - a22*yc + y));
            double x1 = (factorA + sqrFactor)/denominator;
            double x2 = (factorA - sqrFactor)/denominator;

            double xMin = Math.min(x1, x2);
            double xMax = Math.max(x1,x2);
            boolean xsFound = !Double.isNaN(xMin) && !Double.isNaN(xMax);

            if(xsFound)
            {
                //minColumn and maxColumn are included in the ROI
                //minColumn is imageMinColumn - 1 when the hole left edge goes to the left outside the data area
                int minColumn = Math.max(imageMinColumn - 1, grid.getColumnFloorWithinBounds(xMin));
                int maxColumn = Math.min(imageMaxColumn + 1, grid.getColumnCeilingWithinBounds(xMax));

                //these mins and max in for loops guard against exceptions when ROIs are completely
                //outside data area

                for(int j = imageMinColumn; j<minColumn + 1; j++)
                {
                    recepient.addPointInside(i, j);
                }
                for(int j = maxColumn; j<imageMaxColumn + 1; j++)
                {
                    recepient.addPointInside(i, j);
                }
                for(int j = minColumn + 1; j<Math.min(imageMaxColumn + 1, maxColumn); j++)
                {
                    recepient.addPointOutside(i, j);
                }
            }
            else
            {
                for(int j = imageMinColumn; j<imageMaxColumn + 1; j++)
                {
                    recepient.addPointInside(i, j);
                }
            }
        }

        for(int i = imageMinRow; i<roiMinRow; i++)
        {
            for(int j = imageMinColumn; j<imageMaxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }
        }

        for(int i = roiMaxRow + 1; i<imageMaxRow + 1; i++)
        {
            for(int j = imageMinColumn; j<imageMaxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }
        }
    }

    @Override
    public ROIProxy getProxy()
    {
        return new ROIEllipseHoleSerializationProxy(holeShape, datasetArea, getTransform(), getCustomLabel(), isFinished());
    }

    private static class ROIEllipseHoleSerializationProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Path2D datasetArea;
        private final Ellipse2D holeShape;
        private final AffineTransform transform;
        private final String customLabel;
        private final boolean finished;

        private ROIEllipseHoleSerializationProxy(Ellipse2D holeShape, Area datasetArea, AffineTransform transform, String customLabel, boolean finished)
        {
            this.holeShape = holeShape;
            this.datasetArea = new Path2D.Double(datasetArea); //we must use Path2D.Double, as area is not serializable
            this.transform = transform;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIEllipseHole recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();
            ROIEllipseHole roi = new ROIEllipseHole(new Area(datasetArea), holeShape, transform, key, label, roiStyle);
            roi.setFinished(finished);

            return roi;
        }     
    }
}
