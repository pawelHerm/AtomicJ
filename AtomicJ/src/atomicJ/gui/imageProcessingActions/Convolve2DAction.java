package atomicJ.gui.imageProcessingActions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import atomicJ.gui.imageProcessing.Convolution2DDialog;
import atomicJ.gui.imageProcessing.Convolution2DModel;
import atomicJ.resources.Channel2DResourceView;


public class Convolve2DAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public Convolve2DAction(Channel2DResourceView manager) 
    {
        this.manager = manager;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/convolve.png"));
        putValue(LARGE_ICON_KEY, icon);

        putValue(NAME, "Convole");
        putValue(SHORT_DESCRIPTION, "Convole");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Convolution2DDialog dialog = new Convolution2DDialog(manager.getAssociatedWindow(), "Convolve", true);
        Convolution2DModel model = new Convolution2DModel(manager);

        dialog.showDialog(model);
    }
}