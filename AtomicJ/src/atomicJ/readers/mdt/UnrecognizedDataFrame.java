package atomicJ.readers.mdt;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;

import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;

public class UnrecognizedDataFrame implements MDTFrameReader
{
    public static final int FRAME_TYPE = -1;

    @Override
    public List<ChannelSource> readInAllSources(File f, FileChannel channel, int maxIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {
        return Collections.emptyList();
    }

    @Override
    public List<SimpleSpectroscopySource> readInSpectroscopySources(File f, FileChannel channel, int maxIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {
        return Collections.emptyList();
    }

    @Override
    public List<ImageSource> readInImages(File f, FileChannel channel, int maxIndex, MDTFrameHeader frameHeader) throws UserCommunicableException 
    {
        return Collections.emptyList();
    }

    @Override
    public int getFrameType() {
        return FRAME_TYPE;
    }    
}