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
import atomicJ.statistics.BivariateFunction;

public class ReplaceDataBivariateTransformation implements Channel2DDataInROITransformation 
{
    private final BivariateFunction f;

    public ReplaceDataBivariateTransformation(BivariateFunction f)
    {
        this.f = f;
    }

    public boolean isIdentity()
    {
        return f.isZero();
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
        int count = channelData.getItemCount();

        double[] transformedZs = new double[count];

        for(int i = 0; i<count; i++)
        {
            transformedZs[i] =  f.value(xs[i], ys[i]);
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

        int n = grid.getRowCount();
        int m = grid.getColumnCount();

        double[][] transformed = new double[n][m];

        for (int i = 0; i < n; i++) 
        {
            double y = grid.getY(i);

            for (int j = 0; j < m; j++) 
            {               
                double x = grid.getX(j);

                transformed[i][j] = f.value(x, y);
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

        return transformChannelData(channelData, roi, position);
    }

    private Channel2DData transformChannelData(Channel2DData channelData, ROI roi, ROIRelativePosition position)
    {        
        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinatesCopy();
        int count = channelData.getItemCount();

        Shape roiShape = roi.getROIShape();

        double[] transformedZs = channelData.getZCoordinatesCopy();

        if(ROIRelativePosition.INSIDE.equals(position))
        {
            for(int i = 0; i<count; i++)
            {
                double x = xs[i];
                double y = ys[i];

                if(roiShape.contains(x, y))
                {
                    transformedZs[i] = f.value(x, y);
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
                    transformedZs[i] = f.value(x, y);
                }
            } 
        }
        else
        {
            throw new IllegalArgumentException("Unknwon ROIRelativePosition " + position);
        } 

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    public Channel2DData transformGridChannelData(GridChannel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        final Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        final double[][] transformed = channelData.getDataCopy();

        roi.addPoints(grid, position, new GridPointRecepient() 
        {
            @Override
            public void addPoint(int row, int column) {
                double x = grid.getX(column);
                double y = grid.getY(row);

                transformed[row][column] = f.value(x, y);                
            }

            @Override
            public void addBlock(int rowFrom, int rowTo, int columnFrom,int columnTo) {
                for(int i = rowFrom; i<rowTo; i++)
                {
                    double[] dataRow = transformed[i];
                    double y = grid.getY(i);

                    for(int j = columnFrom; j<columnTo; j++)
                    {
                        double x = grid.getX(j);
                        dataRow[j] = f.value(x, y);                    
                    }
                }
            }
        }); 

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);
        return channelDataTransformed;
    }
}
