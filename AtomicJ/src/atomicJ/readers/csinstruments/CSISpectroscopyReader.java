/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2018 by Paweł Hermanowicz
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

package atomicJ.readers.csinstruments;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;

import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.ChannelFilter;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.PermissiveChannelFilter;
import atomicJ.data.Quantities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.readers.csinstruments.CSISpectroscopyData.CSISpectroscopyCurvePosition;
import atomicJ.sources.FlexibleMapSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;


public class CSISpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"nao"};
    private static final String DESCRIPTION = "CS Instruments force curve file (.nao)";

    private static final String SPECTRO_ENTRY_NAME_PREFIX = "Data/Spectro";

    static final String ZIP_ENTRY_PATH_SEPARATOR = "/";
    static final String SYSTEM_FILE_SEPARATOR = System.getProperty("file.separator");

    private final ChannelFilter filter = PermissiveChannelFilter.getInstance();
    private volatile boolean canceled = false;

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
    public List<SimpleSpectroscopySource> readSources(File file, atomicJ.readers.SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        try(ZipFile zipFile = new ZipFile(file))
        {                
            List<ZipEntry> spectroscopyEntries = findZipEntriesWhoseNamesStartWith(zipFile, SPECTRO_ENTRY_NAME_PREFIX);   

            int problemSize = spectroscopyEntries.size();
            SourceReadingState state  = problemSize > 10  ? new SourceReadingStateMonitored(problemSize, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) : new SourceReadingStateMute(problemSize);   
            try{
                List<CSISpectroscopyData> readInSpectroscopyDataAll = new ArrayList<>();
                for(ZipEntry dataEntry : spectroscopyEntries)
                {
                    if(readingDirective.isCanceled())
                    {
                        state.setOutOfJob();
                    }
                    if(state.isOutOfJob())
                    {
                        return sources;
                    }

                    InputStream dataStream = zipFile.getInputStream(dataEntry);
                    Document dataDocument = FileInputUtilities.readInXMLDocument(dataStream);

                    List<CSISpectroscopyData> readInSpectroscopyData =  CSISpectroscopyData.readInSpectroscopySources(dataDocument);
                    readInSpectroscopyDataAll.addAll(readInSpectroscopyData);


                    state.incrementAbsoluteProgress();
                }

                boolean isSuffixNecessary = readInSpectroscopyDataAll.size() > 1;

                ZipEntry imagingDataEntry = zipFile.getEntry(CSIImageReader.IMAGING_DATA_PATH); 

                Grid2D imageGrid = null;

                if(imagingDataEntry != null)
                {
                    InputStream dataStream = zipFile.getInputStream(imagingDataEntry);
                    Document dataDocument = FileInputUtilities.readInXMLDocument(dataStream);
                    imageGrid = CSIChannelSetData.readInGrid(dataDocument);
                }

                List<SimpleSpectroscopySource> sourcesFromMap = new ArrayList<>();
                List<Point2D> nodes = new ArrayList<>();

                for(int i = 0; i<readInSpectroscopyDataAll.size();i++ )
                {
                    CSISpectroscopyData spData = readInSpectroscopyDataAll.get(i);
                    String suffix = isSuffixNecessary ? " (" + Integer.toString(i) + ")" : "";
                    String shortName = IOUtilities.getBareName(file) + suffix;
                    String longName = file.getAbsolutePath() + suffix;                   

                    StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(file, shortName, longName, spData.getApproachChannelData(), spData.getWithdrawChannelData());

                    source.setSensitivity(spData.getSensitivity());
                    source.setSpringConstant(spData.getSpringConstant());
                    source.setPhotodiodeSignalType(spData.getPhotodiodeSignalType());

                    CSISpectroscopyCurvePosition curvePosition = spData.getCurvePosition();
                    Point2D recordingPoint = curvePosition.getPoint(imageGrid);
                    source.setRecordingPoint(recordingPoint);

                    sources.add(source);

                    if(recordingPoint != null)
                    {
                        nodes.add(recordingPoint);
                        sourcesFromMap.add(source);
                    }
                }

                Grid2D grid = Grid2D.getGrid(nodes, 1e-5);

                MapSource<?> mapSource = (grid != null) ?  mapSource = new MapGridSource(file, sourcesFromMap, grid)
                        : new FlexibleMapSource(file, sources, new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensityGeometryPoints(nodes), ChannelDomainIdentifier.getNewDomainKey()), Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

                ReadingPack<ImageSource> readingPack = imagingDataEntry != null ? new FileReadingPack<>(file, new CSIImageReader()) : null;         
                mapSource.setMapAreaImageReadingPack(readingPack);

                zipFile.close();
            }
            catch (Exception e) 
            {
                state.setOutOfJob();
                throw e;
            }         
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading the file", e);
        }

        return sources;
    }

    private static List<ZipEntry> findZipEntriesWhoseNamesStartWith(ZipFile zipFile, String nameBeginning)
    {
        List<ZipEntry> entriesFound = new ArrayList<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            boolean rightNameBeginning = entry.getName().startsWith(nameBeginning);

            if(rightNameBeginning)
            {
                entriesFound.add(entry);
            }
        }

        return entriesFound;
    }
}
