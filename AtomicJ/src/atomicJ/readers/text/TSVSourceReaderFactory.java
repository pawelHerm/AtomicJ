package atomicJ.readers.text;

import atomicJ.readers.SourceReaderFactory;

public class TSVSourceReaderFactory extends SourceReaderFactory<TSVSourceReader>
{
    @Override
    public TSVSourceReader getReader()
    {
        return new TSVSourceReader();
    }   

    @Override
    public String getDescription()
    {
        return TSVSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return TSVSourceReader.getAcceptedExtensions();
    }
}
