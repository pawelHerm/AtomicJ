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

package atomicJ.data;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.BivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.BicubicSplineInterpolator;
import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.units.Quantity;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.resources.CrossSectionSettings;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;

public abstract class AbstractArrayChannel2DData <E extends ArraySupport2D> implements ArrayChannel2DData
{
    private final double[][] gridData;
    private final E grid;
    private final Quantity zQuantity;
    private final Range zRange;
    private Range automaticRange;
    private final ChannelDomainIdentifier domainIdentifier;

    private SoftReference<BivariateRealFunction> interpolationFunction;

    public AbstractArrayChannel2DData(double[][] gridData, E grid, Quantity zQuantity)
    {
        this.grid = grid;
        this.gridData = gridData;
        this.zQuantity = zQuantity;
        this.zRange = ArrayUtilities.getBoundedRange(gridData);
        this.domainIdentifier = new ChannelDomainIdentifier(grid.getGridDensity(), grid);
    }

    public AbstractArrayChannel2DData(AbstractArrayChannel2DData<E> that)
    {
        this.grid = that.grid;
        this.gridData = ArrayUtilities.deepCopy(that.gridData);
        this.zQuantity = that.zQuantity;
        this.zRange = that.zRange;
        this.automaticRange = that.automaticRange;
        this.domainIdentifier = that.domainIdentifier;
    }

    @Override
    public Quantity getXQuantity()
    {
        return grid.getXQuantity();
    }

    @Override
    public Quantity getYQuantity()
    {
        return grid.getYQuantity();
    }

    @Override
    public Quantity getZQuantity()
    {
        return zQuantity;
    }

    @Override
    public E getGrid()
    {
        return grid;
    }

    @Override
    public double[][] getData() 
    {
        return gridData;
    }

    @Override
    public double[][] getDataCopy()
    {
        double[][] data = ArrayUtilities.deepCopy(gridData);
        return data;
    }

    @Override
    public double[][] getPoints() 
    {
        double[] nodeXs = grid.getNodeXs();
        double[] nodeYs = grid.getNodeYs();

        int columnCount = grid.getColumnCount();
        int rowCount = grid.getRowCount();

        double[][] data = new double[rowCount*columnCount][3];

        for(int i = 0, index = 0; i<columnCount; i++)
        {
            double x = nodeXs[i];

            for(int j = 0; j<rowCount; j++,index++)
            {
                double y = nodeYs[j];
                double z = gridData[j][i];

                data[index] = new double[] {x, y, z};
            }
        }

        return data;
    }

    @Override
    public double[][] getPointsCopy()
    {
        return getPoints();
    }

    @Override
    public boolean isEmpty()
    {
        return grid.isEmpty();
    }

    @Override
    public int getItemCount()
    {
        return grid.getItemCount();
    }

    @Override
    public double getDataDensity()
    {
        return grid.getGridDensity();
    }

    @Override
    public double getXDataDensity() 
    {
        return grid.getXDataDensity();
    }

    @Override
    public double getYDataDensity() 
    {
        return grid.getYDataDensity();
    }

    @Override
    public boolean isWithinDataDomain(Point2D dataPoint) 
    {
        return grid.isWithinGridArea(dataPoint.getX(), dataPoint.getY());
    }

    @Override
    public Rectangle2D getDataArea()
    {
        return grid.getDataArea();        
    }

    @Override
    public double getZ(int item)
    {
        int columnCount = grid.getColumnCount();

        int column = item % columnCount;
        int row = item / columnCount;

        return gridData[row][column];
    }

    @Override
    public double getZ(int row, int column) 
    {
        return gridData[row][column];
    }

    @Override
    public double getValue(Point2D dataPoint) 
    {
        int row = grid.getRow(dataPoint);
        int column = grid.getColumn(dataPoint);

        double value = Double.NaN;

        if (row < grid.getRowCount() && column < grid.getColumnCount())
        {
            value = gridData[row][column];
        }

        return value;
    }

    @Override
    public double getX(int item) 
    {
        int columnCount = grid.getColumnCount();

        int column = item % columnCount;
        return grid.getX(column);
    }

