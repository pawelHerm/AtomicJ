package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.gui.imageProcessing.MedianWeighted2DDialog;
import atomicJ.gui.imageProcessing.MedianWeighted2DModel;
import atomicJ.resources.Channel2DResourceView;


public class MedianWeightedFilter2DAction extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public MedianWeightedFilter2DAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        putValue(NAME, "Weighted median");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        MedianWeighted2DDialog dialog = new MedianWeighted2DDialog(manager.getAssociatedWindow(), "Median filter", true);
        MedianWeighted2DModel model = new MedianWeighted2DModel(manager);

        dialog.showDialog(model);
    }
}