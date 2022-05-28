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
import java.util.HashMap;
import java.util.Map;

import atomicJ.data.ImageChannel;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.QuantityArray2DExpression;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.nanosurf.NIDDataset.NIDChannelType;
import atomicJ.sources.CalibrationState;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.MathUtilities;

public class NIDSpectroscopyVersion2Channel extends NIDChannel
{
    private static final String SPECTROSCOPY_POINT_DIMENSION = "SpecPoint";
    private static final String FRAME_SPECTROSCOPY_BACKWARD = "Spec backward";
    private static final String FRAME_SPECTROSCOPY_FORWARD = "Spec forward";

    private static final String Y_AXIS_CURVE_DATASET = "Deflection";

    public NIDSpectroscopyVersion2Channel(int channelIndex, INISection channelSection)
    {
        super(channelIndex, channelSection);        
    }

    public static boolean isAppropriate(Map<String, String> keyValuePairs)
    {
        int version = Integer.parseInt(keyValuePairs.get(NIDChannel.VERSION_KEY));
        String frame = keyValuePairs.get(NIDChannel.FRAME_KEY);

        boolean appropriate = (version == 2) && (FRAME_SPECTROSCOPY_FORWARD.equals(frame.trim()) || FRAME_SPECTROSCOPY_BACKWARD.equals(frame.trim()));

        return appropriate;
    }

    public static NIDSpectroscopyVersion2Channel build(int channelIndex, INISection channelSection)
    {
        NIDSpectroscopyVersion2Channel channelHeaderSection = new NIDSpectroscopyVersion2Channel(channelIndex, channelSection);
        return channelHeaderSection;
    }

    @Override
    public NIDChannelData readInScanDataAndSkipOthers(FileChannel channel, SourceReadingState state) throws UserCommunicableException
    {
        skipData(channel);
        return NIDChannelData.getEmptyInstance();
    }

    @Override
    public NIDChannelData readInSpectroscopyDataAndSkipOthers(FileChannel channel, SourceReadingState state)  throws UserCommunicableException
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

        PrefixedUnit dataUnit = getDataUnit();

        Quantity readInDataQuantity = isCurveXChannel() ? Quantities.DISTANCE_MICRONS : (isCurveYChannel() ? CalibrationState.getDefaultYQuantity(dataUnit) : getDataQuantity());
        double factor = dataUnit.getConversionFactorTo(readInDataQuantity.getUnit());

        double range = dataDimension.getRange();
        double min = dataDimension.getMinimum();

        double trueOffset = factor*(min + range/2.);
        double trueScale = factor*(range/MathUtilities.intPow(2, bitSize));

        int pointCount = getPointCount();
        int lineCount = getLineCount();

        int bufferSize = byteSize*pointCount*lineCount;
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, bufferSize, getByteOrder());

        double[][] readInData = arrayReader.readIn2DArrayRowByRow(lineCount, pointCount, trueScale, trueOffset, buffer);

        String channelType = NIDChannelType.instanceKnown(getChannelIndex()) ? NIDChannelType.getInstance(getChannelIndex()).getName() : getDataDimension().getName();
        Map<String, QuantityArray2DExpression> readInMap = new HashMap<>();
        readInMap.put(channelType, new QuantityArray2DExpression(readInData, readInDataQuantity));

        NIDChannelData channelData = new NIDChannelData(Collections.<String, ImageChannel>emptyMap(), readInMap);
        return channelData;
    }

    @Override
    public NIDChannelData readInData(FileChannel channel, SourceReadingState state)  throws UserCommunicableException
    {
        return readInSpectroscopyDataAndSkipOthers(channel, state);
    }

    @Override
    public void skipData(FileChannel channel) throws UserCommunicableException
    {
        if(isDimensionEmpty() || getLineCount() < 1)
        {
            return;
        } 

        int byteSize = getDataType().getByteSize();
        int lineCount = getLineCount();

        int bytesToSkip = byteSize*getPointCount()*lineCount;
        FileInputUtilities.skipBytes(channel, bytesToSkip);        
    }

    @Override
    public boolean isCurveXChannel()
    {
        if(isDimensionEmpty())
        {
            return false;
        }

        int channelIndex = getChannelIndex();
        boolean xChannel = channelIndex == 7;
        return xChannel;
    }

    @Override
    public boolean isCurveYChannel()
    {
        if(isDimensionEmpty())
        {
            return false;
        }

        NIDDimension dataDimension = getDataDimension();

        boolean yChannel = Y_AXIS_CURVE_DATASET.equals(dataDimension.getName());

        return yChannel;
    }


    @Override
    public int getForceCurveBranchesCount() 
    {
        return getLineCount();
    }

    @Override
    public int getReadableElementCount()
    {
        return getLineCount();
    }

    @Override
    public int getImageElementCount()
    {
        return 0;
    }

    @Override
    public int getSpectroscopyElementCount()
    {
        return getLineCount();
    }
}