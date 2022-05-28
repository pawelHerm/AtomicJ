package atomicJ.gui.curveProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.curveProcessing.Translate1DDialog;
import atomicJ.curveProcessing.Translate1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;


public class Translate1DAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public Translate1DAction(ResourceView<R, Channel1D, String> manager)
    {
        this.manager = manager;

        putValue(NAME, "Translate");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Translate1DDialog dialog = new Translate1DDialog(manager.getAssociatedWindow(), "Translate", true);
        Translate1DModel<R> model = new Translate1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}