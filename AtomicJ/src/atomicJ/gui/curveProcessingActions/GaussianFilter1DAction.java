package atomicJ.gui.curveProcessingActions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import atomicJ.curveProcessing.GaussianBasedFilter1DModel;
import atomicJ.curveProcessing.GaussianFilter1DDialog;
import atomicJ.curveProcessing.GaussianFilter1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public class GaussianFilter1DAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public GaussianFilter1DAction(ResourceView<R, Channel1D, String> manager)
    {
        this.manager = manager;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/SmoothGaussian.png"));

        putValue(LARGE_ICON_KEY, icon);
        putValue(SHORT_DESCRIPTION, "Gaussian smoothing");
        putValue(NAME, "Gaussian");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        GaussianFilter1DDialog dialog = new GaussianFilter1DDialog(manager.getAssociatedWindow(), "Gaussian", true);
        GaussianBasedFilter1DModel<R> model = new GaussianFilter1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}