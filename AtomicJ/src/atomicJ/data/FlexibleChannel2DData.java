
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

import edu.mines.jtk.interp.SibsonInterpolator2;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.units.Quantity;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.resources.CrossSectionSettings;
import atomicJ.sources.IdentityTag;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.GeometryUtilities;

public class FlexibleChannel2DData implements Channel2DData
{	    
    private final Quantity xQuantity;
    private final Quantity yQuantity;
    private final Quantity zQuantity;

    private final ChannelDomainIdentifier dataDomain;
    private final double[][] data;
    private Range xRange;
    private Range yRange;
    private final Range zRange;
    private final Range automaticRange;

    private SoftReference<SibsonInterpolator2> sibsonInterpolator;

    /*
     * THE FIRST ROW IN DATA MATRIX SHOULD CONSIST OF X COORDINATES, THE SECOND OF Y CORDINATES, AND
     * THE THIRD OF Z COORDINATES*/

    public FlexibleChannel2DData(double[][] data, ChannelDomainIdentifier dataDensity,Quantity xQuantity, Quantity yQuantity, Quantity zQuantity)
    {   
        this.data = data;
        this.xRange = ArrayUtilities.getBoundedRange(data[1]);
        this.yRange = ArrayUtilities.getBoundedRange(data[1]);
        this.zRange = ArrayUtilities.getBoundedRange(data[2]);
        this.automaticRange = DescriptiveStatistics.getMADBasedRange(data[2], 3);

        this.dataDomain = dataDensity;
        this.xQuantity = xQuantity;
        this.yQuantity = yQuantity;
        this.zQuantity = zQuantity;
    }

    public FlexibleChannel2DData(FlexibleChannel2DData that)
    {       
        this.data = ArrayUtilities.deepCopy(that.data);
        this.zRange = ArrayUtilities.getBoundedRange(this.data[2]);
        this.automaticRange = DescriptiveStatistics.getMADBasedRange(this.data[2], 3);

        this.dataDomain = that.dataDomain;
        this.xQuantity = that.xQuantity;
        this.yQuantity = that.yQuantity;
        this.zQuantity = that.zQuantity;
    }

    @Override
    public FlexibleChannel2DData getCopy()
    {     
        return new FlexibleChannel2DData(this);
    }

    @Override
    public ChannelDomainIdentifier getDomainIdentifier()
    {
        return dataDomain;
    }

    @Override
    public double getDataDensity()
    {
        return dataDomain.getDataDensity();
    }

    @Override
    public double getX(int item)
    {
        return data[0][item];
    }

    @Override
    public double getY(int item)
    {
        return data[1][item];
    }

    @Override
    public double getZ(int item)
    {
        return data[2][item];
    }

    @Override
    public double[] getPoint(int item) 
    {
        return new double[] {data[0][item], data[1][item], data[2][item]};
    }

    @Override
    public int getItemCount()
    {
        return data[0].length;
    }

    @Override
    public boolean isEmpty()
    {
        boolean empty = data[0].length == 0;
        return empty;
    }

    public double[][] getData()
    {
        return data;
    }

    @Override
    public QuantitativeSample getXSample() 
    {
        QuantitativeSample sample = new StandardSample(getXCoordinatesCopy(), xQuantity);
        return sample;
    }

    @Override
    public QuantitativeSample getYSample() 
    {
        QuantitativeSample sample = new StandardSample(getYCoordinatesCopy(), yQuantity);
        return sample;
    }


    @Override
    public QuantitativeSample getZSample(String nameTag)
    {
        QuantitativeSample sample = new StandardSample(getZCoordinatesCopy(), zQuantity.getName(), zQuantity, zQuantity.getName(), nameTag);
        return sample;
    }

    @Override
    public Quantity getXQuantity()
    {
        return xQuantity;
    }

    @Override
    public Quantity getYQuantity()
    {
        return yQuantity;
    }

    @Override
    public Quantity getZQuantity() 
    {
        return zQuantity;
    }

    @Override
    public double[][] getPoints()
    {
        int n = data[0].length;

        double[][] points =  new double[n][];

        double[] xs = data[0];
        double[] ys = data[1];
        double[] zs = data[2];

        for(int i = 0; i<n; i++)
        {
            points[i] = new double[] {xs[i],ys[i],zs[i]};
        }

        return points;
    }

    @Override
    public double[][] getPointsCopy() 
    {
        return getPoints();
    }

    @Override
    public double[][] getXYZView() 
    {
        return ArrayUtilities.deepCopy(data);
    }

    @Override
    public double[] getXCoordinates()
    {
        return data[0];
    }

