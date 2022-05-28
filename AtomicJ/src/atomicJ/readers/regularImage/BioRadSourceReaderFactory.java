package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class BioRadSourceReaderFactory extends SourceReaderFactory<BioRadSourceReader>
{
    @Override
    public BioRadSourceReader getReader()
    {
        return new BioRadSourceReader();
    }

    @Override
    public String getDescription()
    {
        return BioRadSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return BioRadSourceReader.getAcceptedExtensions();
    }
}
