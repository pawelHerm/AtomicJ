
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.utilities.FileInputUtilities;

public class WaveHeader5 implements WaveHeader
{
    private final long createDate; //UNSIGNED INTEGER DateTime of creation.
    private final long modDate;  // UNSIGNED INTEGER DateTime of last modification.

    private final int npnts;  // Total number of points (multiply dimensions up to first zero).
    private final short type;  // See types (e.g. NT_FP64) above. Zero for text waves.

    private final char[] bname = new char[IgorUtilities.MAX_WAVE_NAME5 + 1]; // Name of wave plus trailing null.

    // Dimensioning info. [0] == rows, [1] == cols etc
    private final int[] dimensionItemCount = new int[IgorUtilities.MAXDIMS5]; // Number of of items in a dimension -- 0 means no data.
    private final double[] sfA = new double[IgorUtilities.MAXDIMS5];  // Index value for element e of dimension d = sfA[d]*e + sfB[d].
    private final double[] sfB = new double[IgorUtilities.MAXDIMS5];

    // SI units
    private final char[] dataUnits = new char[IgorUtilities.MAX_UNIT_CHARS + 1]; // Natural data units go here - null if none.
    private final char[][] dimUnits = new char[IgorUtilities.MAXDIMS5][IgorUtilities.MAX_UNIT_CHARS+1]; // Natural dimension units go here - null if none.

    private final short fsValid;    // TRUE if full scale values have meaning.
    private final double topFullScale;
    private final double botFullScale;   // The max and max full scale value for wave.

    ///////////////// 132 bytes to skip follow ///////////////////////////////////
    private final static int endSkipBytes = 132;

    //fields calculated from basic fields

    private final String waveName;
    private final Date creationDate;
    private final Date modificationDate;

    private final int dimCount;

    private final String dataUnit;
    private final List<String> dimensionUnits = new ArrayList<>();
    private final IgorWaveDataType dataType;

    //skipping n bytes can be done in this way
    // buffer.position(buffer.position() + n)
    public WaveHeader5(ByteBuffer buffer)
    {
        FileInputUtilities.skipBytes(4, buffer); //skips four bytes  struct WaveHeader5 **next;

        this.createDate = FileInputUtilities.getUnsigned(buffer.getInt());   //UNSIGNED INTEGER DateTime of creation.          
        this.modDate = FileInputUtilities.getUnsigned(buffer.getInt());   // UNSIGNED INTEGER DateTime of last modification.              
        this.npnts = buffer.getInt();  // Total number of points (multiply dimensions up to first zero).            
        this.type = buffer.getShort();  // See types (e.g. NT_FP64) above. Zero for text waves.

        FileInputUtilities.skipBytes(10, buffer);  // skips 10 bytes: two bytesReserved, six bytes of whpad1 and 2 bytes of whVersion                     
        FileInputUtilities.populateCharArrayWithBytes(bname, buffer); // Name of wave plus trailing null.            
        FileInputUtilities.skipBytes(8, buffer);//skips 8 bytes of whpad2 and struct DataFolder **dFolder  Reserved. Write zero. Ignore on read.
        FileInputUtilities.populateIntArray(dimensionItemCount, buffer); // Number of of items in a dimension -- 0 means no data.           
        FileInputUtilities.populateDoubleArray(sfA, buffer); // Index value for element e of dimension d = sfA[d]*e + sfB[d].
        FileInputUtilities.populateDoubleArray(sfB, buffer);

        // SI units

        FileInputUtilities.populateCharArrayWithBytes(dataUnits, buffer); // Natural data units go here - null if none.
        FileInputUtilities.populateCharArrayWithBytes2D(dimUnits, buffer); // Natural dimension units go here - null if none.

        this.fsValid = buffer.getShort();    // TRUE if full scale values have meaning.

        FileInputUtilities.skipBytes(2, buffer);   // Skips two bytes whpad3. Reserved. Write zero. Ignore on read.

        this.topFullScale = buffer.getDouble(); // The max and max full scale value for wave.
        this.botFullScale = buffer.getDouble();   

        FileInputUtilities.skipBytes(endSkipBytes, buffer);

        this.waveName = FileInputUtilities.convertBytesToString(bname).trim();
        this.creationDate = FileInputUtilities.convertSecondsToDate(createDate);
        this.modificationDate = FileInputUtilities.convertSecondsToDate(modDate);

        this.dimCount = IgorUtilities.getDimensionCount(dimensionItemCount);

        this.dataUnit = FileInputUtilities.convertBytesToString(dataUnits).trim();

        for(int i = 0; i<dimUnits.length; i++)
        {
            String dimensionUnit = FileInputUtilities.convertBytesToString(dimUnits[i]).trim(); 
            dimensionUnits.add(dimensionUnit);                
        }

        this.dataType = IgorWaveDataType.getType(this.type);

    }

