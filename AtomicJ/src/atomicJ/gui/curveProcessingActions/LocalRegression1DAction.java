package atomicJ.gui.curveProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.curveProcessing.LocalRegression1DDialog;
import atomicJ.curveProcessing.LocalRegression1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;


public class LocalRegression1DAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public LocalRegression1DAction(ResourceView<R, Channel1D, String> manager)
    {
        this.manager = manager;

        putValue(NAME, "Local regression");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        LocalRegression1DDialog dialog = new LocalRegression1DDialog(manager.getAssociatedWindow(), "Local regression", true);
        LocalRegression1DModel<R> model = new LocalRegression1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}