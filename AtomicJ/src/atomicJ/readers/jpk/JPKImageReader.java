
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.readers.jpk;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.zip.ZipFile;

import org.jfree.chart.encoders.EncoderUtil;

import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.ImageChannel.ImageChannelBuilder;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;
import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.gui.BufferedImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.tiff.IFD;
import loci.formats.tiff.IFDList;
import loci.formats.tiff.TiffParser;


public class JPKImageReader extends AbstractSourceReader<ImageSource>
{    
    private static final String JPK_FORCE_EXTENSION = "force";
    private static final String JKP_JPK_EXTENSION = "jpk";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {JPK_FORCE_EXTENSION, JKP_JPK_EXTENSION};
    private static final String DESCRIPTION = "JPK image file (.force, .jpk)";

    private static final String FALSE = "false";
    private static final String TRACE = "Trace";
    private static final String RETRACE = "Retrace";
    private static final int TAG_U_LENGTH = 32834; 
    private static final int TAG_V_LENGTH = 32835;
    private static final int TAG_I_LENGTH =  32838; //or i-length
    private static final int TAG_J_LENGTH =  32839; //or i-length
    private static final int TAG_X_LOWER_BOUND = 32832;
    private static final int TAG_Y_LOWER_BOUND = 32833;
    private static final int TAG_LAST_INDEX = 32866;

    private static final int TAG_CHANNEL_TYPE = 32850;
    private static final int TAG_CHANNEL_ENCODER_TYPE = 32929;
    private static final int TAG_CHANNEL_ENCODER_MULTIPLIER = 32932;
    private static final int TAG_CHANNEL_ENCODER_OFFSET = 32933;
    private static final int TAG_CHANNEL_ENCODER_UNIT = 32930;
    static final int TAG_CHANNEL_INFO = 32851;
    private static final int TAG_DEFAULT_SLOT = 32897;
    private static final int TAG_NUM_SLOTS = 32896;
    private static final int TAG_FIRST_SLOT_NAME = 32912;
    private static final int TAG_INCREMENT = 48;

