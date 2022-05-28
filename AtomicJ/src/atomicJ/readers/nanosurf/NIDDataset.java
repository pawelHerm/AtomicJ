/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2015 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanosurf;

import java.awt.geom.Point2D;
import java.io.File;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.Quantities;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitUtilities;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.regularImage.DummyDensityMetadata;
import atomicJ.sources.FlexibleMapSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.MultiMap;

public class NIDDataset
{
    private static final String NAME = "DataSet";

    private static final String GROUP_PREFIX = "Gr";
    private static final String NAME_SUFFIX = "-Name";
    private static final String ID_SUFFIX = "-ID";
    private static final String COUNT_SIFFIX = "-Count";
    private static final String CHANNEL_SUFFIX = "-Ch";

    private static final String INFO_SET_PREFIX = "InfoSet";

    private static final String VERSION_KEY = "Version";
    private static final String GROUP_COUNT_KEY = "GroupCount";

    private static final String INFO_SET_COUNT = "InfoSetCount";

    private static final String CALIBRATION_INFOSET = "DataSet\\Calibration\\Cantilever";
    private static final String MAP_TABLE_INFOSET = "DataSet\\SpecInfos\\SpecMapTable";
    private static final String POSITION_TABLE_INFOSET = "DataSet\\SpecInfos\\SpecPosTable";
    private static final String SPECTROSCOPY_MODE_INFOSET = "DataSet\\SpecInfos\\SpecHeader";

    private static final String SCANHEAD_INFOSET = "DataSet\\Calibration\\Scanhead";

    private final int version;

    private final List<NIDGroup> groups;
    private final List<NIDInfoset> infoSets;

    private final NIDCantileverInfoSet cantileverInfoSet;
    private final NIDScanheadInfoSet scanheadInfoSet;
    private final NIDSpectroscopyModeInfoSet spectroscopyModeInfoSet;
    private final NIDSpectroscopyPositionsInfo positionInfo;

    private NIDDataset(int version, List<NIDGroup> dataGroups, NIDCantileverInfoSet cantileverInfoSet, NIDScanheadInfoSet scanheadInfoSet, NIDSpectroscopyModeInfoSet spectroscopyModeInfoSet, NIDSpectroscopyPositionsInfo positionInfo, List<NIDInfoset> infoSets)
    {
        this.version = version;
        this.groups = dataGroups;
        this.cantileverInfoSet = cantileverInfoSet;
        this.scanheadInfoSet = scanheadInfoSet;
        this.infoSets = infoSets;
        this.spectroscopyModeInfoSet = spectroscopyModeInfoSet;
        this.positionInfo = positionInfo;
    }

    public int getVersion()
    {
        return version;
    }

    public NIDCantileverInfoSet getCantileverInfoSet()
    {
        return cantileverInfoSet;
    }

    public NIDScanheadInfoSet getScanheadInfoSet()
    {
        return scanheadInfoSet;
    }

    public List<NIDGroup> getGroups()
    {
        return Collections.unmodifiableList(groups);
    }

