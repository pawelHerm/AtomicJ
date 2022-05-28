
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

import org.apache.commons.math3.analysis.DifferentiableUnivariateFunction;
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


@SuppressWarnings("deprecation")
public class MaugisIndentation extends JKRLikeContact
{
    private static final double TOLERANCE = 1e-15;
    private static final String NAME = "Sphere (Maugis)";

    private final Sphere indenter;

    private final double poissonFactor;
    private final double radiusSI;
    private final double radiusSquare;

    public MaugisIndentation(Sphere indenter, SampleModel sample, PrecontactInteractionsModel precontactModel, AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod)
    {
        super(sample, precontactModel, adhesiveEnergyEstimationMethod);
        this.indenter = indenter;

        double v = sample.getPoissonRatio();
        this.poissonFactor = 1-v*v;
        this.radiusSI = 1e-6*indenter.getRadius();
        this.radiusSquare = radiusSI*radiusSI;
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

    //SPRAWDZONE
    public double getIndentation(double contactRadius, double modulusSI, double adhesionWorkSI)
    {                
        double reducedModulus = modulusSI/poissonFactor;
        double coeff1 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));
        double indentation = 0.5*contactRadius*coeff1 - Math.sqrt(2*Math.PI*contactRadius*adhesionWorkSI/reducedModulus);
        return indentation;
    }

    @Override
    public double getPullOffIndentation(double modulusSI, double adhesionWorkSI, double springConstant)    
    {
        double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWorkSI, modulusSI);
        double contactRadius = getPullOffContactRadius(adhesionWorkSI, modulusSI, springConstant, fixedGripsContact);

        double indentation = getIndentation(contactRadius, modulusSI, adhesionWorkSI);

        return indentation;
    }

    public double getPullOffContactRadius(double adhesionWork, double modulusSI, double springConstant, double pullOffFixedGrips)
    {        
        UnivariateFunction f = new PullOffHelperFunction(modulusSI, adhesionWork, springConstant);

        double min = pullOffFixedGrips;
        double max = radiusSI;

        UnivariateSolver solver = new BrentSolver(1e-12);
        double contactRadius = solver.solve(1000, f, min, max);

        return contactRadius;
    }

    //SPRAWDZONE
    public double getAdhesionWork(double adhesionForceSI, double modulusSI)
    {    
        UnivariateFunction f = new AdhesionWorkHelperFunction(adhesionForceSI, modulusSI);

        //in the Maugis model adhesion Force is always lower for a particular adhesion work - it increases with Young's modulus at reaches the value for
        //JKR for infinitely stiff sample
        double min = 2*adhesionForceSI/(3.*radiusSI*Math.PI);
        double max = 2*min;

        UnivariateSolver solver = new BrentSolver();
        double adhesionWork = solver.solve(200, f, min, max);

        return adhesionWork;
    }

    public double getFixedLoadPullOffContactRadius(double adhesionWork, double modulusSI, double fixedGripsContact)
    {        
        DFda f = new DFda(modulusSI, adhesionWork);

        double min = fixedGripsContact;
        double max = 0.999*radiusSI;

        UnivariateSolver solver = new BrentSolver(1e-12);
        double contactRadius = solver.solve(100, f, min, max);

        return contactRadius;
    }


    @Override
    public double getFixedGripsPullOffContactRadius(double adhesionWork, double modulus)
    {
        UnivariateObjectiveFunction f = new UnivariateObjectiveFunction(new IndentationContactRadiusFunction(modulus, adhesionWork));

        UnivariateOptimizer optimizer = new BrentOptimizer(1e-12, 1e-15);
        MaxEval maxEval = new MaxEval(200);

        double pullOffContactRadius = optimizer.optimize(maxEval, f, GoalType.MINIMIZE, new SearchInterval(0, radiusSI)).getPoint();

        return pullOffContactRadius;
    }

    public double getFixedGripsPullOffJKRContactRadius(double adhesionWork, double modulus)
    {
        double reducedModulus = modulus/poissonFactor;
        double contactRadius = Math.cbrt(Math.PI*adhesionWork*radiusSI*radiusSI/(reducedModulus*8.));

        return contactRadius;
    }   

    //we fit a linear function with an intercept - thanks to this, the objective function is usually unimodal

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs, double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        MaugisForceIndentationFunction postcontactFit = fitYoungsModulusAndWorkOfAdhesion(postcontactForceSeparationYs, postcontactForceSeparationXs);
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionValue(postcontactForceSeparationYs, postcontactForceSeparationXs, postcontactFit);      
        return objectiveFunctionMinimum;
    }  

    public class IndentationContactRadiusFunction implements UnivariateFunction
    {
        private final double reducedModulus;
        private final double coeff1;

        public IndentationContactRadiusFunction(double modulusSI, double adhesionWork)
        {
            this.reducedModulus = modulusSI/poissonFactor;
            this.coeff1 = 2*Math.PI*adhesionWork/reducedModulus;
        }

        @Override
        public double value(double contactRadius)
        {         
            double coeff2 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));
            double indentationSI = 0.5*contactRadius*coeff2 -  Math.sqrt(contactRadius*coeff1);
            return indentationSI;      
        }      
    }

    @Override
    public MaugisFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        MaugisFit fit = new MaugisFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    //SPRAWDZONE
    public class MaugisForceIndentationFunction extends AbstractFittedUnivariateFunction implements AdhesiveForceIndentationFunction, FittedUnivariateFunction, ParametrizedUnivariateFunction
    {
        private final double modulusSI;
        private final double reducedModulus;
        private final double adhesionWork;
        private final double fixedGripsContact;
        private final double coeff1;

        public MaugisForceIndentationFunction(double modulus, double adhesionWork)
        {
            this.modulusSI = modulus;
            this.reducedModulus = modulus/poissonFactor;
            this.adhesionWork = adhesionWork;

            this.coeff1 = 2*Math.PI*adhesionWork*poissonFactor/modulusSI;
            this.fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);
        }

        @Override
        public double value(double indentationSI) 
        {          
            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulusSI, fixedGripsContact);

            return forceForContactRadius(contactRadius);
        }

        private double forceForContactRadius(double contactRadius)
        {
            double coeff2 = Math.sqrt(coeff1 * contactRadius);
            double coeff3 = (radiusSI*radiusSI + contactRadius*contactRadius)/(4*contactRadius);
            double coeff4 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));

            double force = 2*reducedModulus*contactRadius*(coeff3*coeff4 - 0.5*radiusSI - coeff2);

            return force;
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

            return forceForContactRadius(contactRadius);
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

    //SPRAWDZONE
    public double getContactRadius(double indentationSI, double adhesionWork, double modulusSI)
    {
        //this is the upper limit on the contact radius - as if the contact were already at the level of the sample surface
        //use as an upper bound for solution by the Brent - Dreker algorithm

        ContactRadiusHelperFunction f = new ContactRadiusHelperFunction(indentationSI, adhesionWork, modulusSI);

        double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulusSI);

        if(f.value(fixedGripsContact)>= -TOLERANCE)
        {
            return fixedGripsContact;
        }

        double min = fixedGripsContact;
        double max = radiusSI;

        UnivariateSolver solver = new BrentSolver(1e-12);
        double contactRadius = solver.solve(1000, f, min, max);           

        return contactRadius; 
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

        double min = fixedGripsContact;
        double max = radiusSI;

        UnivariateSolver solver = new BrentSolver(1e-12);
        double contactRadius = solver.solve(1000, f, min, max);           

        return contactRadius;       
    }

    //sprawdzony
    public class ContactRadiusHelperFunction implements DifferentiableUnivariateFunction
    {
        private final double indentationSI;            
        private final double coeff1;

        public ContactRadiusHelperFunction(double indentationSI, double adhesionWork, double modulusSI)
        {
            this.indentationSI = indentationSI;
            this.coeff1 = 2*Math.PI*adhesionWork*poissonFactor/modulusSI;
        }

        @Override
        public double value(double contactRadius) 
        {       
            double coeff2 = 0.5*contactRadius*Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));
            double val = coeff2 - Math.sqrt(coeff1*contactRadius) - indentationSI;

            return val;
        }

        @Override
        public UnivariateFunction derivative() 
        {
            UnivariateFunction f = new UnivariateFunction() {

                @Override
                public double value(double contactRadius) 
                {
                    //calculate (dIndent/da)YM
                    double coeff3 = contactRadius*contactRadius - radiusSI*radiusSI;
                    double coeff5 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));

                    double coeff7 = -0.5*Math.sqrt(coeff1/contactRadius);
                    double coeff8 = -contactRadius*radiusSI/coeff3;
                    double dIndentda = coeff7 + coeff8 + 0.5*coeff5;
                    return dIndentda;
                }
            };
            return f;
        } 
    }

    public AdhesionWorkHelperFunction getAdhesionWorkHerlperFunction(double adhesionForceSI, double modulusSI)
    {
        return new AdhesionWorkHelperFunction(adhesionForceSI, modulusSI);
    }

    //sprawdzony
    public class AdhesionWorkHelperFunction implements UnivariateFunction
    {
        private final double adhesionForceSI;  
        private final double modulusSI;
        private final double reducedModulus;
        private final double coeff0;

        public AdhesionWorkHelperFunction(double adhesionForceSI, double modulusSI)
        {
            this.adhesionForceSI = adhesionForceSI;
            this.modulusSI = modulusSI;
            this.reducedModulus = modulusSI/poissonFactor;
            this.coeff0 = 2*Math.PI/reducedModulus;
        }

        @Override
        public double value(double adhesionWork) 
        {                     
            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulusSI);
            double contactRadius = getFixedLoadPullOffContactRadius(adhesionWork, modulusSI, fixedGripsContact);

            double coeff2 = Math.sqrt(coeff0 * contactRadius*adhesionWork);
            double coeff3 = (radiusSI*radiusSI + contactRadius*contactRadius)/(4*contactRadius);
            double coeff4 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));

            double force = 2*reducedModulus*contactRadius*(coeff3*coeff4 - 0.5*radiusSI - coeff2);

            double val = force + adhesionForceSI;

            return val;
        } 
    }

    //sprawdzony
    //this class is used by Brent method to find the contact radius for which df/da = 0, i.e.
    //the contact radius, for which the adhesionForce is the lowest
    private class PullOffHelperFunction implements UnivariateFunction
    {
        private final double reducedModulus;
        private final double springConstant;
        private final double coeff0;

        public PullOffHelperFunction(double modulus, double adhesionWork, double springConstant)
        {

            this.reducedModulus = modulus/poissonFactor;
            this.springConstant = springConstant;
            this.coeff0 = 2.*Math.PI*adhesionWork/reducedModulus;
        }

        @Override
        public double value(double contactRadius)
        {

            double contactSquare = contactRadius*contactRadius;

            //calculate (dF/da)YM
            double coeff2 = Math.sqrt(contactRadius*coeff0);
            double coeff3 = contactSquare - radiusSquare;
            double coeff4 = coeff2*coeff3;
            double coeff5 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));
            double coeff6 = 3.*coeff0*radiusSquare + coeff4*coeff5 - 3.*contactSquare*coeff0 - 2.*contactRadius*coeff2*radiusSI;

            double dFda = contactRadius*reducedModulus*coeff6/coeff4;

            //calculate (dIndent/da)YM

            double coeff7 = -0.5*Math.sqrt(coeff0/contactRadius);
            double coeff8 = -contactRadius*radiusSI/coeff3;
            double dIndentda = coeff7 + coeff8 + 0.5*coeff5;

            double value = dFda + springConstant*dIndentda;

            return value;
        }        
    }

    //sprawdzony
    //used by UnivariateSolver a contact radius for which the derivative dF/da equals zero
    //i.e. to find the contact radius at pull off for fixed loads
    private class DFda implements UnivariateFunction
    {
        private final double reducedModulus;
        private final double coeff0;
        private final double coeff1;

        public DFda(double modulus, double adhesionWork)
        {
            this.reducedModulus = modulus/poissonFactor;
            this.coeff0 = 2.*Math.PI*adhesionWork/reducedModulus;
            this.coeff1 = 3.*coeff0*radiusSquare;
        }

        @Override
        public double value(double contactRadius)
        {
            double contactSquare = contactRadius*contactRadius;

            //calculate (dF/da)YM
            double coeff2 = Math.sqrt(contactRadius*coeff0);
            double coeff3 = contactSquare - radiusSquare;
            double coeff4 = coeff2*coeff3;
            double coeff5 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));
            double coeff6 = coeff1 + coeff4*coeff5 - 3.*contactSquare*coeff0 - 2.*contactRadius*coeff2*radiusSI;

            double dFda = contactRadius*reducedModulus*coeff6/coeff4;

            return dFda;
        }        
    }

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
            double force = forceForContactRadius(reducedModulus, contactRadius);

            double val = (force - this.force);

            return val;
        }     


        private double forceForContactRadius(double reducedModulus, double contactRadius)
        {
            double coeff1 = 2*Math.PI*adhesionWork/reducedModulus;

            double coeff2 = Math.sqrt(coeff1 * contactRadius);
            double coeff3 = (radiusSI*radiusSI + contactRadius*contactRadius)/(4*contactRadius);
            double coeff4 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));

            double force = 2*reducedModulus*contactRadius*(coeff3*coeff4 - 0.5*radiusSI - coeff2);

            return force;
        }
    }

    //SPARWDZONE
    public class MaugisYoungsModulusFitHelperFunction implements JKRLikeFitHelper
    {
        private final double adhesionForceSI;

        public MaugisYoungsModulusFitHelperFunction(double adhesionForceSI)
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
            double coeff1 = 2.*Math.PI*adhesionWork/reducedModulus;
            double coeff2 = Math.sqrt(contactRadius*coeff1);
            double coeff3 = contactSquare - radiusSquare;
            double coeff4 = coeff2*coeff3;
            double coeff5 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));
            double coeff6 = 3.*coeff1*radiusSquare + coeff4*coeff5 - 3.*contactSquare*coeff1 - 2.*contactRadius*coeff2*radiusSI;

            double dFda = contactRadius*reducedModulus*coeff6/coeff4;


            //(dF/dYM)a

            double alpha = 2*Math.PI*contactRadius*adhesionWork;
            double beta = Math.sqrt(alpha/reducedModulus);
            double coeff7 = contactRadius*alpha/(modulus*beta);
            double coeff8 = -0.5*radiusSI - beta + (contactSquare + radiusSquare)*coeff5/(4*contactRadius);
            double dFdYM = coeff7 + 2*contactRadius*coeff8/poissonFactor;

            //(dYM/da)indent
            double coeff9 = (2*indentationSI - contactRadius*coeff5);
            double coeff9Squared = coeff9*coeff9;
            double coeff10 = -2.*contactRadius*radiusSI/coeff3 + coeff5;
            double coeff11 = 16*contactRadius*adhesionWork*Math.PI*poissonFactor*coeff10/(coeff9*coeff9Squared);
            double coeff12 = 8*Math.PI*adhesionWork*poissonFactor/coeff9Squared;

            double dYMda = coeff11 + coeff12;
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
            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);          
            return value(indentationSI, adhesionWork, modulus, fixedGripsContact);
        }   

        @Override
        public double value(double indentationSI, double adhesionWork, double modulus, double fixedGripsContact) 
        {
            modulus = Math.abs(modulus);

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus, fixedGripsContact);

            double reducedModulus = modulus/poissonFactor;

            double coeff1 = 2*Math.PI*adhesionWork/reducedModulus;

            double coeff2 = Math.sqrt(coeff1 * contactRadius);
            double coeff3 = (radiusSquare + contactRadius*contactRadius)/(4*contactRadius);
            double coeff4 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));

            double force = 2*reducedModulus*contactRadius*(coeff3*coeff4 - 0.5*radiusSI - coeff2);

            return force;
        }   
    }

    //SPARWDZONE
    public class MaugisFullFitHelperFunction implements ParametricUnivariateFunction
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
            double coeff1 = 2.*Math.PI*adhesionWork/reducedModulus;
            double coeff2 = Math.sqrt(contactRadius*coeff1);
            double coeff3 = contactRadiusSquare - radiusSquare;
            double coeff4 = coeff2*coeff3;
            double coeff5 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));
            double coeff6 = 3.*coeff1*radiusSquare + coeff4*coeff5 - 3.*contactRadiusSquare*coeff1 - 2.*contactRadius*coeff2*radiusSI;

            double dFda = contactRadius*reducedModulus*coeff6/coeff4;

            //(dF/dYM)a

            double alpha = 2*Math.PI*contactRadius*adhesionWork;
            double beta = Math.sqrt(alpha/reducedModulus);
            double coeff7 = contactRadius*alpha/(modulus*beta);
            double coeff8 = -0.5*radiusSI - beta + (contactRadiusSquare + radiusSquare)*coeff5/(4*contactRadius);
            double dFdYM = coeff7 + 2*contactRadius*coeff8/poissonFactor;

            //(dYM/da)indent
            double coeff9 = (2*indentationSI - contactRadius*coeff5);
            double coeff9Squared = coeff9*coeff9;
            double coeff10 = -2.*contactRadius*radiusSI/coeff3 + coeff5;
            double coeff11 = 16*contactRadius*adhesionWork*Math.PI*poissonFactor*coeff10/(coeff9*coeff9Squared);
            double coeff12 = 8*Math.PI*adhesionWork*poissonFactor/coeff9Squared;

            double dYMda = coeff11 + coeff12;

            //derivative (dAdhesionWork/da) constant indentation

            double coeff13 = -2*radiusSquare*indentationSI + 
                    2*contactRadiusSquare*(indentationSI - 2*radiusSI) + contactRadius*coeff3*coeff5;


            double coeff14 = reducedModulus*coeff9*coeff13;
            double dAdhesionWorkda = coeff14/(-8*Math.PI*contactRadiusSquare*coeff3);

            //derivative (dF/dAdhesionWork) constant contact radius

            double dFsdAdhesionWorkConstRadius = -Math.sqrt((2.*Math.PI*contactRadiusCube*reducedModulus)/adhesionWork);


            //derivatives with constant indentation
            double dFsdYMConstantIndent = dFdYM + dFda/dYMda;
            double dFdAdhesionWorkConstantIndent = dFsdAdhesionWorkConstRadius + dFda/dAdhesionWorkda;          

            return new double[] {dFsdYMConstantIndent, dFdAdhesionWorkConstantIndent};
        }  

        @Override
        public double value(double indentationSI, double... parameters) 
        {
            double modulus = Math.abs(parameters[0]);
            double adhesionWork = Math.abs(parameters[1]);
            double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);

            double contactRadius = getContactRadius(indentationSI, adhesionWork, modulus, fixedGripsContact);

            double reducedModulus = modulus/poissonFactor;

            double coeff1 = 2*Math.PI*adhesionWork/reducedModulus;

            double coeff2 = Math.sqrt(coeff1 * contactRadius);
            double coeff3 = (radiusSquare + contactRadius*contactRadius)/(4*contactRadius);
            double coeff4 = Math.log((radiusSI + contactRadius)/(radiusSI - contactRadius));

            double force = 2*reducedModulus*contactRadius*(coeff3*coeff4 - 0.5*radiusSI - coeff2);

            return force;
        }   
    }

    @Override
    public MaugisForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, double adhesionForce)
    {      
        return fitYoungsModulus(forceIndentationYsSI, forceIndentationXsSI, 0, forceIndentationYsSI.length, adhesionForce, 10000);
    }

    @Override
    public MaugisForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, int from, int to, double adhesionForce)
    {      
        return fitYoungsModulus(forceIndentationYsSI, forceIndentationXsSI, from, to, adhesionForce, 10000);
    }

    @Override
    public MaugisForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, double adhesionForce, double startValue)
    {
        return fitYoungsModulus(forceIndentationYsSI, forceIndentationXsSI, 0, forceIndentationYsSI.length, adhesionForce, startValue);
    }

    @Override
    public MaugisForceIndentationFunction fitYoungsModulus(double[] forceIndentationYsSI, double[] forceIndentationXsSI, int from, int to, double adhesionForce, double startValue)
    {
        MaugisYoungsModulusFitHelperFunction fitHelper = new MaugisYoungsModulusFitHelperFunction(adhesionForce);

        double[] parameters = fit(fitHelper, forceIndentationYsSI, forceIndentationXsSI, from, to, new LevenbergMarquardtOptimizer(), 400, new double[] {startValue});

        double modulus = Math.abs(parameters[0]);
        double adhesionWork = getAdhesionWork(adhesionForce, modulus);

        MaugisForceIndentationFunction fittedFunction = new MaugisForceIndentationFunction(modulus, adhesionWork);
        return fittedFunction;
    }

    public class MaugisFit extends JKRLikeContactFit <MaugisIndentation>
    {
        private final AdhesiveAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceIndentationTransitionPoint;
        private final MaugisForceIndentationFunction fittedFunction;

        private final double modulusSI;
        private final double adhesionWorkSI;

        public MaugisFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
        {
            super(deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[][] positiveForceIndentationSIXY = annotatedForceIndentation.getPositiveForceIndentationSIXYView();

            this.fittedFunction =  isFitWorkOfAdhesionAlongsideYoungsModulus() ? fitYoungsModulusAndWorkOfAdhesion(positiveForceIndentationSIXY[1], positiveForceIndentationSIXY[0])
                    :fitYoungsModulus(positiveForceIndentationSIXY[1], positiveForceIndentationSIXY[0], annotatedForceIndentation.getAdhesion());
            this.modulusSI = fittedFunction.getYoungModulus();
            this.adhesionWorkSI = fittedFunction.getAdhesionWork();

            double[][] fittableForceIndentationXY = annotatedForceIndentation.getFittableForceIndentationXYView();
            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            double[] forceIndentationTransitionPointData = regressionStrategy.getLastCoveredPoint(fittableForceIndentationXY[1], fittableForceIndentationXY[0], new UnivariateFunction() {

                @Override
                public double value(double val)
                {
                    double valSI = fittedFunction.value(1e-6*val);
                    return 1e9*valSI;
                }
            });
            this.forceIndentationTransitionPoint =  new Point1DData(forceIndentationTransitionPointData[0], forceIndentationTransitionPointData[1], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);        
        }

        @Override
        public MaugisIndentation getContactModel() 
        {
            return MaugisIndentation.this;
        }

        @Override
        protected MaugisForceIndentationFunction getFittedFunction()
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

    @Override
    protected MaugisForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] ys, double[] xs) 
    {
        return fitYoungsModulusAndWorkOfAdhesion(ys, xs, 0, ys.length, 10000, 1e-4);
    }

    @Override
    protected MaugisForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] ys, double[] xs, int from, int to) 
    {
        return fitYoungsModulusAndWorkOfAdhesion(ys, xs, from, to, 10000, 1e-4);
    }

    @Override
    protected MaugisForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] ys, double[] xs, double startModulusValue, double startAdhesionWork)
    {
        return fitYoungsModulusAndWorkOfAdhesion(ys, xs, 0, ys.length, startModulusValue, startAdhesionWork);
    }

    @Override
    protected MaugisForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] ys, double[] xs, int from, int to, double startModulusValue, double startAdhesionWork)
    {
        CurveFitter<MaugisFullFitHelperFunction> fitter = new CurveFitter<>(new LevenbergMarquardtOptimizer());

        for(int i = from; i < to; i++)
        {
            double x = xs[i];
            double y = ys[i];

            fitter.addObservedPoint(x, y);
        }

        double[] parameters = fitter.fit(200, new MaugisFullFitHelperFunction(), new double[] {startModulusValue, startAdhesionWork});

        double modulus = Math.abs(parameters[0]);
        double adhesionWork = Math.abs(parameters[1]);

        MaugisForceIndentationFunction fittedFunction = new MaugisForceIndentationFunction(modulus, adhesionWork);
        return fittedFunction;    
    }
}