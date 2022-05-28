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

import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import atomicJ.data.ChannelFilter;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.SourceReadingState;

public abstract class NIDChannel
{
    static final String VERSION_KEY = "Version";    
    static final String FRAME_KEY = "Frame";

    private static final String POINTS_KEY = "Points";
    private static final String LINES_KEY = "Lines";
    private static final String SAVE_MODE_KEY = "SaveMode";
    private static final String SAVE_BITS_KEY = "SaveBits";
    private static final String SAVE_SIGNED_KEY = "SaveSign";
    private static final String SAVE_ORDER_KEY = "SaveOrder";

    private static final String SAVE_MODE_VALUE_BINARY = "Binary";
    private static final String SAVE_SIGNED_VALUE_SIGNED = "Signed";
    private static final String SAVE_ORDER_VALUE_INTEL = "Intel";

    private final int version;
    private final int points; //number of values in dimension 0
    private final int lines; //number of values in dimension 1
    private final String groupName; //name of the group
    private final String saveMode; //Code for the storage method (currently ‘Binary’ )
    private final NIDDataType dataType; 
    private final ByteOrder byteOrder; //Code binary storage format (currently ‘Intel’, little endian format)

    private final int channelIndex;
    private final List<NIDDimension> dimensions = new ArrayList<>();

    public NIDChannel(int channelIndex, INISection channelSection)
    {
        Map<String, String> keyValuePairs = channelSection.getKeyValuePairs();

        this.channelIndex = channelIndex;
        this.version = Integer.parseInt(keyValuePairs.get(VERSION_KEY));
        this.points = Integer.parseInt(keyValuePairs.get(POINTS_KEY));
        this.lines = Integer.parseInt(keyValuePairs.get(LINES_KEY));
        this.groupName = keyValuePairs.get(FRAME_KEY);
        this.saveMode = keyValuePairs.get(SAVE_MODE_KEY);
        int saveBits = Integer.parseInt(keyValuePairs.get(SAVE_BITS_KEY));
        String saveSigned = keyValuePairs.get(SAVE_SIGNED_KEY);
        String saveOrder = keyValuePairs.get(SAVE_ORDER_KEY);

        if(!SAVE_MODE_VALUE_BINARY.equals(saveMode))
        {
            throw new IllegalStateException("Unregnized saveMode " + saveMode);
        }

        if(!SAVE_SIGNED_VALUE_SIGNED.equals(saveSigned))
        {
            throw new IllegalStateException("Unregnized saveSigned " + saveSigned);
        }

        this.dataType = NIDDataType.getType(saveBits, true);

        if(!SAVE_ORDER_VALUE_INTEL.equals(saveOrder))
        {
            throw new IllegalStateException("Unregnized saveOrder " + saveOrder);
        }

        this.byteOrder = ByteOrder.LITTLE_ENDIAN;

        for(int i = 0;;i++)
        {
            boolean containsDimension = NIDDimension.containsDimensionInformation(i, keyValuePairs);
            if(containsDimension)
            {
                NIDDimension channelDimension = NIDDimension.build(i, keyValuePairs);
                dimensions.add(channelDimension);                 
            }
            else
            {
                break;
            }
        }         
    }

    public int getChannelIndex()
    {
        return channelIndex;
    }

    public static NIDChannel build(int channelIndex, INISection section)
    {
        Map<String, String> keyValuePairs = section.getKeyValuePairs();

        if(NIDSpectroscopyVersion2Channel.isAppropriate(keyValuePairs))
        {
            return new NIDSpectroscopyVersion2Channel(channelIndex, section);
        }

        int version = Integer.parseInt(keyValuePairs.get(VERSION_KEY));

        NIDChannel channel = version == 3 ? new NIDSpectroscopyVersion3Channel(channelIndex, section) : new NIDScanChannel(channelIndex, section);

        return channel;
    }

    public abstract void skipData(FileChannel channel) throws UserCommunicableException;

    public boolean shouldBeAccepted(ChannelFilter filter)
    {
        Quantity readInDataQuantity = getDataQuantity();

        String groupName = getGroupName();
        String identifier = readInDataQuantity.getName() + " " + groupName;

        return filter.accepts(identifier,readInDataQuantity);
    }

    public int getVersion()
    {
        return version;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public ByteOrder getByteOrder()
    {
        return byteOrder;
    }

    public NIDDataType getDataType()
    {
        return dataType;
    }

    public DoubleArrayReaderType getArrayReaderType()
    {
        return dataType.getArrayReaderType();
    }

    public int getPointCount()
    {
        return points;
    }

    public int getLineCount()
    {
        return lines;
    }

    public abstract NIDChannelData readInData(FileChannel channel, SourceReadingState state)  throws UserCommunicableException;
    public abstract NIDChannelData readInScanDataAndSkipOthers(FileChannel channel,  SourceReadingState state) throws UserCommunicableException;
    public abstract NIDChannelData readInSpectroscopyDataAndSkipOthers(FileChannel channel,  SourceReadingState state)  throws UserCommunicableException;

    public List<NIDDimension> getDimensions()
    {
        return Collections.unmodifiableList(dimensions);
    }

    public NIDDimension getDimension(int index)
    {
        if(index < 0 || index >= dimensions.size())
        {
            throw new IllegalArgumentException("There is no dimension for index " + index);
        }

        return dimensions.get(index);
    }

    public boolean isDimensionEmpty()
    {
        return dimensions.isEmpty();
    }

    public int getDimensionCount()
    {
        return dimensions.size();
    }

    public PrefixedUnit getDataUnit()
    {
        if(dimensions.isEmpty())
        {
            return SimplePrefixedUnit.getNullInstance();
        }

        NIDDimension dataDimension = dimensions.get(dimensions.size() - 1);
        PrefixedUnit unit = dataDimension.getUnit();

        return unit;
    }

    public Quantity getDataQuantity()
    {
        if(dimensions.isEmpty())
        {
            return new UnitQuantity("", SimplePrefixedUnit.getNullInstance());
        }

        NIDDimension dataDimension = dimensions.get(dimensions.size() - 1);

        return dataDimension.getQuantity();
    }

    public NIDDimension getDataDimension()
    {
        NIDDimension dataDimension = !dimensions.isEmpty() ? dimensions.get(dimensions.size() - 1) : null;
        return dataDimension;
    }

    public abstract int getForceCurveBranchesCount();   
    public abstract int getReadableElementCount();
    public abstract int getImageElementCount();
    public abstract int getSpectroscopyElementCount();
    public abstract boolean isCurveXChannel();   
    public abstract boolean isCurveYChannel();
}