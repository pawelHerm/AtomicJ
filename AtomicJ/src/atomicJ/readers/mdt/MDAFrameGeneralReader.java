
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Paweł Hermanowicz
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

import java.io.*;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.*;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.sources.ChannelSource;


public class MDAFrameGeneralReader implements MDTFrameReader
{
    public static final int FRAME_TYPE = 106;

    private volatile boolean cancelled;
    private final ChannelFilter filter;

    public MDAFrameGeneralReader(ChannelFilter filter)
    {
        this.filter = filter;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public List<ImageSource> readInImages(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException  
    {           
        List<ImageSource> sources = new ArrayList<>();

        ExtFrameHeader extHeader = new ExtFrameHeader(FileInputUtilities.readBytesToBuffer(channel, ExtFrameHeader.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN));            
        extHeader.readInTextElements(FileInputUtilities.readBytesToBuffer(channel, extHeader.getTextElementsByteSize(), ByteOrder.LITTLE_ENDIAN));

        //skips Total var size  and Total header size
        FileInputUtilities.readBytesToBuffer(channel, 8, ByteOrder.LITTLE_ENDIAN);

        MDAHeader mdaHeader = MDAHeader.readIn(channel);
        mdaHeader.readInCalibrations(channel);               

        MDAFrame mdaFrame = MDAFrameType.getFrameType(extHeader, mdaHeader).buildMDAFrame(frameIndex, extHeader, mdaHeader); 

        sources.addAll(mdaFrame.readInImageSources(this, f, channel, filter));         

        return sources;        
    }


    @Override
    public List<ChannelSource> readInAllSources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException
    {
        List<ChannelSource> sources = new ArrayList<>();

        try {
            long initPosition = channel.position();

            sources.addAll(readInSpectroscopySources(f, channel, frameIndex, frameHeader));

            channel.position(initPosition);
            sources.addAll(readInImages(f, channel, frameIndex, frameHeader));

        } 
        catch (IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading a file",e);
        }


        return sources;
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        ExtFrameHeader extHeader = new ExtFrameHeader(FileInputUtilities.readBytesToBuffer(channel, ExtFrameHeader.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN));            
        extHeader.readInTextElements(FileInputUtilities.readBytesToBuffer(channel, extHeader.getTextElementsByteSize(), ByteOrder.LITTLE_ENDIAN));

        //skips Total var size  and Total header size
        FileInputUtilities.readBytesToBuffer(channel, 8, ByteOrder.LITTLE_ENDIAN);

        MDAHeader mdaHeader = MDAHeader.readIn(channel);
        mdaHeader.readInCalibrations(channel);               

        MDAFrame mdaFrame = MDAFrameType.getFrameType(extHeader, mdaHeader).buildMDAFrame(frameIndex, extHeader, mdaHeader); 

        sources.addAll(mdaFrame.readInSpectroscopySources(this, f, channel, filter));    

        return sources;
    }

    @Override
    public int getFrameType() 
    {
        return FRAME_TYPE;
    }

    private static enum MDAFrameType
    {
        //extract_mda_data
        SCAN {
            @Override
            public boolean isConsistentWith(ExtFrameHeader extHeader, MDAHeader header)
            {
                boolean consistent = header.getDimensionsCount() == 2 && header.getMeasurandElementCount() == 1;
                return consistent;
            }

            @Override
            public MDAFrame buildMDAFrame(int frameIndex,ExtFrameHeader extHeader, MDAHeader mdaHeader) {
                return new ScanFrame(frameIndex, extHeader, mdaHeader);
            }
        }, RAMAN_SPECTRUM {
            @Override
            public boolean isConsistentWith(ExtFrameHeader extHeader, MDAHeader header)
            {
                boolean consistent = (header.getDimensionsCount() == 0 && header.getMeasurandElementCount() == 2)
                        ||(header.getDimensionsCount() == 1 && header.getMeasurandElementCount() == 1);

                return consistent;
            }

            @Override
            public MDAFrame buildMDAFrame(int frameIndex,ExtFrameHeader extHeader, MDAHeader mdaHeader) {
                return new RamanSpectrumFrame(frameIndex, extHeader, mdaHeader);
            }

        }, RAMAN_IMAGE {
            @Override
            public boolean isConsistentWith(ExtFrameHeader extHeader, MDAHeader header) 
            {
                boolean consistent = (header.getDimensionsCount() == 3
                        && header.getMeasurandElementCount() == 1 && !HybridXMLComment.isHybrid(extHeader.getComment()));
                return consistent;
            }

            @Override
            public MDAFrame buildMDAFrame(int frameIndex,ExtFrameHeader extHeader, MDAHeader mdaHeader) {
                return null;
            }
        }, HYBRID {
            @Override
            public boolean isConsistentWith(ExtFrameHeader extHeader, MDAHeader header) 
            {
                boolean consistent = (header.getDimensionsCount() == 3 && header.getMeasurandElementCount() >= 1
                        && HybridXMLComment.isHybrid(extHeader.getComment()));

                return consistent;
            }

            @Override
            public MDAFrame buildMDAFrame(int frameIndex,ExtFrameHeader extHeader, MDAHeader mdaHeader) throws UserCommunicableException 
            {
                return new HybridFrame(frameIndex, extHeader, mdaHeader);
            }
        };

        public abstract boolean isConsistentWith(ExtFrameHeader extHeader, MDAHeader header);
        public abstract MDAFrame buildMDAFrame(int frameIndex, ExtFrameHeader extHeader, MDAHeader mdaHeader) throws UserCommunicableException;

        public static MDAFrameType getFrameType(ExtFrameHeader extHeader, MDAHeader header)
        {            
            for(MDAFrameType frameType : MDAFrameType.values())
            {
                if(frameType.isConsistentWith(extHeader, header))
                {
                    return frameType;
                }
            }

            throw new IllegalStateException("No MDAFrameType is consistent with the header");
        }
    }


    public static Grid2D buildGrid(MDACalibration xCalibration, MDACalibration yCalibration)
    {
        PrefixedUnit xUnit = xCalibration.getUnit();
        double xScale = xCalibration.getScale();

        PrefixedUnit yUnit = yCalibration.getUnit();
        double yScale = yCalibration.getScale();

        Quantity xQuantity = Quantities.DISTANCE_MICRONS;
        Quantity yQuantity = Quantities.DISTANCE_MICRONS;

        double xFactor = xUnit.getConversionFactorTo(xQuantity.getUnit());
        double yFactor = yUnit.getConversionFactorTo(yQuantity.getUnit());

        int rowCount = (int) yCalibration.getArrayElementCount();
        int columnCount = (int) xCalibration.getArrayElementCount();

        Grid2D grid = new Grid2D(xFactor*xScale, yFactor*yScale, 0, 0, rowCount, columnCount, xQuantity, yQuantity);

        return grid;
    }
}

