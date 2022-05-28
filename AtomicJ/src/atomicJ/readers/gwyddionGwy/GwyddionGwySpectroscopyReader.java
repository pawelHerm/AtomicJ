
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

package atomicJ.readers.gwyddionGwy;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

import atomicJ.analysis.ForceCurveBranch;
import atomicJ.analysis.ForceCurveOrientation;
import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleChannel1DData;
import atomicJ.data.Grid1D;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.StandardQuantityTypes;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.AbstractSourceReader;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.SourceReadingDirectives;
import atomicJ.readers.gwyddionGwy.GwyddionGwyImageReader.GwyDataField;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileExtensionPatternFilter;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.MathUtilities;

public class GwyddionGwySpectroscopyReader extends AbstractSourceReader<SimpleSpectroscopySource>
{    
    private static final String[] ACCEPTED_EXTENSIONS = new String[] {"gwy"};
    private static final String DESCRIPTION = "Gwyddion native format force curve (.gwy)";
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
        List<SimpleSpectroscopySource> spectroscopySources = new ArrayList<>();

        Map<String, GwyGraphModel> graphModels = new LinkedHashMap<>();
        Map<String, GwyBrick> gwyBricks = new LinkedHashMap<>();
        Map<String, Integer> componentByteBufferPositions = new HashMap<>();

        boolean containsImages = false;

        try(FileChannel channel = (FileChannel)Files.newByteChannel(f.toPath());) 
        {         
            ByteBuffer gwyContainerData = GwyddionGwySourceReader.readGwyContainerData(channel);
            while(gwyContainerData.hasRemaining())
            {
                String componentName = FileInputUtilities.readInNullTerminatedString(gwyContainerData);
                componentByteBufferPositions.put(componentName, Integer.valueOf(gwyContainerData.position()));

                char componentTypeChar = (char)gwyContainerData.get();
                GwyDataType componentType = GwyDataType.getDataType(componentTypeChar);                

                if(GwyDataTypeSimple.OBJECT.equals(componentType))
                {
                    String objectNameTrimmed = FileInputUtilities.readInNullTerminatedString(gwyContainerData).trim();

                    if(GwyGraphModel.GWY_GRAPH_MODEL_NAME.equals(objectNameTrimmed))
                    {                                               
                        GwyGraphModel graphModel = GwyGraphModel.readInObjectExceptForName(gwyContainerData);
                        graphModels.put(componentName, graphModel);                        
                    }
                    else if(GwyBrick.GWY_BRICK_NAME.equals(objectNameTrimmed))
                    {
                        GwyBrick gwyBrick = GwyBrick.readInObjectExceptForName(gwyContainerData);
                        gwyBricks.put(componentName, gwyBrick);
                    }
                    else
                    {
                        if(GwyDataField.GWY_DATA_FIELD_NAME.equals(objectNameTrimmed))
                        {
                            containsImages = true;
                        }
                        //we cannot just call GwyDataTypeSimple.OBJECT.skip(), because we have already read in the object name
                        long dataSize = FileInputUtilities.getUnsigned(gwyContainerData.getInt());

                        int position = gwyContainerData.position();
                        gwyContainerData.position(position + (int)dataSize);
                    }
                }
                else
                {
                    componentType.skipData(gwyContainerData); 
                }
            }

            boolean multipleCurves = graphModels.size() > 1;

            Iterator<GwyGraphModel> graphModelIterator = graphModels.values().iterator();

            for(int i = 0; i< graphModels.size();i++)
            {
                GwyGraphModel graphModel = graphModelIterator.next();

                if(!graphModel.isWellSpecifiedForceCurve())
                {
                    continue;
                }

                String suffix = multipleCurves ? " (" + Integer.toString(i) + ")" : "";
                String longName = f.getAbsolutePath() + suffix;
                String shortName = IOUtilities.getBareName(f) + suffix;

                Channel1DData approach = graphModel.getApproachData();           
                Channel1DData withdraw = graphModel.getWithdrawData();
                StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName,approach, withdraw);

                PhotodiodeSignalType photodiodeSignalType = graphModel.getPhotodiodeSignalType();
                source.setPhotodiodeSignalType(photodiodeSignalType);

                spectroscopySources.add(source);
            }

            List<ForceVolumeBrickPair> finishedBrickPairs = new ArrayList<>();
            ForceVolumeBrickPair lastBrickPair = new ForceVolumeBrickPair();

