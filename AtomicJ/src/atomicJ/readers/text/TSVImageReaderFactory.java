package atomicJ.readers.text;

import atomicJ.readers.SourceReaderFactory;

public class TSVImageReaderFactory extends SourceReaderFactory<TSVImageReader>
{
    @Override
    public TSVImageReader getReader()
    {
        return new TSVImageReader();
    }

    @Override
    public String getDescription()
    {
        return TSVImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return TSVImageReader.getAcceptedExtensions();
    }
}
