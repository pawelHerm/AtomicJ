package atomicJ.imageProcessing;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Channel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.functions.BiVariatePolynomial;
import atomicJ.functions.MultivariateFunction;
import atomicJ.functions.Polynomial;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.statistics.MultivariateL2Regression;


public class PolynomialFitCorrection implements Channel2DDataInROITransformation
{  
    private final int[][] model;

    public PolynomialFitCorrection(int[][] model)
    {
        this.model = model;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData);
        }

        return transformChannelData(channelData);
    }

    private Channel2DData transformChannelData(Channel2DData channelData)
    {        
        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinatesCopy();
        double[] zs = channelData.getZCoordinates();

        BiVariatePolynomial f = MultivariateL2Regression.getFittedFunction(xs, ys, zs, model[0], model[1]);

        int count = channelData.getItemCount();

        double[] transformedZs = new double[count];
        for(int i = 0; i<count; i++)
        {
            transformedZs[i] = zs[i] - f.value(xs[i], ys[i]);
        }

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }


    private GridChannel2DData transformGridChannel(GridChannel2DData channelData)
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        BiVariatePolynomial f = MultivariateL2Regression.getFittedFunction(matrix, rowCount, columnCount, model[0], model[1]);

        double[][] transformed = new double[rowCount][columnCount];

        Polynomial fX = f.getXPolynomial();
        Polynomial fY = f.getYPolynomial();

        for(int i = 0; i<rowCount; i++)
        {
            double fYVal = fY.value(i);
            double[] matrixRow = matrix[i];

            for(int j = 0; j<columnCount; j++)
            {                
                transformed[i][j] = matrixRow[j] - fYVal - fX.value(j);
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
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinatesCopy();
        double[] zs = channelData.getZCoordinates();

        int count = channelData.getItemCount();

        Shape roiShape = roi.getROIShape();
        final List<double[]> data = new ArrayList<>();

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(int i = 0; i < count; i++)
            {
                double x = xs[i];
                double y = ys[i];
                double z = zs[i];

                if(roiShape.contains(x, y))
                {
                    data.add(new double[] {x, y, z});                
                }
            }
        }
        else if(ROIRelativePosition.OUTSIDE.equals(position))
        {
            for(int i = 0; i < count; i++)
            {
                double x = xs[i];
                double y = ys[i];
                double z = zs[i];

                if(!roiShape.contains(x, y))
                {
                    data.add(new double[] {x, y, z});                
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown ROIPOsition " + position);
        }

        double[][] dataArray =  data.toArray(new double[][] {});

        MultivariateL2Regression regression = MultivariateL2Regression.findFit(dataArray, model);
        MultivariateFunction f = regression.getBestFit();

        double[] transformedZs = new double[count];
        for(int i = 0; i<count; i++)
        {
            transformedZs[i] = zs[i] - f.value(new double[] {xs[i], ys[i]});
        }

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }


    private GridChannel2DData transformGridChannel(GridChannel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        final Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        final double[][] matrix = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        final List<double[]> dataToFit = new ArrayList<>();

        roi.addPoints(grid, position, new GridPointRecepient() 
        {
            @Override
            public void addPoint(int row, int column)
            {
                double z = matrix[row][column];
                dataToFit.add(new double[] {column, row, z});                
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo) {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] matrixRow = matrix[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        double z = matrixRow[j];
                        dataToFit.add(new double[] {j, i, z});                
                    }
                }                
            }
        });


        double[][] dataToFitArray =  dataToFit.toArray(new double[][] {});

        MultivariateL2Regression regression = MultivariateL2Regression.findFit(dataToFitArray, model);
        MultivariateFunction f = regression.getBestFit();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] matrixRow = matrix[i];
            for(int j = 0; j<columnCount; j++)
            {                
                transformed[i][j] = matrixRow[j] - f.value(new double[] {j,i});
            }
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }
}
