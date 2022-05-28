package atomicJ.gui;

import java.util.List;

public class SelectionEvent <E extends Object>
{
    private final Object source;

    private final E selectedItemOld;
    private final E selectedItemNew;

    private final List<E> selectedItems;

    private final int selectedIndexNew;

    private final int[] selectedIndicesNew;

    public SelectionEvent(Object source, E selectedItemOld, E selectedItemNew, List<E> selectedItemsNew, int selectedIndexNew, int[] selectedIndicesNew)
    {
        this.source = source;
        this.selectedItemOld = selectedItemOld;
        this.selectedItemNew = selectedItemNew;
        this.selectedItems = selectedItemsNew;
        this.selectedIndexNew = selectedIndexNew;
        this.selectedIndicesNew = selectedIndicesNew;   
    }

    public Object getSource()
    {
        return source;        
    }

    public E getSelectedItemOld()
    {
        return selectedItemOld;
    }
    public E getSelectedItemNew()
    {
        return selectedItemNew;        
    }
    public List<E> getSelectedItems()
    {
        return selectedItems;
    }
    public int getSelectedIndexNew(){
        return selectedIndexNew;
    }
    public int[] getSelectedIndicesNew(){
        return selectedIndicesNew;
    }
}