
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

import java.awt.geom.Point2D;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.data.Channel1DData;
import atomicJ.data.IndexRange;

public final class ClassicalFlexibleEstimator implements ContactEstimator
{
    private final ContactEstimationGuide model;
    private final MinimumSearchStrategy searchStrategy;

    public ClassicalFlexibleEstimator(MinimumSearchStrategy searchStrategy, ContactEstimationGuide model)
    {
        this.model = model;
        this.searchStrategy = searchStrategy;
    }	

    @Override
    public double[] getContactPoint(Channel1DData deflectionCurveBranch, Point2D recordingPosition, double springConstant) 
    {			
        final int n = deflectionCurveBranch.getItemCount();

        SequentialSearchAssistant searchAssistant = model.getSequentialSearchAssistant(deflectionCurveBranch, recordingPosition, springConstant);
        UnivariateFunction f = new SearchAssistantFunction(searchAssistant);

        IndexRange validContactIndices = model.getRangeOfValidTrialContactPointIndices(deflectionCurveBranch, recordingPosition, springConstant);
        int minIndex = Math.max((int)(0.075*n), validContactIndices.getMinIndex());
        int maxIndex = Math.min(n - 5, validContactIndices.getMaxIndex());

        int contactPointIndex = (int)Math.rint(searchStrategy.getMinimum(f, minIndex, maxIndex));

        return deflectionCurveBranch.getPoint(contactPointIndex);				
    }

    @Override
    public boolean isAutomatic()
    {
        return true;
    }

    private static class SearchAssistantFunction implements UnivariateFunction
    {
        private final SequentialSearchAssistant searchAssistant;
        public SearchAssistantFunction(SequentialSearchAssistant searchAssistant)
        {
            this.searchAssistant = searchAssistant;
        }

        @Override
        public double value(double x) 
        {
            int j = (int)Math.rint(x);               

            try
            {
                double objective = searchAssistant.getObjectiveFunctionValue(BasicRegressionStrategy.CLASSICAL_L2, BasicRegressionStrategy.CLASSICAL_L2, j);
                return objective;
            }
            catch(Exception e)
            {
                return Double.POSITIVE_INFINITY;
            }
        }
    }
}
