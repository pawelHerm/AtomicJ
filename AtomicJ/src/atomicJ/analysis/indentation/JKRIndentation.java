
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

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.functions.AbstractFittedUnivariateFunction;
import atomicJ.functions.ParametrizedUnivariateFunction;
import atomicJ.utilities.MathUtilities;


public class JKRIndentation extends JKRLikeContact
{
    private static final String NAME = "Sphere (JKR)";

    private final Sphere indenter;

    private final double poissonFactor;
    private final double radiusSI;
    private final double radiusSquare;

    public JKRIndentation(Sphere indenter, SampleModel sample, PrecontactInteractionsModel precontactModel, AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod)
    {
        super(sample, precontactModel,adhesiveEnergyEstimationMethod);
        this.indenter = indenter;

        double v = sample.getPoissonRatio();
        this.poissonFactor = 1-v*v;
        this.radiusSI = 1e-6*indenter.getRadius();
        this.radiusSquare = radiusSI*radiusSI;
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

    public double getIndentation(double contactRadius, double modulus, double adhesionWork)
    {                
        double reducedModulus = modulus/poissonFactor;
        double indentation = contactRadius*contactRadius/radiusSI - Math.sqrt(2*Math.PI*contactRadius*adhesionWork/reducedModulus);
        return indentation;
    }

    public double getContactRadius(double indentationSI, double adhesionWork, double modulusSI)
    {
        double reducedModulus = modulusSI/poissonFactor;
        double coefficient1 = (27*Math.PI*Math.PI*adhesionWork*adhesionWork*radiusSI/(32*reducedModulus*reducedModulus));

        double coeff2 = (indentationSI*indentationSI*indentationSI) + coefficient1;
        double delta = Math.cbrt(coeff2 + Math.sqrt(coeff2*coeff2 - MathUtilities.intPow(indentationSI, 6)));
        double coeff3 = radiusSI*(indentationSI + delta + (indentationSI*indentationSI)/delta)/3.;

        double contactRadius = Math.sqrt(coeff3) + Math.sqrt(-coeff3 + indentationSI*radiusSI+Math.PI*radiusSI*radiusSI*adhesionWork/(2*Math.sqrt(coeff3)*reducedModulus));     

        return contactRadius; 
    }

    @Override
    protected double getPullOffIndentation(double modulus, double adhesionWork, double springConstant)
    {
        double contactRadius = getPullOffContactRadius(adhesionWork, modulus, springConstant);
        double indentation = getIndentation(contactRadius, modulus, adhesionWork);
        return indentation;
    }

    public double getPullOffContactRadius(double adhesionWork, double modulus, double springConstant)
    {        
        JKRPullOffHelperFunction f = new JKRPullOffHelperFunction(modulus, adhesionWork, springConstant);

        double min = getFixedGripsPullOffContactRadius(adhesionWork, modulus);
        double max = getFixedLoadPullOffContactRadius(adhesionWork, modulus);

        UnivariateSolver solver = new BrentSolver(1e-12);
        double contactRadius = solver.solve(1000, f, min, max);

        return contactRadius;
    }

    @Override
    public double getFixedGripsPullOffContactRadius(double adhesionWork, double modulus)
    {
        double reducedModulus = modulus/poissonFactor;
        double contactRadius = Math.cbrt(Math.PI*adhesionWork*radiusSquare/(reducedModulus*8.));
        return contactRadius;
    }

    public double getFixedLoadPullOffContactRadius(double adhesionWork, double modulus)
    {
        double reducedModulus = modulus/poissonFactor;
        double contactRadius = Math.cbrt(9*Math.PI*adhesionWork*radiusSquare/(reducedModulus*8.));
        return contactRadius;
    }    

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs, double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        JKRForceIndentationFunction postcontactFit = fitYoungsModulusAndWorkOfAdhesion(postcontactForceSeparationYs, postcontactForceSeparationXs);
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionValue(postcontactForceSeparationYs, postcontactForceSeparationXs, postcontactFit);      
        return objectiveFunctionMinimum;
    }  

    @Override
    public JKRFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        JKRFit fit = new JKRFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    public JKRContactRadiusIndentationFunction getJKRContactRadiusIndentationFunction(double modulus, double adhesionWork)
    {
        return new JKRContactRadiusIndentationFunction(modulus, adhesionWork);
    }

    public JKRForceIndentationFunction getJKRForceIndentationFunction(double modulus, double adhesionWork)
    {
        return new JKRForceIndentationFunction(modulus, adhesionWork);
    }

