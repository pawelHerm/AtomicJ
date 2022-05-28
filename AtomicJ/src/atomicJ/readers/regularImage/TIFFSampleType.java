package atomicJ.readers.regularImage;

import java.util.Collection;

import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IntArrayReaderType;

public enum TIFFSampleType 
{ 
    INT8(1, 2, IntArrayReaderType.INT8, DoubleArrayReaderType.INT8), UINT8(1,1, IntArrayReaderType.UINT8, DoubleArrayReaderType.UINT8), INT16(2, 2, IntArrayReaderType.INT16, DoubleArrayReaderType.INT16), UINT16(2, 1, IntArrayReaderType.UINT16, DoubleArrayReaderType.UINT16),
    UNDEFINED_8_BITS(1, 4, IntArrayReaderType.UINT8, DoubleArrayReaderType.UINT8), UNDEFINED_16_BITS(2, 4, IntArrayReaderType.UINT16, DoubleArrayReaderType.UINT16), UNDEFINED_32_BITS(4, 4, null, DoubleArrayReaderType.UINT32),
    DEFAULT_8_BITS(1, -1, IntArrayReaderType.UINT8, DoubleArrayReaderType.UINT8), DEFAULT_16_BITS(2, -1, IntArrayReaderType.UINT16, DoubleArrayReaderType.UINT16), DEFAULT_32_BITS(4, -1, null, DoubleArrayReaderType.UINT32),
    INT32(4,2, IntArrayReaderType.INT32, DoubleArrayReaderType.INT32), UINT32(4,1, null, DoubleArrayReaderType.UINT32), FLOAT16(2,3, null, DoubleArrayReaderType.FLOAT16), FLOAT32(4,3, null, DoubleArrayReaderType.FLOAT32), FLOAT64(8,3, null,DoubleArrayReaderType.FLOAT64);

    private final int byteCount;
    private final int bitFormat;

    private final DoubleArrayReaderType doubleReader;
    private final IntArrayReaderType intReader;

    TIFFSampleType(int bitCount, int bitFormat, IntArrayReaderType intReader, DoubleArrayReaderType doubleReader)
    {
        this.byteCount = bitCount;
        this.bitFormat = bitFormat;
        this.doubleReader = doubleReader;
        this.intReader = intReader;
    }

    public int getByteSize()
    {
        return byteCount;
    }

    public DoubleArrayReaderType getDoubleReader()
    {
        return doubleReader;
    }

    public IntArrayReaderType getIntReader()
    {
        return intReader;
    }

    public static int countBytes(Collection<TIFFSampleType> readerTypes)
    {
        int byteCount = 0;

        for(TIFFSampleType sampleType : readerTypes)
        {
            byteCount += sampleType.getByteSize();
        }

        return byteCount;
    }

    public static TIFFSampleType get(int bitCount, int bitFormat)
    {
        for(TIFFSampleType type : TIFFSampleType.values())
        {
            if(type.byteCount == bitCount && type.bitFormat == bitFormat)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("No PixelType corresponds to the bitCount " + bitCount + " and bitFormat " + bitFormat);
    }
}
