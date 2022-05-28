
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
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.ArrayUtilities;

/**
 * Adds two {@link atomicJ.ImageMatrix ImageMatrix} objects, taking into account the physical dimensions of their grids. 
 * The operation performed is a linear combination of images a*A + b*B, where A is the image matrix passed as an argument to the {@link AddImageGeometrically#transform}
 * method, the image matrix B is passed to the constructor, just as are the scalar factors a and b.
 *
 */

public class AddImageGeometrically implements Channel2DDataInROITransformation
{   
    private final InterpolationMethod2D interpolation;
    private final Channel2DData channelDataB;
    private final double fractionA;
    private final double fractionB;

    /**
     * Creates a new <code>AddImageGeometrically</code>
     * 
     * @param channelDataB image to be added.
     * @param interpolation method used to find image values if the pixel size or physical dimensions of opperands differ
     * @param fractionA fraction of the channelDataB to add 
     * @param fractionB fraction of {@link AddImageGeometrically#channelDataB} to add
     */

    public AddImageGeometrically(Channel2DData channelDataB, InterpolationMethod2D interpolation, double fractionA, double fractionB)
    {        
        this.channelDataB = channelDataB;
        this.interpolation = interpolation;
        this.fractionA = fractionB;
        this.fractionB = fractionA;
    }

    /**
     * Adds the imageB (passed to the constructor) to the imageA.
     * This method can handle differences in the physical dimensions of the image grids, e.g. it works correctly when width of one
     * image is nanometers and the width of the second in microns. Interpolation is used if there is no pixel in imageB that has the same
     * position (in physical units) to a pixel in imageB. 
     * 
     * If the zQuantities of imageA and imageB are the same up to a unit, e.g. MPa and kPa or nN and mN,
     * then values pixels of imageB are converted to the units of imageA before addition. If the zQuantities are
     * not compatible (ex. when imageA is in MPa and imageB in nF), then the pixel values of imageB are not converted and
     * the resulting image has the zQuantity of the imageA. 
     * 
     * 
     * @param imageA the first addition operand
     */

    @Override
    public Channel2DData transform(Channel2DData channelDataA) 
    {
        if(channelDataA instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelDataA);
        }

