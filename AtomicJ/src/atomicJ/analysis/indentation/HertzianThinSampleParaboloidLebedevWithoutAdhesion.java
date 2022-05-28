
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


public class HertzianThinSampleParaboloidLebedevWithoutAdhesion extends HertzianThinSampleModel
{
    private static final double TOLERANCE = 1e-10;
    private static final String NAME = "Paraboloid (Lebedev-Chebyshev)";

    private final UnivariateFunction reducedLoadReducedIndentationDepthFunction;
    private final Sphere indenter;

    private final double coeff;

    public HertzianThinSampleParaboloidLebedevWithoutAdhesion(Sphere indenter, ThinSampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(sample, precontactModel);
        this.indenter = indenter;

        double radius = indenter.getRadius();
        double v = sample.getPoissonRatio();

        this.coeff = 1/(radius*(1-v*v));

        if(isSampleBondedToSubstrate())
        {
            if(MathUtilities.equalWithinTolerance(v, 0.5, TOLERANCE))
            {
                reducedLoadReducedIndentationDepthFunction = ReducedLoadVsReducedIndentationApproximationThinBondedIncompressibleLayer.PARABOLOID;
            }        
            else 
            {
                UnivariateFunction contactRadiusFunction = TauLambdaDependenceBondedSample.DEGREE_TWO.getFunction(v);
                UnivariateFunction xi0Funct = XiApproximationBondedLayer.XI_ZERO.getFunction(v);
                UnivariateFunction xi2Funct = XiApproximationBondedLayer.XI_TWO.getFunction(v);
                reducedLoadReducedIndentationDepthFunction =  new ReducedLoadVsReducedIndentationParaboloid(contactRadiusFunction, xi0Funct, xi2Funct);
            }        
        }
        else
        {
            reducedLoadReducedIndentationDepthFunction = ReducedLoadVsReducedIndentationApproximationThinLooseLayer.PARABOLOID;
        }
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
    public HertzianThinSampleParaboloidFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianThinSampleParaboloidFit fit = new HertzianThinSampleParaboloidFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    @Override
    protected double getForceModulusRatio(double indent, double h)
    {
        double reducedIndentation = indenter.getRadius()*indent/(h*h);
        double ratio = Double.NaN;
        double reducedLoad = reducedLoadReducedIndentationDepthFunction.value(reducedIndentation);

        ratio = coeff*reducedLoad*h*h*h;

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

    public class HertzianThinSampleParaboloidFit extends HertzianFit<HertzianThinSampleParaboloidLebedevWithoutAdhesion>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation; 

        private final Point1DData forceIndentationTransitionPoint;

        private final HertzianThinSampleForceIndentationFunction fittedFunction;

        private final double thickness;
        private final double modulus;

        public HertzianThinSampleParaboloidFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianThinSampleParaboloidLebedevWithoutAdhesion.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues, recordingPoint);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fittedTransformedFunction = regressionStrategy.getFitFunction(fittableForceValues,convertedFittableIndentationValues , 1, false);
            this.modulus = fittedTransformedFunction.getCoefficient(Double.valueOf(1));
            this.thickness = getSampleThickness(recordingPoint);

            this.fittedFunction = new HertzianThinSampleForceIndentationFunction(modulus, thickness);

            double[] lastCoveredPoint = regressionStrategy.getLastCoveredPoint(fittableForceValues,convertedFittableIndentationValues, fittedTransformedFunction);            
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

    private static class ReducedLoadVsReducedIndentationParaboloid implements UnivariateFunction
    {
        private final UnivariateFunction contactRadiusFunction;
        private final UnivariateFunction xi0Funct;
        private final UnivariateFunction xi2Funct;

        private ReducedLoadVsReducedIndentationParaboloid(UnivariateFunction contactRadiusFunction, UnivariateFunction xi0Funct, UnivariateFunction xi2Funct)
        {
            this.contactRadiusFunction = contactRadiusFunction;
            this.xi0Funct = xi0Funct;
            this.xi2Funct = xi2Funct;
        }

        @Override
        public double value(double reducedIndent) 
        {
            double reducedContact = contactRadiusFunction.value(reducedIndent);

            double xi0 = xi0Funct.value(reducedContact);
            double xi2 = xi2Funct.value(reducedContact);

            double reducedLoad = reducedContact*(2*reducedIndent*(1 + xi0) - (2./3.)*reducedContact*reducedContact*(1+xi2));
            return reducedLoad;
        }

    }
}
