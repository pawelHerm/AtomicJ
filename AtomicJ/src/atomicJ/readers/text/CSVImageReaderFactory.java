package atomicJ.readers.text;

import atomicJ.readers.SourceReaderFactory;

public class CSVImageReaderFactory extends SourceReaderFactory<CSVImageReader>
{
    @Override
    public CSVImageReader getReader() 
    {
        return new CSVImageReader();
    }

    @Override
    public String getDescription()
    {
        return CSVImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return CSVImageReader.getAcceptedExtensions();
    }
}
