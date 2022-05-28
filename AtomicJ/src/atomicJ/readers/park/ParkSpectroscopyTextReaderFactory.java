package atomicJ.readers.park;

import atomicJ.readers.SourceReaderFactory;

public class ParkSpectroscopyTextReaderFactory extends SourceReaderFactory<ParkSpectroscopyTextReader>
{
    @Override
    public ParkSpectroscopyTextReader getReader()
    {
        return new ParkSpectroscopyTextReader();
    }   

    @Override
    public String getDescription()
    {
        return ParkSpectroscopyTextReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ParkSpectroscopyTextReader.getAcceptedExtensions();
    }
}