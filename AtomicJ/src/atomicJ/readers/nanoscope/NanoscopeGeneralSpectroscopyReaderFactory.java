package atomicJ.readers.nanoscope;

import atomicJ.readers.SourceReaderFactory;

public class NanoscopeGeneralSpectroscopyReaderFactory extends SourceReaderFactory<NanoscopeGeneralSpectroscopyReader>
{
    @Override
    public NanoscopeGeneralSpectroscopyReader getReader()
    {
        return new NanoscopeGeneralSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return NanoscopeGeneralSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return NanoscopeGeneralSpectroscopyReader.getAcceptedExtensions();
    }
}
