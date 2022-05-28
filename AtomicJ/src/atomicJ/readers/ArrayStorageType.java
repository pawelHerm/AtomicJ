package atomicJ.readers;

import java.nio.ByteBuffer;

public enum ArrayStorageType
{
    ROW_BY_ROW 
    {
        @Override
        public double[][] readIn2DArray(DoubleArrayReaderType readerType,
                DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection, 
                int rowCount, int columnCount, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[][] data = new double[rowCount][];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<rowCount; i++)
                {
                    data[i] = insideVectorDirection.readIn1DDoubleArray(readerType, columnCount, scale, offset, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = rowCount - 1; i >= 0; i--)
                {
                    data[i] = insideVectorDirection.readIn1DDoubleArray(readerType, columnCount, scale, offset, dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }

        @Override
        public int[][] readIn2DArray(IntArrayReaderType readerType,
                DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection, int rowCount, int columnCount,ByteBuffer dataBuffer)
        {
            int[][] data = new int[rowCount][];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<rowCount; i++)
                {
                    data[i] = insideVectorDirection.readIn1DIntArray(readerType, columnCount, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = rowCount - 1; i >= 0; i--)
                {
                    data[i] = insideVectorDirection.readIn1DIntArray(readerType, columnCount,  dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }

        @Override
        public double[][] readIn2DArray(DoubleArrayReaderType readerType, DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection, 
                int skipByteStep, int rowCount, int columnCount,  double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[][] data = new double[rowCount][columnCount];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<rowCount; i++)
                {
                    data[i] = insideVectorDirection.readIn1DDoubleArray(readerType, skipByteStep, columnCount, scale, offset, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = rowCount - 1; i >= 0; i--)
                {
                    data[i] = insideVectorDirection.readIn1DDoubleArray(readerType, skipByteStep, columnCount, scale, offset, dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }

        @Override
        public int[][] readIn2DArray(IntArrayReaderType readerType, DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection, 
                int skipByteStep, int rowCount, int columnCount,  ByteBuffer dataBuffer) 
        {
            int[][] data = new int[rowCount][columnCount];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<rowCount; i++)
                {
                    data[i] = insideVectorDirection.readIn1DIntArray(readerType, skipByteStep, columnCount, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = rowCount - 1; i >= 0; i--)
                {
                    data[i] = insideVectorDirection.readIn1DIntArray(readerType, skipByteStep, columnCount, dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }
    },
    COLUMN_BY_COLUMN 
    {
        @Override
        public double[][] readIn2DArray(DoubleArrayReaderType readerType, DataStorageDirection betweenVectorDirection, 
                DataStorageDirection insideVectorDirection, int rowCount, int columnCount, double scale, double offset,
                ByteBuffer dataBuffer) 
        {
            double[][] data = new double[rowCount][columnCount];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<columnCount; i++)
                {
                    insideVectorDirection.readInAndFill2DDoubleColumn(readerType, data, rowCount, i, scale, offset, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = rowCount - 1; i >= 0; i--)
                {
                    insideVectorDirection.readInAndFill2DDoubleColumn(readerType, data, rowCount, i, scale, offset, dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }

        @Override
        public int[][] readIn2DArray(IntArrayReaderType readerType, DataStorageDirection betweenVectorDirection, 
                DataStorageDirection insideVectorDirection, int rowCount, int columnCount, ByteBuffer dataBuffer) 
        {
            int[][] data = new int[rowCount][columnCount];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<columnCount; i++)
                {
                    insideVectorDirection.readInAndFill2DIntColumn(readerType, data, rowCount, i, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = rowCount - 1; i >= 0; i--)
                {
                    insideVectorDirection.readInAndFill2DIntColumn(readerType, data, rowCount, i,  dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }


        @Override
        public double[][] readIn2DArray(DoubleArrayReaderType readerType,DataStorageDirection betweenVectorDirection,
                DataStorageDirection insideVectorDirection, int skipBytesStep,
                int rowCount, int columnCount, double scale, double offset, ByteBuffer dataBuffer) 
        {
            double[][] data = new double[rowCount][columnCount];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<columnCount; i++)
                {
                    insideVectorDirection.readInAndFill2DDoubleColumn(readerType, data, skipBytesStep, rowCount, i, scale, offset, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = columnCount - 1; i >= 0; i--)
                {
                    insideVectorDirection.readInAndFill2DDoubleColumn(readerType, data, skipBytesStep, rowCount, i, scale, offset, dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }

        @Override
        public int[][] readIn2DArray(IntArrayReaderType readerType,DataStorageDirection betweenVectorDirection,
                DataStorageDirection insideVectorDirection, int skipBytesStep,
                int rowCount, int columnCount, ByteBuffer dataBuffer) 
        {
            int[][] data = new int[rowCount][columnCount];

            if(DataStorageDirection.STRAIGHT.equals(betweenVectorDirection))
            {
                for(int i = 0; i<columnCount; i++)
                {
                    insideVectorDirection.readInAndFill2DIntColumn(readerType, data, skipBytesStep, rowCount, i, dataBuffer);
                }
            }
            else if(DataStorageDirection.REVERSED.equals(betweenVectorDirection))
            {
                for(int i = columnCount - 1; i >= 0; i--)
                {
                    insideVectorDirection.readInAndFill2DIntColumn(readerType, data, skipBytesStep, rowCount, i, dataBuffer);
                }
            }
            else 
            {
                throw new IllegalArgumentException("The enum " + betweenVectorDirection + " is not recognized");
            }

            return data;
        }
    };

    public abstract double[][] readIn2DArray(DoubleArrayReaderType readerType, DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection,
            int rowCount, int columnCount, double scale, double offset, ByteBuffer dataBuffer);

    public abstract double[][] readIn2DArray(DoubleArrayReaderType readerType, DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection,
            int bytesSkipStep, int rowCount, int columnCount, double scale, double offset, ByteBuffer dataBuffer);

    public abstract int[][] readIn2DArray(IntArrayReaderType readerType, DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection, int rowCount, int columnCount, ByteBuffer dataBuffer);
    public abstract int[][] readIn2DArray(IntArrayReaderType readerType,DataStorageDirection betweenVectorDirection, DataStorageDirection insideVectorDirection, int skipBytesStep, int rowCount, int columnCount, ByteBuffer dataBuffer);

    public double[][] readIn2DArray(DoubleArrayReaderType readerType, 
            int rowCount, int columnCount, double scale, double offset, ByteBuffer dataBuffer)
    {
        return readIn2DArray(readerType, DataStorageDirection.STRAIGHT, DataStorageDirection.STRAIGHT, rowCount, columnCount, scale, offset, dataBuffer);
    }
}