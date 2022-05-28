package atomicJ.readers.park;

import atomicJ.readers.DoubleArrayReaderType;

public enum ParkDataType
{
    INT16(0, DoubleArrayReaderType.INT16),INT32(1, DoubleArrayReaderType.INT32),FLOAT32(2,DoubleArrayReaderType.FLOAT32);

    private final int code;
    private final DoubleArrayReaderType reader;

    ParkDataType(int code, DoubleArrayReaderType reader)
    {
        this.code = code;
        this.reader = reader;
    }

    public DoubleArrayReaderType getArrayReaderType()
    {
        return reader;
    }

    public int getByteSize()
    {
        return reader.getByteSize();
    }

    public static ParkDataType getParkDataType(int code)
    {
        for(ParkDataType type : ParkDataType.values())
        {
            if(type.code == code)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No ParkDataType corresponds to the code " + code);
    }
}