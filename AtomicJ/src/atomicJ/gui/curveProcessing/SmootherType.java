package atomicJ.gui.curveProcessing;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.curveProcessing.Kernel1DSet;
import atomicJ.curveProcessing.LocalRegressionTransformation;
import atomicJ.curveProcessing.SavitzkyGolay1DConvolution;
import atomicJ.curveProcessing.SavitzkyGolay1DKernel;
import atomicJ.curveProcessing.SpanType;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;

public enum SmootherType
{
    SAVITZKY_GOLAY("Savitzky-Golay") 
    {
        @Override
        public Channel1DDataTransformation getSmoothingTransformation(BasicSmootherModel model) 
        {
            int degree = Double.valueOf(Math.rint(model.getSavitzkyDegree().doubleValue())).intValue();
            int halfWidth = Double.valueOf(Math.rint(model.getSavitzkySpan())).intValue();

            Kernel1DSet<SavitzkyGolay1DKernel> kernelSet = SavitzkyGolay1DKernel.buildKernelSet(-halfWidth, 2*halfWidth + 1, degree, 0);

            Channel1DDataTransformation smoother = new SavitzkyGolay1DConvolution(kernelSet);
            return smoother;
        }

        @Override
        public boolean isInputProvided(BasicSmootherModel model) 
        {
            boolean isWindowWidthSpecified = !Double.isNaN(model.getSavitzkySpan());
            boolean isDegreeSpecified = !Double.isNaN(model.getSavitzkyDegree().doubleValue());

            boolean inputProvided = isWindowWidthSpecified && isDegreeSpecified;
            return inputProvided;
        }
    }, 

    LOCAL_REGRESSION("Local regression")
    {
        @Override
        public Channel1DDataTransformation getSmoothingTransformation(BasicSmootherModel model)
        {
            double span = model.getLoessSpan();
            int iter = Double.valueOf(Math.rint(model.getLoessIterations().doubleValue())).intValue();

            Channel1DDataTransformation smoother = new LocalRegressionTransformation(span, SpanGeometry.NEAREST_NEIGHBOUR, SpanType.POINT_FRACTION, iter, 1, 0, LocalRegressionWeightFunction.TRICUBE);
            return smoother;
        }

        @Override
        public boolean isInputProvided(BasicSmootherModel model) 
        {
            boolean isSpanSpecified = !Double.isNaN(model.getLoessSpan());
            boolean areIterationsSpecified = !Double.isNaN(model.getLoessIterations().doubleValue());
            boolean inputProvided = isSpanSpecified && areIterationsSpecified;

            return inputProvided;
        }
    };

    private final String prettyName;

    SmootherType(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public abstract Channel1DDataTransformation getSmoothingTransformation(BasicSmootherModel model);
    public abstract boolean isInputProvided(BasicSmootherModel model);

    @Override
    public String toString()
    {
        return prettyName;
    }

    public static interface BasicSmootherModel
    {
        public void setLoessSpan(double loessSpanNew);
        public double getLoessSpan();
        public void setLoessIterations(Number loessIterationsNew);
        public Number getLoessIterations();
        public void setSavitzkyDegree(Number savitzkyDegreeNew);
        public Number getSavitzkyDegree();
        public void setSavitzkySpan(double savitzkySpanNew);
        public double getSavitzkySpan();
    }
}