
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
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import atomicJ.analysis.ForceContourStackFunction;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.FittedLinearUnivariateFunction;



public class HertzianSneddonSphere extends HertzianContactModel
{
    private static final double TOLERANCE = 1e-10;

    private static final String NAME = "Sphere (Sneddon)";

    private final Sphere indenter;
    private final SampleModel sample;

    private final double poissonFactor;
    private final double radius;

    public HertzianSneddonSphere(Sphere indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
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
    public HertzianSneddonSphereFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianSneddonSphereFit fit = new HertzianSneddonSphereFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    private double getForceModulusRatio(double indent)
    {
        double coefficient = 0;

        if(indent>TOLERANCE)
        {
            //this is the upper limit on the contact radius - as if the contact were already at the level of the sample surface
            //use as an upper bound for solution by the Brent - Dreker algorithm
            double max = indent<radius ? Math.sqrt(2*radius - indent)*Math.sqrt(indent) : radius;              

            ContactDepthFunction funct = new ContactDepthFunction(indent);

            UnivariateSolver solver = new BrentSolver(1e-12);

            double a = solver.solve(5000, funct,  0, max);                
            coefficient = poissonFactor*(((radius*radius + a*a)/2)*Math.log((radius + a)/(radius - a)) - a*radius);

        }

        return coefficient;
    }

    private double[] convertIndentationData(double[] indentationValues)
    {
        int n = indentationValues.length;
        double[] indentationValuesConverted = new double[n];

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            indentationValuesConverted[i] = getForceModulusRatio(indent);
        }

        return indentationValuesConverted;       
    } 

    private double[] transformIndentationData(double[] indentationValues)
    {
        int n = indentationValues.length;

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            indentationValues[i] =  getForceModulusRatio(indent);
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
            //this is the upper limit on the contact radius - as if the contact were already at the level of the sample surface
            //use as an upper bound for solution by the Brent - Dreker algorithm
            double max = indent<radius ? Math.sqrt(2*radius - indent)*Math.sqrt(indent) : radius;              

            ContactDepthFunction f = new ContactDepthFunction(indent);

            UnivariateSolver solver = new BrentSolver(1e-12);
            contactRadius = solver.solve(5000, f,  0, max);  
        }

        return contactRadius; 
    }

    public class ContactDepthFunction implements UnivariateFunction
    {
        private final double indent;

        public ContactDepthFunction(double indent)
        {
            this.indent = indent;
        }

        @Override
        public double value(double a) 
        {           
            double val = 0.5*a*Math.log((radius + a)/(radius - a)) - indent;
            return val;
        } 
    }

    public class SneddonSphereForceIndentationFunction implements UnivariateFunction
    {
        private final double modulus;

        public SneddonSphereForceIndentationFunction(double modulus)
        {
            this.modulus = modulus;
        }

        @Override
        public double value(double indent) 
        {
            double coefficient = getForceModulusRatio(indent);
            double force = coefficient*modulus;
            return force;
        }
    }

    public class HertzianSneddonSphereFit extends HertzianFit<HertzianSneddonSphere>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceIndentationTransitionPoint;

        private final SneddonSphereForceIndentationFunction fittedFunction;
        private final double modulus;

        public HertzianSneddonSphereFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianSneddonSphere.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);
            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(fittableForceValues, convertedFittableIndentationValues, 1);

            this.modulus = fit.getCoefficient(Double.valueOf(1));      
            this.fittedFunction = new SneddonSphereForceIndentationFunction(modulus);

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
            double modulus = denominator > 0 ? force/denominator : Double.NaN;
            return modulus;  
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint() 
        {
            return forceIndentationTransitionPoint;
        }

        @Override
        protected SneddonSphereForceIndentationFunction getFittedFunction() 
        {
            return fittedFunction;
        }
    }
}



