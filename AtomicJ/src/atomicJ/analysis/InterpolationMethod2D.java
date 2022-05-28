package atomicJ.analysis;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.apache.commons.math.analysis.BivariateRealFunction;
import org.jfree.data.Range;

import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.interp.SibsonInterpolator2;
import edu.mines.jtk.mesh.TriMesh;

import atomicJ.data.ArrayChannel2DData;
import atomicJ.data.ArraySupport2D;
import atomicJ.data.Channel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.gui.DistanceShapeFactors;
import atomicJ.utilities.ArrayUtilities;


public enum InterpolationMethod2D 
{
    BICUBIC_SPLINE("Bicubic spline")
    {
        @Override
        public double getValue(Channel2DData channel, double x, double y) 
        {
            GridChannel2DData gridChannel = channel.getDefaultGridding();

            double z = Double.NaN;

            BivariateRealFunction interpolationFunction = gridChannel.getBicubicSplineInterpolationFunction();
            if (interpolationFunction != null) 
            {          
                try {
                    z = interpolationFunction.value(x, y);
                } catch (Exception e) {
                    z = Double.NaN;
                }
            }

            return z;
        }

        @Override
        public double[] getProfileValues(Channel2DData channel, Shape profile, int n) 
        {
            GridChannel2DData gridChannel = channel.getDefaultGridding();

            BivariateRealFunction interpolationFunction = gridChannel.getBicubicSplineInterpolationFunction();
            if (interpolationFunction == null) 
            {          
                return new double[] {};        
            }


            double[] values = new double[n];
            double[][] profilePoints = DistanceShapeFactors.getProfilePoints(profile, n);

            for (int i = 0; i < n; i++) {
                double x = profilePoints[i][0];
                double y = profilePoints[i][1];
                double z;
                try {
                    z = interpolationFunction.value(x, y);
                } catch (Exception e) {
                    z = Double.NaN;
                }
                values[i] = z;     
            }

            return values;
        }

        @Override
        public double[][] getCrossSection(Channel2DData channel, double[][] profilePoints) 
        {
            GridChannel2DData gridChannel = channel.getDefaultGridding();

            BivariateRealFunction interpolationFunction = gridChannel.getBicubicSplineInterpolationFunction();
            if(interpolationFunction == null)
            {
                return new double[][] {};
            }

            int pointCount = profilePoints.length;
            double[][] data = new double[pointCount][];

            for(int i = 0; i<pointCount; i++)
            {
                double[] p = profilePoints[i];
                double x = p[0];
                double y = p[1];
                double d = p[2];
                double z;
                try
                {
                    z = interpolationFunction.value(x, y);
                }
                catch(Exception e)
                {   
                    z = Double.NaN;
                }
                data[i] = new double[] {d,z};
            }

            return data;
        }

        @Override
        public double[][] getGriddedData(Channel2DData channel, int rowCount, int columnCount)
        {
            GridChannel2DData gridChannel = channel.getDefaultGridding();

            Grid2D grid = gridChannel.getGrid();

            double domainLength = grid.getDomainLength();
            double rangeLength = grid.getRangeLength();

            double domainOrigin = grid.getXOrigin();
            double rangeOrigin = grid.getYOrigin();

            double domainMax = grid.getXMaximum();
            double rangeMax = grid.getYMaximum();

            double domainIncrement = domainLength/(columnCount - 1.);
            double rangeIncrement = rangeLength/(rowCount - 1.);

            BivariateRealFunction interpolationFunction = gridChannel.getBicubicSplineInterpolationFunction();

            double[][] data = new double[rowCount][columnCount];        

            for(int i = 0; i<rowCount; i++)
            {
                double y = rangeOrigin + i*rangeIncrement;
                double ySafe = Math.min(Math.max(rangeOrigin + TOLERANCE, y), rangeMax - TOLERANCE);

                for(int j = 0; j<columnCount;j++)
                {               
                    double x = domainOrigin + j*domainIncrement;
                    double xSafe = Math.min(Math.max(domainOrigin + TOLERANCE, x), domainMax - TOLERANCE);
                    try 
                    {
                        data[i][j] = interpolationFunction.value(xSafe, ySafe);
                    } 
                    catch (Exception e) 
                    {
                        //                        data[i][j] = 0;
                        //e.printStackTrace();
                    }
                }
            }

            return data;
        }
    }, BILINEAR("Bilinear") 
    {
        @Override
        public double getValue(Channel2DData channel, double x, double y)
        {
            GridChannel2DData gridChannel = channel.getDefaultGridding();

            return getValue(gridChannel.getGrid(), gridChannel.getData(), x, y);
        }

        private double getValue(ArraySupport2D grid, double[][] gridData, double x, double y)
        {
            double z = Double.NaN;

            if(grid.isWithinGridArea(x, y))
            {
                int row1 = grid.getRowFloorWithinBounds(y);
                int column1 = grid.getColumnFloorWithinBounds(x);
                int row2 = grid.getRowCeilingWithinBounds(y);
                int column2 = grid.getColumnCeilingWithinBounds(x);

                if(column1 == column2 && row1 ==row2)
                {
                    z = gridData[row1][column1];               
                }
                else if(column1 == column2)
                {
                    double y1 = grid.getY(row1);
                    double y2 = grid.getY(row2);

                    double length = y2 - y1;
                    double f1 = gridData[row1][column1];
                    double f2 = gridData[row2][column1];

                    z = f2 *(y - y1)/length + f1*(y2 - y)/length;
                }
                else if(row1 == row2)
                {
                    double x1 = grid.getX(column1);
                    double x2 = grid.getX(column2);

                    double length = x2 - x1;
                    double f1 = gridData[row1][column1];
                    double f2 = gridData[row1][column2];

                    z = f2 *(x - x1)/length + f1*(x2 - x)/length;
                }
                else
                {
                    double x1 = grid.getX(column1);
                    double x2 = grid.getX(column2);
                    double y1 = grid.getY(row1);
                    double y2 = grid.getY(row2);

                    double f11 = gridData[row1][column1];
                    double f21 = gridData[row1][column2];
                    double f12 = gridData[row2][column1];
                    double f22 = gridData[row2][column2];

                    z = (f11*(x2 - x)*(y2 - y) + f21*(x - x1)*(y2 - y) + f12*(x2 - x)*(y - y1) + f22*(x - x1)*(y - y1))/((x2 - x1)*(y2 - y1));
                }
            }

            return z; 
        }

        @Override
        public double[] getProfileValues(Channel2DData channel, Shape profile, int n) 
        {
            double[] values = new double[n];
            double[][] profilePoints = DistanceShapeFactors.getProfilePoints(profile, n);

            double largest = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < n; i++) 
            {
                double x = profilePoints[i][0];
                double y = profilePoints[i][1];

                values[i] = getValue(channel, x, y);  

                if(values[i]>largest)
                {
                    largest = values[i];
                }
            }

            return values;
        }

