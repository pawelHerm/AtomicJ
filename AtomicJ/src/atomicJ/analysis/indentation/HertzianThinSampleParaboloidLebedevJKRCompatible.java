
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


public class HertzianThinSampleParaboloidLebedevJKRCompatible extends HertzianThinSampleModel
{
    private static final double TOLERANCE = 1e-10;
    private static final String NAME = "Paraboloid (Lebedev-Chebyshev)";

    private final UnivariateFunction tauLambdaDependece;
    private final UnivariateFunction xi0Funct;
    private final UnivariateFunction xi2Funct;
    private final Sphere indenter;

    private final double coeff;

    public HertzianThinSampleParaboloidLebedevJKRCompatible(Sphere indenter, ThinSampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(sample, precontactModel);
        this.indenter = indenter;

        double radius = indenter.getRadius();
        double v = sample.getPoissonRatio();

        this.coeff = 2/(3*(1 - v*v)*radius);

        if(isSampleBondedToSubstrate())
        {
            if(MathUtilities.equalWithinTolerance(v, 0.5, TOLERANCE))
            {
                tauLambdaDependece = TauLambdaDependenceBondedIncompressible.DEGREE_TWO;
                xi0Funct = XiApproximationBondedIncompressibleLayer.XI_ZERO;
                xi2Funct = XiApproximationBondedIncompressibleLayer.XI_TWO; 
            }        
            else if(MathUtilities.equalWithinTolerance(v, 0, TOLERANCE))
            {
                tauLambdaDependece = TauLambdaDependenceBondedIdeallyCompressible.DEGREE_TWO;
                xi0Funct = XiApproximationBondedIdeallyCompressibleLayer.XI_ZERO;
                xi2Funct = XiApproximationBondedIdeallyCompressibleLayer.XI_TWO; 
            }
            else 
            {
                tauLambdaDependece = TauLambdaDependenceBondedSample.DEGREE_TWO.getFunction(v);
                xi0Funct = XiApproximationBondedLayer.XI_ZERO.getFunction(v);
                xi2Funct = XiApproximationBondedLayer.XI_TWO.getFunction(v);
            }        
        }
        else
        {
            tauLambdaDependece = TauLambdaDependenceLooseSample.DEGREE_TWO;
            xi0Funct = XiApproximationLooseLayer.XI_ZERO;
            xi2Funct = XiApproximationLooseLayer.XI_TWO;
        }
    }

    private double getContactRadius(double indent, double h)
    {
        double lambda = indenter.getRadius()*indent/(h*h);       
        double contactRadius = tauLambdaDependece.value(lambda)*h;
        return contactRadius;
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
        double radius = indenter.getRadius();

        double a = getContactRadius(indent, h);
        double tau = a/h;

        double ratio = Double.NaN;
        double xi0 = xi0Funct.value(tau);
        double xi2 = xi2Funct.value(tau);

        ratio = coeff*a*(3*indent*radius*(1 + xi0) - a*a*(1 + xi2));

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

    public class HertzianThinSampleParaboloidFit extends HertzianFit<HertzianThinSampleParaboloidLebedevJKRCompatible>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation; 

        private final Point1DData forceIndentationTransitionPoint;

        private final HertzianThinSampleForceIndentationFunction fittedFunction;

        private final double thickness;
        private final double modulus;

        public HertzianThinSampleParaboloidFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianThinSampleParaboloidLebedevJKRCompatible.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

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
}
