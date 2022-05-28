package atomicJ.readers.park;

import java.nio.ByteBuffer;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.utilities.FileInputUtilities;

public class ParkSpectroscopyChannel //complete size is 112
{
    private final int channelIndex;

    private final String sourceName; //64 bytes
    private final PrefixedUnit unit; //16 bytes
    private final double dataGain; //w sumie 88
    private final boolean xAxisSource; //92
    private final boolean yAxisSource; //96

    private double offset; //104
    private boolean logScale; //108
    private boolean square; //112

    private ParkSpectroscopyChannel(ByteBuffer buffer, int channelIndex)
    {
        this.channelIndex = channelIndex;

        this.sourceName = FileInputUtilities.readInString(32, buffer).trim();
        String wUnitName = FileInputUtilities.readInString(8, buffer).trim();
        this.unit = UnitUtilities.getSIUnit(wUnitName);

        this.dataGain = buffer.getDouble();

        this.xAxisSource = (buffer.getInt() != 0);
        this.yAxisSource = (buffer.getInt() != 0);
    }

    public static ParkSpectroscopyChannel readIn(ByteBuffer buffer, int channelIndex)
    {
        return new ParkSpectroscopyChannel(buffer, channelIndex);
    }

    public int getChannelIndex()
    {
        return channelIndex;
    }

    public boolean isXAxisSource()
    {
        return xAxisSource;
    }

    public boolean isYAxisSource()    
    {
        return yAxisSource;
    }

    public double getDataGain()
    {
        return dataGain;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public void setOffset(double offset)
    {
        this.offset = offset;
    }

    public void setLogScale(boolean logScale)
    {
        this.logScale = logScale;
    }

    public void setSquare(boolean square)
    {
        this.square = square;
    }

}