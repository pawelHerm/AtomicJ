
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

import org.apache.commons.math3.special.Gamma;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.statistics.FittedLinearUnivariateFunction;
import atomicJ.statistics.LinearRegressionEsimator;


public class HertzianPowerShaped extends HertzianLinearContactModel
{
    private static final String NAME = "Power-shaped (Galin)";

    private final PowerShapedTip indenter;
    private final SampleModel sample;

    private final double A;
    private final double lambda;
    private final double indentationExp;
    private final double gammaFactor;

    public HertzianPowerShaped(PowerShapedTip indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);
        this.indenter = indenter;
        this.sample = sample;

        this.A = indenter.getFactor();
        this.lambda = indenter.getExponent();

        double coeff1 = Math.pow(Gamma.gamma((1 + lambda)/2.)/(A*Math.sqrt(Math.PI)*Gamma.gamma(1 + lambda/2)), 1./lambda);
        this.gammaFactor = 2*this.lambda*coeff1/((1+lambda));

        this.indentationExp = (lambda + 1)/lambda;
    }

    @Override
    public String getName()
    {
        return NAME;
    }


    @Override
    public PowerShapedTip getIndenter()
    {
        return indenter;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }      

    private double[] transformIndentationData(double[] indentationValues, Point2D recordingPoint)
    {
        int n = indentationValues.length;

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];            
            indentationValues[i] =  indent > 0 ? Math.pow(indent, indentationExp) : 0;
        }

        return indentationValues; 
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
        LinearRegressionEsimator f = regressionStrategy.performRegressionForSingleExponent(forceIndentation, indentationExp);
        return f;
    }


    @Override
    protected FittedLinearUnivariateFunction getFittedFunction(double[][] forceIndentation, RegressionStrategy regressionStrategy, Point2D recordingPoint)
    {
        FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(forceIndentation, indentationExp);

        return fit;
    }

    @Override
    protected FittedLinearUnivariateFunction getFittedFunction(double[] forceIndentationYs, double[] forceIndentationXs, RegressionStrategy regressionStrategy, Point2D recordingPoint)
    {
        FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(forceIndentationYs, forceIndentationXs, indentationExp);
        return fit;
    }

    @Override
    public HertzianPowerShapedFit getModelFit(Channel1DData deflectionCurveBranch, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianPowerShapedFit fit = new HertzianPowerShapedFit(this, deflectionCurveBranch, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    public static class HertzianPowerShapedFit extends HertzianLinearFit<HertzianPowerShaped>
    {
        public HertzianPowerShapedFit(HertzianPowerShaped indentationModel, Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings) 
        {
            super(indentationModel, deflectionChannel, contactPoint, recordingPoint, processingSettings);
        }

        @Override
        public double getYoungModulus() 
        {
            HertzianPowerShaped indentationModel = getContactModel();

            double v = getContactModel().getSampleModel().getPoissonRatio();
            double indentationExp = indentationModel.indentationExp;
            double gammaFactor = indentationModel.gammaFactor;

            double a = getFittedFunction().getCoefficient(indentationExp);    
            double modulus = (a*(1 - v*v))/gammaFactor;  

            return modulus;
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            HertzianPowerShaped indentationModel = getContactModel();

            double v = getContactModel().getSampleModel().getPoissonRatio();
            double indentationExp = indentationModel.indentationExp;
            double gammaFactor = indentationModel.gammaFactor;

            double modulus = (force*(1 - v*v))/(gammaFactor*Math.pow(indentation, indentationExp));
            return modulus;
        }        
    }
}



