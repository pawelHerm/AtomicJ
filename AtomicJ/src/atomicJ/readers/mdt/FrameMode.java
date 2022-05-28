package atomicJ.readers.mdt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import atomicJ.gui.UserCommunicableException;
import atomicJ.utilities.FileInputUtilities;

public class FrameMode
{
    public static final int BYTE_SIZE = 8;

    private final int mode; //2 bytes, uint
    private final int columnCount; //2 bytes, uint, Number of image points in X direction.
    private final int rowCount; //2 bytes, uint, Number of image points in Y direction.
    private final int dotCount; //2 bytes, uint, Number of dots

    private FrameMode(ByteBuffer buffer)
    {
        this.mode = FileInputUtilities.getUnsigned(buffer.getShort());
        this.columnCount = FileInputUtilities.getUnsigned(buffer.getShort());
        this.rowCount = FileInputUtilities.getUnsigned(buffer.getShort());
        this.dotCount = FileInputUtilities.getUnsigned(buffer.getShort());
    }

    public static FrameMode readIn(FileChannel channel) throws UserCommunicableException
    {
        ByteBuffer buffer = FileInputUtilities.readBytesToBuffer(channel, FrameMode.BYTE_SIZE, ByteOrder.LITTLE_ENDIAN);
        FrameMode frameMode = new FrameMode(buffer);

        return frameMode;
    }

    public int getMode()
    {
        return mode;
    }

    public int getRowCount()
    {
        return rowCount;
    }

    public int getColumnCount()
    {
        return columnCount;
    }

    public int getPixelCount()
    {
        return columnCount*rowCount;
    }

    public int getDotCount()
    {
        return dotCount;
    }
}
