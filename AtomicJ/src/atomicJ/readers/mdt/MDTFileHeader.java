
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

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;


public class MDTFileHeader 
{
    public static final int BYTE_SIZE = 33;

    private final int fileMask; // MDT file mask ($FF93B001).                       
    private final int allFramesSize; //The size of all Frames.       
    private final int lastFrameIndex; // last Frame index (zero-based).

    private MDTFileHeader(ByteBuffer buffer)
    {
        this.fileMask = buffer.getInt(); 
        this.allFramesSize = buffer.getInt();
        buffer.getInt(); //skips reserved f_isz0                      
        this.lastFrameIndex = buffer.getShort();          
        //the rest is f_r0 and f_str, which are just reserved       

    }

    public static MDTFileHeader readIn(FileChannel channel) throws IOException, UserCommunicableException
    {
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, MDTFileHeader.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN);
        MDTFileHeader fileHeader = new MDTFileHeader(buffer);
        return fileHeader;
    }

    public int getMask()
    {
        return fileMask;
    }

    public int getAllFramesSize()
    {
        return allFramesSize;
    }

    public int getLastFrameIndex()
    {
        return lastFrameIndex;
    }
}