package atomicJ.analysis.indentation;

import java.awt.geom.Point2D;
import java.util.Arrays;
import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.analysis.AdhesionEventEstimate;
import atomicJ.analysis.BasicRegressionStrategy;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.analysis.UnspecificAdhesionForceEstimator;
import atomicJ.data.Channel1DData;
import atomicJ.data.IndexRange;
import atomicJ.utilities.ArrayUtilities;

public abstract class AdhesiveContactModel implements ContactModel 
{
    private final SampleModel sample;
    private final AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod;
    private final PrecontactInteractionsModel precontactModel;

    public AdhesiveContactModel(SampleModel sample, PrecontactInteractionsModel precontactModel, AdhesiveEnergyEstimationMethod adhesiveEnergyEstimationMethod)
    {
        this.precontactModel = precontactModel;
        this.adhesiveEnergyEstimationMethod = adhesiveEnergyEstimationMethod;
        this.sample = sample;
    }

    @Override
    public IndexRange getRangeOfValidTrialContactPointIndices(Channel1DData deflectionChannel, Point2D recordingPoint, double springConstant)
    {
        return new IndexRange(0, deflectionChannel.getItemCount() - 1);
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }        

    protected boolean isFitWorkOfAdhesionAlongsideYoungsModulus()
    {
        return AdhesiveEnergyEstimationMethod.FROM_FIT.equals(adhesiveEnergyEstimationMethod);
    }    

    @Override
    public double getPrecontactObjectiveFunctionMinimum(double[] forceValues, double[] separationValues, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        return getPrecontactObjectiveFunctionMinimum(forceValues, separationValues, 0, forceValues.length, recordingPoint, regressionStrategy);
    }

    public double getPrecontactObjectiveFunctionMinimum(double[] forceValues, double[] separationValues, int from, int to, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        return precontactModel.getPrecontactObjectiveFunctionMinimum(forceValues, separationValues, from, to,  recordingPoint, regressionStrategy);
    }


    //does not modify the dValues and zValues arrays
    protected double getBaselineDeflection(double[] dValues, double[] zValues, double contactZ)
    {     
        UnivariateFunction f = precontactModel.getPrecontactFit(dValues, zValues, null, BasicRegressionStrategy.CLASSICAL_L2);       
        return f.value(contactZ);
    }

    @Override
    public double[][] getDeflectionSeparationSeparateCoordinateArrays(double[] dValues, double[] zValues, int from, int to, double contactZ, double contactD)
    {
        int n = zValues.length;

        int contactIndex = ArrayUtilities.binarySearchDescending(zValues, 0, n, contactZ);

        double d0 = getBaselineDeflection(dValues, zValues, contactZ);            
        double zContact = zValues[contactIndex];
        double dContact = dValues[contactIndex];

        double[][] transformedAll = getDeflectionSeparationSeparateCoordinateArrays(dValues,zValues, 0, n, zContact, dContact, d0);

        return transformedAll;
    }

    public double[][] getDeflectionSeparationSeparateCoordinateArrays(double[] dValues, double[] zValues, int from, int to, double zContact, double dContact, double deflectionReference)
    {
        double[] xs = new double[to - from];
        double[] ys = new double[to - from];

        for(int i = from; i<to; i++)
        {
            double z = zValues[i];
            double d = dValues[i];
            double delta_d = d - dContact;
            double delta_z = zContact - z;
            double s = delta_z - delta_d;

            xs[i - from] = s;
            ys[i - from] = d - deflectionReference; 
        }       

        return new double[][] {xs, ys};
    }

