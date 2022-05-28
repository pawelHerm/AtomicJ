package atomicJ.analysis.indentation;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SequentialSearchAssistant;
import atomicJ.data.Channel1DData;
import atomicJ.data.ConstantChannel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.IndexRange;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.statistics.DescriptiveStatistics;
import atomicJ.utilities.ArrayUtilities;


public abstract class HertzianContactModel implements ContactModel 
{
    private final PrecontactInteractionsModel precontactModel;

    public HertzianContactModel(PrecontactInteractionsModel precontactModel)
    {
        this.precontactModel = precontactModel;
    }

    @Override
    public IndexRange getRangeOfValidTrialContactPointIndices(Channel1DData deflectionChannel, Point2D recordingPoint, double springConstant)
    {
        return new IndexRange(0, deflectionChannel.getItemCount() - 1);
    }

    @Override
    public SequentialSearchAssistant getSequentialSearchAssistant(Channel1DData curveBranch, Point2D recordingPosition, double springConstant)
    {
        return new HerziantIndentationSearchAssistant(curveBranch, recordingPosition);
    }

    @Override
    public double getPrecontactObjectiveFunctionMinimum(double[] precontactForceSeparationYs, double[] precontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        return getPrecontactObjectiveFunctionMinimum(precontactForceSeparationYs, precontactForceSeparationXs, 0, precontactForceSeparationYs.length, recordingPoint, regressionStrategy);
    }

    public double getPrecontactObjectiveFunctionMinimum(double[] precontactForceSeparationYs, double[] precontactForceSeparationXs, int from, int to, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        return precontactModel.getPrecontactObjectiveFunctionMinimum(precontactForceSeparationYs,precontactForceSeparationXs, from, to, recordingPoint, regressionStrategy);
    }

    @Override
    public double[][] getDeflectionSeparationSeparateCoordinateArrays(double[] deflectionValues, double[] zValues, int from, int to, double zContact, double dContact)
    {       
        int n = to - from;
        double[] transformedXs = new double[n];
        double[] transformedYs = new double[n];

        for(int i = from; i<to; i++)
        {
            double z = zValues[i];
            double d = deflectionValues[i];
            double delta_d = d - dContact;
            double delta_z = zContact - z;
            double s = delta_z-delta_d;

            transformedXs[i - from] = s; 
            transformedYs[i - from] = delta_d; 
        }  

        double[][] transformed = new double[][] {transformedXs, transformedYs};
        return transformed;
    }

    private final double[][] getForceSeparationSeparateCoordinateArrays(double[] dValues, double[] zValues, double minIndent, double maxIndent, 
            double maxDeflection,int from, int to, double zContact, double dContact, double springConstant)
    {
        if(maxIndent == Double.POSITIVE_INFINITY && maxDeflection == Double.POSITIVE_INFINITY)
        {
            return getForceSeparationSeparateCoordinateArrays(dValues, zValues, minIndent, from, to, zContact, dContact, springConstant);
        }

        int n= to - from;
        double[] forceIndentationXs = new double[n];
        double[] forceIndentationYs = new double[n];

        int j = 0;
        for(int i = from; i<to; i++)
        {
            double z = zValues[i];
            double d = zValues[i];
            double delta_d = d - dContact;
            double delta_z = zContact - z;
            double indent = delta_z - delta_d;

            if(indent >= minIndent && indent <= maxIndent && delta_d <= maxDeflection)
            {
                forceIndentationXs[j] = indent;
                forceIndentationYs[j] = springConstant*delta_d;
                j++;
            }
        }       

        if(j == n)
        {
            double[][] forceIndentationArray = new double[][] {forceIndentationXs, forceIndentationYs};
            return forceIndentationArray;
        }

        double[][] forceIndentationArray = new double[][] {Arrays.copyOfRange(forceIndentationXs, 0, j), Arrays.copyOfRange(forceIndentationYs, 0, j)};
        return forceIndentationArray;
    }

