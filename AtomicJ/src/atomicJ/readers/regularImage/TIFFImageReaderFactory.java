package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class TIFFImageReaderFactory extends SourceReaderFactory<TIFFImageReader>
{
    @Override
    public TIFFImageReader getReader() 
    {
        return new TIFFImageReader();
    }

    @Override
    public String getDescription()
    {
        return TIFFImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return TIFFImageReader.getAcceptedExtensions();
    }
}
