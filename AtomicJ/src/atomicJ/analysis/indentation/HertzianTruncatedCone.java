
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



public class HertzianTruncatedCone extends HertzianContactModel
{
    private static final double TOLERANCE = 1e-10;

    private static final String NAME = "Truncated cone";

    private final TruncatedCone indenter;
    private final SampleModel sample;

    private final double poissonFactor;
    private final double cotTheta;
    private final double b;


    public HertzianTruncatedCone(TruncatedCone indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);

        this.indenter = indenter;
        this.sample = sample;

        double v = sample.getPoissonRatio();
        poissonFactor = 1/(1 - v*v);

        double angle = indenter.getHalfAngle();
        cotTheta = 1./Math.tan(angle);

        this.b = indenter.getTransitionRadius();        
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public HertzianTruncatedConeFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianTruncatedConeFit fit = new HertzianTruncatedConeFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
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

    private double getForceModulusRatio(double indent)
    {

        double ratio;
        if(indent>TOLERANCE)
        {
            double startValue =  b + (indent)/cotTheta;

            ContactDepthFunction funct = new ContactDepthFunction(indent);
            BrentSolver solver = new BrentSolver(1e-12);

            double a = solver.solve(5000, funct, b, startValue);
            ratio = 2*poissonFactor*(a*indent - 0.5*a*a*cotTheta*(0.5*Math.PI - Math.asin(b/a)) + Math.sqrt(a*a - b*b)
            *(0.5*b*cotTheta));
        }
        else
        {
            ratio = 0;
        }    

        return ratio;
    }

    private double[] transformIndentationData(double[] indentationValues, Point2D recordingPoint)
    {
        int n = indentationValues.length;

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];

            indentationValues[i] = getForceModulusRatio(indent);
        }

        return indentationValues;       
    } 


    private double[] convertIndentationData(double[] indentationValues, Point2D recordingPoint)
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


    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs,double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionMinimumForThroughOriginLinearRegression(postcontactForceSeparationYs, transformIndentationData(postcontactForceSeparationXs, recordingPoint));
        return objectiveFunctionMinimum;
    } 

    public double getContactRadius(double indent)
    {        
        double contactRadius = 0;
        if(indent>TOLERANCE)
        {           
            double startValue =  b + (indent)/cotTheta;

            ContactDepthFunction f = new ContactDepthFunction(indent);

            BrentSolver solver = new BrentSolver(1e-12);
            contactRadius = solver.solve(5000, f,  b, startValue);  
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
            double val = indent - a*cotTheta*(0.5*Math.PI - Math.asin(b/a));
            return val;
        }      
    }

    public class TruncatedConeForceIndentation implements UnivariateFunction
    {
        private final double modulus;

        public TruncatedConeForceIndentation(double modulus)
        {
            this.modulus = modulus;
        }

        @Override
        public double value(double indentation) 
        {
            double ratio = getForceModulusRatio(indentation);
            double force = ratio*modulus;
            return force;
        }        
    }

    public class HertzianTruncatedConeFit extends HertzianFit<HertzianTruncatedCone>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceIndentationTransitionPoint;

        private final TruncatedConeForceIndentation fittedFunction;
        private final double modulus;

        public HertzianTruncatedConeFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianTruncatedCone.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues, recordingPoint);       

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fittedTransformedFunction = regressionStrategy.getFitFunctionForSingleExponent(fittableForceValues,convertedFittableIndentationValues, 1);
            this.modulus = fittedTransformedFunction.getCoefficient(Double.valueOf(1));

            this.fittedFunction = new TruncatedConeForceIndentation(modulus);

            double[] lastCoveredPoint = regressionStrategy.getLastCoveredPoint(fittableForceValues, convertedFittableIndentationValues, fittedTransformedFunction);            
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
        protected TruncatedConeForceIndentation getFittedFunction() 
        {            
            return fittedFunction;
        }       
    }
}



