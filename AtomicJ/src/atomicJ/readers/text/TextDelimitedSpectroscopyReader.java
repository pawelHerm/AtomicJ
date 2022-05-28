
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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.analysis.ForceCurveChannelStorage;
import atomicJ.analysis.ForceCurveOrientation;
import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.IOUtilities;
import gnu.trove.list.array.TDoubleArrayList;



public abstract class TextDelimitedSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final Pattern BRACKETS_CONTENT_PATTERN = Pattern.compile(".+?\\((.*)\\)");

    protected abstract String getDelimiter();

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException 
    {
        try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
        {                   
            try(Scanner scanner = new Scanner(channel);) 
            {
                List<SimpleSpectroscopySource> sources = new ArrayList<>();

                scanner.useLocale(Locale.US);
                scanner.useDelimiter(getDelimiter());     

                String unitsLine = scanner.nextLine().trim();

                while(unitsLine.isEmpty() && scanner.hasNext())
                {
                    unitsLine = scanner.nextLine().trim();
                }

                if(unitsLine.startsWith(TextDelimitedImageReader.TYPE))
                {               
                    String[] splitted = unitsLine.split(getDelimiter());                
                    boolean isImage = TextDelimitedImageReader.IMAGE.equals(splitted[1]);

                    if(isImage)
                    {
                        throw new IllegalImageException();
                    }               
                }   

                String[] units = unitsLine.split(getDelimiter());

                String xLabel = units[0];
                String yLabel = units[1];

                PrefixedUnit xUnit = extractUnit(xLabel);
                Quantity xQuantity = Quantities.DISTANCE_MICRONS;

                PrefixedUnit yUnit = extractUnit(yLabel);
                Quantity yQuantity = CalibrationState.getDefaultYQuantity(yUnit);

                double factorX = xUnit.getConversionFactorTo(xQuantity.getUnit());
                double factorY = yUnit.getConversionFactorTo(yQuantity.getUnit());

                TDoubleArrayList xs = new TDoubleArrayList();
                TDoubleArrayList ys = new TDoubleArrayList();

                while(scanner.hasNextDouble())
                {
                    double x = factorX*scanner.nextDouble();
                    double y = factorY*scanner.nextDouble();

                    xs.add(x);
                    ys.add(y);                  
                }  

                double[] xsArray = xs.toArray();
                double[] ysArray = ys.toArray();

                ForceCurveChannelStorage fc = ForceCurveOrientation.partition(xsArray, ysArray, xQuantity, yQuantity);

                StandardSimpleSpectroscopySource source = buildSource(f, "", fc.getApproach(), fc.getWithdraw());

                PhotodiodeSignalType photodiodeSignalType = PhotodiodeSignalType.getSignalType(yUnit, PhotodiodeSignalType.VOLTAGE);
                source.setPhotodiodeSignalType(photodiodeSignalType);

                sources.add(source);

                return sources;
            } 
            catch (RuntimeException e) 
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
        String unitString = matcher.group(1);

        PrefixedUnit unit = UnitUtilities.getSIUnit(unitString);

        return unit;
    }

    private static StandardSimpleSpectroscopySource buildSource(File f, String suffix, Channel1DData approach, Channel1DData withdraw)
    {
        String longName = f.getAbsolutePath() + suffix;

        String shortName = IOUtilities.getBareName(f) + suffix;
        StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approach, withdraw);

        return source;
    }
}