package atomicJ.readers.jpk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import atomicJ.gui.UserCommunicableException;


public enum JPKEncodedDataReader
{
    SIGNED_SHORT("signedshort", 2) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                
            double[] data = new double[numPoints];

            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getShort();
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getShort();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    }, 
    SIGNED_SHORT_LIMITED("signedshort-limited", 2) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getShort();
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getShort();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    }, 

    SIGNED_SHORT_WITH_VALIDITY("SignedShortWithValidity", 2) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getShort();
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getShort();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    }, 
    UNSIGNED_SHORT("unsignedshort", 2) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getShort() & 0xffff;
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getShort() & 0xffff;
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    }, 
    UNSIGNED_SHORT_LIMITED("unsignedshortlimited", 2) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getShort() & 0xffff;
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getShort() & 0xffff;
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    },
    SIGNED_INTEGER("signedinteger", 4) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getInt();
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getInt();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    },
    SIGNED_INTEGER_LIMITED("signedinteger-limited", 4) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getInt();
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getInt();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    },

    SIGNED_INTEGER_WITH_VALIDITY("SignedIntegerWithValidity",4)
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                int readIn = buffer.getInt();
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    int readIn = flippedBuffer.getInt();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    },

    UNSIGNED_INTEGER("unsignedinteger", 4) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter) throws UserCommunicableException 
        {  
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                long readIn = (buffer.getInt() & 0xffffffffL);
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    long readIn = (flippedBuffer.getInt() & 0xffffffffL);
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    }, 
    UNSIGNED_INTEGER_LIMITED("unsignedinteger-limited", 4) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();
            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints];
            for(int i = 0; i<numPoints; i++)
            {
                long readIn = (buffer.getInt() & 0xffffffffL);
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    long readIn = (flippedBuffer.getInt() & 0xffffffffL);
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    },
    SIGNED_LONG("signedlong", 8) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry,int numPoints,  JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();
            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints]; 

            for(int i = 0; i<numPoints; i++)
            {
                long readIn = buffer.getLong();
                data[i] = readIn*multiplier + offset;
            }                
            return data;
        }


        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    long readIn = flippedBuffer.getLong();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    }, 
    SIGNED_LONG_LIMITED("signedlong-limited", 8) 
    {
        @Override
        public double[] readIndData(ZipFile zipFile, ZipEntry dataEntry,
                int numPoints, JPKLinearConverter encodingConverter)
                        throws UserCommunicableException
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();
            ByteBuffer buffer = readInToByteBuffer(zipFile, dataEntry, numPoints);                

            double[] data = new double[numPoints]; 

            for(int i = 0; i<numPoints; i++)
            {
                long readIn = buffer.getLong();
                data[i] = readIn*multiplier + offset;
            }
            return data;
        }

        @Override
        public double[][] readIndData(ByteBuffer flippedBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter)
                throws UserCommunicableException 
        {
            double multiplier = encodingConverter.getMultiplier();
            double offset = encodingConverter.getOffset();

            double[][] data = new double[rowCount][columnCount];
            for(int i = 0; i<rowCount; i++)
            {
                for(int j = 0; j<columnCount; j++)
                {
                    long readIn = flippedBuffer.getLong();
                    data[i][j] = readIn*multiplier + offset;
                }
            }    
            return data;
        }
    };

    private final String key;
    private final int byteCount;

    private JPKEncodedDataReader(String key, int byteCount)
    {
        this.key = key;
        this.byteCount = byteCount;
    }

    public static JPKEncodedDataReader getDataReader(String key) 
    {    
        for (JPKEncodedDataReader reader : values()) 
        {
            if (reader.key.equalsIgnoreCase(key)) 
            {
                return reader;
            }
        }

        throw new IllegalArgumentException("Invalid DataReader value: " + key);
    }

    protected ByteBuffer readInToByteBuffer(ZipFile zipFile, ZipEntry dataEntry, int numPoints) throws UserCommunicableException
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

    public abstract double[] readIndData(ZipFile zipFile, ZipEntry dataEntry, int numPoints, JPKLinearConverter encodingConverter) throws UserCommunicableException;

    public abstract double[][] readIndData(ByteBuffer flippedByteBuffer, int rowCount, int columnCount, JPKLinearConverter encodingConverter) throws UserCommunicableException;

}