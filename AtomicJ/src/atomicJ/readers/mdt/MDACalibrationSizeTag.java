package atomicJ.readers.mdt;

import java.nio.ByteBuffer;

import atomicJ.utilities.FileInputUtilities;

public class MDACalibrationSizeTag
{
    public static final int BYTE_SIZE = 8;

    private final long totalCalibrationSize;//4 bytes, unsigned integer, Total calibration size with strings after record
    private final long calibrationStructureSize; //4 bytes, unsigned integer, Calibration structure size

    public MDACalibrationSizeTag(ByteBuffer buffer)
    {
        this.totalCalibrationSize = FileInputUtilities.getUnsigned(buffer.getInt());
        this.calibrationStructureSize = FileInputUtilities.getUnsigned(buffer.getInt());
    }

    public long getTotalCalibrationSize()
    {
        return totalCalibrationSize;
    }

    public long getCalibratonStructureSize()
    {
        return calibrationStructureSize;
    }
}