
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

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;

import atomicJ.analysis.ForceContourStackFunction;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.FittedLinearUnivariateFunction;


public class HertzianHyperboloid extends HertzianContactModel
{
    private static final double TOLERANCE = 1e-10;

    private static final String NAME = "Hyperboloid";

    private final Hyperboloid indenter;
    private final SampleModel sample;

    private final double poissonFactor;
    private final double cotAngle;
    private final double radius;

    public HertzianHyperboloid(Hyperboloid indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);
        this.indenter = indenter;
        this.sample = sample;

        double v = sample.getPoissonRatio();
        poissonFactor = 1/(1 - v*v);

        double angle = indenter.getHalfAngle();
        cotAngle = 1./Math.tan(angle);

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

    public double getHalfAngle()
    {
        return this.indenter.getHalfAngle();
    }

    @Override
    public Indenter getIndenter()
    {
        return indenter;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }

    @Override
    public HertzianHyperboloidFit getModelFit(Channel1DData deflectionCurveBranch, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianHyperboloidFit fit = new HertzianHyperboloidFit(deflectionCurveBranch, contactPoint, recordingPoint, processingSettings);
        return fit;
    }   

    private double[] convertIndentationData(double[] indentationValues)
    {
        int n = indentationValues.length;
        double[] convertedIndentationValues = new double[n];

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            convertedIndentationValues[i] = getForceModulusRatio(indent);
        }

        return convertedIndentationValues;       
    } 

    private double[] transformIndentationData(double[] indentationValues)
    {
        int n = indentationValues.length;

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            indentationValues[i] = getForceModulusRatio(indent);
        }

        return indentationValues;       
    }  

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs, double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionMinimumForThroughOriginLinearRegression(postcontactForceSeparationYs, transformIndentationData(postcontactForceSeparationXs));
        return objectiveFunctionMinimum;
    }  

    public double getContactRadius(double indent)
    {
        double contactRadius = 0;
        if(indent>TOLERANCE)
        {
            double startValue = Math.sqrt(indent)*Math.sqrt(indent + 2*radius*cotAngle*cotAngle)/cotAngle;

            HertzianHyperboloidContactRadiusVsIndentationFunction f = new HertzianHyperboloidContactRadiusVsIndentationFunction(indent);

            BrentSolver solver = new BrentSolver(1e-12);
            contactRadius = solver.solve(5000, f,  0, startValue);  
        }

        return contactRadius; 
    }

    public class HertzianHyperboloidContactRadiusVsIndentationFunction implements UnivariateDifferentiableFunction
    {
        private final double indent;

        public HertzianHyperboloidContactRadiusVsIndentationFunction(double indent)
        {
            this.indent = indent;
        }

        @Override
        public double value(double a) 
        {             
            double val = 0.5*a*cotAngle*(0.5*Math.PI + Math.atan(0.5*(a/(radius*cotAngle) - (radius * cotAngle)/a))) - indent;
            return val;
        }

        @Override
        public DerivativeStructure value(DerivativeStructure a) throws DimensionMismatchException 
        {           
            DerivativeStructure result = a.multiply(0.25*Math.PI*cotAngle)
                    .add(a.multiply(0.5*cotAngle).multiply(a.multiply(0.5/(radius*cotAngle)).subtract(a.pow(-1).multiply(0.5*radius * cotAngle))).atan()).subtract(indent);
            return result;
        }        
    }

    private double getForceModulusRatio(double indent)
    {
        double coefficient = 0;
        if(indent>TOLERANCE)
        {
            double startValue = Math.sqrt(indent)*Math.sqrt(indent + 2*radius*cotAngle*cotAngle)/cotAngle;
            HertzianHyperboloidContactRadiusVsIndentationFunction funct = new HertzianHyperboloidContactRadiusVsIndentationFunction(indent);
            BrentSolver solver = new BrentSolver(1e-12);

            double a = solver.solve(5000, funct,  0, startValue);

            coefficient = poissonFactor*a*a*cotAngle*(radius*cotAngle/a + 0.5*(1 - radius*radius*cotAngle*cotAngle/(a*a))
                    *(0.5*Math.PI + Math.atan(0.5*a/(radius*cotAngle) - 0.5*radius*cotAngle/a)));        
        }

        return coefficient;
    }

    public class HertzianHyperboloidForceIndentationFunction implements UnivariateFunction
    {
        private final double modulus;

        public HertzianHyperboloidForceIndentationFunction(double modulus)
        {
            this.modulus = modulus;
        }

        public double getYoungModulus()
        {
            return modulus;
        }

        @Override
        public double value(double indent) 
        {            
            double coefficient = getForceModulusRatio(indent);
            double force = modulus*coefficient;
            return force;
        }
    }

    public class HertzianHyperboloidFit extends HertzianFit<HertzianHyperboloid>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;
        private final HertzianHyperboloidForceIndentationFunction fittedFunction;

        private final Point1DData forceIndentationTransitionPoint;

        public HertzianHyperboloidFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianHyperboloid.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);
            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(fittableForceValues, convertedFittableIndentationValues, 1);
            double modulus = fit.getCoefficient(Double.valueOf(1));

            double[] lastCoveredPoint = regressionStrategy.getLastCoveredPoint(fittableForceValues, convertedFittableIndentationValues, fit);            
            double force = lastCoveredPoint[1];
            double indentation = ForceContourStackFunction.getIndentation(fittableForceValues, fittableIndentationValues, force);

            this.forceIndentationTransitionPoint = new Point1DData(indentation, force, Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
            this.fittedFunction = new HertzianHyperboloidForceIndentationFunction(modulus);
        }

        @Override
        public double getYoungModulus() 
        {                
            double modulus = fittedFunction.getYoungModulus();
            return modulus;
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            double denominator = getForceModulusRatio(indentation);

            double modulus = denominator > 0 ? force/denominator : Double.NaN;
            return modulus; 
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint() 
        {       
            return forceIndentationTransitionPoint;
        }

        @Override
        protected HertzianHyperboloidForceIndentationFunction getFittedFunction() 
        {
            return fittedFunction;
        }
    }
}



