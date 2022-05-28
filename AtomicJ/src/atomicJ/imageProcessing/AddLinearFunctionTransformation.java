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

public class AddLinearFunctionTransformation implements Channel2DDataInROITransformation 
{
    private final MultivariateFunction f;
    private final boolean useDependentVariable;

    public AddLinearFunctionTransformation(MultivariateFunction f, boolean useDependent)
    {
        this.f = f;
        this.useDependentVariable = useDependent;
    }

    public boolean isIdentity()
    {
        return f.isZero();
    }

    @Override
    public Channel2DData transform(Channel2DData channelData)
    {               
        if(f.isZero())
        {
            return channelData;
        }

        if(channelData instanceof GridChannel2DData)
        {
            return transformGridData((GridChannel2DData)channelData);
        }

        return transformChannelData(channelData);
    }


    private Channel2DData transformGridData(GridChannel2DData channelData)
    {
        if(useDependentVariable)
        {
            return transformGridDataUseDepended(channelData);
        }
        else
        {
            return transformGridDataDoNotUseDepended(channelData);
        }
    }

    private Channel2DData transformGridDataDoNotUseDepended(GridChannel2DData channelData)
    {               
        if(f.isZero())
        {
            return channelData;
        }

        Grid2D grid = channelData.getGrid();
        double[][] original = channelData.getData();

        int n = grid.getRowCount();
        int m = grid.getColumnCount();

        double[][] transformed = new double[n][m];
        for (int i = 0; i < n; i++)
        {
            double y = grid.getY(i);
            double[] rowOriginal = original[i];

            for (int j = 0; j < m; j++) 
            {
                double x = grid.getX(j);

                double zOld = rowOriginal[j];

                transformed[i][j] = zOld + f.value(new double[] {x, y});

            }
        }

        GridChannel2DData channelDataNew = new GridChannel2DData(transformed, grid, channelData.getZQuantity());

        return channelDataNew;
    }


    private Channel2DData transformGridDataUseDepended(GridChannel2DData channelData)
    {               
        if(f.isZero())
        {
            return channelData;
        }

        Grid2D grid = channelData.getGrid();
        double[][] original = channelData.getData();

        int n = grid.getRowCount();
        int m = grid.getColumnCount();

        double[][] transformed = new double[n][m];
        for (int i = 0; i < n; i++) 
        {
            double y = grid.getY(i);
            double[] rowOriginal = original[i];
            for (int j = 0; j < m; j++) 
            {
                double x = grid.getX(j);

                double zOld = rowOriginal[j];
                transformed[i][j] = zOld + f.value(new double[] {x, y, zOld});
            }
        }

        GridChannel2DData channelDataNew = new GridChannel2DData(transformed, grid, channelData.getZQuantity());

        return channelDataNew;
    }

