package atomicJ.readers.mdt;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.sources.SimpleSpectroscopySource;

public class ScanFrame implements MDAFrame
{
    private final int frameIndex;

    private final ExtFrameHeader extHeader;
    private final MDAHeader mdaHeader;

    public ScanFrame(int frameIndex, ExtFrameHeader extHeader, MDAHeader mdaHeader)
    {
        this.frameIndex = frameIndex;
        this.extHeader = extHeader;
        this.mdaHeader = mdaHeader;
    }

    @Override
    public List<ImageSource> readInImageSources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter) throws UserCommunicableException 
    {
        List<MDACalibration> measurands = mdaHeader.getMeasurandCalibrations();
        int dataSize = extHeader.getDataSize();

        Grid2D grid = MDAFrameGeneralReader.buildGrid(getXCalibration(), getYCalibration());

        int rowCount = grid.getRowCount();
        int columnCount = grid.getColumnCount();

        ByteBuffer dataBuffer = FileInputUtilities.readBytesToBuffer(channel, dataSize, ByteOrder.LITTLE_ENDIAN);

        List<ImageChannel> imageChannels = new ArrayList<>();

        int byteReadIn = 0;
        for(MDACalibration measurand : measurands)
        {
            DoubleArrayReaderType readerType = measurand.getDataType().getArrayReaderType();

            double scale = measurand.getScale();
            double offset = measurand.getBias();

            dataBuffer.position(byteReadIn);

            String identifier = measurand.getName();
            Quantity zQuantity = new UnitQuantity(identifier, measurand.getUnit());

            if(filter.accepts(identifier, zQuantity))
            {
                double[][] imageData = readerType.readIn2DArrayRowByRow(rowCount, columnCount, scale, offset, dataBuffer);

                ImageChannel imageChannel = new ImageChannel(imageData, grid, zQuantity, identifier, false);
                imageChannels.add(imageChannel);
            }

            byteReadIn = byteReadIn + rowCount*columnCount*readerType.getByteSize();

            dataBuffer.rewind();
        }

        ImageSource sourceFile = new StandardImageSource(f);
        sourceFile.setChannels(imageChannels);

        List<ImageSource> sources = new ArrayList<>();
        sources.add(sourceFile);

        return sources;
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter)
            throws UserCommunicableException 
    {
        return Collections.emptyList();
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
}