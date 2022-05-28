
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
import atomicJ.utilities.MathUtilities;


public class HertzianThinSampleConeLebedevWithoutAdhesion extends HertzianThinSampleModel
{
    private static final double TOLERANCE = 1e-10;
    private static final String NAME = "Cone (Lebedev-Chebyshev)";

    private final UnivariateFunction reducedLoadReducedIndentationDepthFunction;
    private final Cone indenter;

    private final double tipHalfAngleTan;
    private final double reducedLoadCoeff;

    public HertzianThinSampleConeLebedevWithoutAdhesion(Cone indenter, ThinSampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(sample, precontactModel);
        this.indenter = indenter;

        this.tipHalfAngleTan = Math.tan(indenter.getHalfAngle());

        double v = sample.getPoissonRatio();

        this.reducedLoadCoeff = 1/(tipHalfAngleTan*(1-v*v));

        if(isSampleBondedToSubstrate())
        {
            if(MathUtilities.equalWithinTolerance(v, 0.5, TOLERANCE))
            {
                reducedLoadReducedIndentationDepthFunction = ReducedLoadVsReducedIndentationApproximationThinBondedIncompressibleLayer.CONE;
            }        
            else 
            {
                UnivariateFunction contactRadiusFunction = TauLambdaDependenceBondedSample.DEGREE_ONE.getFunction(v);
                UnivariateFunction xi0Funct = XiApproximationBondedLayer.XI_ZERO.getFunction(v);
                UnivariateFunction xi1Funct = XiApproximationBondedLayer.XI_ONE.getFunction(v);
                reducedLoadReducedIndentationDepthFunction =  new ReducedLoadVsReducedIndentationCone(contactRadiusFunction, xi0Funct, xi1Funct);
            }        
        }
        else
        {
            reducedLoadReducedIndentationDepthFunction = ReducedLoadVsReducedIndentationApproximationThinLooseLayer.CONE;
        }
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
        double reducedIndentation = tipHalfAngleTan*indent/h;
        double ratio = Double.NaN;
        double reducedLoad = reducedLoadReducedIndentationDepthFunction.value(reducedIndentation);

        ratio = reducedLoadCoeff*reducedLoad*h*h;

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

    public class HertzianThinSampleConeFit extends HertzianFit<HertzianThinSampleConeLebedevWithoutAdhesion>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation; 

        private final Point1DData forceIndentationTransitionPoint;

        private final HertzianThinSampleForceIndentationFunction fittedFunction;

        private final double thickness;
        private final double modulus;

        public HertzianThinSampleConeFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianThinSampleConeLebedevWithoutAdhesion.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues, recordingPoint);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fittedTransformedFunction = regressionStrategy.getFitFunction(fittableForceValues, convertedFittableIndentationValues, 1,false);
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

    private static class ReducedLoadVsReducedIndentationCone implements UnivariateFunction
    {
        private final UnivariateFunction contactRadiusFunction;
        private final UnivariateFunction xi0Funct;
        private final UnivariateFunction xi1Funct;

        private ReducedLoadVsReducedIndentationCone(UnivariateFunction contactRadiusFunction, UnivariateFunction xi0Funct, UnivariateFunction xi1Funct)
        {
            this.contactRadiusFunction = contactRadiusFunction;
            this.xi0Funct = xi0Funct;
            this.xi1Funct = xi1Funct;
        }

        @Override
        public double value(double reducedIndent) 
        {
            double reducedContact = contactRadiusFunction.value(reducedIndent);

            double xi0 = xi0Funct.value(reducedContact);
            double xi1 = xi1Funct.value(reducedContact);

            double reducedLoad = reducedContact*(2*reducedIndent*(1 + xi0) - (Math.PI/2.)*reducedContact*(1+xi1));
            return reducedLoad;
        }

    }
}
