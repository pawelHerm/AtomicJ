package atomicJ.readers.park;

import atomicJ.readers.SourceReaderFactory;

public class ParkSourceReaderFactory extends SourceReaderFactory<ParkSourceReader>
{
    @Override
    public ParkSourceReader getReader()
    {
        return new ParkSourceReader();
    }   

    @Override
    public String getDescription()
    {
        return ParkSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ParkSourceReader.getAcceptedExtensions();
    }
}