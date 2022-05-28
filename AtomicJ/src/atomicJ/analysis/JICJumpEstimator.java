
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

package atomicJ.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import atomicJ.curveProcessing.Channel1DDataInROITransformation;
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.LocalRegressionTransformation;
import atomicJ.curveProcessing.SortX1DTransformation;
import atomicJ.curveProcessing.SpanType;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.IndexRange;
import atomicJ.data.Point1DData;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.statistics.JICPenalty;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.GeometryUtilities;

public class JICJumpEstimator implements ForceEventEstimator 
{
    private final Channel1DDataTransformation sorter = new SortX1DTransformation(SortedArrayOrder.ASCENDING);

    private final double span ;
    private final SpanType spanType;
    private final int polynomialDegree;
    private final int robustnessIterationsCount = 0;
    private LocalRegressionWeightFunction weightFunction = LocalRegressionWeightFunction.TRICUBE;    
    private final JICPenalty jicPenalty;

    public JICJumpEstimator(int polynomialDegree, double span, SpanType spanType, LocalRegressionWeightFunction weightFunction, JICPenalty jicPenalty)
    {
        this.polynomialDegree = polynomialDegree;
        this.span = span;
        this.spanType = spanType;
        this.weightFunction = weightFunction;
        this.jicPenalty = jicPenalty;
    }

    @Override
    public List<ForceEventEstimate> getEventEstimates(Channel1DData approachBranch, Channel1DData withdrawChannel, double domainMin, double domainMax)
    {         
        int n = withdrawChannel.getItemCount();

        Channel1DData channelSorted = sorter.transform(withdrawChannel);
        int windowWidth = spanType.getSpanLengthInPoints(span, n);

        Channel1DDataTransformation leftOpenSmoother = new LocalRegressionTransformation(span, SpanGeometry.LEFT_OPEN, spanType, robustnessIterationsCount, polynomialDegree, 0, weightFunction);
        Channel1DDataTransformation rightClosedSmoother = new LocalRegressionTransformation(span, SpanGeometry.RIGHT, spanType, robustnessIterationsCount, polynomialDegree, 0, weightFunction);
        Channel1DDataTransformation centralSmoother = new LocalRegressionTransformation(span, SpanGeometry.NEAREST_NEIGHBOUR, spanType, robustnessIterationsCount, polynomialDegree, 0, weightFunction);

        Channel1DData leftSmoothedChannel = leftOpenSmoother.transform(channelSorted);
        Channel1DData rightSmoothedChannel = rightClosedSmoother.transform(channelSorted);

        double[][] leftSmoothedChannelYData = leftSmoothedChannel.getPoints();
        double[][] rightSmoothedChannelYData = rightSmoothedChannel.getPoints();


        double[][] absDifferences = new double[n][];

        for(int i = 0; i<n;i++)
        {
            double[] leftSmoothedPoint = leftSmoothedChannelYData[i];

            double x = leftSmoothedPoint[0];
            double leftY = leftSmoothedPoint[1];
            double rightY = rightSmoothedChannelYData[i][1];

            absDifferences[i] = new double[] {x, Math.abs(rightY - leftY)};            
        }

        List<IndexRange> excludedRanges = new ArrayList<>();
        List<Integer> possibleEventsIndices = new ArrayList<>();

        while(n > calculateTotalLength(excludedRanges))
        {            
            int maxDifferenceIndex = -1;
            double maxAbsDifference = Double.NEGATIVE_INFINITY;

            int nextMin = 0;
            for(IndexRange excludedRange : excludedRanges)
            {
                int min = nextMin;
                int max = excludedRange.getMinIndex();

                for(int i = min; i < max; i++)
                {
                    double currentDifference = absDifferences[i][1];
                    if(currentDifference > maxAbsDifference)
                    {
                        maxAbsDifference = currentDifference;
                        maxDifferenceIndex = i;
                    }
                }

                nextMin = excludedRange.getMaxIndex() + 1;
            }

            for(int i = nextMin; i < n; i++)
            {
                double currentDifference = absDifferences[i][1];
                if(currentDifference > maxAbsDifference)
                {
                    maxAbsDifference = currentDifference;
                    maxDifferenceIndex = i;
                }
            }

            possibleEventsIndices.add(maxDifferenceIndex);
            excludedRanges.add(new IndexRange(Math.max(0, maxDifferenceIndex - windowWidth/2), Math.min(n - 1, maxDifferenceIndex + windowWidth/2)));
            excludedRanges = IndexRange.simplify(excludedRanges);

            Collections.sort(excludedRanges, new Comparator<IndexRange>() 
            {
                @Override
                public int compare(IndexRange range1, IndexRange range2) 
                {
                    return Integer.compare(range1.getMinIndex(), range2.getMinIndex());
                }
            });
        }


        Channel1DData continuousChannel  = channelSorted;
        double penaltyTerm = 0;

        double hn = windowWidth/(2.*(n - 1.));
        double penaltyAdjustmentFactor = jicPenalty.getAdjustmentFactor(n, hn);

        double initialDistanceTerm = calculateDistanceTerm(continuousChannel, centralSmoother.transform(continuousChannel));

        double[] criteria = new double[possibleEventsIndices.size() + 1];
        criteria[0] = initialDistanceTerm;

        for(int i = 0; i< possibleEventsIndices.size();i++)
        {
            int eventIndex = possibleEventsIndices.get(i);

            double absDifference = absDifferences[eventIndex][1];
            Channel1DDataTransformation tr = new Add1DTransformation(new IndexRange(eventIndex, n - 1), -absDifference);
            continuousChannel = tr.transform(continuousChannel);

            double distanceTerm = calculateDistanceTerm(continuousChannel, centralSmoother.transform(continuousChannel));

            penaltyTerm += penaltyAdjustmentFactor/absDifference;

            criteria[i + 1] = penaltyTerm + distanceTerm;            
        }

        int expectedJumpCount = ArrayUtilities.getMinimumIndex(criteria);

        List<ForceEventEstimate> forceEventEstimates = new ArrayList<>();

        for(int i = 0; i<expectedJumpCount;i++)
        {
            int index = possibleEventsIndices.get(i);

            double z = rightSmoothedChannelYData[index][0];
            double maxF = rightSmoothedChannelYData[index][1];
            double minF = leftSmoothedChannelYData[index][1];

            ForceEventEstimate estimate = new ForceEventSimpleEstimate(z, minF, z, maxF);
            forceEventEstimates.add(estimate);
        }


        return forceEventEstimates;      
    }  

