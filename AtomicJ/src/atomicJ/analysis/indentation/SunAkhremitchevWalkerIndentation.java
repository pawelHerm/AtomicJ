
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 by PaweĹ‚ Hermanowicz
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
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.functions.AbstractFittedUnivariateFunction;
import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.functions.ParametrizedUnivariateFunction;


public class SunAkhremitchevWalkerIndentation extends JKRLikeContact
{
    private static final double TOLERANCE = 1e-15;

    private static final String NAME = "Hyperboloid (Sun)";

    private final Hyperboloid indenter;

    private final double cotAngle;
    private final double poissonFactor;
    private final double radiusSI;
    private final double radiusSquare;
    private final double A;

    public SunAkhremitchevWalkerIndentation(Hyperboloid indenter, SampleModel sample, PrecontactInteractionsModel precontactModel, AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod)
    {
        super(sample, precontactModel, adhesiveEnergyEstimationMethod);
        this.indenter = indenter;

        double v = sample.getPoissonRatio();
        this.poissonFactor = 1-v*v;
        this.radiusSI = 1e-6*indenter.getRadius();
        this.radiusSquare = radiusSI*radiusSI;
        double angle = indenter.getHalfAngle();
        this.cotAngle = 1./Math.tan(angle);
        this.A = radiusSI*cotAngle;
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
    public Hyperboloid getIndenter()
    {
        return indenter;
    }

    //SUN
    public double getIndentation(double contactRadius, double modulusSI, double adhesionWorkSI)
    {                
        double reducedModulus = modulusSI/poissonFactor;
        double coeff2 = contactRadius/A;
        double coeff2Square = coeff2*coeff2;

        double coeff3 = 0.5*Math.PI + Math.asin((coeff2Square - 1)/(coeff2Square + 1));
        double coeff4 = 0.5*contactRadius*cotAngle*coeff3;

        double coeff5 = Math.sqrt(2*contactRadius*Math.PI*adhesionWorkSI/reducedModulus);

        double force = coeff4 - coeff5;
        return force;
    }

    @Override
    protected double getPullOffIndentation(double modulusSI, double adhesionWorkSI, double springConstant)    
    {
        double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWorkSI, modulusSI);
        double contactRadius = getPullOffContactRadius(adhesionWorkSI, modulusSI, springConstant,fixedGripsContact);
        double neckIndentation = getIndentation(contactRadius, modulusSI, adhesionWorkSI);
        return neckIndentation;
    }