    static final String KEY_SEGMENT_STYLE = "segment-style.style";
    private static final String KEY_CHANNEL_RETRACE = "retrace";

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
        return filter.accept(f);       
    }

    @Override
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException 
    {      
        try
        {
            String extension = IOUtilities.getExtension(f);
            List<ImageSource> readInSources = (!JPK_FORCE_EXTENSION.equals(extension) && !JKP_JPK_EXTENSION.equals(extension)) ? readFromZipEntry(f) : readSourcesFromInputStream(f,  new RandomAccessInputStream(f.getAbsolutePath()));
            return readInSources; 
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }  
    }     

    private List<ImageSource> readFromZipEntry(File f) throws UserCommunicableException 
    {
        try
        {
            RandomAccessInputStream in = new RandomAccessInputStream(IOUtilities.getZipEntryBytes(f, JPK_FORCE_EXTENSION));

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
        try{

            TiffParser parser = new TiffParser(in);
            IFDList ifds = parser.getIFDs();           
            IFD firstIFD = parser.getFirstIFD();                

            Grid2D grid = readInGrid(firstIFD);

            List<ImageChannel> imageChannels = new ArrayList<>();

            for(int i = 1; i<ifds.size();i++)
            {  
                IFD ifd = ifds.get(i);
                ImageChannel channel = getImageChannel(parser, ifd, grid);
                imageChannels.add(channel);
            }

            in.close();


            ImageSource sourceFile = new StandardImageSource(f);
            sourceFile.setChannels(imageChannels);

            List<ImageSource> sourceFiles = Collections.singletonList(sourceFile);

            return sourceFiles; 
        }
        catch (IOException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");

        } catch (FormatException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading in the image");
        }    

    }

    private ImageChannel getImageChannel(TiffParser parser, IFD ifd, Grid2D grid) throws FormatException, IOException, UserCommunicableException
    {                               
        int[] bytesPerSample = ifd.getBytesPerSample();
        int bytesPerPixel = ArrayUtilities.total(bytesPerSample);

        int imageWidth = (int) ifd.getImageWidth();
        int imageLength = (int)ifd.getImageLength();

        int totalByteCount = imageWidth*imageLength*bytesPerPixel;

        byte[] buffer = new byte[totalByteCount];

        parser.getSamples(ifd, buffer);

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalByteCount);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.put(buffer);
        byteBuffer.flip();

        String channelType = (String) ifd.get(TAG_CHANNEL_TYPE);        

        String channelName = getChannelName(ifd, channelType);
        String defaultSlot = (String)ifd.get(TAG_DEFAULT_SLOT);
        int numSlots = (int)ifd.get(TAG_NUM_SLOTS);

        double multiplier = 1;
        double offset = 0;
        String encoderType = "";

        double prefixConversion = 1;        
        Quantity quantity = null;

        for(int i = 0; i<numSlots; i++)
        {
            String name = (String)ifd.get(TAG_FIRST_SLOT_NAME + i*48);

            if(defaultSlot.equals(name))
            {
                multiplier = (double) ifd.get(TAG_CHANNEL_ENCODER_MULTIPLIER + i*48);
                offset = (double) ifd.get(TAG_CHANNEL_ENCODER_OFFSET + i*48);

                String rawUnitName = (String) ifd.get(TAG_CHANNEL_ENCODER_UNIT + i*48);

                PrefixedUnit siUnit = UnitUtilities.getSIUnit(rawUnitName);
                PrefixedUnit defaultSIUnit = UnitUtilities.getDefaultUnit(siUnit);             
                prefixConversion = siUnit.getConversionFactorTo(defaultSIUnit);

                quantity = new UnitQuantity(channelType, defaultSIUnit);

                encoderType = (String)ifd.get(TAG_CHANNEL_ENCODER_TYPE);
                break;
            }
        }

        JPKEncodedDataReader reader = JPKEncodedDataReader.getDataReader(encoderType);

        JPKLinearConverter encodingConverter = new JPKLinearConverter(prefixConversion*multiplier, prefixConversion*offset);            

        double[][] data = reader.readIndData(byteBuffer, imageLength, imageWidth, encodingConverter);

        ImageChannelBuilder channelBuilder = new ImageChannelBuilder();       
        channelBuilder.setZQuantity(quantity).setIdentifier(channelName);       
        channelBuilder.setData(data).setGrid(grid);

        ImageChannel channel = new ImageChannel(channelBuilder);

        return channel;
    }

    protected String getChannelName(IFD ifd, String channelType)
    {
        String channelInfo = (String) ifd.get(TAG_CHANNEL_INFO);
        Map<String, String> curvesInforProperties = convertToMap(channelInfo, "\n", ":");

        String isRetraceString = curvesInforProperties.get(KEY_CHANNEL_RETRACE);
        String channelName = channelType;
        if(isRetraceString != null)
        {
            boolean isRetrace = FALSE.equalsIgnoreCase(isRetraceString);
            String direction = isRetrace ? RETRACE : TRACE;
            channelName = channelName  + " (" + direction + ")";
        }

        return channelName;
    }

    private static Grid2D readInGrid(IFD firstIFD) throws FormatException
    {                      
        int rowCount = (int) firstIFD.get(TAG_J_LENGTH);
        int columnCount = (int) firstIFD.get(TAG_I_LENGTH);

        double flankedWidth = 1e6*(double)firstIFD.get(TAG_U_LENGTH);
        double flankedHeight = 1e6*(double)firstIFD.get(TAG_V_LENGTH);

        double incrementX = flankedWidth/(columnCount);
        double incrementY = flankedHeight/(rowCount);

        double originX = 1e6*(double)firstIFD.get(TAG_X_LOWER_BOUND) + 0.5*incrementX;
        double originY = 1e6*(double)firstIFD.get(TAG_Y_LOWER_BOUND) + 0.5*incrementY;     

        Grid2D grid = new Grid2D(incrementX, incrementY, originX, originY, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);
        return grid;
    }

    static Map<String, String> convertToMap(String propertiesString, String fieldSeparator, String keyValueSeparator)
    {
        Map<String, String> properties = new LinkedHashMap<>();
        String[] lines = propertiesString.split(fieldSeparator);

        for(String line : lines)
        {
            int index = line.lastIndexOf(keyValueSeparator);
            boolean wellFormed = index >= 0;

            String key = line.substring(0, index).trim();
            String value = wellFormed ? line.substring(index + 1, line.length()).trim() : "";

            properties.put(key, value);
        }

        return properties;
    }

    public static boolean containsStandardImageEntry(ZipFile zipFile)
    {
        boolean containsEntry = IOUtilities.containsEntryByExtension(zipFile, JPK_FORCE_EXTENSION);

        return containsEntry;
    }

    private static void open(IFormatReader rawReader, String path)
    {
        BufferedImageReader myReader = new BufferedImageReader(rawReader);
        IMetadata meta = null;
        try
        {
            myReader.setId(path);
            int num = myReader.getImageCount();

            BufferedImage[] img = new BufferedImage[num];
            for (int i=0; i<num; i++) 
            {
                img[i] = myReader.openImage(i);
            }

            myReader.close(true);

            for(int i = 0; i<num;i++)
            {
                BufferedImage image = img[i];

                WritableRaster raster = image .getRaster();
                DataBufferByte dataBuffer   = (DataBufferByte) raster.getDataBuffer();

                String newName = "C:\\Users\\Paszczak\\Desktop\\JPK\\test" + "_" + i + ".jpg";
                OutputStream out = new BufferedOutputStream(new FileOutputStream(newName));

                EncoderUtil.writeBufferedImage(image, org.jfree.chart.encoders.ImageFormat.JPEG, out, 1.f); 
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

}

