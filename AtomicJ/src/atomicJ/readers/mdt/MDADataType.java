package atomicJ.readers.mdt;

import java.nio.ByteBuffer;

import atomicJ.readers.DoubleArrayReaderType;

public enum MDADataType
{
    INT8("int8", -1, DoubleArrayReaderType.INT8),
    UINT8("uint8",1, DoubleArrayReaderType.UINT8),
    INT16("int16", -2, DoubleArrayReaderType.INT16),
    UINT16("unit16",2, DoubleArrayReaderType.UINT16),
    INT32("int32",-4, DoubleArrayReaderType.INT32),
    UINT32("uint32",4, DoubleArrayReaderType.UINT32),
    INT64("int64",-8, DoubleArrayReaderType.INT64),
    UINT64("uint64",8, DoubleArrayReaderType.UINT64),
    FLOAT32("float32",-(4 + 23 * 256), DoubleArrayReaderType.FLOAT32), //(for floats, the code is numer of bytes + number of bits in the fraction part * 256 ?)
    FLOAT48("float48",-(6 + 39 * 256), DoubleArrayReaderType.FLOAT48),
    FLOAT64("float64",-(8 + 52 * 256), DoubleArrayReaderType.FLOAT64),
    FLOAT80("float80",-(10 + 63 * 256), DoubleArrayReaderType.FLOAT80), //extended precision, is it really used
    FLOATFIX("floatfix",-(8 + 256 * 256), null);

    private final String name;
    private final int code;
    private final DoubleArrayReaderType reader;

    MDADataType(String name, int code, DoubleArrayReaderType reader)
    {
        this.code = code;
        this.name = name;
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

    public double[] readIn1DArray(int length, double scale, ByteBuffer dataBuffer)
    {
        return reader.readIn1DArray(length, scale, dataBuffer);
    }

    public double[] readIn1DArray(int length, int skipBytesStep, double scale, ByteBuffer dataBuffer)
    {
        return reader.readIn1DArray(length, skipBytesStep, scale, dataBuffer);
    }

    public double[][] readIn2DArray(int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)
    {
        return reader.readIn2DArrayRowByRow(rowCount, columnCount, scale, dataBuffer);
    }

    public double[][][] readIn3DArray(int channelCount,int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)
    {
        return reader.readIn3DArrayRowByRow(channelCount, rowCount, columnCount, scale, dataBuffer);
    }

    @Override
    public String toString()
    {
        return name;
    }

    public static MDADataType getMDADataType(int code)
    {
        for(MDADataType dataType : MDADataType.values())
        {
            if(dataType.code == code)
            {
                return dataType;
            }
        }

        throw new IllegalArgumentException("No MDADataType corresponds to the code " + code);
    }
}
