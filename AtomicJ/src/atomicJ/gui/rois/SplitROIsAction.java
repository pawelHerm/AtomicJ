package atomicJ.gui.rois;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import atomicJ.gui.Channel2DSupervisor;
import atomicJ.gui.imageProcessingActions.OperationListener;


public class SplitROIsAction extends AbstractAction 
{
    private static final long serialVersionUID = 1L;

    private final Channel2DSupervisor supervisor;
    private SplitROIDialog dialog;
    private SplitROIModel model;
    private final SplitROIModelListener listener = new SplitROIModelListener();

    public SplitROIsAction(Channel2DSupervisor supervisor)
    {
        this.supervisor = supervisor;    

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/SplitROIs.png"));

        putValue(LARGE_ICON_KEY, icon);
        putValue(SHORT_DESCRIPTION, "Split ROIs");

        putValue(NAME, "Split ROIs");
        putValue(SELECTED_KEY, false);
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {              
        boolean selected = (boolean) getValue(SELECTED_KEY);

        if(selected)
        {           
            if(model != null)
            {
                model.removeOperationListener(listener);
            }

            this.model = new SplitROIModel(supervisor);            
            model.addOperationListener(listener);

            dialog = (dialog == null) ? new SplitROIDialog(supervisor.getPublicationSite(), "Split ROIs", true) : dialog;         
            dialog.showDialog(model);    
            supervisor.useMouseInteractiveTool(model.getMouseTool());
        }
        else if(model != null)
        {
            model.operationFinished();
        }
    }

    private class SplitROIModelListener implements OperationListener
    {
        @Override
        public void finished() 
        {      
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void applied() {                
        }
    }
}