    public static NIDDataset build(Map<String, INISection> sectionMap)
    {
        INISection datasetSection = sectionMap.get(NAME);
        Map<String, String> keyValuePairs = datasetSection.getKeyValuePairs();

        String versionString = keyValuePairs.get(VERSION_KEY);
        String groupCountString = keyValuePairs.get(GROUP_COUNT_KEY);

        int version = Integer.parseInt(versionString);
        int groupCount = Integer.parseInt(groupCountString);

        List<NIDGroup> dataGroups = new ArrayList<>();

        for(int i = 0; i<groupCount;i++)
        {
            String groupIndexString = Integer.toString(i);

            String groupNameKey = GROUP_PREFIX + groupIndexString + NAME_SUFFIX;
            String groupIdKey = GROUP_PREFIX + groupIndexString + ID_SUFFIX;
            String groupCountKey = GROUP_PREFIX + groupIndexString + COUNT_SIFFIX;

            String groupName = keyValuePairs.get(groupNameKey);
            String groupId = keyValuePairs.get(groupIdKey);
            int channelCount = Integer.parseInt(keyValuePairs.get(groupCountKey));

            SortedMap<Integer, NIDChannel> channels = new TreeMap<>();

            for(int j = 0; j < channelCount; j++)
            {
                String channelIndexString = Integer.toString(j);
                String channelKey = GROUP_PREFIX + groupIndexString + CHANNEL_SUFFIX + channelIndexString;

                String channelInformationSection = keyValuePairs.get(channelKey);

                if(channelInformationSection != null)
                {
                    channels.put(j, NIDChannel.build(j, sectionMap.get(channelInformationSection)));
                }
            }

            NIDGroup group = new NIDGroup(groupName, groupId, channels);

            dataGroups.add(group);
        }

        String infosetCountString = keyValuePairs.get(INFO_SET_COUNT);
        int infosetCount = (infosetCountString != null) ? Integer.parseInt(infosetCountString) : 0;

        List<NIDInfoset> infosets = new ArrayList<>();

        //        for(int j = 0; j < infosetCount; j++)
        //        {
        //            String infoSetIndexString = Integer.toString(j);
        //            String channelKey = INFO_SET_PREFIX + infoSetIndexString;
        //            String infosetSectionName = keyValuePairs.get(channelKey);
        //
        //            if(infosetSectionName != null)
        //            {
        //                infosets.add(NIDInfoset.build(NAME, infosetSectionName, sectionMap));
        //            }
        //        }

        NIDCantileverInfoSet cantileverInfoSet = NIDCantileverInfoSet.build(sectionMap.get(CALIBRATION_INFOSET));        
        NIDScanheadInfoSet scanheadInfoSet = NIDScanheadInfoSet.build(sectionMap.get(SCANHEAD_INFOSET));
        NIDSpectroscopyModeInfoSet spectroscopyModeInfoSet = NIDSpectroscopyModeInfoSet.build(sectionMap.get(SPECTROSCOPY_MODE_INFOSET));
        NIDSpectroscopyPositionsInfo positionInfo = (sectionMap.containsKey(MAP_TABLE_INFOSET)) ? NIDSpectroscopyMapTable.build(sectionMap.get(MAP_TABLE_INFOSET)) : NIDSpectroscopyPositionTable.build(sectionMap.get(POSITION_TABLE_INFOSET));

        NIDDataset dataset = new NIDDataset(version, dataGroups, cantileverInfoSet, scanheadInfoSet, spectroscopyModeInfoSet, positionInfo, infosets);

        return dataset;
    }

    public static enum NIDChannelType
    {
        Z_CONTROLLER_INPUT(0, "Deflection"), Z_CONTROLLER_OUTPUT(1,"Topography"), AMPLITUDE(2, "Amplitude"), PHASE(3,"Phase"), USER_INPUT_1(4,"UserInput1"), USER_INPUT_2(5,"UserInput2"), USER_INPUT_3(6,"UserInput3"), Z_POSITION(7, "Z-Position");

        private final int index;
        private final String name;

        NIDChannelType(int index, String name)
        {
            this.name = name;
            this.index = index;
        }

        public String getName()
        {
            return name;
        }

        public int getIndex()
        {
            return index;
        }

        @Override
        public String toString()
        {
            return name;
        }   

        public static boolean instanceKnown(int index)
        {
            for(NIDChannelType type : NIDChannelType.values())
            {
                if(type.index == index)
                {
                    return true;
                }
            }

            return false;
        }

        public static NIDChannelType getInstance(int index)
        {
            for(NIDChannelType type : NIDChannelType.values())
            {
                if(type.index == index)
                {
                    return type;
                }
            }

            throw new IllegalArgumentException("The NIDChannelType is not known for he index " + index);
        }
    }

    public static class NIDInfoset
    {
        private static final String SUBSECTION_COUNT_KEY = "SubSectionCount";
        private static final String SUBSECTION_PREFIX = "SubSection";
        private static final String INFOSET_PATH_SEPARATOR = "\\";

