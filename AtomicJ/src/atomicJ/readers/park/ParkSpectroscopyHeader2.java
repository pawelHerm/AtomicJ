package atomicJ.readers.park;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import atomicJ.data.Grid2D;
import atomicJ.data.Quantities;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.utilities.FileInputUtilities;

public class ParkSpectroscopyHeader2 extends ParkSpectroscopyHeader //952 + 330 = 1282
{
    private final int spectPointPerX;   // If volume_image && spec_point_per_x == 0, then
    //points are arranged in a square matrix 
    private final boolean hasReferenceImage;
    private final double xScanSize;     // Grid dimensions 
    private final double yScanSize;
    private final double xOffset;
    private final double yOffset;
    private final double forceConstant;      // F-D, in Newton/meter 
    private final double sensitivity;         // F-D, in Volt/micrometer 
    private final double forceLimit;         // F-D, in Volts 
    private final double timeInterval;       // F-D, in seconds? 
    private final double maxVoltage;         // I-V, in Volts 
    private final double minVoltage;         // I-V, in Volts 
    private final double startVoltage;       // I-V, in Volts 
    private final double endVoltage;         // I-V, in Volts 
    private final double delayedStartTime;  // I-V, in seconds? 
    private final boolean zServo;            // I-V
    private final double dataGain;
    private final PrefixedUnit wUnit;
    private final boolean useExtendedHeader;
    private final ParkSpectroscopyType spectType;
    private final double resetLevel;           // Photo-current information 
    private final double resetDuration;
    private final double operationLevel;
    private final double operationDuration;
    private final double timeBeforeReset;
    private final double timeAfterReset;
    private final double timeBeforeLightOn;
    private final double timeLightDuration;

    private final List<Point2D> curvePoints = new ArrayList<>();


    private ParkSpectroscopyHeader2(ByteBuffer buffer)
    {
        super(buffer);

        this.spectPointPerX = buffer.getInt();   // If volume_image && spec_point_per_x == 0, then
        //points are arranged in a square matrix 
        this.hasReferenceImage = (buffer.getInt() != 0);

        this.xScanSize = buffer.getDouble();     // Grid dimensions //16
        this.yScanSize = buffer.getDouble();//24
        this.xOffset = buffer.getDouble();//32
        this.yOffset = buffer.getDouble();//40


        // size = 2 + 2 + 1 + 1 = "6 ints"

        this.forceConstant = buffer.getDouble();      // F-D, in Newton/meter  //48    
        this.sensitivity = 1./buffer.getDouble();         // F-D, values are in Volt/micrometer, so I convert them to micrometer/V, which is the way of presenting sensitivity in AtomicJ //56
        this.forceLimit = buffer.getFloat();         // F-D, in Volts 
        this.timeInterval = buffer.getFloat();       // F-D, 

        // I/V Spectroscopy Information
        // size = 1 + 1 + 1 + 1 + 1 + 1 = "6 ints"

        this.maxVoltage = buffer.getFloat();         // I-V, in Volts 
        this.minVoltage = buffer.getFloat();         // I-V, in Volts 
        this.startVoltage = buffer.getFloat();       // I-V, in Volts //96
        this.endVoltage = buffer.getFloat();         // I-V, in Volts  102
        this.delayedStartTime = buffer.getFloat();  // I-V, in seconds?  110
        this.zServo = (buffer.getInt() != 0);            // I-V 114


        this.dataGain = buffer.getDouble(); //122

        String w_unitString = FileInputUtilities.readInString(8, buffer).trim(); //138, 16 bytes read in
        this.wUnit = UnitUtilities.getSIUnit(w_unitString);

        this.useExtendedHeader = (buffer.getInt() != 0); //142
        this.spectType = ParkSpectroscopyType.getParkSpectroscopyType(buffer.getInt()); //146

        // Photo-current information , size 8*4 bytes
        this.resetLevel = buffer.getFloat();           
        this.resetDuration = buffer.getFloat();
        this.operationLevel = buffer.getFloat();
        this.operationDuration = buffer.getFloat();
        this.timeBeforeReset = buffer.getFloat(); 
        this.timeAfterReset = buffer.getFloat(); //194
        this.timeBeforeLightOn = buffer.getFloat(); //202
        this.timeLightDuration = buffer.getFloat(); //210 + 120 reserved = 330



        // TA Information
        // size = "5 ints", changes introduced 2011.01.14
        float fOffsetTemperature =  buffer.getFloat();
        float fOffsetSThMError  = buffer.getFloat();
        float fReferenceTemperature  = buffer.getFloat();
        float fReferenceProbeCurrent = buffer.getFloat();
        float fReferenceSThMError = buffer.getFloat();

        //field added 2011.07.28
        int nDataType = buffer.getInt(); // 0 = 16bit short, 1 = 32bit int, 2 = 32bit float

        FileInputUtilities.skipBytes(24*4, buffer);


        List<Point2D> curvePoints = new ArrayList<>();
        for(int i = 0; i<getRecordingPointCount() + 2;i++)
        {
            double x = buffer.getFloat();
            double y = buffer.getFloat();

            curvePoints.add(new Point2D.Double(x, y));
            buffer.getFloat(); //we skip time
        }              

        //       int Reserved[24];

    }

    @Override
    public boolean isWellSpecifiedGridMap()
    {
        boolean gridMap = isVolumeImage() && this.spectPointPerX != 0 && this.xScanSize != 0 && this.yScanSize != 0;
        return gridMap;
    }

    @Override
    public Grid2D buildGrid()
    {
        if(!isWellSpecifiedGridMap())
        {
            return null;
        }

        int columnCount = this.spectPointPerX;        

        int itemCount = getRecordingPointCount();
        int rowCount = itemCount/columnCount;

        double xIncrement = this.xScanSize/(columnCount - 1);
        double yIncrement = this.yScanSize/(rowCount - 1);

        Grid2D grid = new Grid2D(xIncrement, yIncrement, 0, 0, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

        return grid;
    }

    @Override
    public boolean hasReferenceImage()
    {
        return hasReferenceImage;
    }

    public Point2D getCurvePoint(int index)
    {
        return curvePoints.get(index);
    }

    //cantilever spring constant in N/m
    @Override
    public double getSpringConstant()
    {
        return forceConstant;
    }

    //sensitivity in um/V
    @Override
    public double getSensitivity()
    {
        return sensitivity;
    }

    public boolean arePointsArrangedInSquareArray()
    {
        boolean squareArray = isVolumeImage() && (spectPointPerX == 0);
        return squareArray;
    }

    public static ParkSpectroscopyHeader2 readIn(ByteBuffer buffer)
    {
        return new ParkSpectroscopyHeader2(buffer);
    }
}
