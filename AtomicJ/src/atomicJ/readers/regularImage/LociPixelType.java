package atomicJ.readers.regularImage;

import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IntArrayReaderType;
import loci.formats.FormatTools;

public enum LociPixelType 
{
    BIT(FormatTools.BIT, IntArrayReaderType.INT2, null), INT8(FormatTools.INT8, IntArrayReaderType.INT8, DoubleArrayReaderType.INT8), 
    UNIT8(FormatTools.UINT8, IntArrayReaderType.UINT8, DoubleArrayReaderType.UINT8), 
    INT16(FormatTools.INT16, IntArrayReaderType.INT16, DoubleArrayReaderType.INT16),
    UINT16(FormatTools.UINT16, IntArrayReaderType.UINT16, DoubleArrayReaderType.UINT16),
    INT32(FormatTools.INT32, IntArrayReaderType.INT32, DoubleArrayReaderType.INT32), 
    UINT32(FormatTools.UINT32, IntArrayReaderType.UINT32, DoubleArrayReaderType.UINT32),
    FLOAT(FormatTools.FLOAT, null, DoubleArrayReaderType.FLOAT32), DOUBLE(FormatTools.DOUBLE, null, DoubleArrayReaderType.FLOAT64);

    private final int code;
    private final IntArrayReaderType intReader;
    private final DoubleArrayReaderType doubleReader;

    LociPixelType(int type, IntArrayReaderType intReader, DoubleArrayReaderType doubleReader)
    {
        this.code = type;
        this.intReader = intReader;
        this.doubleReader = doubleReader;
    }

    public DoubleArrayReaderType getDoubleArrayReader()
    {
        return doubleReader;
    }

    public IntArrayReaderType getIntArrayReader()
    {
        return intReader;
    }

    public static LociPixelType getInstance(int code)
    {
        for(LociPixelType type : LociPixelType.values())
        {
            if(type.code == code)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No LociPixelType known for code " + code);
    }
}
