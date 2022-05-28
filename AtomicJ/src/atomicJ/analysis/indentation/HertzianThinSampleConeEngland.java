
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

public class HertzianThinSampleConeEngland extends HertzianThinSampleModel
{
    private static final String NAME = "Cone (Thin sample)";

    private final Cone indenter;

    private final double f2;

    private static final double K0 = -0.74330250045001508930387727610390350332;
    private static final double K1 = 0.50341521648589239640822922341891830862;
    private static final double K2 = -0.21837266938754440666356068163630537278;

    public HertzianThinSampleConeEngland(Cone indenter, ThinSampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(sample, precontactModel);
        this.indenter = indenter;

        double angle = indenter.getHalfAngle();
        double tanAngle = Math.tan(angle);

        this.f2 = tanAngle/Math.PI;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    public double getTipHalfAngle()
    {
        return this.indenter.getHalfAngle();
    }

    @Override
    public Cone getIndenter()
    {
        return indenter;
    }

    @Override
    public HertzianThinSampleConeFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianThinSampleConeFit fit = new HertzianThinSampleConeFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    @Override
    protected double getForceModulusRatio(double indent, double h)
    {
        double coeffA = f2*indent/h;

        double thinFactor = 1 - K0*coeffA + 2*K0*K0*coeffA*coeffA-(5*K0*K0*K0+2*K1)*coeffA*coeffA*coeffA + 2*(7*K0*K0*K0*K0+6*K0*K1)*coeffA*coeffA*coeffA*coeffA;
        double a0 = 2*f2*indent*thinFactor;

        double v = getPoissonRatio();

        double ratio = a0*a0/(f2*2*(1-v*v));

        return ratio;
    }

    public class HertzianThinSampleConeForceIndentationFunction implements UnivariateFunction
    {
        private final double modulus;
        private final double thickness;

        public HertzianThinSampleConeForceIndentationFunction(double modulus, double thickness)
        {
            this.modulus = modulus;
            this.thickness = thickness;
        }

        @Override
        public double value(double indent) 
        {
            double ratio = getForceModulusRatio(indent, thickness);
            double force = ratio*modulus;
            return force;
        }
    }

    public class HertzianThinSampleConeFit extends HertzianFit<HertzianThinSampleConeEngland>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceIndentationTransitionPoint;

        private final double thickness;

        private final HertzianThinSampleConeForceIndentationFunction fittedFunction;
        private final double modulus;

        public HertzianThinSampleConeFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianThinSampleConeEngland.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);
            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues, recordingPoint);

            this.thickness = getSampleThickness(recordingPoint); 

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fittedTransformedFunction = regressionStrategy.getFitFunction(fittableForceValues, convertedFittableIndentationValues, 1, false);
            this.modulus = fittedTransformedFunction.getCoefficient(Double.valueOf(1));
            this.fittedFunction = new HertzianThinSampleConeForceIndentationFunction(modulus, thickness);

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
            double ratio = getForceModulusRatio(indentation, thickness);
            double modulus = force/ratio;
            return modulus;
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint() 
        {
            return forceIndentationTransitionPoint;
        }

        @Override
        protected HertzianThinSampleConeForceIndentationFunction getFittedFunction() 
        {       
            return fittedFunction;
        }
    }
}
