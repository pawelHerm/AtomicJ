package atomicJ.gui;

import org.jfree.util.ObjectUtilities;

import atomicJ.data.ModificationConstraint1D;

public class DataDrawingMouseInputMode  implements MouseInputMode
{
    private final Object datasetGroupTagId;

    private final ModificationConstraint1D modificationConstraint;
    private final int maxPointCount;

    public DataDrawingMouseInputMode(Object datasetGroupTag, ModificationConstraint1D modificationConstraint, int maxPointCount)
    {
        this.datasetGroupTagId = datasetGroupTag;
        this.modificationConstraint = modificationConstraint;
        this.maxPointCount = maxPointCount;
    }

    public Object getDatasetGroupTag()
    {
        return datasetGroupTagId;
    }

    public ModificationConstraint1D getModificationConstraint()
    {
        return modificationConstraint;
    }

    public int getMaxDataPointCount()
    {
        return maxPointCount;
    }

    @Override
    public boolean isROI() {
        return false;
    }

    @Override
    public boolean isMeasurement() {
        return false;
    }

    @Override
    public boolean isProfile() {
        return false;
    }

    @Override
    public boolean isMoveDataItems(Object key)
    {
        return false;
    }

    @Override
    public boolean isMoveDataItems(Object key, DataModificationType movementType)
    {
        return false;
    }

    @Override
    public boolean isDrawDataset(Object datasetGroupTagId) 
    {
        return ObjectUtilities.equal(this.datasetGroupTagId, datasetGroupTagId);
    }
}
