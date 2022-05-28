
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

import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.ImageChannel.ImageChannelBuilder;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;


public class ParkImageReader extends AbstractSourceReader<ImageSource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"tiff"};
    private static final String DESCRIPTION = "Park image (.tiff)";

    static final int PARK_MAGIC_NUMBER = 0x0E031301;

    static final int TAG_PARK_MAGIC_NUMBER = 50432;
    private static final int TAG_VERSION = 50433;
    private static final int TAG_HEADER = 50435;
    private static final int TAG_COMMENT = 50436;
    private static final int TAG_DATA = 50434;

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

    @Override
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirectives) throws UserCommunicableException 
    {      
        try(RandomAccessInputStream in = new RandomAccessInputStream(f.getAbsolutePath()))
        {
            return readSourcesFromInputStream(f, in); 
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }  
    }     

    protected List<ImageSource> readSourcesFromInputStream(File f, RandomAccessInputStream in)  throws UserCommunicableException 
    {
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

            //            System.out.println("MAGIC is PARK " + (PARK_MAGIC_NUMBER == ((Number)firstIFD.getIFDValue(TAG_PARK_MAGIC_NUMBER)).intValue()));


            ByteBuffer headerBuffer = ByteBuffer.allocate(headerBytes.length);
            headerBuffer.order(ByteOrder.LITTLE_ENDIAN).put(headerBytes).flip();

            ParkHeader header = ParkFileVersion.VERSION_2.equals(version) ? ParkHeader2.readIn(headerBuffer) : ParkHeader.readIn(headerBuffer);


            TiffIFDEntry dataEntry = parser.getFirstIFDEntry(TAG_DATA);
            byte[] imageDataBytes = FileInputUtilities.readInBytes(parser, dataEntry);

            ByteBuffer dataBuffer = ByteBuffer.allocate(imageDataBytes.length);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN).put(imageDataBytes).flip();

            DoubleArrayReaderType dataReader = header.getDataReader();

            Grid2D grid = header.getGrid();

            int rowCount = grid.getRowCount();
            int columnCount = grid.getColumnCount();

            double[][] data = new double[rowCount][];

            double dataGain = header.getDataGain();

            double zScale = dataGain*header.getZScale();
            double zOffset = dataGain*header.getZOffset();

            for(int i = 0; i<rowCount; i++)
            {
                data[i] = dataReader.readIn1DArray(columnCount, zScale, zOffset, dataBuffer);
            }

            String imageName = header.getSourceName();
            Quantity zQuantity = new UnitQuantity(imageName, header.getZUnit());

            ImageChannelBuilder channelBuilder = new ImageChannelBuilder();       
            channelBuilder.setZQuantity(zQuantity).setIdentifier(imageName);       
            channelBuilder.setData(data).setGrid(grid);

            ImageChannel channel = new ImageChannel(channelBuilder);
            List<ImageChannel> imageChannels = new ArrayList<>();
            imageChannels.add(channel);

            String comment = "";

            try {
                comment = firstIFD.getIFDStringValue(TAG_COMMENT);
            } catch (FormatException e) {
                e.printStackTrace();
            }

            in.close();

            ImageSource source = new StandardImageSource(f);
            source.setChannels(imageChannels);

            List<ImageSource> sourceFiles = Collections.singletonList(source);

            return sourceFiles; 
        }
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }
    }
}