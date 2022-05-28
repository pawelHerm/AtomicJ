package atomicJ.gui.curveProcessing;

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
import atomicJ.gui.generalProcessing.OperationDialog;

public class RecalculateMapCurvesDialog extends OperationDialog<RecalculateMapCurvesModel>
{
    private static final long serialVersionUID = 1L;

    private boolean initModifyMapsInPlace;
    private boolean initDeleteOldCurveCharts;
    private boolean initDeleteOldNumericalResults;

    private final JCheckBox boxModifyMapInPlace = new JCheckBox("Modify maps in place");
    private final JCheckBox boxDeleteOldCurveCharts = new JCheckBox("Delete old curve charts");
    private final JCheckBox boxDeleteOldNumericalResults = new JCheckBox("Delete old calculations");

    public RecalculateMapCurvesDialog(Window parent, boolean temporary) 
    {
        super(parent, "Recalculate", temporary);

        setLayout(new BorderLayout());
        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();

        add(mainPanel, BorderLayout.NORTH);     
        add(panelButtons, BorderLayout.SOUTH);      

        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initItemListener()
    {
        boxModifyMapInPlace.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                getModel().setModifyMapsInPlace(selected); 
            }
        });

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
        panelSettings.addComponent(boxModifyMapInPlace, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
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

        RecalculateMapCurvesModel model = getModel();

        this.initModifyMapsInPlace = model.isModifyMapsInPlace();
        this.initDeleteOldCurveCharts = model.isDeleteOldCurveCharts();
        this.initDeleteOldNumericalResults = model.isDeleteOldNumericalResults();
    }

    @Override
    protected void setModelToInitialState()
    {
        super.setModelToInitialState();

        RecalculateMapCurvesModel model = getModel();
        model.setModifyMapsInPlace(initModifyMapsInPlace);
        model.setDeleteOldCurveCharts(initDeleteOldCurveCharts);
        model.setDeleteOldNumericalResults(initDeleteOldNumericalResults);
    }

    @Override
    protected void resetEditor()
    {   
        super.resetEditor();

        boxModifyMapInPlace.setSelected(initModifyMapsInPlace);
        boxDeleteOldCurveCharts.setSelected(initDeleteOldCurveCharts);
        boxDeleteOldNumericalResults.setSelected(initDeleteOldNumericalResults);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        super.propertyChange(evt);

        String property = evt.getPropertyName();

        if(RecalculateMapCurvesModel.MODIFY_MAPS_IN_PLACE.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxModifyMapInPlace.isSelected();

            if(valOld != valNew)
            {
                boxModifyMapInPlace.setSelected(valNew);
            }
        }
        else if(RecalculateMapCurvesModel.DELETE_OLD_CURVE_CHARTS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDeleteOldCurveCharts.isSelected();

            if(valOld != valNew)
            {
                boxDeleteOldCurveCharts.setSelected(valNew);
            }
        }
        else if(RecalculateMapCurvesModel.DELETE_OLD_NUMERICAL_RESULTS.equals(property))
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
