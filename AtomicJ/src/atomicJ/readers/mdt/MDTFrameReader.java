package atomicJ.readers.mdt;

import java.io.File;
import java.nio.channels.FileChannel;
import java.util.List;

import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;

public interface MDTFrameReader
{
    public List<ChannelSource> readInAllSources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException;
    public List<SimpleSpectroscopySource> readInSpectroscopySources(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException;
    public List<ImageSource> readInImages(File f, FileChannel channel, int frameIndex, MDTFrameHeader frameHeader) throws UserCommunicableException;
    public int getFrameType();
}