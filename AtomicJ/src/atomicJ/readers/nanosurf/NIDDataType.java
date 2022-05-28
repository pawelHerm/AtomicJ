/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanosurf;

import atomicJ.readers.DoubleArrayReaderType;

public enum NIDDataType
{
    INT8(8, true, DoubleArrayReaderType.INT8), INT16(16, true, DoubleArrayReaderType.INT16), INT32(32, true, DoubleArrayReaderType.INT32);

    private final int bitSize;
    private final boolean signed;
    private final DoubleArrayReaderType arrayReaderType;

    NIDDataType(int bitSize, boolean signed, DoubleArrayReaderType arrayReaderType)
    {
        this.bitSize = bitSize;
        this.signed = signed;
        this.arrayReaderType = arrayReaderType;
    }

    public int getByteSize()
    {
        int byteSize = bitSize/8;
        return byteSize;
    }

    public int getBitSize()
    {
        return bitSize;
    }

    public boolean isSigned()
    {
        return signed;
    }

    public DoubleArrayReaderType getArrayReaderType()
    {
        return arrayReaderType;
    }

    public static NIDDataType getType(int bitSize, boolean signed)
    {
        for(NIDDataType dataType : NIDDataType.values())
        {
            if(dataType.bitSize == bitSize && dataType.signed == signed)
            {
                return dataType;
            }
        }

        throw new IllegalArgumentException("No NIDDataType corresponds to combination of the bit size " + bitSize + " and signedness " + signed);
    }
}