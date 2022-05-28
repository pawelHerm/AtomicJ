package atomicJ.readers.mdt;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.data.units.Units;
import atomicJ.utilities.FileInputUtilities;

public class MDACalibration 
{
    private final long nameSize; //4 bytes, Size of Name of Dimension/measurand
    private final long commentSize; //4 bytes, Size of Comment on physical  meaning of Dimension/measurand
    private final long unitsSize; //4 bytes, Size of Physical units of Dimension/measurand calibration
    private final long unitsCode; //8 bytes, physical units of Dimension/measurand calibration
    private final double accuracy; //8 bytes, Accuracy of calibration in physical units of Dimension/measurand calibration
    private final int funcId; //4 bytes, Identifier of calibration function

    private final int funcPointer;//4 bytes, Pointer to Dimensions of calibration function
    private final double bias;
    private final double scale;

    private final long minCalibrationIndex; //8 bytes, Min Index of Calibration (Zero Start)
    private final long maxCalibrationIndex; //8 bytes, MaxIndex of Calibration (MaxIndex-MinIndex+1 = Size)

    private final MDADataType dataType; //Type of Data (DigiType)
    private final long authorFieldSize;//4 bytes, Size of Author field

    private final String name;
    private Document comment;
    private final PrefixedUnit unit;
    private final String author;

    public MDACalibration(ByteBuffer buffer)
    {         
        this.nameSize = FileInputUtilities.getUnsigned(buffer.getInt());
        this.commentSize = FileInputUtilities.getUnsigned(buffer.getInt());
        this.unitsSize = FileInputUtilities.getUnsigned(buffer.getInt());
        this.unitsCode = buffer.getLong();
        this.accuracy = buffer.getDouble();
        this.funcId = buffer.getInt();
        this.funcPointer = buffer.getInt();
        this.bias = buffer.getDouble();
        this.scale = buffer.getDouble();

        this.minCalibrationIndex = buffer.getLong();
        this.maxCalibrationIndex = buffer.getLong();

        this.dataType = MDADataType.getMDADataType(buffer.getInt());
        this.authorFieldSize = FileInputUtilities.getUnsigned(buffer.getInt());

        //skips ADCDAC, ChannelID, ID
        FileInputUtilities.skipBytes(36, buffer);

        //        System.out.println("units Code " + unitsCode);
        //        System.out.println("data Type  " + dataType);
        //
        //        System.out.println("nameSize " + nameSize);
        //        System.out.println("commentSize " + commentSize);
        //        System.out.println("unitsSIze " + unitsSize);
        //        System.out.println("authorFieldSize " + authorFieldSize);

        this.name = nameSize > 0 ? FileInputUtilities.readInStringFromBytes((int)nameSize, buffer) : "";
        if(commentSize > 2)
        {
            try {
                this.comment = FileInputUtilities.readInXMLDocument2((int)commentSize, buffer);
                FileInputUtilities.printChildNodes(comment.getDocumentElement());

            } catch (ParserConfigurationException | SAXException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        this.unit = unitsSize > 0 ? UnitUtilities.getSIUnit(FileInputUtilities.readInStringFromBytes((int)unitsSize, buffer))
                : UnitCode.getUnitCode(unitsCode).getUnit();
        this.author = authorFieldSize > 0 ? FileInputUtilities.readInStringFromBytes((int)authorFieldSize, buffer) : "";

        //        System.out.println("!!! NAME !!! " + name);
        //        System.out.println("!!! UNIT PARSED !!! " +unit.getFullName());
        //        System.out.println("!!! UNIT PARSED CLASS !!! " +unit.getClass().getCanonicalName());
        //
        //        System.out.println("!!! AUTHOR !!! " + author);
    }

    public String getName()
    {
        return name;
    }

    public Document get«omment()
    {
        return comment;
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public long getArrayElementCount()
    {
        long count = maxCalibrationIndex - minCalibrationIndex + 1;
        return count;
    }

    public MDADataType getDataType()
    {
        return dataType;
    }

    public long getVariablePartSize()
    {
        long size = Math.max(0, nameSize) + Math.max(0, commentSize) + Math.max(0, unitsSize) + Math.max(0, authorFieldSize);
        return size;
    }

    public double getScale()
    {
        return scale;
    }

    public double getBias()
    {
        return bias;
    }

    private static enum UnitCode
    {
        NONE(0x0000000000000001L, SimplePrefixedUnit.getNullInstance()), METER(0x0000000000000101L, Units.METER_UNIT),
        AMPERE(0x0000000000100001L, Units.AMPERE_UNIT), VOLT(0x000000FFFD010200L, Units.VOLT_UNIT),
        SECOND(0x0000000001000001L, Units.SECOND_UNIT);

        private final long code;
        private final PrefixedUnit unit;

        UnitCode(long code, PrefixedUnit unit)
        {
            this.code = code;
            this.unit = unit;
        }

        public PrefixedUnit getUnit()
        {
            return unit;
        }

        public static UnitCode getUnitCode(long code)
        {
            for(UnitCode unitCode : UnitCode.values())
            {
                if(unitCode.code == code)
                {
                    return unitCode;
                }
            }

            return UnitCode.NONE;
        }
    }
}
