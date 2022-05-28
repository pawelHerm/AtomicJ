
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
import java.util.List;

public interface IgorBinaryHeader
{
    public int getVersionNo();
    public ByteOrder getByteOrder();

    //DEPENDENCY FORMULA
    public boolean containsDependencyFormula();
    public int getDependencyFormulaSize();

    //WAVE NOTE
    public boolean containsWaveNote();
    public int getWaveNoteSize();
    public IgorWaveNote readInWaveNote(ByteBuffer waveNoteBuffer);

    //EXTENDED DATA UNITS
    public boolean containsExtendedDataUnits();
    public int getExtendedDataUnitSize();
    public String readInExtendedDataUnit(ByteBuffer byteBuffer);

    //EXTENDED DIMENSION UNITS
    public boolean containsExtendedDimensionUnits(int dimIndex);      
    public int getExtendedDimensionUnitSize(int dimIndex);
    public int getTotalExtendedDimensionUnitSize();
    public List<String> readInExtendedDimensionUnits(ByteBuffer buffer);

    //DIMENSION LABELS
    public boolean containsDimensionLabel(int dimIndex);
    public int getDimensionLabelSize(int dimIndex);
    public int getTotalDimensionLabelSize();
    public List<String> readInDimensionLabels(ByteBuffer buffer);

    //STRING INDICES
    public boolean containsStringIndices();
    public int getStringIndicesSize();
}