package atomicJ.utilities;

import java.io.File;
import atomicJ.readers.SourceReader;


public final class ReaderBasedFileFilter extends javax.swing.filechooser.FileFilter 
{
    private final String description;
    private final SourceReader<?> reader;

    public ReaderBasedFileFilter(String description, SourceReader<?> reader) {
        if (reader == null) {
            throw new IllegalArgumentException(
                    "reader must be non-null");
        }
        this.description = description;
        this.reader = reader;
    }

    @Override
    public boolean accept(File f) 
    {
        if (f == null) 
        {
            return false;
        }

        boolean accept = f.isDirectory() || reader.accept(f);
        return accept;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return super.toString() + "[description = " + getDescription() +"]";
    }
}

