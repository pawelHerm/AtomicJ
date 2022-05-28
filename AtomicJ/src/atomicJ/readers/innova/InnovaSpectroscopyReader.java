
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Paweł Hermanowicz
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

package atomicJ.readers.innova;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.ForceCurveOrientation;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.OrderedPair;

/**
 * A reader to open the Innova (.dat) spectroscopy files. 
 *  *
 */
public class InnovaSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"dat"};
    private static final String DESCRIPTION = "Innova force curve file (.dat)";

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
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {      
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try(FileChannel channel = (FileChannel) Files.newByteChannel(f.toPath());Scanner fileScanner = new Scanner(channel,"ISO-8859-1"))
        {            
            fileScanner.useDelimiter("\\Z");
            fileScanner.useLocale(Locale.US);    

            String header = fileScanner.next();

            // Read in header, creating a list of NIDSections using {@link atomicJ.readers.nanosurf.NIDSection#INIT_SECTION_PATTERN} as a delimeter
            Map<String, InnovaINISection> sectionMap = new LinkedHashMap<>();

            try(Scanner headerScanner = new Scanner(header))
            {
                while(true)
                {
                    String sectionString = headerScanner.findWithinHorizon(InnovaINISection.INIT_SECTION_PATTERN, 0);

                    if(sectionString == null)
                    {
                        break;
                    }

                    InnovaINISection section = InnovaINISection.build(sectionString);               
                    sectionMap.put(section.getName(), section);
                }
            }

            InnovaINISection dataSection = sectionMap.get(InnovaSpectroscopyTextData.NAME);
            InnovaINISection seriesLayoutSection = sectionMap.get(InnovaSeriesLayout.NAME);
            InnovaINISection spectroscopyDecriptionSection = sectionMap.get(InnovaSpectroscopyData.NAME);

            InnovaSeriesLayout seriesLayout = new InnovaSeriesLayout(seriesLayoutSection);
            InnovaSpectroscopyData spectroscopyDescription = new InnovaSpectroscopyData(spectroscopyDecriptionSection);
            InnovaSpectroscopyTextData textData = new InnovaSpectroscopyTextData(dataSection, seriesLayout);

            int readInSeriesCount = textData.getReadInSeriesCount();

            OrderedPair<InnovaChannelHeader> approachChannelHeaders = seriesLayout.getBranchHeaders(ForceCurveBranch.APPROACH);
            OrderedPair<InnovaChannelHeader> withdrawChannelHeaders = seriesLayout.getBranchHeaders(ForceCurveBranch.WITHDRAW);

            if(approachChannelHeaders == null && withdrawChannelHeaders == null)
            {
                return sources;
            }

            PrefixedUnit nonNullDeflBranchUnit = approachChannelHeaders != null ? approachChannelHeaders.getSecond().getUnit() : withdrawChannelHeaders.getSecond().getUnit();

            Quantity zPositionQuantity = Quantities.DISTANCE_MICRONS;
            Quantity deflectionQuantity = CalibrationState.getDefaultYQuantity(nonNullDeflBranchUnit);

            Channel1DData emptyChannel = FlexibleChannel1DData.getEmptyInstance(zPositionQuantity, deflectionQuantity);

            SourceReadingState state  = readInSeriesCount> 10  ? new SourceReadingStateMonitored(readInSeriesCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
                new SourceReadingStateMute(readInSeriesCount);

            List<Channel1DData> approachChannels = (approachChannelHeaders == null) ? Collections.<Channel1DData>nCopies(readInSeriesCount, emptyChannel) : buildChannels(textData, approachChannelHeaders, ForceCurveBranch.APPROACH, zPositionQuantity, deflectionQuantity, spectroscopyDescription.getZStart(), spectroscopyDescription.getZEnd());       
            List<Channel1DData> withdrawChannels = (approachChannelHeaders == null) ? Collections.<Channel1DData>nCopies(readInSeriesCount, emptyChannel) : buildChannels(textData, withdrawChannelHeaders, ForceCurveBranch.WITHDRAW, zPositionQuantity, deflectionQuantity, spectroscopyDescription.getZEnd(), spectroscopyDescription.getZStart());       

            boolean addSuffix = readInSeriesCount > 1;

            for(int i = 0; i<readInSeriesCount; i++)
            {
                String suffix = addSuffix ? " (" + i + ")": "";

                String longName = f.getAbsolutePath() + suffix;
                String shortName = IOUtilities.getBareName(f) + suffix;

                Channel1DData apporoachChannel = approachChannels.get(i);
                Channel1DData withdrawChannel = withdrawChannels.get(i);

                if(!apporoachChannel.isEmpty() || !withdrawChannel.isEmpty())
                {
                    StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, apporoachChannel, withdrawChannel);
                    sources.add(source);
                }

            }
        } catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);     
        }

        return sources; 
    }  

    private List<Channel1DData> buildChannels(InnovaSpectroscopyTextData textData, OrderedPair<InnovaChannelHeader> channelHeaders,  ForceCurveBranch branch,  Quantity zPositionQuantity, Quantity deflectionQuantity, UnitExpression zBegin, UnitExpression zEnd)
    {
        List<Channel1DData> readInChannels = new ArrayList<>();

        InnovaChannelHeader zHeader = channelHeaders.getFirst();
        InnovaChannelHeader defHeader = channelHeaders.getSecond();

        PrefixedUnit zPositionUnit = zHeader.getUnit();
        PrefixedUnit deflUnit = defHeader.getUnit();

        double factorZPosition = zPositionUnit.getConversionFactorTo(zPositionQuantity.getUnit());
        double factorDeflection = deflUnit.getConversionFactorTo(deflectionQuantity.getUnit());

        double zBeginValue = zBegin.derive(zPositionUnit).getValue();
        double zEndValue = zEnd.derive(zPositionUnit).getValue();

        int seriesCount = textData.getReadInSeriesCount();

        for(int i = 0; i<seriesCount; i++)
        {
            double[][] points = textData.extractPointArray(i, factorZPosition, factorDeflection, zHeader.getColumnIndex(), defHeader.getColumnIndex(), zBeginValue, zEndValue);

            double[][] pointsSorted = points.length > 1 ? ForceCurveOrientation.LEFT.correctOrientation(points, branch) : points;

            Channel1DData channel = new FlexibleChannel1DData(pointsSorted, zPositionQuantity, deflectionQuantity, branch.getDefaultXOrderForLeftHandSideContactOrientation());
            readInChannels.add(channel);
        }     

        return readInChannels;
    }
}