        private final String name;
        private final String parentPath;
        private final Map<String, NIDInfoset> childrenInfosets;

        public NIDInfoset(String parentPath, String name, Map<String, NIDInfoset> childrenInfosets)
        {
            this.parentPath = parentPath;
            this.name = name;
            this.childrenInfosets = childrenInfosets;
        }

        public String getName()
        {
            return name;
        }

        public String getParentPath()
        {
            return parentPath;
        }

        public String getFullPath()
        {
            String fullPath = parentPath + INFOSET_PATH_SEPARATOR + name;
            return fullPath; 
        }

        public static NIDInfoset build(String parentPath, String name, Map<String, INISection> sectionMap)
        {
            INISection parentSection = sectionMap.get(parentPath + INFOSET_PATH_SEPARATOR + name);
            Map<String, String> keyValuePairs = parentSection.getKeyValuePairs();
            String subSectionCountString = keyValuePairs.get(SUBSECTION_COUNT_KEY);
            int subsectionCount = Integer.parseInt(subSectionCountString);

            Map<String, NIDInfoset> childrenInfosets = new HashMap<>();

            for(int i = 0; i<subsectionCount; i++)
            {
                String subsectionKey = SUBSECTION_PREFIX + Integer.toString(i);
                String subsectionName = keyValuePairs.get(subsectionKey);
                childrenInfosets.put(subsectionName, NIDInfoset.build(parentPath + INFOSET_PATH_SEPARATOR + name, subsectionName, sectionMap));
            }

            return new NIDInfoset(parentPath, name, childrenInfosets);
        }
    }

    public static class NIDSpectroscopyModeInfoSet
    {
        private static final String SPECTROSCOPY_MODE_KEY = "SpecMode";
        private static final String SPECTROSCOPY_MODE_MAP = "Map";

        private final String spectroscopyMode;

        private NIDSpectroscopyModeInfoSet(String spectroscopyMode)
        {
            this.spectroscopyMode = spectroscopyMode;
        }

        public static NIDSpectroscopyModeInfoSet build(INISection section)
        {
            if(section == null)
            {
                return new NIDSpectroscopyModeInfoSet("");
            }

            Map<String, String> keyValuePairs = section.getKeyValuePairs();
            String spectroscopyModeValue = keyValuePairs.get(SPECTROSCOPY_MODE_KEY);

            String spectroscopyMode = (spectroscopyModeValue != null) ? spectroscopyModeValue.trim() : "";
            return new NIDSpectroscopyModeInfoSet(spectroscopyMode);
        }

        public boolean isMap()
        {
            return SPECTROSCOPY_MODE_MAP.equals(spectroscopyMode);
        }
    }

    public static interface NIDSpectroscopyPositionsInfo
    {
        public Point2D getPosition(int index);
        public boolean isPossiblyMap();
        public MapSource<?> buildMapSource(File f, List<SimpleSpectroscopySource> sources);
    }

    public static class NIDSpectroscopyPositionTable implements NIDSpectroscopyPositionsInfo
    {
        private static final String POS_AXIS_KEY = "PosAxis";
        private static final String POS_UNITS_KEY = "PosUnits";
        private static final String POS_UNIT_KEY = "PosUnit";
        private static final String COUNT_KEY = "Count";
        private static final String POS_KEY_PREFIX = "Pos";

        private static final String X_AXIS_VALUE = "X";
        private static final String Y_AXIS_VALUE = "Y";

        private static final String DELIMITER = ";";

        private final List<Point2D> positions;

        private NIDSpectroscopyPositionTable(List<Point2D> positions)
        {
            this.positions = positions;
        }

        @Override
        public boolean isPossiblyMap()
        {
            boolean canBeMap = positions.size() > 1;
            return canBeMap;
        }

