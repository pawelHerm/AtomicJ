package atomicJ.readers.jpk;

import atomicJ.readers.SourceReaderFactory;

public class JPKSourceReaderFactory extends SourceReaderFactory<JPKSourceReader>
{
    @Override
    public JPKSourceReader getReader() 
    {
        return new JPKSourceReader();
    }

    @Override
    public String getDescription()
    {
        return JPKSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return JPKSourceReader.getAcceptedExtensions();
    }
}
