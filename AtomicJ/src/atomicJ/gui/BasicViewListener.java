package atomicJ.gui;

import javax.swing.Action;

public class BasicViewListener implements DataViewListener
{
    private final Action action;

    public BasicViewListener(Action action)
    {
        this.action = action;
    }

    @Override
    public void dataViewVisibilityChanged(boolean visibleNew)
    {
        action.putValue(Action.SELECTED_KEY, visibleNew);            
    }

    @Override
    public void dataAvailabilityChanged(boolean availableNew)
    {         
        action.setEnabled(availableNew);
    }
}