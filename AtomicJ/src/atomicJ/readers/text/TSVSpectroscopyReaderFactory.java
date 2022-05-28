package atomicJ.readers.text;

import atomicJ.readers.SourceReaderFactory;

public class TSVSpectroscopyReaderFactory extends SourceReaderFactory<TSVSpectroscopyReader>
{
    @Override
    public TSVSpectroscopyReader getReader()
    {
        return new TSVSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return TSVSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return TSVSpectroscopyReader.getAcceptedExtensions();
    }
}
