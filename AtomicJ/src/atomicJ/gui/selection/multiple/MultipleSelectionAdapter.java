package atomicJ.gui.selection.multiple;

import java.util.Set;

public class MultipleSelectionAdapter<E> implements MultipleSelectionListener<E>
{
    @Override
    public void keySelectionChanged(E key, boolean selectedOld, boolean selectedNew){}
    @Override
    public void allKeysSelectedChanged(boolean allSelectedOld, boolean allSelectedNew){}
    @Override
    public void allKeysDeselectedChanged(boolean allDeselectedOld, boolean allDeselectedNew){}
    @Override
    public void keySetChanged(Set<E> keysOld, Set<E> keysNew){}
}