    public JKRModulusFitHelperFunction getFitterFunction(double adhesionWork)
    {
        return new JKRModulusFitHelperFunction(adhesionWork);
    }

    public class JKRContactRadiusIndentationFunction implements UnivariateFunction
    {
        private final double reducedModulus;
        private final double adhesionWork;
        private final double coefficient1;

        public JKRContactRadiusIndentationFunction(double modulus, double adhesionWork)
        {
            this.reducedModulus = modulus/poissonFactor;
            this.adhesionWork = adhesionWork;

            this.coefficient1 = (27*Math.PI*Math.PI*adhesionWork*adhesionWork*radiusSI/(32*reducedModulus*reducedModulus));
        }

        @Override
        public double value(double indentationSI) 
        {
            double coeff2 = (indentationSI*indentationSI*indentationSI) + coefficient1;
            double delta = Math.cbrt(coeff2 + Math.sqrt(coeff2*coeff2 - MathUtilities.intPow(indentationSI, 6)));
            double coeff3 = radiusSI*(indentationSI + delta + (indentationSI*indentationSI)/delta)/3;

            double contactRadius = Math.sqrt(coeff3) + Math.sqrt(-coeff3 + indentationSI*radiusSI+Math.PI*radiusSI*radiusSI*adhesionWork/(2*Math.sqrt(coeff3)*reducedModulus));
            return contactRadius;
        }
    }

    public class JKRForceIndentationFunction extends AbstractFittedUnivariateFunction implements AdhesiveForceIndentationFunction, ParametrizedUnivariateFunction
    {
        private final double modulus;
        private final double reducedModulus;
        private final double adhesionWork;
        private final double coefficient1;

        public JKRForceIndentationFunction(double modulus, double adhesionWork)
        {
            this.modulus = modulus;
            this.reducedModulus = modulus/poissonFactor;
            this.adhesionWork = adhesionWork;

            this.coefficient1 = (27*Math.PI*Math.PI*adhesionWork*adhesionWork*radiusSI/(32*reducedModulus*reducedModulus));
        }

        @Override
        public double value(double indentation) 
        {
            double coeff2 = (indentation*indentation*indentation) + coefficient1;
            double delta = Math.cbrt(coeff2 + Math.sqrt(coeff2*coeff2 - MathUtilities.intPow(indentation, 6)));
            double coeff3 = radiusSI*(indentation + delta + (indentation*indentation)/delta)/3;

            double contactRadius = Math.sqrt(coeff3) + Math.sqrt(-coeff3 + indentation*radiusSI+Math.PI*radiusSI*radiusSI*adhesionWork/(2*Math.sqrt(coeff3)*reducedModulus));
            double force = 4*reducedModulus*MathUtilities.intPow(contactRadius, 3)/(3*radiusSI)-2*Math.sqrt(2*Math.PI*reducedModulus*adhesionWork*MathUtilities.intPow(contactRadius, 3));          

            return force;
        }

        @Override
        public double getYoungModulus()
        {
            return modulus;
        }

        @Override
        public double getAdhesionWork()
        {
            return adhesionWork;
        }

        @Override
        public double getAdhesionForce()
        {
            return 1.5*Math.PI*adhesionWork*radiusSI;
        }

        @Override
        public int getEstimatedParameterCount() 
        {
            return 2;
        }

        @Override
        public double[] getParameters()
        {
            return new double[] {modulus, adhesionWork};
        }
    }

    private class JKRPullOffHelperFunction implements UnivariateFunction
    {
        private final double reducedModulus;
        private final double adhesionWork;
        private final double springConstant;

        public JKRPullOffHelperFunction(double modulus, double adhesionWork, double springConstant)
        {
            this.reducedModulus = modulus/poissonFactor;
            this.adhesionWork = adhesionWork;
            this.springConstant = springConstant;
        }

        @Override
        public double value(double a)
        {
            double coeff1 = Math.sqrt(Math.PI*adhesionWork/(2.*a*reducedModulus));
            double coeff2 = 2.*a/radiusSI;
            double val = 2.*a*reducedModulus*(coeff2 - 3*coeff1) + springConstant*(coeff2 - coeff1);
            return val;
        }        
    }

    public class PointwiseModulusHelperFunction implements UnivariateFunction
    {
        private final double adhesionWork;
        private final double indentation;
        private final double indentationCube;
        private final double force;

