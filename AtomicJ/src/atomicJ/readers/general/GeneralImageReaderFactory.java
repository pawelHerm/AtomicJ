package atomicJ.readers.general;

import atomicJ.readers.SourceReaderFactory;

public class GeneralImageReaderFactory extends SourceReaderFactory<GeneralImageReader>
{
    @Override
    public GeneralImageReader getReader()
    {
        return new GeneralImageReader();
    }

    @Override
    public String getDescription()
    {
        return GeneralImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return GeneralImageReader.getAcceptedExtensions();
    }
}
