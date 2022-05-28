package atomicJ.readers.wsxm;

import atomicJ.readers.SourceReaderFactory;

public class WSxMImageReaderFactory extends SourceReaderFactory<WSxMImageReader>
{
    @Override
    public WSxMImageReader getReader() 
    {
        return new WSxMImageReader();
    }

    @Override
    public String getDescription()
    {
        return WSxMImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return WSxMImageReader.getAcceptedExtensions();
    }
}