        public PointwiseModulusHelperFunction(double indentation, double force, double adhesionWork)
        {
            this.adhesionWork = adhesionWork;
            this.indentation = indentation;
            this.indentationCube = indentation*indentation*indentation;
            this.force = force;
        }

        @Override
        public double value(double modulus)
        {            
            double reducedModulus = modulus/poissonFactor;
            double coefficient1 = (27*Math.PI*Math.PI*adhesionWork*adhesionWork*radiusSI/(32*reducedModulus*reducedModulus));

            double coeff2 = indentationCube + coefficient1;
            double delta = Math.cbrt(coeff2 + Math.sqrt(coeff2*coeff2 - indentationCube*indentationCube));
            double coeff3 = radiusSI*(indentation + delta + (indentation*indentation)/delta)/3;

            double contactRadius = Math.sqrt(coeff3) + Math.sqrt(-coeff3 + indentation*radiusSI+Math.PI*radiusSI*radiusSI*adhesionWork/(2*Math.sqrt(coeff3)*reducedModulus));

            double force = 4*reducedModulus*MathUtilities.intPow(contactRadius, 3)/(3*radiusSI)-2*Math.sqrt(2*Math.PI*reducedModulus*adhesionWork*MathUtilities.intPow(contactRadius, 3));          

            double val = (force - this.force);

            return val;
        }            
    }

    public class JKRModulusFitHelperFunction implements ParametricUnivariateFunction
    {
        private final double adhesionWork;

        public JKRModulusFitHelperFunction(double adhesionWork)
        {
            this.adhesionWork = adhesionWork;
        }

        @Override
        public double[] gradient(double indentationSI, double... parameters) 
        {
            double modulus = Math.abs(parameters[0]);
            double reducedModulus = modulus/poissonFactor;

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus);
            double contactRadiusSquare = contactRadius*contactRadius;
            double contactRadiusCube = contactRadiusSquare*contactRadius;

            //derivative dF/da

            double coeff3 = 4*reducedModulus/(3.*radiusSI);
            double coeff4 = 2*Math.PI*reducedModulus*adhesionWork;
            double dFsda = 3*contactRadiusSquare*coeff3 - 3*Math.sqrt(contactRadius*coeff4);

            //derivative (dF/dYM)a

            double coeff1 = 4.*contactRadiusCube/(3.*poissonFactor*radiusSI);
            double coeff2 = 2.*Math.PI*adhesionWork*contactRadiusCube/poissonFactor;

            double dFsdYMConstRadius = coeff1 - Math.sqrt(coeff2/modulus);

            //derivative (dYM/da) 

            double coeff5 = contactRadiusSquare - radiusSI*indentationSI;
            double coeff6 = coeff5*coeff5*coeff5;
            double coeff7 = 3*contactRadiusSquare + radiusSI*indentationSI;
            double coeff8 = -2*Math.PI*radiusSI*radiusSI*poissonFactor*adhesionWork*coeff7;

            double dYMsda = coeff8/coeff6;

            double dFsdYMConstantIndent = dFsdYMConstRadius + dFsda/dYMsda;

            return new double[] {dFsdYMConstantIndent};
        }

        @Override
        public double value(double indentationSI, double... parameters) 
        {
            double modulusSI = Math.abs(parameters[0]);
            double reducedModulus = modulusSI/poissonFactor;

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulusSI);
            double force = 4*reducedModulus*MathUtilities.intPow(contactRadius, 3)/(3*radiusSI)-2*Math.sqrt(2*Math.PI*reducedModulus*adhesionWork*MathUtilities.intPow(contactRadius, 3));

