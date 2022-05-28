
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

package atomicJ.readers.jpk;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.ForceCurveOrientation;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;
import atomicJ.data.units.UnitUtilities;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.OrderedPair;



public class JPKOutSpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{  
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"out"};
    private static final String DESCRIPTION = "JPK text force curve format (.out)";

    private static final String HEADER_TAG = "#";
    private static final Pattern HEADER_KEY_PATTERN = Pattern.compile("\\s*?#([^:]++)\\s*?:(.++)");

    private static final String UNITS_KEY = "units";    
    private static final String COLUMNS_KEY = "columns";
    private static final String FANCY_NAMES_KEY = "fancyNames";
    private static final String CURVE_LENGTH_KEY = "kLength";
    private static final String DIRECTION_KEY = "direction";
    private static final String SPRING_CONSTANT_KEY = "springConstant";

    private static final String TRACE_DIRECTION = "trace";
    private static final String RETRACE_DIRECTION = "retrace";

    private static final String SMMOTHED_STRAIN_GAUGE_HEIGHT = "smoothedStrainGaugeHeight";
    private static final String STRAIN_GAUGE_HEIGHT = "strainGaugeHeight";
    private static final String HEIGHT = "height";
    private static final String VERTICAL_DEFLECTION = "vDeflection";

    public static String getDescription()
    {
        return DESCRIPTION;
    }

    public static String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public List<SimpleSpectroscopySource> readSources(File f, SourceReadingDirectives readingDirective) throws UserCommunicableException, IllegalImageException 
    {
        List<BranchData> branches = new ArrayList<>();
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        int currentLineIndex = 0;

        while(true)
        {
            BranchHeader header = new BranchHeader();
            header.readInFile(f, currentLineIndex);
            currentLineIndex += header.getLineCount();

            if(header.isEmpty())
            {
                break;
            }

            BranchData branch = new BranchData(header);
            branch.readInFile(f, currentLineIndex);
            currentLineIndex += branch.getLineCount();

            branches.add(branch);
        }

        int branchCount = branches.size();
        int curveCount = branchCount/2;


        for(int i = 0; i<curveCount; i++)
        {
            int index = i*2;

            BranchData firstBranch = branches.get(index);
            BranchData secondBranch = branchCount > index + 1 ? branches.get(index + 1) : null;

            BranchData approachBranch = ForceCurveBranch.APPROACH.equals(firstBranch.getBranchType()) ? firstBranch : secondBranch;
            BranchData withdrawBranch = ForceCurveBranch.WITHDRAW.equals(firstBranch.getBranchType()) ? firstBranch : secondBranch;

            Channel1DData approach = (approachBranch != null) ? approachBranch.getData() : FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_VOLTS);
            Channel1DData withdraw = (withdrawBranch != null) ? withdrawBranch.getData() : FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_VOLTS);

            StandardSimpleSpectroscopySource source = buildSource(f, "", approach, withdraw);

            sources.add(source);
        }

        return sources;
    }   

    private static int getDeflectionIndex(Map<String, Integer> indices)
    {
        int deflIndex = -1;

        if(indices.containsKey(VERTICAL_DEFLECTION))
        {
            deflIndex = indices.get(VERTICAL_DEFLECTION);                    
        }

        return deflIndex;
    }

    private static int getHeightIndex(Map<String, Integer> indices)
    {
        int zIndex = -1;

        if(indices.containsKey(SMMOTHED_STRAIN_GAUGE_HEIGHT))
        {
            zIndex = indices.get(SMMOTHED_STRAIN_GAUGE_HEIGHT);
        }
        else if(indices.containsKey(STRAIN_GAUGE_HEIGHT))
        {
            zIndex = indices.get(STRAIN_GAUGE_HEIGHT);
        }
        else                          
        {
            zIndex = indices.get(HEIGHT);
        }

        return zIndex;
    }


    private static StandardSimpleSpectroscopySource buildSource(File f, String suffix, Channel1DData approach, Channel1DData withdraw)
    {
        String longName = f.getAbsolutePath() + suffix;

        String shortName = IOUtilities.getBareName(f) + suffix;
        StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approach, withdraw);

        return source;
    }

    private static OrderedPair<String> dropHeaderTag(String field)
    {
        String key = field;
        String value = "";

        if(field.startsWith(HEADER_TAG))
        {
            Matcher matcher = HEADER_KEY_PATTERN.matcher(field);

            if(matcher.find())
            {                
                key = matcher.group(1);
                value = matcher.group(2);
            }
        }   

        OrderedPair<String> result = new OrderedPair<>(key, value);
        return result;
    }

    @Override
    public boolean accept(File f) 
    {
        String[] acceptedExtensions = getAcceptedExtensions();

        FileExtensionPatternFilter filter = new FileExtensionPatternFilter(acceptedExtensions);
        return filter.accept(f);    
    }

    private static class BranchHeader
    {
        private final List<DataChunk> dataChunks = new ArrayList<>();
        private final Map<String, Integer> indices = new LinkedHashMap<>();
        private int pointCount = -1;
        private ForceCurveBranch branch;
        private int lineCount = 0;

        public List<DataChunk> getDataChunks()
        {
            return Collections.unmodifiableList(dataChunks);
        }

        public Map<String, Integer> getColumnIndices()
        {
            return Collections.unmodifiableMap(indices);
        }

        public boolean isEmpty()
        {
            return dataChunks.isEmpty();
        }

        public int getBranchPointCount()
        {
            return pointCount;
        }

        public int getLineCount()
        {
            return lineCount;
        }

        public ForceCurveBranch getBranchType()
        {
            return branch;
        }

        public void readInFile(File f, int linesToSKip) throws UserCommunicableException
        {
            try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))

            {
                try(Scanner scanner = new Scanner(channel, "ISO-8859-1");) 
                {
                    for(int i = 0; i<linesToSKip; i++)
                    {
                        scanner.nextLine();
                    }

                    scanner.useLocale(Locale.US);

                    if(!scanner.hasNext())
                    {
                        return;
                    }

                    String line = scanner.nextLine();

                    while(line.startsWith(HEADER_TAG) || line.isEmpty())
                    {   
                        lineCount++;

                        OrderedPair<String> keyValuePair = dropHeaderTag(line);
                        String key = keyValuePair.getFirst().trim();

                        if(COLUMNS_KEY.equals(key))
                        {                        
                            String columnNamesConcatenated = keyValuePair.getSecond().trim();
                            String[] columnNames = columnNamesConcatenated.split("\\s++");

                            for(int i = 0; i<columnNames.length; i++)
                            {
                                String columnName = columnNames[i];
                                indices.put(columnName, i);
                                DataChunk chunk = new DataChunk(columnName, i);                              
                                dataChunks.add(chunk);
                            }
                        }
                        else if(UNITS_KEY.equals(key))
                        {
                            String unitNamesConcatenated = keyValuePair.getSecond().trim();
                            String[] unitNames = unitNamesConcatenated.split("\\s++");
                            for(int i = 0; i<dataChunks.size(); i++)
                            {
                                PrefixedUnit unit = UnitUtilities.getSIUnit(unitNames[i]);
                                DataChunk chunk = dataChunks.get(i);
                                chunk.setUnit(unit);                              
                            }
                        }
                        else if(FANCY_NAMES_KEY.equals(key))
                        {
                            String fancyNamesConcatenated = keyValuePair.getSecond().trim();
                            String[] fancyNames = fancyNamesConcatenated.split("\\s++");
                            for(int i = 0; i<dataChunks.size(); i++)
                            {

                                DataChunk chunk = dataChunks.get(i);
                                chunk.setColumnFancyName(fancyNames[i]);                              
                            }
                        }
                        else if(DIRECTION_KEY.equals(key))
                        {
                            String direction = keyValuePair.getSecond().trim();
                            branch = findCurveBranch(direction);
                        }
                        else if(CURVE_LENGTH_KEY.equals(key))
                        {
                            pointCount = Integer.parseInt(keyValuePair.getSecond().trim());
                        }

                        if(scanner.hasNext())
                        {
                            line = scanner.nextLine();
                        }
                        else
                        {
                            break;
                        }
                    }
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

        private ForceCurveBranch findCurveBranch(String branchString)
        {
            ForceCurveBranch curveBranch = null;
            if(TRACE_DIRECTION.equals(branchString))
            {
                curveBranch = ForceCurveBranch.APPROACH;
            }
            else if(RETRACE_DIRECTION.equals(branchString))
            {
                curveBranch = ForceCurveBranch.WITHDRAW;
            }

            return curveBranch;
        }
    }

    private static class BranchData
    {
        private boolean forceCalibrated;
        private boolean sensitivityCalibrated;

        private final BranchHeader header;

        private Channel1DData data; 

        public BranchData(BranchHeader header)
        {
            this.header = header;

            this.data = new FlexibleChannel1DData(new double[][] {}, Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_VOLTS,header.getBranchType().getDefaultXOrderForLeftHandSideContactOrientation());
        }

        public boolean isForceCalibrated()
        {
            return forceCalibrated;
        }

        public boolean isSensitivityCalibrated()
        {
            return sensitivityCalibrated;
        }

        public Channel1DData getData()
        {
            return data;
        }

        public int getLineCount()
        {
            return header.getBranchPointCount();
        }

        public ForceCurveBranch getBranchType()
        {
            return header.getBranchType();
        }

        public void readInFile(File f, int linesToSKip) throws UserCommunicableException
        {
            try (FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath()))
            {     
                List<DataChunk> dataChunks = header.getDataChunks();
                Map<String, Integer> indices = header.getColumnIndices();
                int pointCount = header.getBranchPointCount();           
                ForceCurveBranch branch = header.getBranchType();

                try(Scanner scannerData = new Scanner(channel, "ISO-8859-1");) 
                {     
                    scannerData.useLocale(Locale.US);

                    //skips header
                    for(int i = 0; i<linesToSKip; i++)
                    {
                        scannerData.nextLine();
                    }

                    int heightIndex = getHeightIndex(indices);

                    if(heightIndex < 0)
                    {
                        throw new UserCommunicableException("Error occured while reading in a force curve\n Height data not found");
                    }

                    int deflIndex = getDeflectionIndex(indices);
                    if(deflIndex < 0)
                    {
                        throw new UserCommunicableException("Error occured while reading in a force curve\n Deflection data not found");
                    }

                    //reads in data
                    int chunkCount = dataChunks.size();
                    double[][] allData = new double[pointCount][chunkCount];

                    for(int i = 0; i<pointCount; i++)
                    {
                        for(int j = 0; j<chunkCount; j++)
                        {
                            allData[i][j] = scannerData.nextDouble();

                        }                      
                    }

                    DataChunk heightDataChunk = dataChunks.get(heightIndex);
                    PrefixedUnit heightUnit = heightDataChunk.getUnit();

                    Quantity heighQuantity = Quantities.DISTANCE_MICRONS;
                    double heightFactor = heightUnit.getConversionFactorTo(heighQuantity.getUnit());

                    DataChunk deflectionDataChunk = dataChunks.get(deflIndex);
                    PrefixedUnit deflUnit = deflectionDataChunk.getUnit();

                    Quantity deflQuantity = CalibrationState.getDefaultYQuantity(deflUnit);
                    double defFactor = deflUnit.getConversionFactorTo(deflQuantity.getUnit());

                    forceCalibrated = StandardQuantityTypes.FORCE.isCompatible(deflUnit);
                    sensitivityCalibrated = StandardQuantityTypes.LENGTH.isCompatible(deflUnit);

                    double[][] points = new double[pointCount][];
                    for(int i = 0; i<pointCount; i++)
                    {
                        double[] dataRow = allData[i];
                        points[i] = new double[] {heightFactor*dataRow[heightIndex], defFactor*dataRow[deflIndex]};
                    }

                    double[][] pointsCorrected = ForceCurveOrientation.LEFT.correctOrientation(points, branch);
                    data = new FlexibleChannel1DData(pointsCorrected, heighQuantity, deflQuantity, header.getBranchType().getDefaultXOrderForLeftHandSideContactOrientation());
                }
            }
            catch (IOException | RuntimeException e) 
            {
                e.printStackTrace();

                throw new UserCommunicableException("Error occured while reading the file", e);     
            } 
        }
    }

    private static class DataChunk
    {
        private String columnName;
        private String columnFancyName;
        private PrefixedUnit unit;
        private final int columnIndex;

        public DataChunk(String columnName, int columnIndex)
        {
            this.columnName = columnName;
            this.columnIndex = columnIndex;
        }

        public String getColumnName()
        {
            return columnName;
        }

        public void setColumnName(String columnName)
        {
            this.columnName = columnName;
        }

        public String getColumnFancyName()
        {
            return columnFancyName;
        }

        public void setColumnFancyName(String columnFancyName)
        {
            this.columnFancyName = columnFancyName; 
        }

        public int getColumnIndex()
        {
            return columnIndex;
        }

        public PrefixedUnit getUnit()
        {
            return unit;
        }

        public void setUnit(PrefixedUnit unit)
        {
            this.unit = unit;
        }

        public boolean isWellBuild()
        {
            boolean wellBuild = (unit != null) && (columnName != null && !columnName.isEmpty());
            return wellBuild;
        }
    }
}