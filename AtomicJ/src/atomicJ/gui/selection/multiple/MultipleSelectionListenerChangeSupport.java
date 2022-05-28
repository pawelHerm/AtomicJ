package atomicJ.gui.selection.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MultipleSelectionListenerChangeSupport<E>
{
    private final List<MultipleSelectionListener<E>> listeners = new ArrayList<>();

    public void addSelectionChangeListener(MultipleSelectionListener<E> listener)
    {
        listeners.add(listener);
    }

    public void removeSelectionChangeListener(MultipleSelectionListener<E> listener)
    {
        listeners.remove(listener);
    }

    public void fireKeySelectionChanged(E key, boolean selectedOld, boolean selectedNew)
    {
        for(MultipleSelectionListener<E> l : listeners)
        {
            l.keySelectionChanged(key, selectedOld, selectedNew);
        }
    }

    public void fireAllKeysSelectedChanged(boolean allSelectedOld, boolean allSelectedNew)
    {
        for(MultipleSelectionListener<E> l : listeners)
        {
            l.allKeysSelectedChanged(allSelectedOld, allSelectedNew);
        }
    }

    public void fireAllKeysDeselectedChanged(boolean allDeselectedOld, boolean allDeselectedNew)
    {
        for(MultipleSelectionListener<E> l : listeners)
        {
            l.allKeysDeselectedChanged(allDeselectedOld, allDeselectedNew);
        }
    }

    public void fireKeySetChanged(Set<E> keysOld, Set<E> keysNew)
    {
        for(MultipleSelectionListener<E> l : listeners)
        {
            l.keySetChanged(keysOld, keysNew);
        }
    }
}