    //SUN
    public double getAdhesionWork(double adhesionForceSI, double modulusSI)
    {                       
        if(adhesionForceSI < TOLERANCE)
        {
            return 0;
        }

        UnivariateFunction f = new AdhesionWorkHerlperFunction(adhesionForceSI, modulusSI);

        double min = findCloseNegative(2.*adhesionForceSI/(3.*radiusSI*Math.PI),f);
        double max = findClosePositive(4*min, f);

        UnivariateSolver solver = new BrentSolver(1e-12);
        try
        {
            double adhesionWork = solver.solve(200, f, min, max);           
            return adhesionWork;

        }
        catch(Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }
    //SUN
    public double getPullOffContactRadius(double adhesionWork, double modulusSI, double springConstant, double fixedGripsContact)
    {        
        PullOffHelperFunction f = new PullOffHelperFunction(modulusSI, adhesionWork, springConstant);

        double min = fixedGripsContact;
        double max = findClosePositive(getFixedLoadPullOffJKRContactRadius(adhesionWork, modulusSI), f);

        UnivariateSolver solver = new BrentSolver(1e-12);
        double contactRadius = solver.solve(1000, f, min, max);

        return contactRadius;
    }

    //SUN
    public double getFixedLoadPullOffContactRadius(double adhesionWork, double modulusSI, double fixedGripsContact)
    {        
        FixedLoadContactRadiusHelperFunction f = new FixedLoadContactRadiusHelperFunction(modulusSI, adhesionWork);

        double min = fixedGripsContact;
        double max = findClosePositive(getFixedLoadPullOffJKRContactRadius(adhesionWork, modulusSI), f);

        if(f.value(fixedGripsContact)>-TOLERANCE)
        {
            return fixedGripsContact;
        }

        UnivariateSolver solver = new BrentSolver(1e-12);
        double contactRadius = solver.solve(100, f, min, max);

        return contactRadius;
    }

    private double getFixedLoadPullOffJKRContactRadius(double adhesionWork, double modulus)
    {
        double reducedModulus = modulus/poissonFactor;
        double contactRadius = Math.cbrt(9*Math.PI*adhesionWork*radiusSI*radiusSI/(reducedModulus*8.));
        return contactRadius;
    }

    @Override
    public double getFixedGripsPullOffContactRadius(double adhesionWork, double modulus)
    {
        UnivariateObjectiveFunction f = new UnivariateObjectiveFunction(new IndentationContactRadiusFunction(modulus, adhesionWork));

        UnivariateOptimizer optimizer = new BrentOptimizer(1e-9, 1e-14);
        MaxEval maxEval = new MaxEval(200);

        double pullOffContactRadius = optimizer.optimize(maxEval, f, GoalType.MINIMIZE, new SearchInterval(0, 10*radiusSI)).getPoint();

        return pullOffContactRadius;
    }

    public class IndentationContactRadiusFunction implements UnivariateFunction
    {
        private final double modulusSI;
        private final double adhesionWorkSI;

        public IndentationContactRadiusFunction(double modulusSI, double adhesionWorkSI)
        {
            this.modulusSI = modulusSI;
            this.adhesionWorkSI = adhesionWorkSI;
        }

        @Override
        public double value(double contactRadius) 
        {
            double indentation = getIndentation(contactRadius, modulusSI, adhesionWorkSI);
            return indentation;
        }     
    }

    //we fit a linear function with an intercept - thanks to this, the objective function is usually unimodal
    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs, double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        SunForceIndentationFunction postcontactFit = fitYoungsModulusAndWorkOfAdhesion(postcontactForceSeparationYs, postcontactForceSeparationXs);
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionValue(postcontactForceSeparationYs, postcontactForceSeparationXs, postcontactFit);      
        return objectiveFunctionMinimum;
    }      

    @Override
    public SunFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        SunFit fit = new SunFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    public ContactRadiusIndentationFunction getJKRContactRadiusIndentationFunction(double modulus, double adhesionWork)
    {
        return new ContactRadiusIndentationFunction(modulus, adhesionWork);
    }

    public SunForceIndentationFunction getJKRForceIndentationFunction(double modulus, double adhesionWork)
    {
        return new SunForceIndentationFunction(modulus, adhesionWork);
    }

    //SUN
    public class ContactRadiusIndentationFunction implements UnivariateFunction
    {
        private final double modulus;
        private final double adhesionWork;
        private final double fixedGripsContact;

        public ContactRadiusIndentationFunction(double modulus, double adhesionWork)
        {
            this.modulus = modulus;
            this.adhesionWork = adhesionWork;
            this.fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);
        }

