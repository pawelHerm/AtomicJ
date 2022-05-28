package atomicJ.readers.innova;

import java.util.Arrays;
import java.util.Scanner;

import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.AbscissaComparator;

public class InnovaSpectroscopyTextData
{
    public static final String NAME = "Data";

    private final InnovaINISection section;
    private double[][][] data;

    public InnovaSpectroscopyTextData(InnovaINISection dataSection, InnovaSeriesLayout seriesLayout)
    {
        this.section = dataSection;
        int seriesCount = seriesLayout.getSeriesCount();
        int maxPointCount = seriesLayout.getMaximalPointCount();
        int channelCount = seriesLayout.getChannelCount();

        initValues(seriesCount, maxPointCount, channelCount);
    }

    public int getReadInSeriesCount()
    {
        return data.length;
    }

    private void initValues(int seriesCount, int maxPointCount, int columnCount)
    {
        String text = section.getText();

        double[][][] readInValues = new double[seriesCount][maxPointCount][];

        try (Scanner scanner = new Scanner(text))
        {
            scanner.useDelimiter("[\\t]|[\\r\\n]+");

            outerLoop:
                for(int i = 0; i<maxPointCount;i++)
                {
                    for(int s = 0; s < seriesCount;s++)
                    {
                        double[] row = new double[columnCount];
                        readInValues[s][i] = row;

                        for(int j = 0; j <columnCount;j++)
                        {
                            if(scanner.hasNext())
                            {
                                String nextToken = scanner.next(); 
                                boolean tokenEmpty = nextToken.trim().isEmpty(); //if the the nextToken were not trimmed, then the isEmpty() would return false for string of whitespaces, but for our purposes such strting should be treated like an empty one

                                double val = tokenEmpty? Double.NaN : Double.parseDouble(nextToken);
                                row[j] = val;
                            }
                            else
                            {
                                break outerLoop;
                            }
                        }                  
                    }               
                }
        } 

        this.data = readInValues;
    }

    public double[][] extractPointArray(int seriesIndex, double xFactor, double yFactor, int xColumn, int yColumn, double xBegin, double xEnd)
    {
        double[][] xyData = data[seriesIndex];

        int nonNaNPointsCount = 0;

        int pointCount = xyData.length;
        double[][] pointsNonNaN = new double[pointCount][];

        for(int i = 0; i<pointCount; i++)
        {
            double[] row = xyData[i];

            double x = xFactor*row[xColumn];
            double y = yFactor*row[yColumn];
            if(!Double.isNaN(x) && !Double.isNaN(y))
            {
                pointsNonNaN[nonNaNPointsCount++] = new double[] {x,y};
            }            
        }

        if(nonNaNPointsCount < 2)
        {
            return new double[][] {};
        }

        int finalPointCount = 0;

        pointsNonNaN = (nonNaNPointsCount == pointCount) ? pointsNonNaN : Arrays.copyOfRange(pointsNonNaN, 0, nonNaNPointsCount);
        Arrays.sort(pointsNonNaN, new AbscissaComparator());

        double[] distanceToNeighbour = getShortestDistanceToNeighbour(pointsNonNaN);

        double medianDistance = DescriptiveStatistics.median(distanceToNeighbour);
        double tolerance = Math.abs(10*medianDistance);

        double[][] pointsCleared = new double[nonNaNPointsCount][];

        for(int i = 0; i<nonNaNPointsCount; i++)
        {
            double[] row = pointsNonNaN[i];

            if(distanceToNeighbour[i] < tolerance)
            {
                pointsCleared[finalPointCount++] = row;
            }         
        }

        double[][] correctedPoints = (nonNaNPointsCount == finalPointCount) ? pointsCleared : Arrays.copyOfRange(pointsCleared, 0, finalPointCount);

        return correctedPoints;
    }

    private double[] getShortestDistanceToNeighbour(double[][] points)
    {
        double previousX = points[0][0];
        double nextX = points[1][0];
        int n = points.length;

        double[] differences = new double[n];
        differences[0] = nextX - previousX;

        for(int i = 1; i< n - 1;i++)
        {
            double x = nextX;
            nextX = points[i + 1][0];
            differences[i] = Math.min(nextX - x, x - previousX);
            previousX = x;
        }

        differences[n - 1] = nextX - previousX;
        return differences;
    }

    /*
     *     public double[][] extractPointArray(int seriesIndex, double xFactor, double yFactor, int xColumn, int yColumn, double xBegin, double xEnd)
    {
        double[][] xyData = data[seriesIndex];
        int pointCount = xyData.length;
        double[][] points = new double[pointCount][];

        int correctPointCount = 0;

        double rampLength = (xEnd - xBegin);
        double avarageIncrement = rampLength/(pointCount - 1);
        double tolerance = Math.abs(0.5*rampLength);

        for(int i = 0; i<pointCount; i++)
        {
            double[] row = xyData[i];

            double x = xFactor*row[xColumn];
            double y = yFactor*row[yColumn];
            if(!Double.isNaN(x) && !Double.isNaN(y))
            {

                double expectedX = xBegin + i*avarageIncrement;

                if(Math.abs(expectedX - x) < tolerance)
                {
                    points[correctPointCount] = new double[] {x, y};
                    correctPointCount++;
                }
            }
        }

        double[][] correctedPoints = (pointCount == correctPointCount) ? points : Arrays.copyOfRange(points, 0, correctPointCount);

        return correctedPoints;
    }
     */

    public String getName()
    {
        return NAME;
    }
}
