package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.UnsharpMaskDialog;
import atomicJ.gui.imageProcessing.UnsharpMaskModel;
import atomicJ.resources.Channel2DResourceView;


public class UnsharpMaskAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public UnsharpMaskAction(Channel2DResourceView manager)
    {
        this.manager = manager;
        putValue(NAME, "Unsharp");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        UnsharpMaskDialog dialog = new UnsharpMaskDialog(manager.getAssociatedWindow(), "Unsharp", true);
        UnsharpMaskModel model = new UnsharpMaskModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}