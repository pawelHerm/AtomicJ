package atomicJ.readers;

import java.nio.ByteBuffer;

public enum DataStorageDirection
{
    STRAIGHT {
        @Override
        public double[] readIn1DDoubleArray(DoubleArrayReaderType readerType, int length,double scale, double offset, ByteBuffer dataBuffer) 
        {
            return readerType.readIn1DArray(length, scale, offset, dataBuffer);
        }

        @Override
        public double[] readIn1DDoubleArray(DoubleArrayReaderType readerType, int skipBytesStep, int length,double scale, double offset, ByteBuffer dataBuffer) 
        {
            return readerType.readIn1DArray(length, skipBytesStep, scale, offset, dataBuffer);
        }

        //ints

        @Override
        public int[] readIn1DIntArray(IntArrayReaderType readerType, int length, ByteBuffer dataBuffer) 
        {
            return readerType.readIn1DArray(length, dataBuffer);
        }

        @Override
        public int[] readIn1DIntArray(IntArrayReaderType readerType, int skipBytesStep, int length, ByteBuffer dataBuffer) 
        {
            return readerType.readIn1DArray(length, skipBytesStep, dataBuffer);
        }

        @Override
        public void readInAndFill2DDoubleColumn(DoubleArrayReaderType readerType,double[][] data, int rowToFillCount, int columnIndex, double scale, double offset, ByteBuffer dataBuffer) 
        {
            readerType.readInAndFill2DColumn(data, rowToFillCount, columnIndex, scale, offset, dataBuffer);                  
        }

        @Override
        public void readInAndFill2DIntColumn(IntArrayReaderType readerType, int[][] data, int rowToFillCount, int columnIndex, ByteBuffer dataBuffer) 
        {
            readerType.readInAndFill2DColumn(data, rowToFillCount, columnIndex, dataBuffer);                  
        }

        @Override
        public void readInAndFill2DDoubleColumn(DoubleArrayReaderType readerType,double[][] data, int skipBytesStep, int rowToFillCount, int columnIndex,double scale, double offset, ByteBuffer dataBuffer) 
        {
            readerType.readInAndFill2DColumn(data, skipBytesStep, rowToFillCount, columnIndex, scale, offset, dataBuffer);                  
        }

        @Override
        public void readInAndFill2DIntColumn(IntArrayReaderType readerType, int[][] data, int skipBytesStep, int rowToFillCount, int columnIndex, ByteBuffer dataBuffer) 
        {
            readerType.readInAndFill2DColumn(data, skipBytesStep, rowToFillCount, columnIndex, dataBuffer);                  
        }

        @Override
        public double[][] readInArrayOfDoubleTuples(DoubleArrayReaderType readerType, int skipByteStep, int columnCount, int tupleLength, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[][] data = new double[columnCount][];

            for(int i = 0; i <columnCount; i++)
            {
                data[i] = readerType.readIn1DArray(tupleLength, scale, offset, dataBuffer);
                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + skipByteStep));
            }

