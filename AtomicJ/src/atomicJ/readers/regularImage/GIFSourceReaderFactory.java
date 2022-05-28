package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class GIFSourceReaderFactory extends SourceReaderFactory<GIFSourceReader>
{
    @Override
    public GIFSourceReader getReader()
    {
        return new GIFSourceReader();
    }

    @Override
    public String getDescription()
    {
        return GIFSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return GIFSourceReader.getAcceptedExtensions();
    }
}
