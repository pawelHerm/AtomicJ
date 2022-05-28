package atomicJ.readers.text;

import atomicJ.readers.SourceReaderFactory;

public class CSVSpectroscopyReaderFactory extends SourceReaderFactory<CSVSpectroscopyReader>
{
    @Override
    public CSVSpectroscopyReader getReader() 
    {
        return new CSVSpectroscopyReader();
    }

    @Override
    public String getDescription()
    {
        return CSVSpectroscopyReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return CSVSpectroscopyReader.getAcceptedExtensions();
    }
}
