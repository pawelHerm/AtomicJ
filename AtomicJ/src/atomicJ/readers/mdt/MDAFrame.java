package atomicJ.readers.mdt;

import java.io.File;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import atomicJ.data.ChannelFilter;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ImageSource;
import atomicJ.sources.SimpleSpectroscopySource;

public interface MDAFrame
{
    public List<ImageSource> readInImageSources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter) throws UserCommunicableException;
    public List<SimpleSpectroscopySource> readInSpectroscopySources(MDAFrameGeneralReader parentReader, File f, ReadableByteChannel channel, ChannelFilter filter)
            throws UserCommunicableException;
}