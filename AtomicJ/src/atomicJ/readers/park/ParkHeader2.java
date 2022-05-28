package atomicJ.readers.park;

import java.nio.ByteBuffer;

import atomicJ.utilities.FileInputUtilities;

public class ParkHeader2 extends ParkHeader
{
    private final double zServoGain;
    private final double zScannerRange;
    private final String xyVoltageMode; 
    private final String zVoltageMode;     
    private final String xyServoMode;    
    private final ParkDataType dataType;
    private final double ncmAmplitude;
    private final double ncmFrequency;
    private final double headRotationAngle;
    private final String cantileverName;  //[16] 

    private ParkHeader2(ByteBuffer buffer)
    {
        super(buffer);

        this.zServoGain = buffer.getInt();
        this.zScannerRange = buffer.getInt();
        this.xyVoltageMode = FileInputUtilities.readInString(8, buffer);
        this.zVoltageMode = FileInputUtilities.readInString(8, buffer);
        this.xyServoMode = FileInputUtilities.readInString(8, buffer);
        this.dataType = ParkDataType.getParkDataType(buffer.getInt());

        FileInputUtilities.skipBytes(8, buffer); //skips reserved1 and reserved2

        this.ncmAmplitude = buffer.getDouble();
        this.ncmFrequency = buffer.getDouble();
        this.headRotationAngle = buffer.getDouble();
        this.cantileverName = FileInputUtilities.readInString(16, buffer);
    }

    @Override
    public ParkDataType getParkDataType()
    {
        return dataType;
    }

    public static ParkHeader2 readIn(ByteBuffer buffer)
    {
        return new ParkHeader2(buffer);
    }
}