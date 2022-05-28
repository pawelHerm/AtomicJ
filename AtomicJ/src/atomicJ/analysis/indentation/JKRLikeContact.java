
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.MultivariateVectorOptimizer;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;

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
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.MathUtilities;


public abstract class JKRLikeContact extends AdhesiveContactModel
{
    private static final List<? extends ProcessedPackFunction<ProcessedSpectroscopyPack>> SPECIAL_FUNCTIONS = Collections.singletonList(AdhesionWorkProcessedPackFunction.getInstance());

    public JKRLikeContact(SampleModel sample, PrecontactInteractionsModel precontactModel, AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod)
    {
        super(sample, precontactModel, adhesiveEnergyEstimationMethod);
    }

    protected boolean isFitNegativeIndentations()
    {
        return true;
    }

    protected double[] fit(final JKRLikeFitHelper f, final double[] forceIndentationDataYs, final double[] forceIndentationDataXs, int from, int to, MultivariateVectorOptimizer optimizer, int maxEval, double[] initialGuess)
    {
        final int n = to - from;

        MultivariateVectorFunction modelFunction = new MultivariateVectorFunction()
        {
            @Override
            public double[] value(double[] point) 
            {

                double modulus = point[0];
                double adhesionWork = f.calculateAdhesionWork(modulus);
                double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);

                // compute the residuals
                final double[] values = new double[n];
                for (int i = from; i<to;i++)
                {
                    values[i - from] = f.value(forceIndentationDataXs[i], adhesionWork, modulus, fixedGripsContact);
                }

                return values;
            }
        };

        ModelFunctionJacobian modelFunctionJacobian = new ModelFunctionJacobian(
                new MultivariateMatrixFunction() 
                {                    
                    @Override
                    public double[][] value(double[] point)
                    {
                        double modulus = point[0];
                        double adhesionWork = f.calculateAdhesionWork(modulus);
                        double fixedGripsContact = getFixedGripsPullOffContactRadius(adhesionWork, modulus);

                        final double[][] jacobian = new double[n][];
                        for (int i = from; i<to;i++) 
                        {
                            jacobian[i - from] = f.gradient(forceIndentationDataXs[i], adhesionWork, modulus, fixedGripsContact);
                        }  

                        return jacobian;
                    }
                });

        // Prepare least squares problem.
        double[] target  = new double[n];
        double[] weights = new double[n];
        for(int i = from; i<to;i++) 
        {
            target[i - from]  = forceIndentationDataYs[i];
            weights[i - from] = 1;
        }

        // Input to the optimizer: the model and its Jacobian.

