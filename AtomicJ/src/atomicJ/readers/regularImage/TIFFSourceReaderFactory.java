package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class TIFFSourceReaderFactory extends SourceReaderFactory<TIFFSourceReader>
{
    @Override
    public TIFFSourceReader getReader()
    {
        return new TIFFSourceReader();
    }

    @Override
    public String getDescription()
    {
        return TIFFSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return TIFFSourceReader.getAcceptedExtensions();
    }
}
