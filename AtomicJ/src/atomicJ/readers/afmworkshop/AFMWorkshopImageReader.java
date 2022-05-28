
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

package atomicJ.readers.afmworkshop;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileExtensionPatternFilter;


public class AFMWorkshopImageReader extends AbstractSourceReader<ImageSource>
{ 
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"wsf"};
    private static final String DESCRIPTION = "AFMWorkshop image (.wsf)";

    private static final String DELIMITER_DATA = "((?:\\p{javaWhitespace}*)[\\n\\t]+(?:\\p{javaWhitespace}*))";
    private static final String DELIMITER_HEADER = "((?:\\p{javaWhitespace}*)[\\n]+(?:\\p{javaWhitespace}*))";

    private static final String PIXELS_IN_X = "Pixels in X";
    private static final String LINES_IN_Y = "Lines in Y";
    private static final String X_RANGE = "X Range";
    private static final String Y_RANGE = "Y Range";
    private static final String Z_CALIBRATION = "Z Calibration";
    private static final String X_CALIBRATION = "X Calibration";
    private static final String Y_CALIBRATION = "Y Calibration";
    private static final String X_OFFSET = "X Offset";
    private static final String Y_OFFSET = "Y Offset";
    private static final String DISPLAY_TYPE = "Display Type";

    private static final Pattern KEY_FIELD_PATTERN = Pattern.compile("(.+?):(.*)");

    @Override
    public List<ImageSource> readSources(File f, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalSpectroscopySourceException
    {        		
        try (Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(f),"ISO-8859-1"))))
        {					
            scanner.useLocale(Locale.US);           
            scanner.useDelimiter(DELIMITER_HEADER);     

            scanner.next(); //skips the first line with the name of the file,together with any adjacent empty lines        

            Map<String, String> keys = new LinkedHashMap<>();

            while(scanner.hasNext(KEY_FIELD_PATTERN))
            {                
                String keyField = scanner.next().trim();

                Matcher matcher = KEY_FIELD_PATTERN.matcher(keyField);
                boolean matches = matcher.matches();

                if(matches)
                {
                    String key = matcher.group(1).trim();
                    String value = matcher.group(2).trim();

                    keys.put(key, value);
                }             
            }

            double xLength = Double.parseDouble(keys.get(X_RANGE));
            double yLength = Double.parseDouble(keys.get(Y_RANGE));

            int xCount = Integer.parseInt(keys.get(PIXELS_IN_X));
            int yCount = Integer.parseInt(keys.get(LINES_IN_Y));

            double incrementX = xLength/(xCount - 1);
            double incrementY = yLength/(yCount - 1);

            Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, yCount, xCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

            List<ImageChannel> channels = new ArrayList<>();

            AFMWorkshopSignalType channelType = AFMWorkshopSignalType.getAFMWorkshopChannelType(keys.get(DISPLAY_TYPE));

            Quantity channelQuantity = new UnitQuantity(channelType.getName(), channelType.getDefaultUnit());

            scanner.useDelimiter(DELIMITER_DATA);     

            ChannelFilter channelFilter = readingDirective.getDataFilter();
            if(channelFilter.accepts(channelType.getName(), channelQuantity))
            {
                double[][] data = new double[yCount][xCount];

                for(int i = 0;i<yCount; i++)
                {
                    for(int j = 0; j<xCount; j++)
                    {
                        data[yCount - 1 - i][j]  = scanner.nextDouble();
                    }                           
                }   

                ImageChannel channel = new ImageChannel(data, grid, channelQuantity, channelType.getName(), true);
                channels.add(channel);
            }

            ImageSource sourceFile = new StandardImageSource(f);
            sourceFile.setChannels(channels);

            List<ImageSource> sourceFiles = Collections.singletonList(sourceFile);

            return sourceFiles; 

        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }
    }

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    public static boolean isRightRecordingType(File f)
    { 
        //only image files have the "wsf" extension
        return true;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        boolean accept = filter.accept(f) && isRightRecordingType(f);  

        return accept;
    }
}

