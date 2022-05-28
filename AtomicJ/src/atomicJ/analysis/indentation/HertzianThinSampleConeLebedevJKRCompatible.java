
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


public class HertzianThinSampleConeLebedevJKRCompatible extends HertzianThinSampleModel
{
    private static final String NAME = "Cone (Lebedev-Chebyshev)";

    private static final double TOLERANCE = 1e-10;

    private final UnivariateFunction tauLambdaDependece;
    private final UnivariateFunction xi0Funct;
    private final UnivariateFunction xi1Funct;
    private final Cone indenter;
    private final double tanAngle;

    private final double poissonCoeff;

    public HertzianThinSampleConeLebedevJKRCompatible(Cone indenter, ThinSampleModel sample, PrecontactInteractionsModel precontactModel)
    {
        super(sample, precontactModel);
        this.indenter = indenter;

        double angle = indenter.getHalfAngle();
        this.tanAngle = Math.tan(angle);

        double v = sample.getPoissonRatio();

        this.poissonCoeff = 1/((1 - v*v));

        if(isSampleBondedToSubstrate())
        {
            if(MathUtilities.equalWithinTolerance(v, 0.5, TOLERANCE))
            {
                tauLambdaDependece = TauLambdaDependenceBondedIncompressible.DEGREE_TWO;
                xi0Funct = XiApproximationBondedIncompressibleLayer.XI_ZERO;
                xi1Funct = XiApproximationBondedIncompressibleLayer.XI_ONE; 
            }        
            else if(MathUtilities.equalWithinTolerance(v, 0, TOLERANCE))
            {
                tauLambdaDependece = TauLambdaDependenceBondedIdeallyCompressible.DEGREE_TWO;
                xi0Funct = XiApproximationBondedIdeallyCompressibleLayer.XI_ZERO;
                xi1Funct = XiApproximationBondedIdeallyCompressibleLayer.XI_ONE; 
            }
            else 
            {
                tauLambdaDependece = TauLambdaDependenceBondedSample.DEGREE_TWO.getFunction(v);
                xi0Funct = XiApproximationBondedLayer.XI_ZERO.getFunction(v);
                xi1Funct = XiApproximationBondedLayer.XI_ONE.getFunction(v);
            }        
        }
        else
        {
            tauLambdaDependece = TauLambdaDependenceLooseSample.DEGREE_TWO;
            xi0Funct = XiApproximationLooseLayer.XI_ZERO;
            xi1Funct = XiApproximationLooseLayer.XI_ONE;
        }
    }

    private double getContactRadius(double indent, double h)
    {
        boolean bonded = isSampleBondedToSubstrate();
        double contactRadius = bonded ? getContactRadiusBonded(indent, h): getContactRadiusLoose(indent, h);

        return contactRadius;
    }

    private double getContactRadiusLoose(double indent, double h)
    {
        double lambda = indent*tanAngle/h;
        double tau = TauLambdaDependenceLooseSample.DEGREE_ONE.value(lambda);
        double contactRadius = tau*h;
        return contactRadius;
    }

