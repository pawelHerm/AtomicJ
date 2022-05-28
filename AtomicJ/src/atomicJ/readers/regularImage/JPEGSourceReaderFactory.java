package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class JPEGSourceReaderFactory extends SourceReaderFactory<JPEGSourceReader>
{
    @Override
    public JPEGSourceReader getReader()
    {
        return new JPEGSourceReader();
    }

    @Override
    public String getDescription()
    {
        return JPEGSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return JPEGSourceReader.getAcceptedExtensions();
    }
}
