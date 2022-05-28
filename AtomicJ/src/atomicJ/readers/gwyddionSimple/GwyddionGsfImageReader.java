
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

package atomicJ.readers.gwyddionSimple;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;

public class GwyddionGsfImageReader extends AbstractSourceReader<ImageSource>
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"gsf"};
    private static final String DESCRIPTION = "Gwyddion simple format image (.gsf)";

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
    public List<ImageSource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {           
        List<ImageSource> sources = new ArrayList<>();

        try(FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath());) 
        {        
            ByteBuffer magicBuffer = FileInputUtilities.readBytesToBuffer(channel, GwyddionGsfSourceReader.GSF_MAGIC_LINE_STRING.length(), ByteOrder.LITTLE_ENDIAN); 
            String magic = StandardCharsets.UTF_8.decode(magicBuffer).toString();

            if(!GwyddionGsfSourceReader.GSF_MAGIC_LINE_STRING.equals(magic))
            {
                throw new UserCommunicableException("The magic line is not " + GwyddionGsfSourceReader.GSF_MAGIC_LINE_STRING);
            }          

            int remainingBytes = (int) (channel.size() - GwyddionGsfSourceReader.GSF_MAGIC_LINE_STRING.length());

            ByteBuffer byteBuffer = FileInputUtilities.readBytesToBuffer(channel, remainingBytes, ByteOrder.LITTLE_ENDIAN);

            String headerText = FileInputUtilities.readInNullTerminatedString(byteBuffer);
            GwyddionSimpleFormatHeader header = GwyddionSimpleFormatHeader.buildHeader(headerText);

            if(!header.areRasterDimensionsSpecified())
            {
                throw new UserCommunicableException("The header does not specify the raster size of the image");     
            }

            int textHeaderLengthInBytes = byteBuffer.position() - 1;//we subtract 1, because we already read in one null (calling FileInputUtilities.readInNullTerminatedString)

            int remainingNullBytes = 4 - ((textHeaderLengthInBytes + GwyddionGsfSourceReader.GSF_MAGIC_LINE_STRING.length()) % 4) - 1;//we subtract 1, because we already read in one null (calling FileInputUtilities.readInNullTerminatedString)

            FileInputUtilities.skipBytes(remainingNullBytes, byteBuffer);

            Grid2D grid = header.buildGrid();
            double[][] channelData = DoubleArrayReaderType.FLOAT32.readIn2DArrayRowByRowReversed(grid.getRowCount(), grid.getColumnCount(), 1, byteBuffer);

            Quantity zQuantity = header.getZQuantity();
            boolean isTrace = true;
            String title = header.getTitle();

            ImageChannel imageChannel = new ImageChannel(channelData, grid, zQuantity, title, isTrace);
            ImageSource sourceFile = new StandardImageSource(f);
            sourceFile.setChannels(Collections.singletonList(imageChannel));
            sources.add(sourceFile);
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;        
    }

    private static class GwyddionSimpleFormatHeader
    {
        private static final String X_RES_FIELD = "XRes";
        private static final String Y_RES_FIELD = "YRes";
        private static final String X_REAL_FIELD = "XReal";
        private static final String Y_REAL_FIELD = "YReal";
        private static final String XY_UNITS_FIELD = "XYUnits";
        private static final String Z_UNITS_FIELD = "ZUnits";
        private static final String TITLE_FIELD = "Title";

        private static final String KEY_VALUE_SEPARATOR = "=";
        private static final String DEFAULT_TITLE = "Image";

        private int xRes = -1;
        private int yRes = -1;
        private double xReal = 1;//default value if not specified
        private double yReal = 1;//default value if not specified
        private PrefixedUnit xyUnits;
        private PrefixedUnit zUnit;
        private String title;

        private GwyddionSimpleFormatHeader(){};

        public boolean areRasterDimensionsSpecified()
        {
            boolean wellSpecified = (xRes > 0 && yRes > 0);

            return wellSpecified;
        }

        public Grid2D buildGrid()
        {    
            if(!areRasterDimensionsSpecified())
            {
                return null;
            }

            Quantity xyQuantity = StandardQuantityTypes.LENGTH.isCompatible(xyUnits) ? Quantities.DISTANCE_MICRONS : new UnitQuantity("", xyUnits);

            double factor = xyUnits.getConversionFactorTo(xyQuantity.getUnit());

            double incrementX = (xRes > 1) ? factor*xReal/(xRes - 1) : 1;
            double incrementY = (yRes > 1) ? factor*yReal/(yRes - 1) : 1;

            Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, yRes, xRes, xyQuantity, xyQuantity);

            return grid;
        }

        public String getTitle()
        {
            return title;
        }

        public Quantity getZQuantity()
        {
            return new UnitQuantity(title, zUnit);
        }

        public static GwyddionSimpleFormatHeader buildHeader(String headerText)
        {
            GwyddionSimpleFormatHeader header = new GwyddionSimpleFormatHeader();
            String lines[] = headerText.split("\\r?\\n");

            Map<String, String> keyValuePairs = new HashMap<>();
            for(String line : lines)
            {
                String[] keyValue = line.split(KEY_VALUE_SEPARATOR);
                if(keyValue.length == 2)
                {
                    keyValuePairs.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            header.xRes = keyValuePairs.containsKey(X_RES_FIELD) ? Integer.valueOf(keyValuePairs.get(X_RES_FIELD)) : header.xRes;
            header.yRes = keyValuePairs.containsKey(Y_RES_FIELD) ? Integer.valueOf(keyValuePairs.get(Y_RES_FIELD)) : header.yRes;
            header.xReal = keyValuePairs.containsKey(X_REAL_FIELD) ? Double.valueOf(keyValuePairs.get(X_REAL_FIELD)) : header.xReal;
            header.yReal = keyValuePairs.containsKey(Y_REAL_FIELD) ? Double.valueOf(keyValuePairs.get(Y_REAL_FIELD)) : header.yReal;
            header.xyUnits = keyValuePairs.containsKey(XY_UNITS_FIELD) ? UnitUtilities.getSIUnit(keyValuePairs.get(XY_UNITS_FIELD)) : SimplePrefixedUnit.getNullInstance();
            header.zUnit = keyValuePairs.containsKey(Z_UNITS_FIELD) ? UnitUtilities.getSIUnit(keyValuePairs.get(Z_UNITS_FIELD)) : SimplePrefixedUnit.getNullInstance();
            header.title = keyValuePairs.containsKey(TITLE_FIELD) ? keyValuePairs.get(TITLE_FIELD) : DEFAULT_TITLE;

            return header;
        }
    }
}

