package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import atomicJ.gui.imageProcessing.ImageMathDialog;
import atomicJ.gui.imageProcessing.ImageMathModel;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.Channel2DResourceView;


public class ImageMathAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public ImageMathAction(Channel2DResourceView manager) 
    {
        this.manager = manager;

        putValue(NAME, "Image math");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        imageMath();
    }

    private void imageMath() 
    {
        Channel2DResource resource = manager.getSelectedResource();
        String type = manager.getSelectedType();

        ImageMathDialog dialog = new ImageMathDialog(manager.getAssociatedWindow(), "Image math", true);
        ImageMathModel model = new ImageMathModel(manager);
        //this requires that the dialog is modal
        dialog.showDialog(model);

        UndoableCommand command = model.getExecutedCommand();
        if(command != null)
        {
            manager.pushCommand(resource, type, command);
        }
        dialog.dispose();

    }
}