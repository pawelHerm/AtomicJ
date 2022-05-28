package atomicJ.readers.mi;

import atomicJ.readers.SourceReaderFactory;

public class MISpectroscopyReaderFactory extends SourceReaderFactory<MISpectroscopyReader>
{
    @Override
    public MISpectroscopyReader getReader() 
    {
        return new MISpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return MISpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return MISpectroscopyReader.getAcceptedExtensions();
    }
}
