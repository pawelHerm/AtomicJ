package atomicJ.readers.gwyddionGwy;

import java.nio.ByteBuffer;

import atomicJ.utilities.FileInputUtilities;

public enum GwyDataTypeSimple implements GwyDataType
{        
    BOOLEAN('b', 1), CHARACTER('c', 1), INTEGER_32_BIT('i', 4), INTEGER_64_BIT('q',8), DOUBLE('d',8), STRING('s',-1) {
        @Override
        public void skipData(ByteBuffer byteBuffer) 
        {
            FileInputUtilities.readInNullTerminatedString(byteBuffer);
        }
    }, 

    OBJECT('o',-1) {
        @Override
        public void skipData(ByteBuffer byteBuffer) 
        {
            FileInputUtilities.readInNullTerminatedString(byteBuffer);//skips object name
            long dataSize = FileInputUtilities.getUnsigned(byteBuffer.getInt());

            int position = byteBuffer.position();
            byteBuffer.position(position + (int)dataSize);                
        }
    }, 
    ;

    final char typeChar;
    private final int byteSize;

    GwyDataTypeSimple(char typeChar, int byteSize)
    {
        this.typeChar = typeChar;
        this.byteSize = byteSize;
    }

    @Override
    public char getTypeChar()
    {
        return typeChar;
    }

    @Override
    public void skipData(ByteBuffer byteBuffer) 
    {
        int currentPosition = byteBuffer.position();
        byteBuffer.position(currentPosition + this.byteSize);                  
    }

    static GwyDataTypeSimple getDataType(char dataTypeChar)
    {
        for(GwyDataTypeSimple dataType : values())
        {
            if(dataType.typeChar == dataTypeChar)
            {
                return dataType;
            }
        }
        return null;
    }
}