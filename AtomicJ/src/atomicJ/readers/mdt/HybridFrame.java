package atomicJ.readers.mdt;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.Quantities;
import atomicJ.data.SinusoidalChannel1DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.ArrayStorageType;
import atomicJ.readers.DataStorageDirection;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.FileReadingPack;
import atomicJ.readers.ReadingPack;
import atomicJ.readers.SourceReadingState;
import atomicJ.readers.SourceReadingStateMonitored;
import atomicJ.readers.SourceReadingStateMute;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;

public class HybridFrame implements MDAFrame
{        
    private final int frameIndex;

    private final ExtFrameHeader extHeader;
    private final MDAHeader mdaHeader;
    private final HybridXMLComment xmlComment;

    public HybridFrame(int frameIndex, ExtFrameHeader extHeader, MDAHeader mdaHeader) throws UserCommunicableException
    {    
        this.frameIndex = frameIndex;
        this.extHeader = extHeader;
        this.mdaHeader = mdaHeader;
        this.xmlComment = new HybridXMLComment(extHeader.getComment());
    }

    @Override
    public List<ImageSource> readInImageSources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter) throws UserCommunicableException 
    {
        List<ImageSource> sources = new ArrayList<>();

        MDACalibration xCalibration = getXCalibration();
        MDACalibration yCalibration = getYCalibration();
        MDACalibration zCalibration = getZCalibration();
        MDACalibration hybridCalibration = getHybridCalibration();

        Path externalDataPath = xmlComment.getFullExternalDataPath(f.getParentFile().toPath());

        Grid2D grid = MDAFrameGeneralReader.buildGrid(xCalibration, yCalibration);
        int columnCount = grid.getRowCount();
        int rowCount = grid.getColumnCount();

        List<MDACalibration> measurands = mdaHeader.getMeasurandCalibrations();
        List<DoubleArrayReaderType> measurandArrayReaderTypes = new ArrayList<>();
        int mapCount = measurands.size() - 1;

        for(int i = 0; i < mapCount; i++)
        {
            MDACalibration measurand = measurands.get(i);
            measurandArrayReaderTypes.add(measurand.getDataType().getArrayReaderType());
        }

        DoubleArrayReaderType hybridReaderType = hybridCalibration.getDataType().getArrayReaderType();
        int hybridElementCount = (int) zCalibration.getArrayElementCount();
        int hybridElementSizeInBytes = hybridReaderType.getByteSize()*hybridElementCount;

        HybridScanDirection scanDirection = xmlComment.getScanDirection();
        ArrayStorageType storageType = scanDirection.getStorageType();

        DataStorageDirection betweenVectorDirection = scanDirection.getBetweenVectorsDirection();
        DataStorageDirection insideVectorDirection = scanDirection.getiInsideVectorDirection();

        try(FileChannel dataChannel = (FileChannel)Files.newByteChannel(externalDataPath);) 
        {        
            int dataSizeInBytes = rowCount*columnCount*(DoubleArrayReaderType.countBytes(measurandArrayReaderTypes) + hybridElementSizeInBytes); 

            List<ImageChannel> imageChannels = new ArrayList<>();

            ByteBuffer dataBuffer = FileInputUtilities.readBytesToBuffer(dataChannel, dataSizeInBytes, ByteOrder.LITTLE_ENDIAN);

            for(int i = 0; i<mapCount; i++)
            {
                MDACalibration measurand = measurands.get(i);
                DoubleArrayReaderType readerType = measurand.getDataType().getArrayReaderType();

                double scale = measurand.getScale();
                double offset = measurand.getBias();

                int byteSizeBefore = DoubleArrayReaderType.countBytes(measurandArrayReaderTypes.subList(0, i));
                int byteSizeAfter = DoubleArrayReaderType.countBytes(measurandArrayReaderTypes.subList(i + 1, mapCount)) + hybridElementSizeInBytes;
                int byteSkipStep = byteSizeAfter + byteSizeBefore;

                dataBuffer.position(byteSizeBefore);

                String identifier = measurand.getName();
                Quantity zQuantity = new UnitQuantity(identifier, measurand.getUnit());

                if(filter.accepts(identifier, zQuantity))
                {
                    double[][] mapData = storageType.readIn2DArray(readerType, betweenVectorDirection, insideVectorDirection, byteSkipStep, rowCount, columnCount, scale, offset, dataBuffer);

                    ImageChannel imageChannel = new ImageChannel(mapData, grid, zQuantity, identifier, false);
                    imageChannels.add(imageChannel);
                }

                dataBuffer.rewind();
            }

            ImageSource sourceFile = new StandardImageSource(f);
            sourceFile.setChannels(imageChannels);

            sources.add(sourceFile);
        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter)
            throws UserCommunicableException 
    {
        List<SimpleSpectroscopySource> sources = new ArrayList<>();

        MDACalibration xCalibration = getXCalibration();
        MDACalibration yCalibration = getYCalibration();
        MDACalibration zCalibration = getZCalibration();
        MDACalibration hybridCalibration = getHybridCalibration();

        Path externalDataPath = xmlComment.getFullExternalDataPath(f.getParentFile().toPath());

        Grid2D grid = MDAFrameGeneralReader.buildGrid(xCalibration, yCalibration);
        int columnCount = grid.getRowCount();
        int rowCount = grid.getColumnCount();


        List<MDACalibration> measurands = mdaHeader.getMeasurandCalibrations();
        List<DoubleArrayReaderType> mapMeasurandArrayReaderTypes = new ArrayList<>();
        int mapCount = measurands.size() - 1;

        for(int i = 0; i < mapCount; i++)
        {
            MDACalibration measurand = measurands.get(i);
            mapMeasurandArrayReaderTypes.add(measurand.getDataType().getArrayReaderType());
        }

        DoubleArrayReaderType hybridReaderType = hybridCalibration.getDataType().getArrayReaderType();
        int hybridElementCount = (int) zCalibration.getArrayElementCount();
        int hybridElementSizeInBytes = hybridReaderType.getByteSize()*hybridElementCount;

        int curveCount = rowCount * columnCount;

        HybridScanDirection scanDirection = xmlComment.getScanDirection();
        ArrayStorageType storageType = scanDirection.getStorageType();

        DataStorageDirection betweenVectorDirection = scanDirection.getBetweenVectorsDirection();
        DataStorageDirection insideVectorDirection = scanDirection.getiInsideVectorDirection();

        try(FileChannel dataChannel = (FileChannel)Files.newByteChannel(externalDataPath);) 
        {        
            int dataSizeInBytes = rowCount*columnCount*(DoubleArrayReaderType.countBytes(mapMeasurandArrayReaderTypes) + hybridElementSizeInBytes); 

            ByteBuffer dataBuffer = FileInputUtilities.readBytesToBuffer(dataChannel, dataSizeInBytes, ByteOrder.LITTLE_ENDIAN);

            int byteSkipStep = DoubleArrayReaderType.countBytes(mapMeasurandArrayReaderTypes);

            dataBuffer.position(byteSkipStep);

            String identifier = hybridCalibration.getName();

            if(filter.accepts(identifier, new UnitQuantity(identifier, hybridCalibration.getUnit())))
            {
                double deflScale = hybridCalibration.getScale();
                double deflOffset = hybridCalibration.getBias();
                PrefixedUnit deflUnit = hybridCalibration.getUnit();
                Quantity deflQuantity = CalibrationState.getDefaultYQuantity(deflUnit);
                double deflFactor = deflUnit.getConversionFactorTo(deflQuantity.getUnit());

                double zAmp = xmlComment.getZAmplitudeValue();
                PrefixedUnit zUnit = xmlComment.getZAmplitudeUnit();
                Quantity zQuantity = Quantities.DISTANCE_MICRONS;
                double zFactor = zUnit.getConversionFactorTo(zQuantity.getUnit());
                double zAmpFactorized = zAmp*zFactor;

                double deflScaleFactored = deflFactor*deflScale;
                double deflOffsetFactored = deflFactor*deflOffset;

                int approachLength = hybridElementCount/2;
                int withdrawLength = hybridElementCount - approachLength;

                SourceReadingState state  = curveCount > 10  ? new SourceReadingStateMonitored(curveCount, SourceReadingStateMonitored.FORCE_VOLUME_PROBLEM) :
                    new SourceReadingStateMute(curveCount);

                try{
                    if(ArrayStorageType.ROW_BY_ROW.equals(storageType))
                    {
                        int betweenOffset = betweenVectorDirection.getOffset(rowCount);
                        int betweenStep = betweenVectorDirection.getStep();

                        int innerOffset = insideVectorDirection.getOffset(columnCount);
                        int innerStep = insideVectorDirection.getStep();

                        for(int i = 0; i<rowCount; i++)
                        {
                            int rowIndex = betweenOffset + betweenStep*i;

                            for(int j = 0; j <columnCount; j++)
                            {
                                int columnIndex = innerOffset + innerStep*j;

                                double[] approachData = hybridReaderType.readIn1DArray(approachLength, deflScaleFactored, deflOffsetFactored, dataBuffer);
                                double[] withdrawData = hybridReaderType.readIn1DArray(withdrawLength, deflScaleFactored, deflOffsetFactored, dataBuffer);

                                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + byteSkipStep));

                                state.incrementAbsoluteProgress();

                                if(parentReader.isCancelled())
                                {
                                    state.setOutOfJob();
                                }
                                if(state.isOutOfJob())
                                {
                                    return Collections.emptyList();
                                }

                                sources.add(buildSource(f, grid, rowIndex, columnIndex, zQuantity, deflQuantity, approachData, withdrawData, zAmpFactorized));
                            }
                        }
                    }
                    else
                    {
                        int betweenOffset = betweenVectorDirection.getOffset(columnCount);
                        int betweenStep = betweenVectorDirection.getStep();

                        int innerOffset = insideVectorDirection.getOffset(rowCount);
                        int innerStep = insideVectorDirection.getStep();

                        for(int i = 0; i<columnCount; i++)
                        {
                            int columnIndex = betweenOffset + betweenStep*i;

                            for(int j = 0; j<rowCount; j++)
                            {
                                int rowIndex = innerOffset + innerStep*j;

                                double[] approachData = hybridReaderType.readIn1DArray(approachLength, deflScaleFactored, deflOffsetFactored, dataBuffer);
                                double[] withdrawData = hybridReaderType.readIn1DArray(withdrawLength, deflScaleFactored, deflOffsetFactored, dataBuffer);

                                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + byteSkipStep));

                                state.incrementAbsoluteProgress();

                                if(parentReader.isCancelled())
                                {
                                    state.setOutOfJob();
                                }
                                if(state.isOutOfJob())
                                {
                                    return Collections.emptyList();
                                }

                                sources.add(buildSource(f, grid, rowIndex, columnIndex, zQuantity, deflQuantity, approachData, withdrawData, zAmpFactorized));
                            }
                        }
                    }

                    MapGridSource mapSource = new MapGridSource(f, sources, grid);

                    ReadingPack<ImageSource> readingPack = mapCount > 0 ? new FileReadingPack<>(Collections.singletonList(f), new MDTImageReader()) : null;               
                    mapSource.setMapAreaImageReadingPack(readingPack);
                }
                catch(Exception e)
                {
                    state.setOutOfJob();
                    throw e;
                }


            }
        }
        catch (IOException | RuntimeException e) 
        {
            e.printStackTrace();

            throw new UserCommunicableException("Error occured while reading the file", e);     
        } 

        return sources;
    }

    private SimpleSpectroscopySource buildSource(File f, Grid2D grid, int i, int j, Quantity zQuantity, Quantity deflQuantity, double[] approachYValues, double[] withdrawYValues, double zAmpFactorized)
    {
        int approachLength = approachYValues.length;
        int withdrawLength = withdrawYValues.length;
        int hybridElementCount = approachLength + withdrawLength;

        Channel1DData approachChannel = new SinusoidalChannel1DData(approachYValues, -zAmpFactorized, 2.*Math.PI/hybridElementCount, 0, Math.PI/2., zQuantity, deflQuantity);
        Channel1DData withdrawChannel = new SinusoidalChannel1DData(withdrawYValues, -zAmpFactorized, 2.*Math.PI/hybridElementCount, approachLength, Math.PI/2., zQuantity, deflQuantity);

        Point2D recordingPoint= grid.getPoint(i, j);

        String suffix = " (" + Integer.toString(i) + "," + Integer.toString(j) + ")";

        String longName = f.getAbsolutePath() + suffix;
        String shortName = IOUtilities.getBareName(f) + suffix;

        StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName,approachChannel, withdrawChannel);

        PrefixedUnit deflUnit = deflQuantity.getUnit();
        PhotodiodeSignalType photodiodeSignalType = PhotodiodeSignalType.getSignalType(deflUnit, PhotodiodeSignalType.ELECTRIC_CURRENT);

        source.setPhotodiodeSignalType(photodiodeSignalType);
        source.setRecordingPoint(recordingPoint);

        return source;
    }

    private MDACalibration getXCalibration()
    {
        return mdaHeader.getDimensionCalibrations().get(0);
    }

    private MDACalibration getYCalibration()
    {
        return mdaHeader.getDimensionCalibrations().get(1);
    }

    private MDACalibration getZCalibration()
    {
        return mdaHeader.getDimensionCalibrations().get(2);
    }


    private MDACalibration getHybridCalibration()
    {
        return mdaHeader.getMeasurandCalibrations().get(mdaHeader.getMeasurandElementCount() - 1);
    }
}