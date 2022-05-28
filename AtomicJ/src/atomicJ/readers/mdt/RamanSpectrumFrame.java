package atomicJ.readers.mdt;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;

import atomicJ.data.ChannelFilter;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.utilities.FileInputUtilities;

public class RamanSpectrumFrame implements MDAFrame
{        
    private final int frameIndex;

    private final ExtFrameHeader extHeader;
    private final MDAHeader mdaHeader;

    public RamanSpectrumFrame(int frameIndex, ExtFrameHeader extHeader, MDAHeader mdaHeader)
    {
        this.frameIndex = frameIndex;
        this.extHeader = extHeader;
        this.mdaHeader = mdaHeader;
    }

    @Override
    public List<ImageSource> readInImageSources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter) throws UserCommunicableException
    {                        
        int dataLength = getDataLength();
        ByteBuffer dataBuffer = FileInputUtilities.readBytesToBuffer(channel, getDataLengthInBytes(), ByteOrder.LITTLE_ENDIAN);

        double xScale = getXCalibration().getScale();

        MDADataType xDataType = getXDataType();
        MDADataType yDataType = getYDataType();

        double[] xValues = xDataType.readIn1DArray(dataLength, yDataType.getByteSize(),
                xScale, dataBuffer);

        dataBuffer.position(xDataType.getByteSize());
        double yScale = getYCalibration().getScale();
        double[] yValues = yDataType.readIn1DArray(dataLength, xDataType.getByteSize(), yScale, dataBuffer);

        double[][] val = new double[dataLength][2];
        for(int i = 0; i<dataLength; i++)
        {
            val[i][0] = xValues[i];
            val[i][1] = yValues[i];
        }

        return Collections.emptyList();
    }

    private MDACalibration getXCalibration()
    {
        if(mdaHeader.getDimensionCalibrations().isEmpty())
        {
            return mdaHeader.getMeasurandCalibrations().get(0);
        }
        else
        {
            return mdaHeader.getDimensionCalibrations().get(0);
        }
    }

    private MDACalibration getYCalibration()
    {
        if(mdaHeader.getDimensionCalibrations().isEmpty())
        {
            return mdaHeader.getMeasurandCalibrations().get(0);
        }
        else 
        {
            return mdaHeader.getMeasurandCalibrations().get(1);          
        }
    }

    private int getDataLength()
    {
        long xCalLength = getXCalibration().getArrayElementCount();

        int length = xCalLength > 0 ? (int) xCalLength : (int)mdaHeader.getMeasurandVectorCount();
        return length;
    }

    private MDADataType getXDataType()
    {
        return getXCalibration().getDataType();
    }


    private int getXDataLengthInBytes()
    {
        int dataLength = getDataLength();
        int bytesPerItem = getXDataType().getByteSize();

        int byteSize = dataLength*bytesPerItem;

        return byteSize;
    }

    private int getDataLengthInBytes()
    {
        int dataLengthInBytes = getXDataLengthInBytes() + getYDataLengthInBytes();
        return dataLengthInBytes;
    }

    private MDADataType getYDataType()
    {
        return getYCalibration().getDataType();
    }

    private int getYDataLengthInBytes()
    {
        int dataLength = getDataLength();
        int bytesPerItem = getYDataType().getByteSize();

        int byteSize = dataLength*bytesPerItem;

        return byteSize;
    }

    private String getDataName()
    {
        String extHeaderName = extHeader.getName();
        String name = !extHeaderName.isEmpty() ? extHeaderName : "Raman spectrum from frame " + Integer.toString(frameIndex);
        return name;
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter) throws UserCommunicableException
    {
        return Collections.emptyList();
    }
}