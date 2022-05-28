package atomicJ.gui.imageProcessingActions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import atomicJ.data.PermissiveChannel2DFilter;
import atomicJ.gui.imageProcessing.Gridding2DDialog;
import atomicJ.gui.imageProcessing.Gridding2DModel;
import atomicJ.resources.Channel2DResourceView;

public class Gridding2DAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DResourceView manager;

    public Gridding2DAction(Channel2DResourceView manager)
    {
        this.manager = manager;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(
                toolkit.getImage("Resources/Smooth.png"));
        putValue(LARGE_ICON_KEY, icon);

        putValue(NAME, "Gridding");
        putValue(SHORT_DESCRIPTION, "Gridding");
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        Gridding2DDialog dialog = new Gridding2DDialog(manager.getAssociatedWindow(), "Gridding", true);
        Gridding2DModel model = new Gridding2DModel(manager, PermissiveChannel2DFilter.getInstance());

        dialog.showDialog(model);
    }
}
