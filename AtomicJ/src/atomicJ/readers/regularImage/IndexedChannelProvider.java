package atomicJ.readers.regularImage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import atomicJ.data.Coordinate4D;
import atomicJ.data.units.UnitExpression;
import atomicJ.readers.ArrayStorageType;
import atomicJ.readers.DataStorageDirection;
import atomicJ.readers.IntArrayReaderType;

public class IndexedChannelProvider implements ChannelProvider
{
    private final List<IntArrayReaderType> sampleReaders;
    private final double[][] palette;
    private final ByteOrder byteOrder;
    private final int palletteSize;
    private final boolean interleaved;
    private final int rowCount;
    private final int columnCount;

    private final byte[] imageData;

    private final String colorSpaceName;
    private final List<String> channelNames;

    private final UnitExpression xLength;
    private final UnitExpression yLength;

    private final PlaneSetMetadata planeMetadata;
    private final Channel2DSourceMetadata imageMetadata;

    public IndexedChannelProvider(byte[] imageData, ByteOrder byteOrder, boolean interleaved, List<IntArrayReaderType> sampleReaders, String colorSpaceName, List<String> channelNames, double[][] palette,
            int rowCount, int columnCount, UnitExpression xLength, UnitExpression yLength)
    {
        this(imageData, byteOrder, interleaved, sampleReaders, colorSpaceName, channelNames, PlaneSetMetadata.getNullInstance(), palette, rowCount, columnCount, xLength, yLength, DummyDensityMetadata.getInstance());
    }

    public IndexedChannelProvider(byte[] imageData, ByteOrder byteOrder, boolean interleaved, List<IntArrayReaderType> sampleReaders, String colorSpaceName, List<String> channelNames, PlaneSetMetadata planeMetadata,double[][] palette,
            int rowCount, int columnCount, UnitExpression xLength, UnitExpression yLength, Channel2DSourceMetadata imageMetadata)
    {
        this.imageData = imageData;
        this.palette = palette;
        this.rowCount = rowCount;
        this.columnCount = columnCount;            
        this.sampleReaders = sampleReaders;
        this.byteOrder = byteOrder;
        this.palletteSize = palette.length;
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
        //we assume that all imageData comes from a single plane
        return planeMetadata.getCoordinate(0);
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
        ByteBuffer buffer = ByteBuffer.wrap(imageData).order(byteOrder);

        int channelCount = palletteSize*sampleReaders.size();

        double[][][] channels = new double[channelCount][][];

        if(interleaved)
        {
            for(int r = 0, channelIndex = 0; r <sampleReaders.size(); r++)
            {
                IntArrayReaderType reader = sampleReaders.get(r);

                int bufferInitialPosition = buffer.position();

                List<IntArrayReaderType> precedingSamples = sampleReaders.subList(0, r);
                List<IntArrayReaderType> followingSamples = sampleReaders.subList(r + 1, palletteSize);

                int readFrom = bufferInitialPosition + IntArrayReaderType.countBytes(precedingSamples);
                int skipBytesStep = IntArrayReaderType.countBytes(followingSamples);

                buffer.position(readFrom);

                int[][] indices = ArrayStorageType.ROW_BY_ROW.readIn2DArray(reader,DataStorageDirection.REVERSED, DataStorageDirection.STRAIGHT, skipBytesStep, rowCount, columnCount, buffer);

                for(int i = 0; i < palletteSize; i++)
                {                                                         
                    channels[channelIndex++] = getSamples(palette[i], indices, rowCount, columnCount);
                }
            }

        }
        else
        {
            for(int r = 0, channelIndex = 0; r <sampleReaders.size(); r++)
            {
                IntArrayReaderType reader = sampleReaders.get(r);

                int[][] indices = ArrayStorageType.ROW_BY_ROW.readIn2DArray(reader,DataStorageDirection.REVERSED, DataStorageDirection.STRAIGHT, rowCount, columnCount, buffer);;

                for(int i = 0; i < palletteSize; i++)
                {                       
                    channels[channelIndex++] = getSamples(palette[i], indices, rowCount, columnCount);
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

    private double[][] getSamples(double[] subPalette, int[][] indices, int rowCount, int columnCount)
    {
        double[][] channel = new double[rowCount][columnCount];

        for(int i = 0; i<rowCount;i++)
        {
            for(int j = 0; j<columnCount;j++)
            {
                channel[i][j] = subPalette[indices[i][j]];
            }
        }

        return channel;
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