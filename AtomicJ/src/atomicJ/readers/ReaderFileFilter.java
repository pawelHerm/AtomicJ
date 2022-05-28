package atomicJ.readers;

import java.io.File;
import java.io.FileFilter;

public class ReaderFileFilter implements FileFilter
{
    private final SourceReader<?> reader;

    public ReaderFileFilter(SourceReader<?> reader)
    {
        this.reader = reader;
    }

    @Override
    public boolean accept(File pathname)
    {
        return reader.accept(pathname);
    }

}
