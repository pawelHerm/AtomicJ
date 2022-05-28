
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

import atomicJ.analysis.ForceContourStackFunction;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.FittedLinearUnivariateFunction;


public class HertzianBluntConeParaboloidalCapSmooth extends HertzianContactModel
{
    private static final double TOLERANCE = 1e-10;

    private static final String NAME = "Blunt cone";

    private final BluntConeWithParaboloidalCap indenter;
    private final SampleModel sample;

    private final double poissonFactor;
    private final double cotTheta;
    private final double R;
    private final double sqrtR;
    private final double b;

    private final double modelSwitch;

    public HertzianBluntConeParaboloidalCapSmooth(BluntConeWithParaboloidalCap indenter, SampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(precontactModel);
        this.indenter = indenter;
        this.sample = sample;

        double v = sample.getPoissonRatio();
        poissonFactor = 1/(1 - v*v);

        double angle = indenter.getHalfAngle();
        cotTheta = 1./Math.tan(angle);

        this.R = indenter.getApexRadius();
        this.sqrtR = Math.sqrt(R);
        this.b = indenter.getTransitionRadius();

        this.modelSwitch = b*b/R;
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
    public BluntConeWithParaboloidalCap getIndenter()
    {
        return indenter;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }

    @Override
    public HertzianBluntConeFit getModelFit(Channel1DData deflectionCurveBranch, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianBluntConeFit fit = new HertzianBluntConeFit(deflectionCurveBranch, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    private double getCoefficient(double indent)
    {
        double coefficient = 0;

        if(indent > modelSwitch)
        {  
            double a = getApproximateContactRadiusForConicalPart(indent);

            coefficient = 2*poissonFactor*(a*indent - 0.5*a*a*cotTheta*(0.5*Math.PI - Math.asin(b/a)) - a*a*a/(3*R) + Math.sqrt(a*a - b*b)*(0.5*b*cotTheta + (a*a - b*b)/(3*R)));                
        }
        else
        {
            coefficient = 4.*poissonFactor*sqrtR*Math.sqrt(indent)*indent/3.;
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
            indentationValuesConverted[i] = getCoefficient(indent);
        }

        return indentationValuesConverted;       
    } 

    private double[] transformIndentationData(double[] indentationValues)
    {        
        int n = indentationValues.length;

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            indentationValues[i] = getCoefficient(indent);
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
            contactRadius = indent <= modelSwitch ? Math.sqrt(R*indent) : getApproximateContactRadiusForConicalPart(indent);          
        }

        return contactRadius; 
    }

    private double getApproximateContactRadiusForConicalPart(double indent)
    {
        double contactRadius = 0;
        double Rindt = R*indent;

        if(Rindt <= 2.5*b*b)
        {
            double var = Math.sqrt(Rindt - b*b)/b;
            double coeff = 0.712042342*var + 0.0998906457*var*var - 0.034964139*var*var*var;
            contactRadius = b*(coeff*coeff + 1);
        }

        else
        {
            double var = b*b/Rindt;
            double coeff = Math.sqrt(2/Math.PI) - 0.421927566722*var-0.14427923695 *var*var;
            contactRadius = Rindt*coeff*coeff/b + b;
        }

        return contactRadius;
    }

    public class BluntConeForceIndentationFunction implements UnivariateFunction
    {
        private final double modulus;

        public BluntConeForceIndentationFunction(double modulus)
        {
            this.modulus = modulus;
        }

        @Override
        public double value(double indent) 
        {
            double coefficient = getCoefficient(indent);

            double force = modulus*coefficient;
            return force;
        }

        public double getYoungsModulus()
        {
            return modulus;
        }
    }

    public class HertzianBluntConeFit extends HertzianFit<HertzianBluntConeParaboloidalCapSmooth>
    {
        private final BluntConeForceIndentationFunction fittedFunction;
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation; 
        private final Point1DData forceIndentationTransitionPoint;    
        private final double modulus;


        public HertzianBluntConeFit(Channel1DData deflectionCurveBranch, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {   
            super(HertzianBluntConeParaboloidalCapSmooth.this, deflectionCurveBranch, deflectionContactPoint, recordingPoint, processingSettings);
            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fit = regressionStrategy.getFitFunctionForSingleExponent(fittableForceValues, convertedFittableIndentationValues, 1);
            this.modulus = fit.getCoefficient(Double.valueOf(1));

            this.fittedFunction = new BluntConeForceIndentationFunction(modulus);

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
            double denominator= getCoefficient(indentation);       

            double modulus = force/denominator;
            return modulus;       
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint() 
        {
            return forceIndentationTransitionPoint;
        }

        @Override
        protected BluntConeForceIndentationFunction getFittedFunction() 
        {
            return fittedFunction;
        }      
    }
}



