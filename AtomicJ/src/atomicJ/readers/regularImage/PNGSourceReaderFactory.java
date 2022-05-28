package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class PNGSourceReaderFactory extends SourceReaderFactory<PNGSourceReader>
{
    @Override
    public PNGSourceReader getReader()
    {
        return new PNGSourceReader();
    }

    @Override
    public String getDescription()
    {
        return PNGSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return PNGSourceReader.getAcceptedExtensions();
    }
}
