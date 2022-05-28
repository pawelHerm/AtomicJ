package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class BMPImageReaderFactory extends SourceReaderFactory<BMPImageReader>
{
    @Override
    public BMPImageReader getReader() 
    {
        return new BMPImageReader();
    }

    @Override
    public String getDescription()
    {
        return BMPImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return BMPImageReader.getAcceptedExtensions();
    }
}