    @Override
    public double getDimensionLength(int level)
    {
        double length = 0;

        if(level >= 0 && level <IgorUtilities.MAXDIMS5)
        {
            length = dimensionItemCount[level]*sfA[level];
        }

        return length;
    }

    @Override
    public int getDimensionItemCount(int level)
    {
        int dim = 0;

        if(level >= 0 && level < dimensionItemCount.length)
        {
            dim = dimensionItemCount[level];
        }

        return dim;
    }
    @Override
    public String getDimensionUnit(int level)
    {
        String unit = "";

        if(level >= 0 && level <IgorUtilities.MAXDIMS5)
        {
            unit = dimensionUnits.get(level);
        }

        return unit;
    }

    @Override
    public PrefixedUnit getYSIUnit()
    {
        String unitString = dimensionUnits.get(0);
        return UnitUtilities.getSIUnit(unitString);
    }

    @Override
    public PrefixedUnit getXSIUnit()
    {
        String unitString = dimensionUnits.get(1);
        return UnitUtilities.getSIUnit(unitString);
    }

    @Override
    public Grid2D getGrid()
    {
        PrefixedUnit xUnit = getXSIUnit();
        PrefixedUnit yUnit = getYSIUnit();

        Quantity distanceQuantity = Quantities.DISTANCE_MICRONS;
        double factorX = xUnit.getConversionFactorTo(distanceQuantity.getUnit());
        double factorY = yUnit.getConversionFactorTo(distanceQuantity.getUnit());

        Grid2D grid = new Grid2D(factorX*sfA[1], factorY*sfA[0], 0, 0, dimensionItemCount[0],
                dimensionItemCount[1], distanceQuantity, distanceQuantity);

        return grid;
    }

    @Override
    public double[][][] readIn(ByteBuffer buffer)
    {
        int rowCount = Math.max(1, dimensionItemCount[0]);
        int columnCount = Math.max(1, dimensionItemCount[1]);
        int layerCount = Math.max(1, dimensionItemCount[2]);


        return this.dataType.readInImageData(rowCount, columnCount, layerCount, buffer);
    }

    @Override
    public double[][][] readInRowByRow(ByteBuffer buffer)
    {
        int rowCount = Math.max(1, dimensionItemCount[0]);
        int columnCount = Math.max(1, dimensionItemCount[1]);
        int layerCount = Math.max(1, dimensionItemCount[2]);

        return this.dataType.readInTransposedImageData(rowCount, columnCount, layerCount, buffer);
    }

    public String getWaveName()
    {
        return waveName;
    }

    @Override
    public Date getCreationDate()
    {
        return creationDate;
    }

    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    @Override
    public int getTotalNumberOfPoints()
    {
        return npnts;
    }

    @Override
    public String getDataUnit()
    {
        return dataUnit;
    }

    public PrefixedUnit getDataSIUnit()
    {
        return UnitUtilities.getSIUnit(dataUnit);
    }

    @Override
    public IgorWaveDataType getDataType()
    {
        return dataType;
    }

    @Override
    public int getDataDimensionCount()
    {
        return dimCount;
    }

    @Override
    public boolean canBeImage()
    {
        return dimCount == 3;
    }

    @Override
    public int getLayerCount()
    {
        return dimensionItemCount[2];
    }


    @Override
    public int getWaveDataByteCount()
    {
        return npnts * dataType.getBytesPerPoint();
    }
}