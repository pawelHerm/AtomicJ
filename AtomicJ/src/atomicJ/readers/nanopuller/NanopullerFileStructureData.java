package atomicJ.readers.nanopuller;

import java.io.File;

import atomicJ.readers.nanopuller.NanopullerFileStructureModel.NanopullerFileStructure;

public abstract class NanopullerFileStructureData 
{
    public abstract NanopullerFileStructure getFileStructure(File f);
}
