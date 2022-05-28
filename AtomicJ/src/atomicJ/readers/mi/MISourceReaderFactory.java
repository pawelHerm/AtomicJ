package atomicJ.readers.mi;

import atomicJ.readers.SourceReaderFactory;

public class MISourceReaderFactory extends SourceReaderFactory<MISourceReader>
{
    @Override
    public MISourceReader getReader()
    {
        return new MISourceReader();
    }

    @Override
    public String getDescription()
    {
        return MISourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return MISourceReader.getAcceptedExtensions();
    }
}
