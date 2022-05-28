package atomicJ.readers.gwyddionGwy;

import java.nio.ByteBuffer;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;

public class GwySIUnit
{
    public static final String GWY_SI_UNIT_NAME = "GwySIUnit";

    private static final String UNIT_STRING_COMPONENT = "unitstr";

    private PrefixedUnit unit;

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    static GwySIUnit readInObject(ByteBuffer byteBuffer) throws UserCommunicableException
    {
        FileInputUtilities.readInNullTerminatedString(byteBuffer); //reads in object name
        GwySIUnit unit = readInObjectExceptForName(byteBuffer);
        return unit;
    }

    private static GwySIUnit readInObjectExceptForName(ByteBuffer byteBuffer) throws UserCommunicableException
    {
        GwySIUnit unitObject = new GwySIUnit();

        long dataSize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
        int finalPosition = byteBuffer.position() + (int)dataSize;

        while(byteBuffer.position() < finalPosition)
        {
            String componentNameTrimmed = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
            char componentTypeChar = (char)byteBuffer.get();

            if(UNIT_STRING_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.STRING.typeChar == componentTypeChar)
            {
                String unitString = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
                unitObject.unit = UnitUtilities.getSIUnit(unitString);
            }
            else
            {
                GwyDataType.getDataType(componentTypeChar).skipData(byteBuffer);
            }
        }

        return unitObject;
    }
}