    private double getContactRadiusBonded(double indent, double h)
    {
        double v = getPoissonRatio();

        double v2 = v*v;
        double v3 = v2*v;
        double v4 = v3*v;
        double v5 = v4*v;

        double kappa = indent*tanAngle/h;
        double kappa2 = kappa*kappa;
        double kappa3 = kappa2*kappa;
        double kappa4 = kappa3*kappa;
        double kappa5 = kappa4*kappa;

        double contactRadius = (2/Math.PI)*tanAngle*indent*(1 + kappa*(0.245775732030593337196353359011740121374298606728868577423 + 
                0.048746102527187574015469047099029033665870609841913650325*v + 
                0.054300928036947971075916572298770529277997311625449853374*v2 + 
                0.89102775098468432763816634639322173658921074681322048293*v3 - 
                2.189659572521793802519311608787063962450492105431098343701*v4 + 
                3.298043129388262840720725493125787337950750529728179337314*v5) + kappa2*(0.113465098563516286143541950997427865260217873427726910501 + 
                        0.046672582159720516686153114212672031530019897328450412738*v + 
                        0.103995184730280153309153659092222039417207655154542905788*v2 + 
                        0.256157799357380348271070951792992855193388814129986123221*v3 - 
                        0.158165993883055389248053056057188726726817248252265808348*v4 + 
                        0.809599866305207380782270216721603182687871953529535542429*v5) + kappa3*(0.026491217992348978053795409700250702753535018617139568095 + 
                                0.033973924441636884769763314835183181736120869079267890652*v - 
                                0.858630816802993629109754309574803765344412225984048604356*v2 + 
                                9.266611312710846622053981502306492385760178865739510077514*v3 - 
                                29.374469210461992375020744082521559405072060433528623524302*v4 + 
                                36.9313189978580207652037233810479693125686150562205478532*v5) + kappa4*(-0.164843256367371270504861723796777267445134714091499892423 - 
                                        0.144444823051232284962218672033394170132820259873109122114*v + 
                                        0.385747604577697689585716837109793165962845165950283836116*v2 - 
                                        7.446556216512638514665972018520323072731331782578036152201*v3 + 
                                        22.604219949775453354940832232581792617359265011703457499386*v4 - 
                                        31.720639991709613233181845007469824330637921536820871971643*v5) + kappa5*(0.06710708548996240601566041824098072598958000293770936737 + 
                                                0.068703828737494904058567000391058703549670738605786288302*v - 
                                                0.096679568604879833901229485714613338120785353974608455881*v2 + 
                                                2.46356155783978400210053412098482358951469972296609729504*v3 - 
                                                7.296833864346776281833601362720776680080431742981842477415*v4 + 
                                                10.412578037452137565226718872071255575544162874339964473449*v5));

        return contactRadius;
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
    public HertzianThinSampleSphereFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        HertzianThinSampleSphereFit fit = new HertzianThinSampleSphereFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    @Override
    protected double getForceModulusRatio(double indent, double h)
    {
        double a = getContactRadius(indent, h);
        double tau = a/h;

        boolean bonded = isSampleBondedToSubstrate();

        if(bonded)
        {
            if(MathUtilities.equalWithinTolerance(getPoissonRatio(), 0.5, TOLERANCE))
            {
                double xi0 = XiApproximationBondedIncompressibleLayer.XI_ZERO.value(tau);
                double xi1 = XiApproximationBondedIncompressibleLayer.XI_ONE.value(tau);

                double ratio = poissonCoeff*a*(2*indent*(1+xi0) - Math.PI*a*(1+xi1)/(tanAngle*2));

                return ratio;
            }

            if(MathUtilities.equalWithinTolerance(getPoissonRatio(), 0, TOLERANCE))
            {
                double xi0 = XiApproximationBondedIdeallyCompressibleLayer.XI_ZERO.value(tau);
                double xi1 = XiApproximationBondedIdeallyCompressibleLayer.XI_ONE.value(tau);

                double ratio = poissonCoeff*a*(2*indent*(1+xi0) - Math.PI*a*(1+xi1)/(tanAngle*2));

                return ratio;
            }

            double xi0 = XiApproximationBondedLayer.XI_ZERO.value(tau, getPoissonRatio());
            double xi1 = XiApproximationBondedLayer.XI_ONE.value(tau, getPoissonRatio());

            double ratio = poissonCoeff*a*(2*indent*(1+xi0) - Math.PI*a*(1+xi1)/(tanAngle*2));

            return ratio;
        }

        double xi0 = XiApproximationLooseLayer.XI_ZERO.value(tau);
        double xi1 = XiApproximationLooseLayer.XI_ONE.value(tau);

        double ratio = poissonCoeff*a*(2*indent*(1+xi0) - Math.PI*a*(1+xi1)/(tanAngle*2));

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

    public class HertzianThinSampleSphereFit extends HertzianFit<HertzianThinSampleConeLebedevJKRCompatible>
    {
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation; 

        private final Point1DData forceIndentationTransitionPoint;

        private final HertzianThinSampleForceIndentationFunction fittedFunction;

        private final double thickness;
        private final double modulus;

        public HertzianThinSampleSphereFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            super(HertzianThinSampleConeLebedevJKRCompatible.this, deflectionChannel, deflectionContactPoint, recordingPoint, processingSettings);

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
            double indentation = ForceContourStackFunction.getIndentation(fittableForceValues,fittableIndentationValues, force);
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
