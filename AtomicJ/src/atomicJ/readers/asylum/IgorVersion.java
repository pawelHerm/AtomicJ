
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


public enum IgorVersion
{
    VERSION_1(1, 8, 110) {
        @Override
        public IgorBinaryHeader getBinHeader(ByteOrder byteOrder, ByteBuffer byteBuffer) {
            return new IgorBinaryHeader1(byteOrder, byteBuffer);
        }

        @Override
        public WaveHeader getWaveHeader(ByteBuffer byteBuffer) {
            return new WaveHeader2(byteBuffer);
        }
    }, VERSION_2(2, 16,110) {
        @Override
        public IgorBinaryHeader getBinHeader(ByteOrder byteOrder, ByteBuffer byteBuffer) {
            return new IgorBinaryHeader2(byteOrder, byteBuffer);
        }

        @Override
        public WaveHeader getWaveHeader(ByteBuffer byteBuffer) {
            return new WaveHeader2(byteBuffer);
        }
    }, VERSION_3(3,20,110) {
        @Override
        public IgorBinaryHeader getBinHeader(ByteOrder byteOrder, ByteBuffer byteBuffer) {
            return new IgorBinaryHeader3(byteOrder, byteBuffer);
        }

        @Override
        public WaveHeader getWaveHeader(ByteBuffer byteBuffer) {
            return new WaveHeader2(byteBuffer);
        }
    },
    VERSION_5(5, 64,320) {
        @Override
        public IgorBinaryHeader getBinHeader(ByteOrder byteOrder, ByteBuffer byteBuffer) {
            return new IgorBinaryHeader5(byteOrder, byteBuffer);
        }

        @Override
        public WaveHeader getWaveHeader(ByteBuffer byteBuffer) {
            return new WaveHeader5(byteBuffer);
        }
    };

    private final int versionIndex;
    private final int binHeaderSize;
    private final int waveHeaderSize;

    IgorVersion(int versionIndex, int binHeaderSize, int waveHeaderSize)
    {
        this.versionIndex = versionIndex;
        this.binHeaderSize = binHeaderSize;
        this.waveHeaderSize = waveHeaderSize;
    }

    public static IgorVersion getVersion(int versionIndex)
    {
        for(IgorVersion version : IgorVersion.values())
        {
            if(version.versionIndex == versionIndex)
            {
                return version;
            }
        }

        throw new IllegalArgumentException("Version "
                + versionIndex + " is not recognized version of IBW file");
    }

    public int getVersionIndex()
    {
        return versionIndex;
    }

    public int getBinHeaderSize()
    {
        return binHeaderSize;
    }

    public int getWaveHeaderSize()
    {
        return waveHeaderSize;
    }

    abstract public IgorBinaryHeader getBinHeader(ByteOrder byteOrder, ByteBuffer byteBuffer);
    abstract public WaveHeader getWaveHeader(ByteBuffer byteBuffer);
}