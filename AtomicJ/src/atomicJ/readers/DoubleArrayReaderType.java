package atomicJ.readers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import atomicJ.utilities.NumberUtilities;

public enum DoubleArrayReaderType
{
    INT8(1, false)
    {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.get();
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.get() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*dataBuffer.get() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];       

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*dataBuffer.get(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];    

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*dataBuffer.get(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*dataBuffer.get();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = scale*dataBuffer.get() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = length; i >= 0; i--)
            {
                data[i][columnIndex] = scale*dataBuffer.get() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*dataBuffer.get(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data,int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = scale*dataBuffer.get(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

    }, UINT8(1, true) {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*(dataBuffer.get() & 0x000000ff);
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*(dataBuffer.get() & 0x000000ff) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*(dataBuffer.get() & 0x000000ff) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*(dataBuffer.get(currentPosition) & 0x000000ff) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*(dataBuffer.get(currentPosition) & 0x000000ff) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*(dataBuffer.get() & 0x000000ff);
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = scale*(dataBuffer.get() & 0x000000ff) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = length; i >= 0; i--)
            {
                data[i][columnIndex] = scale*(dataBuffer.get() & 0x000000ff) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*(dataBuffer.get(currentPosition) & 0x000000ff) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = scale*(dataBuffer.get(currentPosition) & 0x000000ff) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 
    INT16(2, false) 
    {
        @Override
        public double[] readIn1DArray(int length, double scale, ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getShort();
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getShort() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*dataBuffer.getShort() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*dataBuffer.getShort(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*dataBuffer.getShort(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*dataBuffer.getShort();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = scale*dataBuffer.getShort() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = scale*dataBuffer.getShort() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*dataBuffer.getShort(currentPosition) + offset;
            }     

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = scale*dataBuffer.getShort(currentPosition) + offset;
            }         

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 
    UINT16(2, true)
    {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*(dataBuffer.getShort() & 0x0000ffff);
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*(dataBuffer.getShort() & 0x0000ffff) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*(dataBuffer.getShort() & 0x0000ffff) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*(dataBuffer.getShort(currentPosition) & 0x0000ffff) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*(dataBuffer.getShort(currentPosition) & 0x0000ffff) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*(dataBuffer.getShort() & 0x0000ffff);
                }
            }

            return data;
        }


        @Override
        public void readInAndFill2DColumn(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = scale*(dataBuffer.getShort() & 0x0000ffff) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = scale*(dataBuffer.getShort() & 0x0000ffff) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*(dataBuffer.getShort(currentPosition) & 0x0000ffff) + offset;
            }          

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = scale*(dataBuffer.getShort(currentPosition) & 0x0000ffff) + offset;
            }    

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 
    INT32(4, false) {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getInt();
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getInt() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*dataBuffer.getInt() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*dataBuffer.getInt(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*dataBuffer.getInt(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*dataBuffer.getInt();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = scale*dataBuffer.getInt() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = length - 1; i >= 0; i--)
            {
                data[i][columnIndex] = scale*dataBuffer.getInt() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*dataBuffer.getInt(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = scale*dataBuffer.getInt(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));
        }
    },
    UINT32(4, true)
    {
        @Override
        public double[] readIn1DArray(int length, double scale, ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*(dataBuffer.getInt() & 0x00000000ffffffffL);
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*(dataBuffer.getInt()& 0x00000000ffffffffL) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*(dataBuffer.getInt()& 0x00000000ffffffffL) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*(dataBuffer.getInt(currentPosition)& 0x00000000ffffffffL) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*(dataBuffer.getInt(currentPosition)& 0x00000000ffffffffL) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*(dataBuffer.getInt()& 0x00000000ffffffffL);
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<rowToFillCount; i++)
            {
                data[i][columnIndex] = scale*(dataBuffer.getInt()& 0x00000000ffffffffL) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = rowToFillCount; i >= 0; i--)
            {
                data[i][columnIndex] = scale*(dataBuffer.getInt()& 0x00000000ffffffffL) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i][columnIndex] = scale*(dataBuffer.getInt(currentPosition)& 0x00000000ffffffffL) + offset;
            }       

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();
            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i][columnIndex] = scale*(dataBuffer.getInt(currentPosition)& 0x00000000ffffffffL) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }
    },
    INT64(8, false) {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getLong();
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getLong() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*dataBuffer.getLong() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*dataBuffer.getLong(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*dataBuffer.getLong(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*dataBuffer.getLong();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<rowToFillCount; i++)
            {
                data[i][columnIndex] = scale*dataBuffer.getLong() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = rowToFillCount; i >= 0; i--)
            {
                data[i][columnIndex] = scale*dataBuffer.getLong() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data,int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*dataBuffer.getLong(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data,int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = scale*dataBuffer.getLong(currentPosition) + offset;
            }    

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 
    UINT64(8, true) {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = 0; i<length; i++)
            {
                for (int j = 0; j < 8; j++) {
                    bytes[littleEndian ? 8 - 1 - j : j] = dataBuffer.get();
                }

                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[i] = n.multiply(scaleBD).doubleValue();
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = 0; i<length; i++)
            {
                for (int j = 0; j < 8; j++) {
                    bytes[littleEndian ? 8 - 1 - j : j] = dataBuffer.get();
                }
                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[i] = n.multiply(scaleBD).doubleValue() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = length - 1; i >= 0; i--)
            {
                for (int j = 0; j < 8; j++) {
                    bytes[littleEndian ? 8 - 1 - j : j] = dataBuffer.get();
                }
                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[i] = n.multiply(scaleBD).doubleValue() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                dataBuffer.position(currentPosition);

                for (int j = 0; j < 8; j++) {
                    bytes[littleEndian ? 8 - 1 - j : j] = dataBuffer.get();
                }
                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[i] = n.multiply(scaleBD).doubleValue() + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());
            int byteSize = getByteSize();

            int initPosition = dataBuffer.position();

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                dataBuffer.position(currentPosition);

                for (int j = 0; j < 8; j++) {
                    bytes[littleEndian ? 8 - 1 - j : j] = dataBuffer.get();
                }
                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[length - 1 - i] = n.multiply(scaleBD).doubleValue() + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }


        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount, int columnCount, double scale, ByteBuffer dataBuffer) 
        {
            double[][] data = new double[rowCount][columnCount];

            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    for (int k = 0; k < 8; k++) {
                        bytes[littleEndian ? 8 - 1 - k : k] = dataBuffer.get();
                    }
                    BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                    data[j][i] = n.multiply(scaleBD).doubleValue();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = 0; i<rowToFillCount; i++)
            {
                for (int k = 0; k < 8; k++) {
                    bytes[littleEndian ? 8 - 1 - k : k] = dataBuffer.get();
                }
                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[i][columnIndex] = n.multiply(scaleBD).doubleValue() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            for(int i = rowToFillCount; i >= 0; i--)
            {
                for (int k = 0; k < 8; k++) {
                    bytes[littleEndian ? 8 - 1 - k : k] = dataBuffer.get();
                }
                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[i][columnIndex] = n.multiply(scaleBD).doubleValue() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());
            int byteSize = getByteSize();

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            int initPosition = dataBuffer.position();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                dataBuffer.position(currentPosition);

                for (int k = 0; k < 8; k++) {
                    bytes[littleEndian ? 8 - 1 - k : k] = dataBuffer.get();
                }
                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[i][columnIndex] = n.multiply(scaleBD).doubleValue() + offset;
            }        

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            boolean littleEndian = ByteOrder.LITTLE_ENDIAN.equals(dataBuffer.order());

            BigDecimal scaleBD = BigDecimal.valueOf(scale);
            byte[] bytes = new byte[8];

            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                dataBuffer.position(currentPosition);

                for (int k = 0; k < 8; k++) {
                    bytes[littleEndian ? 8 - 1 - k : k] = dataBuffer.get();
                }

                BigDecimal n = new BigDecimal(new BigInteger(1, bytes));
                data[length - 1 - i][columnIndex] = n.multiply(scaleBD).doubleValue() + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }
    }, 

    FLOAT16(2, false)
    {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                int bits = dataBuffer.getShort() & 0x0000ffff;               
                data[i] = scale*NumberUtilities.halfFloatToFloat(bits);
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                int bits = dataBuffer.getShort() & 0x0000ffff;               
                data[i] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                int bits = dataBuffer.getShort() & 0x0000ffff;               
                data[i] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                int bits = dataBuffer.getShort(currentPosition) & 0x0000ffff;               

                data[i] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                int bits = dataBuffer.getShort(currentPosition) & 0x0000ffff;               
                data[length - 1 - i] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(), initPosition + (length)*(byteSize + skipBytesStep)));


            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)      
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    int bits = dataBuffer.getShort() & 0x0000ffff;               
                    data[j][i] = scale*NumberUtilities.halfFloatToFloat(bits);
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<rowToFillCount; i++)
            {
                int bits = dataBuffer.getShort() & 0x0000ffff;               
                data[i][columnIndex] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = rowToFillCount; i >= 0; i--)
            {
                int bits = dataBuffer.getShort() & 0x0000ffff;               
                data[i][columnIndex] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<rowToFillCount; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                int bits = dataBuffer.getShort(currentPosition) & 0x0000ffff;               
                data[i][columnIndex] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (rowToFillCount)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<rowToFillCount; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                int bits = dataBuffer.getShort(currentPosition) & 0x0000ffff;               
                data[currentPosition - 1 - i][columnIndex] = scale*NumberUtilities.halfFloatToFloat(bits) + offset;
            }   

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (rowToFillCount)*(byteSize + skipBytesStep)));
        }
    },

    FLOAT32(4, false) 
    {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getFloat();
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getFloat() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*dataBuffer.getFloat() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*dataBuffer.getFloat(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*dataBuffer.getFloat(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));


            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)      
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*dataBuffer.getFloat();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<rowToFillCount; i++)
            {
                data[i][columnIndex] = scale*dataBuffer.getFloat() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = rowToFillCount; i >= 0; i--)
            {
                data[i][columnIndex] = scale*dataBuffer.getFloat() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<rowToFillCount; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*dataBuffer.getFloat(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (rowToFillCount)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int rowToFillCount,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<rowToFillCount; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[currentPosition - 1 - i][columnIndex] = scale*dataBuffer.getFloat(currentPosition) + offset;
            }   

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (rowToFillCount)*(byteSize + skipBytesStep)));
        }
    }, 

    FLOAT48(6, false) {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer)
        {
            throw new UnsupportedOperationException("The method readIn1DArray is not yet implemented for float64");
        }


        @Override
        public double[] readIn1DArrayReversed(int length, double scale,
                double offset, ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn1DArrayReversed is not yet implemented for float64");
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn1DArray is not yet implemented for float64");

        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer) 
        {
            throw new UnsupportedOperationException("The method readIn2DArrayColumnByColumn is not yet implemented for float64");
        }

        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn1DArray is not yet implemented for float64");
        }


        @Override
        public void readInAndFill2DColumn(double[][] data, int rowToFillCount,
                int columnIndex, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            throw new UnsupportedOperationException("The method readInAndFill2DColumn is not yet implemented for float64");            
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data,
                int rowToFillCount, int columnIndex, double scale,
                double offset, ByteBuffer dataBuffer) 
        {
            throw new UnsupportedOperationException("The method readInAndFill2DColumn is not yet implemented for float64");
        }


        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            throw new UnsupportedOperationException("The method readIn1DArrayReversed is not yet implemented for float64");
        }


        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep,
                int rowToFillCount, int columnIndex, double scale,
                double offset, ByteBuffer dataBuffer)
        {
            throw new UnsupportedOperationException("The method readInAndFill2DColumn is not yet implemented for float64");
        }


        @Override
        public void readInAndFill2DColumnReversed(double[][] data,
                int skipBytesStep, int rowToFillCount, int columnIndex,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            throw new UnsupportedOperationException("The method readInAndFill2DColumnReversed is not yet implemented for float64");            
        }
    },

    FLOAT64(8, false) {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer)
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getDouble();
            }

            return data;
        }


        @Override
        public double[] readIn1DArray(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = 0; i<length; i++)
            {
                data[i] = scale*dataBuffer.getDouble() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[] data = new double[length];

            for(int i = length - 1; i >= 0; i--)
            {
                data[i] = scale*dataBuffer.getDouble() + offset;
            }

            return data;
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer)
        {
            double[] data = new double[length];
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[i] = scale*dataBuffer.getDouble(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer)
        {
            double[] data = new double[length];
            int initPosition = dataBuffer.position();

            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);
                data[length - 1 - i] = scale*dataBuffer.getDouble(currentPosition) + offset;
            }

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));

            return data;
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][columnCount];

            for(int i = 0; i<columnCount; i++)
            { 
                for(int j = 0; j<rowCount; j++)
                {
                    data[j][i] = scale*dataBuffer.getDouble();
                }
            }

            return data;
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = 0; i<length; i++)
            {
                data[i][columnIndex] = scale*dataBuffer.getDouble() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            for(int i = length; i >= 0; i--)
            {
                data[i][columnIndex] = scale*dataBuffer.getDouble() + offset;
            }           
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[i][columnIndex] = scale*dataBuffer.getDouble(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
                int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            int initPosition = dataBuffer.position();
            int byteSize = getByteSize();

            for(int i = 0; i<length; i++)
            {
                int currentPosition = initPosition + i*(byteSize + skipBytesStep);

                data[length - 1 - i][columnIndex] = scale*dataBuffer.getDouble(currentPosition) + offset;
            }           

            dataBuffer.position(Math.min(dataBuffer.limit(),initPosition + (length)*(byteSize + skipBytesStep)));
        }
    },
    FLOAT80(10, false) {
        @Override
        public double[] readIn1DArray(int length, double scale,
                ByteBuffer dataBuffer)
        {
            throw new UnsupportedOperationException("The method readIn1DArray is not yet implemented for float80");
        }

        @Override
        public double[] readIn1DArray(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn1DArray is not yet implemented for float80");
        }


        @Override
        public double[] readIn1DArrayReversed(int length, double scale,
                double offset, ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn1DArrayReversed is not yet implemented for float80");
        }

        @Override
        public double[][] readIn2DArrayColumnByColumn(int rowCount,
                int columnCount, double scale, ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn2DArrayColumnByColumn is not yet implemented for float80");
        }

        @Override
        public double[] readIn1DArray(int length, double scale, double offset,
                ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn1DArray is not yet implemented for float80");
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int rowToFillCount,int columnIndex, double scale, double offset,ByteBuffer dataBuffer) 
        {
            throw new UnsupportedOperationException("The method readInAndFill2DColumn is not yet implemented for float80");
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data,int rowToFillCount, int columnIndex, double scale,
                double offset, ByteBuffer dataBuffer)
        {
            throw new UnsupportedOperationException("The method readInAndFill2DColumn is not yet implemented for float80");
        }

        @Override
        public double[] readIn1DArrayReversed(int length, int skipBytesStep,
                double scale, double offset, ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readIn1DArrayReversed is not yet implemented for float80");
        }

        @Override
        public void readInAndFill2DColumn(double[][] data, int skipBytesStep,
                int rowToFillCount, int columnIndex, double scale,
                double offset, ByteBuffer dataBuffer) {
            throw new UnsupportedOperationException("The method readInAndFill2DColumn is not yet implemented for float80");            
        }

        @Override
        public void readInAndFill2DColumnReversed(double[][] data,
                int skipBytesStep, int rowToFillCount, int columnIndex,
                double scale, double offset, ByteBuffer dataBuffer) 
        {
            throw new UnsupportedOperationException("The method readInAndFill2DColumnReversed is not yet implemented for float80");            
        }
    },
    ;

    private final int byteSize;
    private final boolean unsigned;

    DoubleArrayReaderType(int byteSize, boolean unsigned)
    {
        this.byteSize = byteSize;
        this.unsigned = unsigned;
    }

    public static DoubleArrayReaderType getReaderForFloatingPointInput(int byteSize)
    {
        List<DoubleArrayReaderType> readers = Arrays.asList(FLOAT16,FLOAT32,FLOAT48,FLOAT64,FLOAT80);
        for(DoubleArrayReaderType reader : readers)
        {
            if(reader.byteSize == byteSize)
            {
                return reader;
            }
        }

        throw new IllegalArgumentException("No reader known for floating point numbers with byteSize " + byteSize);
    }

    public static DoubleArrayReaderType getReaderForIntegerInput(int byteSize, boolean unsigned)
    {
        List<DoubleArrayReaderType> readers = Arrays.asList(INT8,INT16,INT32,INT64,UINT8,UINT16,UINT32,UINT64);
        for(DoubleArrayReaderType reader : readers)
        {
            if(reader.byteSize == byteSize && reader.unsigned == unsigned)
            {
                return reader;
            }
        }

        throw new IllegalArgumentException("No reader known for floating point numbers with byteSize " + byteSize);
    }

    public boolean isUnsigned()
    {
        return unsigned;
    }

    public int getByteSize()
    {
        return byteSize;
    }

    public abstract double[] readIn1DArray(int length, double scale, ByteBuffer dataBuffer);
    public abstract double[] readIn1DArray(int length, double scale, double offset, ByteBuffer dataBuffer);
    public abstract double[] readIn1DArrayReversed(int length, double scale, double offset, ByteBuffer dataBuffer);
    public abstract double[] readIn1DArray(int length, int skipBytesStep, double scale, double offset, ByteBuffer dataBuffer);
    public abstract double[] readIn1DArrayReversed(int length, int skipBytesStep, double scale, double offset, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumn(double[][] data, int length,
            int columnIndex, double scale, double offset, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumnReversed(double[][] data, int length,
            int columnIndex, double scale, double offset, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumn(double[][] data, int skipBytesStep, int length,
            int columnIndex, double scale, double offset, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DColumnReversed(double[][] data, int skipBytesStep, int length,
            int columnIndex, double scale, double offset, ByteBuffer dataBuffer);

    public double[][] readIn2DArrayRowByRow(int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)
    {
        double[][] data = new double[rowCount][];

        for(int i = 0; i<rowCount; i++)
        {
            data[i] = readIn1DArray(columnCount, scale, dataBuffer);
        }

        return data;
    }

    public double[][] readIn2DArrayRowByRowReversed(int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)
    {
        double[][] data = new double[rowCount][];

        for(int i = rowCount - 1; i >= 0; i--)
        {
            data[i] = readIn1DArray(columnCount, scale, dataBuffer);
        }

        return data;
    }

    public double[][] readIn2DArrayRowByRow(int rowCount, int columnCount, double scale, double offset, ByteBuffer dataBuffer)
    {
        double[][] data = new double[rowCount][];

        for(int i = 0; i<rowCount; i++)
        {
            data[i] = readIn1DArray(columnCount, scale, offset, dataBuffer);
        }

        return data;
    }

    public double[][] readIn2DArrayRowByRow(int rowCount, int columnCount, int skipBytesStep, double scale, double offset, ByteBuffer dataBuffer)
    {
        double[][] data = new double[rowCount][];

        for(int i = 0; i<rowCount; i++)
        {
            data[i] = readIn1DArray(columnCount, skipBytesStep, scale, offset, dataBuffer);
        }

        return data;
    }


    public abstract double[][] readIn2DArrayColumnByColumn(int rowCount, int columnCount, double scale, ByteBuffer dataBuffer);

    public double[][][] readIn3DArrayRowByRow(int channelCount, int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)
    {
        double[][][] data = new double[channelCount][][];

        for(int i = 0; i<channelCount; i++)
        {
            data[i] = readIn2DArrayRowByRow(rowCount, columnCount, scale, dataBuffer);
        }

        return data;
    }

    public double[][][] readIn3DArrayColumnByColumn(int channelCount,int rowCount, int columnCount, double scale, ByteBuffer dataBuffer)
    {
        double[][][] data = new double[channelCount][][];

        for(int i = 0; i<channelCount; i++)
        {
            data[i] = readIn2DArrayColumnByColumn(rowCount, columnCount, scale, dataBuffer);
        }

        return data;
    }

    public static int countBytes(Collection<DoubleArrayReaderType> readerTypes)
    {
        int byteCount = 0;

        for(DoubleArrayReaderType readerType : readerTypes)
        {
            byteCount += readerType.getByteSize();
        }

        return byteCount;
    }
}