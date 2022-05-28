package atomicJ.readers.park;

import java.nio.ByteBuffer;

import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitUtilities;
import atomicJ.data.units.Units;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.utilities.FileInputUtilities;

public class ParkHeader
{
    private final ParkImageType imageType;
    private final String sourceName;
    private final String imageMode;
    private final double lpfStrength; //low pass filter strength
    private final boolean autoflatten;
    private final boolean acTrack;
    private final long columnCount;
    private final long rowCount;
    private final double angle;
    private final boolean sineScan;
    private final double overscanRate; //in percents
    private final boolean forward;
    private final boolean scanUp;
    private final boolean swapXY;

    private final UnitExpression xLength;
    private final UnitExpression yLength;
    private final UnitExpression xOffset;
    private final UnitExpression yOffset;

    private final double scanRate; //rows per second
    private final UnitExpression setPoint; //Error signal set point 

    private final double tipBias; // in volts
    private final double sampleBias; // in volts
    private final double dataGain;

    private final double zScale;
    private final double zOffset;
    private final PrefixedUnit zUnit;

    private final double dataMin;
    private final double dataMax;
    private final double dataAvg;

    private final boolean compression;
    private final boolean logScale;
    private final boolean square;

    protected ParkHeader(ByteBuffer buffer)
    {
        this.imageType = ParkImageType.getParkImageType(buffer.getInt());

        this.sourceName = FileInputUtilities.readInString(32, buffer).trim();

        this.imageMode = FileInputUtilities.readInString(8, buffer).trim();
        this.lpfStrength = buffer.getDouble();
        this.autoflatten = (buffer.getInt() != 0);
        this.acTrack = (buffer.getInt() != 0);
        this.columnCount = FileInputUtilities.getUnsigned(buffer.getInt());
        this.rowCount = FileInputUtilities.getUnsigned(buffer.getInt());
        this.angle = buffer.getDouble();
        this.sineScan = (buffer.getInt() != 0);
        this.overscanRate = buffer.getDouble();

        this.forward = (buffer.getInt() != 0);
        this.scanUp = (buffer.getInt() != 0);

        this.swapXY = (buffer.getInt() != 0);


        this.xLength = new UnitExpression(buffer.getDouble(), Units.MICRO_METER_UNIT);
        this.yLength = new UnitExpression(buffer.getDouble(), Units.MICRO_METER_UNIT);

        this.xOffset = new UnitExpression(buffer.getDouble(), Units.MICRO_METER_UNIT);
        this.yOffset = new UnitExpression(buffer.getDouble(), Units.MICRO_METER_UNIT);

        this.scanRate = buffer.getDouble();

        double setPointValue = buffer.getDouble();

        String setPointUnitName = FileInputUtilities.readInString(8, buffer);
        PrefixedUnit setPointUnit = UnitUtilities.getSIUnit(setPointUnitName);

        this.setPoint = new UnitExpression(setPointValue, setPointUnit);

        this.tipBias = buffer.getDouble();
        this.sampleBias = buffer.getDouble();
        this.dataGain = buffer.getDouble();

        this.zScale = buffer.getDouble();
        this.zOffset = buffer.getDouble();

        String zUnitString = FileInputUtilities.readInString(8, buffer).trim();

        this.zUnit = UnitUtilities.getSIUnit(zUnitString);

        this.dataMin = buffer.getDouble();
        this.dataMax = buffer.getDouble();
        this.dataAvg = buffer.getDouble();

        this.compression = (buffer.getInt() != 0);
        this.logScale = (buffer.getInt() != 0);
        this.square = (buffer.getInt() != 0);
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public double getZScale()
    {
        return zScale;
    }

    public double getZOffset()
    {
        return zOffset;
    }

    public double getDataGain()
    {
        return dataGain;
    }

    public PrefixedUnit getZUnit()
    {
        return zUnit;
    }

    public Grid2D getGrid()
    {
        double incrementX = xLength.getValue()/(columnCount - 1);
        double incrementY = yLength.getValue()/(rowCount - 1);
        Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, (int)rowCount, (int)columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);
        return grid;
    }

    public ParkDataType getParkDataType()
    {
        return ParkDataType.INT16;
    }

    public ParkImageType getParkImageType()
    {
        return imageType;
    }

    public DoubleArrayReaderType getDataReader()
    {
        return getParkDataType().getArrayReaderType();
    }

    public static ParkHeader readIn(ByteBuffer buffer)
    {
        return new ParkHeader(buffer);
    }
}