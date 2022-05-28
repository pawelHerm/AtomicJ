package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class ZeissImageReaderFactory extends SourceReaderFactory<ZeissImageReader>
{
    @Override
    public ZeissImageReader getReader() 
    {
        return new ZeissImageReader();
    }

    @Override
    public String getDescription()
    {
        return ZeissImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ZeissImageReader.getAcceptedExtensions();
    }
}
