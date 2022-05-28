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
import atomicJ.gui.WizardPage;
import atomicJ.gui.selection.multiple.MultipleSelectionModel;
import atomicJ.gui.selection.multiple.MultipleSelectionPanel;
import atomicJ.sources.IdentityTag;

public class ModifyObjectsPage extends AbstractWizardPage implements WizardPage, PropertyChangeListener
{   
    private final String identifier;

    private final JCheckBox boxDeleteOriginals = new JCheckBox();
    private final JLabel labelObjects = new JLabel();
    private final JPanel view;

    private final MultipleSelectionPanel<IdentityTag, MultipleSelectionModel<IdentityTag>> selectionPanel = new MultipleSelectionPanel<>();

    private ModifyObjectsModel<?> model;

    public ModifyObjectsPage(String identifier)
    {
        this.identifier = identifier;
        this.view = buildView();
        initItemListener();
    }

    private JPanel buildView()
    {
        SubPanel panelView = new SubPanel();

        SubPanel panelObjectsToModify = new SubPanel();

        panelObjectsToModify.addComponent(selectionPanel.getView(), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);
        panelObjectsToModify.addComponent(selectionPanel.getControls(), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.5, 0);

        panelView.addComponent(labelObjects, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelView.addComponent(panelObjectsToModify, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        SubPanel panelSettings = new SubPanel();
        panelSettings.addComponent(boxDeleteOriginals, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        panelView.addComponent(panelSettings, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        return panelView;
    }

    public void setModel(ModifyObjectsModel<?> modelNew)
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        modelNew.addPropertyChangeListener(this);

        selectionPanel.setModel(modelNew.getMergeObjectsModel());

        pullModelProperties();
    }

    private void pullModelProperties()
    {
        boolean deleteOriginalROIs = model.isDeleteOriginalObjects();
        boxDeleteOriginals.setSelected(deleteOriginalROIs);

        labelObjects.setText(model.getObjectListLabel());
        boxDeleteOriginals.setText(model.getObjectOriginalDeleteLabel());
    }

    private void initItemListener()
    {
        boxDeleteOriginals.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setDeleteOriginalObjects(selected);
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
        return identifier;
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
    public Component getControls() 
    {
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

        if(ModifyObjectsModel.DELETE_ORIGINAL_OBJECTS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxDeleteOriginals.isSelected();

            if(valOld != valNew)
            {
                boxDeleteOriginals.setSelected(valNew);
            }
        }
    }
}
