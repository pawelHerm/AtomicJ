package atomicJ.readers.wsxm;

import atomicJ.readers.SourceReaderFactory;

public class WSxMSpectroscopyReaderFactory extends SourceReaderFactory<WSXMSpectroscopyReader>
{
    @Override
    public WSXMSpectroscopyReader getReader() 
    {
        return new WSXMSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return WSXMSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return WSXMSpectroscopyReader.getAcceptedExtensions();
    }
}
