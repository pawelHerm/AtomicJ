package atomicJ.imageProcessing;

import java.awt.Shape;

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

public class MultiplyOperation implements Channel2DDataInROITransformation
{   
    private final double factor;

    public MultiplyOperation(double factor)
    {
        this.factor = factor;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData) 
    {
        if(this.factor == 1)
        {
            return channelData;
        }

        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData);
        }

        return transformChannelData(channelData);
    }

    private Channel2DData transformChannelData(Channel2DData channelData)
    {        
        double[] originalZs = channelData.getZCoordinates();
        int count = originalZs.length;

        double[] transformedZs = new double[count];
        for(int i = 0; i<count; i++)
        {
            transformedZs[i] = factor*originalZs[i];
        }

        double[][] dataNew = new double[][] {channelData.getXCoordinatesCopy(), channelData.getYCoordinatesCopy(), transformedZs};
        ChannelDomainIdentifier domainIdentifier = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, domainIdentifier, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannel(GridChannel2DData channelData)
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        double[][] original = channelData.getData();

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        double[][] transformed = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount; i++)
        {
            double[] originalRow = original[i];
            for(int j = 0; j<columnCount; j++)
            {    
                transformed[i][j] = factor*originalRow[j];
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

        if(this.factor == 1)
        {
            return channelData;
        }

        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channelData, roi, position);
        }

        return transformChannelData(channelData, roi, position);
    }

    private Channel2DData transformChannelData(Channel2DData channelData, ROI roi, ROIRelativePosition position)
    {        
        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinatesCopy();
        int count = channelData.getItemCount();

        Shape roiShape = roi.getROIShape();

        double[] originalZs = channelData.getZCoordinates();
        double[] transformedZs = channelData.getZCoordinatesCopy();

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(int i = 0; i<count; i++)
            {
                double x = xs[i];
                double y = ys[i];

                if(roiShape.contains(x, y))
                {
                    transformedZs[i] = factor*originalZs[i];
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
                    transformedZs[i] = factor*originalZs[i];
                }

            } 
        }
        else
        {
            throw new IllegalArgumentException("Unknwon ROIRelativePosition " + position);
        }

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier domainIdentifier = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, domainIdentifier, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannel(GridChannel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        final double[][] matrix = channelData.getData();

        final double[][] transformed = ArrayUtilities.deepCopy(matrix);

        roi.addPoints(grid, position, new GridPointRecepient() {

            @Override
            public void addPoint(int i, int j)
            {               
                transformed[i][j] = factor*matrix[i][j];
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,
                    int columnTo)
            {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] matrixRow = matrix[i];
                    double[] transformedRow = transformed[i];

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        transformedRow[j] = factor*matrixRow[j];
                    }
                }                      
            }
        });


        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }
}