    private double calculateDistanceTerm(Channel1DData raw, Channel1DData smoothed)
    {
        double[] ys = raw.getYCoordinatesCopy();
        double[] ySmoothed = smoothed.getYCoordinatesCopy();

        int n = ys.length;
        double ssq = 0;

        for(int j = 0; j<n; j++)
        {
            double dy = ys[j] - ySmoothed[j];

            ssq += dy*dy;
        }

        double distanceTerm = n*Math.log(ssq/n);

        return distanceTerm;
    }

    private static int calculateTotalLength(Collection<IndexRange> indexRanges)
    {
        int totalLength = 0;

        for(IndexRange range : indexRanges)
        {
            totalLength += range.getLengthIncludingEdges();
        }

        return totalLength;
    }

    private static class Add1DTransformation implements Channel1DDataInROITransformation
    {   
        private final double dY;

        private final IndexRange range;

        Add1DTransformation(IndexRange range, double yTranslate)
        {
            this.range = range;
            this.dY = yTranslate;
        }

        @Override
        public Point1DData transformPointChannel(Point1DData channel)
        {
            if(!range.contains(0))
            {
                return channel;
            }

            double xNew = channel.getX();
            double yNew = channel.getY() + dY;

            Point1DData channelNew = new Point1DData(xNew, yNew, channel.getXQuantity(), channel.getYQuantity());    
            return channelNew;
        }

        @Override
        public Channel1DData transform(Channel1DData channel) 
        {     
            if(channel instanceof GridChannel1DData)
            {
                return translateGridChannel((GridChannel1DData)channel);
            }

            if(channel instanceof SinusoidalChannel1DData)
            {
                return translatePeakForceChannel((SinusoidalChannel1DData)channel);
            }

            if(channel instanceof Point1DData)
            {
                return transformPointChannel((Point1DData)channel);
            }

            double[][] points = channel.getPoints();
            double[][] translatedPoints = GeometryUtilities.translatePointsY(points, range.getMinIndex(), range.getMaxIndex() + 1, dY);

            FlexibleChannel1DData channelData = new FlexibleChannel1DData(translatedPoints, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());
            return channelData;
        }

        public GridChannel1DData translateGridChannel(GridChannel1DData channel) 
        {   
            double[] dataOriginal = channel.getData();

            double[] dataTranslated = GeometryUtilities.translate(dataOriginal, range.getMinIndex(), range.getMaxIndex() + 1, dY);

            GridChannel1DData channelData = new GridChannel1DData(dataTranslated, channel.getGrid(), channel.getYQuantity());

            return channelData;
        }

        public SinusoidalChannel1DData translatePeakForceChannel(SinusoidalChannel1DData channel) 
        {   
            double[] dataOriginal = channel.getData();
            double[] dataTranslated = GeometryUtilities.translate(dataOriginal, range.getMinIndex(), range.getMaxIndex() + 1, dY);

            SinusoidalChannel1DData channelData = new SinusoidalChannel1DData(dataTranslated, channel.getAmplitude(), 
                    channel.getAngleFactor(), channel.getInitIndex(), channel.getPhaseShift(), 
                    channel.getXShift(), channel.getXQuantity(), channel.getYQuantity());

            return channelData;
        }

        @Override
        public Channel1DData transform(Channel1DData channel, ROI roi, ROIRelativePosition position) 
        {        
            if(ROIRelativePosition.EVERYTHING.equals(position))
            {
                return transform(channel);
            }

            GridChannel1DData gridChannel = (channel instanceof GridChannel1DData) ? (GridChannel1DData)channel : GridChannel1DData.convert(channel);
            Grid1D grid = gridChannel.getGrid();
            double[] data = gridChannel.getData();

            int columnCount = grid.getIndexCount();

            double[] transformed = new double[columnCount];

            return null;
        }
    }
}
