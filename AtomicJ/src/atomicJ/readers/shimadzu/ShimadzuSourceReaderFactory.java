package atomicJ.readers.shimadzu;

import atomicJ.readers.SourceReaderFactory;

public class ShimadzuSourceReaderFactory extends SourceReaderFactory<ShimadzuSourceReader>
{
    @Override
    public ShimadzuSourceReader getReader()
    {
        return new ShimadzuSourceReader();
    }

    @Override
    public String getDescription()
    {
        return ShimadzuSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ShimadzuSourceReader.getAcceptedExtensions();
    }
}
