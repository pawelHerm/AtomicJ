
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


public class ROIEllipse extends ROIRectangularShape implements Cloneable, PublicCloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private final Ellipse2D nonTransformedShape; 
    private final float handleLength = 10.f;


    public ROIEllipse(Ellipse2D ellipse, Integer key, ROIStyle style) 
    {
        super(key, style);
        this.nonTransformedShape = new Ellipse2D.Double(ellipse.getX(), ellipse.getY(), ellipse.getWidth(), ellipse.getHeight());    
    }

    public ROIEllipse(Ellipse2D ellipse, Integer key, String label, ROIStyle style) 
    {
        super(key, label, style);

        this.nonTransformedShape = new Ellipse2D.Double(ellipse.getX(), ellipse.getY(), ellipse.getWidth(), ellipse.getHeight());    
    }

    public ROIEllipse(Ellipse2D ellipse, AffineTransform transform, Integer key, String label, ROIStyle style) 
    {
        super(transform, key, label, style);

        this.nonTransformedShape = new Ellipse2D.Double(ellipse.getX(), ellipse.getY(), ellipse.getWidth(), ellipse.getHeight());    
    }

    public ROIEllipse(ROIEllipse that)
    {
        this(that, that.getStyle());
    }

    public ROIEllipse(ROIEllipse that, ROIStyle style)
    {
        super(that, style);     
        this.nonTransformedShape = new Ellipse2D.Double(that.nonTransformedShape.getX(), that.nonTransformedShape.getY(), that.nonTransformedShape.getWidth(), that.nonTransformedShape.getHeight());
    }

    public ROIEllipse(ROIEllipse that, ROIStyle style, Integer key, String label)
    {
        super(that, style, key, label);     
        this.nonTransformedShape = new Ellipse2D.Double(that.nonTransformedShape.getX(), that.nonTransformedShape.getY(), that.nonTransformedShape.getWidth(), that.nonTransformedShape.getHeight());
    }

    @Override
    public ShapeFactors getShapeFactors(double flatness)
    {
        return  isTransformed() ? ShapeFactors.getShapeFactorsForArbitraryShape(getROIShape(), flatness) : ShapeFactors.getShapeFactors(nonTransformedShape);
    }

    @Override
    public ROIEllipse copy()
    {
        return new ROIEllipse(this);
    }

    @Override
    public ROIEllipse copy(ROIStyle style)
    {
        return new ROIEllipse(this, style);
    }

    @Override
    public ROIEllipse copy(ROIStyle style, Integer key, String label)
    {
        return new ROIEllipse(this, style, key, label);
    }

    @Override
    public ROIDrawable getConvexHull(Integer key, String label) 
    {
        return copy(getStyle(), key, label);
    }

    @Override
    public boolean isCorrectlyConstructed()
    {
        return true;
    }

    @Override
    public RectangularShape getNonTransformedModifiableShape() 
    {
        return nonTransformedShape;
    }

    @Override
    public Shape getROIShape() 
    {
        if(isTransformed())
        {
            AffineTransform tr = getTransform();
            return tr.createTransformedShape(nonTransformedShape);
        }

        return nonTransformedShape;
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
    public int getPointsInsideCountUpperBound(ArraySupport2D grid)
    {
        return GridPositionCalculator.getInsidePointCountUpperBound(grid, getROIShape());
    }

    @Override
    public void addPointsInside(ArraySupport2D grid, GridPointRecepient recepient)
    {        
        if(grid instanceof Grid2D)
        {
            addPointsInsideRegularGridOptimized((Grid2D)grid, recepient);
            return;
        }

        if(isTransformed())
        {           
            addPointsInsideTransformed(grid, recepient); 
            return;
        }

        double minY = nonTransformedShape.getMinY();
        double maxY = nonTransformedShape.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        int roiMinRow = Math.max(0, grid.getRowCeiling(minY));
        int roiMaxRow = Math.min(rowCount - 1, grid.getRowFloor(maxY));

        double centerX = nonTransformedShape.getCenterX();
        double centerY = nonTransformedShape.getCenterY();

        double a = nonTransformedShape.getWidth()/2.;
        double b = nonTransformedShape.getHeight()/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i) - centerY;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerX;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerX;

            int minColumn = Math.max(0, grid.getColumnCeiling(xMinTr));
            int maxColumn = Math.min(columnCount -1, grid.getColumnFloor(xMaxTr));

            recepient.addBlock(i, i + 1, minColumn, maxColumn + 1);
        }
    }

    private void addPointsInsideRegularGridOptimized(Grid2D grid, GridPointRecepient recepient)
    {
        if(isTransformed())
        {           
            addPointsInsideTransformed(grid, recepient); 
            return;
        }

        double xIncrement = grid.getXIncrement();
        double yIncrement = grid.getYIncrement();

        double widthNew = nonTransformedShape.getWidth()/xIncrement;
        double heightNew = nonTransformedShape.getHeight()/yIncrement;

        double centerXNew = (nonTransformedShape.getCenterX() - grid.getXOrigin())/xIncrement;
        double centerYNew = (nonTransformedShape.getCenterY() - grid.getYOrigin())/yIncrement;

        Ellipse2D ellipseInGridCoords = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = ellipseInGridCoords.getMinY();
        double maxY = ellipseInGridCoords.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        int roiMinRow = Math.max(0, (int)Math.ceil(minY));
        int roiMaxRow = Math.min(rowCount - 1, (int)Math.floor(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = i - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            int minColumn = Math.max(0, (int)Math.ceil(xMinTr));
            int maxColumn = Math.min(columnCount -1, (int)Math.floor(xMaxTr));

            recepient.addBlock(i, i + 1, minColumn, maxColumn + 1);
        }
    }

    private void addPointsInsideTransformed(ArraySupport2D grid, GridPointRecepient recepient)
    {
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
                int minColumn = Math.max(0, grid.getColumnCeiling(xMin));
                int maxColumn = Math.min(columnCount - 1, grid.getColumnFloor(xMax));

                recepient.addBlock(i, i + 1, minColumn, maxColumn + 1);
            }
        }
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

        double widthNew = nonTransformedShape.getWidth();
        double heightNew = nonTransformedShape.getHeight();

        double centerXNew = nonTransformedShape.getCenterX();
        double centerYNew = nonTransformedShape.getCenterY();

        double minY = nonTransformedShape.getMinY();
        double maxY = nonTransformedShape.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int roiMinRow = Math.max(0, grid.getRowCeiling(minY));
        int roiMaxRow = Math.min(rowCount - 1, grid.getRowFloor(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i) - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            int minColumn = Math.max(0, grid.getColumnCeiling(xMinTr));
            int maxColumn = Math.min(columnCount - 1, grid.getColumnFloor(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            recepient.addBlock(i, i + 1, 0, Math.min(columnCount, minColumn));
            recepient.addBlock(i, i + 1, Math.max(0, maxColumn + 1), columnCount);
        }

        //adds points that are outside bounding box

        recepient.addBlock(0, Math.min(roiMinRow, rowCount), 0, columnCount);
        recepient.addBlock(Math.max(roiMaxRow + 1, 0), rowCount, 0, columnCount);
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

        double widthNew = nonTransformedShape.getWidth()/xIncrement;
        double heightNew = nonTransformedShape.getHeight()/yIncrement;

        double centerXNew = (nonTransformedShape.getCenterX() - grid.getXOrigin())/xIncrement;
        double centerYNew = (nonTransformedShape.getCenterY() - grid.getYOrigin())/yIncrement;

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int roiMinRow = Math.max(0, (int)Math.ceil(minY));
        int roiMaxRow = Math.min(rowCount - 1, (int)Math.floor(maxY));


        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = i - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            int minColumn = Math.max(0, (int)Math.ceil(xMinTr));
            int maxColumn = Math.min(columnCount - 1, (int)Math.floor(xMaxTr));

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area

            recepient.addBlock(i, i + 1, 0, Math.min(columnCount, minColumn));
            recepient.addBlock(i, i + 1, Math.max(0, maxColumn + 1), columnCount);
        }

        //adds points that are outside bounding box

        recepient.addBlock(0, Math.min(roiMinRow, rowCount), 0, columnCount);
        recepient.addBlock(Math.max(roiMaxRow + 1, 0), rowCount, 0, columnCount);
    }


    public void addPointsOutsideTransformed(ArraySupport2D grid, GridPointRecepient recepient)
    {
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
                int minColumnInside = Math.max(0, grid.getColumnCeilingWithinBounds(xMin));
                int maxColumnInside = Math.min(columnCount - 1, grid.getColumnFloorWithinBounds(xMax));

                //these mins and max in for loops guard against exceptions when ROIs are completely
                //outside data area

                recepient.addBlock(i, i + 1, 0, Math.min(columnCount, minColumnInside));
                recepient.addBlock(i, i + 1, Math.max(0, maxColumnInside + 1), columnCount);
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
    public void dividePoints(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        if(grid instanceof Grid2D)
        {
            dividePointsRegularGridOptimized((Grid2D)grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);
            return;
        }

        if(isTransformed())
        {                      
            dividePointsTransformed(grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);  
            return;
        }

        double widthNew = nonTransformedShape.getWidth();
        double heightNew = nonTransformedShape.getHeight();

        double centerXNew = nonTransformedShape.getCenterX();
        double centerYNew = nonTransformedShape.getCenterY();

        double minY = nonTransformedShape.getMinY();
        double maxY = nonTransformedShape.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int roiMinRow = Math.max(imageMinRow, grid.getRowCeiling(minY));
        int roiMaxRow = Math.min(imageMaxRow, grid.getRowFloor(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = grid.getY(i) - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            int minColumn = Math.max(imageMinColumn, grid.getColumnCeiling(xMinTr));
            int maxColumn = Math.min(imageMaxColumn, grid.getColumnFloor(xMaxTr));

            for(int j = minColumn; j <= maxColumn; j++ )
            {
                recepient.addPointInside(i, j);
            }

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area
            for(int j = imageMinColumn; j < Math.min(imageMaxColumn, minColumn); j++ )
            {
                recepient.addPointOutside(i, j);
            }

            for(int j = Math.max(imageMinColumn, maxColumn + 1); j <= imageMaxColumn; j++ )
            {
                recepient.addPointOutside(i, j);
            }
        }

        //adds points that are outside bounding box

        for(int i = imageMinRow; i<Math.min(roiMinRow, imageMaxRow); i++)
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

    private void dividePointsRegularGridOptimized(Grid2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {
        if(isTransformed())
        {                      
            dividePointsTransformed(grid, imageMinRow, imageMaxRow, imageMinColumn, imageMaxColumn, recepient);  
            return;
        }

        double xIncrement = grid.getXIncrement();
        double yIncrement = grid.getYIncrement();

        double widthNew = nonTransformedShape.getWidth()/xIncrement;
        double heightNew = nonTransformedShape.getHeight()/yIncrement;

        double centerXNew = (nonTransformedShape.getCenterX() - grid.getXOrigin())/xIncrement;
        double centerYNew = (nonTransformedShape.getCenterY() - grid.getYOrigin())/yIncrement;

        Ellipse2D transformed = new Ellipse2D.Double(centerXNew - 0.5*widthNew, centerYNew - 0.5*heightNew, widthNew, heightNew);

        double minY = transformed.getMinY();
        double maxY = transformed.getMaxY();

        //the minimal and maximal indices of rows and columns INSIDE the ROI

        int roiMinRow = Math.max(imageMinRow, (int)Math.ceil(minY));
        int roiMaxRow = Math.min(imageMaxRow, (int)Math.floor(maxY));

        double a = widthNew/2.;
        double b = heightNew/2.;
        double b2 = b*b;

        for(int i = roiMinRow; i<= roiMaxRow; i++)
        {
            double y = i - centerYNew;
            double xMinTr = -a*Math.sqrt(1 - y*y/b2) + centerXNew;
            double xMaxTr = a*Math.sqrt(1 - y*y/b2) + centerXNew;

            int minColumn = Math.max(imageMinColumn, (int)Math.ceil(xMinTr));
            int maxColumn = Math.min(imageMaxColumn, (int)Math.floor(xMaxTr));

            for(int j = minColumn; j <= maxColumn; j++ )
            {
                recepient.addPointInside(i, j);
            }

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area
            for(int j = imageMinColumn; j < Math.min(imageMaxColumn, minColumn); j++ )
            {
                recepient.addPointOutside(i, j);
            }

            for(int j = Math.max(imageMinColumn, maxColumn + 1); j <= imageMaxColumn; j++ )
            {
                recepient.addPointOutside(i, j);
            }
        }

        //adds points that are outside bounding box

        for(int i = imageMinRow; i<Math.min(roiMinRow, imageMaxRow); i++)
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

    private void dividePointsTransformed(ArraySupport2D grid, int imageMinRow, int imageMaxRow, int imageMinColumn, int imageMaxColumn, GridBiPointRecepient recepient)
    {                            
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

        Rectangle2D bounds = getROIShape().getBounds2D();

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

            int minColumn = xsFound ? Math.max(imageMinColumn, grid.getColumnCeilingWithinBounds(xMin)) : imageMaxColumn;
            int maxColumn = xsFound ? Math.min(imageMaxColumn, grid.getColumnFloorWithinBounds(xMax)) : imageMinColumn - 1;

            for(int j = minColumn; j <= maxColumn; j++)
            {
                recepient.addPointInside(i, j);
            }

            //these mins and max in for loops guard against exceptions when ROIs are completely
            //outside data area
            for(int j = imageMinColumn; j < Math.min(imageMaxColumn, minColumn); j++ )
            {
                recepient.addPointOutside(i, j);
            }

            for(int j = Math.max(imageMinColumn, maxColumn + 1); j <= imageMaxColumn; j++ )
            {
                recepient.addPointOutside(i, j);
            }
        }

        //adds points that are outside bounding box

        for(int i = imageMinRow; i<Math.min(roiMinRow, imageMaxRow); i++)
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
    public ROIProxy getProxy()
    {
        return new ROIEllipseProxy(nonTransformedShape, getTransform(), getCustomLabel(), isFinished());
    }

    public static class ROIEllipseProxy implements ROIProxy
    {
        private static final long serialVersionUID = 1L;

        private final Ellipse2D roiShape;
        private final AffineTransform transform;
        private final String customLabel;
        private final boolean finished;

        public ROIEllipseProxy(Ellipse2D roiShape, AffineTransform transform, String customLabel, boolean finished)
        {
            this.roiShape = roiShape;
            this.transform = transform;
            this.finished = finished;
            this.customLabel = customLabel;
        }

        @Override
        public ROIEllipse recreateOriginalObject(ROIStyle roiStyle, Integer key) 
        {
            String label = (customLabel != null) ? customLabel : key.toString();
            ROIEllipse roi =  new ROIEllipse(roiShape, transform, key, label, roiStyle);
            roi.setFinished(finished);

            return roi;
        }     
    }
}
