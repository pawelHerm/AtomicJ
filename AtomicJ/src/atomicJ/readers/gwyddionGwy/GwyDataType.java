package atomicJ.readers.gwyddionGwy;

import java.nio.ByteBuffer;

import atomicJ.gui.UserCommunicableException;

public interface GwyDataType
{
    public void skipData(ByteBuffer byteBuffer);
    public char getTypeChar();

    public static GwyDataType getDataType(char dataTypeChar) throws UserCommunicableException
    {
        GwyDataType dataType = null;
        if(Character.isLowerCase(dataTypeChar))
        {
            dataType = GwyDataTypeSimple.getDataType(dataTypeChar);
        }
        else if(Character.isUpperCase(dataTypeChar))
        {
            dataType = GwyDataTypeSimple.getDataType(dataTypeChar);
        }
        if(dataType != null)
        {
            return dataType;
        }

        throw new UserCommunicableException("No GwyDataType known for the character " + dataTypeChar);
    }
}