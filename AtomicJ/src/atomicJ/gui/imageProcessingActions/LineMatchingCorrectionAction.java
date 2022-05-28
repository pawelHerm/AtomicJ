package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.LineMatchingCorrectionDialog;
import atomicJ.gui.imageProcessing.LineMatchingCorrectionModel;
import atomicJ.resources.Channel2DResourceView;

public class LineMatchingCorrectionAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public LineMatchingCorrectionAction(Channel2DResourceView manager)
    {
        this.manager = manager;
        putValue(NAME, "Match lines");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {     
        LineMatchingCorrectionDialog dialog = new LineMatchingCorrectionDialog(manager.getAssociatedWindow(), "Line height correction", true);
        LineMatchingCorrectionModel model = new LineMatchingCorrectionModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}