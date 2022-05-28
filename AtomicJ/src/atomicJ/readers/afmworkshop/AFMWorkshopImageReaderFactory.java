package atomicJ.readers.afmworkshop;

import atomicJ.readers.SourceReaderFactory;

public class AFMWorkshopImageReaderFactory extends SourceReaderFactory<AFMWorkshopImageReader>
{
    @Override
    public AFMWorkshopImageReader getReader() 
    {
        return new AFMWorkshopImageReader();
    }

    @Override
    public String getDescription()
    {
        return AFMWorkshopImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return AFMWorkshopImageReader.getAcceptedExtensions();
    }
}
