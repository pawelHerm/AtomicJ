
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

import atomicJ.analysis.OgdenExponentProcessedPackFunction;
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
import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.functions.ParametrizedUnivariateFunction;


public class OgdenIndentation extends HertzianContactModel
{
    private static final String NAME = "Sphere (Ogden)";

    private final Sphere indenter;

    private final double poissonFactor;
    private final double radiusSI;
    private final SampleModel sample;


    public OgdenIndentation(Sphere indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);

        this.indenter = indenter;
        this.sample = sample;

        double v = sample.getPoissonRatio();
        this.poissonFactor = 1-v*v;
        this.radiusSI = 1e-6*indenter.getRadius();
    }

    public double getTipRadius()
    {
        return this.indenter.getRadius();
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public Sphere getIndenter()
    {
        return indenter;
    }

    //poprawione
    public double getIndentation(double contactRadius, double modulus, double adhesionWork)
    {                
        double indentation = contactRadius*contactRadius/radiusSI;
        return indentation;
    }

    //poprawione
    public double getContactRadius(double indentationSI, double adhesionWork, double modulusSI)
    {
        double contactRadius = Math.sqrt(radiusSI*indentationSI);     
        return contactRadius; 
    }

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs, double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        OgdenForceIndentationFunction postcontactFit = fitFull(postcontactForceSeparationYs, postcontactForceSeparationXs);
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionValue(postcontactForceSeparationYs, postcontactForceSeparationXs, postcontactFit);      
        return objectiveFunctionMinimum;
    }   

    @Override
    public OgdenFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        OgdenFit fit = new OgdenFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    public JKRContactRadiusIndentationFunction getContactRadiusIndentationFunction()
    {
        return new JKRContactRadiusIndentationFunction();
    }

    public OgdenForceIndentationFunction getForceIndentationFunction(double modulus, double adhesionWork)
    {
        return new OgdenForceIndentationFunction(modulus, adhesionWork);
    }

    //poprawione
    public class JKRContactRadiusIndentationFunction implements UnivariateFunction
    {
        @Override
        public double value(double indentationSI) 
        {
            double contactRadius = Math.sqrt(indentationSI*radiusSI);
            return contactRadius;
        }
    }

    //poprawiony, sprawdzony
    public class OgdenForceIndentationFunction extends AbstractFittedUnivariateFunction implements ParametrizedUnivariateFunction, FittedUnivariateFunction
    {
        private final double modulusSI;
        private final double reducedModulusSI;
        private final double alpha;

        public OgdenForceIndentationFunction(double modulusSI, double alpha)
        {
            this.modulusSI = modulusSI;
            this.reducedModulusSI = modulusSI/poissonFactor;
            this.alpha = alpha; 
        }

        //aceppts indentation in microns and returns force in nN
        @Override
        public double value(double indentation) 
        {            
            double indentationSI = 1e-6*indentation;
            double contactRadius = Math.sqrt(indentationSI * radiusSI);
            double coeff2 = (1 - 0.2*contactRadius/radiusSI);
            double force = 40*contactRadius*contactRadius*reducedModulusSI/(9*alpha) * (Math.pow(coeff2, -0.5*alpha - 1) - Math.pow(coeff2, alpha - 1));          

            return 1e9*force;
        }

        //returns Young's modulus in kPa
        public double getYoungModulus()
        {
            return 1e-3*modulusSI;
        }

        public double getAlpha()
        {
            return alpha;
        }

        @Override
        public int getEstimatedParameterCount() 
        {
            return 2;
        }

        @Override
        public double[] getParameters()
        {
            return new double[] {1e-3*modulusSI, alpha};
        }
    }

    //poprawione, sprawdzone
    public class OgdenFullFitHelperFunction implements ParametricUnivariateFunction
    {
        @Override
        public double[] gradient(double indentationSI, double... parameters) 
        {            
            double modulus = Math.abs(parameters[0]);
            double alpha = Math.abs(parameters[1]);
            double reducedModulus = modulus/poissonFactor;

            double contactRadiusSquare = radiusSI*indentationSI;
            double contactRadius = Math.sqrt(contactRadiusSquare);

            //derivative (dF/dYM) constant indentation

            double coeff2 = (1 - 0.2*contactRadius/radiusSI);
            double dFdYMConstantIndent = 40*contactRadius*contactRadius/(9*alpha*poissonFactor) * (Math.pow(coeff2, -0.5*alpha - 1) - Math.pow(coeff2, alpha - 1));          

            //derivative (dF/dAlpha) contact indentation

            double coeff3 = 2 + alpha*Math.log(coeff2) + 2*Math.pow(coeff2, 3.*alpha/2.)*(alpha*Math.log(coeff2) - 1);
            double dFdAlphaConstantIndent = -(40*reducedModulus*contactRadiusSquare*Math.pow(coeff2, -0.5*alpha - 1)*coeff3)/(18*alpha*alpha);


            return new double[] {dFdYMConstantIndent, dFdAlphaConstantIndent};
        }

        @Override
        public double value(double indentationSI, double... parameters) 
        {            
            double contactRadius = Math.sqrt(indentationSI * radiusSI);
            double reducedModulus = parameters[0]/poissonFactor;
            double alpha = parameters[1];
            double coeff2 = (1 - 0.2*contactRadius/radiusSI);
            double force = 40*contactRadius*contactRadius*reducedModulus/(9*alpha) * (Math.pow(coeff2, -0.5*alpha - 1) - Math.pow(coeff2, alpha - 1));          

            return force;
        }        
    }

    public OgdenForceIndentationFunction fitFull(double[][] forceIndentation)
    {
        return fitFull(forceIndentation, 10000, 1);
    }

    public OgdenForceIndentationFunction fitFull(double[][] forceIndentation, double startModulusValue, double startAlpha)
    {
        LevenbergMarquardtOptimizer optimiser = new LevenbergMarquardtOptimizer();

        CurveFitter<OgdenFullFitHelperFunction> fitter = new CurveFitter<>(optimiser);

        for(double[] p : forceIndentation)
        {
            double x = 1e-6*p[0];
            double y = 1e-9*p[1];

            fitter.addObservedPoint(x, y);
        }

        double[] parameters = fitter.fit(200, new OgdenFullFitHelperFunction(), new double[] {startModulusValue, startAlpha});

        double modulus = Math.abs(parameters[0]);
        double alpha = Math.abs(parameters[1]);

        OgdenForceIndentationFunction fittedFunction = new OgdenForceIndentationFunction(modulus, alpha);
        return fittedFunction;
    }

    public OgdenForceIndentationFunction fitFull(double[] forceValues, double[] indentationValues)
    {
        return fitFull(forceValues, indentationValues, 10000, 1);
    }


    public OgdenForceIndentationFunction fitFull(double[] forceValues, double[] indentationValues, double startModulusValue, double startAlpha)
    {
        LevenbergMarquardtOptimizer optimiser = new LevenbergMarquardtOptimizer();

        CurveFitter<OgdenFullFitHelperFunction> fitter = new CurveFitter<>(optimiser);

        int n = forceValues.length;
        for(int i = 0; i < n; i++)
        {
            double x = 1e-6*indentationValues[i];
            double y = 1e-9*forceValues[i];

            fitter.addObservedPoint(x, y);
        }

        double[] parameters = fitter.fit(200, new OgdenFullFitHelperFunction(), new double[] {startModulusValue, startAlpha});

        double modulus = Math.abs(parameters[0]);
        double alpha = Math.abs(parameters[1]);

        OgdenForceIndentationFunction fittedFunction = new OgdenForceIndentationFunction(modulus, alpha);
        return fittedFunction;
    }

    public class OgdenFit extends HertzianFit <OgdenIndentation>
    {                
        private final Point1DData forceIndentationTransitionPoint;
        private final OgdenForceIndentationFunction fittedFunction;

        public OgdenFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
        {
            super(OgdenIndentation.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            HertzianAnnotatedForceIndentation annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();

            this.fittedFunction = fitFull(fittableForceValues, fittableIndentationValues);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            double[] forceIndentationTransitionPointData = regressionStrategy.getLastCoveredPoint(fittableForceValues, fittableIndentationValues, fittedFunction);
            this.forceIndentationTransitionPoint = new Point1DData(forceIndentationTransitionPointData[0],forceIndentationTransitionPointData[1],Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);    
        }

        @Override
        public List<ProcessedPackFunction<ProcessedSpectroscopyPack>> getSpecialFunctions()
        {
            List<ProcessedPackFunction<ProcessedSpectroscopyPack>> processedPackFunctions = Collections.singletonList(OgdenExponentProcessedPackFunction.getInstance());
            return processedPackFunctions;
        }

        @Override
        public OgdenIndentation getContactModel() 
        {
            return OgdenIndentation.this;
        }

        @Override
        protected OgdenForceIndentationFunction getFittedFunction()
        {
            return fittedFunction;
        }

        public double getOgdenExponent()
        {
            return fittedFunction.getAlpha();
        }

        @Override
        public double getYoungModulus() 
        {
            double modulus = fittedFunction.getYoungModulus();
            return modulus;
        }

        //returns pointwise modulus in kPa
        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            if(indentation<0)
            {
                return Double.NaN;
            }
            double alpha = fittedFunction.getAlpha();

            double forceSI = 1e-9*force;
            double indentationSI = 1e-6*indentation;
            double contactRadius = Math.sqrt(indentationSI*radiusSI);

            double coeff2 = (1 - 0.2*contactRadius/radiusSI);
            double denominator = 40*contactRadius*contactRadius/(9*alpha*poissonFactor) * (Math.pow(coeff2, -0.5*alpha - 1) - Math.pow(coeff2, alpha - 1));          

            double pointwiseModulus = 1e-3*forceSI/denominator;

            return pointwiseModulus;
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint()
        {
            return forceIndentationTransitionPoint;
        }
    }

    @Override
    public SampleModel getSampleModel() 
    {
        return sample;
    }
}