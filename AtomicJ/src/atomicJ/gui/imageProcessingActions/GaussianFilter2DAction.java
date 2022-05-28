package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.GaussianBasedFilter2DModel;
import atomicJ.gui.imageProcessing.GaussianFilter2DDialog;
import atomicJ.gui.imageProcessing.GaussianFilter2DModel;
import atomicJ.resources.Channel2DResourceView;


public class GaussianFilter2DAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public GaussianFilter2DAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        putValue(NAME, "Gaussian");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        GaussianFilter2DDialog dialog = new GaussianFilter2DDialog(manager.getAssociatedWindow(), "Gaussian", true);
        GaussianBasedFilter2DModel model = new GaussianFilter2DModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}