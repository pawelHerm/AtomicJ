
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
import java.nio.ByteOrder;


public class IgorFileType
{
    private final ByteOrder byteOrder;
    private final IgorVersion version;

    public IgorFileType(ByteBuffer buffer)
    {
        byte firstByte = buffer.get();
        this.byteOrder = getByteOrder(firstByte);

        buffer.order(byteOrder);            
        buffer.rewind();

        int versionIndex = buffer.getShort();
        this.version = IgorVersion.getVersion(versionIndex);
    }

    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    public IgorVersion getVersion()
    {
        return version;
    }

    public int getBinHeaderSize()
    {
        return version.getBinHeaderSize();
    }

    public int getWaveHeaderSize()
    {
        return version.getWaveHeaderSize();
    }

    private ByteOrder getByteOrder(byte firstByte)
    {
        if(firstByte == 0)
        {
            return ByteOrder.BIG_ENDIAN;
        }
        return ByteOrder.LITTLE_ENDIAN;
    }

    public IgorBinaryHeader readInBinHeader(ByteBuffer byteBuffer)
    {
        byteBuffer.order(byteOrder);
        return version.getBinHeader(byteOrder, byteBuffer);
    }

    public WaveHeader readInWaveHeader(ByteBuffer byteBuffer)
    {
        byteBuffer.order(byteOrder);
        return version.getWaveHeader(byteBuffer);
    }
}