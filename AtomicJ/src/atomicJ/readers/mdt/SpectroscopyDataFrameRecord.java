package atomicJ.readers.mdt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;

public class SpectroscopyDataFrameRecord
{
    private final AxisScale xAxisScale;
    private final AxisScale yAxisScale;
    private final AxisScale zAxisScale;

    private final int mode; //2 bytes, uint, Spectroscopy mode.
    private final int filter; //2 bytes, uint, adc filter
    private final double begin; //4 bytes, float, spec Ut : begin
    private final double end;//4 bytes, float, spec Ut : end
    private final int zUp;//2 bytes, Z diap for It(z)
    private final int zDown;//2 bytes, Z diap for It(z)
    private final int spAveraging; //2 bytes, uint, averaging
    private final boolean spRepeat;
    private final boolean spBack;
    private char spn4; //!!! only one byte should be read !!!!!
    private double sp4x0;
    private double sp4xr;
    private int sp4u;
    private int sp4i;
    private int spnx;
    private final char[] sp_reserved = new char[3]; ///!!! only three bytes should be read !!!!
    private char spEv2; //only one byte should be read

    private SpectroscopyDataFrameRecord(ByteBuffer buffer)
    {
        this.xAxisScale = AxisScale.readInInstance(buffer);
        this.yAxisScale = AxisScale.readInInstance(buffer);
        this.zAxisScale = AxisScale.readInInstance(buffer);

        this.mode = FileInputUtilities.getUnsigned(buffer.getShort());
        this.filter = FileInputUtilities.getUnsigned(buffer.getShort());
        this.begin = buffer.getFloat();
        this.end = buffer.getFloat();
        this.zUp = buffer.getShort();
        this.zDown = buffer.getShort();
        this.spAveraging = FileInputUtilities.getUnsigned(buffer.getShort());
        this.spRepeat = (buffer.get() != 0);
        this.spBack = (buffer.get() != 0);
    }

    public AxisScale getXAxisScale()
    {
        return xAxisScale;
    }

    public AxisScale getYAxisScale()
    {
        return yAxisScale;
    }

    public AxisScale getZAxisScale()
    {
        return zAxisScale;
    }

    public static SpectroscopyDataFrameRecord readIn(FileChannel channel, MDTFrameHeader frameHeader) throws UserCommunicableException
    {
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, frameHeader.getSizeOfVariables(), ByteOrder.LITTLE_ENDIAN);
        SpectroscopyDataFrameRecord frame = new SpectroscopyDataFrameRecord(buffer);

        return frame;
    }
}
