package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.gui.imageProcessing.PlaneBasedCorrectionModel;
import atomicJ.gui.imageProcessing.PlaneBasedCorrrectionDialog;
import atomicJ.gui.imageProcessing.ReplaceImagePartModel;
import atomicJ.resources.Channel2DResourceView;


public class ReplaceDataAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public ReplaceDataAction(Channel2DResourceView manager) 
    {
        this.manager = manager;

        putValue(NAME, "Replace");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        PlaneBasedCorrrectionDialog dialog = new PlaneBasedCorrrectionDialog(manager.getAssociatedWindow(), "Replace data", true);
        PlaneBasedCorrectionModel model = new ReplaceImagePartModel(manager);

        dialog.showDialog(model);
    }
}