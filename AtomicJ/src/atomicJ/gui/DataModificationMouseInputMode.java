package atomicJ.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jfree.util.ObjectUtilities;

public class DataModificationMouseInputMode  implements MouseInputMode
{
    private final Set<Object> datasetKeys;
    private final Set<DataModificationType> movementTypes;

    public DataModificationMouseInputMode(Object datasetKey, Set<DataModificationType> movementTypes)
    {
        this.datasetKeys = Collections.singleton(datasetKey);
        this.movementTypes = new HashSet<>(movementTypes);
    }

    public DataModificationMouseInputMode(Collection<Object> datasetKey, Set<DataModificationType> movementTypes)
    {
        this.datasetKeys = new HashSet<>(datasetKey);
        this.movementTypes = new HashSet<>(movementTypes);
    }

    public Set<Object> getDatasetKeys()
    {
        return Collections.unmodifiableSet(datasetKeys);
    }

    public Set<DataModificationType> getMovementTypes()
    {
        return Collections.unmodifiableSet(movementTypes);
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
        return isKeyRecognized(key);
    }

    @Override
    public boolean isMoveDataItems(Object key, DataModificationType movementType)
    {
        return isKeyRecognized(key) && movementTypes.contains(movementType);
    }

    private boolean isKeyRecognized(Object key)    
    {
        boolean recognized = false;
        for(Object datasetKey : datasetKeys)
        {
            recognized = recognized || ObjectUtilities.equal(datasetKey, key);
            if(recognized)break;
        }
        return recognized;
    }

    @Override
    public boolean isDrawDataset(Object datasetGroupTag) 
    {
        return false;
    }
}
