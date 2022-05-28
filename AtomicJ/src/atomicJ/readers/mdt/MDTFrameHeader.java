
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

package atomicJ.readers.mdt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;


public class MDTFrameHeader 
{
    public static final int BYTE_SIZE = 22;

    private final int totalFrameSize; // total Frame Size.                       
    private final int frameTypeCode; //frame type
    private final MDTFrameType frameType;

    private final byte version; //Frame version  7 – Windows version (for h_what = 1) 8 – Non-spectroscopic curve (for h_what = 1) 9 – Histogram

    private final int year;// year, unsigned two-byte int,
    private final int month;// month, unsigned two-byte int,
    private final int day;// day, unsigned two-byte int,
    private final int hour;// hour, unsigned two-byte int,
    private final int minute;// minute, unsigned two-byte int,
    private final int second;// second, unsigned two-byte int,
    private final int variablesSize; //h_am, unsigned integer, size of variables (in version 6 and earlier). Not used in version 7.
    //for h_what = 0 or 1 or 201, h_am  = sizeof(AxisScales) + sizeof(ScanVar or SpecVar);
    //for h_what = 3, h_am = 10


    private MDTFrameHeader(ByteBuffer buffer)
    {
        this.totalFrameSize = buffer.getInt(); 
        this.frameTypeCode = buffer.getShort();

        this.frameType = MDTFrameType.getMDTFrameType(frameTypeCode);
        this.version = buffer.get();   
        buffer.get(); //skips reserved f_ver1

        // Following fields store Frame creation date and time

        this.year = FileInputUtilities.getUnsigned(buffer.getShort());
        this.month = FileInputUtilities.getUnsigned(buffer.getShort());
        this.day = FileInputUtilities.getUnsigned(buffer.getShort());
        this.hour = FileInputUtilities.getUnsigned(buffer.getShort());
        this.minute = FileInputUtilities.getUnsigned(buffer.getShort());
        this.second = FileInputUtilities.getUnsigned(buffer.getShort());
        this.variablesSize = FileInputUtilities.getUnsigned(buffer.getShort());

    }

    public static MDTFrameHeader readIn(FileChannel channel) throws IOException, UserCommunicableException
    {
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, MDTFrameHeader.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN);
        MDTFrameHeader frameHeader = new MDTFrameHeader(buffer);
        return frameHeader;
    }

    public int getFrameTypeCode()
    {
        return frameTypeCode;
    }

    public MDTFrameType getFrameType()
    {
        return frameType;
    }

    public byte getVersion()
    {
        return version;
    }

    public int getSizeOfVariables()
    {
        return variablesSize;
    }

    public int getTotalFrameSize()
    {
        return totalFrameSize;
    }

    public int getRemainingFrameSize()
    {
        int size = totalFrameSize - BYTE_SIZE;
        return size;
    }

    public int getSecond()
    {
        return second;
    }

    public int getMinute()
    {
        return minute;
    }

    public int getHour()
    {
        return hour;
    }

    public int getDay()
    {
        return day;
    }

    public int getMonth()
    {
        return month;
    }

    public int getYear()
    {
        return year;
    }

    public Date getDate()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second); //we have to subtract 1 from the month, because in calendar.set() month is 0-based (January is 0), while int MDT file January is 1

        return calendar.getTime();
    }
}