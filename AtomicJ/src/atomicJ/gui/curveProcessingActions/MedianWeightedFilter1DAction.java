package atomicJ.gui.curveProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.curveProcessing.MedianWeighted1DDialog;
import atomicJ.curveProcessing.MedianWeighted1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;


public class MedianWeightedFilter1DAction<R extends SpectroscopyResource> extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public MedianWeightedFilter1DAction(ResourceView<R, Channel1D, String> manager)
    {
        this.manager = manager;

        putValue(NAME, "Weighted median");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        MedianWeighted1DDialog dialog = new MedianWeighted1DDialog(manager.getAssociatedWindow(), "Median filter", true);
        MedianWeighted1DModel<R> model = new MedianWeighted1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}