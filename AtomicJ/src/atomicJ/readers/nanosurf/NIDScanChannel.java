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

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;

import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.SourceReadingState;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.MathUtilities;

public class NIDScanChannel extends NIDChannel
{
    public NIDScanChannel(int channelIndex, INISection channelSection)
    {
        super(channelIndex, channelSection);
    }

    private Grid2D buildGrid()
    {
        NIDDimension xDimension = getDimension(0);
        NIDDimension yDimension = getDimension(1);

        int pointCount = getPointCount();
        int lineCount = getLineCount();

        double xIncrement = xDimension.getRange()/(pointCount - 1);
        double yIncrement = yDimension.getRange()/(lineCount - 1);

        double xOrigin = xDimension.getMinimum();
        double yOrigin = yDimension.getMinimum();

        Grid2D grid = new Grid2D(xIncrement, yIncrement, xOrigin, yOrigin, lineCount, pointCount, xDimension.getQuantity(), yDimension.getQuantity());

        return grid;
    }

    @Override
    public NIDChannelData readInScanDataAndSkipOthers(FileChannel channel, SourceReadingState state) throws UserCommunicableException
    {
        if(isDimensionEmpty())
        {
            return NIDChannelData.getEmptyInstance();
        }

        NIDDataType dataType = getDataType();
        DoubleArrayReaderType arrayReader = dataType.getArrayReaderType();
        int byteSize = dataType.getByteSize();
        int bitSize = dataType.getBitSize();

        NIDDimension dataDimension = getDataDimension();

        double range = dataDimension.getRange();
        double min = dataDimension.getMinimum();

        double trueOffset = min + range/2.;
        double trueScale = range/MathUtilities.intPow(2, bitSize);

        int pointCount = getPointCount();
        int lineCount = getLineCount();

        int bufferSize = byteSize*pointCount*lineCount;
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, bufferSize, getByteOrder());

        double[][] readInData = arrayReader.readIn2DArrayRowByRow(lineCount, pointCount, trueScale, trueOffset, buffer);
        Quantity readInDataQuantity = getDataQuantity();

        Grid2D grid = buildGrid();

        String groupName = getGroupName();

        boolean isTrace = NIDGroup.SCAN_FORWARD.equals(groupName);
        String identifier = readInDataQuantity.getName() + " " + groupName;

        ImageChannel imageChannel = new ImageChannel(readInData, grid, readInDataQuantity, identifier, isTrace); 
        state.incrementAbsoluteProgress();

        return NIDChannelData.getScanOnlyInstance(Collections.singletonMap(identifier,imageChannel));
    }


    @Override
    public NIDChannelData readInSpectroscopyDataAndSkipOthers(FileChannel channel, SourceReadingState state)  throws UserCommunicableException
    {
        skipData(channel);
        return NIDChannelData.getEmptyInstance();
    }

    @Override
    public NIDChannelData readInData(FileChannel channel, SourceReadingState state)  throws UserCommunicableException
    {
        return readInScanDataAndSkipOthers(channel, state);
    }

    @Override
    public void skipData(FileChannel channel) throws UserCommunicableException
    {
        if(isDimensionEmpty())
        {
            return;
        }

        int byteSize = getDataType().getByteSize();        
        int bufferSize = byteSize*getPointCount()*getLineCount();

        FileInputUtilities.skipBytes(channel, bufferSize);
    }

    @Override
    public int getForceCurveBranchesCount() 
    {
        return 0;
    }

    @Override
    public int getReadableElementCount()
    {
        return 1;
    }

    @Override
    public int getImageElementCount()
    {
        return 1;
    }

    @Override
    public int getSpectroscopyElementCount()
    {
        return 0;
    }

    @Override
    public boolean isCurveXChannel()
    {
        return false;
    }

    @Override
    public boolean isCurveYChannel()
    {
        return false;
    }
}