        public static NIDSpectroscopyPositionTable build(INISection section)
        {
            if(section == null)
            {
                return new NIDSpectroscopyPositionTable(Collections.<Point2D>emptyList());
            }

            Map<String, String> keyValuePairs = section.getKeyValuePairs();

            String positionCountString = keyValuePairs.get(COUNT_KEY);
            int positionCount = Integer.parseInt(positionCountString);

            String positionLabelsString = keyValuePairs.get(POS_AXIS_KEY);
            String[] positionLabelsTokens = positionLabelsString.split(DELIMITER);

            int xIndex = Arrays.asList(positionLabelsTokens).indexOf(X_AXIS_VALUE);
            int yIndex = Arrays.asList(positionLabelsTokens).indexOf(Y_AXIS_VALUE);

            if(xIndex < 0 || yIndex < 0)
            {
                throw new IllegalStateException("Could not found the indices of axes");
            }

            String unitsString = keyValuePairs.get(POS_UNITS_KEY);
            unitsString = (unitsString == null) ? keyValuePairs.get(POS_UNIT_KEY) : unitsString;
            String[] unitTokens = unitsString.split(DELIMITER);

            PrefixedUnit xUnit = UnitUtilities.getSIUnit(unitTokens[xIndex]);
            PrefixedUnit yUnit = UnitUtilities.getSIUnit(unitTokens[yIndex]);

            PrefixedUnit finalUnit = Units.MICRO_METER_UNIT;
            if(!finalUnit.isCompatible(xUnit) || !finalUnit.isCompatible(yUnit))
            {
                throw new IllegalStateException("Units of position cannot be converted to microns");
            }

            double factorX = xUnit.getConversionFactorTo(finalUnit);
            double factorY = yUnit.getConversionFactorTo(finalUnit);

            List<Point2D> positions = new ArrayList<>();

            for(int i = 0; i < positionCount; i++)
            {
                String key = new StringBuffer(POS_KEY_PREFIX).append(i).toString();
                String positionsString = keyValuePairs.get(key);
                String[] positionsTokens = positionsString.split(DELIMITER);

                double x = factorX*Double.parseDouble(positionsTokens[xIndex]);
                double y = factorY*Double.parseDouble(positionsTokens[yIndex]);

                positions.add(new Point2D.Double(x, y));
            }

            return new NIDSpectroscopyPositionTable(positions);
        }         

        @Override
        public Point2D getPosition(int index)
        {
            return positions.get(index);
        }

        @Override
        public MapSource<?> buildMapSource(File f, List<SimpleSpectroscopySource> sources)
        {
            Grid2D grid = Grid2D.getGrid(positions, 1e-6);

            if(grid != null)
            {
                MapSource<?> mapSource = new MapGridSource(f, sources, grid); 
                return mapSource;
            }

            double probingDensity = FlexibleChannel2DData.calculateProbingDensityGeometryPoints(positions, 1);
            MapSource<?> source = new FlexibleMapSource(f, sources, new ChannelDomainIdentifier(probingDensity, ChannelDomainIdentifier.getNewDomainKey()), Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);
            return source;
        }
    }

    /**
     * All relevant information for the map table
     *
     */
    public static class NIDSpectroscopyMapTable implements NIDSpectroscopyPositionsInfo
    {
        private static final String MAP_TABLE_KEY = "Map0";
        private static final String DELIMITER = ";";

        private final Grid2D grid;

        NIDSpectroscopyMapTable(Grid2D grid)
        {
            this.grid = grid;
        }

