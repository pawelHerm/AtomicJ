package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.gui.imageProcessing.AddPlaneModel;
import atomicJ.gui.imageProcessing.PlaneBasedCorrectionModel;
import atomicJ.gui.imageProcessing.PlaneBasedCorrrectionDialog;
import atomicJ.resources.Channel2DResourceView;


public class AddPlaneAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public AddPlaneAction(Channel2DResourceView manager) 
    {
        this.manager = manager;

        putValue(NAME, "Add function");
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        PlaneBasedCorrrectionDialog dialog = new PlaneBasedCorrrectionDialog(manager.getAssociatedWindow(), "Add function", true);
        PlaneBasedCorrectionModel model = new AddPlaneModel(manager);

        dialog.showDialog(model);
    }
}