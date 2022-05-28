
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

package atomicJ.readers.mi;

import java.awt.geom.Point2D;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Quantities;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid2D;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.DynamicSpectroscopySource;
import atomicJ.sources.FlexibleMapSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;


public class MISpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("bufferLabel\\s*(Z|Distance)");
    private static final Pattern DEFLECTION_PATTERN = Pattern.compile("bufferLabel\\s*(Deflection|Force|Raw_Defl)");
    private static final Pattern TOPOGRAPHY_PATTERN = Pattern.compile("bufferLabel\\s*(Topography)");
    private static final Pattern AMPLITUDE_PATTERN = Pattern.compile("bufferLabel\\s*(Amplitude)");
    private static final Pattern PHASE_PATTERN = Pattern.compile("bufferLabel\\s*(Phase)");

    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"mi"};
    private static final String DESCRIPTION = "Agilent force curve file (.mi)";

    private static final String MODE = "mode";
    private static final String DATA = "data";
    private static final String BINARY = "BINARY";
    private static final String HARMONIC = "Harmonic";
    private static final String SPECTROSCOPY = "Spectroscopy";
    private static final String FILE_TYPE = "fileType";
    private static final String IMAGE_FILE = "BgImageFile";
    private static final String GRID = "grid";

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
        boolean isBinary = false;

        int textLength = 0;

        try(BufferedReader bsr = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ISO-8859-1"));) 
        {          
            String firstLine = bsr.readLine();
            textLength = textLength + firstLine.length() + 1;

            if(firstLine.startsWith(FILE_TYPE))
            {               
                String[] words = firstLine.split("\\s+");               
                boolean isSpectroscopy = SPECTROSCOPY.equals(words[1]);

                if(!isSpectroscopy)
                {
                    throw new IllegalImageException();
                }               
            }

            String line;

            boolean amplitudeSensitivityCalibrated = false;

            Quantity deflectionQuantity = UnitQuantity.NULL_QUANTITY;

            double factorXY = 1;            
            double factorZDeflection = 1;
            double factorZAmplitude = 1;
            double factorZPhase = 1;

            double originX = 0;
            double originY = 0;

            int rowCount = 1;
            int columnCount = 1;

            double xRange = 0;
            double yRange = 0;
            double gridIncrement =  1;
            boolean isGrid = false;
            boolean isFreeMultiple = false;
            boolean isXForward = false;
            boolean isYForward = false;

            boolean isHarmonic = false;

            MIChunkGroup deflectionChunksGroup = MIChunkGroup.getNullInstance();
            MIChunkGroup topographyChunksGroup = MIChunkGroup.getNullInstance();

            List<File> imageFiles = new ArrayList<>();

            Map<Integer,double[]> points = new HashMap<>();

            while((line = bsr.readLine()) != null)
            {   
                textLength = textLength + line.length() + 1;

                if(DISTANCE_PATTERN.matcher(line).matches())
                {
                    line = bsr.readLine();
                    textLength = textLength + line.length() + 1;

                    String[] words = line.split("\\s+");
                    String unitString = words[words.length - 1];
                    PrefixedUnit distanceUnit = UnitUtilities.getSIUnit(unitString);

                    factorXY = distanceUnit.getConversionFactorTo(Units.MICRO_METER_UNIT);
                }

                //if the line contains information about grid
                //we read it and get the next line
                else if(line.startsWith(GRID))
                {
                    isGrid = true;

                    String[] words = line.split("\\s+");

                    originX = 0;
                    originY = 0;
                    rowCount = Integer.parseInt(words[4]);
                    columnCount = Integer.parseInt(words[5]);
                    gridIncrement = Double.parseDouble(words[6]);
                    isXForward = (Integer.parseInt(words[7]) == 0);
                    isYForward = (Integer.parseInt(words[8]) == 0);

                }
                else if (DEFLECTION_PATTERN.matcher(line).matches())
                {    
                    try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
                    {
                        channel.position(textLength);
                        deflectionChunksGroup = MIChunkGroup.readIn(channel, factorXY);
                    }                   

                    PrefixedUnit deflUnit = deflectionChunksGroup.getUnit();

                    deflectionQuantity = CalibrationState.getDefaultYQuantity(deflUnit);
                    factorZDeflection = factorZDeflection*deflUnit.getConversionFactorTo(deflectionQuantity.getUnit());
                }
                else if(TOPOGRAPHY_PATTERN.matcher(line).matches())
                {
                    try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
                    {
                        channel.position(textLength);
                        topographyChunksGroup = MIChunkGroup.readIn(channel, factorXY);
                    }  
                }
                else if(AMPLITUDE_PATTERN.matcher(line).matches())
                {       

                    line = bsr.readLine();
                    textLength = textLength + line.length() + 1;

                    String[] words = line.split("\\s+");
                    String unitString = words[words.length - 1];
                    PrefixedUnit amplitudeUnit = UnitUtilities.getSIUnit(unitString);

                    Quantity amplitudeQuantity = CalibrationState.getDefaultYQuantity(amplitudeUnit);
                    factorZAmplitude = factorZAmplitude*amplitudeUnit.getConversionFactorTo(amplitudeQuantity.getUnit());
                } 

                else if (PHASE_PATTERN.matcher(line).matches())
                {       
                    line = bsr.readLine();
                    textLength = textLength + line.length() + 1;
                } 

                else if(line.startsWith(MODE))
                {
                    String[] words = line.split("\\s+");              
                    isHarmonic = HARMONIC.equals(words[1]);  
                }
                else if(line.startsWith("point"))
                {
                    isFreeMultiple = true;

                    String[] words = line.split("\\s+");

                    Integer key = Integer.parseInt(words[1]);
                    double xPosition = factorXY*Double.parseDouble(words[2]);
                    double yPosition = factorXY*Double.parseDouble(words[3]);
                    double[] p = new double[] {xPosition, yPosition};
                    points.put(key, p);
                }
                else if(line.startsWith(IMAGE_FILE))
                {
                    String[] words = line.split("\\s+");

                    if(words.length >1)
                    {
                        String imageFileName = words[1];
                        String imageFilePath = f.getParent();

                        File imageFile = new File(imageFilePath, imageFileName);    

                        if(imageFile.exists())
                        {
                            imageFiles.add(imageFile);
                        }
                    }
                }
                else if(line.startsWith(DATA))
                {
                    isBinary = line.contains(BINARY);
                    break;
                }
            }

            ReadingPack<ImageSource> readingPack = null;
            if(!imageFiles.isEmpty())
            {
                MIImageReader imageReader = new MIImageReader();
                readingPack = new FileReadingPack<>(imageFiles, imageReader);               
            }

            xRange = gridIncrement*(rowCount - 1) - originX;
            yRange = gridIncrement*(columnCount - 1) - originY;

            try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
            {
                channel.position(textLength);

                boolean readInSuccessfully = isBinary ? readInBinaryData(channel, deflectionChunksGroup.getAllChunks(), factorZDeflection, readingDirective) : readInTextData(channel, deflectionChunksGroup.getAllChunks(), isHarmonic, factorXY, factorZDeflection, factorZAmplitude, readingDirective);
                if(!readInSuccessfully)
                {
                    return Collections.emptyList();
                }
            }

            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            Set<Integer> sortedAllKeys = deflectionChunksGroup.getKeys();
            Map<Integer, MIChunk> approachChunks = deflectionChunksGroup.getApproachChunks();
            Map<Integer, MIChunk> withdrawChunks = deflectionChunksGroup.getWithdrawChunks();

            boolean addSuffix = sortedAllKeys.size() > 1;

            if(isGrid || isFreeMultiple)
            {
                if(isFreeMultiple)
                {
                    double probingDensity = FlexibleChannel2DData.calculateProbingDensity(new ArrayList<>(points.values()));
                    ChannelDomainIdentifier dataDomain = new ChannelDomainIdentifier(probingDensity, ChannelDomainIdentifier.getNewDomainKey());

                    List<SimpleSpectroscopySource> simpleSources  = new ArrayList<>();  

                    for(Integer key: sortedAllKeys)
                    {
                        double x = isXForward? points.get(key)[0] : points.get(key)[0];
                        double y = isYForward ? points.get(key)[1] : points.get(key)[1];       

                        Point2D p = new Point2D.Double(x, y);                

                        String suffix = addSuffix ? " (" + key + ")": "";
                        MIChunk approachChunk = approachChunks.get(key);
                        MIChunk withdrawChunk = withdrawChunks.get(key);

                        StandardSimpleSpectroscopySource source = buildSource(f, suffix, deflectionQuantity, approachChunk, withdrawChunk, isHarmonic);                     
                        source.setRecordingPoint(p);

                        sources.add(source);

                        simpleSources.add(source);
                    }       
                    MapSource<?> mapSource = new FlexibleMapSource(f, simpleSources, dataDomain, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);
                    mapSource.setMapAreaImageReadingPack(readingPack);
                }

                if(isGrid)
                {
                    Grid2D grid = new Grid2D(factorXY*gridIncrement, factorXY*gridIncrement, factorXY*originX, factorXY*originY, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

                    List<SimpleSpectroscopySource> mapSources = new ArrayList<>();

                    for(Integer key: sortedAllKeys)
                    {
                        int row = key/rowCount;
                        int column = key % rowCount;

                        double x = isXForward? factorXY*(originX + column*gridIncrement) : factorXY*(xRange - column*gridIncrement);
                        double y = isYForward ? factorXY*(originY + row*gridIncrement) : factorXY*(yRange  - row*gridIncrement);               
                        Point2D p = new Point2D.Double(x, y);                

                        String suffix = addSuffix ? " (" + key + ")": "";
                        MIChunk approachChunk = approachChunks.get(key);
                        MIChunk withdrawChunk = withdrawChunks.get(key);

                        StandardSimpleSpectroscopySource source = buildSource(f, suffix, deflectionQuantity, approachChunk, withdrawChunk, isHarmonic);

                        source.setRecordingPoint(p);

                        sources.add(source);
                        mapSources.add(source);
                    }           

                    MapSource<?> mapSource = new MapGridSource(f, mapSources, grid);   
                    mapSource.setMapAreaImageReadingPack(readingPack);
                }
            }
            else
            {
                for(Integer key: sortedAllKeys)
                {
                    String suffix = addSuffix ? " (" + key + ")": "";
                    MIChunk approachChunk = approachChunks.get(key);
                    MIChunk withdrawChunk = withdrawChunks.get(key);

                    StandardSimpleSpectroscopySource source = buildSource(f, suffix, deflectionQuantity, approachChunk, withdrawChunk, isHarmonic);

                    sources.add(source);
                }
            }

            return sources; 
        } 

        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 
    }

    private boolean readInTextData(FileChannel channel, List<MIChunk> chunks, boolean isHarmonic, double factorXY, double factorZDeflection, double factorZAmplitude, SourceReadingDirectives readingDirective) throws UserCommunicableException
    {
        int n = chunks.size();
        SourceReadingState state  = n > 10  ? new SourceReadingStateMonitored(n, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) : new SourceReadingStateMute(n);   

        try(Scanner scanner = new Scanner(channel);) 
        {
            scanner.useLocale(Locale.US);
            scanner.nextLine();

            int lengthAll = 0;

            for(MIChunk chunk: chunks)
            {
                int count = chunk.getCount();
                lengthAll = lengthAll + count;

                if(isHarmonic)
                {
                    double[] deflectionData = chunk.initializeAndGetDeflectionData();               
                    double[] amplitudeData = chunk.initializeAndGetAmplitudeData();               
                    double[] phaseData = chunk.initializeAndGetPhaseData();               

                    for(int i = 0; i<count; i++)
                    {                              
                        scanner.nextDouble();//czas
                        double x = factorXY*scanner.nextDouble();
                        double amplitude = factorZAmplitude*scanner.nextDouble();
                        double z = factorZDeflection*scanner.nextDouble();
                        double phase = scanner.nextDouble();

                        deflectionData[i] = z;
                        amplitudeData[i] = amplitude;
                        phaseData[i] = phase;
                    }
                }
                else
                {
                    double[] deflectionData = chunk.initializeAndGetDeflectionData();               

                    for(int i = 0; i<count; i++)
                    {                              
                        scanner.nextDouble();//time
                        scanner.nextDouble();//z position
                        double z = factorZDeflection*scanner.nextDouble();

                        deflectionData[i] = z;
                    }
                }

                if(readingDirective.isCanceled())
                {
                    state.setOutOfJob();
                }
                if(state.isOutOfJob())
                {
                    return false;
                }

                state.incrementAbsoluteProgress();
            }         
            return true;

        } 
        catch(Exception e)
        {
            state.setOutOfJob();
            throw new UserCommunicableException("Error has occured while reading a MI recording", e);

        }

    }

    private boolean readInBinaryData(FileChannel channel, List<MIChunk> chunks, double factorZDeflection, SourceReadingDirectives readingDirective) throws IOException, UserCommunicableException
    {    
        int n = chunks.size();
        SourceReadingState state  = n > 10  ? new SourceReadingStateMonitored(n, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) : new SourceReadingStateMute(n);   

        try
        {
            int bufferSize = 0;
            for(MIChunk chunk: chunks)
            {
                bufferSize = bufferSize + 4*chunk.getCount();
            }

            ByteBuffer byteBuffer = FileInputUtilities.readBytesToBuffer(channel, bufferSize, ByteOrder.LITTLE_ENDIAN);       

            for(MIChunk chunk: chunks)
            {
                int count = chunk.getCount();

                double[] data = chunk.initializeAndGetDeflectionData();

                for(int i = 0;i<count; i++)
                {
                    double z = factorZDeflection*byteBuffer.getFloat();
                    data[i] = z;
                }   

                if(readingDirective.isCanceled())
                {
                    state.setOutOfJob();
                }
                if(state.isOutOfJob())
                {
                    return false;
                }

                state.incrementAbsoluteProgress();              
            }         

            return true;
        }
        catch (Exception e) 
        {
            state.setOutOfJob();
            throw new UserCommunicableException("Error has occured while reading a MI recording", e);
        }          
    }

    private static StandardSimpleSpectroscopySource buildSource(File f, String suffix, Quantity deflectionQuantity,MIChunk approachChunk, MIChunk withdrawChunk, boolean harmonic)
    {
        String longName = f.getAbsolutePath() + suffix;
        String shortName = IOUtilities.getBareName(f) + suffix;

        if(harmonic)
        {
            boolean amplitudeSensitivityCalibrated = false;
            Quantity amplitudeQuantity = amplitudeSensitivityCalibrated ? Quantities.AMPLITUDE_MICRONS : Quantities.AMPLITUDE_VOLTS;

            boolean isApprachEmpty = (approachChunk == null);
            boolean isWithdrawEmpty = (withdrawChunk == null);

            Channel1DData approachDeflectionData = isApprachEmpty ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, deflectionQuantity) :approachChunk.getDeflectionData(deflectionQuantity);
            Channel1DData withdrawDeflectionData = isWithdrawEmpty ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, deflectionQuantity) : withdrawChunk.getDeflectionData(deflectionQuantity);

            Channel1DData approachAmplitudeData = isApprachEmpty ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, amplitudeQuantity) :approachChunk.getAmplitudeData(amplitudeQuantity);
            Channel1DData withdrawAmplitudeData = isWithdrawEmpty ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, amplitudeQuantity) : withdrawChunk.getAmplitudeData(amplitudeQuantity);

            Channel1DData approachPhaseData = isApprachEmpty ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.PHASE_DEGREES) :approachChunk.getPhaseData();
            Channel1DData withdrawPhaseData = isWithdrawEmpty ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.PHASE_DEGREES) : withdrawChunk.getPhaseData();

            DynamicSpectroscopySource source = new DynamicSpectroscopySource(f, shortName, longName, approachDeflectionData, withdrawDeflectionData);

            source.setAmplitudeData(approachAmplitudeData, withdrawAmplitudeData);
            source.setPhaseData(approachPhaseData, withdrawPhaseData);

            return source;
        }
        else
        {
            Channel1DData approachChannel = (approachChunk == null) ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, deflectionQuantity) :approachChunk.getDeflectionData(deflectionQuantity);
            Channel1DData withdrawChannel = (withdrawChunk == null) ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, deflectionQuantity) : withdrawChunk.getDeflectionData(deflectionQuantity);

            StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approachChannel, withdrawChannel);

            return source;
        }
    }
}

