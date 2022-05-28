package atomicJ.gui.curveProcessingActions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import atomicJ.curveProcessing.SavitzkyGolayFilter1DDialog;
import atomicJ.curveProcessing.SavitzkyGolayFilter1DModel;
import atomicJ.data.Channel1D;
import atomicJ.resources.ResourceView;
import atomicJ.resources.SpectroscopyResource;


public class SavitzkyGolayFilter1DAction<R extends SpectroscopyResource> extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final ResourceView<R, Channel1D, String> manager;

    public SavitzkyGolayFilter1DAction(ResourceView<R, Channel1D, String> manager)
    {
        this.manager = manager;

        putValue(NAME, "Savitzky - Golay");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        SavitzkyGolayFilter1DDialog dialog = new SavitzkyGolayFilter1DDialog(manager.getAssociatedWindow(), "Savitzky - Golay", true);
        SavitzkyGolayFilter1DModel<R> model = new SavitzkyGolayFilter1DModel<>(manager);

        dialog.showDialog(model);
        dialog.dispose();
    }
}