            for(Entry<String, GwyBrick> entry : gwyBricks.entrySet())
            {
                GwyBrick brick = entry.getValue();   

                if(!brick.isWellSpecifiedVolumeRecording())
                {
                    continue;
                }

                String gwyBrickComponentName = entry.getKey();

                String titleComponentName = new StringBuilder(gwyBrickComponentName).append(GwyddionGwySourceReader.COMPONENT_NAME_SEPARATOR).append(GwyddionGwySourceReader.TITILE_COMPONENT_NAME_SUFFIX).toString();  

                Integer titlePosition = (titleComponentName != null) ? componentByteBufferPositions.get(titleComponentName) : null;
                String titleRoot = "";
                if(titlePosition != null)
                {
                    gwyContainerData.position(titlePosition);

                    char titleType = (char)gwyContainerData.get(); 
                    titleRoot = (GwyDataTypeSimple.STRING.getTypeChar() == titleType) ? FileInputUtilities.readInNullTerminatedString(gwyContainerData) : titleRoot;                  
                }

                ForceCurveBranch branch = ForceCurveBranch.guessBranchFromTextualDescription(titleRoot);

                if(branch == null)
                {
                    continue;
                }

                if(lastBrickPair.canBeAdded(brick, branch))
                {
                    lastBrickPair.addBrick(brick, branch);

                    if(lastBrickPair.containsBothBranches())
                    {
                        finishedBrickPairs.add(lastBrickPair);
                        lastBrickPair = new ForceVolumeBrickPair();
                    }
                }
                else
                {
                    finishedBrickPairs.add(lastBrickPair);
                    lastBrickPair = new ForceVolumeBrickPair();
                    lastBrickPair.addBrick(brick, branch);
                }
            }

            if(lastBrickPair.containsAnyData())
            {
                finishedBrickPairs.add(lastBrickPair);
            }