    @Override
    public double getY(int item) 
    {
        int columnCount = grid.getColumnCount();        
        int row = item / columnCount;

        return grid.getY(row);
    }

    @Override
    public Range getZRange() 
    {
        return zRange;
    }

    @Override
    public Range getXRange()
    {
        return new Range(grid.getXMinimum(), grid.getXMaximum());
    }

    @Override
    public Range getYRange()
    {
        return new Range(grid.getYMinimum(), grid.getYMaximum());
    }

    @Override
    public Range getAutomaticZRange()
    {
        this.automaticRange = (automaticRange != null) ? automaticRange : DescriptiveStatistics.getMADBasedRange(ArrayUtilities.flatten(gridData), 3.5);
        return automaticRange;
    }

    @Override
    public double[] getPoint(int item)
    {
        int columnCount = grid.getColumnCount();

        int column = item % columnCount;
        int row = item / columnCount;

        return new double[] {grid.getX(column), grid.getY(row), gridData[row][column]};
    } 

    @Override
    public double[][] getXYZView()
    {
        double[] xNodes = grid.getNodeXs();
        double[] yNodes = grid.getNodeYs();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();
        int count = rowCount * columnCount;

        double[] xs = new double[count];
        double[] ys = new double[count];
        double[] zs = new double[count];

        for (int i = 0, index = 0; i < rowCount; i++) 
        {
            double y = yNodes[i];
            double[] gridRow = gridData[i];

            for (int j = 0; j < columnCount; j++, index++) 
            {
                xs[index] = xNodes[j];
                ys[index] = y;
                zs[index] = gridRow[j];
            }
        }

        double[][] xyzData = new double[][] {xs, ys, zs}; 

        return xyzData;
    }

    @Override
    public double[] getXCoordinates()
    {
        return getXCoordinatesCopy();
    }

    @Override
    public double[] getYCoordinates()
    {
        return getYCoordinatesCopy();
    }

    @Override
    public double[] getZCoordinates()
    {
        return getZCoordinatesCopy();
    }

    @Override
    public double[] getXCoordinatesCopy()
    {
        double[] xNodes = grid.getNodeXs();
        int columnCount = grid.getRowCount();
        int count = grid.getItemCount();

        double[] xs = new double[count];

        for (int i = 0; i < count; i++) 
        {
            double x = xNodes[i % columnCount];
            xs[i] = x;
        }

        return xs;        
    }

    @Override
    public double[] getYCoordinatesCopy()
    {
        double[] yNodes = grid.getNodeYs();
        int columnCount = grid.getRowCount();
        int count = grid.getItemCount();

        double[] ys = new double[count];

        for (int i = 0; i < count; i++) 
        {
            double y = yNodes[i / columnCount];
            ys[i] = y;
        }

        return ys;        
    }

    @Override
    public double[] getZCoordinatesCopy() 
    {
        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[] zs = new double[rowCount * columnCount];

        for (int i = 0, index = 0; i < rowCount; i++) 
        {
            double[] row = gridData[i];
            for (int j = 0; j < columnCount; j++) 
            {
                zs[index++] = row[j];                
            }
        }

        return zs;
    } 

    @Override
    public double[] getRow(int rowIndex)
    {
        return gridData[rowIndex];
    }

    @Override
    public double[] getColumn(int columnIndex)
    {
        int rowCount = grid.getRowCount();

        double[] column = new double[rowCount];

        for(int i = 0; i<rowCount; i++)
        {
            column[i] = gridData[i][columnIndex];
        }

        return column;
    }

    @Override
    public BivariateRealFunction getBicubicSplineInterpolationFunction() 
    {
        if(interpolationFunction == null)
        {
            buildBicubicSplineInterpolationFunction();
        }

        return interpolationFunction.get();
    }