    private final double[][] getForceSeparationSeparateCoordinateArrays(double[] dValues, double[] zValues, double minIndent, int from, int to, double zContact, double dContact, double springConstant)
    {
        int n = to - from;
        double[] forceIndentationXs = new double[n];
        double[] forceIndentationYs = new double[n];

        int j = 0;
        for(int i = from; i<to; i++)
        {
            double z = zValues[i];
            double d = dValues[i];
            double delta_d = d - dContact;
            double delta_z = zContact - z;
            double indent = delta_z - delta_d;

            if(indent >= minIndent)
            {
                forceIndentationXs[j] = indent;
                forceIndentationYs[j] = springConstant*delta_d;
                j++;
            }
        }       

        if (j == n)
        {
            double[][] forceIndentationArray = new double[][] {forceIndentationXs, forceIndentationYs};
            return forceIndentationArray;
        }
        double[][] forceIndentationArray = new double[][] {Arrays.copyOfRange(forceIndentationXs, 0, j),Arrays.copyOfRange(forceIndentationYs, 0, j)};

        return forceIndentationArray;
    }

    public double getMaximalIndentationDepthUnderAssumptionOfIndentationMonotonicIncrease(Channel1DData deflectionChannel, double[] contactPoint)
    {
        int n = deflectionChannel.getItemCount();

        double zContact = contactPoint[0];
        double dContact = contactPoint[1];

        double[] p = deflectionChannel.getPoint(n - 1);
        double z = p[0];
        double d = p[1];
        double delta_d = d - dContact;
        double delta_z = zContact - z;
        double indent = delta_z - delta_d;

        return indent;
    }

    public HertzianAnnotatedForceIndentation getAnnotatedForceIndentation(Channel1DData deflectionChannel, double[] contactPoint, ProcessingSettings processingSettings)
    {    
        int n = deflectionChannel.getItemCount();

        double zContact = contactPoint[0];
        double dContact = contactPoint[1];

        double springConstant = 1000*processingSettings.getSpringConstant();

        double maxIndent = processingSettings.getIndentationLimit();
        double minIndent = 0;
        double maxDeflection = processingSettings.getLoadLimit()/springConstant;

        //bylo wczesniej ArrayUtilities.binarySearchDescendingX(deflectionChannel.getPoints(), 0, n, zContact)
        //czyli smaller or equal, czyli po stronie in-contact
        int contactIndex = deflectionChannel.getIndexOfGreatestXSmallerOrEqualTo(zContact);

        double[][] forceIndentationArray = getForceSeparationSeparateCoordinateArrays(deflectionChannel.getYCoordinates(), deflectionChannel.getXCoordinates(), minIndent, maxIndent, 
                maxDeflection,contactIndex, n, zContact, dContact, springConstant);

        double[] indentationValues = forceIndentationArray[0];
        double[] forceValues = forceIndentationArray[1];

        double fitIndentationLimit = processingSettings.getFitIndentationLimit();
        int upperLimitIndentationIndex = Double.isInfinite(fitIndentationLimit) ? indentationValues.length :
            ArrayUtilities.binarySearchAscending(indentationValues, 0, indentationValues.length, fitIndentationLimit);

        double leftFitZLimit = processingSettings.getFitZMinimum();
        double rightFitZLimit = processingSettings.getFitZMaximum();

        int lowerLimitIndex = Double.isInfinite(rightFitZLimit) ? 0 :  deflectionChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(rightFitZLimit) - contactIndex - 1;
        int upperLimitZIndex = Double.isInfinite(leftFitZLimit) ? n - contactIndex : deflectionChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(leftFitZLimit) - contactIndex;

        int upperLimitIndex = Math.min(upperLimitIndentationIndex, upperLimitZIndex);

        HertzianAnnotatedForceIndentation annotated = 
                new HertzianAnnotatedForceIndentation(indentationValues, forceValues, lowerLimitIndex, upperLimitIndex, contactPoint, dContact, springConstant);

        return annotated;
    }

    public static abstract class HertzianFit <E extends HertzianContactModel> implements ContactModelFit <E>
    {
        private final E indentationModel;
        private final HertzianAnnotatedForceIndentation annotatedForceIndentation;

        private final Point1DData forceContactPoint;

        public HertzianFit(E indentationModel, Channel1DData deflectionChannel, double[] contactPoint, Point2D recordingPoint, ProcessingSettings processingSettings)
        {           
            this.indentationModel = indentationModel;
            this.annotatedForceIndentation = indentationModel.getAnnotatedForceIndentation(deflectionChannel, contactPoint, processingSettings);

            double springConstant = 1000*processingSettings.getSpringConstant();
            this.forceContactPoint = new Point1DData(contactPoint[0], springConstant*contactPoint[1], Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);
        }

