package atomicJ.readers.park;

import atomicJ.readers.SourceReaderFactory;

public class ParkSpectroscopyReaderFactory extends SourceReaderFactory<ParkSpectroscopyReader>
{
    @Override
    public ParkSpectroscopyReader getReader()
    {
        return new ParkSpectroscopyReader();
    }   

    @Override
    public String getDescription()
    {
        return ParkSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ParkSpectroscopyReader.getAcceptedExtensions();
    }
}