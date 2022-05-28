package atomicJ.readers.regularImage;

import atomicJ.readers.SourceReaderFactory;

public class BioRadImageReaderFactory extends SourceReaderFactory<BioRadImageReader>
{
    @Override
    public BioRadImageReader getReader() 
    {
        return new BioRadImageReader();
    }

    @Override
    public String getDescription()
    {
        return BioRadImageReader.getDescription();
    }

    @Override
    public String[] getAcceptedExtensions()
    {
        return BioRadImageReader.getAcceptedExtensions();
    }
}
