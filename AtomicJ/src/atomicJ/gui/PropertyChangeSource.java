package atomicJ.gui;

import java.beans.PropertyChangeListener;

public interface PropertyChangeSource 
{
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