    @Override
    public double[] getYCoordinates()
    {
        return data[1];
    }

    @Override
    public double[] getZCoordinates()
    {
        return data[2];
    }

    @Override
    public double[] getXCoordinatesCopy()
    {
        double[] xs = data[0];        
        double[] xsCopy = Arrays.copyOf(xs, xs.length);

        return xsCopy;
    }

    @Override
    public double[] getYCoordinatesCopy()
    {
        double[] ys = data[1];        
        double[] ysCopy = Arrays.copyOf(ys, ys.length);

        return ysCopy;
    }

    @Override
    public double[] getZCoordinatesCopy()
    {
        double[] zs = data[2];        
        double[] zsCopy = Arrays.copyOf(zs, zs.length);

        return zsCopy;
    }

    @Override
    public Rectangle2D getDataArea() 
    {     
        double width = xRange.getLength();
        double height = yRange.getLength();

        Rectangle2D dataArea = new Rectangle2D.Double(xRange.getLowerBound(), yRange.getLowerBound(), width, height);
        return dataArea;
    }

    @Override
    public boolean isWithinDataDomain(Point2D dataPoint) 
    {
        double x = dataPoint.getX();
        double y = dataPoint.getY();
        boolean withinDomain = xRange.contains(x) && yRange.contains(y);

        return withinDomain;
    }

    @Override
    public Map<Object, QuantitativeSample> getROISamples(Collection<ROI> rois, ROIRelativePosition position, String sampleKeyTail) {

        Map<Object, QuantitativeSample> samples = new LinkedHashMap<>();

        for(ROI roi: rois)
        {
            Object roiKey = roi.getKey();
            QuantitativeSample sample = getROISample(roi, position, sampleKeyTail);
            samples.put(roiKey, sample);
        }

        return samples;	
    }