        // Perform the fit.
        PointVectorValuePair optimum = optimizer.optimize(new MaxEval(maxEval),new ModelFunction(modelFunction), modelFunctionJacobian,
                new Target(target),new Weight(weights),new InitialGuess(initialGuess));
        // Extract the coefficients.
        return optimum.getPointRef();
    }

    @Override
    public SequentialSearchAssistant getSequentialSearchAssistant(Channel1DData curveBranch, Point2D recordingPosition, double springConstant)
    {
        return new JKRLikeIndentationSearchAssistant(curveBranch, recordingPosition, springConstant);
    }

    public abstract double getFixedGripsPullOffContactRadius(double adhesionWork, double modulus);
    protected abstract double getPullOffIndentation(double modulusSI, double adhesionWorkSI, double springConstant);

    protected abstract AdhesiveForceIndentationFunction fitYoungsModulus(double[] forceIndentationYs, double[] forceIndentationXs, double adhesionForce);
    protected abstract AdhesiveForceIndentationFunction fitYoungsModulus(double[] forceIndentationYs, double[] forceIndentationXs, int from, int to, double adhesionForce);

    protected abstract AdhesiveForceIndentationFunction fitYoungsModulus(double[] forceIndentationYs, double[] forceIndentationXs, double adhesionForce, double startModulusValue);
    protected abstract AdhesiveForceIndentationFunction fitYoungsModulus(double[] forceIndentationYs, double[] forceIndentationXs, int from, int to, double adhesionForce, double startModulusValue);


    protected abstract AdhesiveForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValues);
    protected abstract AdhesiveForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValues, int from, int to);

    protected abstract AdhesiveForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValues, double startModulusValue, double startsAdhesionWork);
    protected abstract AdhesiveForceIndentationFunction fitYoungsModulusAndWorkOfAdhesion(double[] forceValuesSI, double[] indentationValues, int from, int to, double startModulusValue, double startsAdhesionWork);

    protected int getPullOffIndex(double[] deflectionValues, double[] zValues, double[] contactPoint, double baselineDeflection, double adhesionForce, double springConstant)
    {
        int n = deflectionValues.length;

        int zeroIndentationIndex = ArrayUtilities.binarySearchDescending(zValues, 0, n, contactPoint[0]);    

        double zContact = contactPoint[0];
        double dContact = contactPoint[1];

        //We have to multiply indentation and deflection by 1e-6 because the values passed
        //to this method are in microns and we need meters
        double[][] deflectionSeparationSI = getDeflectionSeparationSeparateCoordinateArrays(deflectionValues, zValues, 0, n, zContact, dContact, baselineDeflection, 1e-6, 1e-6);          
        double[] separationValuesSI = deflectionSeparationSI[0];
        double[] deltaDeflectionValuesSI = deflectionSeparationSI[1];

        double[] forceValuesSI = MathUtilities.multiply(deltaDeflectionValuesSI, springConstant);

        AdhesiveForceIndentationFunction postcontactFit = isFitWorkOfAdhesionAlongsideYoungsModulus() ? fitYoungsModulusAndWorkOfAdhesion(forceValuesSI,separationValuesSI , zeroIndentationIndex, n) : fitYoungsModulus(forceValuesSI, separationValuesSI, zeroIndentationIndex, n, adhesionForce);

        double modulus = postcontactFit.getYoungModulus();
        double adhesionWork = postcontactFit.getAdhesionWork();

        double pullOffIndentation = getPullOffIndentation(modulus, adhesionWork, springConstant);    

        int pullOffIndex = ArrayUtilities.binarySearchAscending(separationValuesSI, 0, zeroIndentationIndex + 1, pullOffIndentation);

        return pullOffIndex;
    }    

    private final double[][] getForceSeparationSeparateCoordinateArrays(double[] ddeflectionValues, double[] zValues, 
            double maxIndent, double maxDeflection, int from, int to, double zContact, double dContact, double baselineDeflection,  double springConstant)
    {
        int n= to - from;
        double[] separationValues = new double[n];
        double[] forceValues = new double[n];

        int j = 0;

        for(int i = from; i<to; i++)
        {
            double z = zValues[i];
            double d = ddeflectionValues[i];
            double delta_d = d - dContact;
            double delta_z = zContact - z;
            double indent = delta_z - delta_d;

            if(indent <= maxIndent && (d - baselineDeflection) <= maxDeflection)
            {
                separationValues[j] = indent;
                forceValues[j] = springConstant*(d - baselineDeflection);
                j++;
            }
        }       
        if(j == n)
        {
            double[][] forceIndentationArray = new double[][] {separationValues, forceValues};
            return forceIndentationArray;
        }

        double[][] forceIndentationArray = new double[][] {Arrays.copyOfRange(separationValues, 0, j), Arrays.copyOfRange(forceValues, 0, j)};
        return forceIndentationArray;
    }

    @Override
    public AdhesiveAnnotatedForceIndentation getAnnotatedForceIndentation(Channel1DData deflectionChannel, double[] deflectionContactPoint, ProcessingSettings processingSettings)
    {        
        double springConstantSI = processingSettings.getSpringConstant();
        double springConstant = 1000*springConstantSI;
        double maxIndent = processingSettings.getIndentationLimit();
        double maxDeflection = processingSettings.getLoadLimit()/springConstant;

        int n = deflectionChannel.getItemCount();
        UnspecificAdhesionForceEstimator adhesionEstimator = new UnspecificAdhesionForceEstimator(2);
        AdhesionEventEstimate adhesionEstimate = adhesionEstimator.getAdhesionEventEstimate(deflectionChannel);
        double baselineDeflection = adhesionEstimate.getLiftOffPoint().getY();

        double adhesionForceSI = 1e-6*springConstantSI*adhesionEstimate.getForceMagnitude();

        double[] zValues = deflectionChannel.getXCoordinates();
        double[] deflectionValues = deflectionChannel.getYCoordinates();

        double zContact = deflectionContactPoint[0];
        double dContact = deflectionContactPoint[1];

        int postcontactLimitIndex = isFitNegativeIndentations() ? getPullOffIndex(deflectionValues, zValues, deflectionContactPoint, baselineDeflection, adhesionForceSI, springConstantSI) :
            deflectionChannel.getIndexOfGreatestXSmallerOrEqualTo(zContact);


        double[][] forceIndentationArray = getForceSeparationSeparateCoordinateArrays(deflectionValues, zValues, maxIndent, maxDeflection, postcontactLimitIndex, n, zContact, dContact, baselineDeflection, springConstant);
        double[] indentationValues = forceIndentationArray[0];
        double[] forceValues = forceIndentationArray[1];

        //calculate pull-off point (it may not be included in the force indentation list due to minIndent/maxIndent and minDeflection/maxDeflection conditions)
        //so we have to perform this calculation separately 
        double[] p = deflectionChannel.getPoint(postcontactLimitIndex);
        double z = p[0];
        double d = p[1];
        double delta_d = d - dContact;
        double delta_z = zContact - z;
        double pullOffIndentation = delta_z - delta_d;
        double pullOfDeflection = (d - baselineDeflection);
        double[] pullOffPoint = new double[] {pullOffIndentation, pullOfDeflection};

        double fitIndentationLimit = processingSettings.getFitIndentationLimit();
        int upperLimitIndentationIndex = Double.isInfinite(fitIndentationLimit) ? indentationValues.length :
            ArrayUtilities.binarySearchAscending(indentationValues, 0, indentationValues.length, fitIndentationLimit);

        double leftFitZLimit = processingSettings.getFitZMinimum();
        double rightFitZLimit = processingSettings.getFitZMaximum();

        int lowerLimitIndex = Double.isInfinite(rightFitZLimit) ? 0 :  deflectionChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(rightFitZLimit) - postcontactLimitIndex - 1;
        int upperLimitZIndex = Double.isInfinite(leftFitZLimit) ? n - postcontactLimitIndex : deflectionChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(leftFitZLimit) - postcontactLimitIndex;

        int upperLimitIndex = Math.min(upperLimitIndentationIndex, upperLimitZIndex);

        AdhesiveAnnotatedForceIndentation annotated = new AdhesiveAnnotatedForceIndentation(indentationValues, forceValues, lowerLimitIndex, upperLimitIndex, deflectionContactPoint, adhesionForceSI, pullOffPoint, baselineDeflection, springConstant);

        return annotated;
    }

    protected class JKRLikeIndentationSearchAssistant implements SequentialSearchAssistant
    {
        private final Point2D recordingPosition;
        private final double[] zValues;
        private final double[] deflectionValues;
        private final double adhesionForce;
        private final double baselineDeflection;
        private final double springConstant;

        private double startModulusValue = 10000;
        private double startAdhesionValue = 1e-4;

        private JKRLikeIndentationSearchAssistant(Channel1DData curveBranch, Point2D recordingPosition, double springConstant)
        {        
            this.zValues = curveBranch.getXCoordinates();
            this.deflectionValues = curveBranch.getYCoordinates();
            this.recordingPosition = recordingPosition;
            this.springConstant = springConstant;

            UnspecificAdhesionForceEstimator adhesionEstimator = new UnspecificAdhesionForceEstimator(1);
            AdhesionEventEstimate adhesionEstimate = adhesionEstimator.getAdhesionEventEstimate(curveBranch);
            this.adhesionForce =  1e-6*springConstant*adhesionEstimate.getForceMagnitude();
            this.baselineDeflection = adhesionEstimate.getLiftOffPoint().getY();
        }

        @Override
        public double getObjectiveFunctionValue(RegressionStrategy precontactStrategy, RegressionStrategy postcontactStrategy, int zeroIndentationIndex) 
        {            
            int n = zValues.length;
            double zContact = zValues[zeroIndentationIndex];
            double dContact = deflectionValues[zeroIndentationIndex];

            double[][] deflectionSeparationSIXYView = getDeflectionSeparationSeparateCoordinateArrays(deflectionValues, zValues, 0, n, zContact, dContact, baselineDeflection, 1e-6, 1e-6);          
            double[] separationValuesSI = deflectionSeparationSIXYView[0];
            double[] deflectionValuesSI = deflectionSeparationSIXYView[1];
            double[] forceValuesSI = MathUtilities.multiply(deflectionValuesSI, springConstant);     

            AdhesiveForceIndentationFunction postcontactFit = isFitWorkOfAdhesionAlongsideYoungsModulus() 
                    ? fitYoungsModulusAndWorkOfAdhesion(forceValuesSI, separationValuesSI, zeroIndentationIndex, n, startModulusValue, startAdhesionValue):
                        fitYoungsModulus(forceValuesSI, separationValuesSI, zeroIndentationIndex, n, adhesionForce, startModulusValue);

                    double modulus = postcontactFit.getYoungModulus();
                    double adhesionWork = postcontactFit.getAdhesionWork();

                    this.startModulusValue = modulus;
                    this.startAdhesionValue = adhesionWork;

                    double pullOffIndentation = getPullOffIndentation(modulus, adhesionWork, springConstant);

                    //We have to multiply indentation and deflection by  1e-6 because the values passed
                    //to this method are in microns and we need meters

                    int pullOffIndex = ArrayUtilities.binarySearchAscending(separationValuesSI, 0, zeroIndentationIndex + 1, pullOffIndentation);

                    double precontactOFMinimum = getPrecontactObjectiveFunctionMinimum(forceValuesSI, separationValuesSI, 0, pullOffIndex, recordingPosition, precontactStrategy);
                    double postcontactOFMinimum = isFitNegativeIndentations() ? postcontactStrategy.getObjectiveFunctionValue(forceValuesSI, separationValuesSI, pullOffIndex, n, postcontactFit) : postcontactStrategy.getObjectiveFunctionValue(forceValuesSI, separationValuesSI, zeroIndentationIndex, n, postcontactFit);

                    double result = precontactOFMinimum + postcontactOFMinimum;
                    return result;   
        }               
    }


    public abstract class JKRLikeContactFit<E extends JKRLikeContact> implements AdhesiveContactModelFit <E>
    {
        private final AdhesiveAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceContactPoint;

        public JKRLikeContactFit(Channel1DData deflectionChannel, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
        {
            this.annotatedForceIndentation = JKRLikeContact.this.getAnnotatedForceIndentation(deflectionChannel, deflectionContactPoint, processingSettings);

            double springConstant = 1000*processingSettings.getSpringConstant();
            this.forceContactPoint = new Point1DData(deflectionContactPoint[0], springConstant*deflectionContactPoint[1], Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);                   
        }

        @Override
        public List<? extends ProcessedPackFunction<ProcessedSpectroscopyPack>> getSpecialFunctions()
        {
            return SPECIAL_FUNCTIONS;
        }

        @Override
        public Channel1DData getForceIndentation()
        {
            return new FlexibleFlatChannel1DData(annotatedForceIndentation.getIndentationValues(),annotatedForceIndentation.getForceValues(), Quantities.INDENTATION_MICRONS,
                    Quantities.FORCE_NANONEWTONS, null);        
        }

        protected AdhesiveAnnotatedForceIndentation getAnnotatedForceIndentation()
        {
            return annotatedForceIndentation;
        }

        protected abstract UnivariateFunction getFittedFunction();

        @Override
        public Channel1DData getPointwiseModulus() 
        {
            double[] indentationValues = annotatedForceIndentation.getIndentationValues();
            double[] forceValues = annotatedForceIndentation.getForceValues();

            int n = indentationValues.length;
            if(n - 5 > 0)
            {
                double[] pointwiseModulusXs = new double[n - 5];
                double[] pointwiseModulusYs = new double[n - 5];
                for(int i = 5; i<n;i++)
                {
                    double indent = indentationValues[i];
                    double F = forceValues[i];
                    pointwiseModulusXs[i - 5] = indent;
                    pointwiseModulusYs[i - 5] = getPointwiseModulus(indent, F);
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
            UnivariateFunction fittedFunction = getFittedFunction();
            double[] fittedCurve = new double[plotPoints];

            double minIndent = annotatedForceIndentation.getMinimalIndentation();
            double maxIndent = annotatedForceIndentation.getMaximalIndentation();
            double step = (maxIndent - minIndent)/(plotPoints - 1.);

            for(int i = 0; i<plotPoints;i++)
            {       
                fittedCurve[i] = 1e9*fittedFunction.value(1e-6*(i*step + minIndent));
            }

            Grid1D grid = new Grid1D(step, minIndent, plotPoints, Quantities.INDENTATION_MICRONS);
            Channel1DData data = new GridChannel1DData(fittedCurve, grid, Quantities.FORCE_NANONEWTONS);

            return data;
        }

        @Override
        public double getCoefficientOfDetermination()
        {
            double[] indentationValues = annotatedForceIndentation.getIndentationValues();   
            double[] forceValues = annotatedForceIndentation.getForceValues();
            final UnivariateFunction fittedFunction = getFittedFunction();

            return DescriptiveStatistics.getCoefficientOfDetermination(forceValues, indentationValues, new UnivariateFunction() {

                @Override
                public double value(double indent)
                {
                    return 1e9*fittedFunction.value(1e-6*indent);
                }
            });
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
        public boolean isPullOffSeparateFromContact()
        {
            return true;
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
            return annotatedForceIndentation.convertToDeflectionCurvePoint(annotatedForceIndentation.getPullOffPoint());
        }

        @Override
        public Point1DData getForceCurvePullOffPoint()
        {
            return annotatedForceIndentation.convertToForceCurvePoint(annotatedForceIndentation.getPullOffPoint());
        }

        @Override
        public Point1DData getDeflectionCurveTransitionPoint() 
        {
            return annotatedForceIndentation.convertToDeflectionCurvePoint(getForceIndentationTransitionPoint());
        }

        @Override
        public Point1DData getForceCurveTransitionPoint() 
        {
            return annotatedForceIndentation.convertToForceCurvePoint(getForceIndentationTransitionPoint());
        }   

        @Override
        public Point1DData getPointwiseModulusTransitionPoint()
        {
            Point1DData forceIndentationTransitionPoint = getForceIndentationTransitionPoint();
            double indent = forceIndentationTransitionPoint.getX();
            double F = forceIndentationTransitionPoint.getY();

            Point1DData pointwiseModulusTranstionPoint = new Point1DData(indent, getPointwiseModulus(indent, F), forceIndentationTransitionPoint.getXQuantity(), Quantities.POINTWISE_MODULUS_KPA);
            return pointwiseModulusTranstionPoint;
        }

        @Override
        public Point1DData getMaximalDeformationPoint()
        {
            return annotatedForceIndentation.getMaximalForceIndentationPoint();
        }       
    }


    protected interface JKRLikeFitHelper extends ParametricUnivariateFunction
    {
        public double value(double indentationSI, double adhesionWork, double modulus);
        public double value(double indentationSI, double adhesionWork, double modulus, double fixedGripsContact);
        public double[] gradient(double indentationSI, double adhesionWork, double modulus);
        public double[] gradient(double indentationSI, double adhesionWork, double modulus, double fixedGripsContact);
        public double calculateAdhesionWork(double modulus);
    }

}