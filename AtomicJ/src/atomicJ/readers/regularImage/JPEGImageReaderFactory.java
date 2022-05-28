package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class JPEGImageReaderFactory extends SourceReaderFactory<JPEGImageReader>
{
    @Override
    public JPEGImageReader getReader() 
    {
        return new JPEGImageReader();
    }

    @Override
    public String getDescription()
    {
        return JPEGImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return JPEGImageReader.getAcceptedExtensions();
    }
}
