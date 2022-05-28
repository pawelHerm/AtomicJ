
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

package atomicJ.readers.gwyddionGwy;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;

public class GwyddionGwyImageReader extends AbstractSourceReader<ImageSource>
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"gwy"};
    private static final String DESCRIPTION = "Gwyddion native format image (.gwy)";

    private static final String DEFAULT_IMAGE_TITLE_ROOT = "Image";

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
            ByteBuffer gwyContainerData = GwyddionGwySourceReader.readGwyContainerData(channel);

            Map<String, GwyDataField> gwyDataFields = new LinkedHashMap<>();
            Map<String, Integer> componentByteBufferPositions = new HashMap<>();

            while(gwyContainerData.hasRemaining())
            {
                String componentName = FileInputUtilities.readInNullTerminatedString(gwyContainerData);
                componentByteBufferPositions.put(componentName, Integer.valueOf(gwyContainerData.position()));

                char componentTypeChar = (char)gwyContainerData.get();
                GwyDataType componentType = GwyDataType.getDataType(componentTypeChar);                

                if(GwyDataTypeSimple.OBJECT.equals(componentType))
                {
                    String objectName = FileInputUtilities.readInNullTerminatedString(gwyContainerData);

                    if(GwyDataField.GWY_DATA_FIELD_NAME.equals(objectName.trim()))
                    {                        
                        GwyDataField dataField = GwyDataField.readInObjectExceptForName(gwyContainerData);
                        gwyDataFields.put(componentName, dataField);                        
                    }
                    else
                    {
                        //we cannot just call GwyDataTypeSimple.OBJECT.skip(), because we have already read in the object name
                        long dataSize = FileInputUtilities.getUnsigned(gwyContainerData.getInt());

                        int position = gwyContainerData.position();
                        gwyContainerData.position(position + (int)dataSize);
                    }
                }
                else
                {
                    componentType.skipData(gwyContainerData); 
                }
            }

            List<ImageChannel> imageChannels = new ArrayList<>();
            List<String> titleRoots = new ArrayList<>();

            //we are executing this field after the while loop so that the order of components does not matter
            for(Entry<String, GwyDataField> entry : gwyDataFields.entrySet())
            {
                String gwyDataFieldComponentName = entry.getKey();
                GwyDataField dataField = entry.getValue();

                if(!dataField.isWellSpecifiedSpatialImage())
                {
                    continue;
                }

                String titleComponentName = new StringBuilder(gwyDataFieldComponentName).append(GwyddionGwySourceReader.COMPONENT_NAME_SEPARATOR).append(GwyddionGwySourceReader.TITILE_COMPONENT_NAME_SUFFIX).toString();  

                Integer titlePosition = (titleComponentName != null) ? componentByteBufferPositions.get(titleComponentName) : null;
                String titleRoot = DEFAULT_IMAGE_TITLE_ROOT;
                if(titlePosition != null)
                {
                    gwyContainerData.position(titlePosition);

                    char titleType = (char)gwyContainerData.get(); 
                    titleRoot = (GwyDataTypeSimple.STRING.getTypeChar() == titleType) ? FileInputUtilities.readInNullTerminatedString(gwyContainerData) : titleRoot;                  
                }

                titleRoots.add(titleRoot);

                int titleFrequency = Collections.frequency(titleRoots, titleRoot);
                String title =  titleFrequency> 1 ? titleRoot + " (" + titleFrequency + ")": titleRoot;                                     
                Quantity zQuantity = new UnitQuantity(titleRoot, dataField.getValueUnit());

                Grid2D grid = dataField.buildGrid();
                double[][] channelData = dataField.getChannelData();
                boolean isTrace = true;
                ImageChannel imageChannel = new ImageChannel(channelData, grid, zQuantity, title, isTrace);
                imageChannels.add(imageChannel);              
            }


            if(!imageChannels.isEmpty())
            {
                ImageSource sourceFile = new StandardImageSource(f);
                sourceFile.setChannels(imageChannels);
                sources.add(sourceFile);
            }
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;        
    }

    static class GwyDataField
    {
        static final String GWY_DATA_FIELD_NAME = "GwyDataField"; 

        private static final String XRES_COMPONENT = "xres";
        private static final String YRES_COMPONENT = "yres";
        private static final String XREAL_COMPONENT = "xreal";
        private static final String YREAL_COMPONENT = "yreal";
        private static final String XOFF_COMPONENT = "xoff";
        private static final String YOFF_COMPONENT = "yoff";
        private static final String LATERAL_DIMENSIONS_UNIT_COMPONENT = "si_unit_xy";
        private static final String VALUE_UNIT_COMPONENT = "si_unit_z";
        private static final String DATA_COMPONENT = "data";

        private int xRes = -1;
        private int yRes = -1;
        private double xReal = -1;//Horizontal dimension in physical units
        private double yReal = -1;//Vertical dimension in physical units 
        private double xOff; //Horizontal offset of the top-left corner in physical units
        private double yOff; //Vertical offset of the top-left corner in physical units
        private PrefixedUnit lateralDimensionsUnit;
        private PrefixedUnit valueUnit;

        private double[][] channelData;

        private GwyDataField(){}

        public boolean isWellSpecifiedSpatialImage() 
        {
            boolean wellSpecified = (xRes > 0 && yRes > 0 && xReal > 0 && yReal > 0 && (lateralDimensionsUnit != null) 
                    && (valueUnit != null) && (channelData != null));

            wellSpecified = wellSpecified && StandardQuantityTypes.LENGTH.isCompatible(lateralDimensionsUnit);

            return wellSpecified;
        }

        public PrefixedUnit getValueUnit()
        {
            return valueUnit;
        }

        public Grid2D buildGrid()
        {    
            if(!isWellSpecifiedSpatialImage())
            {
                return null;
            }

            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
            Quantity yQuantity = Quantities.DISTANCE_MICRONS;

            double factor = lateralDimensionsUnit.getConversionFactorTo(xQuantity.getUnit());

            double incrementX = (xRes > 1) ? factor*xReal/(xRes - 1) : 1;
            double incrementY = (yRes > 1) ? factor*yReal/(yRes - 1) : 1;

            Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, yRes, xRes, xQuantity, yQuantity);

            return grid;
        }

        public double[][] getChannelData()
        {
            return channelData;
        }

        private static GwyDataField readInObjectExceptForName(ByteBuffer byteBuffer) throws UserCommunicableException
        {
            GwyDataField dataField = new GwyDataField();

            long dataSize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
            int finalPosition = byteBuffer.position() + (int)dataSize;

            int dataPosition = -1;
            long dataArraySize = -1;

            while(byteBuffer.position() < finalPosition)
            {
                String componentNameTrimmed = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
                char componentTypeChar = (char)byteBuffer.get();

                if(XRES_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.INTEGER_32_BIT.getTypeChar() == componentTypeChar)
                {
                    dataField.xRes = byteBuffer.getInt();                   
                }
                else if(YRES_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.INTEGER_32_BIT.getTypeChar() == componentTypeChar)
                {
                    dataField.yRes = byteBuffer.getInt();
                }
                else if(XREAL_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.xReal = byteBuffer.getDouble();
                }
                else if(YREAL_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.yReal = byteBuffer.getDouble();
                }
                else if(XOFF_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.xOff = byteBuffer.getDouble();
                }
                else if(YOFF_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.yOff = byteBuffer.getDouble();
                }
                else if(LATERAL_DIMENSIONS_UNIT_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    dataField.lateralDimensionsUnit = gwyUnit.getUnit();
                }
                else if(VALUE_UNIT_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {                                        
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    dataField.valueUnit = gwyUnit.getUnit();
                }
                else if(DATA_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeArray.DOUBLE_ARRAY.getTypeChar() == componentTypeChar)
                {
                    //we delay reading the data in case xres and yres components appear after data component
                    dataArraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
                    dataPosition = byteBuffer.position();
                    byteBuffer.position(dataPosition + (int)(GwyDataTypeArray.DOUBLE_ARRAY.getItemSize()*dataArraySize));
                }
                else
                {
                    GwyDataType.getDataType(componentTypeChar).skipData(byteBuffer);
                }
            }
            if(dataPosition > -1)
            {
                byteBuffer.position(dataPosition);
                dataField.channelData = DoubleArrayReaderType.FLOAT64.readIn2DArrayRowByRowReversed(dataField.yRes, dataField.xRes, 1, byteBuffer);
            }

            byteBuffer.position(finalPosition);

            return dataField;
        }
    }
}

