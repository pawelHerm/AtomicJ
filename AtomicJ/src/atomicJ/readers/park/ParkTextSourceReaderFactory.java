package atomicJ.readers.park;

import atomicJ.readers.SourceReaderFactory;

public class ParkTextSourceReaderFactory extends SourceReaderFactory<ParkTextSourceReader>
{
    @Override
    public ParkTextSourceReader getReader()
    {
        return new ParkTextSourceReader();
    }   

    @Override
    public String getDescription()
    {
        return ParkTextSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ParkTextSourceReader.getAcceptedExtensions();
    }
}