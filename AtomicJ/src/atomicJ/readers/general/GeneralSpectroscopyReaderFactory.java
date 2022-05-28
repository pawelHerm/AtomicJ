package atomicJ.readers.general;

import atomicJ.readers.SourceReaderFactory;

public class GeneralSpectroscopyReaderFactory extends SourceReaderFactory<GeneralSpectroscopyReader>
{
    @Override
    public GeneralSpectroscopyReader getReader()
    {
        return new GeneralSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return GeneralSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return GeneralSpectroscopyReader.getAcceptedExtensions();
    }
}
