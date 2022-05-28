package atomicJ.imageProcessing;

import java.awt.Shape;

import atomicJ.data.Channel2DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.functions.MultivariateFunction;
import atomicJ.gui.rois.GridPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;

public class ReplaceDataTransformation implements Channel2DDataInROITransformation 
{
    private final MultivariateFunction f;
    private final boolean useDependentVariable;

    public ReplaceDataTransformation(MultivariateFunction f, boolean useDependentVariable)
    {
        this.f = f;
        this.useDependentVariable = useDependentVariable;
    }

    public boolean isIdentity()
    {
        return false;
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

        if(useDependentVariable)
        {
            double[] originalZs = channelData.getZCoordinates();

            for(int i = 0; i<count; i++)
            {
                double zOld = originalZs[i];
                transformedZs[i] =  f.value(new double[] {xs[i], ys[i], zOld});
            }
        }
        else
        {
            for(int i = 0; i<count; i++)
            {
                transformedZs[i] =  f.value(new double[] {xs[i], ys[i]});
            }
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
        double[][] original = channelData.getData();

        int n = grid.getRowCount();
        int m = grid.getColumnCount();

        double[][] transformed = new double[n][m];

        if (useDependentVariable) 
        {
            for (int i = 0; i < n; i++) 
            {
                double y = grid.getY(i);
                double[] rowOriginal = original[i];

                for (int j = 0; j < m; j++) 
                {                   
                    double x = grid.getX(j);
                    double zOld = rowOriginal[j];

                    transformed[i][j] = f.value(new double[] {x, y, zOld});
                }
            }
        }
        else 
        {
            for (int i = 0; i < n; i++)
            {
                double y = grid.getY(i);

                for (int j = 0; j < m; j++) {

                    double x = grid.getX(j);
                    transformed[i][j] = f.value(new double[] {x, y});
                }
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
            return transformGridChannelData((GridChannel2DData)channelData, roi, position);
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

        if(useDependentVariable)
        {
            if(ROIRelativePosition.INSIDE.equals(position))
            {
                for(int i = 0; i<count; i++)
                {
                    double x = xs[i];
                    double y = ys[i];

                    if(roiShape.contains(x, y))
                    {
                        double zOld = transformedZs[i];
                        transformedZs[i] = f.value(new double[] {x, y, zOld});
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
                        double zOld = transformedZs[i];
                        transformedZs[i] = f.value(new double[] {x, y, zOld});
                    }

                } 
            }
            else
            {
                throw new IllegalArgumentException("Unknwon ROIRelativePosition " + position);
            }
        }
        else
        {
            if(ROIRelativePosition.INSIDE.equals(position))
            {
                for(int i = 0; i<count; i++)
                {
                    double x = xs[i];
                    double y = ys[i];

                    if(roiShape.contains(x, y))
                    {
                        transformedZs[i] = f.value(new double[] {x, y});
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
                        transformedZs[i] = f.value(new double[] {x, y});
                    }
                } 
            }
            else
            {
                throw new IllegalArgumentException("Unknwon ROIRelativePosition " + position);
            } 
        }

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannelData(GridChannel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        final Grid2D grid = channelData.getGrid();
        Quantity zQuantity = channelData.getZQuantity();
        final double[][] original = channelData.getData();

        final double[][] transformed = channelData.getDataCopy();

        if(useDependentVariable)
        {
            roi.addPoints(grid, position, new GridPointRecepient() {

                @Override
                public void addPoint(int row, int column) 
                {
                    double x = grid.getX(column);
                    double y = grid.getY(row);
                    double zOld = original[row][column];

                    transformed[row][column] = f.value(new double[] {x, y, zOld});
                }

                @Override
                public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo) 
                {
                    for(int i = rowFrom; i<rowTo; i++)
                    {
                        double[] rowTransformed = transformed[i];
                        double[] rowOriginal = original[i];

                        double y = grid.getY(i);

                        for(int j = columnFrom; j<columnTo; j++)
                        {
                            double x = grid.getX(j);
                            double zOld = rowOriginal[j];

                            rowTransformed[j] = f.value(new double[] {x, y, zOld});                    
                        }
                    }                    
                }
            });
        }
        else
        {
            roi.addPoints(grid, position, new GridPointRecepient() 
            {

                @Override
                public void addPoint(int row, int column) 
                {

                    double x = grid.getX(column);
                    double y = grid.getY(row);

                    transformed[row][column] = f.value(new double[] {x, y});                    
                }

                @Override
                public void addBlock(int rowFrom, int rowTo, int columnFrom,
                        int columnTo) 
                {
                    for(int i = rowFrom; i<rowTo; i++)
                    {
                        double[] transformedRow = transformed[i];
                        double y = grid.getY(i);

                        for(int j = columnFrom; j<columnTo; j++)
                        {
                            double x = grid.getX(j);
                            transformedRow[j] = f.value(new double[] {x, y});                    
                        }
                    }
                }
            });
        }

        GridChannel2DData channelDataTransformed = new GridChannel2DData(transformed, grid, zQuantity);

        return channelDataTransformed;
    }
}
