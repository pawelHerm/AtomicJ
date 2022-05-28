package atomicJ.readers;

import java.nio.ByteBuffer;
import java.util.Collection;

import atomicJ.utilities.NumberUtilities;

public enum IntArrayReaderType
{

    INT2(0, false)
    {
        @Override
        public int[] readIn1DArray(int length, 
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int currentPosition = 0;
            byte currentByte = 0;

            for(int i = 0; i<length; i++)
            {
                currentByte = currentPosition == 0 ? dataBuffer.get() : currentByte;
                data[i] = (currentByte >> currentPosition) & 1;
                currentPosition = ++currentPosition % 8;
            }

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, 
                ByteBuffer dataBuffer) 
        {           
            int[] data = new int[length];

            int currentPosition = 0;
            byte currentByte = 0;

            for(int i = length - 1; i >= 0; i--)
            {
                currentByte = currentPosition == 0 ? dataBuffer.get() : currentByte;
                data[i] = (currentByte >> currentPosition) & 1;
                currentPosition = ++currentPosition % 8;
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];       

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = dataBuffer.get(currentPosition);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];    

            int readInStep = 1;
            int initPosition = dataBuffer.position();

            for(int i = 0; i < length; i++)
            {
                int currentPosition = initPosition + i*(readInStep + skipBytesStep);
                byte readInByte = dataBuffer.get(currentPosition);
                data[length - 1 - i] = readInByte & 1;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(readInStep + skipBytesStep)));

