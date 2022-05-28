package atomicJ.readers.nanoscope;

import atomicJ.readers.SourceReaderFactory;

public class NanoscopeImageReaderFactory extends SourceReaderFactory<NanoscopeImageReader>
{
    @Override
    public NanoscopeImageReader getReader() 
    {
        return new NanoscopeImageReader();
    }

    @Override
    public String getDescription()
    {
        return NanoscopeImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return NanoscopeImageReader.getAcceptedExtensions();
    }
}
