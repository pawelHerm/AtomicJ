
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import atomicJ.analysis.FungExponentProcessedPackFunction;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.functions.AbstractFittedUnivariateFunction;
import atomicJ.statistics.FittedLinearUnivariateFunction;
import static atomicJ.utilities.MathUtilities.intPow;

public class HyperelasticFungSphere extends HertzianContactModel
{
    private static final double TOLERANCE = 1e-5;

    private static final String NAME = "Sphere (Fung)";

    private final Sphere indenter;
    private final SampleModel sample;

    private final double poissonFactor;
    private final double radius;

    public HyperelasticFungSphere(Sphere indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);
        this.indenter = indenter;
        this.sample = sample;

        double v = sample.getPoissonRatio();
        poissonFactor = 1/(1 - v*v);

        radius = indenter.getRadius();
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

    @Override
    public FungSphereFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        FungSphereFit fit = new FungSphereFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] forceIndentationYs, double[] forceIndentationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {

        double[][] transformed = convertIndentationData(forceIndentationYs, forceIndentationXs, recordingPoint);
        FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunction(transformed[1], transformed[0], 1, true);

        double modulusLog = fit.getCoefficient(Double.valueOf(0));       
        double modulus = Math.pow(Math.E, modulusLog);
        double b = fit.getCoefficient(Double.valueOf(1));  

        double objective = regressionStrategy.getObjectiveFunctionValue(forceIndentationYs, forceIndentationXs, new FungForceIndentationFit(modulus, b));       
        return objective;              
    }

    //we cannot overwrite components of the forceIndentation arrays, as there are later used as input for for getObjectiveFunctionValue
    private double[][] convertIndentationData(double[] foceValues, double[] indentationValues, Point2D recordingPoint)
    {
        int n = foceValues.length;

        double[] indentationValuesConverted = new double[n];
        double[] forceValuesConverted = new double[n];
        int j = 0;
        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            double force = foceValues[i];

            double coefficient1 = 1;

            double coefficient2 = 1;

            if(indent>TOLERANCE && force >TOLERANCE)
            {
                double a = Math.sqrt(radius*indent);  

                double B = 20*poissonFactor/(9);

                double numerator1 = intPow(a, 5) -15*radius*intPow(a, 4) + 75*radius*radius*a*a*a;
                double denominator1 = 5*radius*a*a - 50*radius*radius*a + 125*radius*radius*radius;

                coefficient1 = B*numerator1/denominator1;

                double numerator2 = a*a*a - 15*radius*a*a;
                double denominator2 = 25*radius*radius*a - 125*radius*radius*radius;

                coefficient2 = numerator2/denominator2;

                if(coefficient1 > Math.pow(10, -2.8) && coefficient2 > Math.pow(10, -2.8))
                {                 
                    indentationValuesConverted[j] = coefficient2;
                    forceValuesConverted[j] = Math.log(force) - Math.log(coefficient1);
                    j++;
                }
            }                 
        }

        double[] forceIndentationXsTransformedFinal = j == n ? indentationValues : Arrays.copyOf(indentationValuesConverted, j);
        double[] forceIndentationYsTransformedFinal = j == n ? foceValues : Arrays.copyOf(forceValuesConverted, j);

        double[][] transformed = new double[][] {forceIndentationXsTransformedFinal, forceIndentationYsTransformedFinal};
        return transformed;       
    } 

    public double getContactRadius(double indent)
    {
        double a = indent > TOLERANCE  ? Math.sqrt(radius*indent) : 0;     
        return a; 
    }

    private class FungForceIndentationFit extends AbstractFittedUnivariateFunction implements FittedLinearUnivariateFunction
    {
        private final double modulus;
        private final double b;
        private final double scalingFactor;

        private FungForceIndentationFit(double modulus, double b)
        {
            this.modulus = modulus;
            this.b = b;
            this.scalingFactor = 1;
        }

        private FungForceIndentationFit(double modulus, double b, double scalingFactor)
        {
            this.modulus = modulus;
            this.b = b;
            this.scalingFactor = scalingFactor;
        }

        @Override
        public FungForceIndentationFit multiply(double s)
        {
            return new FungForceIndentationFit(this.modulus, this.b, s*this.scalingFactor);
        }

        public double getModulus()
        {
            return modulus;
        }

        public double getExponent()
        {
            return b;
        }

        public double getPointwiseModulus(double indentation, double force)
        {
            double modulus = 0;
            if(indentation>TOLERANCE  && force >TOLERANCE)
            {                        
                double coefficent = getCoefficient(indentation);
                modulus = force/coefficent;
            }

            return modulus; 
        }

        @Override
        public double value(double indent) 
        {       
            double coefficient = getCoefficient(indent);   
            double value = scalingFactor*modulus*coefficient;
            return value;
        }

        private double getCoefficient(double indent)
        {
            double coefficient1 = 0;
            double coefficient2 = 0;

            if(indent>TOLERANCE)
            {
                double a = Math.sqrt(radius*indent);  

                double B = 20*poissonFactor/(9);

                double numerator1 = intPow(a, 5) - 15*radius*intPow(a, 4) + 75*radius*radius*a*a*a;
                double denominator1 = 5*radius*a*a - 50*radius*radius*a + 125*radius*radius*radius;

                coefficient1 = B*numerator1/denominator1;

                double numerator2 = a*a*a - 15*radius*a*a;
                double denominator2 = 25*radius*radius*a - 125*radius*radius*radius;

                coefficient2 = numerator2/denominator2;
            }

            double coefficient = coefficient1*Math.exp(b*coefficient2);
            return coefficient;
        }

        @Override
        public double residual(double[] p) 
        {
            double x = p[0];
            double y = p[1];
            double r = y - value(x);
            return r;
        }

        @Override
        public double residual(double x, double y) 
        {
            double r = y - value(x);
            return r;
        }

        @Override
        public double getCoefficient(Number n) 
        {
            int c = n.intValue();
            if(c == 0)
            {
                return modulus;
            }
            if(c == 1)
            {
                return b;
            }

            return 0;
        }

        @Override
        public int getEstimatedParameterCount() 
        {
            return 2;
        }

        @Override
        public double[] getParameters() 
        {
            return new double[] {modulus, b};
        }

        @Override
        public FittedLinearUnivariateFunction getDerivative(int n) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public class FungSphereFit extends HertzianFit<HyperelasticFungSphere>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceIndentationTransitionPoint;

        private final FungForceIndentationFit fittedFunction;

        public FungSphereFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HyperelasticFungSphere.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[][] transformedForceIndentationXY = convertIndentationData(fittableForceValues, fittableIndentationValues, recordingPoint);  

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction transformedFitFunction = regressionStrategy.getFitFunction(transformedForceIndentationXY[1], transformedForceIndentationXY[0], 1, true);
            double modulusLog = transformedFitFunction.getCoefficient(Double.valueOf(0));

            double modulus = Math.pow(Math.E, modulusLog);
            double b = transformedFitFunction.getCoefficient(Double.valueOf(1));   

            this.fittedFunction = new FungForceIndentationFit(modulus, b);           

            double[] forceIndentationTransitionPointData = regressionStrategy.getLastCoveredPoint(fittableForceValues, fittableIndentationValues, fittedFunction);
            this.forceIndentationTransitionPoint = new Point1DData(forceIndentationTransitionPointData[0], forceIndentationTransitionPointData[1], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
        }

        @Override
        public List<ProcessedPackFunction<ProcessedSpectroscopyPack>> getSpecialFunctions()
        {
            List<ProcessedPackFunction<ProcessedSpectroscopyPack>> processedPackFunctions = Collections.singletonList(FungExponentProcessedPackFunction.getInstance());
            return processedPackFunctions;
        }

        public double getFungExponent()
        {
            return fittedFunction.getExponent();
        }

        @Override
        public double getYoungModulus() 
        {
            return fittedFunction.getModulus();
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            return fittedFunction.getPointwiseModulus(indentation, force);
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint() 
        {
            return forceIndentationTransitionPoint;
        }

        @Override
        protected FungForceIndentationFit getFittedFunction() 
        {
            return fittedFunction;
        }           
    }
}



