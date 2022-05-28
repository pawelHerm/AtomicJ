package atomicJ.gui;

import java.util.LinkedHashSet;
import java.util.Set;

import atomicJ.utilities.Validation;

public class DataViewSupport
{
    private final Set<DataViewListener> listeners = new LinkedHashSet<>();

    public void addDataViewListener(DataViewListener listener)
    {
        Validation.requireNonNullParameterName(listener, "listener");

        listeners.add(listener);
    }

    public void removeDataViewListener(DataViewListener listener)
    {
        listeners.remove(listener);
    }

    public void fireDataViewVisiblityChanged(boolean visibleNew)
    {
        for(DataViewListener listener : listeners)
        {
            listener.dataViewVisibilityChanged(visibleNew);
        }
    }

    public void fireDataAvailabilityChanged(boolean availableNew)
    {
        for(DataViewListener listener : listeners)
        {
            listener.dataAvailabilityChanged(availableNew);
        }
    }
}