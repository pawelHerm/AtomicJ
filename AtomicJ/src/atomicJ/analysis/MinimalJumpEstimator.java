
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

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.LocalRegressionTransformation;
import atomicJ.curveProcessing.SortX1DTransformation;
import atomicJ.curveProcessing.SpanType;
import atomicJ.data.Channel1DData;
import atomicJ.data.IndexRange;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;

public class MinimalJumpEstimator implements ForceEventEstimator 
{
    private final Channel1DDataTransformation sorter = new SortX1DTransformation(SortedArrayOrder.ASCENDING);

    private final int jumpWidthInPoints; 
    private final double span;
    private final SpanType spanType;
    private final int polynomialDegree;
    private final int robustnessIterationsCount = 0;
    private final UnitExpression minDistanceFromUnspecificAdhesion;
    private LocalRegressionWeightFunction weightFunction = LocalRegressionWeightFunction.TRICUBE;    

    public MinimalJumpEstimator(int polynomialDegree, int jumpWidthInPoints, UnitExpression minDistanceFromUnspecificAdhesion, double span, SpanType spanType, LocalRegressionWeightFunction weightFunction)
    {
        this.jumpWidthInPoints = jumpWidthInPoints;
        this.polynomialDegree = polynomialDegree;
        this.minDistanceFromUnspecificAdhesion = minDistanceFromUnspecificAdhesion;
        this.span = span;
        this.spanType = spanType;
        this.weightFunction = weightFunction;
    }

    @Override
    public List<ForceEventEstimate> getEventEstimates(Channel1DData approachChannel, Channel1DData withdrawChannel, double domainMin, double domainMax)
    {                
        PrefixedUnit approachUnit = approachChannel.getXQuantity().getUnit();

        double domainMinUnitConverted = domainMin + minDistanceFromUnspecificAdhesion.derive(approachUnit).getValue();
        double domainMaxUnitConverted = domainMax;

        int n = withdrawChannel.getItemCount();

        Channel1DData withdrawChannelSorted = sorter.transform(withdrawChannel);
        Channel1DData approachChannelSorted = sorter.transform(approachChannel);      

        int windowWidth = spanType.getSpanLengthInPoints(span, n);

        Channel1DDataTransformation leftOpenSmoother = new LocalRegressionTransformation(span, SpanGeometry.LEFT_OPEN, spanType, robustnessIterationsCount, polynomialDegree, 0, weightFunction);
        Channel1DDataTransformation rightClosedSmoother = new LocalRegressionTransformation(span, SpanGeometry.RIGHT, spanType, robustnessIterationsCount, polynomialDegree, 0, weightFunction);

        Channel1DData leftSmoothedWithdrawChannel = leftOpenSmoother.transform(withdrawChannelSorted);
        Channel1DData rightSmoothedWithdrawChannel = rightClosedSmoother.transform(withdrawChannelSorted);

        IndexRange approachIndexRange = approachChannelSorted.getIndexRangeBoundedBy(domainMinUnitConverted, domainMaxUnitConverted);
        IndexRange withdrawIndexRange = withdrawChannelSorted.getIndexRangeBoundedBy(domainMinUnitConverted, domainMaxUnitConverted);

        double[][] leftSmoothedWithdrawChannelPoints = leftSmoothedWithdrawChannel.getPoints();
        double[][] rightSmoothedWithdrawChannelPoints = rightSmoothedWithdrawChannel.getPoints();


        int withdrawPointWithinRangeCount = withdrawIndexRange.getLengthIncludingEdges();
        double[][] withdrawDifferences = new double[withdrawPointWithinRangeCount - jumpWidthInPoints][];

        for(int i = withdrawIndexRange.getMinIndex(), j = 0; i<withdrawIndexRange.getMaxIndex() + 1 - jumpWidthInPoints;i++, j++)
        {
            double[] leftSmoothedPoint = leftSmoothedWithdrawChannelPoints[i];

            double x = leftSmoothedPoint[0];
            double leftY = leftSmoothedPoint[1];
            double rightY = rightSmoothedWithdrawChannelPoints[i + jumpWidthInPoints][1];

            withdrawDifferences[j] = new double[] {x, rightY - leftY};            
        }

        Channel1DData leftSmoothedApproachChannel = leftOpenSmoother.transform(approachChannelSorted);
        Channel1DData rightSmoothedApproachChannel = rightClosedSmoother.transform(approachChannelSorted);

        double[][] leftSmoothedApproachChannelPoints = leftSmoothedApproachChannel.getPoints();
        double[][] rightSmoothedApproachChannelPoints = rightSmoothedApproachChannel.getPoints();

        int approachPointWithinRangeCount = approachIndexRange.getLengthIncludingEdges();
        double[] approachDifferences = new double[approachPointWithinRangeCount - jumpWidthInPoints];

        for(int i = approachIndexRange.getMinIndex(),  j = 0; i<approachIndexRange.getMaxIndex() + 1 - jumpWidthInPoints; i++, j++)
        {
            double leftY =  leftSmoothedApproachChannelPoints[i][1];
            double rightY = rightSmoothedApproachChannelPoints[i + jumpWidthInPoints][1];

            approachDifferences[j] = rightY - leftY;            
        }

        double sd = DescriptiveStatistics.standardDeviationSample(approachDifferences);

        //        EmpiricalDistribution distr = new EmpiricalDistribution((int)Math.rint(approachDifferences.length/100.));
        //        distr.load(approachDifferences);

        double criterion = 4*sd;

        List<Integer> possibleEventsIndices = getPossibleEventIndices(withdrawDifferences, withdrawIndexRange.getMinIndex(), windowWidth);     

        List<ForceEventEstimate> forceEventEstimates = new ArrayList<>();

        for(int i = 0; i<possibleEventsIndices.size(); i++)
        {
            int index = possibleEventsIndices.get(i);

            double z = leftSmoothedWithdrawChannelPoints[index][0];
            double maxF = rightSmoothedWithdrawChannelPoints[index + jumpWidthInPoints][1];
            double minF = leftSmoothedWithdrawChannelPoints[index][1];

            if(maxF - minF < criterion)
            { 
                break;
            }

            if(z >= domainMinUnitConverted && z <= domainMaxUnitConverted)
            {
                ForceEventEstimate estimate = new ForceEventSimpleEstimate(z, minF, z, maxF);
                forceEventEstimates.add(estimate);  
            }     
        }

        return forceEventEstimates;      
    }  

    private List<Integer> getPossibleEventIndices(double[][] absDifferences, int shift, int windowWidth)
    {
        int n = absDifferences.length;

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

            possibleEventsIndices.add(maxDifferenceIndex + shift);

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

        return possibleEventsIndices;
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
}
