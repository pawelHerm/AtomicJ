package atomicJ.gui.generalProcessing;

import java.util.ArrayList;
import java.util.List;
import atomicJ.gui.AbstractModel;
import atomicJ.gui.imageProcessingActions.OperationListener;

public abstract class BasicOperationModel extends AbstractModel
{
    public static final String APPLY_ENABLED = "ApplyEnabled";

    private boolean applyEnabled = true;
    private boolean applied;

    private final List<OperationListener> operationListeners = new ArrayList<>();

    public void reset()
    {}

    public void cancel()
    {
        operationFinished();
    }

    public void ok()
    {        
        operationFinished();
        apply();
    }

    public boolean isApplied()
    {
        return applied;
    }

    public void apply()
    {
        this.applied = true;
    }

    public boolean isApplyEnabled() {
        return applyEnabled;
    }

    protected boolean calculateApplyEnabled()
    {
        return true;
    }

    protected void checkIfApplyEnabled()
    {
        boolean applyEnabledNew = calculateApplyEnabled();

        if(this.applyEnabled != applyEnabledNew)
        {
            boolean applyEnabledOld = this.applyEnabled;
            this.applyEnabled = applyEnabledNew;

            firePropertyChange(BasicOperationModel.APPLY_ENABLED, applyEnabledOld, applyEnabledNew);
        }
    }

    public void operationFinished()
    {
        fireOperationFinished();
    }

    public void addOperationListener(OperationListener listener)
    {
        operationListeners.add(listener);
    }

    public void removeOperationListener(OperationListener listener)
    {
        operationListeners.remove(listener);
    }

    /**
     * This method should be called by non-modal dialogs, when they are being closed. When processing is finished and a tool mode is associated with the processing,
     * then the manager should be notified that the tool mode should be switched off. 
     * 
     * FloodFillAction is an example of ProcessingListener. It stores a reference to a dialog, which needs to be informed that the tool mode is no longer necessary.
     */

    protected void fireOperationFinished()
    {
        for(OperationListener listener : operationListeners)
        {
            listener.finished();
        }
    }

    protected void fireProcessingApplied()
    {
        for(OperationListener listener : operationListeners)
        {
            listener.applied();
        }
    }
}
