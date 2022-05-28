package atomicJ.readers.mi;

import atomicJ.readers.SourceReaderFactory;

public class MIImageReaderFactory extends SourceReaderFactory<MIImageReader>
{
    @Override
    public MIImageReader getReader() 
    {
        return new MIImageReader();
    }

    @Override
    public String getDescription()
    {
        return MIImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return MIImageReader.getAcceptedExtensions();
    }
}
