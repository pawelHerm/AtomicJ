package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.RotateImageDialog;
import atomicJ.gui.imageProcessing.RotateImageModel;
import atomicJ.resources.Channel2DResourceView;


public class RotateByArbitraryAngleAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public RotateByArbitraryAngleAction(Channel2DResourceView manager)
    {
        this.manager = manager;
        putValue(NAME, "Rotate");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {         
        RotateImageDialog dialog = new RotateImageDialog(manager.getAssociatedWindow(), "Rotation", true);
        RotateImageModel model = new RotateImageModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}