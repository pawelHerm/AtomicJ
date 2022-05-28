
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.analysis.AdhesionEventEstimate;
import atomicJ.analysis.AdhesionWorkProcessedPackFunction;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.analysis.SequentialSearchAssistant;
import atomicJ.analysis.UnspecificAdhesionForceEstimator;
import atomicJ.data.Channel1DData;
import atomicJ.data.ConstantChannel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.statistics.LinearRegressionEsimator;


public class DMTIndentation extends AdhesiveContactModel
{
    private static final List<? extends ProcessedPackFunction<ProcessedSpectroscopyPack>> SPECIAL_FUNCTIONS = Collections.singletonList(AdhesionWorkProcessedPackFunction.getInstance());
    private static final String NAME = "Sphere (DMT)";

    private final Sphere indenter;

    public DMTIndentation(Sphere indenter, SampleModel sample, PrecontactInteractionsModel precontactModel, AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod)
    {
        super(sample, precontactModel, adhesiveEnergyEstimationMethod);
        this.indenter = indenter;
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

    private double[][] createTransformIndentationDataSeparateArraysXYView(double[] forceValues, double[] indentationValues, Point2D recordingPoint)
    {
        int n = forceValues.length;

        double[] indentationValuesConverted = new double[n];
        double[] forceValuesConverted = new double[n];

        double adhesionForce = forceValues[0];

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i]; 
            double force = forceValues[i];
            indentationValuesConverted[i] = indent > 0 ? indent*Math.sqrt(indent) : 0;
            forceValuesConverted[i] = force - adhesionForce;
        }

