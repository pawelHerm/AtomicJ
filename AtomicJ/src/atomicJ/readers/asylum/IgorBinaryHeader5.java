
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
import java.util.ArrayList;
import java.util.List;

import atomicJ.utilities.FileInputUtilities;


public class IgorBinaryHeader5 implements IgorBinaryHeader
{
    private final short versionNo; 
    private final ByteOrder byteOrder;

    private final short checksum; // Checksum over this header and the wave header.
    private final int wfmSize; // The size of the WaveHeader5 data structure plus the wave data.                       
    private final int formulaSize; // The size of the dependency formula, if any.       
    private final int noteSize; // The size of the note text.
    private final int dataEUnitsSize; // The size of optional extended data units.       
    private final int[] dimEUnitsSize = new int[IgorUtilities.MAXDIMS5];// The size of optional extended dimension units.
    private final int[] dimLabelsSize = new int[IgorUtilities.MAXDIMS5];// The size of optional dimension labels.
    private final int sIndicesSize; // The size of string indices if this is a text wave.      
    private final int optionsSize1; // Reserved. Write zero. Ignore on read.      
    private final int optionsSize2;// Reserved. Write zero. Ignore on read.

    public IgorBinaryHeader5(ByteOrder byteOrder, ByteBuffer buffer)
    {
        this.byteOrder = byteOrder;

        this.versionNo = buffer.getShort(); 
        this.checksum = buffer.getShort();
        this.wfmSize = buffer.getInt();                      
        this.formulaSize = buffer.getInt();          
        this.noteSize = buffer.getInt();           
        this.dataEUnitsSize = buffer.getInt();

        for(int i = 0; i<dimEUnitsSize.length; i++)
        {
            dimEUnitsSize[i] = buffer.getInt();
        }

        for(int i = 0; i<dimLabelsSize.length; i++)
        {            
            dimLabelsSize[i] = buffer.getInt();
        }

        this.sIndicesSize = buffer.getInt();         
        this.optionsSize1 = buffer.getInt();            
        this.optionsSize2 = buffer.getInt();
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
        return formulaSize > 0;
    }

    @Override
    public int getDependencyFormulaSize()
    {
        return formulaSize;
    }

    @Override
    public boolean containsWaveNote()
    {
        return noteSize > 0;
    }

    @Override
    public int getWaveNoteSize()
    {
        return noteSize;
    }

    @Override
    public IgorWaveNote readInWaveNote(ByteBuffer waveNoteBuffer)
    {
        waveNoteBuffer.order(byteOrder);
        IgorWaveNote waveNote = new IgorWaveNote(waveNoteBuffer, noteSize);

        return waveNote;
    }

    @Override
    public boolean containsExtendedDataUnits()
    {
        return dataEUnitsSize > 0;
    }

    @Override
    public int getExtendedDataUnitSize()
    {
        return dataEUnitsSize;
    }

    @Override
    public String readInExtendedDataUnit(ByteBuffer byteBuffer)
    {
        byteBuffer.order(byteOrder);

        char[] chars = new char[dataEUnitsSize];
        FileInputUtilities.populateCharArrayWithBytes(chars, byteBuffer);

        String unit =  FileInputUtilities.convertBytesToString(chars);
        return unit;
    }


    @Override
    public boolean containsExtendedDimensionUnits(int dimIndex)
    {
        boolean contains = false;
        if(dimIndex >= 0 && dimIndex < dimEUnitsSize.length)
        {
            contains = dimEUnitsSize[dimIndex] >0;
        }
        return contains;
    }

    @Override
    public int getExtendedDimensionUnitSize(int dimIndex)
    {
        int size = 0;
        if(dimIndex >= 0 && dimIndex < dimEUnitsSize.length)
        {
            size = dimEUnitsSize[dimIndex];
        }
        return size;
    }

    @Override
    public int getTotalExtendedDimensionUnitSize()
    {
        int total = 0;

        for(int i = 0; i<dimEUnitsSize.length; i++)
        {
            total += dimEUnitsSize[i];
        }

        return total;
    }

    @Override
    public List<String> readInExtendedDimensionUnits(ByteBuffer buffer)
    {
        List<String> dimensionUnits = new ArrayList<>();
        for(int i = 0; i<dimEUnitsSize.length; i++)
        {                
            buffer.order(byteOrder);        
            char[] dimensionLabelChars = new char[dimEUnitsSize[i]];
            FileInputUtilities.populateCharArrayWithBytes(dimensionLabelChars, buffer);

            String unit =  FileInputUtilities.convertBytesToString(dimensionLabelChars);
            dimensionUnits.add(unit);
        }

        return dimensionUnits;
    }

    @Override
    public boolean containsDimensionLabel(int dimIndex)
    {
        boolean contains = false;
        if(dimIndex >= 0 && dimIndex < dimLabelsSize.length)
        {
            contains = dimLabelsSize[dimIndex] >0;
        }
        return contains;
    }

    @Override
    public int getDimensionLabelSize(int dimIndex)
    {
        int size = 0;
        if(dimIndex >= 0 && dimIndex < dimLabelsSize.length)
        {
            size = dimLabelsSize[dimIndex];
        }
        return size;
    }

    @Override
    public int getTotalDimensionLabelSize()
    {
        int total = 0;

        for(int i = 0; i<dimLabelsSize.length; i++)
        {
            total += dimLabelsSize[i];
        }

        return total;
    }

    @Override
    public List<String> readInDimensionLabels(ByteBuffer buffer)
    {
        List<String> dimensionLabels = new ArrayList<>();
        for(int i = 0; i<dimLabelsSize.length; i++)
        {                
            buffer.order(byteOrder);        
            char[] dimensionLabelChars = new char[dimLabelsSize[i]];
            FileInputUtilities.populateCharArrayWithBytes(dimensionLabelChars, buffer);

            String label =  FileInputUtilities.convertBytesToString(dimensionLabelChars);
            dimensionLabels.add(label);
        }

        return dimensionLabels;
    }

    @Override
    public boolean containsStringIndices()
    {
        return sIndicesSize > 0;
    }

    @Override
    public int getStringIndicesSize()   
    {
        return sIndicesSize;
    }
}