        @Override
        public double[][] getCrossSection(Channel2DData channel, double[][] profilePoints) 
        {
            int pointCount = profilePoints.length;
            double[][] data = new double[pointCount][];

            for(int i = 0; i<pointCount; i++)
            {
                double[] p = profilePoints[i];
                double x = p[0];
                double y = p[1];
                double d = p[2];

                double z = getValue(channel, x, y);

                data[i] = new double[] {d,z};
            }

            return data;
        }

        @Override
        public double[][] getGriddedData(Channel2DData channel, int rowCount,
                int columnCount) 
        {
            ArrayChannel2DData gridChannel = channel instanceof ArrayChannel2DData ? (ArrayChannel2DData)channel : channel.getDefaultGridding();

            ArraySupport2D grid = gridChannel.getDefaultGriddingGrid();  
            double[][] gridData = gridChannel.getData();

            double domainLength = grid.getDomainLength();
            double rangeLength = grid.getRangeLength();

            double domainOrigin = grid.getXOrigin();
            double rangeOrigin = grid.getYOrigin();        

            double domainIncrement = domainLength/(columnCount - 1.);
            double rangeIncrement = rangeLength/(rowCount - 1.);

            double[][] data = new double[rowCount][columnCount];        

            for(int i = 0; i<rowCount; i++)
            {
                double y = rangeOrigin + i*rangeIncrement;

                for(int j = 0; j<columnCount;j++)
                {               
                    double x = domainOrigin + j*domainIncrement;                

                    data[i][j] = getValue(grid, gridData, x, y);               
                }
            }

            return data;
        }
    },

    NEAREST_NEIGHBOUR("Nearest neighbour") {
        @Override
        public double getValue(Channel2DData channelData, double x, double y) 
        {
            if(channelData instanceof GridChannel2DData)
            {
                return getValueFromGridChannelData((GridChannel2DData)channelData, x, y);
            }

            return getValueFromPossiblyScatteredData(channelData, x, y);
        }

        private double getValueFromGridChannelData(GridChannel2DData channelData, double x, double y)
        {
            double z = Double.NaN;

            Grid2D grid = channelData.getGrid();
            double[][] gridData = channelData.getData();

            int row = grid.getRow(y);
            int column = grid.getColumn(x);

            int rowCount = grid.getRowCount();
            int columnCount = grid.getColumnCount();

            if(row >= 0 && column >= 0 && row<rowCount && column <columnCount)
            {
                z = gridData[row][column];     
            }

            return z;
        }

        private double getValueFromPossiblyScatteredData(Channel2DData channelData, double x, double y)
        {
            Rectangle2D dataArea = channelData.getDataArea();

            if(!dataArea.contains(x, y))
            {
                return Double.NaN;
            }

            TriMesh mesh = new TriMesh();

            double[] xs = channelData.getXCoordinates();
            double[] ys = channelData.getYCoordinates();
            double[] zs = channelData.getZCoordinates();

            for (int i= 0; i< channelData.getItemCount(); i++) 
            {
                TriMesh.Node node = new TriMesh.Node((float)xs[i],(float)ys[i]);
                node.index = i;
                mesh.addNode(node);
            }


            TriMesh.Node node = mesh.findNodeNearest((float)x, (float)y);                

            double z = zs[node.index];

            return z;
        }

        @Override
        public double[] getProfileValues(Channel2DData channelData, Shape profile, int n) 
        {         
            if(channelData instanceof GridChannel2DData)
            {
                return getProfileValuesFromGridChannelData((GridChannel2DData)channelData, profile, n);
            }

            return getProfileValuesFromPossiblyScatteredData(channelData, profile, n);
        }

        public double[] getProfileValuesFromGridChannelData(GridChannel2DData channelData, Shape profile, int n)
        {
            Grid2D grid = channelData.getGrid();
            double[][] gridData = channelData.getData();

            double[] values = new double[n];
            double[][] profilePoints = DistanceShapeFactors.getProfilePoints(profile, n);

            int rowCount = grid.getRowCount();
            int columnCount = grid.getColumnCount();

            for (int i = 0; i < n; i++) 
            {
                double x = profilePoints[i][0];
                double y = profilePoints[i][1];

                int row = grid.getRow(y);
                int column = grid.getColumn(x);

                if(row >= 0 && column >= 0 && row<rowCount && column <columnCount)
                {
                    values[i] = gridData[grid.getRow(y)][grid.getColumn(x)];     
                }
                else
                {
                    values[i] = Double.NaN;
                }
            }

            return values;
        }

        public double[] getProfileValuesFromPossiblyScatteredData(Channel2DData channelData, Shape profile, int n)
        {           
            TriMesh mesh = new TriMesh();

            double[] xs = channelData.getXCoordinates();
            double[] ys = channelData.getYCoordinates();
            double[] zs = channelData.getZCoordinates();

            for (int i= 0; i< channelData.getItemCount(); i++) 
            {
                TriMesh.Node node = new TriMesh.Node((float)xs[i],(float)ys[i]);
                node.index = i;
                mesh.addNode(node);
            }

            Rectangle2D dataArea = channelData.getDataArea();

            double[] values = new double[n];
            double[][] profilePoints = DistanceShapeFactors.getProfilePoints(profile, n);

            for(int i = 0; i < n; i++) 
            {
                double x = profilePoints[i][0];
                double y = profilePoints[i][1];

                if(dataArea.contains(x, y))
                {
                    TriMesh.Node node = mesh.findNodeNearest((float)x,(float)y);
                    values[i] = zs[node.index];     
                }
                else
                {
                    values[i] = Double.NaN;
                }
            }

            return values;
        }

        @Override
        public double[][] getCrossSection(Channel2DData channelData, double[][] profilePoints) 
        {
            if(channelData instanceof GridChannel2DData)
            {
                return getCrossSectionDataFromGridChannelData((GridChannel2DData)channelData, profilePoints);
            }

            return getCrossSectionDataFromPossiblyScatteredData(channelData, profilePoints);
        }

        private double[][] getCrossSectionDataFromGridChannelData(GridChannel2DData gridChannel, double[][] profilePoints)
        {
            Grid2D grid = gridChannel.getGrid();
            double[][] gridData = gridChannel.getData();

            int pointCount = profilePoints.length;
            double[][] data = new double[pointCount][];

            int rowCount  = grid.getRowCount();
            int columnCount = grid.getColumnCount();

            for(int i = 0; i<pointCount; i++)
            {
                double[] p = profilePoints[i];
                double x = p[0];
                double y = p[1];
                double d = p[2];

                int row = grid.getRow(y);
                int column = grid.getColumn(x);

                double z = Double.NaN;
                if(row >= 0 && column >= 0 && row<rowCount && column <columnCount)
                {
                    z  = gridData[row][column];
                }

                data[i] = new double[] {d,z};
            }

            return data;
        }

        private double[][] getCrossSectionDataFromPossiblyScatteredData(Channel2DData channelData, double[][] profilePoints)
        {
            TriMesh mesh = new TriMesh();

            double[] xs = channelData.getXCoordinates();
            double[] ys = channelData.getYCoordinates();
            double[] zs = channelData.getZCoordinates();

            int n = channelData.getItemCount();

            for (int i= 0; i<n; i++) 
            {
                TriMesh.Node node = new TriMesh.Node((float)xs[i],(float)ys[i]);
                node.index = i;
                mesh.addNode(node);
            }

            int pointCount = profilePoints.length;
            double[][] data = new double[pointCount][];

            for(int i = 0; i<pointCount; i++)
            {
                double[] p = profilePoints[i];
                float x = (float)p[0];
                float y = (float)p[1];
                double d = p[2];

                TriMesh.Node node = mesh.findNodeNearest(x,y);                
                data[i] = new double[] {d, zs[node.index]};
            }

            return data;
        }

        @Override
        public double[][] getGriddedData(Channel2DData channelData, int rowCount, int columnCount) 
        {
            if(channelData instanceof GridChannel2DData)
            {
                return getGriddedDataFromGriddedChannelData((GridChannel2DData)channelData, rowCount, columnCount);
            }

            return getGriddedDataFromPossiblyScatteredData(channelData, rowCount, columnCount);
        }

        private double[][] getGriddedDataFromGriddedChannelData(GridChannel2DData gridChannel, int rowCount, int columnCount)
        {

            Grid2D grid = gridChannel.getGrid();
            if(grid.getRowCount() == rowCount && grid.getColumnCount() == columnCount)
            {
                //                return gridChannel;
            }

            double[][] gridData = gridChannel.getData();

            double domainLength = grid.getDomainLength();
            double rangeLength = grid.getRangeLength();

            double domainOrigin = grid.getXOrigin();
            double rangeOrigin = grid.getYOrigin();        

            double domainIncrement = domainLength/(columnCount - 1.);
            double rangeIncrement = rangeLength/(rowCount - 1.);

            double[][] data = new double[rowCount][columnCount];        

            for(int i = 0; i<rowCount; i++)
            {
                double y = rangeOrigin + i*rangeIncrement;
                int row = grid.getRow(y);

                for(int j = 0; j<columnCount;j++)
                {               
                    double x = domainOrigin + j*domainIncrement;
                    try 
                    {
                        int column = grid.getColumn(x);
                        data[i][j] = gridData[row][column];
                    } 
                    catch (Exception e) 
                    {
                        data[i][j] = 0;
                    }
                }
            }

            return data;
        }

        private double[][] getGriddedDataFromPossiblyScatteredData(Channel2DData channelData, int rowCount, int columnCount)
        {                        
            TriMesh mesh = new TriMesh();

            double[] xs = channelData.getXCoordinates();
            double[] ys = channelData.getYCoordinates();
            double[] zs = channelData.getZCoordinates();

            int n = channelData.getItemCount();

            for (int i=0; i<n; ++i) 
            {
                TriMesh.Node node = new TriMesh.Node((float)xs[i],(float)ys[i]);
                node.index = i;
                mesh.addNode(node);
            }

            Range xRange = channelData.getXRange();
            Range yRange = channelData.getYRange();

            double xOrigin = xRange.getLowerBound();
            double yOrigin = yRange.getLowerBound();        

            double xIncrement = xRange.getLength()/(columnCount - 1.);
            double yIncrement = yRange.getLength()/(rowCount - 1.);

            double[][] data = new double[rowCount][columnCount];

            for (int i = 0; i<rowCount; i++) 
            {
                float y = (float)(yOrigin + i*yIncrement);

                for (int j = 0; j<columnCount; j++)
                {
                    float x = (float)(xOrigin + j*xIncrement);
                    TriMesh.Node node = mesh.findNodeNearest(x,y);
                    data[i][j] = zs[node.index];              
                }
            }


            return data;
        }
    }, 

    SIBSON("Sibson") {
        @Override
        public double getValue(Channel2DData channel, double x, double y) 
        {
            float[] xCoords = ArrayUtilities.toFloat(channel.getXCoordinates());
            float[] yCoords = ArrayUtilities.toFloat(channel.getYCoordinates());
            float[] zCoords = ArrayUtilities.toFloat(channel.getZCoordinates());

            SibsonInterpolator2 sibsonInterpolator = new SibsonInterpolator2(zCoords, xCoords, yCoords);

            Range xRange = channel.getXRange();
            Range yRange = channel.getYRange();

            float xMin = (float) xRange.getLowerBound();
            float yMin = (float) yRange.getLowerBound();        

            float xMax = (float) xRange.getUpperBound();
            float yMax = (float) yRange.getUpperBound();

            sibsonInterpolator.setBounds(xMin, xMax, yMin, yMax);
            sibsonInterpolator.setNullValue(Float.NaN);

            double value = sibsonInterpolator.interpolate((float)x, (float)y);

            return value;
        }

        @Override
        public double[] getProfileValues(Channel2DData channel, Shape profile, int n) 
        {       
            double[][] profilePoints = DistanceShapeFactors.getProfilePoints(profile, n);

            float[] xCoords = ArrayUtilities.toFloat(channel.getXCoordinates());
            float[] yCoords = ArrayUtilities.toFloat(channel.getYCoordinates());
            float[] zCoords = ArrayUtilities.toFloat(channel.getZCoordinates());

            SibsonInterpolator2 sibsonInterpolator = new SibsonInterpolator2(zCoords, xCoords, yCoords);

            Range xRange = channel.getXRange();
            Range yRange = channel.getYRange();

            float xMin = (float) xRange.getLowerBound();
            float yMin = (float) yRange.getLowerBound();        

            float xMax = (float) xRange.getUpperBound();
            float yMax = (float) yRange.getUpperBound();

            sibsonInterpolator.setBounds(xMin, xMax, yMin, yMax);
            sibsonInterpolator.setNullValue(Float.NaN);

            double[] values = new double[n];

            for (int i = 0; i < n; i++) 
            {
                float x = (float)profilePoints[i][0];
                float y = (float)profilePoints[i][1];

                values[i] = (x>= xMin && x <= xMax && y >= yMin && y <= yMax) ? 
                        sibsonInterpolator.interpolate(x, y) : Double.NaN;   
            }

            return values;
        }

        @Override
        public double[][] getCrossSection(Channel2DData channel, double[][] profilePoints) 
        {
            int pointCount = profilePoints.length;
            double[][] data = new double[pointCount][];

            float[] xCoords = ArrayUtilities.toFloat(channel.getXCoordinates());
            float[] yCoords = ArrayUtilities.toFloat(channel.getYCoordinates());
            float[] zCoords = ArrayUtilities.toFloat(channel.getZCoordinates());

            SibsonInterpolator2 sibsonInterpolator = new SibsonInterpolator2(zCoords, xCoords, yCoords);

            Range xRange = channel.getXRange();
            Range yRange = channel.getYRange();

            float xMin = (float) xRange.getLowerBound();               
            float xMax = (float) xRange.getUpperBound();

            float yMin = (float) yRange.getLowerBound();        
            float yMax = (float) yRange.getUpperBound();

            sibsonInterpolator.setBounds(xMin, xMax, yMin, yMax);
            sibsonInterpolator.setNullValue(Float.NaN);

            for (int i = 0; i < pointCount; i++) 
            {
                double[] p = profilePoints[i];
                float x = (float)p[0];
                float y = (float)p[1];
                double d = p[2];

                double z = (x >= xMin && x <= xMax && y >= yMin && y <= yMax) ? sibsonInterpolator.interpolate(x, y) : Double.NaN; 

                data[i] = new double[] {d,z};
            }

            return data;
        }

        @Override
        public double[][] getGriddedData(Channel2DData channel, int rowCount, int columnCount)
        {
            float[] xCoords = ArrayUtilities.toFloat(channel.getXCoordinates());
            float[] yCoords = ArrayUtilities.toFloat(channel.getYCoordinates());
            float[] zCoords = ArrayUtilities.toFloat(channel.getZCoordinates());

            SibsonInterpolator2 sibsonInterpolator = new SibsonInterpolator2(zCoords, xCoords, yCoords);

            Range xRange = channel.getXRange();
            Range yRange = channel.getYRange();

            double xLength = xRange.getLength();
            double yLength = yRange.getLength();

            double xOrigin = xRange.getLowerBound();
            double yOrigin = yRange.getLowerBound();        

            double xIncrement = xLength/(columnCount - 1.);
            double yIncrement = yLength/(rowCount - 1.);

            Sampling xCoordsSampling = new Sampling(columnCount, xIncrement, xOrigin);
            Sampling yCoordsSampling = new Sampling(rowCount, yIncrement, yOrigin);

            sibsonInterpolator.setBounds(xCoordsSampling, yCoordsSampling);

            float[][] interpolated = sibsonInterpolator.interpolate(xCoordsSampling, yCoordsSampling);
            double[][] interpolatedDoubles = ArrayUtilities.toDouble(interpolated);

            return interpolatedDoubles;
        }
    };

    private static final double TOLERANCE = 1e-12;

    private String name;

    InterpolationMethod2D(String name)
    {
        this.name = name;
    }

    public abstract double getValue(Channel2DData channel, double x, double y);
    public abstract double[] getProfileValues(Channel2DData channel, Shape profile, int n);
    public abstract double[][] getCrossSection(Channel2DData channel, double[][] profilePoints);
    public abstract double[][] getGriddedData(Channel2DData channel, int rowCount, int columnCount);

    @Override
    public String toString()
    {
        return name;
    }
}
