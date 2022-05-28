package atomicJ.readers.nanoscope;

import atomicJ.readers.SourceReaderFactory;

public class NanoscopeSourceReaderFactory extends SourceReaderFactory<NanoscopeSourceReader>
{
    @Override
    public NanoscopeSourceReader getReader() 
    {
        return new NanoscopeSourceReader();
    }

    @Override
    public String getDescription()
    {
        return NanoscopeSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return NanoscopeSourceReader.getAcceptedExtensions();
    }
}
