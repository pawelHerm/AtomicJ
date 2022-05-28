
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

package atomicJ.readers.text;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;


public abstract class TextDelimitedImageReader extends AbstractSourceReader<ImageSource>
{ 
    private static final String X_QUANTITY = "XQuantity";
    private static final String Y_QUANTITY = "YQuantity";

    private static final String X_COUNT = "XCount";
    private static final String Y_COUNT = "YCount";

    private static final String X_LENGTH = "XLength";
    private static final String Y_LENGTH = "YLength";

    private static final String CHANNEL = "Channel";

    private static final String DATA = "Data";

    protected static final String TYPE = "Type";
    protected static final String IMAGE = "Image";

    private static final Pattern BRACKETS_CONTENT_PATTERN = Pattern.compile(".+?\\((.*)\\)");
    private static final Pattern CHANNEL_NAME_PATTERN = Pattern.compile("(.+?)\\(.*\\)");

    protected abstract String getDelimiter();

    @Override
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingState) throws UserCommunicableException, IllegalSpectroscopySourceException
    {        		
        try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
        {					
            try(Scanner scanner = new Scanner(channel);) 
            {
                scanner.useLocale(Locale.US);
                scanner.useDelimiter(getDelimiter());     

                String xQuantityString = "";
                String yQuantityString = "";

                int xCount = 1;
                int yCount = 1;

                double xLength = 1;
                double yLength = 1;

                boolean dataFound = false;

                while(scanner.hasNextLine())
                {
                    String parametersLine = scanner.nextLine().trim();

                    if(parametersLine.isEmpty())
                    {
                        parametersLine = scanner.nextLine().trim();
                    }

                    String[] splitted = parametersLine.split(getDelimiter());         

                    String key = splitted[0].trim();

                    if(TYPE.equals(key))
                    {
                        boolean isImage = IMAGE.equals(splitted[1]);

                        if(!isImage)
                        {
                            throw new IllegalSpectroscopySourceException();
                        }
                    }
                    if(X_QUANTITY.equals(key))
                    {
                        xQuantityString = splitted[1];
                    }
                    else if(Y_QUANTITY.equals(key))
                    {
                        yQuantityString = splitted[1];
                    }
                    else if(X_COUNT.equals(key))
                    {
                        xCount = Integer.parseInt(splitted[1]);
                    }
                    else if(Y_COUNT.equals(key))
                    {
                        yCount = Integer.parseInt(splitted[1]);
                    }
                    else if(X_LENGTH.equals(key))
                    {
                        xLength = Double.parseDouble(splitted[1]);
                    }
                    else if(Y_LENGTH.equals(key))
                    {
                        yLength = Double.parseDouble(splitted[1]);
                    }
                    else if(DATA.equals(key))
                    {
                        dataFound = true;
                        break;
                    }
                }

                if(!dataFound)
                {
                    throw new IllegalStateException("The image data were not found in the file");
                }         

                Quantity xQuantity = Quantities.DISTANCE_MICRONS;
                Quantity yQuantity = Quantities.DISTANCE_MICRONS;

                PrefixedUnit xUnit = extractUnit(xQuantityString);
                PrefixedUnit yUnit = extractUnit(yQuantityString);

                double factorX = xUnit.getConversionFactorTo(xQuantity.getUnit());
                double factorY = yUnit.getConversionFactorTo(yQuantity.getUnit());

                double incrementX = factorX*xLength/xCount;
                double incrementY = factorY*yLength/yCount;

                Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, yCount, xCount, xQuantity, yQuantity);

                List<ImageChannel> channels = new ArrayList<>();

                while(scanner.hasNextLine())
                {
                    String parametersLine = scanner.nextLine().trim();

                    if(parametersLine.isEmpty())
                    {
                        parametersLine = scanner.nextLine().trim();
                    }

                    String[] splitted = parametersLine.split(getDelimiter());         

                    String key = splitted[0].trim();

                    boolean channelReadIn = false;

                    if(CHANNEL.equals(key))
                    {
                        String channelLine = scanner.nextLine().trim();
                        String channelQuantityString = channelLine.split(getDelimiter())[1];
                        //parametersLine = scanner.nextLine().trim();

                        PrefixedUnit unit = extractUnit(channelQuantityString);

                        Matcher zNameMatcher = CHANNEL_NAME_PATTERN.matcher(channelQuantityString);
                        zNameMatcher.matches();

                        String channelQuantityName = zNameMatcher.group(1).trim();

                        Quantity channelQuantity = new UnitQuantity(channelQuantityName, unit);

                        ChannelFilter filter = readingState.getDataFilter();
                        if(filter.accepts(channelQuantityName, channelQuantity))
                        {
                            double[][] data = new double[yCount][xCount];

                            for(int i = 0;i<yCount; i++)
                            {
                                for(int j = 0; j<xCount; j++)
                                {
                                    data[i][j]  = scanner.nextDouble();
                                }                           
                            }   

                            ImageChannel ch = new ImageChannel(data, grid, channelQuantity, channelQuantityName, true);

                            channels.add(ch);

                        }

                        //even if the channel is to be skipped, we have to read in it, to move scanner to the right position
                        else
                        {
                            for(int i = 0;i<yCount; i++)
                            {
                                for(int j = 0; j<xCount; j++)
                                {
                                    scanner.nextDouble();
                                }                           
                            }  
                        }
                        channelReadIn = true;
                    }
                    if(!channelReadIn)
                    {
                        throw new IllegalStateException("No channels were not found in the file");
                    }
                }        

                ImageSource source = new StandardImageSource(f);
                source.setChannels(channels);

                List<ImageSource> sourceFiles = Collections.singletonList(source);

                return sourceFiles; 
            } 
            catch (Exception e) 	
            {
                e.printStackTrace();
                throw new UserCommunicableException("Error occured while reading the file", e);
            }    
        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    private static PrefixedUnit extractUnit(String label)
    {
        Matcher matcher = BRACKETS_CONTENT_PATTERN.matcher(label);
        matcher.matches();
        String unitString = matcher.group(1).trim();

        PrefixedUnit unit = UnitUtilities.getSIUnit(unitString);

        return unit;
    }

    public boolean isRightRecordingType(File f)
    {
        boolean isImage = false;

        try(BufferedReader bsr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));) 
        {   
            String line;
            while((line = bsr.readLine()) != null)
            {
                if(line.isEmpty())
                {
                    continue;
                }
                if(line.contains(TextDelimitedImageReader.TYPE))
                {               
                    String[] splitted = line.split(getDelimiter());                
                    isImage = TextDelimitedImageReader.IMAGE.equals(splitted[1]);
                }
                break;
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        } 

        return isImage;
    }
}

