package atomicJ.readers.afmworkshop;

import atomicJ.readers.SourceReaderFactory;

public class AFMWorkshopSourceReaderFactory extends SourceReaderFactory<AFMWorkshopSourceReader>
{
    @Override
    public AFMWorkshopSourceReader getReader() 
    {
        return new AFMWorkshopSourceReader();
    }

    @Override
    public String getDescription()
    {
        return AFMWorkshopSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return AFMWorkshopSourceReader.getAcceptedExtensions();
    }
}
