package atomicJ.readers.wsxm;

import atomicJ.readers.SourceReaderFactory;

public class WSxMSourceReaderFactory extends SourceReaderFactory<WSxMSourceReader>
{
    @Override
    public WSxMSourceReader getReader()
    {
        return new WSxMSourceReader();
    }

    @Override
    public String getDescription()
    {
        return WSxMSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return WSxMSourceReader.getAcceptedExtensions();
    }
}
