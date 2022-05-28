package atomicJ.analysis;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.SortX1DTransformation;
import atomicJ.curveProcessing.SpanType;
import atomicJ.data.Channel1DData;
import atomicJ.statistics.LocalRegression;
import atomicJ.statistics.LocalRegressionWeightFunction;

public class UnloaddingStiffnessLoessEstimator implements StiffnessEstimator
{
    private final Channel1DDataTransformation sortingTransformation = new SortX1DTransformation(SortedArrayOrder.ASCENDING); 
    private final LocalRegressionWeightFunction weightFunction;
    private final int degree;
    private final double span;
    private final SpanType spanType;

    public UnloaddingStiffnessLoessEstimator(double span, SpanType spanType, int degree, LocalRegressionWeightFunction weightFunction)
    {        
        this.weightFunction = weightFunction;
        this.degree = degree;
        this.span = span;
        this.spanType = spanType;
    }

    @Override
    public double getEstimate(Channel1DData forceIndentationData) 
    {   
        if(forceIndentationData == null || forceIndentationData.isEmpty())
        {
            return Double.NaN;
        }

        int n = forceIndentationData.getItemCount();
        Channel1DData forceIndentationSorted = sortingTransformation.transform(forceIndentationData);     

        int spanInPoints = Math.min(spanType.getSpanLengthInPoints(span, n), forceIndentationSorted.getItemCount());

        double[][] data = forceIndentationSorted.getPointsCopy(n - spanInPoints, n);

        double[] derPoint = LocalRegression.getDerivative(data, data.length - 1, data.length - spanInPoints,  data.length - 1, degree, 1, weightFunction);

        double stiffness = derPoint[derPoint.length - 1]/1000.;

        return stiffness;
    }
}