        /**
         * Read the relevant map data(starting point, endpoint) from Map0 Attribute in [DataSet\SpecInfos\SpecMapTable]
         * 
         * @param section The corresponding header section
         * @return A NIDSpectroscropyMapTable encapsuling the grid
         */
        public static NIDSpectroscopyMapTable build(INISection section)
        {
            if(section == null)
            {
                return new NIDSpectroscopyMapTable(null);
            }

            Map<String, String> keyValuePairs = section.getKeyValuePairs();
            String spectroscopyMapTableString = keyValuePairs.get(MAP_TABLE_KEY);

            String[] tokens = spectroscopyMapTableString.split(DELIMITER);

            double xStart = 1e6*Double.parseDouble(tokens[0]);
            double xEnd = 1e6*Double.parseDouble(tokens[1]);
            double yStart = 1e6*Double.parseDouble(tokens[2]);
            double yEnd = 1e6*Double.parseDouble(tokens[3]);

            int columnCount = Integer.parseInt(tokens[4]);
            int rowCount = Integer.parseInt(tokens[5]);

            int rotation = Integer.parseInt(tokens[6]); //currently unused in Nanosurf files
            NIDMapPlacementMode placementMode = NIDMapPlacementMode.getPlacementMode(Integer.parseInt(tokens[7]));

            Grid2D grid = placementMode.buildGrid(xStart, xEnd, yStart, yEnd, columnCount, rowCount);

            return new NIDSpectroscopyMapTable(grid);
        }


        @Override
        public boolean isPossiblyMap()
        {
            boolean canBeMap = grid.getItemCount() > 1;
            return canBeMap;
        }


        @Override
        public Point2D getPosition(int index)
        {
            return grid.getPointFlattenedBackedAndForth(index);
        }

        @Override
        public MapSource<?> buildMapSource(File f, List<SimpleSpectroscopySource> sources)
        {
            MapSource<?> mapSource = new MapGridSource(f, sources, grid); 
            return mapSource;
        }
    }

