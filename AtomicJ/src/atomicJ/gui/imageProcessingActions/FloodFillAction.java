package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import atomicJ.gui.imageProcessing.FloodFillDialog;
import atomicJ.gui.imageProcessing.FloodFillModel;
import atomicJ.resources.Channel2DResourceView;


public class FloodFillAction extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public FloodFillAction(Channel2DResourceView manager)
    {
        this.manager = manager;
        putValue(NAME, "Fill");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {   
        FloodFillDialog dialog = new FloodFillDialog(manager.getAssociatedWindow(), "Wand fill", true);

        FloodFillModel currentModel = new FloodFillModel(manager);
        dialog.showDialog(currentModel);
    }
}