        public HertzianAnnotatedForceIndentation getAnnotatedForceIndentation()
        {
            return annotatedForceIndentation;
        }

        @Override
        public List<ProcessedPackFunction<ProcessedSpectroscopyPack>> getSpecialFunctions()
        {
            return Collections.emptyList();
        }

        @Override
        public E getContactModel() 
        {
            return indentationModel;
        }

        @Override
        public Channel1DData getForceIndentation()
        {
            return new FlexibleFlatChannel1DData(annotatedForceIndentation.getIndentationValues(),annotatedForceIndentation.getForceValues(), Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS, null);
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
            double[] fittedCurve = new double[plotPoints];
            UnivariateFunction fittedFunction = getFittedFunction();

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

        public Channel1DData getForceIndentationFit()
        {
            double[] indentationValues = annotatedForceIndentation.getIndentationValues();
            int n = indentationValues.length;

            double[] forceIndentationFitXs = new double[n];
            double[] forceIndentationFitYs = new double[n];

            UnivariateFunction fittedFunction = getFittedFunction();

            for(int i = 0; i<n;i++)
            {
                double x = indentationValues[i];
                forceIndentationFitXs[i] = x;
                forceIndentationFitYs[i] = fittedFunction.value(x);
            }

            return new FlexibleFlatChannel1DData(forceIndentationFitXs,forceIndentationFitYs, Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS, null);
        }

        @Override
        public double getCoefficientOfDetermination()
        {
            double[] indentationValues = annotatedForceIndentation.getIndentationValues();
            double[] forceValues = annotatedForceIndentation.getForceValues();
            UnivariateFunction fittedFunction = getFittedFunction();

            return DescriptiveStatistics.getCoefficientOfDetermination(forceValues, indentationValues, fittedFunction);
        }

        @Override
        public boolean isPullOffSeparateFromContact()
        {
            return false;
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
        public Point1DData getDeflectionCurveTransitionPoint() 
        {
            Point1DData forceIndentationTransitionPoint = getForceIndentationTransitionPoint();
            return annotatedForceIndentation.convertToDeflectionCurvePoint(forceIndentationTransitionPoint);
        }

        @Override
        public Point1DData getForceCurveTransitionPoint() 
        {
            Point1DData forceIndentationTransitionPoint = getForceIndentationTransitionPoint();

            double[] pData = annotatedForceIndentation.convertToForceCurvePoint(forceIndentationTransitionPoint.getX(), forceIndentationTransitionPoint.getY());
            Point1DData forceCurveTransitionPoint = new Point1DData(pData[0], pData[1], Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);

            return forceCurveTransitionPoint;
        }   

        @Override
        public Point1DData getMaximalDeformationPoint()
        {
            return annotatedForceIndentation.getMaximalForceIndentationPoint();
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
    }

    private class HerziantIndentationSearchAssistant implements SequentialSearchAssistant
    {
        private final Point2D recordingPosition;
        private final double[] zValues;
        private final double[] deflectionValues;

        private HerziantIndentationSearchAssistant(Channel1DData curveBranch, Point2D recordingPosition)
        {
            this.zValues = curveBranch.getXCoordinates();
            this.deflectionValues = curveBranch.getYCoordinates();
            this.recordingPosition = recordingPosition;
        }

        @Override
        public double getObjectiveFunctionValue(RegressionStrategy precontactStrategy, RegressionStrategy postcontactStartegy, int contactIndex) 
        {
            double contactZ = zValues[contactIndex];
            double contactD = deflectionValues[contactIndex];

            int n = zValues.length;

            double[][] precontact = getDeflectionSeparationSeparateCoordinateArrays(deflectionValues, zValues, 0, contactIndex, contactZ, contactD);
            double[][] postcontact = getDeflectionSeparationSeparateCoordinateArrays(deflectionValues, zValues, contactIndex, n, contactZ, contactD);
            double precontactOFMinimum = getPrecontactObjectiveFunctionMinimum(precontact[1], precontact[0], recordingPosition, precontactStrategy);
            double postcontactOFMinimum = getPostcontactObjectiveFunctionMinimum(postcontact[1], postcontact[0], recordingPosition, postcontactStartegy);

            double result = precontactOFMinimum + postcontactOFMinimum;

            return result;
        }              
    }
}

