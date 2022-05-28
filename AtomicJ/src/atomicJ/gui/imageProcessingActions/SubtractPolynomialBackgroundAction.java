package atomicJ.gui.imageProcessingActions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.PolynomialFit2DModel;
import atomicJ.gui.imageProcessing.PolynomialFit2DDialog;
import atomicJ.resources.Channel2DResourceView;


public class SubtractPolynomialBackgroundAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public SubtractPolynomialBackgroundAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        putValue(NAME, "Remove background");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        PolynomialFit2DDialog dialog = new PolynomialFit2DDialog(manager.getAssociatedWindow(), "Remove background", true);
        PolynomialFit2DModel model = new PolynomialFit2DModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}