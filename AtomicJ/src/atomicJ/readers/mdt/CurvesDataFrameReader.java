package atomicJ.readers.mdt;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.analysis.PhotodiodeSignalType;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelFilter;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.Grid1D;
import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel1DData;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.Units;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.sources.CalibrationState;
import atomicJ.sources.FlexibleMapSource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapGridSource;
import atomicJ.sources.MapSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.StandardSimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.ArrayUtilities;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.utilities.IOUtilities;

public class CurvesDataFrameReader implements MDTFrameReader
{
    /*
     * PROBLEMS: In the structure CurvesCoord_201 (p.10), in the section describing Curves Data Frame (h_what = 201)
     * the fields ForwSize and 
     */

    public static final int FRAME_TYPE = 201;

    private final ChannelFilter filter;

    public CurvesDataFrameReader(ChannelFilter filter)
    {
        this.filter = filter; 
    }

    @Override
    public int getFrameType()
    {
        return FRAME_TYPE;
    }

    @Override
    public List<ChannelSource> readInAllSources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {        
        List<ChannelSource> sources = new ArrayList<>();

        try {
            long initPosition = channel.position();

            sources.addAll(readInSpectroscopySources(f, channel, frameIndex, frameHeader));

            channel.position(initPosition);
            sources.addAll(readInImages(f, channel, frameIndex, frameHeader));

        } 
        catch (IOException e) {
            e.printStackTrace();
            throw new UserCommunicableException("Error occured while reading a file",e);
        }

        return sources;
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {                
        SpectroscopyDataFrameRecord dataFrame = SpectroscopyDataFrameRecord.readIn(channel, frameHeader);
        FrameMode mode = FrameMode.readIn(channel);

        Quantity rampQuantity = Quantities.DISTANCE_MICRONS;
        AxisScale rampAxisScale = dataFrame.getXAxisScale();
        PrefixedUnit rampAxisUnit = rampAxisScale.getUnit();
        double rampFactor = rampAxisUnit.getConversionFactorTo(rampQuantity.getUnit());

        double rampScaleValue = rampFactor*rampAxisScale.getScale().getValue();
        double rampOffsetValue = rampFactor*rampAxisScale.getOffset().getValue();

        AxisScale deflAxisScale = dataFrame.getZAxisScale();
        PrefixedUnit deflAxisUnit = deflAxisScale.getUnit();
        Quantity deflQuantity = CalibrationState.getDefaultYQuantity(deflAxisUnit);
        double deflFactor = deflAxisUnit.getConversionFactorTo(deflQuantity.getUnit());

        double deflScaleValue = deflAxisScale.getScale().getValue();
        double deflOffsetValue = deflAxisScale.getOffset().getValue();

        CurvesCoordHeader curvesCoordHeader = CurvesCoordHeader.readIn(channel);
        double pointCoordsFactor = curvesCoordHeader.getUnit().derive(Units.MICRO_METER_UNIT).getValue();

        int curveLength = mode.getColumnCount();
        int dotCount = mode.getDotCount();

        //I think that Gwyddion reads in the h_what = 201 frames incorrectly
        //the following code is used both for frames h_what = 1 (old spectroscopy) and h_what = 201
        /*
         * if (frame->fm_ndots) {
        frame->dots = p;
        p += 14 + frame->fm_ndots * 16;
    }

    if (frame->fm_xres * frame->fm_yres) {
        frame->data = p;
        p += sizeof(gint16)*frame->fm_xres*frame->fm_yres;
    }

         */

        List<CurveCoordinates> curveCoordinatesAll = new ArrayList<>();

        for(int i = 0; i<dotCount; i++)
        {
            curveCoordinatesAll.add(CurveCoordinates.readIn(channel));
        }

        boolean multipleCurves = dotCount > 0;

        List<SimpleSpectroscopySource> sources = new ArrayList<>();
        List<Point2D> curvePositions = new ArrayList<>();

        for(int i = 0; i<dotCount; i++)
        {
            CurveCoordinates curveCoordinates = curveCoordinatesAll.get(i);

            int approachCurveLength = curveCoordinates.getForwardCurveSize();
            int backwardCurveLength = curveCoordinates.getBackwardCurveSize();

            double[] approachCurve = DoubleArrayReaderType.INT16.readIn1DArray(approachCurveLength, deflFactor*deflScaleValue, deflFactor*deflOffsetValue, FileInputUtilities.readBytesToBuffer(channel, approachCurveLength*2, ByteOrder.LITTLE_ENDIAN));
            double[] withdrawCurve = DoubleArrayReaderType.INT16.readIn1DArray(backwardCurveLength, deflFactor*deflScaleValue, deflFactor*deflOffsetValue, FileInputUtilities.readBytesToBuffer(channel, backwardCurveLength*2, ByteOrder.LITTLE_ENDIAN));

            ArrayUtilities.reverseInPlace(approachCurve);


            Grid1D gridApproach = new Grid1D(-rampScaleValue, rampOffsetValue + rampScaleValue*(approachCurveLength - 1), approachCurveLength, rampQuantity);
            Grid1D gridWithdraw = new Grid1D(rampScaleValue, rampOffsetValue, backwardCurveLength, rampQuantity);

            Channel1DData approachChannel = new GridChannel1DData(approachCurve, gridApproach, deflQuantity);
            Channel1DData withdrawChannel = new GridChannel1DData(withdrawCurve, gridWithdraw, deflQuantity);

            Point2D curvePosition = new Point2D.Double(pointCoordsFactor*curveCoordinates.getXPointCoordinate(),
                    pointCoordsFactor*curveCoordinates.getYPointCoordinate());
            curvePositions.add(curvePosition);

            String suffix = multipleCurves ? " (" + Integer.toString(i) + ")": "";

            String longName = f.getAbsolutePath() + suffix;
            String shortName = IOUtilities.getBareName(f) + suffix;

            StandardSimpleSpectroscopySource source = new StandardSimpleSpectroscopySource(f, shortName, longName, approachChannel, withdrawChannel);

            PhotodiodeSignalType photodiodeSignalType = PhotodiodeSignalType.getSignalType(deflQuantity, PhotodiodeSignalType.ELECTRIC_CURRENT);
            source.setPhotodiodeSignalType(photodiodeSignalType);
            source.setRecordingPoint(curvePosition);

            sources.add(source);
        }

        if(dotCount > 10)
        {
            Grid2D grid = Grid2D.getGrid(curvePositions, 1e-1);
            MapSource<?> mapSource = (grid != null) ? new MapGridSource(f, sources, grid): new FlexibleMapSource(f, sources, new ChannelDomainIdentifier(1, ChannelDomainIdentifier.getNewDomainKey()), Quantities.DISTANCE_MICRONS, Quantities.DISTANCE_MICRONS);
        }

        int version = frameHeader.getVersion();
        if(version > 0)
        {
            CurvesDataFrameExtension.readIn(channel);
        }

        return sources;
    }

    @Override
    public List<ImageSource> readInImages(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException
    {
        return Collections.emptyList();
    }

    private static class CurvesCoordHeader
    {
        public static final int BYTE_SIZE = 14;

        private final int headerSize; //Specifies the size of this structure, in bytes
        private final int coordRecordSize; //Specifies the size of CurvesCoord_201 structure, in bytes
        private final int version; //Version of curves structure
        private final int unitCode;
        private final UnitExpression unitExpression;

        private CurvesCoordHeader(ByteBuffer buffer)
        {
            this.headerSize = buffer.getInt();
            this.coordRecordSize = buffer.getInt();
            this.version = buffer.getInt();
            this.unitCode  = buffer.getShort();//Physical units of (X, Y) coordinates. See above about physical unit codes.
            this.unitExpression = UnitCodes.getUnitExpression(unitCode);
        }

        public UnitExpression getUnit()
        {
            return unitExpression;
        }

        public static CurvesCoordHeader readIn(FileChannel channel) throws UserCommunicableException
        {
            ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, BYTE_SIZE, ByteOrder.LITTLE_ENDIAN);
            CurvesCoordHeader header = new CurvesCoordHeader(buffer);

            return header;
        }
    }

    private static class CurveCoordinates
    {
        public static final int BYTE_SIZE = 16;


        private final double x; //4 bytes, x coordinate of the point
        private final double y; //4 bytes, y coordinate of the point
        private final int forwardCurveByteSize; //4 bytes, forward curve size, in bytes
        private final int backwardCurveByteSize; //4 bytes, backward curve size, in bytes

        private CurveCoordinates(ByteBuffer buffer)
        {
            this.x = buffer.getFloat();
            this.y = buffer.getFloat();
            this.forwardCurveByteSize = buffer.getInt();
            this.backwardCurveByteSize = buffer.getInt();
        }

        public static CurveCoordinates readIn(FileChannel channel) throws UserCommunicableException
        {
            ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, BYTE_SIZE, ByteOrder.LITTLE_ENDIAN);
            CurveCoordinates curveCoordinates = new CurveCoordinates(buffer);

            return curveCoordinates;
        }

        public double getXPointCoordinate()
        {
            return x;
        }

        public double getYPointCoordinate()
        {
            return y;
        }


        public int getForwardCurveSize()
        {
            return forwardCurveByteSize;
        }

        public int getBackwardCurveSize()
        {
            return backwardCurveByteSize;
        }
    }
}
