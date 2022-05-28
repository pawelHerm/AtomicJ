package atomicJ.gui;

public interface MouseInputMode
{
    public boolean isROI();
    public boolean isMeasurement();       
    public boolean isProfile();
    public boolean isDrawDataset(Object datasetGroupTag);
    public boolean isMoveDataItems(Object movableDatasetKey); 
    public boolean isMoveDataItems(Object movableDatasetKey, DataModificationType movementType); 
}
