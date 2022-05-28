package atomicJ.readers.general;

import atomicJ.readers.SourceReaderFactory;

public class GeneralSourceReaderFactory extends SourceReaderFactory<GeneralSourceReader>
{
    @Override
    public GeneralSourceReader getReader() 
    {
        return new GeneralSourceReader();
    }

    @Override
    public String getDescription()
    {
        return GeneralSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return GeneralSourceReader.getAcceptedExtensions();
    }
}
