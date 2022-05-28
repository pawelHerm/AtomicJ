package atomicJ.gui.results;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import atomicJ.gui.SubPanel;

public class RecalculateResultsDialog <E extends RecalculateResultsModel<?,?>> extends BatchBasedDialog<E>
{
    private static final long serialVersionUID = 1L;

    private boolean initDeleteOldCurveCharts;
    private boolean initDeleteOldNumericalResults;

    private final JCheckBox boxDeleteOldCurveCharts = new JCheckBox("Delete old curve charts");
    private final JCheckBox boxDeleteOldNumericalResults = new JCheckBox("Delete old calculations");

    public RecalculateResultsDialog(Window parent, boolean temporary) 
    {
        super(parent, "Recalculate", ModalityType.MODELESS, temporary);

        setLayout(new BorderLayout());
        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);     
        add(panelButtons, BorderLayout.SOUTH);      

        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    @Override
    protected void setResultsAvailable(boolean available)
    {
        super.setResultsAvailable(available);

        boxDeleteOldCurveCharts.setEnabled(available);
        boxDeleteOldNumericalResults.setEnabled(available);
    }

    private void initItemListener()
    {
        boxDeleteOldCurveCharts.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setDeleteOldCurveCharts(selected);
            }
        });

        boxDeleteOldNumericalResults.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setDeleteOldNumericalResults(selected);
            }
        });
    }

    private JPanel buildMainPanel()
    {   
        SubPanel mainPanel = new SubPanel();    

        JPanel panelOperationRange = buildPanelROIRelative();

        mainPanel.addComponent(new JLabel("Recalculate: "), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(panelOperationRange, 1, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, .05, 1);

        SubPanel panelSettings = new SubPanel();
        panelSettings.addComponent(boxDeleteOldCurveCharts, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        panelSettings.addComponent(boxDeleteOldNumericalResults, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(panelSettings, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return mainPanel;
    }

    @Override
    protected void pullModelParameters()
    {
        super.pullModelParameters();

        E model = getModel();

        this.initDeleteOldCurveCharts = model.isDeleteOldCurveCharts();
        this.initDeleteOldNumericalResults = model.isDeleteOldNumericalResults();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        E model = getModel();
        model.setDeleteOldCurveCharts(initDeleteOldCurveCharts);
        model.setDeleteOldNumericalResults(initDeleteOldNumericalResults);
    }

    @Override
    protected void resetEditor()
    {           
        super.resetEditor();

        boxDeleteOldCurveCharts.setSelected(initDeleteOldCurveCharts);
        boxDeleteOldNumericalResults.setSelected(initDeleteOldNumericalResults);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(RecalculateResultsModel.DELETE_OLD_CURVE_CHARTS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDeleteOldCurveCharts.isSelected();

            if(valOld != valNew)
            {
                boxDeleteOldCurveCharts.setSelected(valNew);
            }
        }
        else if(RecalculateResultsModel.DELETE_OLD_NUMERICAL_RESULTS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDeleteOldNumericalResults.isSelected();

            if(valOld != valNew)
            {
                boxDeleteOldNumericalResults.setSelected(valNew);
            }
        }
    }
}