        return new double[][] {indentationValuesConverted, forceValuesConverted}; 
    }

    private double[][] transformIndentationData(double[] forceValues, double[] indentationValues, Point2D recordingPoint)
    {
        int n = forceValues.length;

        double adhesionForce = forceValues[0];

        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i]; 
            double force = forceValues[i];
            indentationValues[i] = indent > 0 ? indent*Math.sqrt(indent) : 0;
            forceValues[i] = force - adhesionForce;
        }

        double[][] transformed = new double[][] {indentationValues, forceValues};
        return transformed; 
    } 

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] forceValues, double[] indentationValues, Point2D recordingPoint, RegressionStrategy regressionStrategy)  
    {
        double[][] transformedData = transformIndentationData(forceValues, indentationValues, recordingPoint);
        double f = regressionStrategy.getObjectiveFunctionMinimumForThroughOriginLinearRegression(transformedData[1], transformedData[0]);
        return f;
    }


    private LinearRegressionEsimator getTransformedLinearRegression(double[] forceValues, double[] indentationValues, RegressionStrategy regressionStrategy, Point2D recordingPoint)  
    {
        double[][] transformedXYView = createTransformIndentationDataSeparateArraysXYView(forceValues, indentationValues, recordingPoint);

        LinearRegressionEsimator f = regressionStrategy.performRegressionForSingleExponent(transformedXYView[1], transformedXYView[0], 1);

        return f;
    }

    @Override
    public DMTFit getModelFit(Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
    {
        DMTFit fit = new DMTFit(deflectionChannel, contactPoint, recordingPoint, processingSettings);
        return fit;
    }

    @Override
    public SequentialSearchAssistant getSequentialSearchAssistant(Channel1DData curveBranch, Point2D recordingPosition, double springConstant)
    {
        return new DMTIndentationSearchAssistant(curveBranch, recordingPosition);
    }

    public class DMTForceIndentationFunction implements UnivariateFunction
    {
        private final double modulus;
        private final double adhesionForce;
        private final double rsqrt;
        private final double poissonFactor;

        public DMTForceIndentationFunction(double modulus, double adhesionForce)
        {
            this.modulus = modulus;
            this.adhesionForce = adhesionForce;
            this.rsqrt = Math.sqrt(getTipRadius());

            double poissonRatio = getSampleModel().getPoissonRatio();
            this.poissonFactor = 1 - poissonRatio*poissonRatio;
        }

        public double getYoungModulus()
        {
            return modulus;
        }

        public double getAdhesionForce()
        {
            return adhesionForce;
        }

        public double getAdhesionWork()
        {
            double radius = indenter.getRadius();
            double adhesionWork = adhesionForce/(-2*Math.PI*radius);

            return adhesionWork;
        }

        @Override
        public double value(double indent) 
        {                     
            double F = 4*modulus*rsqrt*indent*Math.sqrt(indent)/(3*poissonFactor) + adhesionForce;
            return F;
        }     
    }

    public class DMTFit implements AdhesiveContactModelFit<DMTIndentation>
    {
        private final AnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceContactPoint;
        private final Point1DData forceIndentTransitionPoint;

        private final DMTForceIndentationFunction fittedFunction;

        public DMTFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)  
        {
            this.annotatedForceIndentation = getAnnotatedForceIndentation(deflectionChannel, deflectionContactPoint, processingSettings);

            double[] indentationValues = annotatedForceIndentation.getFittableIndentationValues();
            double[] forceValues = annotatedForceIndentation.getFittableForceValues();

            RegressionStrategy regressionStrategy = processingSettings.getRegressionStrategy();
            LinearRegressionEsimator transformedLinearRegression = getTransformedLinearRegression(forceValues, indentationValues, regressionStrategy, recordingPoint);

            double fitCoeff = transformedLinearRegression.getBestFit().getCoefficient(Double.valueOf(1));

            double poissonRatio = getSampleModel().getPoissonRatio();
            double radius = getIndenter().getRadius();

            double adhesionForce = forceValues[0];
            double modulus = 3*fitCoeff*(1 - poissonRatio*poissonRatio)/(4*Math.sqrt(radius));

            this.fittedFunction = new DMTForceIndentationFunction(modulus, adhesionForce);

            double springConstant = 1000*processingSettings.getSpringConstant();
            this.forceContactPoint = new Point1DData(deflectionContactPoint[0], springConstant*deflectionContactPoint[1], Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);

            double[] forceIndentationTransitionPoint = regressionStrategy.getLastCoveredPoint(forceValues,indentationValues, fittedFunction);  

            this.forceIndentTransitionPoint = new Point1DData(forceIndentationTransitionPoint[0], forceIndentationTransitionPoint[1], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
        }

        @Override
        public DMTIndentation getContactModel() 
        {
            return DMTIndentation.this;
        }

        @Override
        public List<? extends ProcessedPackFunction<ProcessedSpectroscopyPack>> getSpecialFunctions()
        {
            return SPECIAL_FUNCTIONS;
        }

        @Override
        public Channel1DData getForceIndentation()
        {
            return new FlexibleFlatChannel1DData(annotatedForceIndentation.getIndentationValues(), annotatedForceIndentation.getForceValues(), Quantities.INDENTATION_MICRONS,
                    Quantities.FORCE_NANONEWTONS, null);
        }

        @Override
        public boolean isPullOffSeparateFromContact()
        {
            return false;
        }

        protected DMTForceIndentationFunction getFittedFunction()
        {
            return fittedFunction;
        }

        @Override
        public Channel1DData getPointwiseModulus() 
        {
            double[] forceValues = annotatedForceIndentation.getForceValues();
            double[] indentationValues = annotatedForceIndentation.getIndentationValues();
            int n = indentationValues.length;
            if(n - 5 > 0)
            {
                double[] pointwiseModulusYs = new double[n - 5];
                double[] pointwiseModulusXs = new double[n - 5];

                for(int i = 5; i<n;i++)
                {
                    double indent = indentationValues[i];
                    double F = forceValues[i];

                    pointwiseModulusXs[i-5] = indent;
                    pointwiseModulusYs[i-5] = getPointwiseModulus(indent, F);
                }
                return new FlexibleFlatChannel1DData(pointwiseModulusXs, pointwiseModulusYs, Quantities.INDENTATION_MICRONS, Quantities.POINTWISE_MODULUS_KPA, null);
            }
            return FlexibleChannel1DData.getEmptyInstance(Quantities.INDENTATION_MICRONS, Quantities.POINTWISE_MODULUS_KPA);
        }

        @Override
        public Channel1DData getPointwiseModulusFit(int plotPoints)
        {
            double youngsModulus = getYoungModulus();

            double minIndent = annotatedForceIndentation.getMinimalIndentation();
            double maxIndent = annotatedForceIndentation.getMaximalIndentation();

            double step = (maxIndent - minIndent)/(plotPoints - 1.);

            Grid1D grid = new Grid1D(step, minIndent, plotPoints, Quantities.INDENTATION_MICRONS);
            Channel1DData data = new ConstantChannel1DData(youngsModulus, grid, Quantities.POINTWISE_MODULUS_KPA);

            return data;
        }

        @Override
        public Channel1DData getForceIndentationFit(int plotPoints)
        {            
            double[] fittedCurve = new double[plotPoints];

            double minIndent = annotatedForceIndentation.getMinimalIndentation();
            double maxIndent = annotatedForceIndentation.getMaximalIndentation();
            double step = (maxIndent - minIndent)/(plotPoints - 1.);

            for(int i = 0; i<plotPoints;i++)
            {       
                fittedCurve[i] = fittedFunction.value(i*step + minIndent);
            }

            Grid1D grid = new Grid1D(step, minIndent, plotPoints, Quantities.INDENTATION_MICRONS);
            Channel1DData data = new GridChannel1DData(fittedCurve, grid, Quantities.FORCE_NANONEWTONS);

            return data;
        }

        @Override
        public double getCoefficientOfDetermination()
        {
            UnivariateFunction fittedFunction = getFittedFunction();
            return DescriptiveStatistics.getCoefficientOfDetermination(annotatedForceIndentation.getForceValues(), annotatedForceIndentation.getIndentationValues(), fittedFunction);
        }

        //returns adhesion work in mJ/m^2
        @Override
        public double getAdhesionWork()
        {
            double adhesionWork = fittedFunction.getAdhesionWork();
            return adhesionWork;
        }

        @Override
        public double getYoungModulus() 
        {
            double modulus = fittedFunction.getYoungModulus();  

            return modulus;
        }

        @Override
        public double getPointwiseModulus(double indentation, double force) 
        {
            Sphere cone = getIndenter();           
            double v = getContactModel().getSampleModel().getPoissonRatio();
            double R = cone.getRadius();

            double relativeForce = force - fittedFunction.getAdhesionForce();

            double modulus = (3*relativeForce*(1 - v*v))/(4*Math.sqrt(R)*Math.pow(indentation,1.5));
            return modulus;
        }

        @Override
        public double[] convertToForceCurvePoint(double[] forceIndentationPoint)
        {
            return annotatedForceIndentation.convertToForceCurvePoint(forceIndentationPoint[0], forceIndentationPoint[1]);
        }

        @Override
        public Channel1DData convertToForceCurvePoints(double[] forceValues, double[] indentationValues)
        {
            return annotatedForceIndentation.convertToForceCurvePoints(forceValues, indentationValues);
        }

        @Override
        public double[] convertToDeflectionCurvePoint(double[] forceIndentationPoint)
        {
            return annotatedForceIndentation.convertToDeflectionCurvePoint(forceIndentationPoint[0], forceIndentationPoint[1]);
        }

        @Override
        public double[][] convertToDeflectionCurvePoints(double[][] forceIndentationPoints)
        {
            return annotatedForceIndentation.convertToDeflectionCurvePoints(forceIndentationPoints);
        }

        @Override
        public Point1DData getForceIndentationTransitionPoint()
        {
            return forceIndentTransitionPoint;
        }

        @Override
        public Point1DData getPointwiseModulusTransitionPoint()
        {
            double indent = forceIndentTransitionPoint.getX();
            double force = forceIndentTransitionPoint.getY();

            Point1DData point = new Point1DData(indent, getPointwiseModulus(indent, force), forceIndentTransitionPoint.getXQuantity(), Quantities.POINTWISE_MODULUS_KPA);
            return point;
        }

        @Override
        public Point1DData getDeflectionCurveTransitionPoint() 
        {
            return annotatedForceIndentation.convertToDeflectionCurvePoint(forceIndentTransitionPoint);
        }

        @Override
        public Point1DData getForceCurveTransitionPoint() 
        {
            return annotatedForceIndentation.convertToForceCurvePoint(forceIndentTransitionPoint);
        }   

        @Override
        public Point1DData getDeflectionCurveContactPoint()
        {
            return annotatedForceIndentation.convertToDeflectionCurvePoint(forceContactPoint);
        }

        @Override
        public Point1DData getForceCurveContactPoint()
        {
            return annotatedForceIndentation.convertToForceCurvePoint(forceContactPoint);
        }

        @Override
        public Point1DData getDeflectionCurvePullOffPoint()
        {
            return annotatedForceIndentation.convertToDeflectionCurvePoint(forceContactPoint);

        }
        @Override
        public Point1DData getForceCurvePullOffPoint()
        {
            return annotatedForceIndentation.convertToForceCurvePoint(forceContactPoint);
        }

        @Override
        public Point1DData getMaximalDeformationPoint()
        {
            return annotatedForceIndentation.getMaximalForceIndentationPoint();
        }       
    }

    private class DMTIndentationSearchAssistant implements SequentialSearchAssistant
    {
        private final Point2D recordingPosition;
        private final double[] zValues;
        private final double[] dValues;
        private final double adhesionDeflection;
        private final double baselineDeflection;

        private DMTIndentationSearchAssistant(Channel1DData curveBranch, Point2D recordingPosition)
        {
            this.zValues = curveBranch.getXCoordinates();
            this.dValues = curveBranch.getYCoordinates();

            this.recordingPosition = recordingPosition;

            UnspecificAdhesionForceEstimator adhesionEstimator = new UnspecificAdhesionForceEstimator(1);
            AdhesionEventEstimate adhesionEstimate = adhesionEstimator.getAdhesionEventEstimate(curveBranch);
            this.adhesionDeflection = adhesionEstimate.getForceMagnitude(); //this is really adhesion deflection, i.e. adhesion force divided by cantilever spring constant; names should be changes            
            this.baselineDeflection = adhesionEstimate.getLiftOffPoint().getY();            
        }

        //We shift the deflection - indentation data, subtracting the negative value of adhesion as calculated by the AdhesionEstimator (in deflection units)
        //        so that the deflection for the point of maximal adhesion is 0. This penalizes contact points (here points of zero indentation
        //        for which the maximal adhesion is different from the one calculated by the AdhesionForceEstimator
        @Override
        public double getObjectiveFunctionValue(RegressionStrategy precontactStrategy, RegressionStrategy postcontactStartegy, int contactIndex) 
        {
            int n = zValues.length;
            double zContact = zValues[contactIndex];
            double dContact = dValues[contactIndex];

            double[][] precontactXY = getDeflectionSeparationSeparateCoordinateArrays(dValues, zValues, 0, contactIndex, zContact, dContact, baselineDeflection);
            //adhesion is negative, so here we really add the value of adhesion, so that the point of maximal adhesion becomes zero
            double[][] postcontactXY = getDeflectionSeparationSeparateCoordinateArrays(dValues, zValues, contactIndex, n, zContact, dContact, baselineDeflection + adhesionDeflection);

            double precontactOFMinimum = getPrecontactObjectiveFunctionMinimum(precontactXY[1], precontactXY[0], recordingPosition, precontactStrategy);
            double postcontactOFMinimum = getPostcontactObjectiveFunctionMinimum(postcontactXY[1], postcontactXY[0], recordingPosition, postcontactStartegy);

            double result = precontactOFMinimum + postcontactOFMinimum;

            return result;
        }              
    }
}



