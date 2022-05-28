package atomicJ.gui;

public interface SelectionListener<R extends Object>
{
    public void selectionChanged(SelectionEvent<? extends R> event);
}