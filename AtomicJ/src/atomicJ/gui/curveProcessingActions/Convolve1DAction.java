package atomicJ.gui.curveProcessingActions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import atomicJ.curveProcessing.Convolution1DDialog;
import atomicJ.curveProcessing.Convolution1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;


public class Convolve1DAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public Convolve1DAction(ResourceView<R, Channel1D, String> manager) 
    {
        this.manager = manager;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/convolve1D.png"));
        putValue(LARGE_ICON_KEY, icon);

        putValue(NAME, "Convole");
        putValue(SHORT_DESCRIPTION, "Convole");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Convolution1DDialog dialog = new Convolution1DDialog(manager.getAssociatedWindow(), "Convolve", true);
        Convolution1DModel<R> model = new Convolution1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}