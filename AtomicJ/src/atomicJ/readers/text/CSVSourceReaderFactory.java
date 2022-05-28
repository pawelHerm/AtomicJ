package atomicJ.readers.text;

import atomicJ.readers.SourceReaderFactory;

public class CSVSourceReaderFactory extends SourceReaderFactory<CSVSourceReader>
{
    @Override
    public CSVSourceReader getReader() 
    {
        return new CSVSourceReader();
    }

    @Override
    public String getDescription()
    {
        return CSVSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return CSVSourceReader.getAcceptedExtensions();
    }
}
