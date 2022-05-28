package atomicJ.readers.mdt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;

public class MDAHeader 
{
    public static final int BYTE_SIZE = 33;

    private final long arraySize; //Number of Elements (Measurand Vectors) in MDA
    private final int measurandVectorSizeInBytes; //Number of Elements (Measurand Vectors) in MDA
    private final int dimensionsCount; //Number of MDA Dimensions
    private final int measurandVectorElementCount; //Number of Measurands - elements in Measurand Vector
    private final int pDimensions; //Pointer to the array of Dimension PhRecords
    private final int pMeasurands; //Pointer to the array of Measurand PhRecords
    private final int pData; //Pointer to the MDA Data

    private List<MDACalibration> measurandCalibrations;
    private List<MDACalibration> dimensionCalibrations;

    //DataOwner, 1 byte, reserved

    private MDAHeader(ByteBuffer buffer)
    {
        this.arraySize = buffer.getLong();
        this.measurandVectorSizeInBytes = buffer.getInt();
        this.dimensionsCount = buffer.getInt();
        this.measurandVectorElementCount = buffer.getInt();
        this.pDimensions = buffer.getInt();
        this.pMeasurands = buffer.getInt();
        this.pData = buffer.getInt();
    }

    public static MDAHeader readIn(FileChannel channel) throws UserCommunicableException
    {
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, MDAHeader.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN);
        MDAHeader header = new MDAHeader(buffer);
        return header;
    }

    public void readInCalibrations(FileChannel channel) throws UserCommunicableException
    {
        this.dimensionCalibrations = new ArrayList<>();

        for(int i = 0; i<dimensionsCount;i++)
        { 
            MDACalibrationSizeTag sizeTag = new MDACalibrationSizeTag(FileInputUtilities.readBytesToBuffer(channel, MDACalibrationSizeTag.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN));

            MDACalibration calibration = new MDACalibration(FileInputUtilities.readBytesToBuffer(channel, (int) sizeTag.getTotalCalibrationSize() - MDACalibrationSizeTag.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN));               
            dimensionCalibrations.add(calibration);
        }

        this.measurandCalibrations = new ArrayList<>();

        for(int i = 0; i<measurandVectorElementCount;i++)
        {                
            MDACalibrationSizeTag sizeTag = new MDACalibrationSizeTag(FileInputUtilities.readBytesToBuffer(channel, MDACalibrationSizeTag.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN));

            MDACalibration calibration = new MDACalibration(FileInputUtilities.readBytesToBuffer(channel, (int) sizeTag.getTotalCalibrationSize() - MDACalibrationSizeTag.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN));               
            measurandCalibrations.add(calibration);
        }
    }

    public List<MDACalibration> getDimensionCalibrations()
    {
        return Collections.unmodifiableList(dimensionCalibrations);
    }

    public List<MDACalibration> getMeasurandCalibrations()
    {
        return Collections.unmodifiableList(measurandCalibrations);
    }

    public long getMeasurandVectorCount()
    {
        return arraySize;
    }

    public int getMeasurandElementCount()
    {
        return measurandVectorElementCount;
    }

    public int getMeasurandVectorSizeInBytes()
    {
        return measurandVectorSizeInBytes;
    }

    public int getDimensionsCount()
    {
        return dimensionsCount;
    }
}
