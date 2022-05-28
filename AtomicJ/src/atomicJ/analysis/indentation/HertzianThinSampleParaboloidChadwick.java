
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
import atomicJ.analysis.ThinSampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.FittedLinearUnivariateFunction;


public class HertzianThinSampleParaboloidChadwick extends HertzianThinSampleModel
{
    private static final String NAME = "Sphere (Thin sample)";

    private final Sphere indenter;

    private final double a1;
    private final double a2;
    private final double a3;
    private final double a4;
    private final double a5;

    public HertzianThinSampleParaboloidChadwick(Sphere indenter, ThinSampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(sample, precontactModel);
        this.indenter = indenter;

        boolean bonded = sample.isBondedToSubstrate();

        double radius = indenter.getRadius();
        double v = sample.getPoissonRatio();

        double alpha = bonded ? -(1.2876 - 1.4678*v + 1.3442*v*v)/(1 - v): -0.347*(3 - 2*v)/(1 - v);
        double beta = bonded ? (0.6387 - 1.0277 *v + 1.5164*v*v)/(1 - v) : 0.056*(5 - 2*v)/(1 - v);

        double radSquare = Math.sqrt(radius);
        double pi = Math.PI;

        this.a1 = 4*Math.sqrt(radius)/(3*(1 - v*v));
        this.a2 = (-2*alpha*radSquare/(pi));
        this.a3 = 4*alpha*alpha*radius/(pi*pi);
        this.a4 = -8*radSquare*radius*(alpha*alpha*alpha + 4*pi*pi*beta/15)/(pi*pi*pi);
        this.a5 = 16*alpha*radius*radius*(alpha*alpha*alpha + 3*pi*pi*beta/5)/(pi*pi*pi*pi);
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
    public HertzianThinSampleSphereFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianThinSampleSphereFit fit = new HertzianThinSampleSphereFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    @Override
    protected double getForceModulusRatio(double indent, double h)
    {
        double indentRoot = Math.sqrt(indent);

        double coeff1 = a1*indent*indentRoot;
        double coeff2 = coeff1*a2*indentRoot/h;
        double coeff3 = coeff1*a3*indent/(h*h);
        double coeff4 = coeff1*a4*indent*indentRoot/(h*h*h);
        double coeff5 = coeff1*a5*indent*indent/(h*h*h*h);
        double ratio = coeff1 + coeff2 + coeff3 + coeff4 + coeff5;

        return ratio;
    }


    public class HertzianThinSampleForceIndentationFunction implements UnivariateFunction
    {
        private final double modulus;
        private final double thickness;

        public HertzianThinSampleForceIndentationFunction(double modulus, double thickness)
        {
            this.modulus = modulus;
            this.thickness = thickness;
        }

        @Override
        public double value(double indent) 
        {
            double forceModulusRatio = getForceModulusRatio(indent, thickness);
            double force = forceModulusRatio*modulus;
            return force;
        }        
    }

    public class HertzianThinSampleSphereFit extends HertzianFit<HertzianThinSampleParaboloidChadwick>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation; 

        private final Point1DData forceIndentationTransitionPoint;

        private final HertzianThinSampleForceIndentationFunction fittedFunction;

        private final double thickness;
        private final double modulus;

        public HertzianThinSampleSphereFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianThinSampleParaboloidChadwick.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues, recordingPoint);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fittedTransformedFunction = regressionStrategy.getFitFunction(fittableForceValues, convertedFittableIndentationValues, 1, false);
            this.modulus = fittedTransformedFunction.getCoefficient(Double.valueOf(1));
            this.thickness = getSampleThickness(recordingPoint);

            this.fittedFunction = new HertzianThinSampleForceIndentationFunction(modulus, thickness);

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
            double forceModulusRatio = getForceModulusRatio(indentation, thickness);
            double modulus = force/forceModulusRatio;
            return modulus;
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint() 
        {
            return forceIndentationTransitionPoint;
        }

        @Override
        protected HertzianThinSampleForceIndentationFunction getFittedFunction() 
        {
            return fittedFunction;
        }     
    }
}
