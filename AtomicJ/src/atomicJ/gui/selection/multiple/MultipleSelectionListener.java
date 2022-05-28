package atomicJ.gui.selection.multiple;

import java.util.Set;

public interface MultipleSelectionListener<E>
{
    public void keySelectionChanged(E key, boolean selectedOld, boolean selectedNew);
    public void allKeysSelectedChanged(boolean allSelectedOld, boolean allSelectedNew);
    public void allKeysDeselectedChanged(boolean allDeselectedOld, boolean allDeselectedNew);
    public void keySetChanged(Set<E> keysOld, Set<E> keysNew);
}
