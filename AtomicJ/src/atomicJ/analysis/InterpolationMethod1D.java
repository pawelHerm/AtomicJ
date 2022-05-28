package atomicJ.analysis;

import atomicJ.curveProcessing.SortX1DTransformation;
import atomicJ.data.Channel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.geometricSets.Interval;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.Validation;


public enum InterpolationMethod1D 
{
    LINEAR("Linear") 
    {
        @Override
        public double getValue(double[][] data, double x)
        {
            double y = Double.NaN;

            if(isWithinDataDomain(data, x))
            {
                int largerXIndex = ArrayUtilities.binarySearchAscendingX(data, 0, data.length, x);
                if(largerXIndex == 0)
                {
                    return data[largerXIndex][1];
                }

                double t = (x - data[largerXIndex - 1][0])/(data[largerXIndex][0] - data[largerXIndex - 1][0]);
                y = t*data[largerXIndex][1] + (1 - t)*data[largerXIndex - 1][1];
            }

            return y; 
        } 

        private double getValueForGridChannelData(GridChannel1DData gridChannelData, double x)
        {
            double y = Double.NaN;

            Grid1D grid = gridChannelData.getGrid();
            double[] gridData = gridChannelData.getData();

            if(grid.isWithinDomain(x))
            {
                int column1 = grid.getIndexFloorWithinBounds(x);
                int column2 = grid.getIndexCeilingWithinBounds(x);

                if(column1 != column2)
                {
                    double x1 = grid.getArgumentVal(column1);
                    double x2 = grid.getArgumentVal(column2);

                    double f1 = gridData[column1];
                    double f2 = gridData[column2];

                    y = f1 + (f2-f1)*(x - x1)/(x2 - x1);
                }
                else
                {
                    y = gridData[column1];               
                }
            }

            return y; 
        }

        @Override
        public GridChannel1DData getGriddedData(Channel1DData channelData, int columnCountNew)
        {            
            Validation.requireNonNegativeParameterName(columnCountNew, "columnCountNew");

            if(columnCountNew == 0 || channelData.isEmpty())
            {
                return new GridChannel1DData(new double[] {}, new Grid1D(0, channelData.getXRange().getLowerBound(), 0, channelData.getXQuantity()), channelData.getYQuantity());
            }           

            if(channelData instanceof GridChannel1DData)
            {
                return getGriddedDataFromGriddedData((GridChannel1DData)channelData, columnCountNew);
            }

            return getGriddedDataFromPossiblyScatteredData(channelData, columnCountNew);
        }

        private GridChannel1DData getGriddedDataFromGriddedData(GridChannel1DData channelData, int columnCountNew)
        {          
            Grid1D grid = channelData.getGrid();         

            if(grid.getIndexCount() == columnCountNew)
            {
                return channelData;
            }

            Grid1D gridNew = grid.changeDensity(columnCountNew);

            double origin = gridNew.getOrigin();
            double incrementNew = gridNew.getIncrement();

            double[] data = new double[columnCountNew];        

            for(int i = 0; i < columnCountNew; i++)
            {               
                double x = origin + i*incrementNew;                

                data[i] = getValueForGridChannelData(channelData, x);               
            }

            GridChannel1DData gridChannelTransformed = new GridChannel1DData(data, gridNew, channelData.getYQuantity());

            return gridChannelTransformed;
        }


        private GridChannel1DData getGriddedDataFromGriddedData(GridChannel1DData channelData, double xMin, double xMax, int columnCountNew)
        {            
            Grid1D grid = channelData.getGrid();

            double xMinOld= grid.getMinimum();
            double xMaxOld = grid.getMaximum();
            double columnCountOld = grid.getIndexCount();

            if(columnCountOld == columnCountNew && Double.compare(xMinOld, xMin) == 0 && Double.compare(xMaxOld, xMax) == 0)
            {
                return channelData;
            }

            double incrementNew = columnCountNew > 1 ? (xMax - xMin)/(columnCountNew - 1) :1;

            double[] dataNew = new double[columnCountNew];        

            for(int i = 0; i<columnCountNew; i++)
            {
                double x = xMin + i*incrementNew;
                dataNew[i] = getValueForGridChannelData(channelData, x);               
            }

            Grid1D gridNew = new Grid1D(incrementNew, xMin, columnCountNew, grid.getQuantity());
            GridChannel1DData gridChannelTransformed = new GridChannel1DData(dataNew, gridNew, channelData.getYQuantity());

            return gridChannelTransformed;
        }


        private GridChannel1DData getGriddedDataFromPossiblyScatteredData(Channel1DData channelData,  int columnCountNew)
        {
            return getGriddedDataFromPossiblyScatteredData(channelData, channelData.getXMinimum(),channelData.getXMaximum(), columnCountNew);
        }


        private GridChannel1DData getGriddedDataFromPossiblyScatteredData(Channel1DData channelData, double xMin, double xMax, int columnCountNew)
        {
            boolean initiallyOrdered = channelData.getXOrder() != null;
            Channel1DData sortedChannelData = initiallyOrdered ? channelData : new SortX1DTransformation(SortedArrayOrder.ASCENDING).transform(channelData);

            SortedArrayOrder sortedChannelDataOrder = sortedChannelData.getXOrder();

            double origin = SortedArrayOrder.ASCENDING.equals(sortedChannelDataOrder) ? xMin : xMax;
            double incrementNew = 1;
            if(columnCountNew > 1)
            {
                incrementNew = SortedArrayOrder.ASCENDING.equals(sortedChannelDataOrder) ? (xMax - xMin)/(columnCountNew - 1) : (xMin - xMax)/(columnCountNew - 1);
            }

            double[][] sortedData = sortedChannelData.getPointsCopy();
            int columnCountOriginal = sortedChannelData.getItemCount();

            double[] dataNew = new double[columnCountNew];

            if(SortedArrayOrder.ASCENDING.equals(sortedChannelDataOrder))
            {
                for(int i = 0; i<columnCountNew; i++)
                {
                    double x = origin + i*incrementNew;                   

                    int rightIndex = Math.min(columnCountOriginal - 1, ArrayUtilities.binarySearchAscendingX(sortedData, 0, sortedData.length, x));
                    if(rightIndex == 0) //right index equals 0 for i = 0, it may equal zero for i = 1, if the first and the second indices have the same x
                    {
                        dataNew[i] = sortedData[rightIndex][1];
                        continue;
                    }

                    int leftIndex = rightIndex - 1;  

                    double t = (x - sortedData[leftIndex][0])/(sortedData[rightIndex][0] - sortedData[leftIndex][0]);

                    dataNew[i] = t*sortedData[rightIndex][1] + (1 - t)*sortedData[leftIndex][1];
                }
            }
            else if(SortedArrayOrder.DESCENDING.equals(sortedChannelDataOrder))
            {
                for(int i = 0; i<columnCountNew; i++)
                {
                    double x = origin + i*incrementNew;

                    int rightIndex = Math.min(columnCountOriginal - 1, ArrayUtilities.binarySearchDescendingX(sortedData, 0, sortedData.length, x));
                    if(rightIndex == 0)
                    {
                        dataNew[i] = sortedData[rightIndex][1];
                        continue;
                    }

                    int leftIndex = rightIndex - 1;  //right index may equal zero for i = 1, if the first and the second indices have the same x

                    double t = (x - sortedData[leftIndex][0])/(sortedData[rightIndex][0] - sortedData[leftIndex][0]);
                    dataNew[i] = t*sortedData[rightIndex][1] + (1 - t)*sortedData[leftIndex][1];
                }
            }
            else throw new IllegalArgumentException("Unknown SortedArrayOrder " + sortedChannelDataOrder);

            Grid1D grid = new Grid1D(incrementNew, origin, columnCountNew, sortedChannelData.getXQuantity());    
            GridChannel1DData gridChannel = new GridChannel1DData(dataNew, grid, sortedChannelData.getYQuantity());

            return gridChannel;
        }

        @Override
        public double[][] convertTo2DRegularArray(double[][] sortedPoints, int arrayLengthNew, SortedArrayOrder pointOrder)
        {
            Validation.requireNonNegativeParameterName(arrayLengthNew, "arrayLengthNew");

            if(arrayLengthNew == 0 || sortedPoints.length == 0)
            {
                return new double[][] {};
            }           

            double[][] arrayNew = new double[arrayLengthNew][];

            int n = sortedPoints.length;

            double x0 = sortedPoints[0][0];
            double xn = sortedPoints[n - 1][0];
            double increment = (xn - x0)/(arrayLengthNew - 1);

            arrayNew[0] = new double[] {sortedPoints[0][0], sortedPoints[0][1]};

            if(SortedArrayOrder.ASCENDING.equals(pointOrder))
            {
                for(int i = 1; i<arrayLengthNew; i++)
                {
                    double x = x0 + i*increment;
                    int rightIndex = Math.min(n - 1, ArrayUtilities.binarySearchAscendingX(sortedPoints, 0, sortedPoints.length, x));

                    double t = (x - sortedPoints[rightIndex - 1][0])/(sortedPoints[rightIndex][0] - sortedPoints[rightIndex - 1][0]);
                    double y = t*sortedPoints[rightIndex][1] + (1 - t)*sortedPoints[rightIndex - 1][1];
                    arrayNew[i] = new double[] {x, y};
                }
            }
            else if(SortedArrayOrder.DESCENDING.equals(pointOrder))
            {
                for(int i = 1; i<arrayLengthNew; i++)
                {
                    double x = x0 + i*increment;
                    int rightIndex = Math.min(n - 1, ArrayUtilities.binarySearchDescendingX(sortedPoints, 0, sortedPoints.length, x));

                    double t = (x - sortedPoints[rightIndex - 1][0])/(sortedPoints[rightIndex][0] - sortedPoints[rightIndex - 1][0]);
                    double y = t*sortedPoints[rightIndex][1] + (1 - t)*sortedPoints[rightIndex - 1][1];
                    arrayNew[i] = new double[] {x, y};
                }
            }
            else throw new IllegalArgumentException("Unknown SortedArrayOrder " + pointOrder);

            return arrayNew;
        }

        @Override
        public GridChannel1DData getGriddedData(Channel1DData channelData, double xMin, double xMax, int columnCountNew) 
        {
            Validation.requireNonNegativeParameterName(columnCountNew, "columnCountNew");

            if(columnCountNew == 0 || channelData.isEmpty())
            {
                return new GridChannel1DData(new double[] {}, new Grid1D(0, channelData.getXRange().getLowerBound(), 0, channelData.getXQuantity()), channelData.getYQuantity());
            }           

            if(channelData instanceof GridChannel1DData)
            {
                return getGriddedDataFromGriddedData((GridChannel1DData)channelData, xMin, xMax, columnCountNew);
            }

            return getGriddedDataFromPossiblyScatteredData(channelData, xMin, xMax, columnCountNew);
        }

    },
    NEAREST_NEIGHBOUR("Nearest neighbour") 
    {
        //data must be sorted in ascending order
        @Override
        public double getValue(double[][] data, double x) 
        {
            double y = Double.NaN;

            if(isWithinDataDomain(data, x))
            {
                int largerXIndex = ArrayUtilities.binarySearchAscendingX(data, 0, data.length, x);
                if(largerXIndex == 0)
                {
                    return data[largerXIndex][1];
                }

                double distanceRight = data[largerXIndex][0] - x;
                double distanceLeft = x - data[largerXIndex - 1][0];
                y = distanceRight <  distanceLeft ? data[largerXIndex][1] : data[largerXIndex - 1][1];
            }

            return y;
        }

        @Override
        public GridChannel1DData getGriddedData(Channel1DData channelData, int columnCountNew)
        {
            Validation.requireNonNegativeParameterName(columnCountNew, "columnCountNew");

            if(columnCountNew == 0 || channelData.isEmpty())
            {
                return new GridChannel1DData(new double[] {}, new Grid1D(0, channelData.getXRange().getLowerBound(), 0, channelData.getXQuantity()), channelData.getYQuantity());
            }           

            if(channelData instanceof GridChannel1DData)
            {
                return getGriddedDataFromGriddedChannelData((GridChannel1DData)channelData, columnCountNew);
            }

            return getGriddedDataFromPossiblyScatteredData(channelData, columnCountNew);
        }

        @Override
        public GridChannel1DData getGriddedData(Channel1DData channelData, double xMin, double xMax, int columnCountNew)
        {
            Validation.requireNonNegativeParameterName(columnCountNew, "columnCountNew");

            if(columnCountNew == 0 || channelData.isEmpty())
            {
                return new GridChannel1DData(new double[] {}, new Grid1D(0, xMin, 0, channelData.getXQuantity()), channelData.getYQuantity());
            }           

            if(channelData instanceof GridChannel1DData)
            {
                return getGriddedDataFromGriddedChannelData((GridChannel1DData)channelData, xMin, xMax, columnCountNew);
            }

            return getGriddedDataFromPossiblyScatteredData(channelData, xMin, xMax, columnCountNew);
        }

        private GridChannel1DData getGriddedDataFromGriddedChannelData(GridChannel1DData gridChannel, int columnCountNew)
        {            
            Grid1D grid = gridChannel.getGrid();

            if(grid.getIndexCount() == columnCountNew)
            {
                return gridChannel;
            }

            double[] gridData = gridChannel.getData();

            Grid1D gridNew = grid.changeDensity(columnCountNew);

            double origin = gridNew.getOrigin();
            double incrementNew = gridNew.getIncrement();

            double[] data = new double[columnCountNew];        

            for(int i = 0; i<columnCountNew; i++)
            {
                double x = origin + i*incrementNew;
                int column = grid.getClosestIndexWithinDataBounds(x);
                data[i] = gridData[column];
            }

            GridChannel1DData gridChannelTransformed = new GridChannel1DData(data, gridNew, gridChannel.getYQuantity());

            return gridChannelTransformed;
        }

        private GridChannel1DData getGriddedDataFromGriddedChannelData(GridChannel1DData gridChannel, double xMin, double xMax, int columnCountNew)
        {            
            Grid1D grid = gridChannel.getGrid();

            double xMinOld= grid.getMinimum();
            double xMaxOld = grid.getMaximum();
            double columnCountOld = grid.getIndexCount();

            if(columnCountOld == columnCountNew && Double.compare(xMinOld, xMin)==0 && Double.compare(xMaxOld, xMax) == 0)
            {
                return gridChannel;
            }

            double incrementNew = columnCountNew > 1 ? (xMax - xMin)/(columnCountNew - 1) :1;
            Grid1D gridNew = new Grid1D(incrementNew, xMin, columnCountNew, grid.getQuantity());

            double[] gridData = gridChannel.getData();

            double[] dataNew = new double[columnCountNew];        

            for(int i = 0; i<columnCountNew; i++)
            {
                double x = xMin + i*incrementNew;
                int column = grid.getClosestIndexWithinDataBounds(x);
                dataNew[i] = gridData[column];
            }

            GridChannel1DData gridChannelTransformed = new GridChannel1DData(dataNew, gridNew, gridChannel.getYQuantity());

            return gridChannelTransformed;
        }


        private GridChannel1DData getGriddedDataFromPossiblyScatteredData(Channel1DData channelData,  int columnCountNew)
        {
            return  getGriddedDataFromPossiblyScatteredData(channelData, channelData.getXMinimum(),channelData.getXMaximum(), columnCountNew);
        }


        private GridChannel1DData getGriddedDataFromPossiblyScatteredData(Channel1DData channelData, double xMin, double xMax, int columnCountNew)
        {
            boolean initiallyOrdered = channelData.getXOrder() != null;
            Channel1DData sortedChannelData = initiallyOrdered ? channelData : new SortX1DTransformation(SortedArrayOrder.ASCENDING).transform(channelData);
            SortedArrayOrder sortedChannelDataOrder = sortedChannelData.getXOrder();

            double x0 = SortedArrayOrder.ASCENDING.equals(sortedChannelDataOrder) ? xMin : xMax;
            double incrementNew = 1;
            if(columnCountNew > 1)
            {
                incrementNew = SortedArrayOrder.ASCENDING.equals(sortedChannelDataOrder) ? (xMax - xMin)/(columnCountNew - 1) : (xMin - xMax)/(columnCountNew - 1);
            }

            double[][] sortedPoints = sortedChannelData.getPointsCopy();

            double[] dataNew = new double[columnCountNew];

            int n = sortedPoints.length;

            if(SortedArrayOrder.ASCENDING.equals(sortedChannelDataOrder))
            {
                for(int i = 0; i < columnCountNew; i++)
                {
                    double x = x0 + i*incrementNew;
                    int largerXIndex = ArrayUtilities.binarySearchAscendingX(sortedPoints, 0, n, x);

                    if(largerXIndex == n)
                    {
                        dataNew[i] = sortedPoints[largerXIndex - 1][1];
                    }
                    else if(largerXIndex == 0)
                    {
                        dataNew[i] = sortedPoints[largerXIndex][1];
                    }
                    else
                    {
                        double distanceRight = sortedPoints[largerXIndex][0] - x;
                        double distanceLeft = x - sortedPoints[largerXIndex - 1][0];
                        dataNew[i] = distanceRight <  distanceLeft ? sortedPoints[largerXIndex][1] : sortedPoints[largerXIndex - 1][1];
                    }
                }
            }
            else if(SortedArrayOrder.DESCENDING.equals(sortedChannelDataOrder))
            {
                for(int i = 0; i<columnCountNew; i++)
                {                  
                    double x = x0 + i*incrementNew;
                    int smallerXIndex = ArrayUtilities.binarySearchDescendingX(sortedPoints, 0, n, x);

                    if(smallerXIndex == n)
                    {
                        dataNew[i] = sortedPoints[smallerXIndex - 1][1];
                    }
                    else if(smallerXIndex == 0)
                    {
                        dataNew[i] = sortedPoints[smallerXIndex][1];
                    }
                    else
                    {
                        double distanceRight = sortedPoints[smallerXIndex][0] - x;
                        double distanceLeft = x - sortedPoints[smallerXIndex - 1][0];
                        dataNew[i] = distanceRight <  distanceLeft ? sortedPoints[smallerXIndex][1] : sortedPoints[smallerXIndex - 1][1];
                    }
                }
            }
            else throw new IllegalArgumentException("Unknown SortedArrayOrder " + sortedChannelDataOrder);

            Grid1D grid = new Grid1D(incrementNew, x0, columnCountNew, sortedChannelData.getXQuantity());    
            GridChannel1DData gridChannel = new GridChannel1DData(dataNew, grid, sortedChannelData.getYQuantity());

            return gridChannel;
        }


        @Override
        public double[][] convertTo2DRegularArray(double[][] sortedPoints, int arrayLengthNew, SortedArrayOrder pointOrder)
        {
            Validation.requireNonNegativeParameterName(arrayLengthNew, "arrayLengthNew");

            int n = sortedPoints.length;

            if(arrayLengthNew == 0 || n == 0)
            {
                return new double[][] {};
            }           

            double[][] arrayNew = new double[arrayLengthNew][];

            double x0 = sortedPoints[0][0];
            double xn = sortedPoints[n - 1][0];
            double increment =  (arrayLengthNew > 1) ? (xn - x0)/(arrayLengthNew - 1) :1 ;

            if(SortedArrayOrder.ASCENDING.equals(pointOrder))
            {
                for(int i = 0; i<arrayLengthNew; i++)
                {
                    double x = x0 + i*increment;
                    int largerXIndex = ArrayUtilities.binarySearchAscendingX(sortedPoints, 0, n, x);

                    if(largerXIndex == 0)
                    {
                        arrayNew[i] = new double[] {x, sortedPoints[0][1]};
                    }
                    else
                    {
                        double distanceRight = sortedPoints[largerXIndex][0] - x;
                        double distanceLeft = x - sortedPoints[largerXIndex - 1][0];
                        double y = distanceRight <  distanceLeft ? sortedPoints[largerXIndex][1] : sortedPoints[largerXIndex - 1][1];
                        arrayNew[i] = new double[] {x, y};
                    }
                }
            }

            else if(SortedArrayOrder.DESCENDING.equals(pointOrder))
            {
                for(int i = 0; i<arrayLengthNew; i++)
                {
                    double x = x0 + i*increment;
                    int smallerXIndex = ArrayUtilities.binarySearchDescendingX(sortedPoints, 0, n, x);
                    if(smallerXIndex == 0)
                    {
                        arrayNew[i] = new double[] {x, sortedPoints[0][1]};
                    }
                    else
                    {
                        double distanceRight = sortedPoints[smallerXIndex][0] - x;
                        double distanceLeft = x - sortedPoints[smallerXIndex - 1][0];
                        double y = distanceRight <  distanceLeft ? sortedPoints[smallerXIndex][1] : sortedPoints[smallerXIndex - 1][1];
                        arrayNew[i] = new double[] {x, y};
                    }
                }
            }
            else throw new IllegalArgumentException("Unknown SortedArrayOrder " + pointOrder);

            return arrayNew;
        }
    };

