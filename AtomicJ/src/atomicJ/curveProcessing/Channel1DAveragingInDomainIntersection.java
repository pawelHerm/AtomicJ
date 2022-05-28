package atomicJ.curveProcessing;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.Range;

import atomicJ.analysis.InterpolationMethod1D;
import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DDataWithErrors;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.GridChannel1DData.GridChannel1DDataWithErrorBars;
import atomicJ.data.units.Quantity;
import atomicJ.utilities.Validation;
import atomicJ.geometricSets.ClosedInterval;
import atomicJ.geometricSets.Interval;
import atomicJ.geometricSets.SetIntersectionUtilities;

public class Channel1DAveragingInDomainIntersection implements Channel1DMultiTransformation
{
    private final InterpolationMethod1D interpolationMethod;
    private final ErrorBarType errorBarType;
    private final int noOfPointsInAveragedCurve;

    public Channel1DAveragingInDomainIntersection(InterpolationMethod1D interpolationMethod, int noOfPointsInAveragedCurve, ErrorBarType errorBarType)
    {
        this.interpolationMethod = Validation.requireNonNullParameterName(interpolationMethod, "interpolationMethod");
        this.noOfPointsInAveragedCurve = Validation.requireNonNegativeParameterName(noOfPointsInAveragedCurve, "noOfPointsInAveragedCurve");
        this.errorBarType = Validation.requireNonNullParameterName(errorBarType, "errorBarType");
    }

    /**
     * Each Channel1DData object contained in the list passed as the {@code channels} argument must have the X and Y units
     * compatible with the units of every other object in the {@code channels} list
     * If the units are compatible but the names of the quantities differ between channels,
     * then the returned {@code Channel1DData} uses the names of the quantities of the first element of {@code channels}
     */
    @Override
    public Channel1DDataWithErrors transform(List<Channel1DData> channels) 
    {   
        Validation.requireNonNullAndNonEmptyParameterName(channels, "channels");

        List<Channel1DData> channelsWithTheSameUnits = getChannelDataWhoseQuantitiesUseTheSameUnits(channels);

        List<ClosedInterval> xIntervals = new ArrayList<>();

        for(Channel1DData channelData : channelsWithTheSameUnits)
        {
            Range xRange = channelData.getXRange();
            ClosedInterval interval = new ClosedInterval(xRange.getLowerBound(), xRange.getUpperBound());
            xIntervals.add(interval);
        }

        Interval intersection = SetIntersectionUtilities.calculateIntersectionOfIntervals(xIntervals);

        List<GridChannel1DData> interpolatedChannels = new ArrayList<>();

        for(Channel1DData ch : channelsWithTheSameUnits)
        {
            GridChannel1DData interpolatedChannel = interpolationMethod.getGriddedData(ch, intersection, noOfPointsInAveragedCurve);
            interpolatedChannels.add(interpolatedChannel);
        }

        GridChannel1DData firstChannel = interpolatedChannels.get(0);
        Grid1D grid = firstChannel.getGrid();//all interpolated channels will have the same grid
        Quantity firstChannelYQuantity = firstChannel.getYQuantity();

        int channelCount = interpolatedChannels.size();

        MemoryEfficientStatisticsCalculator statisticsCalculator = new MemoryEfficientStatisticsCalculator(noOfPointsInAveragedCurve);

        for(int i = 0; i < channelCount; i++)
        {
            double[] yValues = interpolatedChannels.get(i).getYCoordinates();
            statisticsCalculator.addANewSampleToEachCurvePosition(yValues);
        }

        double[] means = statisticsCalculator.getMeans();
        double[] ssValues = statisticsCalculator.getSumsOfSquares();
        double[] errorValues = errorBarType.getErrorValues(ssValues, channelCount);

        Channel1DDataWithErrors averageChannel = new GridChannel1DDataWithErrorBars(means, errorValues, grid, firstChannelYQuantity, errorBarType);

        return averageChannel;
    }


    private List<Channel1DData> getChannelDataWhoseQuantitiesUseTheSameUnits(List<Channel1DData> channels)
    {       
        List<Channel1DData> channelsWithTheSameUnits = new ArrayList<>();

        Channel1DData firstChannel = channels.get(0);//we already checked that the channels list is not empty
        Quantity firstChannelXQuantity = firstChannel.getXQuantity();
        Quantity firstChannelYQuantity = firstChannel.getYQuantity();

        int n = channels.size(); 

        for(int i = 1; i < n; i++)
        {
            Channel1DData channel = channels.get(i).changeToCompatibleQuantities(firstChannelXQuantity, firstChannelYQuantity);
            channelsWithTheSameUnits.add(channel);
        }

        return channelsWithTheSameUnits;
    }

    private static class MemoryEfficientStatisticsCalculator
    {
        private final double[] means;
        private final double[] sumsOfSquares;
        private int addedSamplesCount;

        private MemoryEfficientStatisticsCalculator(int noOfPointsInCurve)
        {
            this.means = new double[noOfPointsInCurve];
            this.sumsOfSquares = new double[noOfPointsInCurve];
        }

        //From Knuth D. "Art of Computer Programming", vol. 2. "Seminumerical Algorithms", p. 232.
        private void addANewSampleToEachCurvePosition(double[] samples)
        {
            int n = samples.length;

            addedSamplesCount++;

            for(int i = 0; i < n; i++)
            {
                double x = samples[i];
                double meanOld = means[i];
                double delta = x - meanOld;
                double meanNew = meanOld + delta/addedSamplesCount;
                sumsOfSquares[i] = sumsOfSquares[i] + delta*(x - meanNew);
                means[i] = meanNew;
            }
        }

        public double[] getMeans()
        {
            return means;
        }

        public double[] getSumsOfSquares()
        {
            return sumsOfSquares;
        }
    }
}
