package atomicJ.readers.mdt;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import atomicJ.data.ChannelFilter;
import atomicJ.data.Quantities;
import atomicJ.data.Grid2D;
import atomicJ.data.ImageChannel;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitQuantity;
import atomicJ.gui.UserCommunicableException;
import atomicJ.readers.DoubleArrayReaderType;
import atomicJ.readers.ArrayStorageType;
import atomicJ.sources.ImageSource;
import atomicJ.sources.StandardImageSource;
import atomicJ.utilities.FileInputUtilities;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;

public class ScannedDataFrameReader implements MDTFrameReader
{
    public static final int FRAME_TYPE = 0;
    private final ChannelFilter filter;

    public ScannedDataFrameReader(ChannelFilter filter)
    {
        this.filter = filter; 
    }

    @Override
    public int getFrameType()
    {
        return FRAME_TYPE;
    }

    @Override
    public List<ImageSource> readInImages(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {
        List<ImageSource> sources = new ArrayList<>();

        try 
        {
            int initialChannelPosition = (int) channel.position();       
            int bytesInPostHeaderFramePart = frameHeader.getRemainingFrameSize();

            ScannedDataFrameRecord dataFrame = ScannedDataFrameRecord.readIn(channel, frameHeader);       
            FrameMode frameMode = FrameMode.readIn(channel);

            AxisScale xAxisScale = dataFrame.getXAxisScale();
            AxisScale yAxisScale = dataFrame.getYAxisScale();

            Quantity gridLengthQuantity = Quantities.DISTANCE_MICRONS;     

            //we will ignore the x and y axis offsets, as this is wahat probably most users expect
            //i.e. people what the origin in (0,0)
            Grid2D grid = new Grid2D(xAxisScale.getScale().derive(gridLengthQuantity.getUnit()).getValue(),
                    yAxisScale.getScale().derive(gridLengthQuantity.getUnit()).getValue(),
                    0, 0, frameMode.getRowCount(), frameMode.getColumnCount(),
                    gridLengthQuantity, gridLengthQuantity);


            ////// READS IN IMAGE DATA //////////////

            AxisScale zAxisScale = dataFrame.getZAxisScale();
            double zScaleValue = zAxisScale.getScale().getValue();
            double zOffsetValue = zAxisScale.getOffset().getValue();
            PrefixedUnit zAxisUnit = zAxisScale.getUnit();

            int rowCount = frameMode.getRowCount();
            int columnCount = frameMode.getColumnCount();

            ByteBuffer dataBuffer = FileInputUtilities.readBytesToBuffer(channel, 2*frameMode.getPixelCount(), ByteOrder.LITTLE_ENDIAN);

            double[][] imageData = ArrayStorageType.ROW_BY_ROW.readIn2DArray(DoubleArrayReaderType.INT16,
                    rowCount, columnCount, zScaleValue,zOffsetValue, dataBuffer);

            //reads in title
            int titleSize = (bytesInPostHeaderFramePart - (channel.position() - initialChannelPosition) >= 4) 
                    ? FileInputUtilities.readBytesToBuffer(channel, 4, ByteOrder.LITTLE_ENDIAN).getInt() : 0;
                    String title = titleSize > 0 && (bytesInPostHeaderFramePart - (channel.position() - initialChannelPosition) >= titleSize) ?
                            FileInputUtilities.readInStringFromBytes(titleSize, FileInputUtilities.readBytesToBuffer(channel, titleSize, ByteOrder.LITTLE_ENDIAN)) : "";

                            //reads in XML comment

                            int commentSize = (bytesInPostHeaderFramePart - (channel.position() - initialChannelPosition) >= 4) ? FileInputUtilities.readBytesToBuffer(channel, 4, ByteOrder.LITTLE_ENDIAN).getInt() : 0;

                            Document comment = null;                    
                            try 
                            {
                                comment = commentSize > 0 && (bytesInPostHeaderFramePart - (channel.position() - initialChannelPosition) >= commentSize) ?
                                        FileInputUtilities.readInXMLDocument2(commentSize, FileInputUtilities.readBytesToBuffer(channel, commentSize, ByteOrder.LITTLE_ENDIAN)) : null;

                                        if(comment != null)
                                        {
                                            FileInputUtilities.printChildNodes(comment.getDocumentElement());
                                        }
                            } catch (ParserConfigurationException | SAXException e) {
                                e.printStackTrace();
                            }

                            String identifier = dataFrame.getScanVariables().getChannel().getName();
                            Quantity zQuantity = new UnitQuantity(identifier, zAxisUnit);

                            if(filter.accepts(identifier, zQuantity))
                            {
                                ImageChannel imageChannel = new ImageChannel(imageData, grid, zQuantity, identifier, false);
                                List<ImageChannel> imageChannels = new ArrayList<>();
                                imageChannels.add(imageChannel);

                                ImageSource sourceFile = new StandardImageSource(f);
                                sourceFile.setChannels(imageChannels);
                                sources.add(sourceFile);
                            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sources;
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(File f,
            FileChannel channel, int frameIndex, MDTFrameHeader frameHeader)
                    throws UserCommunicableException
    {
        return Collections.emptyList();
    }

    @Override
    public List<ChannelSource> readInAllSources(File f,
            FileChannel channel, int frameIndex, MDTFrameHeader frameHeader)
                    throws UserCommunicableException 
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
}