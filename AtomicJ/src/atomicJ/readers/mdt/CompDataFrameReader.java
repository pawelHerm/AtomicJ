
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

package atomicJ.readers.mdt;

import java.awt.geom.Point2D;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.ChannelFilter;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.Grid1D;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.QuantityArray1DExpression;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.UnitUtilities;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.FlexibleMapSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.IOUtilities;

public class CompDataFrameReader implements MDTFrameReader
{
    public static final int FRAME_TYPE = 190;

    //QUESTIONS TO THE NT-MDT TEAM

    /*
     * TNTDAMeasInfo structure contains an array rAxisOptions of the type TNTDAAxisOptions. It should be filled
     * with the values of the attributes inverse0, inverse1,..., of the tag Meas. In the examples I received 
     */

    private static final int DAA_AXIS_COUNT = 4;
    private static final String MAIN_INDEX_FILE = "index.xml";
    private static final String XML_PARAMETERS_FILE = "__xmlparams.xml";

    private final ChannelFilter filter;

    public CompDataFrameReader(ChannelFilter filter)
    {
        this.filter = filter;
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {                   
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        int fileCount = FileInputUtilities.readBytesToBuffer(channel, 4, ByteOrder.LITTLE_ENDIAN).getInt();

        List<MDTInnerFileData> dataBlocks = new ArrayList<>();

        ByteBuffer blockLengthBuffer = FileInputUtilities.readBytesToBuffer(channel, 2*4*fileCount, ByteOrder.LITTLE_ENDIAN);
        int blockNamesByteSize = 0;

        for(int i = 0; i<fileCount; i++)
        {
            int nameLength = blockLengthBuffer.getInt();
            int length = blockLengthBuffer.getInt();
            blockNamesByteSize += nameLength;

            MDTInnerFileData dataBlock = new MDTInnerFileData(length, nameLength);
            dataBlocks.add(dataBlock);
        }

        ByteBuffer blockNamesBuffer = FileInputUtilities.readBytesToBuffer(channel,blockNamesByteSize, ByteOrder.LITTLE_ENDIAN);

        for(int i = 0; i<fileCount; i++)
        {
            MDTInnerFileData dataBlock = dataBlocks.get(i);
            char[] nameChars = new char[dataBlock.getNameLength()];
            FileInputUtilities.populateCharArrayWithBytes(nameChars, blockNamesBuffer);

            String name =  FileInputUtilities.convertBytesToString(nameChars);
            dataBlock.setName(name);
        }

        for(int i = 0; i<fileCount; i++)
        {
            MDTInnerFileData dataBlock = dataBlocks.get(i);
            dataBlock.setByteBuffer(FileInputUtilities.readBytesToBuffer(channel, dataBlock.getLength(), ByteOrder.LITTLE_ENDIAN));              
        }

        ////////PRINTS MAIN INDEX FILE //////

        Map<String, MDTInnerFileData> innerFileDataMap = MDTInnerFileData.convertToMap(dataBlocks);

        MDTInnerFileData mainIndexBlock = innerFileDataMap.get(MAIN_INDEX_FILE);
        if(mainIndexBlock == null)
        {
            throw new UserCommunicableException("File does not contain the index.xml data block");
        }

        Document mainIndexDocument;
        try {
            mainIndexDocument = mainIndexBlock.readInAsDocument();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the frame " + frameIndex, e);
        }     

        NodeList pointNodes = mainIndexDocument.getElementsByTagName(PointInfo.CORRESPONDING_TAG_NAME);

        List<PointInfo> pointInfos = new ArrayList<>();
        for(int i = 0; i<pointNodes.getLength(); i++)
        { 
            Node node = pointNodes.item(i);
            if(node instanceof Element)
            {
                pointInfos.add(new PointInfo((Element)node, innerFileDataMap));
            }
        }


        NodeList measurementNodes = mainIndexDocument.getElementsByTagName(MeasurementInfo.CORRESPONDING_TAG_NAME);

        List<MeasurementInfo> measurementInfos = new ArrayList<>();
        for(int i = 0; i<measurementNodes.getLength(); i++)
        { 
            Node node = measurementNodes.item(i);
            if(node instanceof Element)
            {
                MeasurementInfo measurementInfo = new MeasurementInfo((Element)node);
                measurementInfos.add(measurementInfo);
            }
        }

        measurementInfos = sortAccordingToIndexes(measurementInfos);


        NodeList axisNodes = mainIndexDocument.getElementsByTagName(AxisInfo.CORRESPONDING_TAG_NAME);

        List<AxisInfo> axisInfos = new ArrayList<>();
        for(int i = 0; i<axisNodes.getLength(); i++)
        { 
            Node node = axisNodes.item(i);
            if(node instanceof Element)
            {
                axisInfos.add(new AxisInfo((Element)node));
            }
        }

        axisInfos = sortAccordingToIndexes(axisInfos);


        NodeList nameNodes = mainIndexDocument.getElementsByTagName(NameInfo.CORRESPONDING_TAG_NAME);

        List<NameInfo> nameInfos = new ArrayList<>();
        for(int i = 0; i<nameNodes.getLength(); i++)
        { 
            Node node = nameNodes.item(i);
            if(node instanceof Element)
            {
                nameInfos.add(new NameInfo((Element)node));
            }
        }

        nameInfos = sortAccordingToIndexes(nameInfos);


        NodeList dataNodes = mainIndexDocument.getElementsByTagName(DataInfo.CORRESPONDING_TAG_NAME);

        List<DataInfo> dataInfos = new ArrayList<>();
        for(int i = 0; i<dataNodes.getLength(); i++)
        { 
            Node node = dataNodes.item(i);
            if(node instanceof Element)
            {
                dataInfos.add(new DataInfo((Element)node));
            }
        }

        dataInfos = sortAccordingToIndexes(dataInfos);

        List<Point2D> knownRecordingPoints = new ArrayList<>();
        List<SimpleSpectroscopySource> sourcesWithKnownPosition = new ArrayList<>();
        for(PointInfo pointInfo : pointInfos)
        {
            List<SimpleSpectroscopySource> sourceForPoint = pointInfo.buildSources(f, frameIndex, true, measurementInfos, axisInfos, dataInfos, nameInfos, innerFileDataMap);
            sources.addAll(sourceForPoint);
            Point2D recordingPoint = pointInfo.getRecordingPoint();
            if(recordingPoint != null && !sourceForPoint.isEmpty())
            {
                knownRecordingPoints.add(recordingPoint);
                //we can take just one spectroscopy source for point, due to constraints of the MapSource design
                sourcesWithKnownPosition.add(sourceForPoint.iterator().next());
            }
        }

        if(!sourcesWithKnownPosition.isEmpty())
        {
            Grid2D grid = Grid2D.getGrid(knownRecordingPoints, 1e-6);

            MapSource<?> mapSource = (grid != null) ? new MapGridSource(f, sourcesWithKnownPosition, grid): new FlexibleMapSource(f, sourcesWithKnownPosition, new ChannelDomainIdentifier(FlexibleChannel2DData.calculateProbingDensity(GeometryUtilities.extractCoordinates(knownRecordingPoints)), ChannelDomainIdentifier.getNewDomainKey()), Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);

        }

        return sources;        
    }


    private static class MDTInnerFileData
    {
        private String name;
        private ByteBuffer dataBuffer;
        private final int length;
        private final int nameLength;

        public MDTInnerFileData(int length, int nameLength)
        {
            this.length= length;
            this.nameLength = nameLength;
        }

        public int getNameLength()
        {
            return nameLength;
        }

        public int getLength()
        {
            return length;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public ByteBuffer getByteBuffer()
        {
            return dataBuffer;
        }

        public void setByteBuffer(ByteBuffer buffer)
        {
            this.dataBuffer = buffer;
        }

        private double[] readInAsInts(int itemCount, int offsetInBytes, double scale, double bias)
        {
            dataBuffer.position(offsetInBytes);
            double[] values = new double[itemCount];

            for(int i = 0; i<itemCount; i++)
            {
                values[i] = scale*dataBuffer.getInt() + bias;
            }

            return values;
        }

        private double[] readInAsDoubles(int itemCount, int offsetInBytes, double scale, double bias)
        {
            dataBuffer.position(offsetInBytes);
            double[] values = new double[itemCount];

            for(int i = 0; i<itemCount; i++)
            {
                values[i] = scale*dataBuffer.getDouble() + bias;
            }

            return values;
        }

        private Document readInAsDocument() throws ParserConfigurationException, SAXException, IOException
        {
            return FileInputUtilities.readInXMLDocument(length, dataBuffer);
        }

        private static Map<String, MDTInnerFileData> convertToMap(Collection<MDTInnerFileData> files)
        {
            Map<String, MDTInnerFileData> map = new LinkedHashMap<>();

            for(MDTInnerFileData file : files)
            {
                map.put(file.getName(), file);
            }

            return map;
        }
    }

    private static interface InternalFileInfo
    {
        public int getIndex();
    }

    private static class PointInfo implements InternalFileInfo
    {
        private static final String CORRESPONDING_TAG_NAME = "Point";

        // HTML ATTRIBUTES ///

        private static final String POINT_INDEX_ATTRIUTE = "index";
        private static final String FILE_NAME_ATTRIBUTE = "name"; //file name, rPointFileName, we will use it to find the corresponding data block
        private static final String FILE_OFFSET_ATTRIBUTE = "offset"; //fileOffset, rPointFileOffset
        private static final String MEASUREMENT_COUNT_ATTRIBUTE = "meas"; //measurementCount, rMeasCount
        private static final String EXECUTION_COUNT_ATTRIBUTE = "exec";//executionCount,rExecCount,
        private static final String X_COORDINATE_ATRIBUTE = "x";
        private static final String Y_COORDINATE_ATRIBUTE = "y";
        private static final String Z_COORDINATE_ATRIBUTE = "z";
        private static final String UNIT_ATTRIBUTE = "unit";

        private final int index;

        private final double[] coordinates = new double[DAA_AXIS_COUNT];
        private final PrefixedUnit unit;
        private final int measurementCount;
        private final int executionCount;
        private final int[] forwardMeasurementIndices;
        private final int[] backwardMeasurementIndices;
        private final String fileName;
        private final int fileOffset;

        public PointInfo(Element element, Map<String, MDTInnerFileData> innerFileDatas)
        {
            this.index = Integer.parseInt(element.getAttribute(POINT_INDEX_ATTRIUTE).trim());

            this.coordinates[0] = Double.parseDouble(element.getAttribute(X_COORDINATE_ATRIBUTE).trim().replaceAll(",","."));
            this.coordinates[1] = Double.parseDouble(element.getAttribute(Y_COORDINATE_ATRIBUTE).trim().replaceAll(",","."));
            this.coordinates[2] = Double.parseDouble(element.getAttribute(Z_COORDINATE_ATRIBUTE).trim().replaceAll(",","."));

            //            System.out.println("index " + index);
            //
            //            System.out.println("coordinates[0] " + coordinates[0]);
            //            System.out.println("coordinates[1] " + coordinates[1]);
            //            System.out.println("coordinates[2] " + coordinates[2]);

            String unitString = element.getAttribute(UNIT_ATTRIBUTE).trim();
            this.unit = unitString.isEmpty() ? null : UnitUtilities.getSIUnit(unitString);

            this.measurementCount = Integer.parseInt(element.getAttribute(MEASUREMENT_COUNT_ATTRIBUTE).trim());
            this.executionCount = Integer.parseInt(element.getAttribute(EXECUTION_COUNT_ATTRIBUTE).trim());

            //            System.out.println("measurementCount " + measurementCount);

            this.fileOffset = Integer.parseInt(element.getAttribute(FILE_OFFSET_ATTRIBUTE).trim());

            //            System.out.println("fileOffset " + fileOffset);

            this.fileName = element.getAttribute(FILE_NAME_ATTRIBUTE).trim();

            ByteBuffer dataBlockBuffer = innerFileDatas.get(fileName).getByteBuffer();
            dataBlockBuffer.position(fileOffset);

            this.forwardMeasurementIndices = new int[measurementCount*executionCount];
            this.backwardMeasurementIndices = new int[measurementCount*executionCount];
            //
            //            System.out.println("forwardMeasurementIndices " + forwardMeasurementIndices.length);
            //            System.out.println("backwardMeasurementIndices " + backwardMeasurementIndices.length);


            FileInputUtilities.populateIntArray(forwardMeasurementIndices, dataBlockBuffer);
            FileInputUtilities.populateIntArray(backwardMeasurementIndices, dataBlockBuffer);
        }

        private Point2D getRecordingPoint()
        {
            if(this.unit == null || Double.isNaN(coordinates[0]) || Double.isNaN(coordinates[1]))
            {       
                return null;
            }

            PrefixedUnit requiredUnit = Units.MICRO_METER_UNIT;
            double factor = this.unit.getConversionFactorTo(requiredUnit);

            Point2D p = new Point2D.Double(factor*coordinates[0], factor*coordinates[1]);

            return p;
        }

        private List<SimpleSpectroscopySource> buildSources(File f, int frameIndex, boolean addSuffix, List<MeasurementInfo> measurementInfos, List<AxisInfo> axisInfos, List<DataInfo> dataInfos, List<NameInfo> nameInfos, Map<String, MDTInnerFileData> innerFileDatas)
        {
            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            for(int i = 0; i<Math.max(forwardMeasurementIndices.length, backwardMeasurementIndices.length); i++)
            {
                int forwardIndex = forwardMeasurementIndices.length > i ? forwardMeasurementIndices[i] : -1;
                int backwardIndex = backwardMeasurementIndices.length > i ? backwardMeasurementIndices[i] : -1;

                //                System.out.println("m i " + i);
                try
                {
                    MeasurementInfo approachChannelMeasurementInfo = forwardIndex > -1 ? measurementInfos.get(forwardIndex): null;
                    MeasurementInfo withdrawChannelMeasurementInfo = backwardIndex > -1 ? measurementInfos.get(backwardIndex) : null;
                    //
                    //                    System.out.println("approachChannelMeasurementInfo != null " + (approachChannelMeasurementInfo != null ));
                    //                    System.out.println("withdrawChannelMeasurementInfo != null " + (withdrawChannelMeasurementInfo != null ));

                    if(approachChannelMeasurementInfo != null && !approachChannelMeasurementInfo.isForceCurveY(nameInfos))
                    {
                        continue;
                    }

                    if(withdrawChannelMeasurementInfo != null && !withdrawChannelMeasurementInfo.isForceCurveY(nameInfos))
                    {
                        continue;
                    }
                    //                    System.out.println("withdrawChannelMeasurementInfo.getIndex() " + withdrawChannelMeasurementInfo.getIndex());
                    //
                    //                    System.out.println("withdrawChannelMeasurementInfo.getDataInfoIndex() " + withdrawChannelMeasurementInfo.getDataInfoIndex());
                    //                    System.out.println("withdrawChannelMeasurementInfo.getSignalInfoIndex() " + withdrawChannelMeasurementInfo.getSignalInfoIndex());

                    Channel1DData approachChannel = approachChannelMeasurementInfo != null ? approachChannelMeasurementInfo.buildChannel(axisInfos, dataInfos, nameInfos, innerFileDatas) : FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_VOLTS);
                    Channel1DData withdrawChannel = withdrawChannelMeasurementInfo != null ? withdrawChannelMeasurementInfo.buildChannel(axisInfos, dataInfos, nameInfos, innerFileDatas) : FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_VOLTS);

                    String suffix = addSuffix ? " (" + Integer.toString(frameIndex) + "," + Integer.toString(index) + "," + Integer.toString(i) + ")": "";

                    //                    System.out.println("suffix " + suffix);

                    String longName = f.getAbsolutePath() + suffix;
                    String shortName = IOUtilities.getBareName(f) + suffix;

                    StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approachChannel, withdrawChannel);
                    source.setRecordingPoint(getRecordingPoint());

                    PhotodiodeSignalType photodiodeSignalType = PhotodiodeSignalType.ELECTRIC_CURRENT;// : PhotodiodeSignalType.VOLTAGE;
                    source.setPhotodiodeSignalType(photodiodeSignalType);

                    sources.add(source);

                }
                catch(Exception e)               
                {
                    e.printStackTrace();
                }
            }

            return sources;
        }

