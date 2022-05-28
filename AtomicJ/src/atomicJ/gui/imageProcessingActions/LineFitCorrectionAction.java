package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.LineFitCorrectionDialog;
import atomicJ.gui.imageProcessing.LineFitCorrectionModel;
import atomicJ.resources.Channel2DResourceView;


public class LineFitCorrectionAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public LineFitCorrectionAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        putValue(NAME, "Line fit correction");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {       
        LineFitCorrectionDialog dialog = new LineFitCorrectionDialog(manager.getAssociatedWindow(), "Line fit correction", true);
        LineFitCorrectionModel model = new LineFitCorrectionModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}