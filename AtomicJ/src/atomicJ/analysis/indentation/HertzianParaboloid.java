
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


public class HertzianParaboloid extends HertzianLinearContactModel
{
    private static final String NAME = "Paraboloid (Hertz)";

    private final Sphere indenter;
    private final SampleModel sample;

    public HertzianParaboloid(Sphere indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
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

    public double getTipRadius()
    {
        return this.indenter.getRadius();
    }

    @Override
    public Sphere getIndenter()
    {
        return indenter;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }  

    private double[] transformIndentationData(double[] indenationValues, Point2D recordingPoint)
    {
        int n = indenationValues.length;

        for(int i = 0; i<n; i++)
        {
            double indent = indenationValues[i];            
            indenationValues[i] =  indent > 0 ? indent*Math.sqrt(indent): 0;
        }

        return indenationValues; 
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
        LinearRegressionEsimator f = regressionStrategy.performRegressionForSingleExponent(forceIndentation, 1.5);
        return f;
    }

    @Override
    protected FittedLinearUnivariateFunction getFittedFunction(double[][] forceIndentation, RegressionStrategy regressionStrategy, Point2D recordingPoint)
    {
        FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(forceIndentation, 1.5);

        return fit;
    }

    @Override
    protected FittedLinearUnivariateFunction getFittedFunction(double[] forceIndentationYs, double[] forceIndentationXs, RegressionStrategy regressionStrategy, Point2D recordingPoint)
    {
        FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(forceIndentationYs, forceIndentationXs, 1.5);

        return fit;
    }

    @Override
    public HertzianSphereFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianSphereFit fit = new HertzianSphereFit(this, deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    public static class HertzianSphereFit extends HertzianLinearFit<HertzianParaboloid>
    {
        public HertzianSphereFit(HertzianParaboloid indentationModel, Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings) 
        {
            super(indentationModel, deflectionChannel, contactPoint, recordingPoint, processingSettings);
        }

        @Override
        public double getYoungModulus() 
        {
            Sphere sphere =  getContactModel().getIndenter();
            double v = getContactModel().getSampleModel().getPoissonRatio();
            double r = sphere.getRadius();

            double a = getFittedFunction().getCoefficient(1.5);    
            double modulus = (3.*a*(1 - v*v))/(4.*Math.sqrt(r));  

            return modulus;
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            Sphere cone = getContactModel().getIndenter();           
            double v = getContactModel().getSampleModel().getPoissonRatio();
            double R = cone.getRadius();

            double modulus = (3*force*(1 - v*v))/(4*Math.sqrt(R)*Math.pow(indentation,1.5));
            return modulus;
        }        
    }
}