    //deflection reference may be equal to baselineDeflection, or, in the case of DMT model, baselineDeflection + adhesionForce
    // in DMT, adhesion is negative, so here we really add the value of adhesion, so that the point of maximal adhesion becomes zero
    public double[][] getDeflectionSeparationSeparateCoordinateArrays(double[] dValues, double[] zValues, int from, int to, double zContact, double dContact, double baselineDeflection, double sMultiplier, double dMultiplier)
    {
        double[] separations = new double[to - from];
        double[] deltaDs = new double[to - from];

        for(int i = from; i<to; i++)
        {
            double z = zValues[i];
            double d = dValues[i];
            double delta_d = d - dContact;
            double delta_z = zContact - z;

            separations[i - from] = sMultiplier*(delta_z - delta_d);
            deltaDs[i - from] = dMultiplier*(d - baselineDeflection); 
        }       

        return new double[][] {separations, deltaDs};
    }


    private final double[][] getForceSeparationSeparateCoordinateArrays(double[] dValues, double[] zValues, double minIndent, double maxIndent, 
            double maxDeflection,int from, int to, double zContact, double dContact, double baselineDeflection, double springConstant)
    {
        int n= to - from;
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

            if(indent >= minIndent && indent <= maxIndent && (d - baselineDeflection) <= maxDeflection)
            {
                forceIndentationXs[j] = indent; 
                forceIndentationYs[j] = springConstant*(d - baselineDeflection);
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

    public AnnotatedForceIndentation getAnnotatedForceIndentation(Channel1DData deflectionChannel, double[] contactPoint, ProcessingSettings processingSettings)
    {        
        double springConstant = 1000*processingSettings.getSpringConstant();
        double minIndent = 0;
        double maxIndent = processingSettings.getIndentationLimit();
        double maxDeflection = processingSettings.getLoadLimit()/springConstant;

        int n = deflectionChannel.getItemCount();

        double zContact = contactPoint[0];
        double dContact = contactPoint[1];

        //bylo wczesniej ArrayUtilities.binarySearchDescendingX(deflectionChannel.getPoints(), 0, n, zContact)
        //czyli smaller or equal, czyli po stronie in-contact
        int contactIndex = deflectionChannel.getIndexOfGreatestXSmallerOrEqualTo(zContact);

        UnspecificAdhesionForceEstimator adhesionEstimator = new UnspecificAdhesionForceEstimator(2);
        AdhesionEventEstimate adhesionEstimate = adhesionEstimator.getAdhesionEventEstimate(deflectionChannel);
        double baselineDeflection = adhesionEstimate.getLiftOffPoint().getY();

        double[][] forceIndentationArry = getForceSeparationSeparateCoordinateArrays(deflectionChannel.getYCoordinates(),
                deflectionChannel.getXCoordinates() , minIndent, maxIndent, 
                maxDeflection,contactIndex, n, zContact, dContact, baselineDeflection, springConstant);

        double[] indentationValues = forceIndentationArry[0];
        double[] forceValues = forceIndentationArry[1];

        double fitIndentationLimit = processingSettings.getFitIndentationLimit();
        int upperLimitIndentationIndex = Double.isInfinite(fitIndentationLimit) ? indentationValues.length :
            ArrayUtilities.binarySearchAscending(indentationValues, 0, indentationValues.length, fitIndentationLimit);

        double leftFitZLimit = processingSettings.getFitZMinimum();
        double rightFitZLimit = processingSettings.getFitZMaximum();

        int lowerLimitIndex = Double.isInfinite(rightFitZLimit) ? 0 :  deflectionChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(rightFitZLimit) - contactIndex - 1;
        int upperLimitZIndex = Double.isInfinite(leftFitZLimit) ? n - contactIndex : deflectionChannel.getIndexWithinDataBoundsOfItemWithXClosestTo(leftFitZLimit) - contactIndex;

        int upperLimitIndex = Math.min(upperLimitIndentationIndex, upperLimitZIndex);


        AnnotatedForceIndentation annotated = new HertzianAnnotatedForceIndentation(indentationValues,forceValues ,lowerLimitIndex, upperLimitIndex, contactPoint, baselineDeflection, springConstant);

        return annotated;
    }


}

