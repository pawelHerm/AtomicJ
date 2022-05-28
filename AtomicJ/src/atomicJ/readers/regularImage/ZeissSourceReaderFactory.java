package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class ZeissSourceReaderFactory extends SourceReaderFactory<ZeissSourceReader>
{
    @Override
    public ZeissSourceReader getReader()
    {
        return new ZeissSourceReader();
    }

    @Override
    public String getDescription()
    {
        return ZeissSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ZeissSourceReader.getAcceptedExtensions();
    }
}
