package atomicJ.gui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class SpectroscopyResultPanel extends MultipleXYChartPanel<ChannelChart<?>>
{
    private static final long serialVersionUID = 1L;

    private final Action jumpToResultsAction = new JumpToResultsAction();
    private final Action markOnMapAction = new MarkOnMapAction();
    private final Action showRawDataAction = new ShowRawDataAction();

    private SpectroscopyGraphsSupervisor spectroscopySupervisor;

    public SpectroscopyResultPanel(boolean addPopup)
    {
        this(addPopup, true);
    }

    public SpectroscopyResultPanel(boolean addPopup, boolean allowROIbasedActions)
    {
        this(addPopup, allowROIbasedActions, true);
    }

    public SpectroscopyResultPanel(boolean addPopup, boolean allowROIbasedActions, boolean allowStaticsGroupActions)
    {
        super(null, false);

        if(addPopup)
        {
            setPopupMenu(buildDenistyPanelPopupMenu(true, true, true, true, true));
        }
    }

    public void setSpectroscopyGraphSupervisor(SpectroscopyGraphsSupervisor spectroscopySupervisor)
    {
        this.spectroscopySupervisor = spectroscopySupervisor;
    }

    protected final JPopupMenu buildDenistyPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) 
    {
        JPopupMenu popupMenu = super.createPopupMenu(properties, copy, save, print, zoom);

        JMenuItem jumpToResultsItem = new JMenuItem(jumpToResultsAction);
        JMenuItem markOnMapItem = new JMenuItem(markOnMapAction);
        JMenuItem showRawDataItem = new JMenuItem(showRawDataAction);

        popupMenu.add(showRawDataItem);
        popupMenu.add(jumpToResultsItem);
        popupMenu.add(markOnMapItem);

        return popupMenu;
    }

    public void setMapPositionMarkingEnabled(boolean enabled)
    {
        markOnMapAction.setEnabled(enabled);
    }

    public static class SpectroscopyPanelFactory implements AbstractChartPanelFactory<SpectroscopyResultPanel>
    {
        private static final SpectroscopyPanelFactory INSTANCE = new SpectroscopyPanelFactory();

        public static SpectroscopyPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public SpectroscopyResultPanel buildEmptyPanel() 
        {
            return new SpectroscopyResultPanel(true);
        }       
    }

    private class JumpToResultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public JumpToResultsAction() 
        {           
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Jump to result");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(spectroscopySupervisor != null)
            {
                spectroscopySupervisor.jumpToResults();
            }
        }
    }

    private class ShowRawDataAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowRawDataAction() 
        {           
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(spectroscopySupervisor != null)
            {
                spectroscopySupervisor.showRawResourceData();
            }
        }
    }


    private class MarkOnMapAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public MarkOnMapAction() 
        {           
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Mark on map");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(spectroscopySupervisor != null)
            {
                spectroscopySupervisor.markSourcePosition();
            }
        }
    }


}