    @Override
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod) 
    {
        boolean necessary = false;

        if(InterpolationMethod2D.BICUBIC_SPLINE.equals(interpolationMethod))
        {
            necessary = (interpolationFunction == null);
        }

        return necessary;
    }

    @Override
    public void prepareForInterpolationIfNecessary(InterpolationMethod2D interpolation)
    {
        if(InterpolationMethod2D.BICUBIC_SPLINE.equals(interpolation) && interpolationFunction == null)
        {
            buildBicubicSplineInterpolationFunction();
        }
    }

    private void buildBicubicSplineInterpolationFunction() 
    {                 
        double[][] transposedData = ArrayUtilities.transpose(gridData,grid.getRowCount(), grid.getColumnCount());

        this.interpolationFunction = null;

        if (transposedData != null) 
        {
            BicubicSplineInterpolator interpolator = new BicubicSplineInterpolator();

            try 
            {
                this.interpolationFunction = new SoftReference<>(interpolator.interpolate(grid.getNodeXs(), grid.getNodeYs(), transposedData));
            } 
            catch (IllegalArgumentException | MathException e) 
            {
                e.printStackTrace();
            }
        } 
    }

    @Override
    public double[][] getCrossSection(Shape profile, CrossSectionSettings settings) 
    {                   
        InterpolationMethod2D interpolationMethod = settings.getInterpolationMethod();
        int pointCount = settings.getPointCount();

        double[][] profilePoints = DistanceShapeFactors.getXYDTriples(profile, pointCount);

        double[][] crossSection = interpolationMethod.getCrossSection(this, profilePoints);

        return crossSection;
    }

    @Override
    public  QuantitativeSample getROISample(ROI roi, ROIRelativePosition position, String sampleTag)
    {       
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getZSample("");
        }

        Quantity quantity = getZQuantity();

        IdentityTag idTag = roi.getIdentityTag();

        double[] dataArray = getROIData(roi, position);                  

        String sampleKey = ROIRelativePosition.INSIDE.equals(position) ? idTag.getKey() + sampleTag : "Outside" + idTag.getKey() + sampleTag;
        QuantitativeSample sample  = new StandardSample(dataArray, sampleKey, quantity, idTag.getLabel(), sampleTag);

        return sample;
    }

    @Override
    public double[] getROIData(ROI roi, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            getZCoordinatesCopy();
        }

        int sizeBound = roi.getPointCountUpperBound(grid, position);

        final double[] valuesInSample = new double[sizeBound];

        class GridPointRecepientCustom implements GridPointRecepient 
        { 
            private int count = 0;

            @Override
            public void addPoint(int row, int column) {                 
                valuesInSample[count++] = gridData[row][column];                    
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] channelRow = gridData[i];
                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        valuesInSample[count++] = channelRow[j];                    
                    }
                }
            }
        };

        GridPointRecepientCustom receipient = new GridPointRecepientCustom();
        roi.addPoints(grid, position, receipient);

        int count = receipient.count;
        boolean filled = (valuesInSample.length == count);

        double[] dataArray = filled ? valuesInSample : Arrays.copyOf(valuesInSample, count); 

        return dataArray;
    }


    @Override
    public Map<Object, QuantitativeSample> getROISamples(Collection<ROI> rois, ROIRelativePosition position, String sampleKeyTail)
    {
        Map<Object, QuantitativeSample> samples = new LinkedHashMap<>();

        for(ROI roi : rois)
        {
            QuantitativeSample sample = getROISample(roi, position, sampleKeyTail);            
            String key = sample.getKey();           
            samples.put(key, sample);
        }

        return samples;
    }

    @Override
    public QuantitativeSample getXSample() 
    {
        QuantitativeSample sample = new StandardSample(getXCoordinatesCopy(), grid.getXQuantity());
        return sample;
    }

    @Override
    public QuantitativeSample getYSample() 
    {
        QuantitativeSample sample = new StandardSample(getYCoordinatesCopy(), grid.getYQuantity());
        return sample;
    }

    @Override
    public QuantitativeSample getZSample(String nameTag) 
    {
        QuantitativeSample sample = new StandardSample(getZCoordinatesCopy(), zQuantity.getName(), zQuantity, zQuantity.getName(), nameTag);
        return sample;
    }

    @Override
    public double[] getProfileValues(Shape profile, CrossSectionSettings settings)
    {        
        int n = settings.getPointCount();
        InterpolationMethod2D interpolationMethod = settings.getInterpolationMethod();

        double[] values = interpolationMethod.getProfileValues(this, profile, n);

        return values;
    }

    @Override
    public ChannelDomainIdentifier getDomainIdentifier() 
    {
        return domainIdentifier;
    }
}

