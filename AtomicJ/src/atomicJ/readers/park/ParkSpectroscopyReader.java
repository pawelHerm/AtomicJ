
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
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

package atomicJ.readers.park;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffIFDEntry;
import loci.formats.tiff.TiffParser;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.Grid2D;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.readers.ReadingPack;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;


public class ParkSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"tiff"};
    private static final String DESCRIPTION = "Park curve (.tiff)";

    private static final int PARK_MAGIC_NUMBER = 0x0E031301;
    private static final int TAG_PARK_MAGIC_NUMBER = 50432;

    private static final int TAG_VERSION = 50433;
    private static final int TAG_HEADER = 50435;
    private static final int TAG_SPECTROSCOPY_HEADER = 50438;
    private static final int TAG_COMMENT = 50436;
    private static final int TAG_SPECTROSCOPY_DATA = 50439;

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        boolean isParkTiff = filter.accept(f) && ParkSourceReader.checkIfParkTiff(f);      

        return isParkTiff;
    }

    public boolean isSpectroscopy(File f)
    {       
        try {            
            TiffParser parser = new TiffParser(new RandomAccessInputStream(f.getAbsolutePath()));

            IFD firstIFD = parser.getFirstIFD();     
            boolean isParkTiff = (PARK_MAGIC_NUMBER == ((Number)firstIFD.getIFDValue(TAG_PARK_MAGIC_NUMBER)).longValue());

            if(!isParkTiff)
            {
                return false;
            }

            ParkFileVersion version = null;
            try {
                version = ParkFileVersion.getParkFileVersion(firstIFD.getIFDIntValue(TAG_VERSION, 0));
            } catch (FormatException e) {
                e.printStackTrace();
            }

            byte[] headerBytes = (byte[]) firstIFD.getIFDValue(TAG_HEADER);

            ByteBuffer headerBuffer = ByteBuffer.allocate(headerBytes.length);
            headerBuffer.order(ByteOrder.LITTLE_ENDIAN).put(headerBytes).flip();

            ParkHeader header = ParkFileVersion.VERSION_2.equals(version) ? ParkHeader2.readIn(headerBuffer) : ParkHeader.readIn(headerBuffer);

            return ParkImageType.SPECTROSCOPY.equals(header.getParkImageType());
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override   
    public List<SimpleSpectroscopySource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException 
    {     
        try
        {
            RandomAccessInputStream in = new RandomAccessInputStream(f.getAbsolutePath());

            return readSourcesFromInputStream(f, in, readingDirectives); 
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }  
    }    

    protected List<SimpleSpectroscopySource> readSourcesFromInputStream(File f, RandomAccessInputStream in, atomicJ.readers.SourceReadingDirectives readingDirectives)  throws UserCommunicableException, IllegalImageException 
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try
        {
            TiffParser parser = new TiffParser(in);
            IFD firstIFD = parser.getFirstIFD();     

            ParkFileVersion version = null;
            try {
                version = ParkFileVersion.getParkFileVersion(firstIFD.getIFDIntValue(TAG_VERSION, 0));
            } catch (FormatException e) {
                e.printStackTrace();
            }

            byte[] headerBytes = (byte[]) firstIFD.getIFDValue(TAG_HEADER);


            ByteBuffer headerBuffer = ByteBuffer.allocate(headerBytes.length);
            headerBuffer.order(ByteOrder.LITTLE_ENDIAN).put(headerBytes).flip();

            ParkHeader header = ParkFileVersion.VERSION_2.equals(version) ? ParkHeader2.readIn(headerBuffer) : ParkHeader.readIn(headerBuffer);

            if(!ParkImageType.SPECTROSCOPY.equals(header.getParkImageType()))
            {
                throw new IllegalImageException();
            }

            byte[] spectroscopyHeaderBytes = (byte[]) firstIFD.getIFDValue(TAG_SPECTROSCOPY_HEADER);
            if(spectroscopyHeaderBytes == null)
            {
                return sources;
            }         

            ByteBuffer spectroscopyHeaderBuffer = ByteBuffer.allocate(spectroscopyHeaderBytes.length);
            spectroscopyHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN).put(spectroscopyHeaderBytes).flip();

            ParkSpectroscopyHeader spectroscopyHeader = ParkFileVersion.VERSION_2.equals(version) ? ParkSpectroscopyHeader2.readIn(spectroscopyHeaderBuffer) : ParkSpectroscopyHeader.readIn(spectroscopyHeaderBuffer);


            TiffIFDEntry dataEntry = parser.getFirstIFDEntry(TAG_SPECTROSCOPY_DATA);
            byte[] curveDataBytes = FileInputUtilities.readInBytes(parser, dataEntry);

            if(curveDataBytes == null)
            {
                return sources;
            }

            ByteBuffer dataBuffer = ByteBuffer.allocate(curveDataBytes.length);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN).put(curveDataBytes).flip();

            int pointCount = spectroscopyHeader.getRecordingPointCount();
            int resolution = spectroscopyHeader.getDataInLineCount();
            int channelCount = spectroscopyHeader.getSpectroscopyChannelsCount();
            int drivingChannelIndex = spectroscopyHeader.getDrivingChannelIndex();

            String fileBareName = IOUtilities.getBareName(f);

            DoubleArrayReaderType readerType = DoubleArrayReaderType.INT16;

            int channelByteSize = resolution*readerType.getByteSize();

            SourceReadingState state  = pointCount > 10  ? new SourceReadingStateMonitored(pointCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
                new SourceReadingStateMute(pointCount);

            try{
                boolean isMap = spectroscopyHeader.isWellSpecifiedGridMap();
                Grid2D grid = spectroscopyHeader.buildGrid();

                for(int p = 0; p<pointCount; p++)
                {
                    if(readingDirectives.isCanceled())
                    {
                        state.setOutOfJob();
                    }
                    if(state.isOutOfJob())
                    {
                        return Collections.emptyList();
                    }

                    String suffix = " (" + p + ")";
                    String longName = f.getAbsolutePath() + suffix;
                    String shortName = fileBareName + suffix;

                    double[] approachDataXs = null;
                    double[] approachDataYs = null;

                    double[] withdrawDataXs = null;
                    double[] withdrawDataYs = null;

                    ParkSpectroscopyChannel xChannel = spectroscopyHeader.getSpectroscopyChannel(drivingChannelIndex);

                    ParkSpectroscopyChannel yChannel = spectroscopyHeader.getForceDistanceYChannel();
                    int yChannelIndex = yChannel.getChannelIndex();

                    PrefixedUnit xUnit = xChannel.getUnit();
                    PrefixedUnit yUnit = yChannel.getUnit();

                    Quantity xQuantity = Quantities.DISTANCE_MICRONS;
                    Quantity yQuantity = CalibrationState.getDefaultYQuantity(yUnit);

                    double xFactor = xUnit.getConversionFactorTo(xQuantity.getUnit());
                    double yFactor = yUnit.getConversionFactorTo(yQuantity.getUnit());

                    double xScale = xFactor*xChannel.getDataGain();
                    double yScale = yFactor*yChannel.getDataGain();

                    for(int s = 0; s < channelCount; s++)
                    {
                        if(s == drivingChannelIndex)
                        {
                            approachDataXs = readerType.readIn1DArray(resolution/2, xScale, 0, dataBuffer);
                            withdrawDataXs = readerType.readIn1DArray(resolution/2, xScale, 0, dataBuffer);
                        }
                        else if(s == yChannelIndex)
                        {
                            approachDataYs = readerType.readIn1DArray(resolution/2, yScale, 0, dataBuffer);
                            withdrawDataYs = readerType.readIn1DArray(resolution/2, yScale, 0, dataBuffer);
                        }
                        else
                        {
                            FileInputUtilities.skipBytes(channelByteSize, dataBuffer);
                        }
                    }

                    Channel1DData approachChannel = (approachDataXs != null && approachDataYs != null) ? new FlexibleFlatChannel1DData(approachDataXs, approachDataYs,xQuantity, yQuantity, SortedArrayOrder.DESCENDING) :  FlexibleChannel1DData.getEmptyInstance(xQuantity, yQuantity);  
                    Channel1DData withdrawChannel = (withdrawDataXs != null && withdrawDataYs != null) ? new FlexibleFlatChannel1DData(withdrawDataXs, withdrawDataYs, xQuantity, yQuantity, SortedArrayOrder.ASCENDING)  :  FlexibleChannel1DData.getEmptyInstance(xQuantity, yQuantity);

                    double springConstant = spectroscopyHeader.getSpringConstant(); //in N/m
                    double sensitivity = spectroscopyHeader.getSensitivity(); //in V/um

                    StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approachChannel, withdrawChannel);

                    source.setSpringConstant(springConstant);
                    source.setSensitivity(sensitivity);

                    if(isMap)
                    {
                        source.setRecordingPoint(grid.getPointFlattenedWithFullReturn(p));
                    }

                    sources.add(source);

                    state.incrementAbsoluteProgress();
                }

                if(isMap)
                {
                    MapSource<?> mapSource = new MapGridSource(f, sources, grid);  

                    ReadingPack<ImageSource> readingPack = null;
                    if(spectroscopyHeader.hasReferenceImage())
                    {
                        ParkImageReader imageReader = new ParkImageReader();
                        readingPack = new FileReadingPack<>(Collections.singletonList(f), imageReader);               
                    }

                    mapSource.setMapAreaImageReadingPack(readingPack);
                }

                return sources; 
            }
            catch (Exception e) 
            {
                state.setOutOfJob();
                throw e;
            }

        }
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }
    }
}