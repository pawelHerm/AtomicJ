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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class NIDSpectroscopyVersion3Channel extends NIDChannel
{
    static final String X_AXIS_CURVE_DATASET_SENSOR_A = "Z-Axis Sensor";
    static final Object X_AXIS_CURVE_DATASET_RAW_A = "Z-Axis";

    static final String X_AXIS_CURVE_DATASET_SENSOR_B = "ext sensor";
    static final Object X_AXIS_CURVE_DATASET_RAW_B = "external z stage";

    static final String Y_AXIS_CURVE_DATASET = "Deflection";

    private final List<NIDLineDimension> lineDimensions = new ArrayList<>();

    public NIDSpectroscopyVersion3Channel(int channelIndex, INISection channelSection)
    {
        super(channelIndex, channelSection);

        Map<String, String> keyValuePairs = channelSection.getKeyValuePairs();

        if(getVersion() >= 3)
        {
            for(int i = 0; i<getLineCount(); i++)
            {
                lineDimensions.add(NIDLineDimension.build(i, keyValuePairs));
            }
        }               
    }

    public static NIDSpectroscopyVersion3Channel build(int channelIndex, INISection channelSection)
    {
        NIDSpectroscopyVersion3Channel channelHeaderSection = new NIDSpectroscopyVersion3Channel(channelIndex, channelSection);
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
        if(isDimensionEmpty() || lineDimensions.isEmpty())
        {
            return NIDChannelData.getEmptyInstance();
        }

        NIDDataType dataType = getDataType();
        DoubleArrayReaderType arrayReader = dataType.getArrayReaderType();  // DoubleArrayReaderType - INT32


        int byteSize = dataType.getByteSize();
        int bitSize = dataType.getBitSize();


        int lineCount = lineDimensions.size();
        double[][] readInData = new double[lineCount][];

        PrefixedUnit dataUnit = getDataUnit();

        Quantity readInDataQuantity = isCurveXChannel() ? Quantities.DISTANCE_MICRONS : (isCurveYChannel() ? CalibrationState.getDefaultYQuantity(dataUnit) : getDataQuantity());
        double factor = dataUnit.getConversionFactorTo(readInDataQuantity.getUnit());

        NIDDimension dataDimension = getDataDimension();

        for(int i = 0; i<lineCount; i++)
        {
            if(state.isOutOfJob())
            {
                return NIDChannelData.getEmptyInstance();
            }

            NIDLineDimension lineDimension = lineDimensions.get(i);

            int goodPointsCount = lineDimension.getPointCount();

            double range = dataDimension.getRange();//lineDimension.getRange();
            double min = dataDimension.getMinimum();//lineDimension.getMinimum();        

            double trueOffset = factor*(min + range/2.);
            double trueScale = factor*(range/MathUtilities.intPow(2, bitSize));

            int bufferSize =  byteSize * goodPointsCount;

            ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, bufferSize, getByteOrder());

            readInData[i] = arrayReader.readIn1DArray(goodPointsCount, trueScale, trueOffset, buffer);

            int bytesToSkip = byteSize*(getPointCount() - goodPointsCount);
            FileInputUtilities.skipBytes(channel, bytesToSkip);

            state.incrementAbsoluteProgress();
        }        


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
        if(isDimensionEmpty() || lineDimensions.isEmpty())
        {
            return;
        } 

        int byteSize = getDataType().getByteSize();
        int lineCount = lineDimensions.size();

        int bytesToSkip = byteSize*getPointCount()*lineCount;
        FileInputUtilities.skipBytes(channel, bytesToSkip);        
    }

    @Override
    public boolean isCurveXChannel()
    {
        if(isDimensionEmpty() || lineDimensions.isEmpty())
        {
            return false;
        }

        //        NIDDimension dataDimension = getDataDimension();
        //        boolean xChannel = X_AXIS_CURVE_DATASET_SENSOR_A.equals(dataDimension.getName()) || X_AXIS_CURVE_DATASET_RAW_A.equals(dataDimension.getName()) 
        //                || X_AXIS_CURVE_DATASET_SENSOR_B.equals(dataDimension.getName()) 
        //                || X_AXIS_CURVE_DATASET_RAW_B.equals(dataDimension.getName());

        int channelIndex = getChannelIndex();
        boolean xChannel = channelIndex == 6 || channelIndex == 7;
        return xChannel;
    }

    @Override
    public boolean isCurveYChannel()
    {
        if(isDimensionEmpty() || lineDimensions.isEmpty())
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
        return lineDimensions.size();
    }

    @Override
    public int getReadableElementCount()
    {
        return lineDimensions.size();
    }

    @Override
    public int getImageElementCount()
    {
        return 0;
    }

    @Override
    public int getSpectroscopyElementCount()
    {
        return lineDimensions.size();
    }
}