    @Override
    public Channel2DData transform(Channel2DData channelData, ROI roi,  ROIRelativePosition position)
    {               
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channelData);
        }

        if(f.isZero())
        {
            return channelData;
        }       

        if(channelData instanceof GridChannel2DData)
        {
            return transformGridChannelData((GridChannel2DData)channelData, roi, position);
        }

        return transformChannelData(channelData, roi, position);
    }

    private Channel2DData transformChannelData(Channel2DData channelData)
    {
        if(useDependentVariable)
        {
            return transformChannelDataUseDepended(channelData);
        }
        else
        {
            return transformChannelDataDoNotUseDepended(channelData);
        }
    }

    private Channel2DData transformChannelDataUseDepended(Channel2DData channelData)
    {        
        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinatesCopy();
        double[] originalZs = channelData.getZCoordinates();
        int count = originalZs.length;

        double[] transformedZs = new double[count];
        for(int i = 0; i<count; i++)
        {
            double zOld = originalZs[i];
            transformedZs[i] = zOld + f.value(new double[] {xs[i], ys[i], zOld});
        }

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private Channel2DData transformChannelDataDoNotUseDepended(Channel2DData channelData)
    {        
        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinatesCopy();
        double[] originalZs = channelData.getZCoordinates();
        int count = originalZs.length;

        double[] transformedZs = new double[count];
        for(int i = 0; i<count; i++)
        {
            transformedZs[i] = originalZs[i] + f.value(new double[] {xs[i], ys[i]});
        }

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannelData(GridChannel2DData channelData, ROI roi,  ROIRelativePosition position)
    {
        Grid2D grid = channelData.getGrid();

        double[][] original = channelData.getData();
        double[][] transformed = channelData.getDataCopy();

        GridPointRecepient recepient = useDependentVariable ? new AddPointUseDependentGridPointRecepient(f, original, transformed, grid) : new AddPointDoNotUseDependentGridPointRecepient(f, original, transformed, grid);
        roi.addPoints(grid, position, recepient);

        GridChannel2DData channelDataNew = new GridChannel2DData(transformed, grid, channelData.getZQuantity());

        return channelDataNew;
    }


    private Channel2DData transformChannelData(Channel2DData channelData, ROI roi, ROIRelativePosition position)
    {
        if(useDependentVariable)
        {
            return transformChannelDataUseDepended(channelData, roi, position);
        }
        else
        {
            return transformChannelDataDoNotUseDepended(channelData, roi, position);
        }
    }

    private Channel2DData transformChannelDataUseDepended(Channel2DData channelData, ROI roi, ROIRelativePosition position)
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
                    double zOld = transformedZs[i];
                    transformedZs[i] = zOld + f.value(new double[] {x, y, zOld});
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
                    transformedZs[i] = zOld + f.value(new double[] {x, y, zOld});
                }

            }
        }
        else 
        {
            throw new IllegalArgumentException("Unknown ROIRelativePosition " + position);
        }

        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private Channel2DData transformChannelDataDoNotUseDepended(Channel2DData channelData, ROI roi, ROIRelativePosition position)
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
                    transformedZs[i] = transformedZs[i] + f.value(new double[] {x, y});
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
                    transformedZs[i] = transformedZs[i] + f.value(new double[] {x, y});
                }
            }
        }
        else 
        {
            throw new IllegalArgumentException("Unknown ROIRelativePosition " + position);
        }



        double[][] dataNew = new double[][] {xs, ys, transformedZs};
        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();

        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        Channel2DData channelDataNew = new FlexibleChannel2DData(dataNew, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private static class AddPointUseDependentGridPointRecepient implements GridPointRecepient
    {
        private final Grid2D grid;
        private final double[][] original;
        private final double[][] transformed;
        private final MultivariateFunction f;

        public AddPointUseDependentGridPointRecepient(MultivariateFunction f, double[][] original, double[][] transformed, Grid2D grid)
        {
            this.f = f;
            this.original = original;
            this.transformed = transformed;
            this.grid = grid;
        }

        @Override
        public void addPoint(int row, int column)
        {
            double x = grid.getX(column);
            double y = grid.getY(row);

            double zOld = original[row][column];

            transformed[row][column] = zOld + f.value(new double[] {x, y, zOld});                   
        }

        @Override
        public void addBlock(int rowFrom, int rowTo, int columnFrom,int columnTo) 
        {
            for(int i = rowFrom; i<rowTo; i++)
            {
                double y = grid.getY(i);

                double[] rowOriginal = original[i];

                for(int j = columnFrom; j<columnTo; j++)
                {
                    double x = grid.getX(j);
                    double zOld = rowOriginal[j];

                    transformed[i][j] = zOld + f.value(new double[] {x, y, zOld});    
                }
            }                      
        }
    }


    private static class AddPointDoNotUseDependentGridPointRecepient implements GridPointRecepient
    {
        private final Grid2D grid;
        private final double[][] original;
        private final double[][] transformed;
        private final MultivariateFunction f;

        public AddPointDoNotUseDependentGridPointRecepient(MultivariateFunction f, double[][] original, double[][] transformed, Grid2D grid)
        {
            this.f = f;
            this.original = original;
            this.transformed = transformed;
            this.grid = grid;
        }

        @Override
        public void addPoint(int row, int column)
        {
            double x = grid.getX(column);
            double y = grid.getY(row);

            transformed[row][column] = original[row][column] + f.value(new double[] {x, y});                   
        }

        @Override
        public void addBlock(int rowFrom, int rowTo, int columnFrom,int columnTo) 
        {
            for(int i = rowFrom; i<rowTo; i++)
            {
                double y = grid.getY(i);

                double[] rowOriginal = original[i];

                for(int j = columnFrom; j<columnTo; j++)
                {
                    double x = grid.getX(j);

                    transformed[i][j] = rowOriginal[j] + f.value(new double[] {x, y});    
                }
            }                      
        }
    }
}
