package atomicJ.curveProcessing;

import org.apache.commons.math3.util.ArithmeticUtils;

import atomicJ.data.Channel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.utilities.MathUtilities;

public class SavitzkyGolay1DConvolution implements Channel1DDataInROITransformation
{   
    private final Kernel1DSet<SavitzkyGolay1DKernel> kernel;

    public SavitzkyGolay1DConvolution(Kernel1DSet<SavitzkyGolay1DKernel> kernel)
    {
        this.kernel = kernel;
    }


    @Override
    public Point1DData transformPointChannel(Point1DData channel)
    {
        return channel;
    }


    @Override
    public Channel1DData transform(Channel1DData channel) 
    {        
        int derivativeIndex = kernel.getMainKernel().getDerivative();

        if(channel.getItemCount() < 3)
        {          
            return derivativeIndex > 0 ? channel.getCopy(1, getQuantity(channel.getXQuantity(), channel.getYQuantity(), derivativeIndex)) : channel;
        }

        GridChannel1DData gridChannel = (channel instanceof GridChannel1DData) ? (GridChannel1DData)channel : GridChannel1DData.convert(channel);
        Grid1D grid = gridChannel.getGrid();
        double[] data = gridChannel.getData();

        double increment = grid.getIncrement();

        int columnCount = grid.getIndexCount();      

        double factor = ArithmeticUtils.factorial(derivativeIndex)/MathUtilities.intPow(increment, derivativeIndex);

        double[] transformed = kernel.multiply(factor).convolve(data, columnCount);

        GridChannel1DData transformedChannel = (derivativeIndex > 0) ? new GridChannel1DData(transformed, grid, getQuantity(grid.getQuantity(), channel.getYQuantity(), derivativeIndex)) : new GridChannel1DData(transformed, grid, channel.getYQuantity());

        return transformedChannel;
    }

    private Quantity getQuantity(Quantity xQuantity, Quantity yQuantity, int derivativeIndex)
    { 
        PrefixedUnit yUnit = yQuantity.getUnit();
        PrefixedUnit xUnit = xQuantity.getUnit();

        Quantity derivativeQuantity = new UnitQuantity("Derivative", yUnit.divide(xUnit));

        return derivativeQuantity;
    }

    @Override
    public Channel1DData transform(Channel1DData channel, ROI roi, ROIRelativePosition position) 
    {
        if(ROIRelativePosition.EVERYTHING.equals(position))
        {
            return transform(channel);
        }

        return null;
    }
}
