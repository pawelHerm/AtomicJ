package atomicJ.readers;

import java.util.List;

import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ChannelSource;

//this class stores information about a source file and the reader that should be used
//to read it in

public interface ReadingPack<E extends ChannelSource>
{  
    public List<E> readSources() throws UserCommunicableException, IllegalImageException, IllegalSpectroscopySourceException;
}