        @Override
        public int getIndex()
        {
            return index;
        }

        public double[] getCoordinates()
        {
            return coordinates;
        }

        public PrefixedUnit getSIUnit()
        {
            return unit;
        }

        public int getMeasurementCount()
        {
            return measurementCount;
        }

        public int getExecutionCount()
        {
            return executionCount;
        }

        public int getLineCount()
        {
            return measurementCount*executionCount;
        }

        public int[] getForwardMeasurementIndices()
        {
            return forwardMeasurementIndices;
        }

        public int[] getBackwardMeasurementIndices()
        {
            return backwardMeasurementIndices;
        }
    }

    private static enum AxisOption
    {
        //inverts means that we must exchange start value and stop value
        //relative means that we must set init value to 0, i.e. from start value and stop value we
        //subtract init value
        INVERTED(1), RELATIVE(2);

        private final int code;

        AxisOption(int code)
        {
            this.code = code;
        }

        public boolean isToggledOn(int bitField)
        {
            return ((code & bitField) != 0);
        }
    }

    private static class MeasurementInfo implements InternalFileInfo
    {
        private static final String CORRESPONDING_TAG_NAME = "Meas";

        private static final String MEASUREMENT_INDEX_ATTRIUTE = "index";
        private static final String SIGNAL_INFO_INDEX_ATTRIBUTE = "name";
        private static final String DATA_INFO_INDEX_ATTRIBUTE = "data";
        private static final String AXIS_INFO_INDEX_ATTRIBUTE_ROOT = "axis";
        private static final String AXIS_OPTION_ATTRIBUTE_ROOT = "inverse";

        private final int measurementIndex;

        private final int signalInfoIndex;
        private final int dataInfoIndex;

        private final int[] axisInfoIndices = new int[DAA_AXIS_COUNT];
        private final int[] axisOptions = new int[DAA_AXIS_COUNT];

        public MeasurementInfo(Element element)
        {
            this.measurementIndex = Integer.parseInt(element.getAttribute(MEASUREMENT_INDEX_ATTRIUTE).trim());

            this.signalInfoIndex = Integer.parseInt(element.getAttribute(SIGNAL_INFO_INDEX_ATTRIBUTE).trim());
            this.dataInfoIndex = Integer.parseInt(element.getAttribute(DATA_INFO_INDEX_ATTRIBUTE).trim());

            for(int i = 0; i<DAA_AXIS_COUNT; i++)
            {
                String axisInfoString = element.getAttribute(AXIS_INFO_INDEX_ATTRIBUTE_ROOT + Integer.toString(i)).trim();
                this.axisInfoIndices[i] = axisInfoString.isEmpty() ? -1 : Integer.parseInt(axisInfoString);
            }

            for(int i = 0; i<DAA_AXIS_COUNT; i++)
            {
                String axisOptionString = element.getAttribute(AXIS_OPTION_ATTRIBUTE_ROOT + Integer.toString(i)).trim();
                this.axisOptions[i] = axisOptionString.isEmpty() ? 0 
                        : Integer.parseInt(axisOptionString);
            }
        }

        @Override
        public int getIndex()
        {
            return measurementIndex;
        }

        public int getSignalInfoIndex()
        {
            return signalInfoIndex;
        }

        public int getDataInfoIndex()
        {
            return dataInfoIndex;
        }

        public boolean isForceCurveY(List<NameInfo> nameInfos)
        {
            NameInfo dNameInfo = nameInfos.get(signalInfoIndex);
            NameInfoType dNameInfoType = dNameInfo.getNameInfoType();
            boolean isForceCurveY = dNameInfoType.isForceCurveY();

            return isForceCurveY;
        }

        public GridChannel1DData buildChannel(List<AxisInfo> axisInfos, List<DataInfo> dataInfos, List<NameInfo> nameInfos, Map<String, MDTInnerFileData> innerFileDatas)
        {
            AxisInfo zAxisInfo = axisInfos.get(axisInfoIndices[0]);
            int zAxisOption = axisOptions[0];
            DataInfo dDataInfo = dataInfos.get(dataInfoIndex);
            NameInfo dNameInfo = nameInfos.get(signalInfoIndex);

            QuantityArray1DExpression dValues = dDataInfo.collectInformation(dNameInfo, innerFileDatas);

            GridChannel1DData channel = zAxisInfo.buildChannel(dValues, zAxisOption, dataInfos, nameInfos, innerFileDatas);
            return channel;
        }
    }


    private static class AxisInfo implements InternalFileInfo
    {
        private static final String CORRESPONDING_TAG_NAME = "Axis";

        private static final String AXIS_INDEX_ATTRIBUTE = "index";
        private static final String SIGNAL_INFO_INDEX_ATTRIBUTE = "name";
        private static final String POINT_COUNT_ATTRIBUTE = "count";
        private static final String INIT_VALUE_ATTRIBUTE = "value";
        private static final String START_VALUE_ATTRIBUTE = "start";
        private static final String STOP_VALUE_ATTRIBUTE = "stop";
        private static final String DATA_INFO_IND_ATTRIBUTE = "data";

        private final int axisIndex;

        private final int signalInfoIndex;
        private final double initValue;
        private final double startValue;
        private final double stopValue;
        private final int pointCount;
        private final int dataInfoInd;

        public AxisInfo(Element element)
        {
            this.axisIndex = Integer.parseInt(element.getAttribute(AXIS_INDEX_ATTRIBUTE).trim());
            this.signalInfoIndex = Integer.parseInt(element.getAttribute(SIGNAL_INFO_INDEX_ATTRIBUTE).trim());
            this.initValue = Double.parseDouble(element.getAttribute(INIT_VALUE_ATTRIBUTE).trim().replaceAll(",","."));
            this.startValue = Double.parseDouble(element.getAttribute(START_VALUE_ATTRIBUTE).trim().replaceAll(",","."));
            this.stopValue = Double.parseDouble(element.getAttribute(STOP_VALUE_ATTRIBUTE).trim().replaceAll(",","."));
            this.pointCount = Integer.parseInt(element.getAttribute(POINT_COUNT_ATTRIBUTE).trim());
            this.dataInfoInd = Integer.parseInt(element.getAttribute(DATA_INFO_IND_ATTRIBUTE).trim());
        }

        public boolean isRampLinear()
        {
            return true;
        }

        public double getRealStart(int option)
        {
            double toSubtract = AxisOption.RELATIVE.isToggledOn(option) ? initValue : 0;
            double realStart = AxisOption.INVERTED.isToggledOn(option) ? stopValue - toSubtract : startValue - toSubtract;

            return realStart;
        }

        public double getRealEnd(int option)
        {
            double toSubtract = AxisOption.RELATIVE.isToggledOn(option) ? initValue : 0;
            double realEnd = AxisOption.INVERTED.isToggledOn(option) ? startValue - toSubtract :  stopValue - toSubtract;

            return realEnd;
        }

        public double getRealIncrement(int option)
        {
            double realStart = getRealStart(option);
            double realEnd = getRealEnd(option);

            double increment = (realEnd - realStart)/pointCount;

            return increment;
        }

        public GridChannel1DData buildChannel(QuantityArray1DExpression valuesUnitArray, int option, List<DataInfo> dataInfos, List<NameInfo> nameInfos, Map<String, MDTInnerFileData> innerFileDatas)
        {
            GridChannel1DData channel = null;
            NameInfo zNameInfo = nameInfos.get(signalInfoIndex);


            PrefixedUnit zNameUnit = zNameInfo.getSIUnit();
            Quantity zQuantity = Quantities.DISTANCE_MICRONS;
            double zFactor = zNameUnit.getConversionFactorTo(zQuantity.getUnit());

            Quantity rangeQuantity = valuesUnitArray.getQuantity();

            if(isRampLinear())
            {
                Grid1D grid = new Grid1D(zFactor*getRealIncrement(option), zFactor*getRealStart(option), valuesUnitArray.getValueCount(), zQuantity);
                channel = new GridChannel1DData(valuesUnitArray.getValues(), grid, rangeQuantity);

                return channel;
            }
            else
            {
                throw new IllegalStateException("Parametric ramps not yet supported");
            }
        }

        @Override
        public int getIndex()
        {
            return axisIndex;
        }
    }

    private static class NameInfo implements InternalFileInfo
    {
        private static final String CORRESPONDING_TAG_NAME = "Name";

        private static final String SIGNAL_INDEX_ATTRIBUTE = "index";
        private static final String SIGNAL_NAME_ATTRIBUTE = "name";
        private static final String UNIT_ATTRIBUTE = "unit";
        private static final String BIAS_ATTRBUTE = "bias";
        private static final String SCALE_ATTRIBUTE = "scale";

        private final int signalIndex;

        private final String name;
        private final PrefixedUnit unit;
        private final double scale;
        private final double bias;
        private final NameInfoType nameInfoType;

        public NameInfo(Element element)
        {
            this.signalIndex = Integer.parseInt(element.getAttribute(SIGNAL_INDEX_ATTRIBUTE).trim());
            this.name = element.getAttribute(SIGNAL_NAME_ATTRIBUTE).trim();
            this.unit = UnitUtilities.getSIUnit(element.getAttribute(UNIT_ATTRIBUTE).trim());
            this.scale = Double.parseDouble(element.getAttribute(SCALE_ATTRIBUTE).trim().replaceAll(",","."));
            this.bias = Double.parseDouble(element.getAttribute(BIAS_ATTRBUTE).trim().replaceAll(",","."));
            this.nameInfoType = NameInfoType.getNameInfoTypes(name);            
        }

        public double getScale()
        {
            return scale;
        }

        public double getBias()
        {
            return bias;
        }

        public PrefixedUnit getSIUnit()
        {
            return unit;
        }

        @Override
        public int getIndex()
        {
            return signalIndex;
        }

        public String getName()
        {
            return name;
        }

        public Quantity getDefaultQuantity()
        {
            return nameInfoType.getDefaultQuantity(name, unit);
        }

        public NameInfoType getNameInfoType()
        {
            return nameInfoType;
        }
    }

    private static enum NameInfoType
    {
        DEFLECTION(Pattern.compile("DFL"),true)
        {
            @Override
            public Quantity getDefaultQuantity(String name, PrefixedUnit unit)
            {
                return CalibrationState.getDefaultYQuantity(unit);
            }
        }, 

        DEFLECTION_N(Pattern.compile("DFLn"),false) {
            @Override
            public Quantity getDefaultQuantity(String name, PrefixedUnit unit) {
                return new DimensionlessQuantity("Deflection normalized",unit);
            }
        }, 

        Z(Pattern.compile("Z"), false)
        {
            @Override
            public Quantity getDefaultQuantity(String name, PrefixedUnit unit) 
            {
                return Quantities.DISTANCE_MICRONS;
            }
        }, 

        UNKNOWN(Pattern.compile(".*"),  false)
        {
            @Override
            public Quantity getDefaultQuantity(String name, PrefixedUnit unit)
            {
                return new UnitQuantity(name, unit);
            }
        };

        private final Pattern namePattern;
        private final boolean isForceCurveY;

        NameInfoType(Pattern namePattern, boolean isForceCurveY)
        {
            this.namePattern = namePattern;
            this.isForceCurveY = isForceCurveY;
        }


        public boolean isForceCurveY()
        {
            return isForceCurveY;
        }

        public abstract Quantity getDefaultQuantity(String name, PrefixedUnit unit);

        public static NameInfoType getNameInfoTypes(String name)
        {            
            for(NameInfoType type : NameInfoType.values())
            {
                Matcher matcher = type.namePattern.matcher(name);
                if (matcher.matches())
                {
                    return type;
                }
            }

            throw new IllegalArgumentException("Could not found NameInfoType for " + name);
        }
    }


    private static enum MDTDataType
    {
        INT_32("int32", 4), FLOAT_64("float64", 8);

        private final String name;
        private final int byteCount;

        MDTDataType(String name, int byteCount)
        {
            this.name = name;
            this.byteCount = byteCount;
        }

        public int getByteCount()
        {
            return byteCount;
        }

        public static MDTDataType getDataType(String name)
        {
            for(MDTDataType type : MDTDataType.values())
            {
                if(type.name.equals(name))
                {
                    return type;
                }
            }

            throw new IllegalArgumentException("Invalid MDTDataType code: " + name);
        }
    }

    private static class DataInfo implements InternalFileInfo
    {
        private static final String CORRESPONDING_TAG_NAME = "Data";

        private static final String DATA_INDEX_ATTRIBUTE = "index";
        private static final String FILE_NAME_ATTRIBUTE = "name";
        private static final String FILE_OFFSET_ATTRIBUTE = "offset";
        private static final String DATA_COUNT_ATTRIBUTE = "count";
        private static final String DATA_TYPE_ATTRIBUTE = "type";

        private final int index;
        private final String fileName;
        private final int fileOffset;
        private final int dataCount;
        private final MDTDataType dataType;

        public DataInfo(Element element)
        {
            this.index = Integer.parseInt(element.getAttribute(DATA_INDEX_ATTRIBUTE).trim());
            this.fileName = element.getAttribute(FILE_NAME_ATTRIBUTE).trim();
            this.fileOffset = Integer.parseInt(element.getAttribute(FILE_OFFSET_ATTRIBUTE).trim());
            this.dataCount = Integer.parseInt(element.getAttribute(DATA_COUNT_ATTRIBUTE).trim());
            this.dataType = MDTDataType.getDataType(element.getAttribute(DATA_TYPE_ATTRIBUTE).trim());
        }

        @Override
        public int getIndex()
        {
            return index;
        }

        public QuantityArray1DExpression collectInformation(NameInfo nameInfo, Map<String, MDTInnerFileData> innerFileDatas)
        {
            PrefixedUnit unit = nameInfo.getSIUnit();

            //            System.out.println("unit " + unit);
            //            System.out.println("index " + index);
            //            System.out.println("fileOffset " + fileOffset);
            //            System.out.println("dataType " + dataType);
            //            System.out.println("dataCount " + dataCount);
            //            System.out.println("fileName " + fileName);

            MDTInnerFileData innerFile = innerFileDatas.get(fileName);

            Quantity defaultQuantity = nameInfo.getDefaultQuantity();
            double conversionFactor = defaultQuantity != null ? unit.getConversionFactorTo(defaultQuantity.getUnit()) : 1;

            double[] values = null;
            if(MDTDataType.FLOAT_64.equals(dataType))
            {
                values = innerFile.readInAsDoubles(dataCount, dataType.getByteCount()*fileOffset, conversionFactor, 0);
            }
            else if(MDTDataType.INT_32.equals(dataType))
            {
                values = innerFile.readInAsInts(dataCount, dataType.getByteCount()*fileOffset, 
                        nameInfo.getScale(), nameInfo.getBias());
            }

            QuantityArray1DExpression arrayExpression = new QuantityArray1DExpression(values, defaultQuantity);
            return arrayExpression;
        }
    }

    private static <E extends InternalFileInfo> List<E> sortAccordingToIndexes(List<E> infos)
    {
        int largestIndex = -1;

        for(InternalFileInfo info : infos)
        {
            largestIndex = Math.max(largestIndex, info.getIndex());
        }

        List<E> infosArray = new ArrayList<>(Collections.nCopies(largestIndex + 1, (E)null));
        for(E info : infos)
        {
            infosArray.set(info.getIndex(), info);
        }

        return infosArray;
    }

    @Override
    public List<ChannelSource> readInAllSources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {
        List<ChannelSource> sources = new ArrayList<>();

        try {
            long initPosition = channel.position();

            List<SimpleSpectroscopySource> readInSpectroscopySources = readInSpectroscopySources(f, channel, frameIndex, frameHeader);
            sources.addAll(readInSpectroscopySources);

            channel.position(initPosition);

            List<ImageSource> readInImageSources = readInImages(f, channel, frameIndex, frameHeader);
            sources.addAll(readInImageSources);
        } 
        catch (IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading a file",e);
        }

        return sources;
    }

    @Override
    public List<ImageSource> readInImages(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {
        return Collections.emptyList();
    }

    @Override
    public int getFrameType() 
    {
        return FRAME_TYPE;
    }
}