            return data;
        }


        @Override
        public int[][] readInArrayOfIntTuples(IntArrayReaderType readerType, int skipByteStep, int columnCount, int tupleLength, ByteBuffer dataBuffer)
        {
            int[][] data = new int[columnCount][];

            for(int i = 0; i <columnCount; i++)
            {
                data[i] = readerType.readIn1DArray(tupleLength, dataBuffer);
                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + skipByteStep));
            }

            return data;
        }

        @Override
        public void readInAndFillColumnOfDoubleTuples(DoubleArrayReaderType readerType, double[][][] data, int skipByteStep, int rowCount, int columnIndex, int tupleLength, double scale, double offset, ByteBuffer dataBuffer) 
        {
            for(int i = 0; i<rowCount; i++)
            {
                data[i][columnIndex] = readerType.readIn1DArray(tupleLength, scale, offset, dataBuffer);
                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + skipByteStep));
            }
        }

        @Override
        public int getStep() 
        {
            return 1;
        }

        @Override
        public int getOffset(int length) {
            return 0;
        }
    },

    REVERSED {
        @Override
        public double[] readIn1DDoubleArray(DoubleArrayReaderType readerType, int length,double scale, double offset, ByteBuffer dataBuffer)
        {
            return readerType.readIn1DArrayReversed(length, scale, offset, dataBuffer);
        }

        @Override
        public double[] readIn1DDoubleArray(DoubleArrayReaderType readerType, int skipBytesStep, int length,double scale, double offset, ByteBuffer dataBuffer)
        {
            return readerType.readIn1DArrayReversed(length, skipBytesStep, scale, offset, dataBuffer);
        }

        @Override
        public int[] readIn1DIntArray(IntArrayReaderType readerType, int length, ByteBuffer dataBuffer)
        {
            return readerType.readIn1DArrayReversed(length, dataBuffer);
        }

        @Override
        public int[] readIn1DIntArray(IntArrayReaderType readerType, int skipBytesStep, int length, ByteBuffer dataBuffer)
        {
            return readerType.readIn1DArrayReversed(length, skipBytesStep, dataBuffer);
        }

        @Override
        public void readInAndFill2DDoubleColumn(DoubleArrayReaderType readerType, double[][] data, int rowToFillCount, int columnIndex, double scale, double offset, ByteBuffer dataBuffer)
        {
            readerType.readInAndFill2DColumnReversed(data, rowToFillCount, columnIndex, scale, offset, dataBuffer);          
        }

        @Override
        public void readInAndFill2DIntColumn(IntArrayReaderType readerType, int[][] data, int rowToFillCount, int columnIndex, ByteBuffer dataBuffer)
        {
            readerType.readInAndFill2DColumnReversed(data, rowToFillCount, columnIndex,  dataBuffer);          
        }

        @Override
        public void readInAndFill2DDoubleColumn(DoubleArrayReaderType readerType, double[][] data,int skipBytesStep, int rowToFillCount, int columnIndex,
                double scale, double offset, ByteBuffer dataBuffer)
        {
            readerType.readInAndFill2DColumnReversed(data, skipBytesStep, rowToFillCount, columnIndex, scale, offset, dataBuffer);          
        }

        @Override
        public void readInAndFill2DIntColumn(IntArrayReaderType readerType, int[][] data,int skipBytesStep, int rowToFillCount, int columnIndex,
                ByteBuffer dataBuffer)
        {
            readerType.readInAndFill2DColumnReversed(data, skipBytesStep, rowToFillCount, columnIndex, dataBuffer);          
        }

        @Override
        public double[][] readInArrayOfDoubleTuples(DoubleArrayReaderType readerType, int skipByteStep, int columnCount, int tupleLength, double scale, double offset,
                ByteBuffer dataBuffer)
        {
            double[][] data = new double[columnCount][];

            for(int i = columnCount - 1; i >= 0; i--)
            {
                data[i] = readerType.readIn1DArray(tupleLength, scale, offset, dataBuffer);
                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + skipByteStep));
            }

            return data;
        }

        @Override
        public int[][] readInArrayOfIntTuples(IntArrayReaderType readerType, int skipByteStep, int columnCount, int tupleLength, ByteBuffer dataBuffer)
        {
            int[][] data = new int[columnCount][];

            for(int i = columnCount - 1; i >= 0; i--)
            {
                data[i] = readerType.readIn1DArray(tupleLength, dataBuffer);
                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + skipByteStep));
            }

            return data;
        }

        @Override
        public void readInAndFillColumnOfDoubleTuples(DoubleArrayReaderType readerType, double[][][] data, int skipByteStep, int rowCount, int columnIndex, int tupleLength, double scale, double offset, ByteBuffer dataBuffer) 
        {
            for(int i = rowCount - 1; i >= 0; i--)
            {
                data[i][columnIndex] = readerType.readIn1DArray(tupleLength, scale, offset, dataBuffer);
                dataBuffer.position(Math.min(dataBuffer.limit(), dataBuffer.position() + skipByteStep));
            }
        }

        @Override
        public int getStep() {
            return -1;
        }

        @Override
        public int getOffset(int length) {
            return length - 1;
        }
    };

    public abstract double[] readIn1DDoubleArray(DoubleArrayReaderType readerType, int length, double scale, double offset, ByteBuffer dataBuffer);
    public abstract double[] readIn1DDoubleArray(DoubleArrayReaderType readerType, int skipBytesStep, int length, double scale, double offset, ByteBuffer dataBuffer);

    public abstract int[] readIn1DIntArray(IntArrayReaderType readerType, int length, ByteBuffer dataBuffer);
    public abstract int[] readIn1DIntArray(IntArrayReaderType readerType, int skipBytesStep, int length, ByteBuffer dataBuffer);


    public abstract double[][] readInArrayOfDoubleTuples(DoubleArrayReaderType readerType, int skipByteStep, int columnCount, int tupleLength, double scale, double offset, ByteBuffer dataBuffer);
    public abstract int[][] readInArrayOfIntTuples(IntArrayReaderType readerType, int skipByteStep, int columnCount, int tupleLength, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DDoubleColumn(DoubleArrayReaderType readerType, double[][] data, int rowToFillCount,int columnIndex, double scale, double offset, ByteBuffer dataBuffer);
    public abstract void readInAndFill2DDoubleColumn(DoubleArrayReaderType readerType, double[][] data, int skipBytesStep, int rowToFillCount, int columnIndex, double scale, double offset, ByteBuffer dataBuffer);

    public abstract void readInAndFill2DIntColumn(IntArrayReaderType readerType, int[][] data, int rowToFillCount, int columnIndex, ByteBuffer dataBuffer);
    public abstract void readInAndFill2DIntColumn(IntArrayReaderType readerType, int[][] data, int skipBytesStep, int rowToFillCount, int columnIndex, ByteBuffer dataBuffer);

    public abstract void readInAndFillColumnOfDoubleTuples(DoubleArrayReaderType readerType, double[][][] data, int skipByteStep, int rowCount, int columnIndex, int tupleLength, double scale, double offset, ByteBuffer dataBuffer); 

    public abstract int getStep();
    public abstract int getOffset(int length);
}
