package atomicJ.readers.wsxm;


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


import java.io.*;
import java.util.*;
import java.util.regex.*;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.ForceCurveOrientation;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;


public class WSXMSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final Pattern voltUnitPattern = Pattern.compile("\\s*([numkM]?)V");
    private static final Pattern meterUnitPattern = Pattern.compile("\\s*([numkM]?)m");
    private static final Pattern newtonUnitPattern = Pattern.compile("\\s*([numkM]?)N");

    private static final String HEADER_END = "Header end";
    private static final String CURVE_SIGNATURE = "FZ curve file";
    private static final String GENERAL_CURVE_SIGNATURE = "Generic curve file";
    private static final String IMAGE_SIGNATURE = "SxM Image file";


    //units and labels of axes are under this title
    private static final String GENERAL_INFO_TITLE = "General Info";
    private static final String X_AXIS_TEXT_LABEL = "X axis text";
    private static final String Y_AXIS_TEXT_LABEL = "Y axis text";
    private static final String X_AXIS_UNIT_LABEL = "X axis unit";
    private static final String Y_AXIS_UNIT_LABEL = "Y axis unit";
    private static final String NUMBER_OF_POINTS_LABEL = "Number of points";
    private static final String NUMBER_OF_LINES_LABEL = "Number of lines";

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"cur", "fz.cur"};
    private static final String DESCRIPTION = "WSxM force curve file (.fz.cur, .cur)";

    final static Pattern squareBracketsPattern = Pattern.compile("\\[(.*?)\\]");

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);       
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirectives) throws UserCommunicableException, IllegalImageException 
    {           
        boolean isBinary = false;

        String xAxisUnit = "";
        String yAxisUnit = "";
        String xAxisLabel = "";
        String yAxisLabel = "";

        boolean deflectionForceCalibrated = false;
        boolean deflectionSensitivityCalibrated = false;
        boolean amplitudeSensitivityCalibrated = false;

        double factorX = 1;            
        double factorY = 1;
        double factorZAmplitude = 1;

        int pointCount = 0;
        int lineCount = 0;

        try(BufferedReader bsr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));) 
        {
            bsr.readLine(); //we skip first line, it always should be "WSxM file copyright Nanotec Electronica"
            String signature = bsr.readLine();

            boolean isImage = IMAGE_SIGNATURE.equals(signature);
            if(isImage)
            {
                throw new IllegalImageException();
            }

            String line; 
            while((line = bsr.readLine().trim()) != null)
            {
                Matcher titleMatcher = squareBracketsPattern.matcher(line);
                boolean titleMatches = titleMatcher.matches();
                if(titleMatches)
                {
                    String title = titleMatcher.group(1);
                    if(HEADER_END.equals(title))
                    {
                        break;
                    }
                } 
                if(line.startsWith(NUMBER_OF_LINES_LABEL))
                {                    
                    lineCount = Integer.parseInt(line.split(":")[1].trim());
                }
                else if(line.startsWith(NUMBER_OF_POINTS_LABEL))
                {
                    pointCount = Integer.parseInt(line.split(":")[1].trim());
                }
                else if(line.startsWith(X_AXIS_UNIT_LABEL))
                {
                    xAxisUnit = line.split(":")[1].trim();

                    Matcher meterMatcher = meterUnitPattern.matcher(xAxisUnit);
                    boolean meterMatches = meterMatcher.matches();
                    if(meterMatches)
                    {
                        String prefix = meterMatcher.group(1);

                        factorX = 1e6*UnitUtilities.getPrefixValue(prefix).getConversion();
                    } 
                }
                else if(line.startsWith(Y_AXIS_UNIT_LABEL))
                {
                    yAxisUnit = line.split(":")[1].trim();

                    Matcher voltMatcher = voltUnitPattern.matcher(yAxisUnit);
                    Matcher meterMatcher = meterUnitPattern.matcher(yAxisUnit);
                    Matcher newtonMatcher = newtonUnitPattern.matcher(yAxisUnit);

                    boolean voltMatches = voltMatcher.matches();
                    boolean meterMatches = meterMatcher.matches();
                    boolean newtonMatches = newtonMatcher.matches();

                    deflectionForceCalibrated = newtonMatches;
                    deflectionSensitivityCalibrated = meterMatches; 

                    if(newtonMatches)
                    {
                        String prefix = newtonMatcher.group(1);
                        factorY = factorY*1e9*UnitUtilities.getPrefixValue(prefix).getConversion();
                    }
                    else if(meterMatches)
                    {
                        String prefix = meterMatcher.group(1);
                        factorY = factorY*1e6*UnitUtilities.getPrefixValue(prefix).getConversion();
                    }
                    else if(voltMatches)
                    {
                        String prefix = voltMatcher.group(1);
                        factorY = factorY*UnitUtilities.getPrefixValue(prefix).getConversion();
                    }    
                }
                else if(line.startsWith(X_AXIS_TEXT_LABEL))
                {
                    xAxisLabel = line.split(":")[1].trim();      
                }
                else if(line.startsWith(Y_AXIS_TEXT_LABEL))
                {
                    yAxisLabel = line.split(":")[1].trim();                
                }
            }

            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            try(Scanner scanner = new Scanner(bsr);) 
            {
                scanner.useLocale(Locale.US);

                double[][][] allLines = new double[lineCount][pointCount][];

                for(int j = 0; j<pointCount; j++)
                {     
                    for(int i = 0; i<lineCount; i++)
                    {
                        double x = factorX*scanner.nextDouble();
                        double y = factorY*scanner.nextDouble();

                        allLines[i][j] = new double[] {x,y};     
                    }

                }         

                Quantity yQuantity = deflectionForceCalibrated ? Quantities.FORCE_NANONEWTONS : (deflectionSensitivityCalibrated ? Quantities.DEFLECTION_MICRONS: Quantities.DEFLECTION_VOLTS);

                StandardSimpleSpectroscopySource source = buildSource(f, "", Quantities.DISTANCE_MICRONS, yQuantity, allLines, lineCount, true);

                sources.add(source);
            } 


            return sources;
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 
    }

    /*
     * AtomicJ requires that the force curves have a specified orientation:
     * 
     * - the in-contact region corresponds to smaller values of argument (x coordinate) (i.e. left part of the curve domain)
     * - the points in the approach part of a force curve are sorted in decreasing order in terms of x coordinate
     * - the points in the withdraw part of a force curve are sorted in increasing order in terms of y coordinate
     */


    private static StandardSimpleSpectroscopySource buildSource(File f, String suffix, Quantity xQuantity, Quantity yQuantity, double[][][] lines, int lineCount, boolean firstForward)
    {
        String longName = f.getAbsolutePath() + suffix;
        String shortName = IOUtilities.getBareName(f) + suffix;

        int approachIndex = -1;
        int withdrawIndex = -1;

        if(firstForward)
        {
            approachIndex = 0;
            withdrawIndex = lineCount > 1 ? 1 : -1;
        }
        else
        {
            withdrawIndex = 0;
            approachIndex= lineCount > 1 ? 1 : -1;
        }

        Channel1DData approachDeflectionData = (approachIndex > -1) ? new FlexibleChannel1DData(ForceCurveOrientation.LEFT.correctOrientation(lines[approachIndex], ForceCurveBranch.APPROACH), xQuantity, yQuantity, SortedArrayOrder.DESCENDING): FlexibleChannel1DData.getEmptyInstance(xQuantity, yQuantity);
        Channel1DData withdrawDeflectionData = (withdrawIndex > -1) ? new FlexibleChannel1DData(ForceCurveOrientation.LEFT.correctOrientation(lines[withdrawIndex], ForceCurveBranch.WITHDRAW), xQuantity, yQuantity, SortedArrayOrder.ASCENDING): FlexibleChannel1DData.getEmptyInstance(xQuantity, yQuantity);

        StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approachDeflectionData, withdrawDeflectionData);

        return source;
    }
}


