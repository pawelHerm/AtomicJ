
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
import org.apache.commons.math3.analysis.solvers.BrentSolver;

import atomicJ.analysis.ForceContourStackFunction;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.FittedLinearUnivariateFunction;


public class HertzianBluntPyramid extends HertzianContactModel
{
    private static final double TOLERANCE = 1e-10;

    private static final String NAME = "Blunt pyramid";

    private final BluntPyramid indenter;
    private final SampleModel sample;

    private final double poissonFactor;
    private final double cotTheta;
    private final double R;
    private final double sqrtR;
    private final double b;
    private final double m;
    private final double n;

    private final double modelSwitch;

    public HertzianBluntPyramid(BluntPyramid indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);

        this.indenter = indenter;
        this.sample = sample;

        double v = sample.getPoissonRatio();
        this.poissonFactor = 1/(1 - v*v);

        double angle = indenter.getHalfAngle();
        this.cotTheta = 1./Math.tan(angle);

        this.R = indenter.getApexRadius();
        this.sqrtR = Math.sqrt(R);
        this.b = indenter.getTransitionRadius();

        this.modelSwitch = b*b/R;

        this.m = Math.sqrt(2)/Math.PI;
        this.n = Math.sqrt(2)*2/Math.PI;       
    }   

    @Override
    public String getName()
    {
        return NAME;
    }

    public double getTipRadius()
    {
        return this.indenter.getApexRadius();
    }

    @Override
    public BluntPyramid getIndenter()
    {
        return indenter;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }

    @Override
    public HertzianBluntPyramidFit getModelFit(Channel1DData deflectionCurveBranch, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianBluntPyramidFit fit = new HertzianBluntPyramidFit(deflectionCurveBranch, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    private double getForceModulusRatio(double indentation)
    {
        double coefficient;
        if(indentation>modelSwitch)
        {
            double startValue = b + (indentation - R + Math.sqrt(R*R - b*b))/cotTheta;

            ContactDepthFunction funct = new ContactDepthFunction(indentation);
            BrentSolver solver = new BrentSolver(1e-12);

            double a = solver.solve(5000, funct, b, startValue);

            coefficient = 2*poissonFactor*(a*indentation - m*a*a*cotTheta*(0.5*Math.PI - Math.asin(b/a)) - a*a*a/(3*R) + Math.sqrt(a*a - b*b)
            *(m*b*cotTheta + (a*a - b*b)/(3*R)));                

        }
        else
        {
            coefficient = 4.*poissonFactor*sqrtR*Math.sqrt(indentation)*indentation/3.;
        }

        return coefficient;
    }

    private double[] transformIndentationData(double[] forceIndentationXs)
    {
        int n = forceIndentationXs.length;

        for(int i = 0; i<n; i++)
        {
            double indent = forceIndentationXs[i];
            forceIndentationXs[i] = getForceModulusRatio(indent);
        }

        return forceIndentationXs;       
    } 

    private double[] convertIndentationData(double[] forceIndentationXs)
    {
        int n = forceIndentationXs.length;
        double[] forceIndentationXsConverted = new double[n];

        for(int i = 0; i<n; i++)
        {
            double indent = forceIndentationXs[i];
            forceIndentationXsConverted[i] = getForceModulusRatio(indent);
        }

        return forceIndentationXsConverted;       
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
            double startValue = b + (indent - R + Math.sqrt(R*R - b*b))/cotTheta;

            ContactDepthFunction f = new ContactDepthFunction(indent);

            BrentSolver solver = new BrentSolver(1e-12);
            contactRadius = solver.solve(5000, f,  b, startValue);  
        }

        return contactRadius; 
    }

    private class ContactDepthFunction implements UnivariateFunction
    {
        private final double indent;

        public ContactDepthFunction(double indent)
        {
            this.indent = indent;
        }

        @Override
        public double value(double a) 
        {           
            double val = indent + a*((Math.sqrt(a*a - b*b) - a)/R) - a*n*cotTheta*(0.5*Math.PI - Math.asin(b/a));
            return val;
        }       
    }

    public class BluntPyramidFroceIndentationFunction implements UnivariateFunction
    {
        private final double modulus;

        public BluntPyramidFroceIndentationFunction(double modulus)
        {
            this.modulus = modulus;
        }

        @Override
        public double value(double indent) 
        {
            double coefficient = getForceModulusRatio(indent);
            double force = modulus*coefficient;
            return force;
        }       
    }

    public class HertzianBluntPyramidFit extends HertzianFit<HertzianBluntPyramid>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceIndentationTransitionPoint;

        private final BluntPyramidFroceIndentationFunction fittedFunction;
        private final double modulus;

        public HertzianBluntPyramidFit(Channel1DData deflectionCurveBranch, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianBluntPyramid.this, deflectionCurveBranch, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();
            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(fittableForceValues, convertedFittableIndentationValues, 1);

            this.modulus = fit.getCoefficient(Double.valueOf(1));
            this.fittedFunction = new BluntPyramidFroceIndentationFunction(modulus);

            double[] lastCoveredPoint = regressionStrategy.getLastCoveredPoint(fittableForceValues, convertedFittableIndentationValues, fit); 

            double force = lastCoveredPoint[1];
            double indentation = ForceContourStackFunction.getIndentation(fittableForceValues, fittableIndentationValues, force);
            this.forceIndentationTransitionPoint = new Point1DData(indentation, force, Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
        }

        @Override
        public double getYoungModulus() 
        {
            return modulus;
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            double denominator = getForceModulusRatio(indentation);          

            double modulus = force/denominator;
            return modulus;       
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint() 
        {
            return forceIndentationTransitionPoint;
        }

        @Override
        protected BluntPyramidFroceIndentationFunction getFittedFunction() 
        {
            return fittedFunction;
        }
    }
}