        return transformChannel(channelDataA);
    }

    private Channel2DData transformGridChannel(GridChannel2DData channelA)
    {
        Grid2D gridA = channelA.getGrid();

        Quantity xQuantityA = channelA.getXQuantity();
        Quantity yQuantityA = channelA.getYQuantity();
        Quantity zQuantityA = channelA.getZQuantity();

        Quantity xQuantityB = channelDataB.getXQuantity();
        Quantity yQuantityB = channelDataB.getYQuantity();       
        Quantity zQuantityB = channelDataB.getZQuantity();

        double[][] matrixA = channelA.getData();

        int rowCount = gridA.getRowCount();
        int columnCount = gridA.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        double factorX = xQuantityA.getUnit().getConversionFactorTo(xQuantityB.getUnit());
        double factorY = yQuantityA.getUnit().getConversionFactorTo(yQuantityB.getUnit());

        double factorZ = zQuantityB.getUnit().getConversionFactorTo(zQuantityA.getUnit());

        for(int i = 0; i<rowCount; i++)
        {
            double y = factorY*gridA.getY(i);
            double[] rowA = matrixA[i];

            for(int j = 0; j<columnCount; j++)
            {    
                double x = factorX*gridA.getX(j);

                double value = rowA[j];
                double otherValue = factorZ*interpolation.getValue(channelDataB, x, y);

                transformed[i][j] = calculate(value, otherValue);
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, gridA, zQuantityA);

        return channelDataTransformed;
    }

    private Channel2DData transformChannel(Channel2DData channelDataA)
    {
        Quantity xQuantityA = channelDataA.getXQuantity();
        Quantity yQuantityA = channelDataA.getYQuantity();
        Quantity zQuantityA = channelDataA.getZQuantity();

        Quantity xQuantityB = channelDataB.getXQuantity();
        Quantity yQuantityB = channelDataB.getYQuantity();       
        Quantity zQuantityB = channelDataB.getZQuantity();

        int count = channelDataA.getItemCount();

        double[] xs = channelDataA.getXCoordinatesCopy();
        double[] ys = channelDataA.getYCoordinatesCopy();
        double[] zs = channelDataA.getZCoordinates();
        double[] zTransformed = new double[count];

        double factorX = xQuantityA.getUnit().getConversionFactorTo(xQuantityB.getUnit());
        double factorY = yQuantityA.getUnit().getConversionFactorTo(yQuantityB.getUnit());

        double factorZ = zQuantityB.getUnit().getConversionFactorTo(zQuantityA.getUnit());

        for(int i = 0; i<count; i++)
        {
            double y = factorY*ys[i];
            double x = factorX*xs[i];
            double value = zs[i];

            double otherValue = factorZ*interpolation.getValue(channelDataB, x, y);

            zTransformed[i] = calculate(value, otherValue);
        }

        double[][] dataNew = new double[][] {xs, ys, zTransformed};
        ChannelDomainIdentifier dataDomain = channelDataA.getDomainIdentifier();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantityA, yQuantityA, zQuantityA);

        return channelDataNew;
    }

    protected double calculate(double valA, double valB)
    {        
        double result = Double.isNaN(valB) ? valA : fractionA*valA + fractionB*valB;

        return result;
    }

    /**
     * Adds the imageB (passed to the constructor) to the imageA within the area in a particular <code>position</code> with respect to the Region of Interest <code>roi</code>.
     * This method can handle differences in the physical dimensions of the image grids, ex. it works correctly when width of one
     * image is nanometers and the width of the second in microns. Interpolation is used if there is no pixel in imageB that has the same
     * position (in physical units) to a pixel in imageB. 
     * 
     * If the zQuantities of imageA and imageB are the same up to a unit, ex. MPa and kPa or nN and mN,
     * then values pixels of imageB are converted to the units of imageA before addition. If the zQuantities are
     * not compatible (ex. when imageA is in MPa and imageB in nF), then the pixel values of imageB are not converted and
     * the resulting image has the zQuantity of the imageA. 
     * 
     * 
     * @param channelDataA the first addition operand
     * @param roi Region of Interest (ROI) which defines the range of operations
     * @param position defines whether the addition should be performed {@link atomicJ.gui.rois.ROIRelativePosition#INSIDE within ROI }, 
     *  {@link atomicJ.gui.rois.ROIRelativePosition#OUTSIDE outside ROI},
     *  or whether {@link atomicJ.gui.rois.ROIRelativePosition#EVERYTHING whole images should be added}
     */

    @Override
    public Channel2DData transform(Channel2DData channelDataA, ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelDataA);
        }

        if(channelDataA instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelDataA, roi, position);
        }

        return transformChannel(channelDataA, roi, position);
    }

    private Channel2DData transformChannel(Channel2DData channelDataA, ROI roi, ROIRelativePosition position)
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transformChannel(channelDataA);
        }

        Quantity xQuantityA = channelDataA.getXQuantity();
        Quantity yQuantityA = channelDataA.getYQuantity();
        Quantity zQuantityA = channelDataA.getZQuantity();

        Quantity xQuantityB = channelDataB.getXQuantity();
        Quantity yQuantityB = channelDataB.getYQuantity();       
        Quantity zQuantityB = channelDataB.getZQuantity();

        int count = channelDataA.getItemCount();

        double[] xs = channelDataA.getXCoordinatesCopy();
        double[] ys = channelDataA.getYCoordinatesCopy();
        double[] zTransformed = channelDataA.getZCoordinatesCopy();

        double factorX = xQuantityA.getUnit().getConversionFactorTo(xQuantityB.getUnit());
        double factorY = yQuantityA.getUnit().getConversionFactorTo(yQuantityB.getUnit());

        double factorZ = zQuantityB.getUnit().getConversionFactorTo(zQuantityA.getUnit());

        Shape roiShape = roi.getROIShape();

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(int i = 0; i<count; i++)
            {
                double x = xs[i];
                double y = ys[i];

                if(roiShape.contains(x, y))
                {
                    double value = zTransformed[i];

                    double otherValue = factorZ*interpolation.getValue(channelDataB, factorX*x, factorY*y);

                    zTransformed[i] = calculate(value, otherValue);
                }
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(int i = 0; i<count; i++)
            {
                double x = xs[i];
                double y = ys[i];

                if(!roiShape.contains(x, y))
                {
                    double value = zTransformed[i];

                    double otherValue = factorZ*interpolation.getValue(channelDataB, factorX*x, factorY*y);

                    zTransformed[i] = calculate(value, otherValue);
                }
            }
        }
        else 
        {
            throw new IllegalArgumentException("Unknown ROIRelativePostion " + position);
        }


        double[][] dataNew = new double[][] {xs, ys, zTransformed};
        ChannelDomainIdentifier dataDomain = channelDataA.getDomainIdentifier();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantityA, yQuantityA, zQuantityA);

        return channelDataNew;
    }

    private Channel2DData transformGridChannel(GridChannel2DData channelDataA, ROI roi, ROIRelativePosition position) 
    {
        final Grid2D gridA = channelDataA.getGrid();
        final double[][] matrixA = channelDataA.getData();

        Quantity xQuantityA = gridA.getXQuantity();
        Quantity yQuantityA = gridA.getYQuantity();
        Quantity zQuantityA = channelDataA.getZQuantity();

        Quantity xQuantityB = channelDataB.getXQuantity();
        Quantity yQuantityB = channelDataB.getYQuantity();       
        Quantity zQuantityB = channelDataB.getZQuantity();

        final double[][] transformed = ArrayUtilities.deepCopy(matrixA);

        final double factorX = xQuantityA.getUnit().getConversionFactorTo(xQuantityB.getUnit());
        final double factorY = yQuantityA.getUnit().getConversionFactorTo(yQuantityB.getUnit());


        final double factorZ = zQuantityB.getUnit().getConversionFactorTo(zQuantityA.getUnit());

        roi.addPoints(gridA, position, new GridPointRecepient() {

            @Override
            public void addPoint(int i, int j)
            {               
                double x = factorX*gridA.getX(j);
                double y = factorY*gridA.getY(i);

                double otherValue = factorZ*interpolation.getValue(channelDataB, x, y);
                double value = matrixA[i][j];
                transformed[i][j] = calculate(value, otherValue); 
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo) {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] transformedRow = transformed[i];
                    double[] matrixARow = matrixA[i];

                    double y = factorY*gridA.getY(i);

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        double x = factorX*gridA.getX(j);

                        double value = matrixARow[j];
                        double otherValue = factorZ*interpolation.getValue(channelDataB, x, y);

                        transformedRow[j] = calculate(value, otherValue);
                    }
                }                   
            }
        });

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, gridA, zQuantityA);

        return channelDataTransformed;
    }
}
