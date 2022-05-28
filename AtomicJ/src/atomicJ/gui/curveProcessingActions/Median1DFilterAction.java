package atomicJ.gui.curveProcessingActions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import atomicJ.curveProcessing.MedianFilter1DDialog;
import atomicJ.curveProcessing.MedianFilter1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;

public class Median1DFilterAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public Median1DFilterAction(ResourceView<R, Channel1D, String> manager)
    {
        this.manager = manager;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/SmoothMedian.png"));

        putValue(LARGE_ICON_KEY, icon);
        putValue(SHORT_DESCRIPTION, "Median smoothing");
        putValue(NAME, "Median");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        MedianFilter1DDialog dialog = new MedianFilter1DDialog(manager.getAssociatedWindow(), "Median filter", true);
        MedianFilter1DModel<R> model = new MedianFilter1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}