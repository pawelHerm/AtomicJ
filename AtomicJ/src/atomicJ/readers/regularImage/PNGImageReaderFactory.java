package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class PNGImageReaderFactory extends SourceReaderFactory<PNGImageReader>
{
    @Override
    public PNGImageReader getReader() 
    {
        return new PNGImageReader();
    }

    @Override
    public String getDescription()
    {
        return PNGImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return PNGImageReader.getAcceptedExtensions();
    }
}
