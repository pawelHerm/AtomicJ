
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
import java.util.Date;

import atomicJ.data.Grid2D;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.utilities.FileInputUtilities;

public class WaveHeader2 implements WaveHeader
{
    short type;                         // See types (e.g. NT_FP64) above. Zero for text waves.
    // Skip 4 bytes of struct WaveHeader2 **next;

    char[] bname = new char[IgorUtilities.MAX_WAVE_NAME2 + 2];       // Name of wave plus trailing null.
    // skip 2 bytes of short whVersion;     
    //skip 2 bytes of short srcFldr;                      
    //skip 4 bytes of Handle fileName;                    

    char[] dataUnits = new char[IgorUtilities.MAX_UNIT_CHARS + 1];   // Natural data units go here - null if none.
    char[] xUnits = new char[IgorUtilities.MAX_UNIT_CHARS + 1];      // Natural x-axis units go here - null if none.

    int npnts;                         // Number of data points in wave.

    // skip two bytes of short aModified; 
    double hsA;
    double hsB;                     // X value for point p = hsA*p + hsB

    //skip two bytes of short wModified;                    
    //skip two bytes of short swModified;                   
    short fsValid;                      // True if full scale values have meaning.
    double topFullScale;
    double botFullScale;   // The min full scale value for wave.

    //skips in total 10 bytes for char useBits;  char kindBits;   void **formula; long depID;     
    //remember that in Igor Pro files char has only one byte, unlike in Java
    long createDate; //UNSIGNED INTEGER DateTime of creation. Not used in version 1 files.
    //skip two bytes for char wUnused[2];                    // Reserved. Write zero. Ignore on read.

    long modDate;  // UNSIGNED INTEGER DateTime of last modification.
    // skip 4 bytes of Handle waveNoteH;      

    //fields calculated from basic fields

    private final Date creationDate;
    private final Date modificationDate;

    private final String dataUnitString;
    private final String xUnitString;
    private final IgorWaveDataType dataType;

    public WaveHeader2(ByteBuffer buffer)
    {
        this.type = buffer.getShort(); // See types (e.g. NT_FP64) above. Zero for text waves.
        FileInputUtilities.skipBytes(4, buffer);// Skip 4 bytes of struct WaveHeader2 **next;

        FileInputUtilities.populateCharArrayWithBytes(bname, buffer); // Name of wave plus trailing null.
        FileInputUtilities.skipBytes(8, buffer);// skip 8 bytes of short whVersion, short srcFldr and Handle fileName;     

        FileInputUtilities.populateCharArrayWithBytes(dataUnits, buffer);  // Natural data units go here - null if none.
        FileInputUtilities.populateCharArrayWithBytes(xUnits, buffer);  // Natural x-axis units go here - null if none.

        this.npnts = buffer.getInt();                         // Number of data points in wave.

        FileInputUtilities.skipBytes(2, buffer);// skip two bytes of short aModified; 

        this.hsA = buffer.getDouble();
        this.hsB = buffer.getDouble();  // X value for point p = hsA*p + hsB

        FileInputUtilities.skipBytes(4, buffer);//skip four bytes of short wModified and short swModified;                    
        this.fsValid = buffer.getShort(); // True if full scale values have meaning.
        this.topFullScale = buffer.getDouble();
        this.botFullScale = buffer.getDouble();   // The min full scale value for wave.

        FileInputUtilities.skipBytes(10, buffer);//skips in total 10 bytes for char useBits;  char kindBits;   void **formula; long depID;     
        //remember that in Igor Pro files char has only one byte, unlike in Java
        this.createDate = FileInputUtilities.getUnsigned(buffer.getInt()); //UNSIGNED INTEGER DateTime of creation. Not used in version 1 files.
        FileInputUtilities.skipBytes(2, buffer);//skip two bytes for char wUnused[2];                    // Reserved. Write zero. Ignore on read.

        this.modDate = FileInputUtilities.getUnsigned(buffer.getInt());  // UNSIGNED INTEGER DateTime of last modification.
        FileInputUtilities.skipBytes(4, buffer);// skip 4 bytes of Handle waveNoteH;   

        this.creationDate = FileInputUtilities.convertSecondsToDate(createDate);
        this.modificationDate = FileInputUtilities.convertSecondsToDate(modDate);
        this.dataUnitString = FileInputUtilities.convertBytesToString(dataUnits).trim();
        this.xUnitString = FileInputUtilities.convertBytesToString(xUnits).trim();
        this.dataType = IgorWaveDataType.getType(this.type);
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
    public int getWaveDataByteCount()
    {
        return npnts * dataType.getBytesPerPoint();
    }
    @Override
    public int getTotalNumberOfPoints()
    {
        return npnts;
    }

    @Override
    public IgorWaveDataType getDataType() {
        return dataType;
    }

    @Override
    public double[][][] readIn(ByteBuffer buffer)
    {
        return this.dataType.readInImageData(npnts, 1, 1, buffer);
    }

    @Override
    public double[][][] readInRowByRow(ByteBuffer buffer)
    {
        return this.dataType.readInImageData(npnts, 1, 1, buffer);
    }

    @Override
    public double getDimensionLength(int level) {

        double length = (level == 0) ? hsA*npnts : 0;
        return length;
    }

    @Override
    public int getDimensionItemCount(int level)
    {
        int itemCount = (level == 0) ? npnts : 0;
        return itemCount;
    }

    @Override
    public String getDimensionUnit(int level) 
    {
        String unit = (level == 0) ? xUnitString : "";
        return unit;
    }

    @Override
    public int getDataDimensionCount()
    {
        return 1;
    }

    @Override
    public boolean canBeImage() {
        return false;
    }

    @Override
    public int getLayerCount() {
        return 0;
    }

    @Override
    public PrefixedUnit getYSIUnit() {
        return SimplePrefixedUnit.getNullInstance();
    }

    @Override
    public PrefixedUnit getXSIUnit()
    {
        return UnitUtilities.getSIUnit(xUnitString);
    }

    @Override
    public String getDataUnit() {
        return dataUnitString;
    }

    @Override
    public Grid2D getGrid() {
        return null;
    }

}