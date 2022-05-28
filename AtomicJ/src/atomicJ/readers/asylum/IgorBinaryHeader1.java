
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Pawe� Hermanowicz
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
import java.util.Collections;
import java.util.List;

public class IgorBinaryHeader1 implements IgorBinaryHeader
{    
    private final short versionNo; // Version number for backwards compatibility.
    private final ByteOrder byteOrder;

    private final int wfmSize; // The size of the WaveHeader2 data structure plus the wave data plus 16 bytes of padding.
    private final short checksum; // Checksum over this header and the wave header.

    public IgorBinaryHeader1(ByteOrder byteOrder, ByteBuffer buffer)
    {
        this.byteOrder = byteOrder;
        this.versionNo = buffer.getShort(); 
        this.wfmSize = buffer.getInt();  
        this.checksum = buffer.getShort();        
    }

    @Override
    public int getVersionNo()
    {
        return versionNo;
    }

    @Override
    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    @Override
    public boolean containsDependencyFormula()
    {
        return false;
    }

    @Override
    public int getDependencyFormulaSize()
    {
        return 0;
    }

    @Override
    public boolean containsWaveNote()
    {
        return false;
    }

    @Override
    public int getWaveNoteSize()
    {
        return 0;
    }

    @Override
    public IgorWaveNote readInWaveNote(ByteBuffer byteBuffer)
    {
        byteBuffer.order(byteOrder);
        IgorWaveNote waveNote = new IgorWaveNote(byteBuffer, 0);

        return waveNote;
    }

    @Override
    public boolean containsExtendedDataUnits()
    {
        return false;
    }

    @Override
    public int getExtendedDataUnitSize()
    {
        return 0;
    }



    @Override
    public String readInExtendedDataUnit(ByteBuffer byteBuffer)
    {
        return "";
    }

    @Override
    public boolean containsExtendedDimensionUnits(int dimIndex)
    {
        return false;
    }

    @Override
    public int getExtendedDimensionUnitSize(int dimIndex)
    {
        return 0;
    }

    @Override
    public int getTotalExtendedDimensionUnitSize()
    {
        return 0;
    }

    @Override
    public List<String> readInExtendedDimensionUnits(ByteBuffer buffer)
    {
        return Collections.nCopies(IgorUtilities.MAXDIMS5, "");
    }

    @Override
    public boolean containsDimensionLabel(int dimIndex)
    {
        return false;
    }

    @Override
    public int getDimensionLabelSize(int dimIndex)
    {
        return 0;
    }

    @Override
    public int getTotalDimensionLabelSize()
    {
        return 0;
    }

    @Override
    public List<String> readInDimensionLabels(ByteBuffer buffer)
    {
        return Collections.nCopies(IgorUtilities.MAXDIMS5, "");
    }

    @Override
    public boolean containsStringIndices()
    {
        return false;
    }

    @Override
    public int getStringIndicesSize()   
    {
        return 0;
    }
}