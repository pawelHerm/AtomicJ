package atomicJ.readers.jpk;

import atomicJ.readers.SourceReaderFactory;

public class JPKQIImageReaderFactory  extends SourceReaderFactory<JPKQIImageReader>
{
    @Override
    public JPKQIImageReader getReader() 
    {
        return new JPKQIImageReader();
    }

    @Override
    public String getDescription()
    {
        return JPKQIImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return JPKQIImageReader.getAcceptedExtensions();
    }
}

