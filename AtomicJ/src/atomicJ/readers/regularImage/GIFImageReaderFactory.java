package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class GIFImageReaderFactory extends SourceReaderFactory<GIFImageReader>
{
    @Override
    public GIFImageReader getReader() 
    {
        return new GIFImageReader();
    }

    @Override
    public String getDescription()
    {
        return GIFImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return GIFImageReader.getAcceptedExtensions();
    }
}