    private String prettyName;

    InterpolationMethod1D(String name)
    {
        this.prettyName = name;
    }

    public abstract double getValue(double[][] data, double x);
    public abstract GridChannel1DData getGriddedData(Channel1DData data, int columnCountNew);
    public abstract GridChannel1DData getGriddedData(Channel1DData data, double xMin, double xMax, int columnCountNew);

    public GridChannel1DData getGriddedData(Channel1DData data, Interval interval, int columnCountNew)
    {
        if(interval.isAnyElementInfinite())
        {
            throw new IllegalArgumentException("The argument interval should not contain infinite values");
        }

        return getGriddedData(data, interval.getSmallestContainedDoubleValue(), interval.getGreatestContainedDoubleValue(), columnCountNew);
    }

    public abstract double[][] convertTo2DRegularArray(double[][] data, int arrayLengthNew, SortedArrayOrder pointOrder);

    @Override
    public String toString()
    {
        return prettyName;
    }

    //data must be in the ascending order in terms of the x-values
    private static boolean isWithinDataDomain(double[][] data, double x)
    {
        int n = data.length;
        if(n == 0)
        {
            return false;
        }

        double xMin = data[0][0];
        double xMax = data[n - 1][0];

        boolean within = x >= xMin && x <= xMax;
        return within;
    }
}
