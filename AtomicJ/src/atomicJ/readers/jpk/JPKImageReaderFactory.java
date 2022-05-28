package atomicJ.readers.jpk;

import atomicJ.readers.SourceReaderFactory;

public class JPKImageReaderFactory extends SourceReaderFactory<JPKImageReader>
{
    @Override
    public JPKImageReader getReader() 
    {
        return new JPKImageReader();
    }

    @Override
    public String getDescription()
    {
        return JPKImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return JPKImageReader.getAcceptedExtensions();
    }
}
