package atomicJ.curveProcessing;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.IndexRange;
import atomicJ.data.Point1DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.statistics.LocalRegression;
import atomicJ.statistics.LocalRegressionWeightFunction;
import atomicJ.statistics.SpanGeometry;

public class LocalRegressionTransformation implements Channel1DDataInROITransformation
{   
    private final double span;
    private final SpanType spanType;
    private final int robustnessIterationsCount;
    private final int degree;
    private final int derivativeIndex;
    private final LocalRegressionWeightFunction weightFunction;
    private final SpanGeometry spanGeometry;

    public LocalRegressionTransformation(double span, SpanGeometry spanGeometry, SpanType spanType, int robustnessIterationsCount, int degree, int derivative, LocalRegressionWeightFunction weightFunction)
    {
        this.robustnessIterationsCount = robustnessIterationsCount;
        this.span = span;
        this.spanType = spanType;
        this.degree = degree;
        this.derivativeIndex = derivative;
        this.weightFunction = weightFunction;
        this.spanGeometry = spanGeometry;
    }

    @Override
    public Channel1DData transform(Channel1DData channel) 
    {                
        if(channel instanceof GridChannel1DData)
        {
            return transformGridChannel((GridChannel1DData)channel);
        }

        if(channel instanceof Point1DData)
        {
            return transformPointChannel((Point1DData)channel);
        }

        double[][] points = channel.getPoints();

        double[][] transformed = LocalRegression.smooth(points, spanGeometry, spanType.getSpanLengthInPoints(span, points.length), robustnessIterationsCount, 1e-6, degree, weightFunction);

        if(derivativeIndex > 0)
        {
            transformed = LocalRegression.smooth(points, spanGeometry, spanType.getSpanLengthInPoints(span, points.length), degree, derivativeIndex, weightFunction);
            return new FlexibleChannel1DData(transformed, channel.getXQuantity(), getQuantity(channel.getXQuantity(), channel.getYQuantity(), derivativeIndex), channel.getXOrder());
        }

        FlexibleChannel1DData channelData = new FlexibleChannel1DData(transformed, channel.getXQuantity(), channel.getYQuantity(), channel.getXOrder());
        return channelData;
    }

    private Quantity getQuantity(Quantity xQuantity, Quantity yQuantity, int derivativeIndex)
    { 
        PrefixedUnit yUnit = yQuantity.getUnit();
        PrefixedUnit xUnit = xQuantity.getUnit();

        Quantity derivativeQuantity = new UnitQuantity("Derivative", yUnit.divide(xUnit));
        return derivativeQuantity;
    }

    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
    }

    public Channel1DData transformGridChannel(GridChannel1DData gridChannel) 
    {               
        Grid1D grid = gridChannel.getGrid();
        double[] data = gridChannel.getData();

        int width = spanType.getSpanLengthInPoints(span, data.length);

        double[] transformed = null;

        if(robustnessIterationsCount == 0)
        {
            IndexRange halfBandwidths = spanGeometry.getKernelMinAndLength(width);

            int minIndex = halfBandwidths.getMinIndex();
            int kernelLength = halfBandwidths.getMaxIndex();

            Kernel1DSet<SavitzkyGolay1DKernel> kernelSet = SavitzkyGolay1DKernel.buildKernelSet(minIndex, kernelLength, degree, weightFunction, derivativeIndex);
            Channel1DDataInROITransformation tr = new SavitzkyGolay1DConvolution(kernelSet);

            return tr.transform(gridChannel);
        }
        else
        {            
            transformed = LocalRegression.smooth(data, grid.getIncrement(), spanGeometry, spanType.getSpanLengthInPoints(span, data.length), robustnessIterationsCount,
                    1e-6, degree, weightFunction);

            if(derivativeIndex > 0)
            {
                //exact fit, we do not want Savitzky filter to smooth more, but just to calculate the derivative
                Kernel1DSet<SavitzkyGolay1DKernel> kernelSet = SavitzkyGolay1DKernel.buildKernelSet(-degree/2, degree + 1, degree, derivativeIndex);
                Channel1DDataInROITransformation tr = new SavitzkyGolay1DConvolution(kernelSet);

                return tr.transform(new GridChannel1DData(transformed, gridChannel.getGrid(), gridChannel.getYQuantity()));
            }
        }

        GridChannel1DData channelData = new GridChannel1DData(transformed, grid, gridChannel.getYQuantity());

        return channelData;
    }

    @Override
    public Channel1DData transform(Channel1DData channel, ROI roi, ROIRelativePosition position) 
    {        
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        GridChannel1DData gridChannel = (channel instanceof GridChannel1DData) ? (GridChannel1DData)channel : GridChannel1DData.convert(channel);
        Grid1D grid = gridChannel.getGrid();
        double[] data = gridChannel.getData();

        int columnCount = grid.getIndexCount();

        double[] transformed = new double[columnCount];

        return null;
    }
}
