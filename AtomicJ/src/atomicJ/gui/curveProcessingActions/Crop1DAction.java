package atomicJ.gui.curveProcessingActions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import atomicJ.curveProcessing.Crop1DDialog;
import atomicJ.curveProcessing.Crop1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;


public class Crop1DAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public Crop1DAction(ResourceView<R, Channel1D, String> manager)
    {
        this.manager = manager;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Crop.png"));
        putValue(LARGE_ICON_KEY, icon);

        putValue(SHORT_DESCRIPTION, "Crop");
        putValue(NAME, "Crop");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Crop1DDialog dialog = new Crop1DDialog(manager.getAssociatedWindow(), "Crop", true);
        Crop1DModel<R> model = new Crop1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}