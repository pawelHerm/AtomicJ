package atomicJ.readers.nanopuller;

import atomicJ.readers.SourceReaderFactory;

public class NanopullerSourceReaderFactory extends SourceReaderFactory<NanopullerSourceReader>
{
    @Override
    public NanopullerSourceReader getReader() 
    {
        return new NanopullerSourceReader();
    }

    @Override
    public String getDescription()
    {
        return NanopullerSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return NanopullerSourceReader.getAcceptedExtensions();
    }
}
