package atomicJ.readers.mdt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;

public class ScannedDataFrameRecord 
{
    private final AxisScale xAxisScale; //10 bytes
    private final AxisScale yAxisScale;
    private final AxisScale zAxisScale;

    private final MDTScanVariables scanVar;

    private ScannedDataFrameRecord(ByteBuffer buffer)
    {
        this.xAxisScale = AxisScale.readInInstance(buffer);
        this.yAxisScale = AxisScale.readInInstance(buffer);
        this.zAxisScale = AxisScale.readInInstance(buffer);
        this.scanVar = MDTScanVariables.readIn(buffer);
    }

    public static ScannedDataFrameRecord readIn(FileChannel channel, MDTFrameHeader frameHeader) throws UserCommunicableException
    {
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, frameHeader.getSizeOfVariables(), ByteOrder.LITTLE_ENDIAN);
        ScannedDataFrameRecord record = new ScannedDataFrameRecord(buffer);

        return record;
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

    public MDTScanVariables getScanVariables()
    {
        return scanVar;
    }
}
