
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.readers.asylum;

import java.nio.ByteBuffer;
import atomicJ.readers.DoubleArrayReaderType;

public enum IgorWaveDataType
{       
    BYTE(8, DoubleArrayReaderType.INT8), //NT_I8, 
    UNSIGNED_BYTE(8, DoubleArrayReaderType.UINT8), //NT_I8,
    SHORT(0x10, DoubleArrayReaderType.INT16), //NT_I16 16 bit integer numbers. Requires Igor Pro 2.0 or later., 
    UNSIGNED_SHORT(0x10, DoubleArrayReaderType.UINT16), //NT_I16 16 bit integer numbers. Requires Igor Pro 2.0 or later.,
    INTEGER(0x20, DoubleArrayReaderType.INT32), //NT_I32 32 bit integer numbers. Requires Igor Pro 2.0 or later.,
    UNSIGNED_INTEGER(0x20, DoubleArrayReaderType.UINT32), //NT_I32 32 bit integer numbers. Requires Igor Pro 2.0 or later.,
    FLOAT(2, DoubleArrayReaderType.FLOAT32), //NT_FP32 32 bit float numbers., 
    DOUBLE(4, DoubleArrayReaderType.FLOAT64);//NT_FP64 64 bit double numbers.'

    static final short NT_CMPLX = 1; // Complex numbers.  
    static final short NT_UNSIGNED = 0x40; // Makes above signed integers unsigned. // Requires Igor Pro 3.0 or later.

    private final int signature;
    private final DoubleArrayReaderType readerType;

    IgorWaveDataType(int byteCountSignature, DoubleArrayReaderType readerType)
    {
        this.signature = byteCountSignature;
        this.readerType = readerType;
    }

    public static IgorWaveDataType getType(short signatureOther)
    {
        if(calculateIsComplex(signatureOther))
        {
            throw new IllegalArgumentException("Complex data types are not supported");
        }

        for(IgorWaveDataType type : IgorWaveDataType.values())
        {
            boolean byteCountMatch = (type.signature == (type.signature & signatureOther));
            boolean signessMatch = (type.readerType.isUnsigned() == calculateIsUnsigned(signatureOther));

            if(byteCountMatch && signessMatch)
            {
                return type;
            }
        }

        throw new IllegalArgumentException("Type of data not recognized");
    }

    private static boolean calculateIsUnsigned(short signature)
    {
        return (NT_UNSIGNED == (NT_UNSIGNED & signature));
    }

    private static boolean calculateIsComplex(short signature)
    {
        return (NT_CMPLX == (NT_CMPLX & signature));
    }

    public int getBytesPerPoint()
    {
        return readerType.getByteSize();
    }

    public boolean isUnsigned()
    {
        return readerType.isUnsigned();
    }

    public double[][][] readInImageData(int rowCount, int columnCount, int layerCount, ByteBuffer buffer)
    {
        return readerType.readIn3DArrayColumnByColumn(layerCount, rowCount, columnCount, 1, buffer);
    }

    public double[][][] readInTransposedImageData(int rowCount, int columnCount, int layerCount, ByteBuffer buffer)
    {
        return readerType.readIn3DArrayRowByRow(layerCount, rowCount, columnCount, 1, buffer);
    }
}