package atomicJ.readers.mi;

import atomicJ.data.Channel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.sources.SimpleSpectroscopySource;

public class MIChunk
{
    private final int count;
    private final double start;
    private final double increment;
    private final Integer sourceKey;

    private double[] deflectionData;
    private double[] amplitudeData;
    private double[] phaseData;

    private final String channelIdentifier;

    MIChunk(int count, double start, double increment, Integer curveKey)
    {
        this.count = count;
        this.start = start;
        this.increment = increment;
        this.sourceKey = curveKey;
        this.channelIdentifier = increment < 0 ? SimpleSpectroscopySource.APPROACH : SimpleSpectroscopySource.WITHDRAW;
    }

    MIChunk copy(Integer newCurveKey)
    {
        MIChunk newChunk = new MIChunk(count, start, increment, newCurveKey);
        return newChunk;
    }

    int getCount()
    {
        return count;
    }

    double getStart()
    {
        return start;
    }

    double getIncrement()
    {
        return increment;
    }

    double[] initializeAndGetDeflectionData()
    {
        this.deflectionData = new double[count];
        return deflectionData;
    }

    double[] initializeAndGetAmplitudeData()
    {
        this.amplitudeData = new double[count];
        return amplitudeData;
    }

    double[] initializeAndGetPhaseData()
    {
        this.phaseData = new double[count];
        return phaseData;
    }

    Channel1DData getDeflectionData(Quantity deflectionQuantity)
    {
        Grid1D grid = new Grid1D(increment, start, count, Quantities.DISTANCE_MICRONS);
        Channel1DData gridChannel = new GridChannel1DData(deflectionData, grid,deflectionQuantity);
        return gridChannel;
    }

    Channel1DData getAmplitudeData(Quantity amplitudeQuantity)
    {
        Grid1D grid = new Grid1D(increment, start, count, Quantities.DISTANCE_MICRONS);
        Channel1DData gridChannel = new GridChannel1DData(amplitudeData, grid,amplitudeQuantity);
        return gridChannel;
    }

    Channel1DData getPhaseData()
    {
        Grid1D grid = new Grid1D(increment, start, count, Quantities.DISTANCE_MICRONS);
        Channel1DData gridChannel = new GridChannel1DData(phaseData, grid,Quantities.PHASE_DEGREES);

        return gridChannel;
    }

    boolean isApproach()
    {
        return increment<0;
    }
}