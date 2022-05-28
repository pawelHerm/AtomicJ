package atomicJ.readers.afmworkshop;

import atomicJ.readers.SourceReaderFactory;

public class AFMWorkshopSpectroscopyReaderFactory extends SourceReaderFactory<AFMWorkshopSpectroscopyReader>
{
    @Override
    public AFMWorkshopSpectroscopyReader getReader() 
    {
        return new AFMWorkshopSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return AFMWorkshopSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return AFMWorkshopSpectroscopyReader.getAcceptedExtensions();
    }
}