    private static enum NIDMapPlacementMode
    {
        ON_GRID(0) 
        {
            @Override
            public Grid2D buildGrid(double xStart, double xEnd, double yStart,
                    double yEnd, int columnCount, int rowCount) 
            {
                double xIncrement = Math.abs((xEnd - xStart)/(columnCount - 1));

                // Adjust value in case yEnd is a negative value
                double yIncrement = Math.abs((yEnd - yStart)/(rowCount - 1));

                Grid2D grid = new Grid2D(xIncrement, yIncrement, 0, 0, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

                return grid;
            }
        }, 
        CENTER_CELL(1) 
        {
            @Override
            public Grid2D buildGrid(double xStart, double xEnd, double yStart,
                    double yEnd, int columnCount, int rowCount) 
            {
                double xIncrement = Math.abs((xEnd - xStart)/(columnCount - 1));
                // Adjust value in case yEnd is a negative value
                double yIncrement = Math.abs((yEnd - yStart)/(rowCount - 1));

                Grid2D grid = new Grid2D(xIncrement, yIncrement, 0, 0, rowCount, columnCount, Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

                return grid;
            }
        };

        private final int code;

        NIDMapPlacementMode(int code)
        {
            this.code = code;
        }

        public abstract Grid2D buildGrid(double xStart, double xEnd, double yStart, double yEnd, int columnCount, int rowCount);

        public static NIDMapPlacementMode getPlacementMode(int code)
        {
            for(NIDMapPlacementMode mode : NIDMapPlacementMode.values())
            {
                if(mode.code == code)
                {
                    return mode;
                }
            }

            throw new IllegalArgumentException("No placement mode corresponds to " + code);
        }
    }

    public static class NIDScanheadInfoSet
    {
        private static final String INPUT_SIGNAL_COUNT_KEY = "InCount";
        private static final String IN_PREFIX = "In";
        private static final String TOKEN_DELIMITER = ",";

        private static final String SENSITIVITY_VOLTS = "TipSignalDC1";
        private static final String SENSITIVITY_METERS = "TipSignalDC";

        private final Map<String, UnitExpression> signalInputs;

        private NIDScanheadInfoSet(Map<String, UnitExpression> signalInputs)
        {
            this.signalInputs = signalInputs;
        }

        public static NIDScanheadInfoSet build(INISection section)
        {
            if(section == null)
            {
                return new NIDScanheadInfoSet(Collections.<String, UnitExpression>emptyMap());
            }

            Map<String, String> keyValuePairs = section.getKeyValuePairs();
            String inputSignalCountString = keyValuePairs.get(INPUT_SIGNAL_COUNT_KEY);
            int signalInputCount = Integer.parseInt(inputSignalCountString);

            Map<String, UnitExpression> signalInputs = new HashMap<>();

            for(int i = 0; i<signalInputCount; i++)
            {
                String propertyKey = IN_PREFIX + Integer.toString(i);
                String propertyValue = keyValuePairs.get(propertyKey);

                String[] tokens = propertyValue.split(TOKEN_DELIMITER);

                signalInputs.put(tokens[1], new UnitExpression(Double.valueOf(tokens[4]),UnitUtilities.getSIUnit(tokens[3])));
            }

            return new NIDScanheadInfoSet(signalInputs);
        }

        public UnitExpression getSensitivity()
        {
            UnitExpression readInSensitivityMeters = signalInputs.get(SENSITIVITY_METERS);
            UnitExpression readInSensitivityVolts = signalInputs.get(SENSITIVITY_VOLTS);

            if(readInSensitivityMeters == null || readInSensitivityVolts == null)
            {
                return new UnitExpression(Double.NaN, Units.MICRO_METER_PER_VOLT_UNIT);
            }

            if(readInSensitivityMeters.getValue() == 0 || readInSensitivityVolts.getValue() == 0)
            {
                return new UnitExpression(Double.NaN, Units.MICRO_METER_PER_VOLT_UNIT);
            }

            UnitExpression sensitivityToReturn = readInSensitivityMeters.divide(readInSensitivityVolts).derive(Units.MICRO_METER_PER_VOLT_UNIT);

            return sensitivityToReturn;
        }
    }


    public static class NIDCantileverInfoSet 
    {
        private static final Pattern PROPERTY_VALUE_PATTER = Pattern.compile("([A-Z])\\[([^\\]]+)\\]\\*\\[([^\\]]*)\\]");

        private static final String PROPERTY_COUNT_KEY = "PropCount";
        private static final String PROPERT_PREFIX = "Prop";

        private final Map<Integer, UnitExpression> properties;

        private NIDCantileverInfoSet(Map<Integer, UnitExpression> properties)
        {
            this.properties = properties;
        }

        public static NIDCantileverInfoSet build(INISection section)
        {
            if(section == null)
            {
                return new NIDCantileverInfoSet(Collections.<Integer, UnitExpression>emptyMap());
            }

            Map<String, String> keyValuePairs = section.getKeyValuePairs();
            String propertyCountString = keyValuePairs.get(PROPERTY_COUNT_KEY);
            int propertyCount = Integer.parseInt(propertyCountString);

            Map<Integer, UnitExpression> properties = new HashMap<>();

            for(int i = 0; i<propertyCount; i++)
            {
                String propertyKey = PROPERT_PREFIX + Integer.toString(i);
                String propertyValue = keyValuePairs.get(propertyKey);

                Matcher matcher = PROPERTY_VALUE_PATTER.matcher(propertyValue);
                boolean matches = matcher.matches();
                if(!matches)
                {
                    continue;
                }

                String valueType = matcher.group(1);
                String valueString = matcher.group(2);
                String unitString = matcher.group(3);

                NIDPropertyType type = NIDPropertyType.getType(valueType);
                if(NIDPropertyType.D.equals(type))
                {
                    properties.put(Integer.valueOf(i), new UnitExpression(Double.parseDouble(valueString), UnitUtilities.getSIUnit(unitString)));
                }
            }

            return new NIDCantileverInfoSet(properties);
        }

        public UnitExpression getSpringConstant()
        {
            UnitExpression readInpringConstant = properties.get(Integer.valueOf(0));
            UnitExpression springConstantToReturn = (readInpringConstant != null) ? readInpringConstant.derive(Units.NEWTON_PER_METER) : new UnitExpression(Double.NaN, Units.NEWTON_PER_METER);
            return springConstantToReturn;
        }
    }

    public static enum NIDPropertyType
    {
        D("D"),L("L"),S("S"),V("V"),B("B");

        private static final String ARRAY_PROPERTY_SEPARATOR = ",";
        private final String code;

        NIDPropertyType(String code)
        {
            this.code = code;
        }

        public static NIDPropertyType getType(String code)
        {
            for(NIDPropertyType type : NIDPropertyType.values())
            {
                if(type.code.equals(code))
                {
                    return type;
                }
            }

            throw new IllegalArgumentException("No known NIDPropertType corresponds to the code " + code);
        }
    }

    public List<ChannelSource> readInData(FileChannel channel, File f, SourceReadingDirectives readingDirectives, SourceReadingState state) throws UserCommunicableException
    {
        List<ChannelSource> sources = new ArrayList<>();

        Map<String, MultiMap<String, ImageChannel>> allScanData = new HashMap<>();
        Map<String, MultiMap<ForceCurveBranch, Channel1DData>> allSpectroscopyData = new HashMap<>();

        for(NIDGroup group : groups)
        {  
            if(readingDirectives.isCanceled())
            {
                state.setOutOfJob();
            }
            if(state.isOutOfJob())
            {
                return Collections.emptyList();
            }

            NIDGroupData groupData = group.readInData(channel, readingDirectives.getDataFilter(), state);
            List<ImageChannel> scanData = groupData.getImageChannels();

            MultiMap<String, ImageChannel> innerScanMap = allScanData.containsKey(group.getId()) ? allScanData.get(group.getId()) : new MultiMap<>();
            innerScanMap.putAll(group.getName(), scanData);
            allScanData.put(group.getId(), innerScanMap);

            List<Channel1DData> spectroscopyData = groupData.getSpectroscopyData();

            MultiMap<ForceCurveBranch, Channel1DData> innerSpectroscopyMap = allSpectroscopyData.containsKey(group.getId()) ? allSpectroscopyData.get(group.getId()) : new MultiMap<>();
            innerSpectroscopyMap.putAll(group.getForceCurveBranch(), spectroscopyData);
            allSpectroscopyData.put(group.getId(), innerSpectroscopyMap);
        }

        sources.addAll(buildImageSources(allScanData, f));
        sources.addAll(buildSpectroscopySources(allSpectroscopyData, f));

        return sources;
    }

    public List<ImageSource> readInImageDataAndSkipOthers(FileChannel channel, File f, SourceReadingDirectives readingDirectives, SourceReadingState state) throws UserCommunicableException
    {
        Map<String, MultiMap<String, ImageChannel>> allScanData = new HashMap<>();

        for(NIDGroup group : groups)
        {  
            if(readingDirectives.isCanceled())
            {
                state.setOutOfJob();
            }
            if(state.isOutOfJob())
            {
                return Collections.emptyList();
            }

            List<ImageChannel> scanData = group.readInScanDataAndSkipOthers(channel, readingDirectives.getDataFilter(), state).getImageChannels();

            MultiMap<String, ImageChannel> innerMap = allScanData.containsKey(group.getId()) ? allScanData.get(group.getId()) : new MultiMap<>();
            innerMap.putAll(group.getName(), scanData);
            allScanData.put(group.getId(), innerMap);
        }

        return buildImageSources(allScanData, f);
    }

    private static List<ImageSource> buildImageSources(Map<String, MultiMap<String, ImageChannel>> allScanData, File f)
    {
        List<ImageSource> imageSources = new ArrayList<>();

        String filePath = f.getAbsolutePath();
        String fileBareName = IOUtilities.getBareName(f);

        boolean multipleScanIds = allScanData.keySet().size() > 1;

        for(Entry<String, MultiMap<String, ImageChannel>> entry : allScanData.entrySet())
        {
            MultiMap<String, ImageChannel> mapForId = entry.getValue();
            if(mapForId.isEmpty())
            {
                continue;
            }

            String id = entry.getKey();

            String shortName = multipleScanIds ? fileBareName + "(" + id + ")": fileBareName;
            String longName = multipleScanIds ? filePath + "(" + id + ")": filePath;

            ImageSource sourceFile = new StandardImageSource(DummyDensityMetadata.getInstance(), f, shortName, longName);
            sourceFile.setChannels(mapForId.allValues());

            imageSources.add(sourceFile);
        }

        return imageSources;
    }

    /**
     * This function will be called, and reads the actual spectroscopy data
     * 
     * @param channel
     * @param f
     * @param state
     * @return
     * @throws UserCommunicableException
     */
    public List<SimpleSpectroscopySource> readInSpectroscopyDataAndSkipOthers(FileChannel channel, File f, SourceReadingDirectives readingDirectives, SourceReadingState state) throws UserCommunicableException
    {        
        Map<String, MultiMap<ForceCurveBranch, Channel1DData>> allSpectroscopyData = new HashMap<>();

        for(NIDGroup group : groups)
        {    
            if(readingDirectives.isCanceled())
            {
                state.setOutOfJob();
            }
            if(state.isOutOfJob())
            {
                return Collections.emptyList();
            }

            List<Channel1DData> spectroscopyData = group.readInSpectroscopyDataAndSkipOthers(channel, readingDirectives.getDataFilter(), state).getSpectroscopyData();

            MultiMap<ForceCurveBranch, Channel1DData> innerMap = allSpectroscopyData.containsKey(group.getId()) ? allSpectroscopyData.get(group.getId()) : new MultiMap<>();
            innerMap.putAll(group.getForceCurveBranch(), spectroscopyData);
            allSpectroscopyData.put(group.getId(), innerMap);
        }

        return buildSpectroscopySources(allSpectroscopyData, f);
    }

    private List<SimpleSpectroscopySource> buildSpectroscopySources(Map<String, MultiMap<ForceCurveBranch, Channel1DData>> allSpectroscopyData, File f)
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        String filePath = f.getAbsolutePath();
        String fileBareName = IOUtilities.getBareName(f);

        double springConstant = cantileverInfoSet.getSpringConstant().getValue();
        double sensitivity = scanheadInfoSet.getSensitivity().getValue();

        boolean fromMap = spectroscopyModeInfoSet.isMap() || positionInfo.isPossiblyMap();

        for(Entry<String, MultiMap<ForceCurveBranch, Channel1DData>> entry : allSpectroscopyData.entrySet())
        {
            MultiMap<ForceCurveBranch, Channel1DData> mapForId = entry.getValue();

            List<Channel1DData> approachBranches = mapForId.get(ForceCurveBranch.APPROACH);
            List<Channel1DData> withdrawBranches = mapForId.get(ForceCurveBranch.WITHDRAW);

            int sourceCount = Math.max(approachBranches.size(), withdrawBranches.size());

            for(int i = 0; i < sourceCount;i++)
            {
                String longName = filePath + "(" + i + ")";
                String shortName = fileBareName  + "(" + i + ")";

                Channel1DData approachChannel = approachBranches.size() <= i ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_MICRONS) : approachBranches.get(i);
                Channel1DData withdrawChannel = withdrawBranches.size() <= i ? FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_MICRONS) : withdrawBranches.get(i);

                StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approachChannel, withdrawChannel);

                source.setSensitivity(sensitivity);
                source.setSpringConstant(springConstant);

                if(fromMap)
                {               
                    //the recording point is set according to the 'slalom' mode. In the initial implementation, it was grid.getPointFlattenedWithFullReturn(i)
                    source.setRecordingPoint(positionInfo.getPosition(i)); 
                }         

                sources.add(source);
            }
        }

        if(fromMap)
        {
            positionInfo.buildMapSource(f, sources);            
        }

        return sources;
    }

    public int getReadableElementCount()
    {
        int elementCount = 0;

        for(NIDGroup group : groups)
        {
            elementCount += group.getReadableElementCount();
        }

        return elementCount;
    }

    public int getSpectroscopyElementCount()
    {
        int elementCount = 0;

        for(NIDGroup group : groups)
        {
            elementCount += group.getSpectroscopyElementCount();
        }

        return elementCount;
    }

    public int getImageElementCount()
    {
        int elementCount = 0;

        for(NIDGroup group : groups)
        {
            elementCount += group.getImageElementCount();
        }

        return elementCount;
    }
}