package atomicJ.readers.mdt;

import java.nio.ByteBuffer;

public class MDTCompDataFrame
{
    private final int fileCount;

    public MDTCompDataFrame(ByteBuffer buffer)
    {
        fileCount = buffer.getInt();
    }

    public int getFileCount()
    {
        return fileCount;
    }
}