            return data;           
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            int currentPosition = 0;
            byte currentByte = 0;

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    currentByte = currentPosition == 0 ? dataBuffer.get() : currentByte;
                    data[j][i] = (currentByte >> currentPosition) & 1;
                    currentPosition = ++currentPosition % 8;
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex,  ByteBuffer dataBuffer)
        {
            int currentPosition = 0;
            byte currentByte = 0;

            for(int i = 0; i<length; i++)
            {                   
                currentByte = currentPosition == 0 ? dataBuffer.get() : currentByte;
                data[i][columnIndex] = (currentByte >> currentPosition) & 1;
                currentPosition = ++currentPosition % 8;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int currentPosition = 0;
            byte currentByte = 0;

            for(int i = length; i >= 0; i--)
            {
                currentByte = currentPosition == 0 ? dataBuffer.get() : currentByte;
                data[i][columnIndex] = (currentByte >> currentPosition) & 1;
                currentPosition = ++currentPosition % 8;
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex,  ByteBuffer dataBuffer)
        {
            int readInStep = 1;
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(readInStep + skipBytesStep);

                data[i][columnIndex] = dataBuffer.get(currentPosition) & 1;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(readInStep + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data,int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int readInStep = 1;
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(readInStep + skipBytesStep);

                data[length - 1 - i][columnIndex] = dataBuffer.get(currentPosition) & 1;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(readInStep + skipBytesStep)));
        }

    },

    INT8(1, false)
    {
        @Override
        public int[] readIn1DArray(int length, 
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = dataBuffer.get();
            }

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, 
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = dataBuffer.get();
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];       

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = dataBuffer.get(currentPosition);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];    

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = dataBuffer.get(currentPosition);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = dataBuffer.get();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex,  ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = dataBuffer.get();
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = length; i >= 0; i--)
            {
                data[i][columnIndex] = dataBuffer.get();
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex,  ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = dataBuffer.get(currentPosition);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data,int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = dataBuffer.get(currentPosition);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

    },

    UINT8(1, true) {
        @Override
        public int[] readIn1DArray(int length, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = (dataBuffer.get() & 0x000000ff);
            }

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, 
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = (dataBuffer.get() & 0x000000ff);
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = (dataBuffer.get(currentPosition) & 0x000000ff);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = (dataBuffer.get(currentPosition) & 0x000000ff);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = (dataBuffer.get() & 0x000000ff);
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = (dataBuffer.get() & 0x000000ff);
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = length; i >= 0; i--)
            {
                data[i][columnIndex] = (dataBuffer.get() & 0x000000ff);
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = (dataBuffer.get(currentPosition) & 0x000000ff);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = (dataBuffer.get(currentPosition) & 0x000000ff);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 

    INT16(2, false) 
    {
        @Override
        public int[] readIn1DArray(int length, ByteBuffer dataBuffer)
        {
            int[] data = new int[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = dataBuffer.getShort();
            }

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = dataBuffer.getShort();
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = dataBuffer.getShort(currentPosition);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = dataBuffer.getShort(currentPosition);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = dataBuffer.getShort();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = dataBuffer.getShort();
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = dataBuffer.getShort();
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = dataBuffer.getShort(currentPosition);
            }     

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = dataBuffer.getShort(currentPosition);
            }         

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 
    UINT16(2, true)
    {
        @Override
        public int[] readIn1DArray(int length, 
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = (dataBuffer.getShort() & 0x0000ffff);
            }

            return data;
        }


        @Override
        public int[] readIn1DArrayReversed(int length, ByteBuffer dataBuffer)
        {
            int[] data = new int[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = (dataBuffer.getShort() & 0x0000ffff);
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = (dataBuffer.getShort(currentPosition) & 0x0000ffff);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = (dataBuffer.getShort(currentPosition) & 0x0000ffff);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = (dataBuffer.getShort() & 0x0000ffff);
                }
            }

            return data;
        }


        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = (dataBuffer.getShort() & 0x0000ffff);
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = (dataBuffer.getShort() & 0x0000ffff);
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = (dataBuffer.getShort(currentPosition) & 0x0000ffff);
            }          

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = (dataBuffer.getShort(currentPosition) & 0x0000ffff);
            }    

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 

    INT32(4, false) 
    {
        @Override
        public int[] readIn1DArray(int length, 
                ByteBuffer dataBuffer)
        {
            int[] data = new int[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = dataBuffer.getInt();
            }

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = dataBuffer.getInt();
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = dataBuffer.getInt(currentPosition);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = dataBuffer.getInt(currentPosition);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = dataBuffer.getInt();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = dataBuffer.getInt();
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = dataBuffer.getInt();
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = dataBuffer.getInt(currentPosition);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = dataBuffer.getInt(currentPosition);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    },

    UINT32(4, true) 
    {
        @Override
        public int[] readIn1DArray(int length, 
                ByteBuffer dataBuffer)
        {
            int[] data = new int[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = NumberUtilities.saturatedCast(dataBuffer.getInt() & 0x00000000ffffffffL);
            }

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = NumberUtilities.saturatedCast(dataBuffer.getInt() & 0x00000000ffffffffL);
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = NumberUtilities.saturatedCast(dataBuffer.getInt(currentPosition) & 0x00000000ffffffffL);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = NumberUtilities.saturatedCast(dataBuffer.getInt(currentPosition) & 0x00000000ffffffffL);
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = NumberUtilities.saturatedCast(dataBuffer.getInt() & 0x00000000ffffffffL);
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getInt() & 0x00000000ffffffffL);
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getInt() & 0x00000000ffffffffL);
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getInt(currentPosition) & 0x00000000ffffffffL);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getInt(currentPosition) & 0x00000000ffffffffL);
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    },

    INT64(8, false) 
    {
        @Override
        public int[] readIn1DArray(int length, 
                ByteBuffer dataBuffer)
        {
            int[] data = new int[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = NumberUtilities.saturatedCast(dataBuffer.getLong());
            }

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = NumberUtilities.saturatedCast(dataBuffer.getLong());
            }

            return data;
        }

        @Override
        public int[] readIn1DArray(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = NumberUtilities.saturatedCast(dataBuffer.getLong(currentPosition));
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[] readIn1DArrayReversed(int length, int skipBytesStep,
                ByteBuffer dataBuffer) 
        {
            int[] data = new int[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = NumberUtilities.saturatedCast(dataBuffer.getLong(currentPosition));
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public int[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = NumberUtilities.saturatedCast(dataBuffer.getLong());
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getLong());
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getLong());
            }           
        }

        @Override
        public void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getLong(currentPosition));
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(int[][] data, int skipBytesStep, int length,
                int columnIndex, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = NumberUtilities.saturatedCast(dataBuffer.getLong(currentPosition));
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }
    ;

    private final int byteSize;
    private final boolean unsigned;

    IntArrayReaderType(int byteSize, boolean unsigned)
    {
        this.byteSize = byteSize;
        this.unsigned = unsigned;
    }

    public boolean isUnsigned()
    {
        return unsigned;
    }

    public int getByteSize()
    {
        return byteSize;
    }

    public static IntArrayReaderType getReader(int byteSize, boolean unsigned)
    {
        for(IntArrayReaderType reader : values())
        {
            if(reader.byteSize == byteSize && reader.unsigned == unsigned)
            {
                return reader;
            }
        }

        throw new IllegalArgumentException("No reader known for numbers with byteSize " + byteSize + " which are " + (unsigned ? "unisgned" : "signed"));
    }

    public abstract int[] readIn1DArray(int length, ByteBuffer dataBuffer);
    public abstract int[] readIn1DArrayReversed(int length, ByteBuffer dataBuffer);
    public abstract int[] readIn1DArray(int length, int skipBytesStep, ByteBuffer dataBuffer);
    public abstract int[] readIn1DArrayReversed(int length, int skipBytesStep, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumn(int[][] data, int length,
            int columnIndex, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumnReversed(int[][] data, int length,
            int columnIndex,ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumn(int[][] data, int skipBytesStep, int length,
            int columnIndex, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumnReversed(int[][] data, int skipBytesStep, int length,
            int columnIndex, ByteBuffer dataBuffer);

    public int[][] readIn2DArrayRowByRow(int rowCount, int columnCount, ByteBuffer dataBuffer)
    {
        int[][] data = new int[rowCount][];

        for(int i = 0; i<rowCount; i++)
        {
            data[i] = readIn1DArray(columnCount, dataBuffer);
        }

        return data;
    }

    public int[][] readIn2DArrayRowByRowReversed(int rowCount, int columnCount, ByteBuffer dataBuffer)
    {
        int[][] data = new int[rowCount][];

        for(int i = rowCount - 1; i >= 0; i--)
        {
            data[i] = readIn1DArray(columnCount, dataBuffer);
        }

        return data;
    }

    public int[][] readIn2DArrayRowByRow(int rowCount, int columnCount, int skipBytesStep, ByteBuffer dataBuffer)
    {
        int[][] data = new int[rowCount][];

        for(int i = 0; i<rowCount; i++)
        {
            data[i] = readIn1DArray(columnCount, skipBytesStep, dataBuffer);
        }

        return data;
    }


    public abstract int[][] readIn2DArrayColumnByColumn(int rowCount, int columnCount, ByteBuffer dataBuffer);

    public int[][][] readIn3DArrayRowByRow(int channelCount,int rowCount, int columnCount, ByteBuffer dataBuffer)
    {
        int[][][] data = new int[channelCount][][];

        for(int i = 0; i<channelCount; i++)
        {
            data[i] = readIn2DArrayRowByRow(rowCount, columnCount, dataBuffer);
        }

        return data;
    }

    public int[][][] readIn3DArrayColumnByColumn(int channelCount,int rowCount, int columnCount, ByteBuffer dataBuffer)
    {
        int[][][] data = new int[channelCount][][];

        for(int i = 0; i<channelCount; i++)
        {
            data[i] = readIn2DArrayColumnByColumn(rowCount, columnCount, dataBuffer);
        }

        return data;
    }

    public static int countBytes(Collection<IntArrayReaderType> readerTypes)
    {
        int byteCount = 0;

        for(IntArrayReaderType readerType : readerTypes)
        {
            byteCount += readerType.getByteSize();
        }

        return byteCount;
    }
}