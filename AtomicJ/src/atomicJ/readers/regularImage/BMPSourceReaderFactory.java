package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class BMPSourceReaderFactory extends SourceReaderFactory<BMPSourceReader>
{
    @Override
    public BMPSourceReader getReader()
    {
        return new BMPSourceReader();
    }

    @Override
    public String getDescription()
    {
        return BMPSourceReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return BMPSourceReader.getAcceptedExtensions();
    }
}
