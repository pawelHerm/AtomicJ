package atomicJ.readers.gwyddionGwy;

import java.nio.ByteBuffer;

import atomicJ.utilities.FileInputUtilities;

public enum GwyDataTypeArray implements GwyDataType
{             
    BOOLEAN_ARRAY('B', 1), CHARACTER_ARRAY('C', 1), INTEGER_32_BIT_ARRAY('I', 4), INTEGER_64_BIT_ARRAY('Q', 8), DOUBLE_ARRAY('D', 8), 
    STRING_ARRAY('S', -1)
    {   
        @Override
        public void skipData(ByteBuffer byteBuffer) 
        {   
            long arraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
            for(long i = 0; i<arraySize;i++)
            {
                FileInputUtilities.readInNullTerminatedString(byteBuffer);
            }
        }
    }, OBJECT_ARRAY('O', - 1) 
    {            
        @Override
        public void skipData(ByteBuffer byteBuffer) 
        {   
            long arraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
            for(long i = 0; i<arraySize;i++)
            {
                GwyDataTypeSimple.OBJECT.skipData(byteBuffer);
            }
        }           
    };

    private final char typeChar;
    private final int itemSize;

    GwyDataTypeArray(char typeChar, int itemSize)
    {
        this.typeChar = typeChar;
        this.itemSize = itemSize;
    }

    @Override
    public char getTypeChar()
    {
        return typeChar;
    }

    public int getItemSize()
    {
        return itemSize;
    }

    @Override
    public void skipData(ByteBuffer byteBuffer) 
    {   
        long arraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
        int position = byteBuffer.position();
        byteBuffer.position(position + this.itemSize*(int)arraySize);                

    }

    private static GwyDataTypeArray getDataType(char dataTypeChar)
    {
        for(GwyDataTypeArray dataType : values())
        {
            if(dataType.typeChar == dataTypeChar)
            {
                return dataType;
            }
        }
        return null;
    }
}