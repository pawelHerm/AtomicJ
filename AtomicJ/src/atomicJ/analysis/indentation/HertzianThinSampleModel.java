package atomicJ.analysis.indentation;

import java.awt.geom.Point2D;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.analysis.ThinSampleModel;
import atomicJ.data.Channel1DData;
import atomicJ.data.IndexRange;

public abstract class HertzianThinSampleModel extends HertzianContactModel
{
    private final ThinSampleModel sample; 

    public HertzianThinSampleModel(ThinSampleModel sample, PrecontactInteractionsModel precontactModel) 
    {
        super(precontactModel);
        this.sample = sample;
    }

    @Override
    public IndexRange getRangeOfValidTrialContactPointIndices(Channel1DData deflectionChannel, Point2D recordingPoint, double springConstant)
    {
        double leniencyFactor = 2;
        double h = sample.getThickness(recordingPoint);
        double maxAllowedIndentation = leniencyFactor*h;
        int n = deflectionChannel.getItemCount();
        double[][] points = deflectionChannel.getPoints();

        int maxIndex = Math.max(0, n - 1);//in case there is only one point in the deflection channel

        for(int i = 0; i < n; i++)
        {
            double[] contactPoint = points[i];
            double maxIndent = getMaximalIndentationDepthUnderAssumptionOfIndentationMonotonicIncrease(deflectionChannel, contactPoint);
            if(maxIndent < maxAllowedIndentation)
            {
                return new IndexRange(i, maxIndex);
            }
        }

        return new IndexRange(Math.max(0, n - 1), maxIndex);
    }

    protected double[] convertIndentationData(double[] indentationValues, Point2D recordingPoint)
    {
        double h = sample.getThickness(recordingPoint);

        int n = indentationValues.length;
        double[] indentationValuesConverted = new double[n];
        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            indentationValuesConverted[i] = getForceModulusRatio(indent, h);
        }

        return indentationValuesConverted;
    }

    protected double[] transformIndentationData(double[] indentationValues, Point2D recordingPoint)
    {
        double h = sample.getThickness(recordingPoint);

        int n = indentationValues.length;
        for(int i = 0; i<n; i++)
        {
            double indent = indentationValues[i];
            indentationValues[i] = getForceModulusRatio(indent, h);
        }

        return indentationValues;
    }

    @Override
    public double getPostcontactObjectiveFunctionMinimum(double[] postcontactForceSeparationYs, double[] postcontactForceSeparationXs, Point2D recordingPoint, RegressionStrategy regressionStrategy)
    {
        double objectiveFunctionMinimum = regressionStrategy.getObjectiveFunctionMinimumForThroughOriginLinearRegression(postcontactForceSeparationYs,transformIndentationData(postcontactForceSeparationXs, recordingPoint));
        return objectiveFunctionMinimum;
    }

    public double computePointwiseModulus(double indent, double force, double h)
    {
        double ratio = getForceModulusRatio(indent, h);
        double modulus = force/ratio;
        return modulus;
    }

    @Override
    public SampleModel getSampleModel()
    {
        return sample;
    }

    public double getPoissonRatio()
    {
        return sample.getPoissonRatio();
    }

    public double getSampleThickness(Point2D p)
    {
        return sample.getThickness(p);
    }

    public boolean isSampleBondedToSubstrate()
    {
        return sample.isBondedToSubstrate();
    }

    protected abstract double getForceModulusRatio(double indent, double h);
}