            for(ForceVolumeBrickPair brickPair : finishedBrickPairs)
            {
                spectroscopySources.addAll(brickPair.buildSpectroscopySources(f, containsImages));
            }
        } 
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return spectroscopySources;                 
    }

    private static class GwyGraphModel 
    {
        public static final String GWY_GRAPH_MODEL_NAME = "GwyGraphModel";

        private static final String CURVES_COMPONENT = "curves";
        private static final String TITLE_COMPONENT = "title";
        private static final String X_UNIT_COMPONENT = "x_unit";
        private static final String Y_UNIT_COMPONENT = "y_unit";

        private final List<GwyGraphCurveModel> approachCurves = new ArrayList<>();
        private final List<GwyGraphCurveModel> withdrawCurves = new ArrayList<>();

        private PrefixedUnit xUnit;
        private PrefixedUnit yUnit;

        private GwyGraphModel(){};

        public boolean isWellSpecified() 
        {
            boolean wellSpecified = (xUnit != null) && (yUnit != null) && !(approachCurves.isEmpty() && withdrawCurves.isEmpty());                     
            return wellSpecified;
        }

        public boolean isWellSpecifiedForceCurve()
        {
            boolean wellSpecified = isWellSpecified() && StandardQuantityTypes.LENGTH.isCompatible(xUnit) && CalibrationState.isCompatibleWithAnyCalibrationState(yUnit);            
            return wellSpecified;
        }

        public List<GwyGraphCurveModel> getCurves()
        {
            return Collections.unmodifiableList(approachCurves);
        }

        public Channel1DData getApproachData()
        {
            if(!isWellSpecifiedForceCurve())
            {
                return null;
            }

            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
            Quantity yQuantity = CalibrationState.getDefaultYQuantity(yUnit);

            if(approachCurves.isEmpty())
            {
                return FlexibleChannel1DData.getEmptyInstance(xQuantity, yQuantity);
            }

            GwyGraphCurveModel approachCurveModel = approachCurves.get(0);

            double xFactor = xUnit.getConversionFactorTo(xQuantity.getUnit());
            double yFactor = yUnit.getConversionFactorTo(yQuantity.getUnit());

            Channel1DData approachData = new FlexibleChannel1DData(approachCurveModel.getDataPointsInCorrectOrientation(xFactor, yFactor), xQuantity, yQuantity, SortedArrayOrder.DESCENDING);
            return approachData;
        }

        public Channel1DData getWithdrawData()
        {
            if(!isWellSpecifiedForceCurve())
            {
                return null;
            }

            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
            Quantity yQuantity = CalibrationState.getDefaultYQuantity(yUnit);

            if(withdrawCurves.isEmpty())
            {
                return FlexibleChannel1DData.getEmptyInstance(xQuantity, yQuantity);
            }

            GwyGraphCurveModel withdrawCurveModel = withdrawCurves.get(0);

            double xFactor = xUnit.getConversionFactorTo(xQuantity.getUnit());
            double yFactor = yUnit.getConversionFactorTo(yQuantity.getUnit());

            Channel1DData approachData = new FlexibleChannel1DData(withdrawCurveModel.getDataPointsInCorrectOrientation(xFactor, yFactor), xQuantity, yQuantity, SortedArrayOrder.ASCENDING);
            return approachData;
        }

        public PhotodiodeSignalType getPhotodiodeSignalType()
        {
            return PhotodiodeSignalType.getSignalType(yUnit, PhotodiodeSignalType.VOLTAGE);
        }

        private static GwyGraphModel readInObjectExceptForName(ByteBuffer byteBuffer) throws UserCommunicableException
        {
            GwyGraphModel graphModel = new GwyGraphModel();

            long dataSize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
            int finalPosition = byteBuffer.position() + (int)dataSize;

            while(byteBuffer.position() < finalPosition)
            {
                String componentNameTrimmed = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
                char componentTypeChar = (char)byteBuffer.get();

                if(CURVES_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeArray.OBJECT_ARRAY.getTypeChar() == componentTypeChar)
                {
                    int xDataArraySize = (int)FileInputUtilities.getUnsigned(byteBuffer.getInt());

                    for(int i = 0; i<xDataArraySize;i++)
                    {
                        GwyGraphCurveModel curveModel = GwyGraphCurveModel.readInObject(byteBuffer);
                        if(ForceCurveBranch.APPROACH.equals(curveModel.getCurveBranch()))
                        {
                            graphModel.approachCurves.add(curveModel);
                        }
                        else if(ForceCurveBranch.WITHDRAW.equals(curveModel.getCurveBranch()))
                        {
                            graphModel.withdrawCurves.add(curveModel);
                        }
                    } 
                }
                else if(X_UNIT_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    graphModel.xUnit = gwyUnit.getUnit();
                }
                else if(Y_UNIT_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    graphModel.yUnit = gwyUnit.getUnit();
                }
                else
                {
                    GwyDataType.getDataType(componentTypeChar).skipData(byteBuffer);
                }
            }

            byteBuffer.position(finalPosition);

            return graphModel;
        }
    }

    private static class GwyGraphCurveModel 
    {
        public static final String GWY_GRAPH_CURVE_MODEL_NAME = "GwyGraphCurveModel";

        private static final String X_DATA_COMPONENT = "xdata";//Abscissa points. The number of points must match ydata.
        private static final String Y_DATA_COMPONENT = "ydata";//Ordinate points. The number of points must match xdata.
        private static final String DESCRIPTION_COMPONENT = "description";//Curve description (name).

        private String description = "";
        private ForceCurveBranch curveBranch;
        private double[] xData;
        private double[] yData;

        private GwyGraphCurveModel(){};

        public boolean isWellSpecified() 
        {
            boolean wellSpecified = (xData != null) && (yData != null) && (xData.length == yData.length) && (curveBranch != null);                     
            return wellSpecified;
        }

        public double[][] getDataPointsInCorrectOrientation(double xFactor, double yFactor)
        {
            if(!isWellSpecified())
            {
                return new double[][] {};
            }

            //we assume that the x-coordinates are sorted, either in descending or ascending order
            int n = xData.length;

            double[][] points = new double[n][];

            for(int i = 0; i<n;i++)
            {
                points[i] = new double[] {xFactor*xData[i], yFactor*yData[i]};
            }

            //ForceCurveOrientation.correctOrientation requires that the passed array of points is sorted according to x-coordinate, either in ascending or descending order
            //we cannot be sure that the points are sorted, we first find the initial x order
            //because if they are ordered or almost ordered, the subsequent sorting is faster
            SortedArrayOrder initialOrder = SortedArrayOrder.getInitialXOrder(points);
            initialOrder.sortX(points);

            double[][] pointsCorrected = ForceCurveOrientation.LEFT.correctOrientation(points, curveBranch);

            return pointsCorrected;
        }

        public ForceCurveBranch getCurveBranch()
        {
            return curveBranch;
        }

        public String getDescription()
        {
            return description;
        }

        private static GwyGraphCurveModel readInObject(ByteBuffer byteBuffer) throws UserCommunicableException
        {
            FileInputUtilities.readInNullTerminatedString(byteBuffer); //reads in object name
            GwyGraphCurveModel model = readInObjectExceptForName(byteBuffer);
            return model;
        }

        private static GwyGraphCurveModel readInObjectExceptForName(ByteBuffer byteBuffer) throws UserCommunicableException
        {
            GwyGraphCurveModel curveModel = new GwyGraphCurveModel();

            long dataSize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
            int finalPosition = byteBuffer.position() + (int)dataSize;

            while(byteBuffer.position() < finalPosition)
            {
                String componentNameTrimmed = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
                char componentTypeChar = (char)byteBuffer.get();

                if(DESCRIPTION_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeSimple.STRING.getTypeChar() == componentTypeChar)
                {
                    curveModel.description = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
                    curveModel.curveBranch = ForceCurveBranch.guessBranchFromTextualDescription(curveModel.description);
                }
                else if(X_DATA_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeArray.DOUBLE_ARRAY.getTypeChar() == componentTypeChar)
                {
                    long xDataArraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
                    curveModel.xData = DoubleArrayReaderType.FLOAT64.readIn1DArray((int)xDataArraySize, 1, byteBuffer);
                }
                else if(Y_DATA_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeArray.DOUBLE_ARRAY.getTypeChar() == componentTypeChar)
                {
                    long yDataArraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
                    curveModel.yData = DoubleArrayReaderType.FLOAT64.readIn1DArray((int)yDataArraySize, 1, byteBuffer);
                }
                else
                {
                    GwyDataType.getDataType(componentTypeChar).skipData(byteBuffer);
                }
            }

            byteBuffer.position(finalPosition);

            return curveModel;
        }
    }

    private static class ForceVolumeBrickPair
    {
        private final Map<ForceCurveBranch, GwyBrick> bricks = new HashMap<>();

        public boolean containsAnyData()
        {
            return (!bricks.isEmpty());
        }

        public boolean containsBothBranches()
        {
            boolean containsBothBranches = bricks.containsKey(ForceCurveBranch.APPROACH) && bricks.containsKey(ForceCurveBranch.WITHDRAW);
            return containsBothBranches;
        }

        public void addBrick(GwyBrick brick, ForceCurveBranch branch)
        {
            if(brick != null)
            {
                this.bricks.put(branch, brick);
            }
        }

        public boolean canBeAdded(GwyBrick newBrick, ForceCurveBranch branch)
        {
            if(bricks.containsKey(branch))
            {
                return false;
            }

            for(GwyBrick brick : bricks.values())
            {
                if(!brick.isConsistentWithOtherVolumeRecording(newBrick))
                {
                    return false;
                }
            }

            return true;
        }

        public List<SimpleSpectroscopySource> buildSpectroscopySources(File f, boolean containImages)
        {           
            GwyBrick approachBrick = bricks.get(ForceCurveBranch.APPROACH);
            GwyBrick withdrawBrick = bricks.get(ForceCurveBranch.WITHDRAW);

            if(approachBrick == null && withdrawBrick == null)
            {
                return Collections.emptyList();
            }

            GwyBrick nonNullBrick = (approachBrick != null) ? approachBrick : withdrawBrick;
            GwyBrick otherBrick = (approachBrick != null) ? withdrawBrick : approachBrick;

            if(!nonNullBrick.isConsistentWithOtherVolumeRecording(otherBrick))
            {
                return Collections.emptyList();
            }

            List<SimpleSpectroscopySource> sources = new ArrayList<>();

            Grid2D grid = nonNullBrick.getDomainGrid();

            Channel1DData[][] approachData = (approachBrick != null) ? approachBrick.getCurveChannels(false) : null;
            Channel1DData[][] withdrawData = (withdrawBrick != null) ? withdrawBrick.getCurveChannels(true) : null;

            Quantity wQuantity = nonNullBrick.getCurveValueQuantity();
            PhotodiodeSignalType photodiodeSignalType = nonNullBrick.getPhotodiodeSignalType();

            int rowCount = grid.getRowCount();
            int columnCount = grid.getColumnCount();

            int index = 0;

            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    Channel1DData approach = (approachData != null) ? approachData[i][j] : FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, wQuantity);
                    Channel1DData withdraw = (withdrawData != null) ? withdrawData[i][j] : FlexibleChannel1DData.getEmptyInstance(Quantities.DISTANCE_MICRONS, wQuantity);

                    String suffix = " (" + Integer.toString(index++) + ")" ;
                    String longName = f.getAbsolutePath() + suffix;
                    String shortName = IOUtilities.getBareName(f) + suffix;

                    StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approach, withdraw);

                    source.setPhotodiodeSignalType(photodiodeSignalType);
                    source.setRecordingPoint(grid.getPoint(i, j));

                    sources.add(source);
                }
            }

            MapSource<?> mapSource = new MapGridSource(f, sources, grid);   

            ReadingPack<ImageSource> readingPack = containImages ? new FileReadingPack<>(f, new GwyddionGwyImageReader()) : null; 
            mapSource.setMapAreaImageReadingPack(readingPack);

            return sources;
        }
    }

    private static class GwyBrick
    {
        public static final String GWY_BRICK_NAME = "GwyBrick";

        private static final String X_RES_COMPONENT = "xres"; //Horizontal size in pixels.
        private static final String Y_RES_COMPONENT = "yres"; //Vertical size in pixels.
        private static final String Z_RES_COMPONENT = "zres"; //Depth (number of levels) in pixels.
        private static final String X_REAL_COMPONENT = "xreal"; //Horizontal dimension in physical units.
        private static final String Y_REAL_COMPONENT = "yreal"; //Vertical dimension in physical units.
        private static final String Z_REAL_COMPONENT = "zreal"; //Depthwise dimension in physical units.
        private static final String X_OFF_COMPONENT = "xoff";//Horizontal offset of the top-left corner in physical units. It usually occurs only if non-zero.
        private static final String Y_OFF_COMPONENT = "yoff";//Vertical offset of the top-left corner in physical units. It usually occurs only if non-zero.
        private static final String Z_OFF_COMPONENT = "zoff";//Vertical offset of the top-left corner in physical units. It usually occurs only if non-zero.
        private static final String X_UNIT_COMPONENT = "si_unit_x";//Unit of horizontal lateral dimensions.
        private static final String Y_UNIT_COMPONENT = "si_unit_y";//Unit of vertical lateral dimensions.
        private static final String Z_UNIT_COMPONENT = "si_unit_z";//Unit of depthwise dimensions.
        private static final String W_UNIT_COMPONENT = "si_unit_w";//Unit of data values.
        private static final String DATA_COMPONENT = "data";//Field data, stored as a flat array of size xres×yres×zres, from the zeroth to the last plane, top to bottom and from left to right.
        private static final String CALIBRATION_COMPONENT = "calibration";//Calibration of the z axis to represent non-linear sampling in this dimension. The number of points must be equal to zres. This component is present only if non-linear sampling is used.

        private static final double TOLERANCE = 1e-10;

        private int xRes = -1;
        private int yRes = -1;
        private int zRes = -1;

        private double xReal = -1;
        private double yReal = -1;
        private double zReal = -1;

        private double xOffset;
        private double yOffset;
        private double zOffset;

        private PrefixedUnit xUnit;
        private PrefixedUnit yUnit;
        private PrefixedUnit zUnit;
        private PrefixedUnit wUnit;

        private GwyDataLine calibration;
        private double[][][] volumeData;

        private GwyBrick(){};

        //we check whether this and other can be two branches from the same force - volume recording
        //it is possible for their ramps to be off (slightly) different lengths, so we just check xRes nad yRes
        //we also assume that the a not well defined volume recording brick is never consistent with any other brick
        public boolean isConsistentWithOtherVolumeRecording(GwyBrick that)
        {
            if(that == null)
            {
                return true;
            }

            if(!this.isWellSpecifiedVolumeRecording() || !that.isWellSpecifiedVolumeRecording())
            {
                return false;
            }

            return this.getDomainGrid().isEqualUpToPrefixes(that.getDomainGrid());
        }

        public boolean isWellSpecified() 
        {
            boolean wellSpecified = (xRes > 0) && (yRes > 0) && (zRes > 0) && (xReal > 0) && (yReal > 0) && (zReal > 0) && (volumeData != null) && (xUnit != null) && (yUnit != null) && (zUnit != null) && (wUnit != null);                     
            return wellSpecified;
        }

        public boolean isWellSpecifiedVolumeRecording()
        {
            boolean wellSpecified = isWellSpecified();
            wellSpecified = wellSpecified && StandardQuantityTypes.LENGTH.isCompatible(xUnit) && StandardQuantityTypes.LENGTH.isCompatible(yUnit) && StandardQuantityTypes.LENGTH.isCompatible(zUnit);          
            wellSpecified = wellSpecified && CalibrationState.isCompatibleWithAnyCalibrationState(wUnit);

            return wellSpecified;
        }

        public Grid2D getDomainGrid()
        {    
            if(!isWellSpecifiedVolumeRecording())
            {
                return null;
            }

            Quantity xQuantity = Quantities.DISTANCE_MICRONS;
            Quantity yQuantity = Quantities.DISTANCE_MICRONS;

            double factorX = xUnit.getConversionFactorTo(xQuantity.getUnit());
            double factorY = yUnit.getConversionFactorTo(xQuantity.getUnit());

            double incrementX = (xRes > 1) ? factorX*xReal/(xRes - 1) : 1;
            double incrementY = (yRes > 1) ? factorY*yReal/(yRes - 1) : 1;

            Grid2D grid = new Grid2D(incrementX, incrementY, 0, 0, yRes, xRes, xQuantity, yQuantity);

            return grid;
        }

        private Grid1D buildZGrid(boolean isWithdraw)
        {
            if(calibration != null)
            {
                return null;
            }

            Quantity distanceQuantity = Quantities.DISTANCE_MICRONS;
            double zFactor = zUnit.getConversionFactorTo(distanceQuantity.getUnit());

            double increment = (isWithdraw ? 1: -1)*zFactor*zReal/(zRes - 1.);
            double origin = isWithdraw ? zFactor*zOffset - (zRes - 1.)*increment  : zFactor*zOffset;

            Grid1D grid = new Grid1D(increment, origin, zRes, distanceQuantity);

            return grid;
        }

        //we assume that all curves in the map are collected in the same orientation, i.e. either the distance values grows when we get closer to the sample or it decrease, for all curves
        //technically, it would be enought to check orientation for a single curve, however, the orientation may be incorrectly determined on a single curve
        //due to outliers (say electrical spikes), so we check it for five curves (on the four corners and in the middle of the map) and then look which orientation was most common
        private boolean shouldGridDataBeReversed( boolean isWithdraw)
        {           
            int reverseScore = 0;
            int[][] indices = new int[][] {{0,0}, {0, xRes - 1}, {yRes - 1, 0}, {yRes - 1, xRes - 1}, {(yRes - 1)/2, (xRes - 1)/2}};

            for(int[] indexPair : indices)
            {
                double[] curveYs = getCurveData(indexPair[0], indexPair[1]);
                boolean reverseCurve = shouldGridDataBeReversed(curveYs, isWithdraw);
                reverseScore = reverseScore + MathUtilities.boole(reverseCurve);
            }

            boolean reverseAll = reverseScore > (indices.length/2);

            return reverseAll;
        }

        //we can safely reverse grid data, because z-domain is the same for all grid curves (i.e. if calibration is null)
        private boolean shouldGridDataBeReversed(double[] curveYs, boolean isWithdraw)
        {
            boolean reverse = (curveYs[0] < curveYs[curveYs.length - 1] && isWithdraw) || (curveYs[0] > curveYs[curveYs.length - 1] && !isWithdraw);
            return reverse;
        }

        public double[] getCurveData(int row, int column)
        {
            double[] curveYs = new double[zRes];

            for(int i = 0; i < zRes;i++)
            {
                curveYs[i] = volumeData[i][row][column];
            }

            return curveYs;
        }

        private Channel1DData[][] getCurveChannels(boolean isWithdraw)
        {
            if(calibration == null)
            {
                return getCurveGridChannels(isWithdraw);
            }

            return null;
        }

        //this assumes that the curve points are equispaced, i.e. the field calibration is null
        private GridChannel1DData[][] getCurveGridChannels(boolean isWithdraw)
        {
            Quantity wQuantity = CalibrationState.getDefaultYQuantity(wUnit);
            double wFactor = wUnit.getConversionFactorTo(wQuantity.getUnit());

            Grid1D grid = buildZGrid(isWithdraw);
            boolean reverseData = shouldGridDataBeReversed(isWithdraw);

            GridChannel1DData[][] gridChannels = new GridChannel1DData[yRes][xRes];

            double[][][] curves = new double[yRes][xRes][zRes];

            if(reverseData)
            {
                for(int i = 0; i < zRes;i++)
                {
                    double[][] plane = volumeData[i];

                    for(int j = 0; j < yRes;j++)
                    {
                        double[] row = plane[j];

                        for(int k = 0; k < xRes; k++)
                        {
                            curves[j][k][zRes - 1 - i] = wFactor*row[k];
                        }
                    }
                }
            }
            else
            {
                for(int i = 0; i < zRes;i++)
                {
                    double[][] plane = volumeData[i];

                    for(int j = 0; j < yRes;j++)
                    {
                        double[] row = plane[j];

                        for(int k = 0; k < xRes; k++)
                        {
                            curves[j][k][i] = wFactor*row[k];
                        }
                    }
                } 
            }

            for(int j = 0; j < yRes;j++)
            {
                double[][] curveRow = curves[j];
                GridChannel1DData[] channelRow = gridChannels[j];

                for(int k = 0; k < xRes; k++)
                {
                    channelRow[k] = new GridChannel1DData(curveRow[k], grid, wQuantity);
                }
            }
            return gridChannels;
        }

        public Quantity getCurveValueQuantity()
        {
            return CalibrationState.getDefaultYQuantity(wUnit);
        }

        public PhotodiodeSignalType getPhotodiodeSignalType()
        {
            return PhotodiodeSignalType.getSignalType(wUnit, PhotodiodeSignalType.VOLTAGE);
        }

        private static GwyBrick readInObjectExceptForName(ByteBuffer byteBuffer) throws UserCommunicableException
        {
            GwyBrick dataField = new GwyBrick();

            long dataSize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
            int finalPosition = byteBuffer.position() + (int)dataSize;

            int dataPosition = -1;
            long dataArraySize = -1;

            while(byteBuffer.position() < finalPosition)
            {
                String componentNameTrimmed = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
                char componentTypeChar = (char)byteBuffer.get();

                if(X_RES_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.INTEGER_32_BIT.getTypeChar() == componentTypeChar)
                {
                    dataField.xRes = byteBuffer.getInt();                   
                }
                else if(Y_RES_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.INTEGER_32_BIT.getTypeChar() == componentTypeChar)
                {
                    dataField.yRes = byteBuffer.getInt();
                }
                else if(Z_RES_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.INTEGER_32_BIT.getTypeChar() == componentTypeChar)
                {
                    dataField.zRes = byteBuffer.getInt();
                }
                else if(X_REAL_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.xReal = byteBuffer.getDouble();
                }
                else if(Y_REAL_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.yReal = byteBuffer.getDouble();
                }
                else if(Z_REAL_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.zReal = byteBuffer.getDouble();
                }
                else if(X_OFF_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.xOffset = byteBuffer.getDouble();
                }
                else if(Y_OFF_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.yOffset = byteBuffer.getDouble();
                }
                else if(Z_OFF_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    dataField.zOffset = byteBuffer.getDouble();
                }
                else if(X_UNIT_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    dataField.xUnit = gwyUnit.getUnit();
                }
                else if(Y_UNIT_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {                                        
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    dataField.yUnit = gwyUnit.getUnit();
                }
                else if(Z_UNIT_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {                                        
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    dataField.zUnit = gwyUnit.getUnit();
                }
                else if(W_UNIT_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {                                        
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    dataField.wUnit = gwyUnit.getUnit();
                }
                else if(CALIBRATION_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {                                        
                    dataField.calibration = GwyDataLine.readInObject(byteBuffer);
                }
                else if(DATA_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeArray.DOUBLE_ARRAY.getTypeChar() == componentTypeChar)
                {
                    //we delay reading the data in case xres, yres and zres components appear after data component
                    dataArraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
                    dataPosition = byteBuffer.position();
                    byteBuffer.position(dataPosition + (int)(GwyDataTypeArray.DOUBLE_ARRAY.getItemSize()*dataArraySize));
                }
                else
                {
                    GwyDataType.getDataType(componentTypeChar).skipData(byteBuffer);
                }
            }

            if(dataPosition > -1 && dataArraySize >= (dataField.xRes * dataField.yRes *dataField.zRes))//these should be equal really, but we are lenient
            {
                byteBuffer.position(dataPosition);
                //first dimension probably should be reversed
                //                dataField.data = DoubleArrayReaderType.FLOAT64.readIn3DArrayRowByRow(dataField.zRes, dataField.yRes, dataField.xRes, 1, byteBuffer);

                double[][][] data = new double[dataField.zRes][][];

                for(int i = 0; i<dataField.zRes; i++)
                {
                    data[i] = DoubleArrayReaderType.FLOAT64.readIn2DArrayRowByRowReversed(dataField.yRes, dataField.xRes, 1, byteBuffer);
                }

                dataField.volumeData = data;
            }

            byteBuffer.position(finalPosition);

            return dataField;          
        }
    }

    private static class GwyDataLine
    {
        public static final String GWY_DATA_LINE_NAME = "GwyDataLine";

        private static final String RES_COMPONENT = "res"; //Number of data points.
        private static final String REAL_COMPONENT = "real"; //Length in physical units.
        private static final String OFF_COMPONENT = "off";//Offset of the beginning in physical units. It usually occurs only if non-zero.
        private static final String X_UNIT_COMPONENT = "si_unit_x";
        private static final String Y_UNIT_COMPONENT = "si_unit_y";
        private static final String DATA_COMPONENT = "data";

        private int xRes = -1;
        private double xReal = -1;
        private double offset;
        private PrefixedUnit xUnit;
        private PrefixedUnit yUnit;
        private double[] data;

        private GwyDataLine(){};

        public boolean isWellSpecified() 
        {
            boolean wellSpecified = (data != null) && (data.length == xRes) && (xUnit != null) && (yUnit != null);                     
            return wellSpecified;
        }

        public boolean isWellSpecifiedForceVolumeCalibration()
        {
            boolean wellSpecified = isWellSpecified();
            return wellSpecified;
        }

        private static GwyDataLine readInObject(ByteBuffer byteBuffer) throws UserCommunicableException
        {
            FileInputUtilities.readInNullTerminatedString(byteBuffer); //reads in object name
            GwyDataLine dataLine = readInObjectExceptForName(byteBuffer);
            return dataLine;
        }

        private static GwyDataLine readInObjectExceptForName(ByteBuffer byteBuffer) throws UserCommunicableException
        {
            GwyDataLine curveModel = new GwyDataLine();

            long dataSize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
            int finalPosition = byteBuffer.position() + (int)dataSize;

            while(byteBuffer.position() < finalPosition)
            {
                String componentNameTrimmed = FileInputUtilities.readInNullTerminatedString(byteBuffer).trim();
                char componentTypeChar = (char)byteBuffer.get();

                if(RES_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.INTEGER_32_BIT.getTypeChar() == componentTypeChar)
                {
                    curveModel.xRes = byteBuffer.getInt();
                }
                else if(REAL_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    curveModel.xReal = byteBuffer.getDouble();
                }
                else if(OFF_COMPONENT.equals(componentNameTrimmed) && GwyDataTypeSimple.DOUBLE.getTypeChar() == componentTypeChar)
                {
                    curveModel.offset = byteBuffer.getDouble();
                }
                else if(X_UNIT_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    curveModel.xUnit = gwyUnit.getUnit();
                }
                else if(Y_UNIT_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeSimple.OBJECT.getTypeChar() == componentTypeChar)
                {
                    GwySIUnit gwyUnit = GwySIUnit.readInObject(byteBuffer);
                    curveModel.yUnit = gwyUnit.getUnit();
                }
                else if(DATA_COMPONENT.equals(componentNameTrimmed)  && GwyDataTypeArray.DOUBLE_ARRAY.getTypeChar() == componentTypeChar)
                {
                    long yDataArraySize = FileInputUtilities.getUnsigned(byteBuffer.getInt());
                    curveModel.data = DoubleArrayReaderType.FLOAT64.readIn1DArray((int)yDataArraySize, 1, byteBuffer);
                }
                else
                {
                    GwyDataType.getDataType(componentTypeChar).skipData(byteBuffer);
                }
            }

            byteBuffer.position(finalPosition);

            return curveModel;
        }
    }
}

