package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.MedianFilter2DDialog;
import atomicJ.gui.imageProcessing.MedianFilter2DModel;
import atomicJ.resources.Channel2DResourceView;


public class Median2DFilterAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public Median2DFilterAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        putValue(NAME, "Median");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        MedianFilter2DDialog dialog = new MedianFilter2DDialog(manager.getAssociatedWindow(), "Median filter", true);
        MedianFilter2DModel model = new MedianFilter2DModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}