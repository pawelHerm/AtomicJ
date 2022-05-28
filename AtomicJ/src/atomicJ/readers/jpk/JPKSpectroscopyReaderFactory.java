package atomicJ.readers.jpk;

import atomicJ.readers.SourceReaderFactory;

public class JPKSpectroscopyReaderFactory extends SourceReaderFactory<JPKSpectroscopyReader>
{
    @Override
    public JPKSpectroscopyReader getReader() 
    {
        return new JPKSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return JPKSpectroscopyReader.getDescription();
    }  

    @Override
    public String[] getAcceptedExtensions()
    {
        return JPKSpectroscopyReader.getAcceptedExtensions();
    }
}
