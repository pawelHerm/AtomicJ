
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

public class HertzianThinSampleSphereLebedevChebyshev extends HertzianThinSampleModel
{
    private static final String NAME = "Sphere (Lebedev-Chebyshev)";

    private static final double PHI1 = 0.7138069436040935;
    private static final double PHI2 = 0.7615440650581432;
    private static final double PHI3 = -0.3516731491863739;
    private static final double PHI4 = 0.08191074776226692;
    private static final double PHI5 = -0.0076065821157105425;

    private static final double OMEGA1 = 0.7280820702421041;
    private static final double OMEGA2 = 0.688804991760332;
    private static final double OMEGA3 = -0.46597008465458;
    private static final double OMEGA4 = 0.14257696898556055;
    private static final double OMEGA5 = -0.01624454245825074;

    private final Sphere indenter;
    private final double poissonCoeff;

    public HertzianThinSampleSphereLebedevChebyshev(Sphere indenter, ThinSampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(sample, precontactModel);

        this.indenter = indenter;

        double v = getPoissonRatio();

        this.poissonCoeff = 1./(1-v*v);
    }

    private double getContactRadius(double indent, double h)
    {
        boolean bonded = isSampleBondedToSubstrate();
        double contactRadius = bonded ? getContactRadiusBonded(indent,h) : getContactRadiusLoose(indent, h);
        return contactRadius; 
    }

    private double getContactRadiusLoose(double indent, double h)
    {
        double radius = indenter.getRadius();

        double varKappaA = indent/radius;
        double varKappaB = radius/h;

        double varTheta = Math.tanh( Math.sqrt(varKappaA)*(1 + varKappaA/6. + 11*varKappaA*varKappaA/360.));
        double varTheta2 = varTheta*varTheta;

        double varKappaB2 = varKappaB*varKappaB;
        double varKappaB3 = varKappaB2*varKappaB;
        double varKappaB4 = varKappaB3*varKappaB;
        double varKappaB5 = varKappaB4*varKappaB;

        double phi1 = 0.23684461705263712*varKappaB + 0.0355544898011291*varKappaB2 - 0.03580154413401823*varKappaB3 + 0.01100159059811124*varKappaB4 - 0.0009657491346944189*varKappaB5;
        double phi2 = 0.25541460436098096*varKappaB - 0.671173296229366*varKappaB2 + 0.8109602219877212*varKappaB3 - 0.24069696720099812*varKappaB4 + 0.020653717834703024*varKappaB5;
        double phi3 = -1.6224090268054143*varKappaB + 4.906106932497222*varKappaB2 - 4.632034071930544*varKappaB3 + 1.2320616018872645*varKappaB4 - 0.10076499111432936*varKappaB5;
        double phi4 = 3.0603140469153267*varKappaB - 9.110901596174042*varKappaB2 + 7.615460859974668*varKappaB3 - 1.923856346047137*varKappaB4 + 0.15336109714418442*varKappaB5;
        double phi5 = -1.8561474123685113*varKappaB + 4.8120195569283455*varKappaB2 - 3.7803302984126517*varKappaB3 + 0.9290082277195211*varKappaB4 - 0.07289440257696243*varKappaB5;

        double contactRadius = radius*varTheta*(1 + phi1*varTheta + phi2*varTheta2 + phi3*varTheta2*varTheta + phi4*varTheta2*varTheta2 + phi5*varTheta2*varTheta2*varTheta);

        return contactRadius; 
    }

    private double getContactRadiusBonded(double indent, double h)
    {
        throw new UnsupportedOperationException("Support for Lebedev - Chebyshev model for bonded samples is not implemented yet");
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
        double radius = indenter.getRadius();

        double a = getContactRadius(indent, h);
        double tau = a/h;

        boolean bonded = isSampleBondedToSubstrate();

        double phi = bonded ? getPhiFactorBonded(tau): getPhiFactorLoose(tau);
        double omega = bonded ? getOmegaFactorBonded(tau): getOmegaFactorLoose(tau);

        double ratio = poissonCoeff*(2*indent*a*phi + (0.5*(radius*radius - a*a)*Math.log((radius + a)/(radius - a)) - radius*a)*omega);

        return ratio;
    }

    private double getPhiFactorLoose(double tau)
    {
        double tau2 = tau*tau;
        double tau3 = tau2*tau;
        double tau4 = tau3*tau;
        double tau5 = tau4*tau;

        double phi = 1 + PHI1*tau + PHI2*tau2 + PHI3*tau3 + PHI4*tau4 + PHI5*tau5;

        return phi;
    }

    private double getOmegaFactorLoose(double tau)
    {
        double tau2 = tau*tau;
        double tau3 = tau2*tau;
        double tau4 = tau3*tau;
        double tau5 = tau4*tau;

        double psi = 1 + OMEGA1*tau + OMEGA2*tau2 + OMEGA3*tau3 + OMEGA4*tau4 + OMEGA5*tau5;

        return psi;
    }

    private double getPhiFactorBonded(double tau)
    {
        throw new UnsupportedOperationException("Support for Lebedev - Chebyshev model for bonded samples is not implemented yet");

    }

    private double getOmegaFactorBonded(double tau)
    {
        throw new UnsupportedOperationException("Support for Lebedev - Chebyshev model for bonded samples is not implemented yet");

    }


    //    private class DimensionalContactDepthFunction implements UnivariateFunction
    //    {
    //        private final double indentByh;//delta/h
    //        private final double Rbyh;//R/h
    //
    //        public DimensionalContactDepthFunction(double indentByh, double Rbyh)
    //        {
    //            this.indentByh = indentByh;
    //            this.Rbyh = Rbyh;
    //        }
    //
    //        @Override
    //        public double value(double tau) 
    //        {      
    //            double x = tau/Rbyh;
    //            double logCoeff = Math.log((1+x)/(1-x))/2;
    //            double coeff1 = 2*x*x;
    //            double coeff2 = x + (x*x - 1)*(logCoeff);
    //            double val = 2*indentByh*(1+getPhiFactorLoose(tau))/Math.PI -(2/Math.PI)*tau*(logCoeff + (coeff2/coeff1)) ;
    //            return val;
    //        }       
    //    }

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

    public class HertzianThinSampleSphereFit extends HertzianFit<HertzianThinSampleSphereLebedevChebyshev>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation; 

        private final Point1DData forceIndentationTransitionPoint;

        private final HertzianThinSampleForceIndentationFunction fittedFunction;

        private final double thickness;
        private final double modulus;

        public HertzianThinSampleSphereFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianThinSampleSphereLebedevChebyshev.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

            this.annotatedForceIndentation = getAnnotatedForceIndentation();

            double[] fittableIndentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] fittableForceValues = annotatedForceIndentation.getFittableForceValues();
            double[] convertedFittableIndentationValues = convertIndentationData(fittableIndentationValues, recordingPoint);

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            FittedLinearUnivariateFunction fittedTransformedFunction = regressionStrategy.getFitFunction(fittableForceValues,convertedFittableIndentationValues, 1, false);
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
