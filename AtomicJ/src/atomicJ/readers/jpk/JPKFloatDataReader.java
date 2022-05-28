package atomicJ.readers.jpk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import atomicJ.gui.UserCommunicableException;

public class JPKFloatDataReader 
{
    public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints) throws UserCommunicableException
    {
        ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints, 4);                

        double[] data = new double[numPoints]; 

        for(int i = 0; i<numPoints; i++)
        {
            double readIn = buffer.getFloat();
            data[i] = readIn;
        }
        return data;
    }

    public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount) throws UserCommunicableException 
    {
        double[][] data = new double[rowCount][columnCount];
        for(int i = 0; i<rowCount; i++)
        {
            for(int j = 0; j<columnCount; j++)
            {
                double readIn = flippedBuffer.getFloat();
                data[i][j] = readIn;
            }
        }    
        return data;
    }

    protected ByteBuffer readInToByteBuffer(ZipFile zipFile, ZipEntry dataEntry, int numPoints, int byteCount) throws UserCommunicableException
    {            
        int bufferSize = numPoints*byteCount;

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);// buffer to hold data
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        try(ReadableByteChannel channel = Channels.newChannel(zipFile.getInputStream(dataEntry)))
        {
            if(channel.read(byteBuffer) == -1) 
            { 
                throw new UserCommunicableException("Error occured while reading the file");
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        byteBuffer.flip();

        return byteBuffer;
    }
}
