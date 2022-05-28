
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
import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.LimitDomain1DTransformation;
import atomicJ.data.Channel1DData;
import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.statistics.HighCoverageLTS;
import atomicJ.statistics.LinearRegressionEsimator;
import atomicJ.statistics.ResidualVector;


public class PrecontactFit implements LinearRegressionEsimator
{	
    private final HighCoverageLTS fit;	

    public PrecontactFit(Channel1DData curveBranch, Point2D recordingPosition, int deg, ContactEstimationGuide model, double springConstant)
    {	
        this(new ClassicalFlexibleEstimator(BasicMinimumSearchStrategy.GOLDEN_SECTION, model), recordingPosition,curveBranch, deg, springConstant);		
    }

    public PrecontactFit(ContactEstimator contactEstimator, Point2D recordingPosition, Channel1DData curveBranch, int deg, double springConstant)
    {   
        double x0 = contactEstimator.getContactPoint(curveBranch, recordingPosition, springConstant)[0];

        Channel1DDataTransformation limitTransformation = new LimitDomain1DTransformation(x0, Double.POSITIVE_INFINITY);
        Channel1DData temporaryPrecontact = limitTransformation.transform(curveBranch);

        this.fit = HighCoverageLTS.findFit(temporaryPrecontact.getPointsCopy(), deg, true, 8, 200);
    }

    @Override
    public FittedLinearUnivariateFunction getBestFit() 
    {
        return fit.getBestFit();
    }

    @Override
    public double getObjectiveFunctionMinimum() 
    {
        return fit.getObjectiveFunctionMinimum();
    }


    @Override
    public ResidualVector getResiduals() 
    {
        return fit.getResiduals();
    }


    public double getNoiseEstimation()
    {
        return fit.getRobustMedian();
    }
}
