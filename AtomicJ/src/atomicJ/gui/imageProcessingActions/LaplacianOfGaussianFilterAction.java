package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.GaussianBasedFilter2DModel;
import atomicJ.gui.imageProcessing.GaussianFilter2DDialog;
import atomicJ.gui.imageProcessing.LaplacianOfGaussianFilterModel;
import atomicJ.resources.Channel2DResourceView;


public class LaplacianOfGaussianFilterAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public LaplacianOfGaussianFilterAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        putValue(NAME, "LoG");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        GaussianFilter2DDialog dialog = new GaussianFilter2DDialog(manager.getAssociatedWindow(), "Laplacian of Gaussian", true);
        GaussianBasedFilter2DModel model = new LaplacianOfGaussianFilterModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}