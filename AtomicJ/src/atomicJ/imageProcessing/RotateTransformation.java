
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


package atomicJ.imageProcessing;

import java.awt.Shape;

import atomicJ.analysis.InterpolationMethod2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIUtilities;


public class RotateTransformation implements Channel2DDataInROITransformation
{   
    private final InterpolationMethod2D interpolation;
    private final double angle;
    private final double cosAngle;
    private final double sinAngle;
    private final UnitExpression centerX;
    private final UnitExpression centerY;
    private final UnitExpression fillValue;

    public RotateTransformation(InterpolationMethod2D interpolation, double angle, UnitExpression centerX, UnitExpression centerY, UnitExpression fillValue)
    {
        this.interpolation = interpolation;
        this.angle = angle;
        this.cosAngle = Math.cos(angle);
        this.sinAngle = Math.sin(angle);
        this.centerX = centerX;
        this.centerY = centerY;
        this.fillValue = fillValue;
    }

    public double getRotationAngle()
    {
        return angle;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData);
        }

        return transformChannel(channelData);
    }

    private Channel2DData transformChannel(Channel2DData channelData)
    {        
        int count = channelData.getItemCount();

        double[] originalXs = channelData.getXCoordinates();
        double[] originalYs = channelData.getYCoordinates();

        double[] transformedXs = new double[count];
        double[] transformedYs = new double[count];

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        double centerXUnitCorrected = centerX.derive(xUnit).getValue();
        double centerYUnitCorrected = centerY.derive(yUnit).getValue();

        //we could use just one conversion factor, but then after rotations the units on one axis would change
        //e.x. if we used only YtoXConversionFactor, then after rotation both X- and Y-axis would have the same unit as
        //X-axis before rotation
        double YtoXUnitConversionFactor = yUnit.getConversionFactorTo(xUnit);
        double XtoYUnitConversionFactor = xUnit.getConversionFactorTo(yUnit);

        for(int i = 0; i<count; i++)
        {
            double x = originalXs[i];
            double y = originalYs[i];
            transformedXs[i] = centerXUnitCorrected - centerXUnitCorrected*cosAngle + x*cosAngle - YtoXUnitConversionFactor*centerYUnitCorrected*sinAngle + YtoXUnitConversionFactor*y*sinAngle;
            transformedYs[i] = centerYUnitCorrected - centerYUnitCorrected*cosAngle + y*cosAngle + XtoYUnitConversionFactor*centerXUnitCorrected*sinAngle - XtoYUnitConversionFactor*x*sinAngle;
        }

        double[][] dataNew = new double[][] {transformedXs, transformedYs, channelData.getZCoordinatesCopy()};
        ChannelDomainIdentifier dataDomain = new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensity(transformedXs, transformedYs), ChannelDomainIdentifier.getNewDomainKey());

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannel(GridChannel2DData gridChannelData) 
    {
        Grid2D grid = gridChannelData.getGrid();   
        Quantity zQuantity = gridChannelData.getZQuantity();

        PrefixedUnit xUnit = gridChannelData.getXQuantity().getUnit();
        PrefixedUnit yUnit = gridChannelData.getYQuantity().getUnit();

        double incrementRatio = grid.getIncrementRatio();

        double[][] matrix = gridChannelData.getData();

        GridChannel2DData imageInTransformedCoords = new GridChannel2DData(matrix, new Grid2D(1, 1./incrementRatio, 0, 0, grid.getRowCount(), grid.getColumnCount(),grid.getXQuantity(), grid.getYQuantity()), zQuantity);

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        UnitExpression centerXInImageUnits = centerX.derive(xUnit);
        UnitExpression centerYInImageUnits = centerY.derive(yUnit);

        double[][] transformed = new double[rowCount][columnCount];

        double centerXInTransformedCoords = grid.getFractionalColumn(centerXInImageUnits.getValue());
        double centerYInTransformedCoords = grid.getFractionalRow(centerYInImageUnits.getValue())/incrementRatio;

        PrefixedUnit zUnit = zQuantity.getUnit();

        double fillValueScalar = zUnit.isCompatible(fillValue.getUnit()) ? fillValue.derive(zUnit).getValue() : fillValue.getValue();

        for(int i = 0; i<rowCount; i++)
        {
            double factor1 = (centerYInTransformedCoords - i);
            for(int j = 0; j<columnCount; j++)
            {    
                double factor2 = (j - centerXInTransformedCoords);

                double xDest = centerXInTransformedCoords + factor2*cosAngle + factor1*sinAngle;
                double yDest = centerYInTransformedCoords - factor1*cosAngle + factor2*sinAngle;

                double destValue = interpolation.getValue(imageInTransformedCoords, xDest, yDest);

                transformed[i][j] = Double.isNaN(destValue) ? fillValueScalar : destValue;
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData, roi, position);
        }

        return transformChannel(channelData, roi, position);
    }

    private Channel2DData transformChannel(Channel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        int count = channelData.getItemCount();

        double[] originalXs = channelData.getXCoordinates();
        double[] originalYs = channelData.getYCoordinates();

        double[] transformedXs = channelData.getXCoordinatesCopy();
        double[] transformedYs = channelData.getYCoordinatesCopy();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        PrefixedUnit xUnit = xQuantity.getUnit();
        PrefixedUnit yUnit = yQuantity.getUnit();

        double centerXUnitCorrected = centerX.derive(xUnit).getValue();
        double centerYUnitCorrected = centerY.derive(yUnit).getValue();

        Shape roiShape = roi.getROIShape();

        //we could use just one conversion factor, but then after rotations the units on one axis would change
        //e.x. if we used only YtoXConversionFactor, then after rotation both X- and Y-axis would have the same unit as
        //X-axis before rotation
        double YtoXUnitConversionFactor = yUnit.getConversionFactorTo(xUnit);
        double XtoYUnitConversionFactor = xUnit.getConversionFactorTo(yUnit);

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(int i = 0; i<count; i++)
            {
                double x = originalXs[i];
                double y = originalYs[i];
                if(roiShape.contains(x,y))
                {
                    transformedXs[i] = centerXUnitCorrected - centerXUnitCorrected*cosAngle + x*cosAngle - YtoXUnitConversionFactor*centerYUnitCorrected*sinAngle + YtoXUnitConversionFactor*y*sinAngle;
                    transformedYs[i] = centerYUnitCorrected - centerYUnitCorrected*cosAngle + y*cosAngle + XtoYUnitConversionFactor*centerXUnitCorrected*sinAngle - XtoYUnitConversionFactor*x*sinAngle;
                }
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(int i = 0; i<count; i++)
            {
                double x = originalXs[i];
                double y = originalYs[i];
                if(!roiShape.contains(x,y))
                {
                    transformedXs[i] = centerXUnitCorrected - centerXUnitCorrected*cosAngle + x*cosAngle - YtoXUnitConversionFactor*centerYUnitCorrected*sinAngle + YtoXUnitConversionFactor*y*sinAngle;
                    transformedYs[i] = centerYUnitCorrected - centerYUnitCorrected*cosAngle + y*cosAngle + XtoYUnitConversionFactor*centerXUnitCorrected*sinAngle - XtoYUnitConversionFactor*x*sinAngle;
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown ROIRelativePosition " + position);
        }

        double[][] dataNew = new double[][] {transformedXs, transformedYs, channelData.getZCoordinatesCopy()};
        ChannelDomainIdentifier dataDomain = new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensity(transformedXs, transformedYs), ChannelDomainIdentifier.getNewDomainKey());

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannel(GridChannel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        Grid2D grid = channelData.getGrid();     

        PrefixedUnit xUnit = channelData.getXQuantity().getUnit();
        PrefixedUnit yUnit = channelData.getYQuantity().getUnit();

        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        UnitExpression centerXInImageUnits = centerX.derive(xUnit);
        UnitExpression centerYInImageUnits = centerY.derive(yUnit);

        double[][] transformed = new double[rowCount][columnCount];

        Quantity zQuantity = channelData.getZQuantity();
        PrefixedUnit zUnit = zQuantity.getUnit();

        double fillValueScalar = zUnit.isCompatible(fillValue.getUnit()) ? fillValue.derive(zUnit).getValue() : fillValue.getValue();

        double centerXUnitCorrected = centerXInImageUnits.getValue();
        double centerYUnitCorrected = centerYInImageUnits.getValue();

        ROI rotatedROI = roi.getRotatedCopy(-angle, centerXUnitCorrected, centerYUnitCorrected);

        boolean[][] insideOriginalROI = ROIUtilities.getInsidnessArray(roi, grid);
        boolean[][] insideRotatedROI = ROIUtilities.getInsidnessArray(rotatedROI, grid);

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(int i = 0; i<rowCount; i++)
            {
                double factor1 = (centerYUnitCorrected - grid.getY(i));

                for(int j = 0; j<columnCount; j++)
                {    
                    boolean insideRotated = insideRotatedROI[i][j];
                    boolean insideOriginal = insideOriginalROI[i][j];

                    if(!insideRotated && !insideOriginal)
                    {
                        transformed[i][j] = matrix[i][j];
                        continue;
                    }

                    if(insideRotated)
                    {
                        double factor2 = (grid.getX(j) - centerXUnitCorrected);

                        double xDest = centerXUnitCorrected + factor2*cosAngle + factor1*sinAngle;
                        double yDest = centerYUnitCorrected - factor1*cosAngle + factor2*sinAngle;

                        double destValue = interpolation.getValue(channelData, xDest, yDest);


                        transformed[i][j] = Double.isNaN(destValue) ? fillValueScalar : destValue;
                    }
                    else
                    {
                        transformed[i][j] = fillValueScalar;
                    }
                }
            }

        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(int i = 0; i<rowCount; i++)
            {
                double factor1 = (centerYUnitCorrected - grid.getY(i));

                for(int j = 0; j<columnCount; j++)
                {    
                    boolean rotate = !insideRotatedROI[i][j];

                    if(rotate)
                    {
                        double factor2 = (grid.getX(j) - centerXUnitCorrected);

                        double xDest = centerXUnitCorrected + factor2*cosAngle + factor1*sinAngle;
                        double yDest = centerYUnitCorrected - factor1*cosAngle + factor2*sinAngle;

                        double destValue = interpolation.getValue(channelData, xDest, yDest);

                        transformed[i][j] = Double.isNaN(destValue) ? fillValueScalar : destValue;
                    }
                    else
                    {
                        transformed[i][j] = matrix[i][j];
                    }
                }
            }
        }
        else 
        {
            throw new IllegalArgumentException("ROIRelativePosition " + position + " is not supported");
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }
}
