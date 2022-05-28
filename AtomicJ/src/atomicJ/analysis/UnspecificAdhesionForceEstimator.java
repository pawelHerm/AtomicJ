
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.SortX1DTransformation;
import atomicJ.data.Channel1DData;
import atomicJ.statistics.HighCoverageLTS;
import atomicJ.statistics.LTS;

public class UnspecificAdhesionForceEstimator implements ForceEventEstimator 
{
    private static final double DEFAULT_FRACTION_OF_END_POINTS_TO_REJECT = 0.075;
    private static final double DEFAULT_INITIAL_LTS_FIT_COVERAGE = 0.25;
    private static final int DEFAULT_NUMBER_OF_LTS_RANDOM_STARTS = 300;

    private final Channel1DDataTransformation sorter = new SortX1DTransformation(SortedArrayOrder.ASCENDING);

    private final int deg;

    public UnspecificAdhesionForceEstimator(int fittedPolynomialDegree)
    {
        this.deg = fittedPolynomialDegree;
    }

    @Override
    public List<ForceEventEstimate> getEventEstimates(Channel1DData approachBranch, Channel1DData withdrawBranch, double domainMin, double domainMax)
    {     
        if(withdrawBranch.isEmpty())
        {
            return Collections.emptyList();
        }    

        ForceEventEstimate result = getAdhesionEventEstimate(withdrawBranch);

        return Collections.singletonList(result);
    }  

    public AdhesionEventEstimate getAdhesionEventEstimate(Channel1DData channel)
    {       
        if(channel.isEmpty())
        {
            return AdhesionEventEstimate.getEmptyInstance();
        } 

        Channel1DData sortedChannel = sorter.transform(channel);

        int n = sortedChannel.getItemCount();

        int m = (int)(DEFAULT_FRACTION_OF_END_POINTS_TO_REJECT*n); //we will reject this many extreme points on the off contact part of withdraw branch
        // while looking for point of maximal adhesion. The extreme parts of the force curve are often spurious. 

        int trimmedLength = n - m;
        int minimalForceIndex = sortedChannel.getYMinimumIndex(0, trimmedLength);

        //        double[][] precontactUpToMinimum  = sortedChannel.getPointsCopy(minimalForceIndex, n);
        //
        //        HighCoverageLTS fit = HighCoverageLTS.findFit(precontactUpToMinimum, deg, true, 4, DEFAULT_INITIAL_LTS_FIT_COVERAGE, DEFAULT_NUMBER_OF_LTS_RANDOM_STARTS);
        //
        //        double liftOffDistance = fit.getLargestClusterOfCoveredCases();
        //        double liftOffForce = fit.getBestFit().value(liftOffDistance);       
        //
        //        AdhesionEventEstimate estimate = new AdhesionEventEstimate(sortedChannel.getY(minimalForceIndex), sortedChannel.getX(minimalForceIndex), liftOffDistance, liftOffForce);
        //
        //        return estimate;


        double[][] precontactUpToMinimum  = sortedChannel.getXYViewCopy(minimalForceIndex, n);

        HighCoverageLTS fit = HighCoverageLTS.findFit(precontactUpToMinimum[1], precontactUpToMinimum[0], deg, true, 4, DEFAULT_INITIAL_LTS_FIT_COVERAGE, DEFAULT_NUMBER_OF_LTS_RANDOM_STARTS);

        //        double liftOffDistance = fit.getLargestClusterOfCoveredCases();
        //        double liftOffForce = fit.getBestFit().value(liftOffDistance);       

        int coveredCount = fit.getCoveredCount();
        double[] coveredXCoordinates = LTS.getCoveredCasesXCoordinatesOverwrittingPassedArrays(precontactUpToMinimum[1], precontactUpToMinimum[0],coveredCount, fit.getBestFit());

        Arrays.sort(coveredXCoordinates, 0, coveredCount);
        double liftOffDistance = LTS.getLargestClusterOfCoveredCases(coveredXCoordinates, coveredCount, fit.getDataCount());
        double liftOffForce = fit.getBestFit().value(liftOffDistance);       

        AdhesionEventEstimate estimate = new AdhesionEventEstimate(sortedChannel.getY(minimalForceIndex), sortedChannel.getX(minimalForceIndex), liftOffDistance, liftOffForce);

        return estimate;
    }
}
