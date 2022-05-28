package atomicJ.readers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.data.PermissiveChannelFilter;
import atomicJ.gui.UserCommunicableException;
import atomicJ.sources.ChannelSource;


//this class stores information about a source file and the reader that should be used
//to read it in

public class FileReadingPack<E extends ChannelSource> implements ReadingPack<E>
{
    private final List<File> files;
    private final SourceReader<E> reader;
    private final atomicJ.readers.SourceReadingDirectives readingDirectives;

    public FileReadingPack(File file, SourceReader<E> reader)
    {
        this.files = Collections.singletonList(file);
        this.reader = reader;
        this.readingDirectives = new SourceReadingDirectives(PermissiveChannelFilter.getInstance(), this.files.size());
    }

    public FileReadingPack(List<File> files, SourceReader<E> reader)
    {
        this.files = files;
        this.reader = reader;
        this.readingDirectives = new SourceReadingDirectives(PermissiveChannelFilter.getInstance(), this.files.size());
    }

    @Override
    public List<E> readSources() throws UserCommunicableException, IllegalImageException, IllegalSpectroscopySourceException
    {
        List<E> readInSources = new ArrayList<>();
        for(File file : files)
        {
            readInSources.addAll(reader.readSources(file, readingDirectives));
        }
        return readInSources;
    }
}
