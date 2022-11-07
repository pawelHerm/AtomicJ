package atomicJ.readers.shimadzu;

import atomicJ.readers.SourceReaderFactory;

public class ShimadzuSpectroscopyReaderFactory extends SourceReaderFactory<ShimadzuSpectroscopyReader>
{
    @Override
    public ShimadzuSpectroscopyReader getReader() 
    {
        return new ShimadzuSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return ShimadzuSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return ShimadzuSpectroscopyReader.getAcceptedExtensions();
    }
}
