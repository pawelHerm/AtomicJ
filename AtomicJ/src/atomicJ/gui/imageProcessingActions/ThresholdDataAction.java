package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.ThresholdFunctionModel;
import atomicJ.gui.imageProcessing.ThresholdImageDialog;
import atomicJ.resources.Channel2DResourceView;

public class ThresholdDataAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public ThresholdDataAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        putValue(NAME, "Threshold");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {     
        ThresholdImageDialog dialog = new ThresholdImageDialog(manager.getAssociatedWindow(),"Set data thresholds", true);
        ThresholdFunctionModel model = new ThresholdFunctionModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}