            return force;
        }        
    }

    public class JKRFullFitHelperFunction implements ParametricUnivariateFunction
    {
        @Override
        public double[] gradient(double indentationSI, double... parameters) 
        {
            double modulus = Math.abs(parameters[0]);
            double adhesionWork = Math.abs(parameters[1]);
            double reducedModulus = modulus/poissonFactor;

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus);
            double contactRadiusSquare = contactRadius*contactRadius;
            double contactRadiusCube = contactRadiusSquare*contactRadius;

            //derivative dF/da

            double coeff3 = 4*reducedModulus/(3.*radiusSI);
            double coeff4 = 2*Math.PI*reducedModulus*adhesionWork;
            double dFsda = 3*contactRadiusSquare*coeff3 - 3*Math.sqrt(contactRadius*coeff4);

            //derivative (dF/dYM)a

            double coeff1 = 4.*contactRadiusCube/(3.*poissonFactor*radiusSI);
            double coeff2 = 2.*Math.PI*adhesionWork*contactRadiusCube/poissonFactor;

            double dFsdYMConstRadius = coeff1 - Math.sqrt(coeff2/modulus);

            //derivative (dYM/da) 

            double coeff5 = contactRadiusSquare - radiusSI*indentationSI;
            double coeff6 = coeff5*coeff5*coeff5;
            double coeff7 = 3*contactRadiusSquare + radiusSI*indentationSI;
            double coeff8 = -2*Math.PI*radiusSquare*poissonFactor*adhesionWork*coeff7;

            double dYMsda = coeff8/coeff6;


            //derivative (dF/dAdhesionWork) constant contact radius

            double dFsdAdhesionWorkConstRadius = -Math.sqrt((2.*Math.PI*contactRadiusCube*reducedModulus)/adhesionWork);

            //derivative (dAdhesionWork/da) constant indentation

            double dAdhesionWorkda = 0.5*reducedModulus*coeff5*coeff7/(Math.PI*contactRadiusSquare*radiusSI*radiusSI);

            double dFsdYMConstantIndent = dFsdYMConstRadius + dFsda/dYMsda;
            double dFdAdhesionWorkConstantIndent = dFsdAdhesionWorkConstRadius + dFsda/dAdhesionWorkda;          

            return new double[] {dFsdYMConstantIndent, dFdAdhesionWorkConstantIndent};
        }

        @Override
        public double value(double indentationSI, double... parameters) 
        {
            double modulusSI = Math.abs(parameters[0]);
            double reducedModulus = modulusSI/poissonFactor;
            double adhesionWork = Math.abs(parameters[1]);

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulusSI);
            double force = 4*reducedModulus*MathUtilities.intPow(contactRadius, 3)/(3*radiusSI)-2*Math.sqrt(2*Math.PI*reducedModulus*adhesionWork*MathUtilities.intPow(contactRadius, 3));

            return force;
        }        
    }

    @Override
    protected JKRForceIndentationFunction fitYoungsModulus(double[] forceIndentationSI, double[] indentationValuesSI, double adhesionForce)
    {
        return fitYoungsModulus(forceIndentationSI, indentationValuesSI, 0, forceIndentationSI.length, adhesionForce, 10000);
    }

    @Override
    protected JKRForceIndentationFunction fitYoungsModulus(double[] forceIndentationSI, double[] indentationValuesSI, int from, int to, double adhesionForce)
    {
        return fitYoungsModulus(forceIndentationSI, indentationValuesSI, from, to, adhesionForce, 10000);
    }

    @Override
    public JKRForceIndentationFunction fitYoungsModulus(double[] forceIndentationSI, double[] indentationValuesSI, double adhesionForce, double startModulusValue)
    {
        return fitYoungsModulus(forceIndentationSI, indentationValuesSI, 0, forceIndentationSI.length, adhesionForce, startModulusValue);
    }

    @Override
    public JKRForceIndentationFunction fitYoungsModulus(double[] forceIndentationSI, double[] indentationValuesSI, int from, int to, double adhesionForce, double startModulusValue)
    {
        double adhesionWork = Math.max(1e-12, 2.*adhesionForce/(3.*radiusSI*Math.PI));

        LevenbergMarquardtOptimizer optimiser = new LevenbergMarquardtOptimizer();

        CurveFitter<JKRModulusFitHelperFunction> fitter = new CurveFitter<>(optimiser);

        for(int i = from; i < to; i++)
        {
            double x = indentationValuesSI[i];
            double y = forceIndentationSI[i];

            fitter.addObservedPoint(x, y);
        }

        double[] parameters = fitter.fit(200, new JKRModulusFitHelperFunction(adhesionWork), new double[] {startModulusValue});

        double modulus = Math.abs(parameters[0]);

        JKRForceIndentationFunction fittedFunction = new JKRForceIndentationFunction(modulus, adhesionWork);
        return fittedFunction;
    }

    @Override
    protected JKRForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValuesSI)
    {
        return fitYoungsModulusAndWorkOfAdhesion(forceValuesSI, indentationValuesSI, 0, forceValuesSI.length, 10000, 1e-4);
    }

    @Override
    protected JKRForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValuesSI, int from, int to)
    {
        return fitYoungsModulusAndWorkOfAdhesion(forceValuesSI, indentationValuesSI, from, to, 10000, 1e-4);
    }

    @Override
    public JKRForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValuesSI, double startModulusValue, double startsAdhesionWork)
    {
        return fitYoungsModulusAndWorkOfAdhesion(forceValuesSI, indentationValuesSI, 0, forceValuesSI.length, startModulusValue, startsAdhesionWork);
    }

    @Override
    public JKRForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValuesSI, int from, int to, double startModulusValue, double startsAdhesionWork)
    {
        LevenbergMarquardtOptimizer optimiser = new LevenbergMarquardtOptimizer();

        CurveFitter<JKRFullFitHelperFunction> fitter = new CurveFitter<>(optimiser);

        for(int i = from; i < to; i++)
        {
            double x = indentationValuesSI[i];
            double y = forceValuesSI[i];

            fitter.addObservedPoint(x, y);
        }

        double[] parameters = fitter.fit(200, new JKRFullFitHelperFunction(), new double[] {startModulusValue, startsAdhesionWork});

        double modulus = Math.abs(parameters[0]);
        double adhesionWork = Math.abs(parameters[1]);

        JKRForceIndentationFunction fittedFunction = new JKRForceIndentationFunction(modulus, adhesionWork);
        return fittedFunction;
    }


    public class JKRFit extends JKRLikeContactFit <JKRIndentation>
    {                
        private final Point1DData forceIndentationTransitionPoint;
        private final JKRForceIndentationFunction fittedFunction;

        private final double adhesionWorkSI;
        private final double modulusSI;

        public JKRFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
        {
            super(deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            AdhesiveAnnotatedForceIndentation annotatedForceIndentation = getAnnotatedForceIndentation();
            double[] forceValues = annotatedForceIndentation.getFittableForceValues();
            double[] indentationValues = annotatedForceIndentation.getFittableIndentationValues();

            double[][] positiveForceIndentationSI = annotatedForceIndentation.getPositiveForceIndentationSIXYView();

            this.fittedFunction =  isFitWorkOfAdhesionAlongsideYoungsModulus() ? 
                    fitYoungsModulusAndWorkOfAdhesion(positiveForceIndentationSI[1], positiveForceIndentationSI[0]) : 
                        fitYoungsModulus(positiveForceIndentationSI[1], positiveForceIndentationSI[0], annotatedForceIndentation.getAdhesion());

                    this.modulusSI = fittedFunction.getYoungModulus();
                    this.adhesionWorkSI = fittedFunction.getAdhesionWork();

                    RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
                    double[] forceIndentationTransitionPointData = regressionStrategy.getLastCoveredPoint(forceValues, indentationValues, new UnivariateFunction() {

                        @Override
                        public double value(double val)
                        {
                            double valSI = fittedFunction.value(1e-6*val);
                            return 1e9*valSI;
                        }
                    });
                    this.forceIndentationTransitionPoint = new Point1DData(forceIndentationTransitionPointData[0], forceIndentationTransitionPointData[1], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);           
        }

        @Override
        public JKRIndentation getContactModel() 
        {
            return JKRIndentation.this;
        }

        @Override
        protected JKRForceIndentationFunction getFittedFunction()
        {
            return fittedFunction;
        }

        //returns adhesion work in mJ/m^2
        @Override
        public double getAdhesionWork()
        {
            return 1e3*adhesionWorkSI;
        }

        @Override
        public double getYoungModulus() 
        {
            double modulus = 1e-3*modulusSI;  

            return modulus;
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            if(indentation<0)
            {
                return Double.NaN;
            }

            double forceSI = 1e-9*force;
            double indentationSI = 1e-6*indentation;

            PointwiseModulusHelperFunction f = new PointwiseModulusHelperFunction(indentationSI, forceSI, adhesionWorkSI);

            double min = 0.01*modulusSI;
            double max = 100*modulusSI;

            UnivariateSolver solver = new BrentSolver(1e-12,1e-12);

            try
            {      
                double pointwiseModulus = 1e-3*solver.solve(100, f, min, max);

                return pointwiseModulus;
            }
            catch(Exception e)
            {                              
                return Double.NaN;
            }

        }

        private double getPointwiseModulusLimit(double indentation)
        {
            if(indentation>=0)
            {
                return Double.POSITIVE_INFINITY;
            }

            double coeff1 = 3*Math.sqrt(3)*Math.PI*poissonFactor*adhesionWorkSI;
            double coeff2 = Math.sqrt(-indentation/Math.cbrt(radiusSI));
            double coeff3 = 8*coeff2*coeff2*coeff2;

            double modulusLimit = coeff1/coeff3;

            return modulusLimit;
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint()
        {
            return forceIndentationTransitionPoint;
        }
    }
}