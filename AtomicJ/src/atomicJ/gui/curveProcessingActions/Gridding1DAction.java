package atomicJ.gui.curveProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.curveProcessing.Gridding1DDialog;
import atomicJ.curveProcessing.Gridding1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;


public class Gridding1DAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public Gridding1DAction(ResourceView<R, Channel1D, String> manager) 
    {
        this.manager = manager;

        putValue(NAME, "Gridding");
        putValue(SHORT_DESCRIPTION, "Gridding");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Gridding1DDialog dialog = new Gridding1DDialog(manager.getAssociatedWindow(), "Gridding", true);
        Gridding1DModel<R> model = new Gridding1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}