    @Override
    public QuantitativeSample getROISample(ROI roi, ROIRelativePosition position, String sampleTag) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return getZSample("");
        }

        double[] dataArray = getROIData(roi, position);       

        IdentityTag idTag = roi.getIdentityTag();
        String sampleKey = ROIRelativePosition.INSIDE.equals(position) ? idTag.getKey() + sampleTag : "Outside" + idTag.getKey() + sampleTag;
        QuantitativeSample sample  = new StandardSample(dataArray, sampleKey, zQuantity);

        return sample; 
    }

    @Override
    public double[] getROIData(ROI roi, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            getZCoordinatesCopy();
        }

        TDoubleList sampleData = new TDoubleArrayList();

        double[] xs = data[0];
        double[] ys = data[1];
        double[] zs = data[2];

        int count = xs.length;

        Shape roiShape = roi.getROIShape();

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(int j = 0; j<count; j++)
            {
                if(roiShape.contains(xs[j], ys[j]))
                {                   
                    sampleData.add(zs[j]);                
                }   
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(int j = 0; j<count; j++)
            {
                if(!roiShape.contains(xs[j], ys[j]))
                {                   
                    sampleData.add(zs[j]);                
                }   
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown ROIRelativePosition " + position);
        }

        double[] dataArray = sampleData.toArray();

        return dataArray;
    }

    @Override
    public Range getXRange()
    {
        return ArrayUtilities.getBoundedRange(data[0]);
    }

    @Override
    public Range getYRange()
    {
        return ArrayUtilities.getBoundedRange(data[1]);
    }

    @Override
    public Range getZRange() 
    {
        return zRange;
    } 

    @Override
    public Range getAutomaticZRange() 
    {
        return automaticRange;
    }

    @Override
    public double getXDataDensity() 
    {
        return getDataDensity();
    }

    @Override
    public double getYDataDensity()
    {
        return getDataDensity();
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
    public double getValue(Point2D dataPoint)
    {
        SibsonInterpolator2 interpolator = getSibsonInterpolator();
        return interpolator.interpolate((float)dataPoint.getX(), (float)dataPoint.getY());
    }


    @Override
    public double[] getProfileValues(Shape profile,
            CrossSectionSettings settings) 
    {
        int n = settings.getPointCount();
        InterpolationMethod2D interpolationMethod = settings.getInterpolationMethod();

        double[] values = interpolationMethod.getProfileValues(this, profile, n);

        return values;
    }

    @Override
    public Grid2D getDefaultGriddingGrid() 
    {
        int itemCount = data[0].length;

        int sideCount = (int)Math.ceil(Math.sqrt(itemCount));
        double xOrigin = xRange.getLowerBound();
        double yOrigin = yRange.getLowerBound();        

        double xIncrement = xRange.getLength()/(sideCount - 1.);
        double yIncrement = yRange.getLength()/(sideCount - 1.);

        Grid2D grid = new Grid2D(xIncrement, yIncrement, xOrigin, yOrigin, sideCount, sideCount, xQuantity, yQuantity);

        return grid;
    }


    @Override
    public GridChannel2DData getDefaultGridding() 
    {
        return getGridding(getDefaultGriddingGrid());
    }

    @Override
    public GridChannel2DData getGridding(Grid2D gridNew)
    {

        InterpolationMethod2D interpolationMethod = InterpolationMethod2D.SIBSON;

        int rowCount = gridNew.getRowCount();
        int columnCount = gridNew.getColumnCount();

        double[][] griddedData = interpolationMethod.getGriddedData(this, rowCount, columnCount);

        double xOrigin = xRange.getLowerBound();
        double yOrigin = yRange.getLowerBound();        

        double xIncrement = xRange.getLength()/(columnCount - 1.);
        double yIncrement = yRange.getLength()/(rowCount - 1.);

        Grid2D grid = new Grid2D(xIncrement, yIncrement, xOrigin, yOrigin, rowCount, columnCount, xQuantity, yQuantity);

        GridChannel2DData griddedChannelData = new GridChannel2DData(griddedData, grid, zQuantity);

        return griddedChannelData;
    }
    @Override
    public boolean isInterpolationPreparationNecessary(InterpolationMethod2D interpolationMethod) 
    {
        boolean necessary = false;

        if(InterpolationMethod2D.SIBSON.equals(interpolationMethod))
        {
            necessary = (sibsonInterpolator == null);
        }

        return necessary;
    }

    @Override
    public void prepareForInterpolationIfNecessary(InterpolationMethod2D interpolation)
    {
        if(InterpolationMethod2D.BICUBIC_SPLINE.equals(interpolation) && sibsonInterpolator == null)
        {
            buildSibsonInterpolator();
        }
    }

    private void buildSibsonInterpolator() 
    {         
        float[] xCoords = ArrayUtilities.toFloat(data[0]);
        float[] yCoords = ArrayUtilities.toFloat(data[1]);
        float[] zCoords = ArrayUtilities.toFloat(data[2]);

        SibsonInterpolator2 sibsonInterpolator = new SibsonInterpolator2(zCoords, xCoords, yCoords);

        float xMin = (float) xRange.getLowerBound();
        float yMin = (float) yRange.getLowerBound();        

        float xMax = (float) xRange.getUpperBound();
        float yMax = (float) yRange.getUpperBound();

        sibsonInterpolator.setBounds(xMin, xMax, yMin, yMax);
        sibsonInterpolator.setNullValue(Float.NaN);

        this.sibsonInterpolator = new SoftReference<>(sibsonInterpolator);
    }

    private SibsonInterpolator2 getSibsonInterpolator() 
    {
        if(sibsonInterpolator == null)
        {
            buildSibsonInterpolator();
        }

        return sibsonInterpolator.get();
    }

    public static double calculateProbingDensityGeometryPoints(List<Point2D> points)
    {                
        return calculateProbingDensityGeometryPoints(points, 1.5);
    }

    public static double calculateProbingDensityGeometryPoints(List<Point2D> points, double factor)
    {                
        int n = points.size() - 1;

        Range xRange = GeometryUtilities.getBoundedXRange(points);
        double xVal = xRange.getLength()/(factor*Math.sqrt(n));

        Range yRange = GeometryUtilities.getBoundedYRange(points);      
        double yVal = yRange.getLength()/(factor*Math.sqrt(n));

        double density = Math.min(xVal, yVal);
        return density;
    }

    public static double calculateProbingDensity(List<double[]> points)
    {
        int n = points.size() - 1;

        Range xRange = ArrayUtilities.getBoundedXRange(points);
        double xVal = xRange.getLength()/(1.5*Math.sqrt(n));

        Range yRange = ArrayUtilities.getBoundedYRange(points);      
        double yVal = yRange.getLength()/(1.5*Math.sqrt(n));

        double density = Math.min(xVal, yVal);
        return density;
    }

    public static double calculateProbingDensity(double[] xCoordinates, double[] yCoordinates)
    {
        int n = xCoordinates.length - 1;

        Range xRange = ArrayUtilities.getBoundedRange(xCoordinates);
        double xVal = xRange.getLength()/(1.5*Math.sqrt(n));

        Range yRange = ArrayUtilities.getBoundedRange(yCoordinates);      
        double yVal = yRange.getLength()/(1.5*Math.sqrt(n));

        double density = Math.min(xVal, yVal);
        return density;
    }
}