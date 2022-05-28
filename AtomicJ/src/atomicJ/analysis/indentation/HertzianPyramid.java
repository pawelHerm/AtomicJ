
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.analysis.indentation;

import java.awt.geom.Point2D;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.statistics.LinearRegressionEsimator;



public class HertzianPyramid extends HertzianLinearContactModel
{
    private static final String NAME = "Pyramid";

    private final Pyramid indenter;
    private final SampleModel sample;

    public HertzianPyramid(Pyramid indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);
        this.indenter = indenter;
        this.sample = sample;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    public double getTipHalfAngle()
    {
        return this.indenter.getHalfAngle();
    }

    @Override
    public Pyramid getIndenter()
    {
        return indenter;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }

    private double[] transformIndentationData(double[] forceIndentationXs, Point2D recordingPoint)
    {
        int n = forceIndentationXs.length;

        for(int i = 0; i<n; i++)
        {
            double indent = forceIndentationXs[i]; 
            forceIndentationXs[i] =  indent > 0 ? indent*indent : 0;
        }

        return forceIndentationXs; 
    }

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs, double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionMinimumForThroughOriginLinearRegression(postcontactForceSeparationYs,transformIndentationData(postcontactForceSeparationXs, recordingPoint));
        return objectiveFunctionMinimum;
    }  

    @Override
    protected LinearRegressionEsimator getLinearRegression(double[][] forceIndentation, RegressionStrategy regressionStrategy, Point2D recordingPoint)  
    {
        LinearRegressionEsimator f = regressionStrategy.performRegressionForSingleExponent(forceIndentation, 2);
        return f;
    }

    @Override
    protected FittedLinearUnivariateFunction getFittedFunction(double[][] forceIndentation, RegressionStrategy regressionStrategy, Point2D recordingPoint)
    {
        FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(forceIndentation, 2);

        return fit;
    }

    @Override
    protected FittedLinearUnivariateFunction getFittedFunction(double[] forceIndentationYs, double[] forceIndentationXs, RegressionStrategy regressionStrategy, Point2D recordingPoint)
    {
        FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(forceIndentationYs, forceIndentationXs, 2);

        return fit;
    }

    @Override
    public HertzianPyramidFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianPyramidFit fit = new HertzianPyramidFit(this, deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    public static class HertzianPyramidFit extends HertzianLinearFit<HertzianPyramid>
    {
        public HertzianPyramidFit(HertzianPyramid indentationModel, Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings) 
        {
            super(indentationModel, deflectionChannel, contactPoint, recordingPoint, processingSettings);
        }

        @Override
        public double getYoungModulus() 
        {
            Pyramid pyramid = getContactModel().getIndenter();
            double v = getContactModel().getSampleModel().getPoissonRatio();
            double angle = pyramid.getHalfAngle();

            double a = getFittedFunction().getCoefficient(Double.valueOf(2));    
            double modulus = (a * (1 - v*v))/(0.7453*Math.tan(angle));  

            return modulus;
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            Pyramid pyramid = getContactModel().getIndenter();

            double v = getContactModel().getSampleModel().getPoissonRatio();
            double angle = pyramid.getHalfAngle();

            double modulus = (force* (1 - v*v))/(0.7453*indentation*indentation*Math.tan(angle));
            return modulus;
        }        
    }
}

