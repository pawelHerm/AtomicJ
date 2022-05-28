package atomicJ.readers.regularImage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import atomicJ.data.Coordinate4D;
import atomicJ.data.units.UnitExpression;
import atomicJ.readers.ArrayStorageType;
import atomicJ.readers.DataStorageDirection;
import atomicJ.readers.DoubleArrayReaderType;

public class WrappedBytesChannelProvider implements ChannelProvider
{
    private final List<DoubleArrayReaderType> sampleReaders;
    private final ByteOrder byteOrder;

    private final int channelCount;
    private final int channelsPerPlane;
    private final boolean interleaved;
    private final int rowCount;
    private final int columnCount;

    private final byte[][] planeBytes;

    private final String colorSpaceName;
    private final List<String> channelNames;

    private final UnitExpression xLength;
    private final UnitExpression yLength;

    private final PlaneSetMetadata planeMetadata;
    private final Channel2DSourceMetadata imageMetadata;

    public WrappedBytesChannelProvider(byte[] planeBytes, ByteOrder byteOrder, boolean interleaved, List<DoubleArrayReaderType> sampleReaders, 
            String colorSpaceName, List<String> channelNames, int rowCount, int columnCount, UnitExpression xLength, UnitExpression yLength)
    {
        this(new byte[][] {planeBytes}, sampleReaders.size(), byteOrder, interleaved, sampleReaders, colorSpaceName, channelNames, PlaneSetMetadata.getNullInstance(), rowCount, columnCount, xLength, yLength, DummyDensityMetadata.getInstance());
    }

    public WrappedBytesChannelProvider(byte[][] planeBytes, int channelsPerPlane, ByteOrder byteOrder, boolean interleaved, List<DoubleArrayReaderType> sampleReaders, 
            String colorSpaceName, List<String> channelNames, PlaneSetMetadata planeMetadata, int rowCount, int columnCount, UnitExpression xLength, UnitExpression yLength, Channel2DSourceMetadata imageMetadata)
    {
        this.planeBytes = planeBytes;
        this.rowCount = rowCount;
        this.columnCount = columnCount;

        this.sampleReaders = sampleReaders;
        this.byteOrder = byteOrder;
        this.channelCount = sampleReaders.size();
        this.channelsPerPlane = channelsPerPlane;
        this.interleaved = interleaved;

        this.colorSpaceName = colorSpaceName;
        this.channelNames = channelNames;

        this.planeMetadata = planeMetadata;

        this.xLength = xLength;
        this.yLength = yLength;

        this.imageMetadata = imageMetadata;
    }     

    @Override
    public Channel2DSourceMetadata getImageMetadata()
    {
        return imageMetadata;
    }

    @Override
    public Coordinate4D getChannelCoordinates(int channelIndex)
    {
        int planeIndex = channelIndex/channelsPerPlane;
        return planeMetadata.getCoordinate(planeIndex);
    }

    @Override
    public Coordinate4D getCombinedChannelCoordinates()
    {
        return planeMetadata.getCombinedCoordinate();
    }

    @Override
    public UnitExpression getXLength()
    {
        return xLength;
    }

    @Override
    public UnitExpression getYLength()
    {
        return yLength;
    }

    //openBytes(no, 0, 0, w, h)
    @Override
    public double[][][] getChannelData(ImageInterpretationModel model)        
    {      
        double[][][] channels = new double[channelCount][][];

        for(int planeIndex = 0, currentChannelIndex = 0; planeIndex < planeBytes.length; planeIndex++)
        {   
            ByteBuffer buffer = ByteBuffer.wrap(planeBytes[planeIndex]).order(byteOrder);
            List<DoubleArrayReaderType> planeSampleReaders = sampleReaders.subList(planeIndex*channelsPerPlane, (planeIndex + 1)*channelsPerPlane);
            if(interleaved)
            {
                for(int i = 0; i < channelsPerPlane; i++)
                {
                    List<DoubleArrayReaderType> precedingSamplesReaders = planeSampleReaders.subList(0, i);
                    List<DoubleArrayReaderType> followingSamplesReaders = planeSampleReaders.subList(i + 1, channelsPerPlane);

                    int readFrom = DoubleArrayReaderType.countBytes(precedingSamplesReaders);
                    int skipBytesStep = DoubleArrayReaderType.countBytes(followingSamplesReaders);

                    buffer.position(readFrom);

                    DoubleArrayReaderType reader = planeSampleReaders.get(i);
                    channels[currentChannelIndex++] = ArrayStorageType.ROW_BY_ROW.readIn2DArray(reader,DataStorageDirection.REVERSED, DataStorageDirection.STRAIGHT, skipBytesStep, rowCount, columnCount, 1, 0, buffer);
                }
            }
            else
            {
                for(int i = 0; i < channelsPerPlane; i++)
                {
                    DoubleArrayReaderType reader = planeSampleReaders.get(i);

                    channels[currentChannelIndex++] = ArrayStorageType.ROW_BY_ROW.readIn2DArray(reader,DataStorageDirection.REVERSED, DataStorageDirection.STRAIGHT, rowCount, columnCount, 1, 0, buffer);               
                }
            }
        }


        if(model.isCombineChannels())
        {
            double[][] channelCombined = new double[rowCount][columnCount];
            double[] coefficients = model.getCombinationCoefficients();

            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    double val = 0;

                    for(int k = 0; k<channelCount;k++)
                    {
                        val += coefficients[k]*channels[k][i][j];
                    }

                    channelCombined[i][j] = val;
                }
            }

            return new double[][][] {channelCombined};

        }

        else return channels;
    }

    @Override
    public int getRowCount()
    {
        return rowCount;
    }

    @Override
    public int getColumnCount()
    {
        return columnCount;
    }

    @Override
    public String getColorSpaceName() 
    {
        return colorSpaceName;
    }

    @Override
    public List<String> getChannelNames()
    { 
        return channelNames;
    }     
}