        @Override
        public double value(double indentation) 
        {   
            double contactRadius =  getContactRadius(indentation, adhesionWork, modulus, fixedGripsContact);
            return contactRadius;
        }
    }

    //SUN
    public class SunForceIndentationFunction extends AbstractFittedUnivariateFunction implements AdhesiveForceIndentationFunction, ParametrizedUnivariateFunction, FittedUnivariateFunction
    {
        private final double modulusSI;
        private final double adhesionWork;
        private final double fixedGripsContact;

        public SunForceIndentationFunction(double modulus, double adhesionWork)
        {
            this.modulusSI = modulus;
            this.adhesionWork = adhesionWork;
            this.fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);
        }

        @Override
        public double value(double indentationSI) 
        {   
            try
            {
                double contactRadius = getContactRadius(indentationSI, adhesionWork, modulusSI, fixedGripsContact);
                double force = getForceForContactRadius(contactRadius, modulusSI/poissonFactor, adhesionWork);

                return force;             
            }
            catch(IllegalStateException e)
            {
                return Double.NaN;
            }
        }

        @Override
        public double getYoungModulus()
        {
            return modulusSI;
        }

        @Override
        public double getAdhesionWork()
        {
            return adhesionWork;
        }

        @Override
        public double getAdhesionForce()
        {
            double contactRadius = getFixedLoadPullOffContactRadius(adhesionWork, modulusSI, fixedGripsContact);
            double adhesionForce = getForceForContactRadius(contactRadius, modulusSI/poissonFactor, contactRadius);  
            return adhesionForce;
        }

        @Override
        public int getEstimatedParameterCount() 
        {
            return 2;
        }

        @Override
        public double[] getParameters()
        {
            return new double[] {modulusSI, adhesionWork};
        }
    }


    //SUN
    public double getContactRadius(double indentationSI, double adhesionWork, double modulusSI)
    {
        //this is the upper limit on the contact radius - as if the contact were already at the level of the sample surface
        //use as an upper bound for solution by the Brent - Dreker algorithm

        double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulusSI);
        return getContactRadius(indentationSI, adhesionWork, modulusSI, fixedGripsContact); 
    }

    public double getContactRadius(double indentationSI, double adhesionWork, double modulusSI, double fixedGripsContact)
    {
        //this is the upper limit on the contact radius - as if the contact were already at the level of the sample surface
        //use as an upper bound for solution by the Brent - Dreker algorithm

        ContactRadiusHelperFunction f = new ContactRadiusHelperFunction(indentationSI, adhesionWork, modulusSI);

        if(f.value(fixedGripsContact)>= -TOLERANCE)
        {
            return fixedGripsContact;
        }

        UnivariateSolver solver = new BrentSolver(1e-12);

        double min = fixedGripsContact;
        double max = findClosePositive(radiusSI, f);

        try
        {   
            double contactRadius = solver.solve(1000, f, min, max);                   
            return contactRadius;     
        }
        catch(Exception e)
        {          
            throw e;
        }

    }

    //SUN
    private double findCloseNegative(double proposal, UnivariateFunction f)
    {
        double proposalValue = f.value(proposal);

        while(proposalValue>0)
        {
            if(proposal <TOLERANCE)
            {
                throw new IllegalStateException();
            }
            proposal = 0.5*proposal;
            proposalValue = f.value(proposal);

        }

        return proposal;
    }

    //SUN
    private double findClosePositive(double proposal, UnivariateFunction f)
    {
        double proposalValue = f.value(proposal);

        while(proposalValue<0)
        {
            proposal = 2*proposal;
            proposalValue = f.value(proposal);
        }

        return proposal;
    }

    //SUN
    public class ContactRadiusHelperFunction implements UnivariateFunction
    {
        private final double indentationSI;            
        private final double coeff1;

        public ContactRadiusHelperFunction(double indentationSI, double adhesionWork, double modulusSI)
        {
            this.indentationSI = indentationSI;
            this.coeff1 = 2.*Math.PI*adhesionWork*poissonFactor/modulusSI;
        }

        @Override
        public double value(double contactRadius) 
        {       
            double coeff2 = contactRadius/A;
            double coeff2Square = coeff2*coeff2;

            double coeff3 = 0.5*Math.PI + Math.asin((coeff2Square - 1.)/(coeff2Square + 1.));

            double predictedIndentation = 0.5*contactRadius*cotAngle*coeff3 - Math.sqrt(coeff1*contactRadius);

            double val = predictedIndentation - indentationSI;

            return val;
        }
    }

    //SUN
    public AdhesionWorkHerlperFunction getAdhesionWorkHerlperFunction(double adhesionForceSI, double modulusSI)
    {
        return new AdhesionWorkHerlperFunction(adhesionForceSI, modulusSI);
    }

    //SUN
    public class AdhesionWorkHerlperFunction implements UnivariateFunction
    {
        private final double adhesionForceSI;  
        private final double modulusSI;
        private final double reducedModulus;

        public AdhesionWorkHerlperFunction(double adhesionForceSI, double modulusSI)
        {
            this.adhesionForceSI = adhesionForceSI;
            this.modulusSI = modulusSI;
            this.reducedModulus = modulusSI/poissonFactor;
        }

        @Override
        public double value(double adhesionWork) 
        {                
            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulusSI);
            double contactRadius = getFixedLoadPullOffContactRadius(adhesionWork, modulusSI,fixedGripsContact);

            double predictedForce = getForceForContactRadius(contactRadius, reducedModulus, adhesionWork);
            double val = -adhesionForceSI - predictedForce;

            return val;
        } 
    }

    //SUN
    //this class is used by Brent method to find the contact radius for which df/da = 0, i.e.
    //the contact radius, for which the adhesionForce is the lowest
    private class PullOffHelperFunction implements UnivariateFunction
    {
        private final double reducedModulus;
        private final double springConstant;
        private final double coeff1;

        public PullOffHelperFunction(double modulus, double adhesionWork, double springConstant)
        {
            this.reducedModulus = modulus/poissonFactor;
            this.springConstant = springConstant;
            this.coeff1 = 2*Math.PI*adhesionWork/reducedModulus;
        }

        @Override
        public double value(double contactRadius)
        {
            //calculate (dF/da)YM

            double contactSquare = contactRadius*contactRadius;

            double coeff2 = contactSquare + A*A;
            double coeff3 = Math.asin((contactSquare - A*A)/(coeff2));
            double coeff5 = Math.sqrt(contactRadius*coeff1);
            double coeff6 = contactRadius*(4*contactRadius*A + Math.PI*contactSquare + Math.PI*A*A)/(2*coeff2);

            double dFda = reducedModulus*(cotAngle*(coeff6 + contactRadius*coeff3) - 3*coeff5);

            //calculate (dIndent/da)YM

            double coeff7 = 4*A*contactRadius/coeff2 + Math.PI;
            double dIndentda = (cotAngle/4.)*(coeff7) - 0.5*Math.sqrt(coeff1/contactRadius) + 0.5*cotAngle*coeff3;

            double value = dFda + springConstant*dIndentda;

            return value;
        }        
    }

    //SUN
    //used by UnivariateSolver a contact radius for which the derivative dF/da equals zero
    //i.e. to find the contact radius at pull off for fixed loads
    private class FixedLoadContactRadiusHelperFunction implements UnivariateFunction
    {
        private final double reducedModulus;
        private final double coeff1;

        public FixedLoadContactRadiusHelperFunction(double modulus, double adhesionWork)
        {
            this.reducedModulus = modulus/poissonFactor;
            this.coeff1 = 2*Math.PI*adhesionWork/reducedModulus;
        }

        @Override
        public double value(double contactRadius)
        {
            double contactSquare = contactRadius*contactRadius;

            //calculate (dF/da)YM

            double coeff2 = contactSquare + A*A;
            double coeff3 = Math.asin((contactSquare - A*A)/(coeff2));
            double coeff5 = Math.sqrt(contactRadius*coeff1);
            double coeff6 = contactRadius*(4*contactRadius*A + Math.PI*contactSquare + Math.PI*A*A)/(2*coeff2);

            double dFda = reducedModulus*(cotAngle*(coeff6 + contactRadius*coeff3) - 3*coeff5);

            return dFda;
        }        
    }

    //SUN
    private double getForceForContactRadius(double contactRadius, double reducedModulus, double adhesionWork)
    {
        double coeff1 = 0.5*(contactRadius*contactRadius - A*A);
        double coeff2 = contactRadius/A;
        double coeff2Square = coeff2*coeff2;

        double coeff3 = 0.5*Math.PI + Math.asin((coeff2Square - 1)/(coeff2Square + 1));
        double coeff4 = contactRadius*A + coeff1 *coeff3;

        double coeff5 = contactRadius*Math.sqrt(2*contactRadius*Math.PI*adhesionWork/reducedModulus);

        double force = 2*reducedModulus*(0.5*cotAngle*coeff4 - coeff5);
        return force;
    }

    //SUN
    public class PointwiseModulusHelperFunction implements UnivariateFunction
    {
        private final double adhesionWork;
        private final double indentation;
        private final double force;

        public PointwiseModulusHelperFunction(double indentation, double force, double adhesionWork)
        {
            this.adhesionWork = adhesionWork;
            this.indentation = indentation;
            this.force = force;
        }

        @Override
        public double value(double modulus)
        {            
            double contactRadius = getContactRadius(indentation, adhesionWork, modulus);
            double reducedModulus = modulus/poissonFactor;
            double force = getForceForContactRadius(contactRadius, reducedModulus, adhesionWork);

            double val = (force - this.force);

            return val;
        }     
    }

    //SUN
    public class SunYoungsModulusFitHelperFunction implements JKRLikeFitHelper
    {
        private final double adhesionForceSI;

        public SunYoungsModulusFitHelperFunction(double adhesionForceSI)
        {
            this.adhesionForceSI = adhesionForceSI;
        }       

        @Override
        public double calculateAdhesionWork(double modulus)
        {
            double adhesionWork = getAdhesionWork(adhesionForceSI, modulus);
            return adhesionWork;
        }

        @Override
        public double[] gradient(double indentationSI, double... parameters) 
        {
            double modulus = Math.abs(parameters[0]);
            double adhesionWork = getAdhesionWork(adhesionForceSI, modulus);
            return gradient(indentationSI, adhesionWork, modulus);
        }

        @Override
        public double[] gradient(double indentationSI, double adhesionWork, double modulus) 
        {    
            modulus = Math.abs(modulus);
            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);
            return gradient(indentationSI, adhesionWork, modulus, fixedGripsContact);
        }

        @Override
        public double[] gradient(double indentationSI, double adhesionWork, double modulus, double fixedGripsContact) 
        {    
            modulus = Math.abs(modulus);

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus, fixedGripsContact);       
            double reducedModulus = modulus/poissonFactor;
            double contactSquare = contactRadius*contactRadius;

            //calculate (dF/da)YM

            double coeff1 = 2*Math.PI*adhesionWork/reducedModulus;
            double coeff2 = contactSquare + A*A;
            double coeff3 = Math.asin((contactSquare - A*A)/(coeff2));
            double coeff5 = Math.sqrt(contactRadius*coeff1);
            double coeff6 = contactRadius*(4*contactRadius*A + Math.PI*contactSquare + Math.PI*A*A)/(2*coeff2);

            double dFda = reducedModulus*(cotAngle*(coeff6 + contactRadius*coeff3) - 3*coeff5);

            //(dF/dYM)a

            double coeff7 = 4*radiusSI*contactRadius*coeff5;
            double coeff8 = 2*A*(contactSquare - A*A)*coeff3;
            double coeff9 = A*(4*contactRadius*A + Math.PI*contactSquare - Math.PI*A*A);

            double dFdYM = (coeff9 - coeff7 + coeff8)/(4.*radiusSI*poissonFactor);

            //(dYM/da)indent

            double coeff10 = (Math.PI*contactRadius*A - 4*radiusSI*indentationSI + 2*contactRadius*A*coeff3);
            double coeff11 = coeff2*coeff10*coeff10*coeff10;
            double coeff12 = contactRadius*A*(8*contactRadius*A + Math.PI*coeff2);
            double coeff13 = coeff12 + 4*indentationSI*radiusSI*coeff2 + 2*contactRadius*A*coeff2*coeff3;
            double coeff14 = -32*Math.PI*radiusSquare*poissonFactor*adhesionWork*coeff13;

            double dYMda = coeff14/coeff11;
            double derivative = dFdYM + dFda/dYMda;

            return new double[] {derivative};
        }

        @Override
        public double value(double indentationSI, double... parameters) 
        {
            double modulus = Math.abs(parameters[0]);

            double adhesionWork = getAdhesionWork(adhesionForceSI, modulus);

            return value(indentationSI, adhesionWork, modulus);
        } 

        @Override
        public double value(double indentationSI, double adhesionWork, double modulus) 
        {
            modulus = Math.abs(modulus);
            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);

            return value(indentationSI, adhesionWork, modulus, fixedGripsContact);
        }   

        @Override
        public double value(double indentationSI, double adhesionWork, double modulus, double fixedGripsContact) 
        {
            modulus = Math.abs(modulus);
            double reducedModulus = modulus/poissonFactor;
            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus, fixedGripsContact);
            double force = getForceForContactRadius(contactRadius, reducedModulus, adhesionWork);

            return force;
        }   
    }


    //SUN SPARWDZONE
    public class SunFullFitHelperFunction implements ParametricUnivariateFunction
    {          
        @Override
        public double[] gradient(double indentationSI, double... parameters) 
        {    
            double modulus = Math.abs(parameters[0]);
            double adhesionWork = Math.abs(parameters[1]);

            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus, fixedGripsContact);       
            double reducedModulus = modulus/poissonFactor;
            double contactRadiusSquare = contactRadius*contactRadius;
            double contactRadiusCube = contactRadiusSquare*contactRadius;

            //calculate (dF/da)YM

            double coeff1 = 2*Math.PI*adhesionWork/reducedModulus;
            double coeff2 = contactRadiusSquare + A*A;
            double coeff3 = Math.asin((contactRadiusSquare - A*A)/(coeff2));
            double coeff5 = Math.sqrt(contactRadius*coeff1);
            double coeff6 = contactRadius*(4*contactRadius*A + Math.PI*contactRadiusSquare + Math.PI*A*A)/(2*coeff2);

            double dFda = reducedModulus*(cotAngle*(coeff6 + contactRadius*coeff3) - 3*coeff5);

            //(dF/dYM)a

            double coeff7 = 4*radiusSI*contactRadius*coeff5;
            double coeff8 = 2*A*(contactRadiusSquare - A*A)*coeff3;
            double coeff9 = A*(4*contactRadius*A + Math.PI*contactRadiusSquare - Math.PI*A*A);

            double dFdYMConstantContactRadius = (coeff9 - coeff7 + coeff8)/(4.*radiusSI*poissonFactor);

            //(dYM/da) constant indentation

            double coeff10 = (Math.PI*contactRadius*A - 4*radiusSI*indentationSI
                    + 2*contactRadius*A*coeff3);
            double coeff11 = coeff2*coeff10*coeff10*coeff10;
            double coeff12 = contactRadius*A*(8*contactRadius*A + Math.PI*coeff2);
            double coeff13 = coeff12 + 4*indentationSI*radiusSI*coeff2 +
                    2*contactRadius*A*coeff2*coeff3;
            double coeff14 = -32*Math.PI*radiusSquare*poissonFactor*adhesionWork*coeff13;

            double dYMda = coeff14/coeff11;


            //derivative (dAdhesionWork/da) constant indentation

            double coeff15 = reducedModulus*coeff10*coeff13;
            double coeff16 = 32*Math.PI*contactRadiusSquare*radiusSquare*coeff2;

            double dAdhesionWorkda = coeff15/coeff16;


            //derivative (dF/dAdhesionWork) constant contact radius

            double dFsdAdhesionWorkConstRadius = -Math.sqrt((2.*Math.PI*contactRadiusCube*reducedModulus)/adhesionWork);

            //derivatives with constant indentation

            double dFsdYMConstantIndent = dFdYMConstantContactRadius + dFda/dYMda;
            double dFdAdhesionWorkConstantIndent = dFsdAdhesionWorkConstRadius + dFda/dAdhesionWorkda;          

            return new double[] {dFsdYMConstantIndent, dFdAdhesionWorkConstantIndent};
        }

        @Override
        public double value(double indentationSI, double... parameters) 
        {
            double modulus = Math.abs(parameters[0]);
            double adhesionWork = Math.abs(parameters[1]);
            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);

            double reducedModulus = modulus/poissonFactor;
            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus, fixedGripsContact);
            double force = getForceForContactRadius(contactRadius, reducedModulus, adhesionWork);

            return force;
        }   
    }

    @Override
    protected SunForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, double adhesionForce)
    {      
        return fitYoungsModulus(forceIndentationYsSI, forceIndentationXsSI, 0, forceIndentationYsSI.length, adhesionForce, 1000);
    }

    @Override
    protected SunForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, int from, int to, double adhesionForce)
    {      
        return fitYoungsModulus(forceIndentationYsSI, forceIndentationXsSI, from, to, adhesionForce, 1000);
    }

    @Override
    public SunForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, double adhesionForce, double startYoungsModulus)
    {
        return fitYoungsModulus(forceIndentationYsSI, forceIndentationXsSI,0, forceIndentationYsSI.length, adhesionForce, startYoungsModulus);
    }

    @Override
    public SunForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, int from, int to, double adhesionForce, double startYoungsModulus)
    {
        SunYoungsModulusFitHelperFunction fitHelper = new SunYoungsModulusFitHelperFunction(adhesionForce);

        double[] parameters = fit(fitHelper, forceIndentationYsSI, forceIndentationXsSI, from, to, new LevenbergMarquardtOptimizer(), 200, new double[] {startYoungsModulus});

        double modulus = Math.abs(parameters[0]);
        double adhesionWork = getAdhesionWork(adhesionForce, modulus);

        SunForceIndentationFunction fittedFunction = new SunForceIndentationFunction(modulus, adhesionWork);
        return fittedFunction;
    }

    @Override
    protected SunForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceIndentationYsSI, double[] forceIndentationXsSI) 
    {
        return fitYoungsModulusAndWorkOfAdhesion(forceIndentationYsSI, forceIndentationXsSI, 0, forceIndentationYsSI.length, 10000, 1e-4);
    }

    @Override
    protected SunForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceIndentationYsSI, double[] forceIndentationXsSI, int from, int to) 
    {
        return fitYoungsModulusAndWorkOfAdhesion(forceIndentationYsSI, forceIndentationXsSI, from, to, 10000, 1e-4);
    }

    @Override
    protected SunForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceIndentationYsSI, double[] forceIndentationXsSI, double startModulusValue, double startsAdhesionWork) 
    {
        return fitYoungsModulusAndWorkOfAdhesion(forceIndentationYsSI, forceIndentationXsSI, 0, forceIndentationYsSI.length, startModulusValue, startsAdhesionWork);
    }

    @Override
    protected SunForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceIndentationYsSI, double[] forceIndentationXsSI, int from, int to, double startModulusValue, double startsAdhesionWork) 
    {
        CurveFitter<SunFullFitHelperFunction> fitter = new CurveFitter<>(new LevenbergMarquardtOptimizer());

        for(int i = from; i < to; i++)
        {
            double x = forceIndentationXsSI[i];
            double y = forceIndentationYsSI[i];

            fitter.addObservedPoint(x, y);
        }
        double[] parameters = fitter.fit(200, new SunFullFitHelperFunction(), new double[] {startModulusValue, startsAdhesionWork});

        double modulus = Math.abs(parameters[0]);
        double adhesionWork = Math.abs(parameters[1]);

        SunForceIndentationFunction fittedFunction = new SunForceIndentationFunction(modulus, adhesionWork);
        return fittedFunction;
    }

    public class SunFit extends JKRLikeContactFit <SunAkhremitchevWalkerIndentation>
    {
        private final AdhesiveAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceIndentationTransitionPoint;
        private final SunForceIndentationFunction fittedFunction;

        private final double modulusSI;
        private final double adhesionWorkSI;

        public SunFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
        {
            super(deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[][] positiveForceIndentationSIXY = annotatedForceIndentation.getPositiveForceIndentationSIXYView();

            this.fittedFunction =  isFitWorkOfAdhesionAlongsideYoungsModulus() ? fitYoungsModulusAndWorkOfAdhesion(positiveForceIndentationSIXY[1], positiveForceIndentationSIXY[0])
                    :fitYoungsModulus(positiveForceIndentationSIXY[1], positiveForceIndentationSIXY[0], annotatedForceIndentation.getAdhesion());

            this.modulusSI = fittedFunction.getYoungModulus();
            this.adhesionWorkSI = fittedFunction.getAdhesionWork();

            double[][] fittableForceIndentationXYView = annotatedForceIndentation.getFittableForceIndentationXYView();
            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            double[] forceIndentationTransitionPointData = regressionStrategy.getLastCoveredPoint(fittableForceIndentationXYView[1], fittableForceIndentationXYView[0], new UnivariateFunction() {

                @Override
                public double value(double val)
                {
                    double valSI = fittedFunction.value(1e-6*val);
                    return 1e9*valSI;
                }
            });    

            this.forceIndentationTransitionPoint = new Point1DData(forceIndentationTransitionPointData[0],forceIndentationTransitionPointData[1], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
        }

        @Override
        public SunAkhremitchevWalkerIndentation getContactModel() 
        {
            return SunAkhremitchevWalkerIndentation.this;
        }

        @Override
        protected SunForceIndentationFunction getFittedFunction()
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
            if(indentation < 0)
            {
                return Double.NaN;
            }

            double forceSI = 1e-9*force;
            double indentationSI = 1e-6*indentation;

            PointwiseModulusHelperFunction f = new PointwiseModulusHelperFunction(indentationSI, forceSI, adhesionWorkSI);

            double min = 0.25*modulusSI;
            double max = 4*modulusSI;

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

        @Override
        public Point1DData getForceIndentationTransitionPoint()
        {
            return forceIndentationTransitionPoint;
        }   
    }
}