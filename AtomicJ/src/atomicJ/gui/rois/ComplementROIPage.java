package atomicJ.gui.rois;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import atomicJ.gui.AbstractWizardPage;
import atomicJ.gui.SubPanel;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionPanel;
import atomicJ.sources.IdentityTag;

public class ComplementROIPage extends AbstractWizardPage implements PropertyChangeListener
{   
    private static final String IDENTIFIER = "ROI complement";

    private final JCheckBox boxDeleteOriginalROIs = new JCheckBox("Delete original ROIs");

    private final MultipleSelectionPanel<IdentityTag, MultipleSelectionModel<IdentityTag>> complementROIsPanel = new MultipleSelectionPanel<>();
    private final JPanel view;
    private ComplementROIModel<?> model;

    public ComplementROIPage()
    {
        this.view = buildView();
        initItemListener();
    }

    private JPanel buildView()
    {
        SubPanel view = new SubPanel();

        SubPanel panelComplementROIsFull = new SubPanel();

        panelComplementROIsFull.addComponent(complementROIsPanel.getView(), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);
        panelComplementROIsFull.addComponent(complementROIsPanel.getControls(), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.5, 0);

        JLabel labelMainROI = new JLabel("ROI to complement");

        view.addComponent(labelMainROI, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        view.addComponent(panelComplementROIsFull, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        SubPanel panelSettings = new SubPanel();
        panelSettings.addComponent(boxDeleteOriginalROIs, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        view.addComponent(panelSettings, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        return view;
    }

    public void setModel(ComplementROIModel<?> modelNew)
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        modelNew.addPropertyChangeListener(this);

        complementROIsPanel.setModel(modelNew.getROIsToComplementModel());

        pullModelProperties();
    }

    private void pullModelProperties()
    {
        boolean deleteOriginalROIs = model.isDeleteOriginalROIs();
        boxDeleteOriginalROIs.setSelected(deleteOriginalROIs);
    }

    private void initItemListener()
    {
        boxDeleteOriginalROIs.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setDeleteOriginalROIs(selected);
            }
        });
    }

    @Override
    public String getTaskName() 
    {
        return model.getTaskName();
    }

    @Override
    public String getTaskDescription() 
    {
        return model.getTaskDescription();
    }

    @Override
    public String getIdentifier() 
    {
        return IDENTIFIER;
    }

    @Override
    public boolean isFirst() 
    {
        return model.isFirst();
    }

    @Override
    public boolean isLast() 
    {
        return model.isLast();
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return model.isNecessaryInputProvided();
    }

    @Override
    public Component getView() 
    {
        return view;
    }

    @Override
    public Component getControls() {
        return new JPanel();
    }

    @Override
    public boolean isBackEnabled() 
    {
        return model.isBackEnabled();
    }

    @Override
    public boolean isNextEnabled() 
    {
        return model.isNextEnabled();
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return model.isBackEnabled();
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return model.isFinishEnabled();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();

        if(ComplementROIModel.DELETE_ORIGINAL_ROIS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDeleteOriginalROIs.isSelected();

            if(valOld != valNew)
            {
                boxDeleteOriginalROIs.setSelected(valNew);
            }
        }
    }
}
